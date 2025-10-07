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
import com.congen.model.UserWeeklyTest
import com.congen.util.KeycloakUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
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
     *
     * @return Mono containing the user's current performance scores
     * @throws NoResultsFoundException if no performance scores exist for the user
     */
    fun getCurrentPerformanceScores(keycloakId: String): Mono<UserPerformanceScores> {
        logger.debug("Retrieving current performance scores for user: $keycloakId")

        return userPerformanceScoresDAL.selectUserPerformanceScores(keycloakId)
            .doOnError { throwable ->
                logger.error("Error retrieving performance scores for user $keycloakId", throwable)
            }
    }

    /**
     * Retrieves historical performance scores for a user within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp Optional start timestamp of the range
     * @param endTimestamp Optional end timestamp of the range
     * @return Mono containing list of historical performance scores
     */
    fun getPerformanceScoresHistory(
        keycloakId: String,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Mono<List<UserPerformanceScores>> {
        logger.debug("Retrieving performance scores history for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return userPerformanceScoresDAL.selectUserPerformanceScoresInRange(keycloakId, startTimestamp, endTimestamp)
            .onErrorResume { throwable ->
                logger.error("Error retrieving performance scores history for user $keycloakId", throwable)
                Mono.error(throwable)
            }
    }

    /**
     * Retrieves current performance metrics for the authenticated user.
     *
     * @return Mono containing the user's current performance metrics
     * @throws NoResultsFoundException if no performance metrics exist for the user
     */
    fun getCurrentPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        logger.debug("Retrieving current performance metrics for user: $keycloakId")

        return userPerformanceMetricsDAL.selectUserPerformanceMetrics(keycloakId)
            .doOnError { throwable ->
                logger.error("Error retrieving performance metrics for user $keycloakId", throwable)
            }
    }

    /**
     * Creates default performance data for new users.
     * This includes both metrics and scores to get them started at Level 1.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the created performance scores
     */
    fun createDefaultPerformanceData(keycloakId: String): Mono<UserPerformanceScores> {
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
            .flatMap {
                createDefaultTestResults(keycloakId, weekStart)
                    .flatMap {
                        // For new users, we'll create default scores without test results
                        performanceScoringService.calculatePerformanceScores(defaultMetrics, null, "account_creation")
                    }
            }
            .flatMap { defaultScores ->
                userPerformanceScoresDAL.insertUserPerformanceScores(defaultScores)
                    .map { defaultScores }
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
                        Flux.fromIterable(results)
                            .flatMap { result -> userTestResultDAL.upsertUserTestResult(result) }
                    }
                    .collectList()
                    .map { it.size }
            }
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
                // Get all test results for the user (not just current week) to ensure we find all data
                userTestResultDAL.getUserTestResultsInRange(keycloakId, null, null)
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

        // Get existing metrics and merge with new ones
        return userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(metrics.keycloakId)
            .map { existingMetrics ->
                // Merge existing metrics with new ones (new values override existing ones)
                existingMetrics.copy(
                    vo2Max = metrics.vo2Max ?: existingMetrics.vo2Max,
                    strain = metrics.strain ?: existingMetrics.strain,
                    recovery = metrics.recovery ?: existingMetrics.recovery,
                    hrv = metrics.hrv ?: existingMetrics.hrv,
                    sleepScore = metrics.sleepScore ?: existingMetrics.sleepScore,
                    remSleepMinutes = metrics.remSleepMinutes ?: existingMetrics.remSleepMinutes,
                    deepSleepMinutes = metrics.deepSleepMinutes ?: existingMetrics.deepSleepMinutes,
                    subjectiveTiredness = metrics.subjectiveTiredness ?: existingMetrics.subjectiveTiredness,
                    updatedAt = Instant.now()
                )
            }
            .onErrorResume { throwable ->
                if (throwable is NoResultsFoundException) {
                    // No existing metrics, use the provided metrics as-is
                    Mono.just(metrics)
                } else {
                    Mono.error(throwable)
                }
            }
            .flatMap { mergedMetrics ->
                // Upsert the merged metrics
                userPerformanceMetricsDAL.upsertUserPerformanceMetrics(mergedMetrics)
            }
            .flatMap { updatedMetrics ->
                // Get weekly test data and calculate new scores
                getWeeklyTests(updatedMetrics.keycloakId)
                    .flatMap { testResults ->
                        // Convert test results to weekly test data for scoring
                        val weeklyTest = convertTestResultsToWeeklyTest(testResults)
                        performanceScoringService.calculatePerformanceScores(updatedMetrics, weeklyTest, "daily_metrics_updated")
                    }.flatMap { scores ->
                        // Insert new scores (historical tracking)
                        userPerformanceScoresDAL.insertUserPerformanceScores(scores)
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
        val weekStartLocalDate = weekStartTimestamp.atZone(ZoneOffset.UTC).toLocalDate()
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
        return Flux.fromIterable(testResults)
            .flatMap { testResult -> userTestResultDAL.upsertUserTestResult(testResult) }
            .collectList()
            .flatMap { updatedTestResults ->
                // Get current daily metrics and calculate new scores
                userPerformanceMetricsDAL.selectUserPerformanceMetrics(keycloakId)
                    .flatMap { dailyMetrics ->
                        // Get ALL test results for the user to ensure we find all data
                        userTestResultDAL.getUserTestResultsInRange(keycloakId, null, null)
                            .flatMap { allTestResults ->
                                // Convert test results to weekly test data for scoring
                                val weeklyTest = convertTestResultsToWeeklyTest(allTestResults)
                                performanceScoringService.calculatePerformanceScores(dailyMetrics, weeklyTest, "weekly_test_updated")
                            }
                    }.flatMap { scores ->
                        // Insert new scores (historical tracking)
                        userPerformanceScoresDAL.insertUserPerformanceScores(scores)
                    }.then(Mono.just(updatedTestResults))
            }
    }

    /**
     * Gets the start date of the current week (Monday) in UTC.
     *
     * @return The start date of the current week in UTC
     */
    private fun getCurrentWeekStart(): Instant {
        val now = Instant.now().atZone(ZoneOffset.UTC)
        val dayOfWeek = now.dayOfWeek.value // 1=Monday, 7=Sunday
        val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1
        val monday = now.minusDays(daysToSubtract.toLong())
        return monday.truncatedTo(ChronoUnit.DAYS).toInstant()
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

    /**
     * Converts a list of UserTestResult to a UserWeeklyTest object for scoring.
     *
     * Implements fallback logic: for each metric, iterates from newest to oldest
     * test results to find the most recent non-null value. If no non-null value
     * is found across all historical results, the metric remains null.
     *
     * @param testResults List of test results for the week (should be ordered newest to oldest)
     * @return UserWeeklyTest object or null if no results
     */
    internal fun convertTestResultsToWeeklyTest(testResults: List<UserTestResult>): UserWeeklyTest? {
        if (testResults.isEmpty()) return null

        val firstResult = testResults.first()
        val keycloakId = firstResult.keycloakId
        val weekStartTimestamp = firstResult.weekStartTimestamp

        // Group test results by test name for efficient lookup
        val testResultsByType = testResults.groupBy { it.testName }

        // Helper function to find the most recent non-null value for a test type
        fun findMostRecentNonNullValue(testName: String): Pair<TestStatus, Double?> {
            val results = testResultsByType[testName]
            if (results == null) {
                return Pair(TestStatus.PENDING, null)
            }

            // Results are already ordered newest to oldest, so find first non-null
            for (result in results) {
                if (result.resultValue != null) {
                    logger.info("Found non-null result for $testName: ${result.resultValue}")
                    return Pair(result.status, result.resultValue)
                }
            }

            // If no non-null value found, return the status from the most recent result
            val mostRecentResult = results.first()
            logger.info("No non-null result found for $testName, returning status: ${mostRecentResult.status}")
            return Pair(mostRecentResult.status, null)
        }

        // Get the most recent non-null values for each test type
        val (verticalJumpStatus, verticalJumpResult) = findMostRecentNonNullValue("vertical_jump")
        val (hrRecoveryStatus, hrRecoveryResult) = findMostRecentNonNullValue("hr_recovery")
        val (reflexStatus, reflexResult) = findMostRecentNonNullValue("reflex")
        val (mobilityStatus, mobilityResult) = findMostRecentNonNullValue("mobility")

        logger.debug(
            "Weekly test fallback results for user $keycloakId: " +
                "vertical_jump=$verticalJumpResult, hr_recovery=$hrRecoveryResult, " +
                "reflex=$reflexResult, mobility=$mobilityResult"
        )

        return UserWeeklyTest(
            keycloakId = keycloakId,
            weekStartTimestamp = weekStartTimestamp,
            verticalJumpStatus = verticalJumpStatus,
            verticalJumpResult = verticalJumpResult,
            hrRecoveryStatus = hrRecoveryStatus,
            hrRecoveryResult = hrRecoveryResult,
            reflexStatus = reflexStatus,
            reflexResult = reflexResult,
            mobilityStatus = mobilityStatus,
            mobilityResult = mobilityResult,
            createdAt = firstResult.createdAt,
            updatedAt = firstResult.updatedAt
        )
    }
}
