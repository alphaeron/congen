package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserExercisePreference entity operations.
 *
 * This class provides database operations for the UserExercisePreference entity in the Congen application.
 * UserExercisePreference represents the relationship between users and their preferences for specific exercises.
 *
 * ## UserExercisePreference Entity
 *
 * UserExercisePreference represents:
 * - Association between a user and an exercise
 * - User's preference to avoid or include the exercise
 * - Used for personalizing workout generation
 *
 * ## Database Operations
 *
 * - **Select by user and exercise**: Retrieve a specific user-exercise preference
 * - **Select by user**: Retrieve all exercise preferences for a specific user
 * - **Insert**: Create new user-exercise preferences
 * - **Update**: Modify existing user-exercise preferences
 * - **Delete**: Remove user-exercise preferences
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When user-exercise preference doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserExercisePreferenceDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserExercisePreferenceDAL::class.java)
    }

    /**
     * Retrieves a specific user-exercise preference.
     *
     * This method queries the database to find the preference between the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user-exercise preference if found
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun selectUserExercisePreference(
        userId: Int,
        exerciseName: String,
    ): Mono<UserExercisePreference> {
        logger.debug("Selecting user exercise preference: {} - {}", userId, exerciseName)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    /**
     * Retrieves all exercise preferences for a specific user.
     *
     * This method fetches all exercise preferences that are associated with the specified user.
     * If no preferences exist for the user, an empty list is returned.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user-exercise preferences
     */
    fun selectUserExercisePreferencesByUser(userId: Int): Mono<List<UserExercisePreference>> {
        logger.debug("Selecting exercise preferences for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_exercise_preference WHERE user_id=$1",
            userId,
        )
    }

    /**
     * Creates a new user-exercise preference in the database.
     *
     * This method inserts a new preference between the specified user and exercise.
     * The combination of user ID and exercise name must be unique.
     *
     * @param userExercisePreference The user-exercise preference to create
     * @return Mono containing the created user-exercise preference
     * @throws DatabaseException when the preference already exists or database operation fails
     */
    fun insertUserExercisePreference(userExercisePreference: UserExercisePreference): Mono<UserExercisePreference> {
        logger.debug("Inserting user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return postgresClient.update(
            """
            INSERT INTO user_exercise_preference
                (user_id, exercise_name, should_avoid)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userExercisePreference.userId,
            userExercisePreference.exerciseName,
            userExercisePreference.shouldAvoid,
        )
    }

    /**
     * Updates an existing user-exercise preference in the database.
     *
     * This method modifies the preference for the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userExercisePreference The user-exercise preference with updated data
     * @return Mono containing the updated user-exercise preference
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun updateUserExercisePreference(userExercisePreference: UserExercisePreference): Mono<UserExercisePreference> {
        logger.debug("Updating user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return postgresClient.update(
            """
            UPDATE user_exercise_preference
            SET should_avoid=$3
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userExercisePreference.userId,
            userExercisePreference.exerciseName,
            userExercisePreference.shouldAvoid,
        )
    }

    /**
     * Deletes a user-exercise preference from the database.
     *
     * This method removes the preference between the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user-exercise preference
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun deleteUserExercisePreference(
        userId: Int,
        exerciseName: String,
    ): Mono<UserExercisePreference> {
        logger.debug("Deleting user exercise preference: {} - {}", userId, exerciseName)
        return postgresClient.update(
            "DELETE FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }
}
