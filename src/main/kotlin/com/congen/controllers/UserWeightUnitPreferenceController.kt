package com.congen.controllers

import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for UserWeightUnitPreference entity operations.
 *
 * This controller provides CRUD operations for user weight unit preferences in the Congen API.
 * User weight unit preferences allow users to specify their preferred weight units (kg or lbs)
 * for individual exercises, enabling them to input weights in their preferred units while
 * the system stores all weights internally in kg.
 *
 * ## UserWeightUnitPreference Entity
 *
 * A user weight unit preference represents:
 * - Association between a user and an exercise
 * - User's preferred weight unit (kg or lbs) for the exercise
 * - Timestamp of when the preference was created and last updated
 * - Used for converting user input to kg and displaying weights in preferred units
 *
 * ## Endpoints
 *
 * - `PUT /user_weight_unit_preference/` - Create or update a user weight unit preference (upsert)
 * - `GET /user_weight_unit_preference/{userId}` - Retrieve all weight unit preferences for a user
 * - `GET /user_weight_unit_preference/{userId}/{exerciseName}` - Retrieve a specific weight unit preference
 * - `DELETE /user_weight_unit_preference/{userId}/{exerciseName}` - Delete a user weight unit preference
 *
 * ## Error Handling
 *
 * - **404 Not Found**: When a weight unit preference with the specified parameters doesn't exist
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @property userWeightUnitPreferenceDAL Data access layer for user weight unit preference operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_weight_unit_preference")
@Tag(
    name = "User Weight Unit Preference Management",
    description = "Operations for managing user weight unit preferences",
)
class UserWeightUnitPreferenceController(
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserWeightUnitPreferenceController::class.java)
    }

    /**
     * Creates or updates a user weight unit preference.
     *
     * This endpoint performs an upsert operation - if a weight unit preference exists for the specified user and exercise,
     * it will be updated; otherwise, a new preference will be created.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @param preferredUnit The user's preferred weight unit for this exercise (KG or LBS)
     * @return ResponseEntity containing the created or updated user weight unit preference
     */
    @PutMapping("/")
    @Operation(
        summary = "Create or update user weight unit preference",
        description = "Creates a new user weight unit preference or updates an existing one (upsert operation).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User weight unit preference created or updated successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun upsert(
        @Parameter(description = "User ID", required = true)
        @RequestParam("user_id") userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @RequestParam("exercise_name") exerciseName: String,
        @Parameter(description = "Preferred weight unit (KG or LBS)", required = true)
        @RequestParam("preferred_unit") preferredUnit: String,
    ): ResponseEntity<*> {
        logger.info("Upserting user weight unit preference: {} - {} - {}", userId, exerciseName, preferredUnit)

        val weightUnit = WeightUnit.fromString(preferredUnit)

        return ResponseEntity.ok(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(userId, exerciseName, weightUnit),
        )
    }

    /**
     * Retrieves all weight unit preferences for a specific user.
     *
     * This endpoint fetches all weight unit preferences that are associated with the specified user,
     * returning a list of user-exercise unit preference relationships.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user weight unit preferences
     */
    @GetMapping("/{user_id}")
    @Operation(
        summary = "Get all weight unit preferences for a user",
        description = "Retrieves all weight unit preferences for a given user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Weight unit preferences retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAllByUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable("user_id") userId: Int,
    ): Mono<ResponseEntity<List<UserWeightUnitPreference>>> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)
            .map {
                logger.debug("Found {} weight unit preferences for user: {}", it.size, userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting weight unit preferences for user: {}", userId, e)
            }
    }

    /**
     * Retrieves a specific weight unit preference for a user and exercise.
     *
     * This endpoint fetches the weight unit preference for the specified user and exercise.
     * If no preference exists, a 404 error will be returned.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user weight unit preference if found, or 404 if not found
     */
    @GetMapping("/{user_id}/{exercise_name}")
    @Operation(
        summary = "Get weight unit preference for user and exercise",
        description = "Retrieves a specific weight unit preference for a given user and exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Weight unit preference found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Weight unit preference not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByUserAndExercise(
        @Parameter(description = "User ID", required = true)
        @PathVariable("user_id") userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @PathVariable("exercise_name") exerciseName: String,
    ): Mono<ResponseEntity<UserWeightUnitPreference>> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
            .map {
                logger.debug("Found weight unit preference for user: {} and exercise: {}", userId, exerciseName)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Weight unit preference not found for user: {} and exercise: {}", userId, exerciseName)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting weight unit preference for user: {} and exercise: {}", userId, exerciseName, e)
            }
    }

    /**
     * Deletes a user weight unit preference by user ID and exercise name.
     *
     * This endpoint removes the weight unit preference for the specified user and exercise.
     * If no preference exists, a 404 error will be returned.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return ResponseEntity containing the deleted user weight unit preference
     */
    @DeleteMapping("/{user_id}/{exercise_name}")
    @Operation(
        summary = "Delete user weight unit preference",
        description = "Deletes a user weight unit preference for a given user and exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User weight unit preference deleted successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Weight unit preference not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun delete(
        @Parameter(description = "User ID", required = true)
        @PathVariable("user_id") userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @PathVariable("exercise_name") exerciseName: String,
    ): ResponseEntity<*> {
        logger.info("Deleting user weight unit preference: {} - {}", userId, exerciseName)
        return ResponseEntity.ok(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(userId, exerciseName),
        )
    }
}
