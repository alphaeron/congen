package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.model.ExerciseWorkoutType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing exercise-workout type relationships.
 *
 * This controller provides endpoints for creating and querying relationships between
 * exercises and workout types. These relationships define which exercises are
 * suitable for different types of workouts (strength, cardio, flexibility, etc.)
 * and movement patterns (push, pull, squat, hinge, etc.).
 *
 * ## Exercise-Workout Type Relationships
 *
 * These relationships enable:
 * - **Workout Generation**: Finding appropriate exercises for specific workout types
 * - **Movement Pattern Organization**: Grouping exercises by movement patterns
 * - **Workout Type Filtering**: Selecting exercises suitable for different training goals
 * - **Program Customization**: Building workouts based on user preferences
 *
 * ## Relationship Structure
 *
 * Each relationship contains:
 * - **Exercise Name**: The exercise being categorized
 * - **Movement Type**: The movement pattern (push, pull, squat, hinge, etc.)
 * - **Workout Type**: The type of workout (strength, cardio, flexibility, etc.)
 *
 * ## Usage Examples
 *
 * - Find all push exercises for strength workouts
 * - Get all squat movements for power training
 * - Retrieve cardio exercises for endurance sessions
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/exercise_workout_type")
@Tag(
    name = "Exercise-Workout Type Management",
    description = "Operations for managing exercise-workout type relationships",
)
class ExerciseWorkoutTypeController(
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseWorkoutTypeController::class.java)
    }

    /**
     * Retrieves all exercise-workout type relationships.
     *
     * This endpoint returns a complete list of all exercise-workout type
     * relationships in the system. This is useful for understanding the
     * complete mapping of exercises to workout types and movement patterns.
     *
     * @return List of all exercise-workout type relationships
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @Operation(
        summary = "Get all exercise workout type relationships",
        description = "Retrieves all exercise-workout type relationships.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of exercise-workout type relationships retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercise workout type relationships")
        return try {
            ResponseEntity.ok(
                exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all exercise workout type relationships", e)
            throw e
        }
    }

    /**
     * Retrieves all workout types associated with a specific exercise.
     *
     * This endpoint finds all workout types and movement patterns that are
     * associated with a given exercise. This helps understand how an exercise
     * can be used in different training contexts.
     *
     * @param exerciseName The name of the exercise to find workout types for
     * @return List of exercise-workout type relationships for the exercise
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/exercise/{exerciseName}")
    @Operation(
        summary = "Get workout types by exercise name",
        description = "Retrieves all workout types associated with a given exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout types found for exercise",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByExercise(
        @Parameter(description = "Name of the exercise", required = true)
        @PathVariable("exerciseName") exerciseName: String,
    ): Mono<ResponseEntity<List<ExerciseWorkoutType>>> {
        return exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(exerciseName)
            .map {
                logger.debug("Found {} workout types for exercise: {}", it.size, exerciseName)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting workout types for exercise: {}", exerciseName, e)
            }
    }

    /**
     * Retrieves all workout types associated with a specific movement pattern.
     *
     * This endpoint finds all exercises and workout types that are associated
     * with a given movement pattern (push, pull, squat, hinge, etc.). This is
     * useful for building workouts that focus on specific movement patterns.
     *
     * @param movementType The movement pattern to find exercises for
     * @return List of exercise-workout type relationships for the movement pattern
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/movement_type/{movementType}")
    @Operation(
        summary = "Get workout types by movement type",
        description = "Retrieves all workout types associated with a given movement type.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout types found for movement type",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByMovementType(
        @Parameter(description = "Movement type", required = true)
        @PathVariable("movementType") movementType: String,
    ): Mono<ResponseEntity<List<ExerciseWorkoutType>>> {
        return exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByMovementType(movementType)
            .map {
                logger.debug("Found {} workout types for movementType: {}", it.size, movementType)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting workout types for movementType: {}", movementType, e)
            }
    }

    /**
     * Creates a new exercise-workout type relationship.
     *
     * This endpoint creates a new relationship between an exercise and a workout type.
     * This relationship defines how the exercise can be used in different training
     * contexts and movement patterns.
     *
     * @param exerciseWorkoutType The exercise-workout type relationship to create
     * @return The created relationship with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @Operation(
        summary = "Create exercise workout type relationship",
        description = "Creates a new exercise-workout type relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise-workout type relationship created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Name of the exercise", required = true)
        @RequestParam exerciseName: String,
        @Parameter(description = "Movement type (push, pull, squat, hinge, etc.)", required = true)
        @RequestParam movementType: String,
        @Parameter(description = "Workout type (strength, hypertrophy, endurance, etc.)", required = true)
        @RequestParam workoutType: String,
    ): ResponseEntity<*> {
        logger.info(
            "Saving exercise workout type relationship: {} - {} - {}",
            exerciseName,
            movementType,
            workoutType,
        )
        return try {
            ResponseEntity.ok(
                exerciseWorkoutTypeDAL.insertExerciseWorkoutType(exerciseName, movementType, workoutType),
            )
        } catch (e: DatabaseQueryException) {
            val msg = e.cause?.message ?: e.message ?: "Database error"
            return when {
                msg.contains(
                    "duplicate key",
                    ignoreCase = true
                ) -> ResponseEntity.status(HttpStatus.CONFLICT).body("Relationship already exists")
                msg.contains(
                    "violates foreign key",
                    ignoreCase = true
                ) -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Exercise, movement type, or workout type does not exist")
                else -> throw e
            }
        }
    }
}
