package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.UserPerformanceMetrics
import com.congen.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

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
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the performance metrics, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_metrics"
    )
    fun selectUserPerformanceMetrics(keycloakId: String): Mono<UserPerformanceMetrics> {
        logger.debug("Selecting performance metrics for user: $keycloakId")
        
        return auditService.logDataAccess("user_performance_metrics", "SELECT", keycloakId)
            .then(
                postgresClient.selectIndividual(
                    "SELECT * FROM user_performance_metrics WHERE keycloak_id = $1",
                    keycloakId
                )
            )
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
        val metricsWithTimestamps = metrics.copy(
            createdAt = now,
            updatedAt = now
        )

        return auditService.logDataAccess("user_performance_metrics", "UPSERT", metrics.keycloakId)
            .then(
                postgresClient.update<UserPerformanceMetrics>(
                    """
                    INSERT INTO user_performance_metrics (
                        keycloak_id, vo2_max, strain, recovery, hrv, sleep_score,
                        rem_sleep_minutes, deep_sleep_minutes, subjective_tiredness,
                        created_at, updated_at
                    ) VALUES (
                        $1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW()
                    )
                    ON CONFLICT (keycloak_id) DO UPDATE SET
                        vo2_max = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.vo2_max 
                            ELSE user_performance_metrics.vo2_max 
                        END,
                        strain = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.strain 
                            ELSE user_performance_metrics.strain 
                        END,
                        recovery = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.recovery 
                            ELSE user_performance_metrics.recovery 
                        END,
                        hrv = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.hrv 
                            ELSE user_performance_metrics.hrv 
                        END,
                        sleep_score = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.sleep_score 
                            ELSE user_performance_metrics.sleep_score 
                        END,
                        rem_sleep_minutes = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.rem_sleep_minutes 
                            ELSE user_performance_metrics.rem_sleep_minutes 
                        END,
                        deep_sleep_minutes = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.deep_sleep_minutes 
                            ELSE user_performance_metrics.deep_sleep_minutes 
                        END,
                        subjective_tiredness = CASE 
                            WHEN DATE(user_performance_metrics.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.subjective_tiredness 
                            ELSE user_performance_metrics.subjective_tiredness 
                        END,
                        updated_at = NOW()
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
            )
    }
}
