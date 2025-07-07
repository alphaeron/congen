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
import java.time.LocalDateTime

class UserProgramPreferencesDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = mock()
        userProgramPreferencesDAL = UserProgramPreferencesDAL(postgresClient, programmedWorkoutDAL)
    }

    @Test
    fun `selectUserProgramPreferences should return user program preferences`() {
        val userProgramPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.selectIndividual<UserProgramPreferences>(
                "SELECT * FROM user_program_preferences WHERE user_id=$1",
                1,
            ),
        ).thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesDAL.selectUserProgramPreferences(1)
        StepVerifier.create(result).expectNext(userProgramPreferences).verifyComplete()
        verify(postgresClient).selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 1)
    }

    @Test
    fun `selectUserProgramPreferences should return null when not found`() {
        whenever(
            postgresClient.selectIndividual<UserProgramPreferences>(
                "SELECT * FROM user_program_preferences WHERE user_id=$1",
                999,
            ),
        ).thenReturn(Mono.empty())
        val result = userProgramPreferencesDAL.selectUserProgramPreferences(999)
        StepVerifier.create(result).verifyComplete()
        verify(postgresClient).selectIndividual<UserProgramPreferences>("SELECT * FROM user_program_preferences WHERE user_id=$1", 999)
    }

    @Test
    fun `insertUserProgramPreferences should return inserted user program preferences`() {
        val userProgramPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<UserProgramPreferences>(
                """
                INSERT INTO user_program_preferences
                    (user_id, program_days_per_week, session_time_length_in_minutes)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                userProgramPreferences.userId,
                userProgramPreferences.programDaysPerWeek,
                userProgramPreferences.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(userProgramPreferences))
        val result =
            userProgramPreferencesDAL.insertUserProgramPreferences(
                userProgramPreferences.userId,
                userProgramPreferences.programDaysPerWeek,
                userProgramPreferences.sessionTimeLengthInMinutes
            )
        StepVerifier.create(result).expectNext(userProgramPreferences).verifyComplete()
        verify(postgresClient).update<UserProgramPreferences>(
            """
            INSERT INTO user_program_preferences
                (user_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userProgramPreferences.userId,
            userProgramPreferences.programDaysPerWeek,
            userProgramPreferences.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should return updated user program preferences`() {
        val userProgramPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 90,
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3, updated_at=NOW()
            WHERE user_id=$1
            """.trimIndent()

        whenever(
            programmedWorkoutDAL.hasUserExistingWorkouts(userProgramPreferences.userId)
        )
            .thenReturn(Mono.just(false))
        whenever(
            postgresClient.update<UserProgramPreferences>(
                expectedQuery,
                userProgramPreferences.userId,
                userProgramPreferences.programDaysPerWeek,
                userProgramPreferences.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(userProgramPreferences))
        val result =
            userProgramPreferencesDAL.updateUserProgramPreferences(
                userProgramPreferences.userId,
                userProgramPreferences.programDaysPerWeek,
                userProgramPreferences.sessionTimeLengthInMinutes
            )
        StepVerifier.create(result).expectNext(userProgramPreferences).verifyComplete()
        verify(postgresClient).update<UserProgramPreferences>(
            expectedQuery,
            userProgramPreferences.userId,
            userProgramPreferences.programDaysPerWeek,
            userProgramPreferences.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should return updated preferences when user has no existing workouts`() {
        val prefs =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 75,
                createdAt = now,
                updatedAt = now
            )

        // Mock that user has no existing workouts
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts(1)).thenReturn(Mono.just(false))

        val expectedQuery =
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3, updated_at=NOW()
            WHERE user_id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<UserProgramPreferences>(
                expectedQuery,
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
            expectedQuery,
            prefs.userId,
            prefs.programDaysPerWeek,
            prefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should allow changing session time when user has existing workouts`() {
        val currentPrefs =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )
        val updatedPrefs =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 75,
                createdAt = now,
                updatedAt = now
            )

        // Mock that user has existing workouts
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts(1)).thenReturn(Mono.just(true))
        val expectecCheckPrefsQueryString = "SELECT * FROM user_program_preferences WHERE user_id=$1"
        whenever(
            postgresClient.selectIndividual<UserProgramPreferences>(
                expectecCheckPrefsQueryString,
                1
            )
        )
            .thenReturn(Mono.just(currentPrefs))

        val expectedUpdateQueryString =
            """
            UPDATE user_program_preferences
            SET program_days_per_week=$2, session_time_length_in_minutes=$3, updated_at=NOW()
            WHERE user_id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<UserProgramPreferences>(
                expectedUpdateQueryString,
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
        verify(postgresClient).selectIndividual<UserProgramPreferences>(expectecCheckPrefsQueryString, 1)
        verify(postgresClient).update<UserProgramPreferences>(
            expectedUpdateQueryString,
            updatedPrefs.userId,
            updatedPrefs.programDaysPerWeek,
            updatedPrefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateUserProgramPreferences should throw ValidationException when changing program days per week with existing workouts`() {
        val currentPrefs =
            UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 60, createdAt = now, updatedAt = now)
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
    fun `deleteUserProgramPreferences should return deleted user program preferences`() {
        val userProgramPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            DELETE FROM user_program_preferences
            WHERE user_id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<UserProgramPreferences>(
                expectedQuery,
                1,
            ),
        ).thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesDAL.deleteUserProgramPreferences(1)
        StepVerifier.create(result).expectNext(userProgramPreferences).verifyComplete()
        verify(postgresClient).update<UserProgramPreferences>(expectedQuery, 1)
    }
}
