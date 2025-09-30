package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.UserTestResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Data Access Layer for user test result operations.
 *
 * This DAL handles all database operations related to user test results,
 * including CRUD operations for individual test results and batch operations
 * for weekly test data.
 *
 * @param postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserTestResultDAL(
    private val postgresClient: PostgresClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserTestResultDAL::class.java)
    }

    /**
     * Retrieves all test results for a user for a specific week.
     *
     * @param keycloakId The user's Keycloak ID
     * @param weekStartTimestamp The start timestamp of the week
     * @return Mono containing list of test results
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_test_results"
    )
    fun getUserTestResultsForWeek(
        keycloakId: String,
        weekStartTimestamp: Instant
    ): Mono<List<UserTestResult>> {
        logger.debug("Retrieving test results for user: $keycloakId, week: $weekStartTimestamp")

        return postgresClient.select(
            "SELECT id, keycloak_id, week_start_timestamp, test_name, status, result_value, created_at, updated_at " +
                "FROM user_test_results " +
                "WHERE keycloak_id = $1 AND week_start_timestamp = $2 " +
                "ORDER BY created_at DESC, test_name",
            keycloakId,
            LocalDateTime.ofInstant(weekStartTimestamp, ZoneOffset.UTC)
        )
    }

    /**
     * Retrieves test results for a user within a date range.
     *
     * @param keycloakId The user's Keycloak ID
     * @param startTimestamp The start timestamp of the range
     * @param endTimestamp The end timestamp of the range
     * @return Mono containing list of test results
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_test_results"
    )
    fun getUserTestResultsInRange(
        keycloakId: String,
        startTimestamp: Instant?,
        endTimestamp: Instant?
    ): Mono<List<UserTestResult>> {
        logger.debug("Retrieving test results for user: $keycloakId, range: $startTimestamp to $endTimestamp")

        return when {
            startTimestamp != null && endTimestamp != null -> {
                postgresClient.select(
                    "SELECT id, keycloak_id, week_start_timestamp, test_name, status, result_value, created_at, updated_at " +
                        "FROM user_test_results " +
                        "WHERE keycloak_id = $1 AND week_start_timestamp >= $2 AND week_start_timestamp <= $3 " +
                        "ORDER BY created_at DESC, test_name",
                    keycloakId,
                    LocalDateTime.ofInstant(startTimestamp, ZoneOffset.UTC),
                    LocalDateTime.ofInstant(endTimestamp, ZoneOffset.UTC)
                )
            }
            else -> {
                postgresClient.select(
                    "SELECT id, keycloak_id, week_start_timestamp, test_name, status, result_value, created_at, updated_at " +
                        "FROM user_test_results " +
                        "WHERE keycloak_id = $1 " +
                        "ORDER BY created_at DESC, test_name",
                    keycloakId
                )
            }
        }
    }

    /**
     * Retrieves a specific test result for a user.
     *
     * @param keycloakId The user's Keycloak ID
     * @param weekStartTimestamp The start timestamp of the week
     * @param testName The test name
     * @return Mono containing the test result
     * @throws NoResultsFoundException when test result doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_test_results"
    )
    fun getUserTestResult(
        keycloakId: String,
        weekStartTimestamp: Instant,
        testName: String
    ): Mono<UserTestResult> {
        logger.debug("Retrieving test result for user: $keycloakId, test: $testName, week: $weekStartTimestamp")

        return postgresClient.selectIndividual(
            "SELECT id, keycloak_id, week_start_timestamp, test_name, status, result_value, created_at, updated_at " +
                "FROM user_test_results " +
                "WHERE keycloak_id = $1 AND week_start_timestamp = $2 AND test_name = $3",
            keycloakId,
            LocalDateTime.ofInstant(weekStartTimestamp, ZoneOffset.UTC),
            testName
        )
    }

    /**
     * Upserts a test result (insert or update).
     *
     * @param testResult The test result to upsert
     * @return Mono containing the upserted test result
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_test_results"
    )
    fun upsertUserTestResult(testResult: UserTestResult): Mono<UserTestResult> {
        logger.debug("Upserting test result for user: ${testResult.keycloakId}, test: ${testResult.testName}")

        return postgresClient.update(
            "INSERT INTO user_test_results (keycloak_id, week_start_timestamp, test_name, status, result_value, created_at, updated_at) " +
                "VALUES ($1, $2, $3, $4, $5, NOW(), NOW()) " +
                "ON CONFLICT (keycloak_id, week_start_timestamp, test_name) " +
                "DO UPDATE SET " +
                "    status = EXCLUDED.status, " +
                "    result_value = EXCLUDED.result_value, " +
                "    updated_at = NOW()",
            testResult.keycloakId,
            LocalDateTime.ofInstant(testResult.weekStartTimestamp, ZoneOffset.UTC),
            testResult.testName,
            testResult.status.name,
            testResult.resultValue
        )
    }

    /**
     * Deletes a test result.
     *
     * @param keycloakId The user's Keycloak ID
     * @param weekStartTimestamp The start timestamp of the week
     * @param testName The test name
     * @return Mono containing the deleted test result
     * @throws NoResultsFoundException when test result doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_test_results"
    )
    fun deleteUserTestResult(
        keycloakId: String,
        weekStartTimestamp: Instant,
        testName: String
    ): Mono<UserTestResult> {
        logger.debug("Deleting test result for user: $keycloakId, test: $testName, week: $weekStartTimestamp")

        return postgresClient.update(
            "DELETE FROM user_test_results " +
                "WHERE keycloak_id = $1 AND week_start_timestamp = $2 AND test_name = $3",
            keycloakId,
            LocalDateTime.ofInstant(weekStartTimestamp, ZoneOffset.UTC),
            testName
        )
    }
}
