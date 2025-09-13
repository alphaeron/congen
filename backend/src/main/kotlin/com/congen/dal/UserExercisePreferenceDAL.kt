package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
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
 * @param postgresClient PostgreSQL client for database operations
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
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user-exercise preference if found
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "user_exercise_preference"
    )
    fun selectUserExercisePreference(
        userId: String,
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
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user-exercise preferences
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_exercise_preference"
    )
    fun selectUserExercisePreferencesByUser(userId: String): Mono<List<UserExercisePreference>> {
        logger.debug("Selecting exercise preferences for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_exercise_preference WHERE user_id=$1",
            userId,
        )
    }


    /**
     * Creates or updates a user-exercise preference in the database (upsert operation).
     *
     * This method performs an upsert operation - if a preference exists for the specified user and exercise,
     * it will be updated; otherwise, a new preference will be created.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param shouldAvoid Whether the user should avoid this exercise
     * @return Mono containing the created or updated user-exercise preference
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_exercise_preference"
    )
    fun upsertUserExercisePreference(
        userId: String,
        exerciseName: String,
        shouldAvoid: Boolean,
    ): Mono<UserExercisePreference> {
        logger.debug("Upserting user exercise preference: {} - {}", userId, exerciseName)
        return postgresClient.update<UserExercisePreference>(
            """
            UPDATE user_exercise_preference
            SET should_avoid = $3
            WHERE user_id = $1 AND exercise_name = $2
            """.trimIndent(),
            userId,
            exerciseName,
            shouldAvoid,
        ).onErrorResume { error ->
            if (error is NoResultsFoundException) {
                // No existing preference found, insert a new one
                postgresClient.update<UserExercisePreference>(
                    """
                    INSERT INTO user_exercise_preference
                        (user_id, exercise_name, should_avoid)
                    VALUES
                        ($1, $2, $3)
                    """.trimIndent(),
                    userId,
                    exerciseName,
                    shouldAvoid,
                )
            } else {
                Mono.error(error)
            }
        }
    }

    /**
     * Deletes a user-exercise preference from the database.
     *
     * This method removes the preference between the specified user and exercise.
     * If no preference exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user-exercise preference
     * @throws NoResultsFoundException when the preference doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_exercise_preference"
    )
    fun deleteUserExercisePreference(
        userId: String,
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
