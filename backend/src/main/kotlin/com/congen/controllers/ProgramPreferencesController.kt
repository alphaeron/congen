package com.congen.controllers

import com.congen.dal.ProgramPreferencesDAL
import com.congen.model.ProgramPreferences
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for ProgramPreferences entity operations.
 *
 * This controller provides operations for program preferences in the Congen API.
 * Program preferences allow programs to specify their workout frequency, duration,
 * and other program-related settings.
 *
 * ## ProgramPreferences Entity
 *
 * Program preferences represent:
 * - Program's workout preferences and settings
 * - Workout frequency, duration, and intensity preferences
 * - Used for personalized program generation
 *
 * ## Endpoints
 *
 * - `GET /program_preferences/{programId}` - Retrieve program preferences
 * - `PATCH /program_preferences/` - Update program preferences
 *
 * ## Error Handling
 *
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @param programPreferencesDAL Data access layer for program preferences operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 * @param programService Service for program operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/program_preferences")
class ProgramPreferencesController(
    private val programPreferencesDAL: ProgramPreferencesDAL,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService,
    private val programService: ProgramService
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ProgramPreferencesController::class.java)
    }

    /**
     * Retrieves program preferences by program ID.
     *
     * This endpoint fetches the program preferences for the specified program,
     * returning the workout program settings and preferences. Access is restricted
     * to program owners or admin/service users.
     *
     * @param programId The ID of the program to retrieve preferences for
     * @return Mono containing the program preferences
     */
    @GetMapping("/{program_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user program preferences by program ID",
        description = "Retrieves user program preferences for a given program.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User program preferences found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun get(
        @Parameter(description = "Program ID", required = true, example = "1")
        @PathVariable("program_id") programId: Long,
    ): Mono<ResponseEntity<ProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService) {
                gdprComplianceService.withUserConsent(currentUserId) {
                    programPreferencesDAL.selectProgramPreferences(programId)
                        .map {
                            logger.debug("Found user program preferences: {}", programId)
                            ResponseEntity.ok(it)
                        }
                        .doOnError { e ->
                            logger.error("Error getting user program preferences: {}", programId, e)
                        }
                }
            } else {
                // For non-admin users, verify they own the program
                programService.selectProgramById(programId)
                    .filter { it.userId == currentUserId }
                    .flatMap {
                        gdprComplianceService.withUserConsent(currentUserId) {
                            programPreferencesDAL.selectProgramPreferences(programId)
                                .map {
                                    logger.debug("Found user program preferences: {}", programId)
                                    ResponseEntity.ok(it)
                                }
                                .doOnError { e ->
                                    logger.error("Error getting user program preferences: {}", programId, e)
                                }
                        }
                    }
                    .switchIfEmpty(
                        Mono.error(AccessDeniedException("Access denied: User can only view preferences for their own programs"))
                    )
            }
        }
    }

    /**
     * Updates existing program preferences.
     *
     * This endpoint modifies only the session time length for the specified program.
     * Program days per week cannot be modified as it would affect workout scheduling.
     *
     * @param programId The ID of the program to update preferences for
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return ResponseEntity containing the updated program preferences
     *
     * @throws ValidationException if attempting to modify program days per week
     * @throws DatabaseException if database operation fails
     */
    @PatchMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update program preferences",
        description = "Updates existing program preferences (session time only).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Program preferences updated successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun update(
        @Parameter(description = "Program ID", required = true, example = "1")
        @RequestParam("program_id") programId: Long,
        @Parameter(description = "Session time length in minutes", required = true)
        @RequestParam("session_time_length_in_minutes") sessionTimeLengthInMinutes: Int,
    ): Mono<ResponseEntity<ProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService) {
                gdprComplianceService.withUserConsent(currentUserId) {
                    logger.info("Updating program preferences: {}", programId)
                    programPreferencesDAL.updateProgramPreferences(programId, sessionTimeLengthInMinutes)
                        .map { ResponseEntity.ok(it) }
                        .doOnError { e ->
                            logger.error("Error updating program preferences: {}", programId, e)
                        }
                }
            } else {
                // For non-admin users, verify they own the program
                programService.selectProgramById(programId)
                    .filter { it.userId == currentUserId }
                    .flatMap {
                        gdprComplianceService.withUserConsent(currentUserId) {
                            logger.info("Updating program preferences: {}", programId)
                            programPreferencesDAL.updateProgramPreferences(programId, sessionTimeLengthInMinutes)
                                .map { ResponseEntity.ok(it) }
                                .doOnError { e ->
                                    logger.error("Error updating program preferences: {}", programId, e)
                                }
                        }
                    }
                    .switchIfEmpty(
                        Mono.error(AccessDeniedException("Access denied: User can only update preferences for their own programs"))
                    )
            }
        }
    }
}
