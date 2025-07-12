package com.congen.controllers

import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserOneRepMax
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
 * REST controller for UserOneRepMax entity operations.
 *
 * This controller provides CRUD operations for user one rep max values in the Congen API.
 * User one rep max values allow users to track their maximum weight for different exercises,
 * which is used for workout generation and progression calculations.
 *
 * ## UserOneRepMax Entity
 *
 * A user one rep max represents:
 * - Association between a user and an exercise
 * - User's one rep max weight for the exercise
 * - Timestamp of when the 1RM was last updated
 * - Used for workout generation and progression calculations
 *
 * ## Endpoints
 *
 * - `PUT /user_one_rep_max/` - Create or update a user one rep max (upsert)
 * - `GET /user_one_rep_max/{userId}` - Retrieve all one rep max values for a user
 * - `GET /user_one_rep_max/{userId}/{exerciseName}` - Retrieve a specific one rep max
 * - `DELETE /user_one_rep_max/{userId}/{exerciseName}` - Delete a user one rep max
 *
 * ## Error Handling
 *
 * - **404 Not Found**: When a one rep max with the specified parameters doesn't exist
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @property userOneRepMaxDAL Data access layer for user one rep max operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_one_rep_max")
@Tag(
    name = "User One Rep Max Management",
    description = "Operations for managing user one rep max values",
)
class UserOneRepMaxController(
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserOneRepMaxController::class.java)
    }

    /**
     * Creates or updates a user one rep max.
     *
     * This endpoint performs an upsert operation - if a one rep max exists for the specified user and exercise,
     * it will be updated; otherwise, a new one rep max will be created.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @return ResponseEntity containing the created or updated user one rep max
     */
    @PutMapping("/")
    @Operation(
        summary = "Create or update user one rep max",
        description = "Creates a new user one rep max or updates an existing one (upsert operation).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User one rep max created or updated successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun upsert(
        @Parameter(description = "User ID", required = true)
        @RequestParam userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @RequestParam exerciseName: String,
        @Parameter(description = "One rep max weight value", required = true)
        @RequestParam oneRepMax: java.math.BigDecimal,
    ): ResponseEntity<*> {
        logger.info("Upserting user one rep max: {} - {} - {}", userId, exerciseName, oneRepMax)
        return ResponseEntity.ok(
            userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, oneRepMax),
        )
    }

    /**
     * Retrieves all one rep max values for a specific user.
     *
     * This endpoint fetches all one rep max values that are associated with the specified user,
     * returning a list of user-exercise 1RM relationships.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user one rep max values
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Get all one rep max values for a user",
        description = "Retrieves all one rep max values for a given user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "One rep max values retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAllByUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserOneRepMax>>> {
        return userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)
            .map {
                logger.debug("Found {} one rep max values for user: {}", it.size, userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting one rep max values for user: {}", userId, e)
            }
    }

    /**
     * Retrieves a specific one rep max for a user and exercise.
     *
     * This endpoint fetches the one rep max value for the specified user and exercise.
     * If no 1RM exists, a 404 error will be returned.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user one rep max if found, or 404 if not found
     */
    @GetMapping("/{userId}/{exerciseName}")
    @Operation(
        summary = "Get one rep max for user and exercise",
        description = "Retrieves a specific one rep max for a given user and exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "One rep max found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "One rep max not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByUserAndExercise(
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @PathVariable("exerciseName") exerciseName: String,
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)
            .map {
                logger.debug("Found one rep max for user: {} and exercise: {}", userId, exerciseName)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("One rep max not found for user: {} and exercise: {}", userId, exerciseName)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting one rep max for user: {} and exercise: {}", userId, exerciseName, e)
            }
    }

    /**
     * Deletes a user one rep max by user ID and exercise name.
     *
     * This endpoint removes the one rep max for the specified user and exercise.
     * If no 1RM exists, a 404 error will be returned.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return ResponseEntity containing the deleted user one rep max
     */
    @DeleteMapping("/{userId}/{exerciseName}")
    @Operation(
        summary = "Delete user one rep max",
        description = "Deletes a user one rep max for a given user and exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User one rep max deleted successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "One rep max not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun delete(
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
        @Parameter(description = "Exercise name", required = true)
        @PathVariable("exerciseName") exerciseName: String,
    ): ResponseEntity<*> {
        logger.info("Deleting user one rep max: {} - {}", userId, exerciseName)
        return ResponseEntity.ok(
            userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName),
        )
    }
}
