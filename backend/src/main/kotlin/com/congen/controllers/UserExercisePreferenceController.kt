package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.UserExercisePreference
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for UserExercisePreference entity operations.
 *
 * This controller provides CRUD operations for user exercise preferences in the Congen API.
 * User exercise preferences allow users to specify their preferences for specific exercises,
 * including whether they like or dislike certain exercises, which influences workout generation.
 *
 * ## UserExercisePreference Entity
 *
 * A user exercise preference represents:
 * - Association between a user and an exercise
 * - Preference rating (like/dislike)
 * - Used for personalized workout generation
 *
 * ## Endpoints
 *
 * - `POST /user_exercise_preference/` - Create a new user exercise preference
 * - `GET /user_exercise_preference/{userId}` - Retrieve all exercise preferences for a user
 * - `PATCH /user_exercise_preference/` - Update an existing user exercise preference
 * - `DELETE /user_exercise_preference/` - Delete a user exercise preference
 *
 * ## Error Handling
 *
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @property userExercisePreferenceDAL Data access layer for user exercise preference operations
 * @property keycloakUtil Utility for Keycloak operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_exercise_preference")
class UserExercisePreferenceController(
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserExercisePreferenceController::class.java)
    }

    /**
     * Creates a new user exercise preference.
     *
     * This endpoint creates a preference relationship between a user and an exercise,
     * allowing the user to specify whether they like or dislike the exercise.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param shouldAvoid Whether the user should avoid this exercise
     * @return ResponseEntity containing the created user exercise preference
     */
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create user exercise preference",
        description = "Creates a new user exercise preference relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User exercise preference created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Exercise name", required = true)
        @RequestParam("exercise_name") exerciseName: String,
        @Parameter(description = "Whether the user should avoid this exercise", required = true)
        @RequestParam("should_avoid") shouldAvoid: Boolean,
    ): Mono<ResponseEntity<UserExercisePreference>> {
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
                        logger.info("Saving user exercise preference: {} - {}", userId, exerciseName)
                        userExercisePreferenceDAL.insertUserExercisePreference(userId, exerciseName, shouldAvoid)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error(
                                    "Error saving user exercise preference: userId={}, exerciseName={}, shouldAvoid={}",
                                    userId,
                                    exerciseName,
                                    shouldAvoid,
                                    e
                                )
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only create preferences for themselves"))
            }
        }
    }

    /**
     * Retrieves all exercise preferences for a specific user.
     *
     * This endpoint fetches all exercise preferences that are associated with the specified user,
     * returning a list of user-exercise preference relationships.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user exercise preferences
     */
    @GetMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user exercise preferences by user ID",
        description = "Retrieves all exercise preferences associated with a given user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User exercise preferences found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByUser(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("user_id") userId: String,
    ): Mono<ResponseEntity<List<UserExercisePreference>>> {
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
                        userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
                            .map { preferences ->
                                logger.debug("Found exercise preferences for user: {}", userId)
                                ResponseEntity.ok(preferences)
                            }
                            .doOnError { e ->
                                logger.error("Error getting exercise preferences for user: {}", userId, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only view their own exercise preferences"))
            }
        }
    }

    /**
     * Deletes a user exercise preference.
     *
     * This endpoint removes the preference relationship between a user and an exercise,
     * effectively removing the user's preference for that exercise.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return ResponseEntity containing the deleted user exercise preference
     */
    @DeleteMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete user exercise preference",
        description = "Deletes a user exercise preference relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User exercise preference deleted successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun delete(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Exercise name", required = true)
        @RequestParam("exercise_name") exerciseName: String,
    ): Mono<ResponseEntity<UserExercisePreference>> {
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
                        logger.info("Deleting user exercise preference: {} - {}", userId, exerciseName)
                        userExercisePreferenceDAL.deleteUserExercisePreference(userId, exerciseName)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error deleting user exercise preference: {} - {}", userId, exerciseName, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only delete their own exercise preferences"))
            }
        }
    }
}
