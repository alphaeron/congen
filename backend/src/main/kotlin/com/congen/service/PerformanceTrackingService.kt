package com.congen.service

import com.congen.dal.TestProtocolConfigDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserTestResultDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.TestProtocol
import com.congen.model.TestStatus
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserTestResult
import com.congen.util.KeycloakUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * Service for managing performance tracking and gamified metrics.
 *
 * This service orchestrates the performance tracking system, including metric collection,
 * score calculation, weekly test protocol management, and integration with wearable devices.
 * It provides a unified interface for the gamified fitness tracking features.
 *
 * ## Features
 *
 * - **Performance Metrics Management**: Store and update raw performance data
 * - **Score Calculation**: Calculate HP/MP/Fatigue and athleticism levels
 * - **Weekly Test Protocol**: Track structured testing schedule
 * - **Wearable Integration**: Support for Whoop and Oura data
 * - **Skill Generation**: Auto-generate skills based on performance thresholds
 *
 * ## Workflow
 *
 * 1. User submits performance metrics (manual or wearable)
 * 2. Service calculates normalized scores and HP/MP/Fatigue
 * 3. Weekly tests are tracked and integrated into overall scores
 * 4. Skills are generated based on performance thresholds
 * 5. All data is cached for responsive dashboard updates
 *
 * @param userPerformanceMetricsDAL DAL for performance metrics operations
 * @param userPerformanceScoresDAL DAL for performance scores operations
 * @param userTestResultDAL DAL for test result operations
 * @param testProtocolConfigDAL DAL for test protocol configuration operations
 * @param performanceScoringService Service for score calculations
 * @param keycloakUtil Utility for Keycloak operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class PerformanceTrackingService(
    private val userPerformanceMetricsDAL: UserPerformanceMetricsDAL,
    private val userPerformanceScoresDAL: UserPerformanceScoresDAL,
    private val userTestResultDAL: UserTestResultDAL,
    private val testProtocolConfigDAL: TestProtocolConfigDAL,
    private val performanceScoringService: PerformanceScoringService,
    private val keycloakUtil: KeycloakUtil
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PerformanceTrackingService::class.java)
    }

    /**
     * Retrieves current performance scores for the authenticated user.
     * If no scores exist, creates default scores for new users.
     *
     * @return Mono containing the user's current performance scores
     */
    fun getCurrentPerformanceScores(keycloakId: String): Mono<UserPerformanceScores> {
        logger.debug("Retrieving current performance scores for user: $keycloakId")

        return userPerformanceScoresDAL.selectUserPerformanceScores(keycloakId)
            .onErrorResume { throwable ->
                if (throwable is NoResultsFoundException) {
                    logger.info("No performance scores found for user $keycloakId, creating default scores")
                    createDefaultPerformanceData(keycloakId)
                } else {
                    logger.error("Error retrieving performance scores for user $keycloakId", throwable)
                    Mono.error(throwable)
                }
            }
    }

    /**
     * Retrieves current performance metrics for the authenticated user.
     * If no metrics exist, creates default metrics for new users.
     *
     * @return Mono containing the user's current performance metrics
     */
    fun getCurrentPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        logger.debug("Retrieving current performance metrics for user: $keycloakId")

        return userPerformanceMetricsDAL.selectUserPerformanceMetrics(keycloakId)
            .onErrorResume { throwable ->
                if (throwable is NoResultsFoundException) {
                    logger.info("No performance metrics found for user $keycloakId, creating default metrics")
                    createDefaultPerformanceMetrics(keycloakId)
                } else {
                    logger.error("Error retrieving performance metrics for user $keycloakId", throwable)
                    Mono.error(throwable)
                }
            }
    }

    /**
     * Creates default performance data for new users.
     * This includes both metrics and scores to get them started at Level 1.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the created performance scores
     */
    private fun createDefaultPerformanceData(keycloakId: String): Mono<UserPerformanceScores> {
        logger.info("Creating default performance data for new user: $keycloakId")

        val now = Instant.now()

        // Create default metrics with null values (no defaults to avoid confusion)
        val defaultMetrics =
            UserPerformanceMetrics(
                keycloakId = keycloakId,
                vo2Max = null,
                strain = null,
                recovery = null,
                hrv = null,
                sleepScore = null,
                remSleepMinutes = null,
                deepSleepMinutes = null,
                subjectiveTiredness = null,
                createdAt = now,
                updatedAt = now
            )

        // Create default test results for each test protocol
        val weekStart = getCurrentWeekStart()

        // Save metrics first, then create default test results
        return userPerformanceMetricsDAL.upsertUserPerformanceMetrics(defaultMetrics)
            .then(createDefaultTestResults(keycloakId, weekStart))
            .then(
                Mono.fromCallable {
                    // For new users, we'll create default scores without test results
                    performanceScoringService.calculatePerformanceScores(defaultMetrics, null)
                }
            )
            .flatMap { defaultScores ->
                userPerformanceScoresDAL.upsertUserPerformanceScores(defaultScores)
                    .then(Mono.just(defaultScores))
            }
    }

    /**
     * Creates default test results for all test protocols for a new user.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param weekStartTimestamp The week start timestamp
     * @return Mono containing the number of test results created
     */
    private fun createDefaultTestResults(
        keycloakId: String,
        weekStartTimestamp: Instant
    ): Mono<Int> {
        logger.debug("Creating default test results for user: $keycloakId, week: $weekStartTimestamp")

        return testProtocolConfigDAL.getAllTestProtocols()
            .flatMap { protocols ->
                val testResults =
                    protocols.map { protocol ->
                        UserTestResult(
                            keycloakId = keycloakId,
                            weekStartTimestamp = weekStartTimestamp,
                            testName = protocol.testName,
                            status = TestStatus.PENDING,
                            resultValue = null,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now()
                        )
                    }

                // Upsert all test results
                Mono.fromCallable { testResults }
                    .flatMapMany { results ->
                        reactor.core.publisher.Flux.fromIterable(results)
                            .flatMap { result -> userTestResultDAL.upsertUserTestResult(result) }
                    }
                    .collectList()
                    .map { it.size }
            }
    }

    /**
     * Creates default performance metrics for new users.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the created performance metrics
     */
    private fun createDefaultPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        logger.info("Creating default performance metrics for new user: $keycloakId")

        val now = Instant.now()

        val defaultMetrics =
            UserPerformanceMetrics(
                keycloakId = keycloakId,
                vo2Max = null,
                strain = null,
                recovery = null,
                hrv = null,
                sleepScore = null,
                remSleepMinutes = null,
                deepSleepMinutes = null,
                subjectiveTiredness = null,
                createdAt = now,
                updatedAt = now
            )

        return userPerformanceMetricsDAL.upsertUserPerformanceMetrics(defaultMetrics)
            .then(Mono.just(defaultMetrics))
    }

    /**
     * Retrieves test results for the current user, optionally within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp Optional start timestamp of the range
     * @param endTimestamp Optional end timestamp of the range
     * @return Mono containing list of test results
     */
    fun getWeeklyTests(
        keycloakId: String,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Mono<List<UserTestResult>> {
        logger.debug("Retrieving test results for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return when {
            startTimestamp != null && endTimestamp != null -> {
                // Get test results within the specified date range
                userTestResultDAL.getUserTestResultsInRange(keycloakId, startTimestamp, endTimestamp)
            }
            else -> {
                // Get test results for the current week if no range specified
                userTestResultDAL.getUserTestResultsForWeek(keycloakId, getCurrentWeekStart())
            }
        }
    }

    /**
     * Retrieves performance metrics for the current user within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp The start timestamp of the range
     * @param endTimestamp The end timestamp of the range
     * @return Mono containing list of performance metrics within the range
     */
    fun getPerformanceMetricsInRange(
        keycloakId: String,
        startTimestamp: Instant,
        endTimestamp: Instant
    ): Mono<List<UserPerformanceMetrics>> {
        logger.debug("Retrieving performance metrics for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return userPerformanceMetricsDAL.getUserPerformanceMetricsInRange(keycloakId, startTimestamp, endTimestamp)
    }

    /**
     * Submits or updates performance metrics for the current user.
     *
     * This method handles both manual test inputs and wearable device data.
     * It automatically calculates updated scores and stores them in the database.
     *
     * @param metrics The performance metrics to submit
     * @return Mono containing the updated performance scores
     */
    fun submitPerformanceMetrics(metrics: UserPerformanceMetrics): Mono<UserPerformanceScores> {
        logger.debug("Submitting performance metrics for user: ${metrics.keycloakId}")

        // Upsert metrics (insert or update)
        return userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics)
            .flatMap { updatedMetrics ->
                // Calculate new scores
                Mono.fromCallable {
                    performanceScoringService.calculatePerformanceScores(updatedMetrics)
                }.flatMap { scores ->
                    // Upsert scores (insert or update)
                    userPerformanceScoresDAL.upsertUserPerformanceScores(scores)
                }
            }
    }

    /**
     * Submits weekly test results for the current user.
     *
     * This method handles the structured weekly testing protocol and automatically
     * integrates results into the overall performance tracking system.
     *
     * @param testResults The list of weekly test results to submit
     * @return Mono containing the updated test results
     */
    fun submitWeeklyTest(testResults: List<UserTestResult>): Mono<List<UserTestResult>> {
        if (testResults.isEmpty()) {
            return Mono.error(ValidationException("At least one test result must be provided"))
        }

        val keycloakId = testResults.first().keycloakId
        val weekStartTimestamp = testResults.first().weekStartTimestamp

        logger.debug("Submitting test results for user: $keycloakId, week: $weekStartTimestamp, count: ${testResults.size}")

        // Validate week start timestamp is a Monday
        val weekStartLocalDate = weekStartTimestamp.atZone(java.time.ZoneOffset.UTC).toLocalDate()
        if (weekStartLocalDate.dayOfWeek.value != 1) {
            return Mono.error(ValidationException("Week start date must be a Monday"))
        }

        // Validate all test results are for the same user and week
        val invalidResults =
            testResults.filter {
                it.keycloakId != keycloakId || it.weekStartTimestamp != weekStartTimestamp
            }
        if (invalidResults.isNotEmpty()) {
            return Mono.error(ValidationException("All test results must be for the same user and week"))
        }

        // Upsert all test results
        return reactor.core.publisher.Flux.fromIterable(testResults)
            .flatMap { testResult -> userTestResultDAL.upsertUserTestResult(testResult) }
            .collectList()
    }

    /**
     * Gets the start date of the current week (Monday) in UTC.
     *
     * @return The start date of the current week in UTC
     */
    private fun getCurrentWeekStart(): Instant {
        val now = Instant.now().atZone(java.time.ZoneOffset.UTC)
        val dayOfWeek = now.dayOfWeek.value // 1=Monday, 7=Sunday
        val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1
        val monday = now.minusDays(daysToSubtract.toLong())
        return monday.truncatedTo(java.time.temporal.ChronoUnit.DAYS).toInstant()
    }

    /**
     * Dynamically generates test protocols from UserWeeklyTest model fields using reflection.
     * This ensures the protocols always match the actual data model structure automatically.
     *
     * @return List of test protocols derived from the model
     */
    fun getTestProtocolsFromDatabase(): Mono<List<TestProtocol>> {
        logger.debug("Retrieving test protocols from database configuration")
        return testProtocolConfigDAL.getAllTestProtocols()
            .doOnSuccess { protocols ->
                logger.debug("Retrieved ${protocols.size} test protocols from database")
            }
            .doOnError { error ->
                logger.error("Failed to retrieve test protocols from database", error)
            }
    }
}
