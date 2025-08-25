package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.UserOneRepMax
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal

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
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the user-exercise 1RM if found
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "user_one_rep_max"
    )
    fun selectUserOneRepMax(
        userId: String,
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
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user-exercise 1RM values
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_one_rep_max"
    )
    fun selectUserOneRepMaxByUser(userId: String): Mono<List<UserOneRepMax>> {
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
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @return Mono containing the created user-exercise 1RM
     * @throws DatabaseException when the 1RM already exists or database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_one_rep_max"
    )
    fun insertUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal,
    ): Mono<UserOneRepMax> {
        logger.debug("Inserting user one rep max: {} - {} - {}", userId, exerciseName, oneRepMax)
        // Validate all CHECK constraints
        ValidationUtil.validateOneRepMax(oneRepMax)
        return postgresClient.update(
            """
            INSERT INTO user_one_rep_max
                (user_id, exercise_name, one_rep_max)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userId,
            exerciseName,
            oneRepMax,
        )
    }

    /**
     * Updates an existing user-exercise one rep max in the database.
     *
     * This method modifies the 1RM for the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @return Mono containing the updated user-exercise 1RM
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_one_rep_max"
    )
    fun updateUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal,
    ): Mono<UserOneRepMax> {
        logger.debug("Updating user one rep max: {} - {} - {}", userId, exerciseName, oneRepMax)
        // Validate all CHECK constraints
        ValidationUtil.validateOneRepMax(oneRepMax)
        return postgresClient.update(
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, updated_at=NOW()
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userId,
            exerciseName,
            oneRepMax,
        )
    }

    /**
     * Creates or updates a user-exercise one rep max in the database.
     *
     * This method performs an upsert operation - if a 1RM exists for the specified user and exercise,
     * it will be updated; otherwise, a new 1RM will be created.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @return Mono containing the created or updated user-exercise 1RM
     * @throws DatabaseException when database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_one_rep_max"
    )
    fun upsertUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal,
    ): Mono<UserOneRepMax> {
        logger.debug("Upserting user one rep max: {} - {} - {}", userId, exerciseName, oneRepMax)
        // Validate all CHECK constraints
        ValidationUtil.validateOneRepMax(oneRepMax)
        return postgresClient.update(
            """
            INSERT INTO user_one_rep_max
                (user_id, exercise_name, one_rep_max)
            VALUES
                ($1, $2, $3)
            ON CONFLICT (user_id, exercise_name)
            DO UPDATE SET
                one_rep_max = EXCLUDED.one_rep_max,
                updated_at = NOW()
            """.trimIndent(),
            userId,
            exerciseName,
            oneRepMax,
        )
    }

    /**
     * Deletes a user-exercise one rep max from the database.
     *
     * This method removes the 1RM between the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user-exercise 1RM
     * @throws NoResultsFoundException when the 1RM doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_one_rep_max"
    )
    fun deleteUserOneRepMax(
        userId: String,
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
