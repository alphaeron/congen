package com.congen.service

import com.congen.dal.DataRetentionDAL
import com.congen.model.DataCleanupResult
import com.congen.model.DataRetentionPolicy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service for automated data retention and cleanup operations.
 *
 * This service implements automated TTL (time-to-live) functionality for GDPR compliance
 * by periodically cleaning up expired audit logs, consent records, and other data
 * according to the configured retention policies.
 *
 * ## Retention Policies
 *
 * Retention periods are configured in the `data_retention_policy` table:
 * - **Audit Logs**: 7 years (default) - GDPR accountability requirements
 * - **Consent Records**: 7 years - Legal requirement to prove consent
 * - **User Profile**: 7 years after account closure
 * - **Exercise Data**: 3 years - Business requirement
 * - **Session Logs**: 1 year - Security monitoring
 *
 * ## Scheduling
 *
 * - **Daily Cleanup**: Runs at 2 AM UTC to minimize impact
 * - **Configurable**: Can be enabled/disabled via `congen.gdpr.data-retention-check-enabled`
 * - **Logged**: All cleanup operations are logged for audit purposes
 *
 * ## GDPR Compliance
 *
 * This service ensures compliance with GDPR Article 5(1)(e) which requires that
 * personal data is "kept in a form which permits identification of data subjects
 * for no longer than is necessary for the purposes for which the personal data are processed."
 *
 * @property dataRetentionDAL Data access layer for retention operations
 * @property auditService Service for logging cleanup operations
 * @property dataRetentionEnabled Whether data retention cleanup is enabled
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class DataRetentionService(
    private val dataRetentionDAL: DataRetentionDAL,
    private val auditService: AuditService,
    @Value("\${congen.gdpr.data-retention-check-enabled}")
    private val dataRetentionEnabled: Boolean
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataRetentionService::class.java)
    }

    /**
     * Scheduled task that runs daily to cleanup expired data.
     *
     * This method runs at 2:00 AM UTC daily to clean up expired audit logs,
     * consent records, and other data according to the retention policies.
     * The timing is chosen to minimize impact on system performance.
     *
     * Cron expression: "0 0 2 * * ?" = At 2:00 AM every day
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    fun performDailyDataCleanup() {
        if (!dataRetentionEnabled) {
            logger.debug("Data retention cleanup is disabled, skipping daily cleanup")
            return
        }

        logger.info("Starting daily data retention cleanup at {}", Instant.now())

        executeCleanupExpiredData()
            .doOnSuccess { results ->
                logger.info("Daily data cleanup completed successfully")
                results.forEach { result ->
                    logger.info("Cleaned up {} records of type: {}", result.count, result.dataType)
                }
            }
            .doOnError { error ->
                logger.error("Daily data cleanup failed", error)
                // Log the failure for audit purposes, using null to indicate system operation
                auditService.logDataOperation(
                    keycloakId = null,
                    operation = "DATA_CLEANUP_FAILED",
                    dataType = "ALL_DATA_TYPES",
                    additionalInfo = "Error: ${error.message}"
                ).subscribe()
            }
            .subscribe()
    }

    /**
     * Manually triggers data cleanup operation.
     *
     * This method can be called programmatically to trigger data cleanup
     * outside of the scheduled time, useful for testing or manual maintenance.
     *
     * @return Mono containing cleanup results
     */
    fun executeCleanupExpiredData(): Mono<List<DataCleanupResult>> {
        if (!dataRetentionEnabled) {
            logger.debug("Data retention cleanup is disabled, returning empty results")
            return Mono.just(emptyList())
        }

        logger.info("Executing data retention cleanup")

        return dataRetentionDAL.executeCleanupExpiredData()
            .flatMap { results ->
                // Log successful cleanup
                val totalDeleted = results.sumOf { it.count }
                // null keycloakId indicates system operation
                auditService.logDataOperation(
                    keycloakId = null,
                    operation = "DATA_RETENTION_CLEANUP",
                    dataType = "ALL_DATA_TYPES",
                    additionalInfo = "Deleted $totalDeleted total records across ${results.size} data types"
                )
                Mono.just(results)
            }
    }

    /**
     * Gets current retention policies for all data types.
     *
     * @return Mono containing list of retention policies
     */
    fun getAllRetentionPolicies(): Mono<List<DataRetentionPolicy>> {
        return dataRetentionDAL.getAllRetentionPolicies()
    }

    /**
     * Updates retention policy for a specific data type.
     *
     * @param dataType The type of data
     * @param retentionPeriodDays New retention period in days
     * @param description Optional description of the policy
     * @return Mono that completes when policy is updated
     */
    fun upsertRetentionPolicy(
        dataType: String,
        retentionPeriodDays: Int,
        description: String? = null
    ): Mono<DataRetentionPolicy> {
        logger.info("Updating retention policy for {} to {} days", dataType, retentionPeriodDays)

        return dataRetentionDAL.upsertRetentionPolicy(dataType, retentionPeriodDays, description)
            .flatMap { policy ->
                // Log the policy change, using null to indicate system operation
                auditService.logDataOperation(
                    keycloakId = null,
                    operation = "RETENTION_POLICY_UPDATE",
                    dataType = dataType,
                    additionalInfo = "Updated retention period to $retentionPeriodDays days"
                )
                Mono.just(policy)
            }
    }

    /**
     * Estimates how much data would be cleaned up without actually deleting it.
     *
     * @param dataType The type of data to estimate cleanup for
     * @return Mono containing the estimated count of records that would be deleted
     */
    fun estimateCleanupImpact(dataType: String): Mono<Int> {
        return when (dataType.uppercase()) {
            "AUDIT_LOGS" -> dataRetentionDAL.estimateAuditLogCleanup()
            "CONSENT_RECORDS" -> dataRetentionDAL.estimateConsentHistoryCleanup()
            else -> Mono.just(0)
        }
    }
}
