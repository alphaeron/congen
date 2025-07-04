package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserExercisePreferenceDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserExercisePreferenceDAL::class.java)
    }

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

    fun selectUserExercisePreferencesByUser(userId: Int): Mono<List<UserExercisePreference>> {
        logger.debug("Selecting exercise preferences for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_exercise_preference WHERE user_id=$1",
            userId,
        )
    }

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
