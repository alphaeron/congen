package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseMuscle
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseMuscleDAL(
    private val postgresClient: PostgresClient,
) {
    fun selectExerciseMuscle(exerciseName: String, muscleName: String): Mono<ExerciseMuscle> =
        postgresClient.selectIndividual(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName
        )

    fun selectExerciseMuscleByExercise(exerciseName: String): Mono<List<ExerciseMuscle>> =
        postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseName
        )

    fun selectExerciseMuscleByMuscle(muscleName: String): Mono<List<ExerciseMuscle>> =
        postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
            muscleName
        )

    fun selectAllExerciseMuscle(): Mono<List<ExerciseMuscle>> =
        postgresClient.select("SELECT * FROM exercise_muscle")

    fun insertExerciseMuscle(exerciseMuscle: ExerciseMuscle): Mono<ExerciseMuscle> =
        postgresClient.update(
            """
                INSERT INTO exercise_muscle
                    (exercise_name, muscle_name)
                VALUES
                    ($1, $2)
            """.trimIndent(),
            exerciseMuscle.exerciseName,
            exerciseMuscle.muscleName
        )

    fun deleteExerciseMuscle(exerciseName: String, muscleName: String): Mono<ExerciseMuscle> =
        postgresClient.update(
            "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName
        )
} 