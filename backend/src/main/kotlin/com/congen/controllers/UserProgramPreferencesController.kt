package com.congen.controllers

import com.congen.dal.UserProgramPreferencesDAL
import com.congen.model.UserProgramPreferences
import com.congen.service.GdprComplianceService
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for UserProgramPreferences entity operations.
 *
 * This controller provides CRUD operations for user program preferences in the Congen API.
 * User program preferences allow users to specify their preferences for workout programs,
 * including workout frequency, duration, and other program-related settings.
 *
 * ## UserProgramPreferences Entity
 *
 * User program preferences represent:
 * - User's workout program preferences and settings
 * - Workout frequency, duration, and intensity preferences
 * - Used for personalized program generation
 *
 * ## Endpoints
 *
 * - `POST /user_program_preferences/` - Create new user program preferences
 * - `GET /user_program_preferences/{userId}` - Retrieve user program preferences
 * - `PATCH /user_program_preferences/` - Update user program preferences
 * - `DELETE /user_program_preferences/{userId}` - Delete user program preferences
 *
 * ## Error Handling
 *
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @param userProgramPreferencesDAL Data access layer for user program preferences operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_program_preferences")
class UserProgramPreferencesController(
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserProgramPreferencesController::class.java)
    }

    /**
     * Creates new user program preferences.
     *
     * This endpoint creates program preferences for a user, allowing them to specify
     * their workout frequency, duration, and other program-related settings.
     *
     * @param userId The Keycloak identifier of the user
     * @param programDaysPerWeek The number of days per week for the program
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return ResponseEntity containing the created user program preferences
     */
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create user program preferences",
        description = "Creates new user program preferences.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User program preferences created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Number of days per week for the program", required = true)
        @RequestParam("program_days_per_week") programDaysPerWeek: Int,
        @Parameter(description = "Session time length in minutes", required = true)
        @RequestParam("session_time_length_in_minutes") sessionTimeLengthInMinutes: Int,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        logger.info("Saving user program preferences: {}", userId)
                        userProgramPreferencesDAL.insertUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only create preferences for themselves"))
            }
        }
    }

    /**
     * Retrieves user program preferences by user ID.
     *
     * This endpoint fetches the program preferences for the specified user,
     * returning their workout program settings and preferences.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing the user program preferences
     */
    @GetMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user program preferences by user ID",
        description = "Retrieves user program preferences for a given user.",
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
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @PathVariable("user_id") userId: String,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        userProgramPreferencesDAL.selectUserProgramPreferences(userId)
                            .map {
                                logger.debug("Found user program preferences: {}", userId)
                                ResponseEntity.ok(it)
                            }
                            .doOnError { e ->
                                logger.error("Error getting user program preferences: {}", userId, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only view their own preferences"))
            }
        }
    }

    /**
     * Updates existing user program preferences.
     *
     * This endpoint modifies the program preferences for a user, allowing them to
     * update their workout frequency, duration, and other program-related settings.
     * Note: Program days per week cannot be changed if the user has existing workouts
     * to prevent day numbering conflicts and maintain program consistency.
     *
     * @param userId The Keycloak identifier of the user
     * @param programDaysPerWeek The number of days per week for the program
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return ResponseEntity containing the updated user program preferences
     *
     * @throws ValidationException if program days per week cannot be changed due to existing workouts
     * @throws DatabaseException if database operation fails
     */
    @PatchMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user program preferences",
        description = "Updates existing user program preferences.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User program preferences updated successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun update(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Number of days per week for the program", required = true)
        @RequestParam("program_days_per_week") programDaysPerWeek: Int,
        @Parameter(description = "Session time length in minutes", required = true)
        @RequestParam("session_time_length_in_minutes") sessionTimeLengthInMinutes: Int,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        logger.info("Updating user program preferences: {}", userId)
                        userProgramPreferencesDAL.updateUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error updating user program preferences: {} - {}", userId, programDaysPerWeek, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only update their own preferences"))
            }
        }
    }

    /**
     * Deletes user program preferences by user ID.
     *
     * This endpoint removes the program preferences for the specified user,
     * effectively resetting their program preferences to default values.
     *
     * @param userId The Keycloak identifier of the user
     * @return ResponseEntity containing the deleted user program preferences
     */
    @DeleteMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete user program preferences by user ID",
        description = "Deletes user program preferences for a given user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User program preferences deleted successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun delete(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @PathVariable("user_id") userId: String,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        logger.info("Deleting user program preferences: {}", userId)
                        userProgramPreferencesDAL.deleteUserProgramPreferences(userId)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error deleting user program preferences: {}", userId, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only delete their own preferences"))
            }
        }
    }
}
