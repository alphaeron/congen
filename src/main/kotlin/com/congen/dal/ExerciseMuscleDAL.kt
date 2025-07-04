package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseMuscle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseMuscleDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseMuscleDAL::class.java)
    }

    fun selectExerciseMuscle(
        exerciseName: String,
        muscleName: String,
    ): Mono<ExerciseMuscle> {
        logger.debug("Selecting exercise muscle: {} - {}", exerciseName, muscleName)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName,
        )
    }

    fun selectExerciseMuscleByExercise(exerciseName: String): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting muscles for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseName,
        )
    }

    fun selectExerciseMuscleByMuscle(muscleName: String): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting exercises for muscle: {}", muscleName)
        return postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
            muscleName,
        )
    }

    fun selectAllExerciseMuscle(): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting all exercise muscle relationships")
        return postgresClient.select("SELECT * FROM exercise_muscle")
    }

    fun insertExerciseMuscle(exerciseMuscle: ExerciseMuscle): Mono<ExerciseMuscle> {
        logger.debug("Inserting exercise muscle: {} - {}", exerciseMuscle.exerciseName, exerciseMuscle.muscleName)
        return postgresClient.update(
            """
            INSERT INTO exercise_muscle
                (exercise_name, muscle_name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            exerciseMuscle.exerciseName,
            exerciseMuscle.muscleName,
        )
    }

    fun deleteExerciseMuscle(
        exerciseName: String,
        muscleName: String,
    ): Mono<ExerciseMuscle> {
        logger.debug("Deleting exercise muscle: {} - {}", exerciseName, muscleName)
        return postgresClient.update(
            "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName,
        )
    }
}
