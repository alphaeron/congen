package com.congen.controllers

import com.congen.model.CleanupEstimationResponse
import com.congen.model.CleanupSummary
import com.congen.model.DataRetentionPolicy
import com.congen.model.EstimatedDeletion
import com.congen.model.ManualCleanupResponse
import com.congen.service.DataRetentionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Controller for data retention and TTL management operations.
 *
 * This controller provides endpoints for administrators to manage data retention
 * policies, trigger manual cleanup operations, and monitor TTL compliance.
 * All operations require admin privileges and are logged for audit purposes.
 *
 * ## Data Retention Features
 *
 * - **Policy Management**: View and update retention policies
 * - **Manual Cleanup**: Trigger immediate data cleanup
 * - **Impact Estimation**: Preview what data would be cleaned up
 * - **Monitoring**: View current retention status
 *
 * ## GDPR Compliance
 *
 * These endpoints support GDPR Article 5(1)(e) compliance by ensuring
 * personal data is not kept longer than necessary for processing purposes.
 *
 * @property dataRetentionService Service for data retention operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/data_retention")
@Tag(name = "Data Retention Management", description = "TTL and data cleanup administration")
class DataRetentionController(
    private val dataRetentionService: DataRetentionService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataRetentionController::class.java)
    }

    /**
     * Retrieves all current data retention policies.
     *
     * This endpoint returns the configured retention periods for all data types,
     * allowing administrators to review and understand current TTL settings.
     *
     * @return ResponseEntity containing list of retention policies
     */
    @GetMapping("/policies")
    @PreAuthorize("hasRole('admin')")
    @Operation(
        summary = "Get all data retention policies",
        description = "Retrieves current TTL policies for all data types including audit logs, consent records, etc."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Retention policies retrieved successfully"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "403", description = "Admin access required"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getRetentionPolicies(): Mono<ResponseEntity<List<DataRetentionPolicy>>> {
        logger.debug("Retrieving all data retention policies")

        return dataRetentionService.getAllRetentionPolicies()
            .map { policies ->
                ResponseEntity.ok(policies)
            }
            .doOnError { error ->
                logger.error("Failed to retrieve retention policies", error)
            }
    }

    /**
     * Updates retention policy for a specific data type.
     *
     * This endpoint allows administrators to modify TTL settings for different
     * types of data to ensure GDPR compliance and optimize storage usage.
     *
     * @param policyUpdate Map containing dataType, retentionPeriodDays, and optional description
     * @return ResponseEntity indicating success or failure
     */
    @PutMapping("/policies")
    @PreAuthorize("hasRole('admin')")
    @Operation(
        summary = "Update data retention policy",
        description =
            "Updates the TTL policy for a specific data type. " +
                "Changes take effect on the next scheduled cleanup."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Policy updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid parameters"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "403", description = "Admin access required"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun updateRetentionPolicy(
        @Parameter(
            description = "Data type to update policy for",
            required = true,
            example = "AUDIT_LOGS"
        )
        @RequestParam dataType: String,
        @Parameter(
            description = "Retention period in days",
            required = true,
            example = "2555"
        )
        @RequestParam retentionPeriodDays: Int,
        @Parameter(
            description = "Optional description of the policy",
            required = false,
            example = "Audit logs retained for 7 years for compliance"
        )
        @RequestParam(required = false) description: String?
    ): Mono<ResponseEntity<DataRetentionPolicy>> {
        if (retentionPeriodDays < 1) {
            return Mono.just(
                ResponseEntity.badRequest().build()
            )
        }

        logger.info("Updating retention policy for {} to {} days", dataType, retentionPeriodDays)

        return dataRetentionService.upsertRetentionPolicy(dataType, retentionPeriodDays.toInt(), description)
            .map { policy ->
                ResponseEntity.ok(policy)
            }
            .doOnError { error ->
                logger.error("Failed to update retention policy for {}", dataType, error)
            }
    }

    /**
     * Estimates the impact of data cleanup without performing it.
     *
     * This endpoint allows administrators to preview how many records would
     * be deleted by the cleanup process before actually executing it.
     *
     * @param dataType Optional data type to estimate cleanup for (if not provided, estimates all)
     * @return ResponseEntity containing cleanup impact estimation
     */
    @GetMapping("/cleanup_estimate")
    @PreAuthorize("hasRole('admin')")
    @Operation(
        summary = "Estimate data cleanup impact",
        description =
            "Estimates how many records would be deleted by the cleanup process " +
                "without actually performing the deletion."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cleanup estimate retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid data type"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "403", description = "Admin access required"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun estimateCleanupImpact(
        @Parameter(
            description = "Data type to estimate cleanup for (optional)",
            required = false,
            example = "AUDIT_LOGS"
        )
        @RequestParam(required = false) dataType: String?
    ): Mono<ResponseEntity<CleanupEstimationResponse>> {
        logger.debug("Estimating cleanup impact for data type: {}", dataType ?: "ALL")

        val estimationMono =
            if (dataType != null) {
                // Validate data type
                if (dataType !in listOf("AUDIT_LOGS", "CONSENT_RECORDS")) {
                    return Mono.just(
                        ResponseEntity.badRequest().build()
                    )
                }

                dataRetentionService.estimateCleanupImpact(dataType)
                    .map { count ->
                        CleanupEstimationResponse(
                            estimatedDeletions =
                                listOf(
                                    EstimatedDeletion(
                                        dataType = dataType,
                                        estimatedRecordsToDelete = count
                                    )
                                )
                        )
                    }
            } else {
                // Estimate for all data types
                dataRetentionService.estimateCleanupImpact("AUDIT_LOGS")
                    .zipWith(dataRetentionService.estimateCleanupImpact("CONSENT_RECORDS"))
                    .map { tuple ->
                        CleanupEstimationResponse(
                            estimatedDeletions =
                                listOf(
                                    EstimatedDeletion(dataType = "AUDIT_LOGS", estimatedRecordsToDelete = tuple.t1),
                                    EstimatedDeletion(dataType = "CONSENT_RECORDS", estimatedRecordsToDelete = tuple.t2)
                                )
                        )
                    }
            }

        return estimationMono
            .map { result ->
                ResponseEntity.ok(result)
            }
            .doOnError { error ->
                logger.error("Failed to estimate cleanup impact", error)
            }
    }

    /**
     * Manually triggers data cleanup operation.
     *
     * This endpoint allows administrators to trigger immediate data cleanup
     * outside of the scheduled daily cleanup. Use with caution as this
     * permanently deletes data according to retention policies.
     *
     * @return ResponseEntity containing cleanup results
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('admin')")
    @Operation(
        summary = "Manually trigger data cleanup",
        description =
            "Immediately executes data cleanup according to retention policies. " +
                "This permanently deletes expired data."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cleanup completed successfully"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "403", description = "Admin access required"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun triggerManualCleanup(): Mono<ResponseEntity<ManualCleanupResponse>> {
        logger.warn("Manual data cleanup triggered by admin")

        return dataRetentionService.executeCleanupExpiredData()
            .map { results ->
                val totalDeleted = results.sumOf { it.count }
                ResponseEntity.ok(
                    ManualCleanupResponse(
                        cleanupResults = results,
                        summary =
                            CleanupSummary(
                                totalDeleted = totalDeleted,
                                dataTypesProcessed = results.size,
                                executionTime = Instant.now()
                            )
                    )
                )
            }
            .doOnError { error ->
                logger.error("Manual data cleanup failed", error)
            }
    }
}
