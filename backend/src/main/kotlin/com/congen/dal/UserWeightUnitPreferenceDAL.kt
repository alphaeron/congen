package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserWeightUnitPreference entity operations.
 *
 * This class provides database operations for the UserWeightUnitPreference entity in the Congen application.
 * UserWeightUnitPreference represents the relationship between users and their weight unit preferences for specific exercises.
 *
 * ## UserWeightUnitPreference Entity
 *
 * UserWeightUnitPreference represents:
 * - Association between a user and an exercise
 * - User's preferred weight unit (kg or lbs) for the exercise
 * - Timestamp of when the preference was created and last updated
 * - Used for converting user input to kg and displaying weights in preferred units
 *
 * ## Database Operations
 *
 * - **Select by user and exercise**: Retrieve a specific user-exercise unit preference
 * - **Select by user**: Retrieve all unit preferences for a specific user
 * - **Insert**: Create new user-exercise unit preferences
 * - **Update**: Modify existing user-exercise unit preferences
 * - **Upsert**: Create or update user-exercise unit preferences
 * - **Delete**: Remove user-exercise unit preferences
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When user-exercise unit preference doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserWeightUnitPreferenceDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserWeightUnitPreferenceDAL::class.java)
    }

    /**
     * Retrieves a specific user-exercise weight unit preference.
     *
     * This method queries the database to find the unit preference between the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user-exercise unit preference if found
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun selectUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
    ): Mono<UserWeightUnitPreference> {
        logger.debug("Selecting user weight unit preference: {} - {}", userId, exerciseName)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_weight_unit_preference WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    /**
     * Retrieves all weight unit preferences for a specific user.
     *
     * This method fetches all unit preferences that are associated with the specified user.
     * If no preferences exist for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user-exercise unit preferences
     */
    fun selectUserWeightUnitPreferencesByUser(userId: String): Mono<List<UserWeightUnitPreference>> {
        logger.debug("Selecting weight unit preferences for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_weight_unit_preference WHERE user_id=$1 ORDER BY exercise_name",
            userId,
        )
    }

    /**
     * Creates a new user-exercise weight unit preference in the database.
     *
     * This method inserts a new unit preference between the specified user and exercise.
     * The combination of user ID and exercise name must be unique.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param preferredUnit The user's preferred weight unit for this exercise
     * @return Mono containing the created user-exercise unit preference
     * @throws DatabaseException when the preference already exists or database operation fails
     */
    fun insertUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
        preferredUnit: WeightUnit,
    ): Mono<UserWeightUnitPreference> {
        logger.debug("Inserting user weight unit preference: {} - {} - {}", userId, exerciseName, preferredUnit)
        return postgresClient.update(
            """
            INSERT INTO user_weight_unit_preference
                (user_id, exercise_name, preferred_unit)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userId,
            exerciseName,
            preferredUnit.name,
        )
    }

    /**
     * Updates an existing user-exercise weight unit preference in the database.
     *
     * This method modifies the unit preference for the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param preferredUnit The user's preferred weight unit for this exercise
     * @return Mono containing the updated user-exercise unit preference
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun updateUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
        preferredUnit: WeightUnit,
    ): Mono<UserWeightUnitPreference> {
        logger.debug("Updating user weight unit preference: {} - {} - {}", userId, exerciseName, preferredUnit)
        return postgresClient.update(
            """
            UPDATE user_weight_unit_preference
            SET preferred_unit=$3, updated_at=NOW()
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userId,
            exerciseName,
            preferredUnit.name,
        )
    }

    /**
     * Creates or updates a user-exercise weight unit preference in the database.
     *
     * This method performs an upsert operation - if a unit preference exists for the specified user and exercise,
     * it will be updated; otherwise, a new preference will be created.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param preferredUnit The user's preferred weight unit for this exercise
     * @return Mono containing the created or updated user-exercise unit preference
     * @throws DatabaseException when database operation fails
     */
    fun upsertUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
        preferredUnit: WeightUnit,
    ): Mono<UserWeightUnitPreference> {
        logger.debug("Upserting user weight unit preference: {} - {} - {}", userId, exerciseName, preferredUnit)
        return postgresClient.update(
            """
            INSERT INTO user_weight_unit_preference
                (user_id, exercise_name, preferred_unit)
            VALUES
                ($1, $2, $3)
            ON CONFLICT (user_id, exercise_name)
            DO UPDATE SET
                preferred_unit = EXCLUDED.preferred_unit,
                updated_at = NOW()
            """.trimIndent(),
            userId,
            exerciseName,
            preferredUnit.name,
        )
    }

    /**
     * Deletes a user-exercise weight unit preference from the database.
     *
     * This method removes the unit preference between the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user-exercise unit preference
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    fun deleteUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
    ): Mono<UserWeightUnitPreference> {
        logger.debug("Deleting user weight unit preference: {} - {}", userId, exerciseName)
        return postgresClient.update(
            "DELETE FROM user_weight_unit_preference WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }
}
