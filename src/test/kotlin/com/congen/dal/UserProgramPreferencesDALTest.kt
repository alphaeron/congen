package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
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
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = mock()
        userProgramPreferencesDAL = UserProgramPreferencesDAL(postgresClient, programmedWorkoutDAL)
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
        val result =
            userProgramPreferencesDAL.insertUserProgramPreferences(
                prefs.userId,
                prefs.programDaysPerWeek,
                prefs.sessionTimeLengthInMinutes
            )
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
    fun `updateUserProgramPreferences should return updated preferences when user has no existing workouts`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 75)

        // Mock that user has no existing workouts
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts(1)).thenReturn(Mono.just(false))

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

        val result =
            userProgramPreferencesDAL.updateUserProgramPreferences(
                prefs.userId,
                prefs.programDaysPerWeek,
                prefs.sessionTimeLengthInMinutes
            )

        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(programmedWorkoutDAL).hasUserExistingWorkouts(1)
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
    fun `updateUserProgramPreferences should allow changing session time when user has existing workouts`() {
        val currentPrefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 60)
        val updatedPrefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 75)

        // Mock that user has existing workouts
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts(1)).thenReturn(Mono.just(true))
        whenever(postgresClient.selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1))
            .thenReturn(Mono.just(currentPrefs))

        whenever(
            postgresClient.update<UserProgramPreferences>(
                """
                UPDATE user_program_preferences
                SET program_days_per_week=$2, session_time_length_in_minutes=$3
                WHERE user_id=$1
                """.trimIndent(),
                updatedPrefs.userId,
                updatedPrefs.programDaysPerWeek,
                updatedPrefs.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(updatedPrefs))

        val result =
            userProgramPreferencesDAL.updateUserProgramPreferences(
                updatedPrefs.userId,
                updatedPrefs.programDaysPerWeek,
                updatedPrefs.sessionTimeLengthInMinutes
            )

        StepVerifier.create(result).expectNext(updatedPrefs).verifyComplete()
        verify(programmedWorkoutDAL).hasUserExistingWorkouts(1)
        verify(postgresClient).selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1)
        verify(postgresClient).update<UserProgramPreferences>(
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3
            WHERE user_id=$1
            """.trimIndent(),
            updatedPrefs.userId,
            updatedPrefs.programDaysPerWeek,
            updatedPrefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should throw ValidationException when changing program days per week with existing workouts`() {
        val currentPrefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 60)
        val newProgramDaysPerWeek = 4
        val expectedMessage =
            "Cannot change program days per week from 3 to 4 for user 1 because they have existing workouts. " +
                "Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts " +
                "and maintain program consistency. To change program frequency, the user must start a new program."

        // Mock that user has existing workouts
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts(1)).thenReturn(Mono.just(true))
        whenever(postgresClient.selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1))
            .thenReturn(Mono.just(currentPrefs))

        val result =
            userProgramPreferencesDAL.updateUserProgramPreferences(
                1,
                newProgramDaysPerWeek,
                60
            )

        StepVerifier.create(result)
            .expectErrorMatches { ex ->
                ex is ValidationException && ex.message == expectedMessage
            }
            .verify()

        verify(programmedWorkoutDAL).hasUserExistingWorkouts(1)
        verify(postgresClient).selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1)
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
