package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserProgramPreferences
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserProgramPreferencesDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserProgramPreferencesDAL::class.java)
    }

    fun selectUserProgramPreferences(userId: Int): Mono<UserProgramPreferences> {
        logger.debug("Selecting program preferences for user: {}", userId)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_program_preferences WHERE user_id=$1",
            userId,
        )
    }

    fun insertUserProgramPreferences(userProgramPreferences: UserProgramPreferences): Mono<UserProgramPreferences> {
        logger.debug("Inserting user program preferences: {}", userProgramPreferences.userId)
        return postgresClient.update(
            """
            INSERT INTO user_program_preferences
                (user_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            RETURNING user_id, program_days_per_week, session_time_length_in_minutes
            """.trimIndent(),
            userProgramPreferences.userId,
            userProgramPreferences.programDaysPerWeek,
            userProgramPreferences.sessionTimeLengthInMinutes,
        )
    }

    fun updateUserProgramPreferences(userProgramPreferences: UserProgramPreferences): Mono<UserProgramPreferences> {
        logger.debug("Updating user program preferences: {}", userProgramPreferences.userId)
        return postgresClient.update(
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3
            WHERE user_id=$1
            RETURNING user_id, program_days_per_week, session_time_length_in_minutes
            """.trimIndent(),
            userProgramPreferences.userId,
            userProgramPreferences.programDaysPerWeek,
            userProgramPreferences.sessionTimeLengthInMinutes,
        )
    }

    fun deleteUserProgramPreferences(userId: Int): Mono<UserProgramPreferences> {
        logger.debug("Deleting user program preferences: {}", userId)
        return postgresClient.update(
            """
            DELETE FROM user_program_preferences
            WHERE user_id=$1
            RETURNING user_id, program_days_per_week, session_time_length_in_minutes
            """.trimIndent(),
            userId,
        )
    }
}
