package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserExercisePreferenceDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userExercisePreferenceDAL = UserExercisePreferenceDAL(postgresClient)
    }

    @Test
    fun `selectUserExercisePreference should return user exercise preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        whenever(
            postgresClient.selectIndividual<UserExercisePreference>(
                "SELECT * FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
                1,
                "Bench Press",
            ),
        ).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceDAL.selectUserExercisePreference(1, "Bench Press")
        StepVerifier.create(result).expectNext(pref).verifyComplete()
        verify(
            postgresClient,
        ).selectIndividual<UserExercisePreference>(
            "SELECT * FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
            1,
            "Bench Press",
        )
    }

    @Test
    fun `selectUserExercisePreferencesByUser should return list of preferences`() {
        val prefs = listOf(UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true))
        whenever(
            postgresClient.select<UserExercisePreference>(
                """SELECT * FROM user_exercise_preference WHERE user_id=$1""".trimIndent(),
                1,
            ),
        ).thenReturn(Mono.just(prefs))
        val result = userExercisePreferenceDAL.selectUserExercisePreferencesByUser(1)
        StepVerifier.create(result).expectNext(prefs).verifyComplete()
        verify(postgresClient).select<UserExercisePreference>("SELECT * FROM user_exercise_preference WHERE user_id=$1", 1)
    }

    @Test
    fun `insertUserExercisePreference should return inserted preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        whenever(
            postgresClient.update<UserExercisePreference>(
                """
                INSERT INTO user_exercise_preference
                    (user_id, exercise_name, should_avoid)
                VALUES
                    ($1, $2, $3)
                RETURNING user_id, exercise_name, should_avoid
                """.trimIndent(),
                pref.userId,
                pref.exerciseName,
                pref.shouldAvoid,
            ),
        ).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceDAL.insertUserExercisePreference(pref)
        StepVerifier.create(result).expectNext(pref).verifyComplete()
        verify(postgresClient).update<UserExercisePreference>(
            """
            INSERT INTO user_exercise_preference
                (user_id, exercise_name, should_avoid)
            VALUES
                ($1, $2, $3)
            RETURNING user_id, exercise_name, should_avoid
            """.trimIndent(),
            pref.userId,
            pref.exerciseName,
            pref.shouldAvoid,
        )
    }

    @Test
    fun `updateUserExercisePreference should return updated preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = false)
        whenever(
            postgresClient.update<UserExercisePreference>(
                """
                UPDATE user_exercise_preference
                SET should_avoid=$3
                WHERE user_id=$1 AND exercise_name=$2
                RETURNING user_id, exercise_name, should_avoid
                """.trimIndent(),
                pref.userId,
                pref.exerciseName,
                pref.shouldAvoid,
            ),
        ).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceDAL.updateUserExercisePreference(pref)
        StepVerifier.create(result).expectNext(pref).verifyComplete()
        verify(postgresClient).update<UserExercisePreference>(
            """
            UPDATE user_exercise_preference
            SET should_avoid=$3
            WHERE user_id=$1 AND exercise_name=$2
            RETURNING user_id, exercise_name, should_avoid
            """.trimIndent(),
            pref.userId,
            pref.exerciseName,
            pref.shouldAvoid,
        )
    }

    @Test
    fun `deleteUserExercisePreference should return deleted preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        whenever(
            postgresClient.update<UserExercisePreference>(
                "DELETE FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2 RETURNING user_id, exercise_name, should_avoid",
                1,
                "Bench Press",
            ),
        ).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceDAL.deleteUserExercisePreference(1, "Bench Press")
        StepVerifier.create(result).expectNext(pref).verifyComplete()
        verify(
            postgresClient,
        ).update<UserExercisePreference>(
            "DELETE FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2 RETURNING user_id, exercise_name, should_avoid",
            1,
            "Bench Press",
        )
    }
}
