package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.UserWeeklyTest
import com.congen.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneOffset

/**
 * Data Access Layer for UserWeeklyTest entity operations.
 *
 * This class provides database operations for the UserWeeklyTest entity in the Congen application.
 * UserWeeklyTest represents the weekly test results for a user.
 *
 * ## UserWeeklyTest Entity
 *
 * UserWeeklyTest represents:
 * - Weekly test results for a user
 * - Tracks vertical jump, HR recovery, and reflex test results
 * - Used for performance tracking and gamification
 *
 * ## Database Operations
 *
 * - **Select by user and week**: Retrieve a specific weekly test
 * - **Select by user and range**: Retrieve weekly tests within a date range
 * - **Insert**: Create new weekly test
 * - **Update**: Update existing weekly test
 * - **Delete**: Remove weekly test
 *
 * ## Validation Rules
 *
 * - Numeric results must be non-negative
 * - Week start timestamp must be a Monday
 * - Keycloak ID must be valid UUID format
 *
 * @param postgresClient Client for database operations
 * @param auditService Service for logging data access operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserWeeklyTestDAL(
    private val postgresClient: PostgresClient,
    private val auditService: AuditService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserWeeklyTestDAL::class.java)
    }

    /**
     * Retrieves all weekly tests for a user within a date range.
     *
     * @param keycloakId The user's Keycloak identifier
     * @param startTimestamp The start timestamp of the range
     * @param endTimestamp The end timestamp of the range
     * @return Mono containing list of weekly tests
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_weekly_test"
    )
    fun selectUserWeeklyTestsInRange(
        keycloakId: String,
        startTimestamp: Instant,
        endTimestamp: Instant
    ): Mono<List<UserWeeklyTest>> {
        logger.debug("Selecting weekly tests for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return auditService.logDataAccess("user_weekly_test", "SELECT_RANGE", keycloakId)
            .then(
                postgresClient.select(
                    """
                    SELECT * FROM user_weekly_test
                    WHERE keycloak_id = $1 AND week_start_timestamp BETWEEN $2 AND $3
                    ORDER BY week_start_timestamp DESC
                    """,
                    keycloakId,
                    startTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime(),
                    endTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime()
                )
            )
    }

    /**
     * Upserts a weekly test for a user (insert or update).
     *
     * @param weeklyTest The weekly test to upsert
     * @return Mono containing the upserted weekly test
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_weekly_test"
    )
    fun upsertUserWeeklyTest(weeklyTest: UserWeeklyTest): Mono<UserWeeklyTest> {
        logger.debug("Upserting weekly test for user: ${weeklyTest.keycloakId}, week: ${weeklyTest.weekStartTimestamp}")

        val now = Instant.now()
        val weeklyTestWithTimestamps =
            weeklyTest.copy(
                createdAt = now,
                updatedAt = now
            )

        return auditService.logDataAccess("user_weekly_test", "UPSERT", weeklyTest.keycloakId)
            .then(
                postgresClient.update<UserWeeklyTest>(
                    """
                    INSERT INTO user_weekly_test (
                        keycloak_id, week_start_timestamp, vertical_jump_status, vertical_jump_result,
                        hr_recovery_status, hr_recovery_result, reflex_status, reflex_result,
                        created_at, updated_at
                    ) VALUES (
                        $1, $2, $3, $4, $5, $6, $7, $8, NOW(), NOW()
                    )
                    ON CONFLICT (keycloak_id, week_start_timestamp) DO UPDATE SET
                        vertical_jump_status = EXCLUDED.vertical_jump_status,
                        vertical_jump_result = EXCLUDED.vertical_jump_result,
                        hr_recovery_status = EXCLUDED.hr_recovery_status,
                        hr_recovery_result = EXCLUDED.hr_recovery_result,
                        reflex_status = EXCLUDED.reflex_status,
                        reflex_result = EXCLUDED.reflex_result,
                        updated_at = NOW()
                    """,
                    weeklyTestWithTimestamps.keycloakId,
                    weeklyTestWithTimestamps.weekStartTimestamp.atZone(ZoneOffset.UTC).toLocalDateTime(),
                    weeklyTestWithTimestamps.verticalJumpStatus.name,
                    weeklyTestWithTimestamps.verticalJumpResult,
                    weeklyTestWithTimestamps.hrRecoveryStatus.name,
                    weeklyTestWithTimestamps.hrRecoveryResult,
                    weeklyTestWithTimestamps.reflexStatus.name,
                    weeklyTestWithTimestamps.reflexResult
                )
            )
    }
}
