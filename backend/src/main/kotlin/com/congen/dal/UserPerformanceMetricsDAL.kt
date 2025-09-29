package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserPerformanceMetrics
import com.congen.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Data Access Layer for UserPerformanceMetrics entity operations.
 *
 * This class provides database operations for the UserPerformanceMetrics entity in the Congen application.
 * UserPerformanceMetrics represents the daily performance metrics for a user.
 *
 * ## UserPerformanceMetrics Entity
 *
 * UserPerformanceMetrics represents:
 * - Daily performance metrics for a user
 * - Wearable device data and subjective metrics
 * - Used for performance tracking and gamification
 *
 * ## Database Operations
 *
 * - **Select by user**: Retrieve performance metrics for a specific user
 * - **Insert**: Create new performance metrics
 * - **Update**: Update existing performance metrics
 * - **Delete**: Remove performance metrics
 *
 * ## Validation Rules
 *
 * - Strain must be between 0 and 21
 * - Recovery and sleep scores must be between 0 and 100
 * - Subjective tiredness must be between 1 and 5
 * - Keycloak ID must be valid UUID format
 *
 * @param postgresClient Client for database operations
 * @param auditService Service for logging data access operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserPerformanceMetricsDAL(
    private val postgresClient: PostgresClient,
    private val auditService: AuditService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserPerformanceMetricsDAL::class.java)
    }

    /**
     * Retrieves performance metrics for a user by their Keycloak ID.
     * This method now returns the latest performance metrics for backward compatibility.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the latest performance metrics, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_metrics"
    )
    fun selectUserPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        return getLatestUserPerformanceMetrics(keycloakId)
    }

    /**
     * Upserts performance metrics for a user (insert or update).
     *
     * @param metrics The performance metrics to upsert
     * @return Mono containing the upserted performance metrics
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_performance_metrics"
    )
    fun upsertUserPerformanceMetrics(metrics: UserPerformanceMetrics): Mono<UserPerformanceMetrics> {
        logger.debug("Upserting performance metrics for user: ${metrics.keycloakId}")

        val now = Instant.now()
        val metricsWithTimestamps =
            metrics.copy(
                createdAt = now,
                updatedAt = now
            )

        return auditService.logDataAccess("user_performance_metrics", "UPSERT", metrics.keycloakId)
            .then(
                // First, check if there's already a record for today
                getLatestUserPerformanceMetrics(metrics.keycloakId)
                    .flatMap { existingMetrics: UserPerformanceMetrics ->
                        val existingDate = existingMetrics.createdAt.atZone(ZoneOffset.UTC).toLocalDate()
                        val newDate = now.atZone(ZoneOffset.UTC).toLocalDate()

                        if (existingDate == newDate) {
                            // Update existing record for today
                            postgresClient.update<UserPerformanceMetrics>(
                                """
                                UPDATE user_performance_metrics SET
                                    vo2_max = $2,
                                    strain = $3,
                                    recovery = $4,
                                    hrv = $5,
                                    sleep_score = $6,
                                    rem_sleep_minutes = $7,
                                    deep_sleep_minutes = $8,
                                    subjective_tiredness = $9,
                                    updated_at = NOW()
                                WHERE keycloak_id = $1 AND DATE(created_at) = DATE(NOW())
                                """,
                                metricsWithTimestamps.keycloakId,
                                metricsWithTimestamps.vo2Max,
                                metricsWithTimestamps.strain,
                                metricsWithTimestamps.recovery,
                                metricsWithTimestamps.hrv,
                                metricsWithTimestamps.sleepScore,
                                metricsWithTimestamps.remSleepMinutes,
                                metricsWithTimestamps.deepSleepMinutes,
                                metricsWithTimestamps.subjectiveTiredness
                            )
                        } else {
                            // Insert new record for today
                            postgresClient.update<UserPerformanceMetrics>(
                                """
                                INSERT INTO user_performance_metrics (
                                    keycloak_id, vo2_max, strain, recovery, hrv, sleep_score,
                                    rem_sleep_minutes, deep_sleep_minutes, subjective_tiredness,
                                    created_at, updated_at
                                ) VALUES (
                                    $1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW()
                                )
                                """,
                                metricsWithTimestamps.keycloakId,
                                metricsWithTimestamps.vo2Max,
                                metricsWithTimestamps.strain,
                                metricsWithTimestamps.recovery,
                                metricsWithTimestamps.hrv,
                                metricsWithTimestamps.sleepScore,
                                metricsWithTimestamps.remSleepMinutes,
                                metricsWithTimestamps.deepSleepMinutes,
                                metricsWithTimestamps.subjectiveTiredness
                            )
                        }
                    }
                    .onErrorResume { throwable: Throwable ->
                        if (throwable is NoResultsFoundException) {
                            // No existing record, insert new one
                            postgresClient.update<UserPerformanceMetrics>(
                                """
                                INSERT INTO user_performance_metrics (
                                    keycloak_id, vo2_max, strain, recovery, hrv, sleep_score,
                                    rem_sleep_minutes, deep_sleep_minutes, subjective_tiredness,
                                    created_at, updated_at
                                ) VALUES (
                                    $1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW()
                                )
                                """,
                                metricsWithTimestamps.keycloakId,
                                metricsWithTimestamps.vo2Max,
                                metricsWithTimestamps.strain,
                                metricsWithTimestamps.recovery,
                                metricsWithTimestamps.hrv,
                                metricsWithTimestamps.sleepScore,
                                metricsWithTimestamps.remSleepMinutes,
                                metricsWithTimestamps.deepSleepMinutes,
                                metricsWithTimestamps.subjectiveTiredness
                            )
                        } else {
                            Mono.error<UserPerformanceMetrics>(throwable)
                        }
                    }
            )
    }

    /**
     * Retrieves performance metrics for a user within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp The start timestamp of the range
     * @param endTimestamp The end timestamp of the range
     * @return Mono containing list of performance metrics within the range
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_metrics"
    )
    fun getUserPerformanceMetricsInRange(
        keycloakId: String,
        startTimestamp: Instant,
        endTimestamp: Instant
    ): Mono<List<UserPerformanceMetrics>> {
        logger.debug("Selecting performance metrics for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return auditService.logDataAccess("user_performance_metrics", "SELECT_RANGE", keycloakId)
            .then(
                postgresClient.select(
                    """
                    SELECT * FROM user_performance_metrics
                    WHERE keycloak_id = $1
                    AND created_at >= $2
                    AND created_at <= $3
                    ORDER BY created_at ASC
                    """,
                    keycloakId,
                    LocalDateTime.ofInstant(startTimestamp, ZoneOffset.UTC),
                    LocalDateTime.ofInstant(endTimestamp, ZoneOffset.UTC)
                )
            )
    }

    /**
     * Retrieves the latest performance metrics for a user.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the latest performance metrics, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_metrics"
    )
    fun getLatestUserPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        logger.debug("Selecting latest performance metrics for user: $keycloakId")

        return auditService.logDataAccess("user_performance_metrics", "SELECT_LATEST", keycloakId)
            .then(
                postgresClient.select<UserPerformanceMetrics>(
                    """
                    SELECT * FROM user_performance_metrics
                    WHERE keycloak_id = $1
                    ORDER BY created_at DESC
                    LIMIT 1
                    """,
                    keycloakId
                )
            )
            .flatMap { metricsList: List<UserPerformanceMetrics> ->
                if (metricsList.isEmpty()) {
                    Mono.error(NoResultsFoundException("No performance metrics found for user: $keycloakId"))
                } else {
                    Mono.just(metricsList.first())
                }
            }
    }
}
