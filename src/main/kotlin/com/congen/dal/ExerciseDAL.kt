package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Exercise
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseDAL::class.java)
    }

    fun selectExerciseByName(exerciseName: String): Mono<Exercise> {
        logger.debug("Selecting exercise by name: {}", exerciseName)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise WHERE name=$1",
            exerciseName,
        )
    }

    fun selectExercises(): Mono<List<Exercise>> {
        logger.debug("Selecting all exercises")
        return postgresClient.select("SELECT * FROM exercise")
    }

    fun insertExercise(exercise: Exercise): Mono<Exercise> {
        logger.debug("Inserting exercise: {}", exercise.name)
        return postgresClient.update(
            """
            INSERT INTO exercise
                (name, description, movement_type, is_unilateral, is_upper, is_accessory)
            VALUES
                ($1, $2, $3, $4, $5, $6)
            """.trimIndent(),
            exercise.name,
            exercise.description,
            exercise.movementType,
            exercise.isUnilateral,
            exercise.isUpper,
            exercise.isAccessory,
        )
    }

    fun updateExercise(exercise: Exercise): Mono<Exercise> {
        logger.debug("Updating exercise: {}", exercise.name)
        return postgresClient.update(
            """
            UPDATE exercise
            SET description=$2, movement_type=$3, is_unilateral=$4, is_upper=$5, is_accessory=$6
            WHERE name=$1
            """.trimIndent(),
            exercise.name,
            exercise.description,
            exercise.movementType,
            exercise.isUnilateral,
            exercise.isUpper,
            exercise.isAccessory,
        )
    }

    fun deleteExercise(exerciseName: String): Mono<Exercise> {
        logger.debug("Deleting exercise: {}", exerciseName)
        return postgresClient.update(
            "DELETE FROM exercise WHERE name=$1",
            exerciseName,
        )
    }
}
