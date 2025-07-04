package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.model.ExerciseWorkoutType
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
    fun getByExercise(
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
    fun getByMovementType(
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
    fun save(
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
