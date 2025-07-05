package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserOneRepMax
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserOneRepMax entity operations.
 *
 * This class provides database operations for the UserOneRepMax entity in the Congen application.
 * UserOneRepMax represents the relationship between users and their one rep max values for specific exercises.
 *
 * ## UserOneRepMax Entity
 *
 * UserOneRepMax represents:
 * - Association between a user and an exercise
 * - User's one rep max weight for the exercise
 * - Timestamp of when the 1RM was last updated
 * - Used for workout generation and progression calculations
 *
 * ## Database Operations
 *
 * - **Select by user and exercise**: Retrieve a specific user-exercise 1RM
 * - **Select by user**: Retrieve all 1RM values for a specific user
 * - **Insert**: Create new user-exercise 1RM records
 * - **Update**: Modify existing user-exercise 1RM records
 * - **Delete**: Remove user-exercise 1RM records
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When user-exercise 1RM doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserOneRepMaxDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserOneRepMaxDAL::class.java)
    }

    /**
     * Retrieves a specific user-exercise one rep max.
     *
     * This method queries the database to find the 1RM between the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException is thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user-exercise 1RM if found
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    fun selectUserOneRepMax(
        userId: Int,
        exerciseName: String,
    ): Mono<UserOneRepMax> {
        logger.debug("Selecting user one rep max: {} - {}", userId, exerciseName)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    /**
     * Retrieves all one rep max values for a specific user.
     *
     * This method fetches all 1RM values that are associated with the specified user.
     * If no 1RM values exist for the user, an empty list is returned.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user-exercise 1RM values
     */
    fun selectUserOneRepMaxByUser(userId: Int): Mono<List<UserOneRepMax>> {
        logger.debug("Selecting one rep max values for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 ORDER BY exercise_name",
            userId,
        )
    }

    /**
     * Creates a new user-exercise one rep max in the database.
     *
     * This method inserts a new 1RM between the specified user and exercise.
     * The combination of user ID and exercise name must be unique.
     *
     * @param userOneRepMax The user-exercise 1RM to create
     * @return Mono containing the created user-exercise 1RM
     * @throws DatabaseException when the 1RM already exists or database operation fails
     */
    fun insertUserOneRepMax(userOneRepMax: UserOneRepMax): Mono<UserOneRepMax> {
        logger.debug("Inserting user one rep max: {} - {} - {}", userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)
        // Validate all CHECK constraints
        ValidationUtil.validateOneRepMax(userOneRepMax.oneRepMax)
        return postgresClient.update(
            """
            INSERT INTO user_one_rep_max
                (user_id, exercise_name, one_rep_max)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    /**
     * Updates an existing user-exercise one rep max in the database.
     *
     * This method modifies the 1RM for the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException is thrown.
     *
     * @param userOneRepMax The user-exercise 1RM with updated data
     * @return Mono containing the updated user-exercise 1RM
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    fun updateUserOneRepMax(userOneRepMax: UserOneRepMax): Mono<UserOneRepMax> {
        logger.debug("Updating user one rep max: {} - {} - {}", userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)
        // Validate all CHECK constraints
        ValidationUtil.validateOneRepMax(userOneRepMax.oneRepMax)
        return postgresClient.update(
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, last_updated=CURRENT_TIMESTAMP
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    /**
     * Deletes a user-exercise one rep max from the database.
     *
     * This method removes the 1RM between the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException is thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user-exercise 1RM
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    fun deleteUserOneRepMax(
        userId: Int,
        exerciseName: String,
    ): Mono<UserOneRepMax> {
        logger.debug("Deleting user one rep max: {} - {}", userId, exerciseName)
        return postgresClient.update(
            "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }
}
