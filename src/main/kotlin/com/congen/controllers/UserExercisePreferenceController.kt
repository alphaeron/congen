package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.UserExercisePreference
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_exercise_preference")
class UserExercisePreferenceController(
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
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
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @param shouldAvoid Whether the user should avoid this exercise
     * @return ResponseEntity containing the created user exercise preference
     */
    @PostMapping("/")
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
        @Parameter(description = "User ID", required = true)
        @RequestParam userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @RequestParam exerciseName: String,
        @Parameter(description = "Whether the user should avoid this exercise", required = true)
        @RequestParam shouldAvoid: Boolean,
    ): ResponseEntity<*> {
        logger.info("Saving user exercise preference: {} - {}", userId, exerciseName)
        return ResponseEntity.ok(
            userExercisePreferenceDAL.insertUserExercisePreference(userId, exerciseName, shouldAvoid),
        )
    }

    /**
     * Retrieves all exercise preferences for a specific user.
     *
     * This endpoint fetches all exercise preferences that are associated with the specified user,
     * returning a list of user-exercise preference relationships.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user exercise preferences
     */
    @GetMapping("/{userId}")
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
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserExercisePreference>>> {
        return userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
            .map { preferences ->
                logger.debug("Found exercise preferences for user: {}", userId)
                ResponseEntity.ok(preferences)
            }
            .doOnError { e ->
                logger.error("Error getting user exercise preferences for user: {}", userId, e)
            }
    }

    /**
     * Deletes a user exercise preference.
     *
     * This endpoint removes the preference relationship between a user and an exercise,
     * effectively removing the user's preference for that exercise.
     *
     * @param userExercisePreference The user exercise preference to delete
     * @return ResponseEntity containing the deleted user exercise preference
     */
    @DeleteMapping("/")
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
        @Parameter(description = "User exercise preference to delete", required = true)
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Deleting user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return ResponseEntity.ok(
            userExercisePreferenceDAL.deleteUserExercisePreference(userExercisePreference.userId, userExercisePreference.exerciseName),
        )
    }
}
