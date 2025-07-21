package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserWeakMuscle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserWeakMuscle entity operations.
 *
 * Provides CRUD operations for the user_weak_muscle table, representing user weak muscle groups.
 *
 * @property postgresClient PostgreSQL client for database operations
 */
@Component
class UserWeakMuscleDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserWeakMuscleDAL::class.java)
    }

    /**
     * Retrieves all weak muscles for a specific user.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of UserWeakMuscle
     */
    fun selectUserWeakMusclesByUser(userId: Int): Mono<List<UserWeakMuscle>> {
        logger.debug("Selecting weak muscles for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_weak_muscle WHERE user_id=$1",
            userId,
        )
    }

    /**
     * Inserts a new weak muscle for a user.
     *
     * @param userId The unique identifier of the user
     * @param muscleName The name of the weak muscle group
     * @return Mono containing the created UserWeakMuscle
     */
    fun insertUserWeakMuscle(
        userId: Int,
        muscleName: String
    ): Mono<UserWeakMuscle> {
        logger.debug("Inserting user weak muscle: {} - {}", userId, muscleName)
        return postgresClient.update(
            """
            INSERT INTO user_weak_muscle (user_id, muscle_name)
            VALUES ($1, $2)
            """.trimIndent(),
            userId,
            muscleName,
        )
    }

    /**
     * Deletes a weak muscle for a user.
     *
     * @param userId The unique identifier of the user
     * @param muscleName The name of the weak muscle group
     * @return Mono containing the deleted UserWeakMuscle
     */
    fun deleteUserWeakMuscle(
        userId: Int,
        muscleName: String
    ): Mono<UserWeakMuscle> {
        logger.debug("Deleting user weak muscle: {} - {}", userId, muscleName)
        return postgresClient.update(
            "DELETE FROM user_weak_muscle WHERE user_id=$1 AND muscle_name=$2",
            userId,
            muscleName,
        )
    }
}
