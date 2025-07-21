package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseMuscle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ExerciseMuscle entity operations.
 *
 * This class provides database operations for the ExerciseMuscle entity in the Congen application.
 * ExerciseMuscle represents the many-to-many relationship between exercises and muscles,
 * indicating which muscles are targeted by specific exercises.
 *
 * ## ExerciseMuscle Entity
 *
 * ExerciseMuscle represents:
 * - Association between exercises and muscles
 * - Many-to-many relationship mapping
 * - Used for exercise targeting and muscle group analysis
 *
 * ## Database Operations
 *
 * - **Select by exercise and muscle**: Retrieve specific exercise-muscle relationship
 * - **Select by exercise**: Retrieve all muscles targeted by a specific exercise
 * - **Select by muscle**: Retrieve all exercises that target a specific muscle
 * - **Select all**: Retrieve all exercise-muscle relationships
 * - **Insert**: Create new exercise-muscle relationships
 * - **Delete**: Remove exercise-muscle relationships
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When exercise-muscle relationship doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExerciseMuscleDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExerciseMuscleDAL::class.java)
    }

    /**
     * Retrieves a specific exercise-muscle relationship.
     *
     * This method queries the database to find the relationship between the specified
     * exercise and muscle. If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param muscleName The name of the muscle
     * @return Mono containing the exercise-muscle relationship if found
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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

    /**
     * Retrieves all muscles targeted by a specific exercise.
     *
     * This method fetches all muscles that are targeted by the specified exercise.
     * If no muscles exist for the exercise, an empty list is returned.
     *
     * @param exerciseName The name of the exercise
     * @return Mono containing a list of exercise-muscle relationships
     */
    fun selectExerciseMuscleByExercise(exerciseName: String): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting muscles for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseName,
        )
    }

    /**
     * Retrieves all exercises that target a specific muscle.
     *
     * This method fetches all exercises that target the specified muscle.
     * If no exercises exist for the muscle, an empty list is returned.
     *
     * @param muscleName The name of the muscle
     * @return Mono containing a list of exercise-muscle relationships
     */
    fun selectExerciseMuscleByMuscle(muscleName: String): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting exercises for muscle: {}", muscleName)
        return postgresClient.select(
            "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
            muscleName,
        )
    }

    /**
     * Retrieves all exercise-muscle relationships from the database.
     *
     * This method fetches all exercise-muscle relationships and returns them as a list.
     * If no relationships exist, an empty list is returned.
     *
     * @return Mono containing a list of all exercise-muscle relationships
     */
    fun selectAllExerciseMuscle(): Mono<List<ExerciseMuscle>> {
        logger.debug("Selecting all exercise muscle relationships")
        return postgresClient.select("SELECT * FROM exercise_muscle")
    }

    /**
     * Creates a new exercise-muscle relationship in the database.
     *
     * This method inserts a new relationship between the specified exercise and muscle.
     * The combination of exercise name and muscle name must be unique.
     *
     * @param exerciseName The name of the exercise
     * @param muscleName The name of the muscle
     * @return Mono containing the created exercise-muscle relationship
     * @throws DatabaseException when the relationship already exists or database operation fails
     */
    fun insertExerciseMuscle(
        exerciseName: String,
        muscleName: String
    ): Mono<ExerciseMuscle> {
        logger.debug("Inserting exercise muscle: {} - {}", exerciseName, muscleName)
        return postgresClient.update(
            """
            INSERT INTO exercise_muscle
                (exercise_name, muscle_name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            exerciseName,
            muscleName,
        )
    }

    /**
     * Deletes an exercise-muscle relationship from the database.
     *
     * This method removes the relationship between the specified exercise and muscle.
     * If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param muscleName The name of the muscle
     * @return Mono containing the deleted exercise-muscle relationship
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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
