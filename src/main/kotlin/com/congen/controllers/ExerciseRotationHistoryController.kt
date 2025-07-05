package com.congen.controllers

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.model.ExerciseRotationHistory
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing exercise rotation history operations.
 *
 * This controller provides endpoints for creating, reading, updating, and deleting
 * exercise rotation history records. Exercise rotation history tracks when users
 * have used specific exercises in their workout programs, categorized by exercise type.
 *
 * ## Exercise Rotation History Model
 *
 * Exercise rotation history contains:
 * - User ID reference
 * - Exercise name reference
 * - Category classification (primary, secondary, accessory, etc.)
 * - Usage timestamp
 *
 * ## Validation Rules
 *
 * - **UserId**: Required, must reference an existing user
 * - **ExerciseName**: Required, must reference an existing exercise
 * - **Category**: Required, must be one of: 'primary', 'secondary', 'accessory', etc.
 *
 * ## Error Handling
 *
 * - `400 Bad Request`: Invalid input data
 * - `404 Not Found`: Record not found
 * - `422 Unprocessable Entity`: Validation errors
 * - `500 Internal Server Error`: Database or system errors
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/exercise-rotation-history")
@Tag(
    name = "Exercise Rotation History Management",
    description = "Operations for managing exercise rotation history records",
)
class ExerciseRotationHistoryController(
    private val exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseRotationHistoryController::class.java)
    }

    /**
     * Creates a new exercise rotation history record.
     *
     * This endpoint creates a new exercise rotation history record with the provided information.
     * The record will be assigned a unique ID and timestamp automatically.
     *
     * @param userId The ID of the user who used the exercise
     * @param exerciseName The name of the exercise that was used
     * @param category The category of the exercise (primary, secondary, accessory, etc.)
     * @return The created exercise rotation history record with assigned ID and timestamp
     *
     * @throws ValidationException if data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @Operation(
        summary = "Create a new exercise rotation history record",
        description =
            "Creates a new exercise rotation history record with the provided information. " +
                "The record will be assigned a unique ID and timestamp automatically.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history record created successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validation error",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun save(
        @Parameter(
            description = "The ID of the user who used the exercise",
            required = true,
            example = "1",
        )
        @RequestParam userId: Long,
        @Parameter(
            description = "The name of the exercise that was used",
            required = true,
            example = "Bench Press",
        )
        @RequestParam exerciseName: String,
        @Parameter(
            description = "The category of the exercise",
            required = true,
            example = "primary",
        )
        @RequestParam category: String,
    ): Mono<ResponseEntity<ExerciseRotationHistory>> {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 0, // Temporary ID, will be replaced by database auto-generation
                userId = userId,
                exerciseName = exerciseName,
                category = category,
            )
        logger.info(
            "Saving exercise rotation history: user_id={}, exercise_name={}, category={}",
            userId,
            exerciseName,
            category,
        )
        return exerciseRotationHistoryDAL.insert(exerciseRotationHistory)
            .map { savedRecord ->
                logger.debug("Saved exercise rotation history with id: {}", savedRecord.id)
                ResponseEntity.ok(savedRecord)
            }
            .doOnError { e ->
                logger.error(
                    "Error saving exercise rotation history: user_id={}, exercise_name={}, category={}",
                    userId,
                    exerciseName,
                    category,
                    e,
                )
            }
    }

    /**
     * Retrieves an exercise rotation history record by its unique identifier.
     *
     * This endpoint fetches a specific exercise rotation history record by its ID.
     * If the record is not found, a 404 error will be returned.
     *
     * @param id The unique identifier of the exercise rotation history record to retrieve
     * @return The exercise rotation history record if found, or 404 if not found
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get exercise rotation history record by ID",
        description = "Retrieves a specific exercise rotation history record by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history record found successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Exercise rotation history record not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun get(
        @Parameter(
            description = "Unique identifier of the exercise rotation history record",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<ExerciseRotationHistory>> {
        logger.info("Getting exercise rotation history by id: {}", id)
        return exerciseRotationHistoryDAL.selectById(id)
            .map { record ->
                logger.debug("Found exercise rotation history record: {}", record.id)
                ResponseEntity.ok(record)
            }
            .doOnError { e ->
                logger.error("Error getting exercise rotation history by id: {}", id, e)
            }
    }

    /**
     * Retrieves all exercise rotation history records for a specific user.
     *
     * This endpoint fetches all exercise rotation history records associated with a user.
     * If no records exist for the user, an empty list is returned.
     *
     * @param userId The ID of the user whose exercise rotation history to retrieve
     * @return List of exercise rotation history records for the user
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get exercise rotation history by user ID",
        description = "Retrieves all exercise rotation history records for a specific user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history records found successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun getByUserId(
        @Parameter(
            description = "ID of the user whose exercise rotation history to retrieve",
            required = true,
            example = "1",
        )
        @PathVariable("userId") userId: Long,
    ): Mono<ResponseEntity<List<ExerciseRotationHistory>>> {
        logger.info("Getting exercise rotation history by user id: {}", userId)
        return exerciseRotationHistoryDAL.selectByUserId(userId)
            .map { records ->
                logger.debug("Found {} exercise rotation history records for user: {}", records.size, userId)
                ResponseEntity.ok(records)
            }
            .doOnError { e ->
                logger.error("Error getting exercise rotation history by user id: {}", userId, e)
            }
    }

    /**
     * Retrieves exercise rotation history records for a specific user and category.
     *
     * This endpoint fetches exercise rotation history records for a user filtered by category.
     * If no records exist for the user and category, an empty list is returned.
     *
     * @param userId The ID of the user whose exercise rotation history to retrieve
     * @param category The category of exercises to filter by
     * @return List of exercise rotation history records for the user and category
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/user/{userId}/category/{category}")
    @Operation(
        summary = "Get exercise rotation history by user ID and category",
        description = "Retrieves exercise rotation history records for a specific user and category.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history records found successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun getByUserIdAndCategory(
        @Parameter(
            description = "ID of the user whose exercise rotation history to retrieve",
            required = true,
            example = "1",
        )
        @PathVariable("userId") userId: Long,
        @Parameter(
            description = "Category of exercises to filter by",
            required = true,
            example = "primary",
        )
        @PathVariable("category") category: String,
    ): Mono<ResponseEntity<List<ExerciseRotationHistory>>> {
        logger.info(
            "Getting exercise rotation history by user id: {} and category: {}",
            userId,
            category,
        )
        return exerciseRotationHistoryDAL.selectByUserIdAndCategory(userId, category)
            .map { records ->
                logger.debug(
                    "Found {} exercise rotation history records for user: {} and category: {}",
                    records.size,
                    userId,
                    category,
                )
                ResponseEntity.ok(records)
            }
            .doOnError { e ->
                logger.error(
                    "Error getting exercise rotation history by user id: {} and category: {}",
                    userId,
                    category,
                    e,
                )
            }
    }

    /**
     * Retrieves all exercise rotation history records.
     *
     * This endpoint fetches all exercise rotation history records from the database.
     * If no records exist, an empty list is returned.
     *
     * @return List of all exercise rotation history records
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @Operation(
        summary = "Get all exercise rotation history records",
        description = "Retrieves all exercise rotation history records from the database.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history records found successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun getAll(): Mono<ResponseEntity<List<ExerciseRotationHistory>>> {
        logger.info("Getting all exercise rotation history records")
        return exerciseRotationHistoryDAL.selectAll()
            .map { records ->
                logger.debug("Found {} exercise rotation history records", records.size)
                ResponseEntity.ok(records)
            }
            .doOnError { e ->
                logger.error("Error getting all exercise rotation history records", e)
            }
    }

    /**
     * Updates an existing exercise rotation history record.
     *
     * This endpoint updates an exercise rotation history record with the provided information.
     * If the record is not found, a 404 error will be returned.
     *
     * @param id The unique identifier of the exercise rotation history record to update
     * @param userId The ID of the user who used the exercise
     * @param exerciseName The name of the exercise that was used
     * @param category The category of the exercise (primary, secondary, accessory, etc.)
     * @return The updated exercise rotation history record
     *
     * @throws ValidationException if data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an exercise rotation history record",
        description = "Updates an existing exercise rotation history record with the provided information.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history record updated successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Exercise rotation history record not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validation error",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun update(
        @Parameter(
            description = "Unique identifier of the exercise rotation history record to update",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Long,
        @Parameter(
            description = "The ID of the user who used the exercise",
            required = true,
            example = "1",
        )
        @RequestParam userId: Long,
        @Parameter(
            description = "The name of the exercise that was used",
            required = true,
            example = "Bench Press",
        )
        @RequestParam exerciseName: String,
        @Parameter(
            description = "The category of the exercise",
            required = true,
            example = "secondary",
        )
        @RequestParam category: String,
    ): Mono<ResponseEntity<ExerciseRotationHistory>> {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = id,
                userId = userId,
                exerciseName = exerciseName,
                category = category,
            )
        logger.info(
            "Updating exercise rotation history: id={}, user_id={}, exercise_name={}, category={}",
            id,
            userId,
            exerciseName,
            category,
        )
        return exerciseRotationHistoryDAL.update(exerciseRotationHistory)
            .map { updatedRecord ->
                logger.debug("Updated exercise rotation history record: {}", updatedRecord.id)
                ResponseEntity.ok(updatedRecord)
            }
            .doOnError { e ->
                logger.error(
                    "Error updating exercise rotation history: id={}, user_id={}, exercise_name={}, category={}",
                    id,
                    userId,
                    exerciseName,
                    category,
                    e,
                )
            }
    }

    /**
     * Deletes an exercise rotation history record.
     *
     * This endpoint removes an exercise rotation history record by its ID.
     * If the record is not found, a 404 error will be returned.
     *
     * @param id The unique identifier of the exercise rotation history record to delete
     * @return The deleted exercise rotation history record
     *
     * @throws DatabaseException if database operation fails
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an exercise rotation history record",
        description = "Deletes an exercise rotation history record by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history record deleted successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Exercise rotation history record not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun delete(
        @Parameter(
            description = "Unique identifier of the exercise rotation history record to delete",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<ExerciseRotationHistory>> {
        logger.info("Deleting exercise rotation history: {}", id)
        return exerciseRotationHistoryDAL.deleteById(id)
            .map { deletedRecord ->
                logger.debug("Deleted exercise rotation history record: {}", deletedRecord.id)
                ResponseEntity.ok(deletedRecord)
            }
            .doOnError { e ->
                logger.error("Error deleting exercise rotation history: {}", id, e)
            }
    }

    /**
     * Deletes all exercise rotation history records for a specific user.
     *
     * This endpoint removes all exercise rotation history records associated with a user.
     * If no records exist for the user, a 404 error will be returned.
     *
     * @param userId The ID of the user whose exercise rotation history records to delete
     * @return The number of deleted records
     *
     * @throws DatabaseException if database operation fails
     */
    @DeleteMapping("/user/{userId}")
    @Operation(
        summary = "Delete all exercise rotation history records for a user",
        description = "Deletes all exercise rotation history records associated with a specific user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise rotation history records deleted successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No exercise rotation history records found for user",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun deleteByUserId(
        @Parameter(
            description = "ID of the user whose exercise rotation history records to delete",
            required = true,
            example = "1",
        )
        @PathVariable("userId") userId: Long,
    ): Mono<ResponseEntity<Int>> {
        logger.info("Deleting exercise rotation history for user: {}", userId)
        return exerciseRotationHistoryDAL.deleteByUserId(userId)
            .map { deletedCount ->
                logger.debug("Deleted {} exercise rotation history records for user: {}", deletedCount, userId)
                ResponseEntity.ok(deletedCount)
            }
            .doOnError { e ->
                logger.error("Error deleting exercise rotation history for user: {}", userId, e)
            }
    }
} 
