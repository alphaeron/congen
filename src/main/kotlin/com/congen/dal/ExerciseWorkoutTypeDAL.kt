package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseWorkoutType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ExerciseWorkoutType entity operations.
 *
 * This class provides database operations for the ExerciseWorkoutType entity in the Congen application.
 * ExerciseWorkoutType represents the relationship between exercises, movement types, and workout types,
 * indicating which exercises are suitable for specific workout types based on their movement patterns.
 *
 * ## ExerciseWorkoutType Entity
 *
 * ExerciseWorkoutType represents:
 * - Association between exercises, movement types, and workout types
 * - Three-way relationship mapping
 * - Used for exercise selection in workout generation
 *
 * ## Database Operations
 *
 * - **Select by exercise, movement type, and workout type**: Retrieve specific relationship
 * - **Select by exercise**: Retrieve all workout types for a specific exercise
 * - **Select by movement type**: Retrieve all relationships for a specific movement type
 * - **Select all**: Retrieve all exercise-workout type relationships
 * - **Insert**: Create new exercise-workout type relationships
 * - **Delete**: Remove exercise-workout type relationships
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When exercise-workout type relationship doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExerciseWorkoutTypeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExerciseWorkoutTypeDAL::class.java)
    }

    /**
     * Retrieves a specific exercise-workout type relationship.
     *
     * This method queries the database to find the relationship between the specified
     * exercise, movement type, and workout type. If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param movementType The movement type (push, pull, squat, hinge, etc.)
     * @param workoutType The workout type (strength, hypertrophy, endurance, etc.)
     * @return Mono containing the exercise-workout type relationship if found
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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

    /**
     * Retrieves all workout types for a specific exercise.
     *
     * This method fetches all workout types that are suitable for the specified exercise.
     * If no workout types exist for the exercise, an empty list is returned.
     *
     * @param exerciseName The name of the exercise
     * @return Mono containing a list of exercise-workout type relationships
     */
    fun selectExerciseWorkoutTypesByExercise(exerciseName: String): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting workout types for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_workout_type WHERE exercise_name=$1",
            exerciseName,
        )
    }

    /**
     * Retrieves all exercise-workout type relationships from the database.
     *
     * This method fetches all exercise-workout type relationships and returns them as a list.
     * If no relationships exist, an empty list is returned.
     *
     * @return Mono containing a list of all exercise-workout type relationships
     */
    fun selectAllExerciseWorkoutTypes(): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting all exercise workout type relationships")
        return postgresClient.select("SELECT * FROM exercise_workout_type")
    }

    /**
     * Creates a new exercise-workout type relationship in the database.
     *
     * This method inserts a new relationship between the specified exercise, movement type, and workout type.
     * The combination of exercise name, movement type, and workout type must be unique.
     *
     * @param exerciseWorkoutType The exercise-workout type relationship to create
     * @return Mono containing the created exercise-workout type relationship
     * @throws DatabaseException when the relationship already exists or database operation fails
     */
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

    /**
     * Deletes an exercise-workout type relationship from the database.
     *
     * This method removes the relationship between the specified exercise, movement type, and workout type.
     * If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param movementType The movement type
     * @param workoutType The workout type
     * @return Mono containing the deleted exercise-workout type relationship
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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

    /**
     * Retrieves all exercise-workout type relationships for a specific movement type.
     *
     * This method fetches all relationships that involve the specified movement type.
     * If no relationships exist for the movement type, an empty list is returned.
     *
     * @param movementType The movement type (push, pull, squat, hinge, etc.)
     * @return Mono containing a list of exercise-workout type relationships
     */
    fun selectExerciseWorkoutTypesByMovementType(movementType: String): Mono<List<ExerciseWorkoutType>> {
        logger.debug("Selecting workout types for movementType: {}", movementType)
        return postgresClient.select(
            "SELECT * FROM exercise_workout_type WHERE movement_type=$1",
            movementType,
        )
    }
}
