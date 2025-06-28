package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Exercise
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseDAL(
    private val postgresClient: PostgresClient,
) {
    fun selectExerciseByName(exerciseName: String): Mono<Exercise> =
        postgresClient.selectIndividual(
            "SELECT * FROM exercise WHERE name=$1",
            exerciseName
        )

    fun selectExercises(): Mono<List<Exercise>> =
        postgresClient.select("SELECT * FROM exercise")

    fun insertExercise(exercise: Exercise): Mono<Exercise> =
        postgresClient.update(
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
            exercise.isAccessory
        )

    fun updateExercise(exercise: Exercise): Mono<Exercise> =
        postgresClient.update(
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
            exercise.isAccessory
        )

    fun deleteExercise(exerciseName: String): Mono<Exercise> =
        postgresClient.update(
            "DELETE FROM exercise WHERE name=$1",
            exerciseName
        )
} 