package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.DataCleanupResult
import com.congen.model.DataRetentionPolicy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for data retention and TTL operations.
 *
 * This class provides database operations for managing data retention policies,
 * executing cleanup operations, and estimating cleanup impact. It handles all
 * direct database interactions for the data retention system.
 *
 * ## Operations
 *
 * - **Policy Management**: CRUD operations for retention policies
 * - **Cleanup Execution**: Running automated and manual cleanup operations
 * - **Impact Estimation**: Calculating cleanup impact before execution
 * - **Direct Queries**: Executing cleanup operations via direct SQL queries
 *
 * @param postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class DataRetentionDAL(
    private val postgresClient: PostgresClient
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(DataRetentionDAL::class.java)
    }

    /**
     * Executes cleanup for all data types based on their retention policies.
     *
     * This method performs cleanup operations directly via SQL queries for
     * all supported data types based on their retention policies.
     *
     * @return Mono containing list of cleanup results by data type
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "data_retention_policy"
    )
    fun executeCleanupExpiredData(): Mono<List<DataCleanupResult>> {
        logger.debug("Executing cleanup for all data types")

        return postgresClient.withTransaction {
            executeAuditLogCleanup()
                .map { auditResult ->
                    listOf(auditResult, DataCleanupResult("CONSENT_RECORDS", 0))
                }
        }
            .doOnSuccess { results ->
                val totalDeleted = results.sumOf { it.count }
                logger.info(
                    "Cleanup completed: {} records deleted across {} data types",
                    totalDeleted,
                    results.size
                )
            }
            .doOnError { error ->
                logger.error("Failed to execute cleanup operations", error)
            }
    }

    /**
     * Executes cleanup for audit logs based on retention policy.
     *
     * @return Mono containing the cleanup result for audit logs
     */
    private fun executeAuditLogCleanup(): Mono<DataCleanupResult> {
        logger.debug("Executing audit log cleanup")

        return postgresClient.selectIndividual<Int>(
            """
            WITH deleted_audit_logs AS (
                DELETE FROM gdpr_audit_log
                WHERE timestamp < (
                    CURRENT_TIMESTAMP - INTERVAL '1 day' * COALESCE(
                        (SELECT retention_period_days FROM data_retention_policy WHERE data_type = 'AUDIT_LOGS'),
                        2555
                    )
                )
                RETURNING id
            )
            SELECT COUNT(*) FROM deleted_audit_logs
            """.trimIndent()
        ).map { count ->
            DataCleanupResult(
                dataType = "AUDIT_LOGS",
                count = count
            )
        }.doOnSuccess { result ->
            logger.info("Cleaned up {} audit log records", result.count)
        }
            .doOnError { error ->
                logger.error("Failed to cleanup audit logs", error)
            }
    }

    /**
     * Retrieves all data retention policies from the database.
     *
     * @return Mono containing list of all retention policies
     */
    @Cacheable(
        ttl = CacheTTL.MEDIUM_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "data_retention_policy"
    )
    fun getAllRetentionPolicies(): Mono<List<DataRetentionPolicy>> {
        logger.debug("Retrieving all retention policies")

        return postgresClient.select<DataRetentionPolicy>("SELECT * FROM data_retention_policy ORDER BY data_type")
            .doOnSuccess { policies ->
                logger.debug("Retrieved {} retention policies", policies.size)
            }
    }

    /**
     * Updates or inserts a data retention policy.
     *
     * This method uses UPSERT logic to either update an existing policy
     * or create a new one if it doesn't exist.
     *
     * @param dataType The type of data this policy applies to
     * @param retentionPeriodDays Number of days to retain the data
     * @param description Optional description of the policy
     * @return Mono containing the updated policy
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "data_retention_policy"
    )
    fun upsertRetentionPolicy(
        dataType: String,
        retentionPeriodDays: Int,
        description: String? = null
    ): Mono<DataRetentionPolicy> {
        logger.debug("Upserting retention policy for {} to {} days", dataType, retentionPeriodDays)

        return postgresClient.update<DataRetentionPolicy>(
            """
            INSERT INTO data_retention_policy (data_type, retention_period_days, description)
            VALUES ($1, $2, $3)
            ON CONFLICT (data_type)
            DO UPDATE SET
                retention_period_days = EXCLUDED.retention_period_days,
                description = EXCLUDED.description
            """.trimIndent(),
            dataType,
            retentionPeriodDays,
            description
        )
            .doOnSuccess {
                logger.info("Successfully updated retention policy for {} to {} days", dataType, retentionPeriodDays)
            }
            .doOnError { error ->
                logger.error("Failed to update retention policy for {}", dataType, error)
            }
    }

    /**
     * Estimates how many audit log records would be deleted.
     *
     * This provides a count of audit log records that exceed the retention period
     * without actually deleting them.
     *
     * @return Mono containing the estimated count
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "audit_log_cleanup_estimate"
    )
    fun estimateAuditLogCleanup(): Mono<Int> {
        logger.debug("Estimating audit log cleanup impact")

        return postgresClient.selectIndividual<Int>(
            """
            SELECT COUNT(*)
            FROM gdpr_audit_log a
            INNER JOIN data_retention_policy p ON p.data_type = 'AUDIT_LOGS'
            WHERE a.timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day' * p.retention_period_days)
            """.trimIndent()
        ).doOnSuccess { count ->
            logger.debug("Estimated audit log cleanup: {} records", count)
        }
    }

    /**
     * Estimates how many consent records would be deleted.
     *
     * This provides a count of consent records that exceed the retention period
     * without actually deleting them.
     *
     * @return Mono containing the estimated count
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "consent_history_cleanup_estimate"
    )
    fun estimateConsentHistoryCleanup(): Mono<Int> {
        logger.debug("Estimating consent history cleanup impact")

        return postgresClient.selectIndividual<Int>(
            """
            SELECT COUNT(*)
            FROM user_consent c
            INNER JOIN data_retention_policy p ON p.data_type = 'CONSENT_RECORDS'
            WHERE c.updated_at < (CURRENT_TIMESTAMP - INTERVAL '1 day' * p.retention_period_days)
            """.trimIndent()
        ).doOnSuccess { count ->
            logger.debug("Estimated consent history cleanup: {} records", count)
        }
    }

    /**
     * Executes cleanup for a specific data type.
     *
     * This method performs cleanup operations directly via SQL queries for
     * the specified data type.
     *
     * @param dataType The type of data to clean up
     * @return Mono containing the cleanup result
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "data_retention_policy"
    )
    fun executeCleanupForDataType(dataType: String): Mono<DataCleanupResult> {
        logger.debug("Executing cleanup for data type: {}", dataType)

        return when (dataType) {
            "AUDIT_LOGS" -> executeAuditLogCleanup()
            "CONSENT_RECORDS" -> Mono.just(DataCleanupResult(dataType, 0)) // No consent history tracking
            else -> {
                logger.warn("Unknown data type for cleanup: {}", dataType)
                Mono.just(DataCleanupResult(dataType, 0))
            }
        }
    }

    /**
     * Retrieves a specific retention policy by data type.
     *
     * @param dataType The data type to get the policy for
     * @return Mono containing the retention policy, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.MEDIUM_TERM,
        keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
        entityName = "data_retention_policy"
    )
    fun getRetentionPolicy(dataType: String): Mono<DataRetentionPolicy> {
        logger.debug("Retrieving retention policy for: {}", dataType)

        return postgresClient.selectIndividual<DataRetentionPolicy>(
            "SELECT * FROM data_retention_policy WHERE data_type = $1",
            dataType
        ).doOnError {
            logger.debug("No retention policy found for: {}", dataType)
        }
            .doOnSuccess { policy ->
                if (policy != null) {
                    logger.debug("Found retention policy for {}: {} days", dataType, policy.retentionPeriodDays)
                }
            }
    }

    /**
     * Deletes a retention policy for a specific data type.
     *
     * @param dataType The data type to remove the policy for
     * @return Mono that completes when policy is deleted
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "data_retention_policy"
    )
    fun deleteRetentionPolicy(dataType: String): Mono<Void> {
        logger.debug("Deleting retention policy for: {}", dataType)

        return postgresClient.updateLiteral(
            "DELETE FROM data_retention_policy WHERE data_type = $1",
            Map::class,
            dataType
        ).then(Mono.empty<Void>())
            .doOnSuccess {
                logger.info("Successfully deleted retention policy for: {}", dataType)
            }
            .doOnError { error ->
                logger.error("Failed to delete retention policy for: {}", dataType, error)
            }
    }
}
