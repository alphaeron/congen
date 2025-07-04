package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseWorkoutType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseWorkoutTypeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseWorkoutTypeDAL::class.java)
    }

    fun selectExerciseWorkoutType(
        exerciseName: String,
        movementType: String,
        workoutType: String,
    ): Mono<ExerciseWorkoutType> {
        logger.debug("Selecting exercise workout type: {} - {} - {}", exerciseName, movementType, workoutType)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise_workout_type WHERE exercise_name=$1 AND movement_type=$2 AND workout_type=$3",
            exerciseName,
            movementType,
            workoutType,
        )
    }

    fun selectExerciseWorkoutTypesByExercise(exerciseName: String): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting workout types for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_workout_type WHERE exercise_name=$1",
            exerciseName,
        )
    }

    fun selectAllExerciseWorkoutTypes(): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting all exercise workout type relationships")
        return postgresClient.select("SELECT * FROM exercise_workout_type")
    }

    fun insertExerciseWorkoutType(exerciseWorkoutType: ExerciseWorkoutType): Mono<ExerciseWorkoutType> {
        logger.debug(
            "Inserting exercise workout type: {} - {} - {}",
            exerciseWorkoutType.exerciseName,
            exerciseWorkoutType.movementType,
            exerciseWorkoutType.workoutType,
        )
        return postgresClient.update(
            """
            INSERT INTO exercise_workout_type
                (exercise_name, movement_type, workout_type)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            exerciseWorkoutType.exerciseName,
            exerciseWorkoutType.movementType,
            exerciseWorkoutType.workoutType,
        )
    }

    fun deleteExerciseWorkoutType(
        exerciseName: String,
        movementType: String,
        workoutType: String,
    ): Mono<ExerciseWorkoutType> {
        logger.debug("Deleting exercise workout type: {} - {} - {}", exerciseName, movementType, workoutType)
        return postgresClient.update(
            "DELETE FROM exercise_workout_type WHERE exercise_name=$1 AND movement_type=$2 AND workout_type=$3",
            exerciseName,
            movementType,
            workoutType,
        )
    }

    fun selectExerciseWorkoutTypesByMovementType(movementType: String): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting workout types for movementType: {}", movementType)
        return postgresClient.select(
            "SELECT * FROM exercise_workout_type WHERE movement_type=$1",
            movementType,
        )
    }
}
