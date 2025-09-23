package com.congen.service

import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserWeeklyTestDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.TestStatus
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserWeeklyTest
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
 * @param userWeeklyTestDAL DAL for weekly test operations
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
    private val userWeeklyTestDAL: UserWeeklyTestDAL,
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
        
        // Create default metrics with minimal values
        val defaultMetrics = UserPerformanceMetrics(
            keycloakId = keycloakId,
            vo2Max = 30.0,
            strain = 0.0,
            recovery = 50.0,
            hrv = 30.0,
            sleepScore = 50.0,
            remSleepMinutes = 90.0,
            deepSleepMinutes = 60.0,
            subjectiveTiredness = 3,
            createdAt = now,
            updatedAt = now
        )
        
        // Create default weekly test with pending status
        val weekStart = getCurrentWeekStart()
        val defaultWeeklyTest = UserWeeklyTest(
            keycloakId = keycloakId,
            weekStartTimestamp = weekStart,
            verticalJumpStatus = TestStatus.PENDING,
            verticalJumpResult = null,
            hrRecoveryStatus = TestStatus.PENDING,
            hrRecoveryResult = null,
            reflexStatus = TestStatus.PENDING,
            reflexResult = null,
            createdAt = now,
            updatedAt = now
        )
        
        // Save all default data and calculate scores
        return userPerformanceMetricsDAL.upsertUserPerformanceMetrics(defaultMetrics)
            .then(userWeeklyTestDAL.upsertUserWeeklyTest(defaultWeeklyTest))
            .then(Mono.fromCallable { 
                performanceScoringService.calculatePerformanceScores(defaultMetrics, defaultWeeklyTest)
            })
            .flatMap { defaultScores ->
                userPerformanceScoresDAL.upsertUserPerformanceScores(defaultScores)
                    .then(Mono.just(defaultScores))
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
        
        val defaultMetrics = UserPerformanceMetrics(
            keycloakId = keycloakId,
            vo2Max = 30.0,
            strain = 0.0,
            recovery = 50.0,
            hrv = 30.0,
            sleepScore = 50.0,
            remSleepMinutes = 90.0,
            deepSleepMinutes = 60.0,
            subjectiveTiredness = 3,
            createdAt = now,
            updatedAt = now
        )
        
        return userPerformanceMetricsDAL.upsertUserPerformanceMetrics(defaultMetrics)
            .then(Mono.just(defaultMetrics))
    }


    /**
     * Retrieves weekly tests for the current user, optionally within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp Optional start timestamp of the range
     * @param endTimestamp Optional end timestamp of the range
     * @return Mono containing list of weekly tests
     */
    fun getWeeklyTests(keycloakId: String, startTimestamp: Instant? = null, endTimestamp: Instant? = null): Mono<List<UserWeeklyTest>> {
        logger.debug("Retrieving weekly tests for user: $keycloakId, range: $startTimestamp to $endTimestamp")
        
        return when {
            startTimestamp != null && endTimestamp != null -> {
                userWeeklyTestDAL.selectUserWeeklyTestsInRange(keycloakId, startTimestamp, endTimestamp)
            }
            else -> {
                // Get all weekly tests for the user (no date range)
                userWeeklyTestDAL.selectUserWeeklyTestsInRange(keycloakId, Instant.MIN, Instant.MAX)
            }
        }
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
     * @param weeklyTest The weekly test results to submit
     * @return Mono containing the updated weekly test
     */
    fun submitWeeklyTest(weeklyTest: UserWeeklyTest): Mono<UserWeeklyTest> {
        logger.debug("Submitting weekly test for user: ${weeklyTest.keycloakId}, week: ${weeklyTest.weekStartTimestamp}")
        
        // Validate week start timestamp is a Monday
        val weekStartLocalDate = weeklyTest.weekStartTimestamp.atZone(java.time.ZoneOffset.UTC).toLocalDate()
        if (weekStartLocalDate.dayOfWeek.value != 1) {
            return Mono.error(ValidationException("Week start date must be a Monday"))
        }
        
        // Upsert weekly test (insert or update)
        return userWeeklyTestDAL.upsertUserWeeklyTest(weeklyTest)
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
}
