package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserProgramPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserProgramPreferencesDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userProgramPreferencesDAL = UserProgramPreferencesDAL(postgresClient)
    }

    @Test
    fun `selectUserProgramPreferences should return user program preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(
            postgresClient.selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1),
        ).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesDAL.selectUserProgramPreferences(1)
        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(postgresClient).selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1)
    }

    @Test
    fun `insertUserProgramPreferences should return inserted preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(
            postgresClient.update<UserProgramPreferences>(
                """
                INSERT INTO user_program_preferences
                    (user_id, program_days_per_week, session_time_length_in_minutes)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                prefs.userId,
                prefs.programDaysPerWeek,
                prefs.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesDAL.insertUserProgramPreferences(prefs)
        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(postgresClient).update<UserProgramPreferences>(
            """
            INSERT INTO user_program_preferences
                (user_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            prefs.userId,
            prefs.programDaysPerWeek,
            prefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should return updated preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 75)
        whenever(
            postgresClient.update<UserProgramPreferences>(
                """
                UPDATE user_program_preferences
                SET program_days_per_week=$2, session_time_length_in_minutes=$3
                WHERE user_id=$1
                """.trimIndent(),
                prefs.userId,
                prefs.programDaysPerWeek,
                prefs.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesDAL.updateUserProgramPreferences(prefs)
        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(postgresClient).update<UserProgramPreferences>(
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3
            WHERE user_id=$1
            """.trimIndent(),
            prefs.userId,
            prefs.programDaysPerWeek,
            prefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `deleteUserProgramPreferences should return deleted preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(
            postgresClient.update<UserProgramPreferences>(
                """
                DELETE FROM user_program_preferences
                WHERE user_id=$1
                """.trimIndent(),
                1,
            ),
        ).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesDAL.deleteUserProgramPreferences(1)
        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(
            postgresClient,
        ).update<UserProgramPreferences>(
            """
            DELETE FROM user_program_preferences
            WHERE user_id=$1
            """.trimIndent(),
            1,
        )
    }
}
