package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.UserPerformanceScores
import com.congen.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Data Access Layer for UserPerformanceScores entity operations.
 *
 * This class provides database operations for the UserPerformanceScores entity in the Congen application.
 * UserPerformanceScores represents the calculated performance scores for a user.
 *
 * ## UserPerformanceScores Entity
 *
 * UserPerformanceScores represents:
 * - Calculated performance scores for a user
 * - HP, MP, Fatigue, and Level tracking
 * - Used for gamified performance tracking
 *
 * ## Database Operations
 *
 * - **Select by user**: Retrieve performance scores for a specific user
 * - **Insert**: Create new performance scores
 * - **Update**: Update existing performance scores
 * - **Delete**: Remove performance scores
 *
 * ## Validation Rules
 *
 * - All scores must be between 0 and 100
 * - Level must be between 1 and 100
 * - Keycloak ID must be valid UUID format
 *
 * @param postgresClient Client for database operations
 * @param auditService Service for logging data access operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserPerformanceScoresDAL(
    private val postgresClient: PostgresClient,
    private val auditService: AuditService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserPerformanceScoresDAL::class.java)
    }

    /**
     * Retrieves the latest performance scores for a user.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the latest performance scores
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_scores"
    )
    fun selectUserPerformanceScores(keycloakId: String): Mono<UserPerformanceScores> {
        logger.debug("Selecting latest performance scores for user: $keycloakId")

        return auditService.logDataAccess("user_performance_scores", "SELECT_LATEST", keycloakId)
            .then(
                postgresClient.selectIndividual(
                    "SELECT * FROM user_performance_scores WHERE keycloak_id = $1 ORDER BY created_at DESC LIMIT 1",
                    keycloakId
                )
            )
    }

    /**
     * Retrieves performance scores history for a user within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp Optional start timestamp of the range
     * @param endTimestamp Optional end timestamp of the range
     * @return Mono containing list of performance scores
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_scores"
    )
    fun selectUserPerformanceScoresInRange(
        keycloakId: String,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Mono<List<UserPerformanceScores>> {
        logger.debug("Selecting performance scores history for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return when {
            startTimestamp != null && endTimestamp != null -> {
                auditService.logDataAccess("user_performance_scores", "SELECT_RANGE", keycloakId)
                    .then(
                        postgresClient.select(
                            """
                            SELECT * FROM user_performance_scores
                            WHERE keycloak_id = $1 AND created_at BETWEEN $2 AND $3
                            ORDER BY created_at DESC
                            """,
                            keycloakId,
                            LocalDateTime.ofInstant(startTimestamp, ZoneOffset.UTC),
                            LocalDateTime.ofInstant(endTimestamp, ZoneOffset.UTC)
                        )
                    )
            }
            else -> {
                auditService.logDataAccess("user_performance_scores", "SELECT_ALL", keycloakId)
                    .then(
                        postgresClient.select(
                            "SELECT * FROM user_performance_scores WHERE keycloak_id = $1 ORDER BY created_at DESC",
                            keycloakId
                        )
                    )
            }
        }
    }

    /**
     * Inserts new performance scores for a user (historical tracking).
     * Each score calculation creates a new record to maintain full history.
     *
     * @param scores The performance scores to insert
     * @return Mono containing the inserted performance scores
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_performance_scores"
    )
    fun insertUserPerformanceScores(scores: UserPerformanceScores): Mono<UserPerformanceScores> {
        logger.debug("Inserting performance scores for user: ${scores.keycloakId}")

        val now = Instant.now()
        val scoresWithTimestamps = scores.copy(createdAt = now)

        return auditService.logDataAccess("user_performance_scores", "INSERT", scores.keycloakId)
            .then(
                postgresClient.update(
                    """
                    INSERT INTO user_performance_scores (
                        keycloak_id, explosiveness_score, aerobic_capacity_score, recovery_score,
                        reaction_time_score, mobility_score, level, level_change_reason,
                        hp, hp_loss, mp, mp_loss, fatigue, fatigue_loss, skills, created_at
                    ) VALUES (
                        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16
                    )
                    """,
                    scoresWithTimestamps.keycloakId,
                    scoresWithTimestamps.explosivenessScore,
                    scoresWithTimestamps.aerobicCapacityScore,
                    scoresWithTimestamps.recoveryScore,
                    scoresWithTimestamps.reactionTimeScore,
                    scoresWithTimestamps.mobilityScore,
                    scoresWithTimestamps.level,
                    scoresWithTimestamps.levelChangeReason,
                    scoresWithTimestamps.hp,
                    scoresWithTimestamps.hpLoss,
                    scoresWithTimestamps.mp,
                    scoresWithTimestamps.mpLoss,
                    scoresWithTimestamps.fatigue,
                    scoresWithTimestamps.fatigueLoss,
                    scoresWithTimestamps.skills.toTypedArray(),
                    LocalDateTime.ofInstant(scoresWithTimestamps.createdAt, ZoneOffset.UTC)
                )
            )
            .then(Mono.just(scoresWithTimestamps))
    }
}
