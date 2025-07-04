package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.model.ExerciseWorkoutType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/exercise_workout_type")
class ExerciseWorkoutTypeController(
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseWorkoutTypeController::class.java)
    }

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
        @Parameter(description = "Exercise-workout type relationship to create", required = true)
        @RequestBody exerciseWorkoutType: ExerciseWorkoutType,
    ): ResponseEntity<*> {
        logger.info(
            "Saving exercise workout type relationship: {} - {} - {}",
            exerciseWorkoutType.exerciseName,
            exerciseWorkoutType.movementType,
            exerciseWorkoutType.workoutType,
        )
        return try {
            ResponseEntity.ok(
                exerciseWorkoutTypeDAL.insertExerciseWorkoutType(exerciseWorkoutType),
            )
        } catch (e: Exception) {
            logger.error(
                "Error saving exercise workout type relationship: {} - {} - {}",
                exerciseWorkoutType.exerciseName,
                exerciseWorkoutType.movementType,
                exerciseWorkoutType.workoutType,
                e,
            )
            throw e
        }
    }
}
