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
     * Retrieves performance scores for a user by their Keycloak ID.
     *
     * @param keycloakId The user's Keycloak identifier
     * @return Mono containing the performance scores, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_performance_scores"
    )
    fun selectUserPerformanceScores(keycloakId: String): Mono<UserPerformanceScores> {
        logger.debug("Selecting performance scores for user: $keycloakId")
        
        return auditService.logDataAccess("user_performance_scores", "SELECT", keycloakId)
            .then(
                postgresClient.selectIndividual(
                    "SELECT * FROM user_performance_scores WHERE keycloak_id = $1",
                    keycloakId
                )
            )
    }

    /**
     * Upserts performance scores for a user (insert or update).
     *
     * @param scores The performance scores to upsert
     * @return Mono containing the upserted performance scores
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_performance_scores"
    )
    fun upsertUserPerformanceScores(scores: UserPerformanceScores): Mono<UserPerformanceScores> {
        logger.debug("Upserting performance scores for user: ${scores.keycloakId}")

        val now = Instant.now()
        val scoresWithTimestamps = scores.copy(
            createdAt = now,
            updatedAt = now
        )

        return auditService.logDataAccess("user_performance_scores", "UPSERT", scores.keycloakId)
            .then(
                postgresClient.update(
                    """
                    INSERT INTO user_performance_scores (
                        keycloak_id, explosiveness_score, aerobic_capacity_score,
                        recovery_score, reaction_time_score,
                        level, hp, hp_loss, mp, mp_loss, fatigue, fatigue_loss, skills, created_at, updated_at
                    ) VALUES (
                        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, NOW(), NOW()
                    )
                    ON CONFLICT (keycloak_id) DO UPDATE SET
                        explosiveness_score = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.explosiveness_score 
                            ELSE user_performance_scores.explosiveness_score 
                        END,
                        aerobic_capacity_score = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.aerobic_capacity_score 
                            ELSE user_performance_scores.aerobic_capacity_score 
                        END,
                        recovery_score = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.recovery_score 
                            ELSE user_performance_scores.recovery_score 
                        END,
                        reaction_time_score = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.reaction_time_score 
                            ELSE user_performance_scores.reaction_time_score 
                        END,
                        level = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.level 
                            ELSE user_performance_scores.level 
                        END,
                        hp = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.hp 
                            ELSE user_performance_scores.hp 
                        END,
                        hp_loss = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.hp_loss 
                            ELSE user_performance_scores.hp_loss 
                        END,
                        mp = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.mp 
                            ELSE user_performance_scores.mp 
                        END,
                        mp_loss = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.mp_loss 
                            ELSE user_performance_scores.mp_loss 
                        END,
                        fatigue = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.fatigue 
                            ELSE user_performance_scores.fatigue 
                        END,
                        fatigue_loss = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.fatigue_loss 
                            ELSE user_performance_scores.fatigue_loss 
                        END,
                        skills = CASE 
                            WHEN DATE(user_performance_scores.created_at) = DATE(NOW()) 
                            THEN EXCLUDED.skills 
                            ELSE user_performance_scores.skills 
                        END,
                        updated_at = NOW()
                    """,
                    scoresWithTimestamps.keycloakId,
                    scoresWithTimestamps.explosivenessScore,
                    scoresWithTimestamps.aerobicCapacityScore,
                    scoresWithTimestamps.recoveryScore,
                    scoresWithTimestamps.reactionTimeScore,
                    scoresWithTimestamps.level,
                    scoresWithTimestamps.hp,
                    scoresWithTimestamps.hpLoss,
                    scoresWithTimestamps.mp,
                    scoresWithTimestamps.mpLoss,
                    scoresWithTimestamps.fatigue,
                    scoresWithTimestamps.fatigueLoss,
                    scoresWithTimestamps.skills.toTypedArray()
                )
            )
    }
}
