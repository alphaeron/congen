package com.congen.controllers

import com.congen.exceptions.ValidationException
import com.congen.model.DataController
import com.congen.model.DataProcessing
import com.congen.model.PrivacyPolicy
import com.congen.model.UserConsent
import com.congen.model.UserDataExport
import com.congen.model.UserRights
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Controller for GDPR compliance and privacy management operations.
 *
 * This controller provides endpoints for users to exercise their GDPR rights
 * including data access, portability, rectification, and erasure. All operations
 * are logged for audit purposes and require proper authentication.
 *
 * ## GDPR Rights Supported
 *
 * - **Right of Access**: Get current consent status
 * - **Right to Data Portability**: Export all personal data
 * - **Right to Object**: Withdraw consent for data processing
 * - **Right to Erasure**: Delete all personal data (right to be forgotten)
 *
 * ## Security
 *
 * - All endpoints require authentication
 * - Users can only access their own data
 * - All operations are logged for audit purposes
 * - Sensitive operations require additional verification
 *
 * @param gdprComplianceService Service for GDPR operations
 * @param keycloakUtil Utility for extracting user information from JWT
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/gdpr")
@Tag(name = "GDPR Compliance", description = "Privacy and data protection operations")
class GdprController(
    private val gdprComplianceService: GdprComplianceService,
    private val keycloakUtil: KeycloakUtil
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GdprController::class.java)
    }

    /**
     * Gives consent for data processing.
     *
     * This endpoint allows users to give their consent for data processing
     * as required by GDPR Article 6. Consent must be freely given, specific,
     * informed, and unambiguous.
     *
     * @param consent Whether consent is given (true) or withdrawn (false)
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Give or withdraw consent for data processing",
        description =
            "Records user consent for data processing under GDPR Article 6. " +
                "Consent must be freely given, specific, informed, and unambiguous."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Consent recorded successfully"),
            ApiResponse(responseCode = "400", description = "Invalid consent value"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun recordConsent(
        @Parameter(
            description = "Whether consent is given (true) or withdrawn (false)",
            required = true,
            example = "true"
        )
        @RequestParam consent: Boolean
    ): Mono<ResponseEntity<UserConsent>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { requestingUserId ->
                logger.info("Recording consent for user: {} - consent: {}", requestingUserId, consent)
                // User can only modify their own consent (implicit check since we use their ID)
                gdprComplianceService.updateUserConsent(requestingUserId, consent)
                    .map { userConsentRecord ->
                        // Return the complete consent record from the database
                        ResponseEntity.ok(userConsentRecord)
                    }
            }
            .doOnError { error ->
                logger.error("Failed to record consent", error)
            }
    }

    /**
     * Checks the current consent status for the authenticated user.
     *
     * This endpoint allows users to check their current consent status
     * for data processing (GDPR Article 7 - Right to withdraw consent).
     *
     * @return ResponseEntity with the current consent status
     */
    @GetMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current consent status",
        description = "Retrieves the current consent status for data processing for the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Consent status retrieved successfully"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getConsentStatus(): Mono<ResponseEntity<UserConsent>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { requestingUserId ->
                logger.debug("Retrieving consent status for user: {}", requestingUserId)
                gdprComplianceService.getUserConsent(requestingUserId)
                    .map { userConsent ->
                        // User can only access their own consent status (implicit check since we use their ID)
                        ResponseEntity.ok(userConsent)
                    }
            }
            .doOnError { error ->
                logger.error("Failed to retrieve consent status", error)
            }
    }

    /**
     * Exports all personal data for the authenticated user.
     *
     * This endpoint implements the GDPR Right to Data Portability (Article 20),
     * allowing users to receive all their personal data in a structured,
     * commonly used, and machine-readable format.
     *
     * @return ResponseEntity containing all user's personal data
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Export all personal data",
        description =
            "Exports all personal data for the authenticated user in compliance with " +
                "GDPR Article 20 (Right to Data Portability). Data is returned in JSON format."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Data exported successfully"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun exportPersonalData(): Mono<ResponseEntity<UserDataExport>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { requestingUserId ->
                logger.info("Exporting personal data for user: {}", requestingUserId)
                gdprComplianceService.exportUserData(requestingUserId)
                    .map { userDataExport ->
                        // Verify the requesting user matches the exported data
                        if (userDataExport.keycloakId != requestingUserId) {
                            logger.warn(
                                "Access denied: User {} attempted to access data for user {}",
                                requestingUserId,
                                userDataExport.keycloakId
                            )
                            throw SecurityException("Access denied: You can only export your own data")
                        }
                        ResponseEntity.ok(userDataExport)
                    }
            }
            .doOnError { error ->
                if (error is SecurityException) {
                    logger.warn("Security violation in data export: {}", error.message)
                } else {
                    logger.error("Failed to export personal data", error)
                }
            }
    }

    /**
     * Deletes all personal data for the authenticated user.
     *
     * This endpoint implements the GDPR Right to Erasure (Article 17),
     * also known as the "right to be forgotten". This permanently deletes
     * all personal data associated with the user.
     *
     * **WARNING**: This operation is irreversible. All user data including
     * profile, preferences, exercise data, and history will be permanently deleted.
     *
     * @param confirmation Required confirmation parameter to prevent accidental deletion
     * @return ResponseEntity indicating success or failure
     */
    @DeleteMapping("/delete_all_data")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete all personal data (Right to be forgotten)",
        description =
            "Permanently deletes all personal data for the authenticated user " +
                "in compliance with GDPR Article 17 (Right to Erasure). " +
                "This operation is irreversible and requires confirmation."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "All data deleted successfully"),
            ApiResponse(responseCode = "400", description = "Missing or invalid confirmation"),
            ApiResponse(responseCode = "401", description = "User not authenticated"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun deleteAllPersonalData(
        @Parameter(
            description = "Confirmation parameter - must be 'DELETE_ALL_MY_DATA' to proceed",
            required = true,
            example = "DELETE_ALL_MY_DATA"
        )
        @RequestParam confirmation: String
    ): Mono<ResponseEntity<Void>> {
        if (confirmation != "DELETE_ALL_MY_DATA") {
            return Mono.error(
                ValidationException("To delete all data, confirmation parameter must be 'DELETE_ALL_MY_DATA'")
            )
        }

        return keycloakUtil.getCurrentUserId()
            .flatMap { requestingUserId ->
                logger.warn("Deleting all personal data for user: {}", requestingUserId)
                // User can only delete their own data (implicit check since we use their ID)
                gdprComplianceService.deleteAllPersonalData(requestingUserId)
                    .thenReturn(
                        ResponseEntity.ok().build<Void>()
                    )
            }
            .doOnError { error ->
                logger.error("Failed to delete all personal data", error)
            }
    }

    /**
     * Provides privacy policy information and data processing details.
     *
     * This endpoint provides transparency about data processing activities
     * as required by GDPR Articles 13 and 14 (Information to be provided).
     *
     * @return ResponseEntity with privacy policy information
     */
    @GetMapping("/privacy_policy")
    @Operation(
        summary = "Get privacy policy and data processing information",
        description =
            "Provides information about data processing activities, " +
                "legal basis, retention periods, and user rights under GDPR."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Privacy policy information retrieved successfully")
        ]
    )
    fun getPrivacyPolicyInfo(): Mono<ResponseEntity<PrivacyPolicy>> {
        val privacyInfo =
            PrivacyPolicy(
                dataController =
                    DataController(
                        name = "Congen Fitness Application",
                        contact = "privacy@congen.com",
                        dpo = "privacy@congen.com"
                    ),
                dataProcessing =
                    DataProcessing(
                        purposes =
                            listOf(
                                "Account creation and user authentication",
                                "Personalized workout generation",
                                "Fitness tracking and progress monitoring",
                                "Exercise preference management",
                                "Physical attributes tracking for personalized recommendations"
                            ),
                        legalBasis =
                            listOf(
                                "Contract performance - Account creation and basic service provision",
                                "Consent - Additional data processing for personalized features",
                                "Legitimate interest - Service improvement and security"
                            ),
                        dataTypes =
                            listOf(
                                "Personal identifiers (name)",
                                "Physical attributes (age, weight, height) - encrypted at rest",
                                "Fitness preferences and exercise data",
                                "Equipment preferences",
                                "Consent records and audit logs"
                            ),
                        retentionPeriods =
                            mapOf(
                                "user_profile" to "7 years after account closure",
                                "physical_attributes" to "7 years after account closure",
                                "exercise_data" to "3 years after last activity",
                                "audit_logs" to "7 years for compliance",
                                "consent_records" to "7 years after withdrawal"
                            )
                    ),
                userRights =
                    UserRights(
                        access = "You can request a complete copy of your personal data",
                        rectification = "You can correct any inaccurate or incomplete data",
                        erasure = "You can request deletion of your personal data (right to be forgotten)",
                        portability = "You can export your data in a structured, machine-readable format",
                        objection = "You can object to the processing of your personal data",
                        complaint = "You can file a complaint with your local data protection authority"
                    ),
                lastUpdated = "2025-09-20T00:00:00Z",
                version = "1.1.0"
            )

        return Mono.just(ResponseEntity.ok(privacyInfo))
    }
}
