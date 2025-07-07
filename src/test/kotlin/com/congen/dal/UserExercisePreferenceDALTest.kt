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
import java.time.LocalDateTime

class UserExercisePreferenceDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userExercisePreferenceDAL = UserExercisePreferenceDAL(postgresClient)
    }

    @Test
    fun `selectUserExercisePreference should return user exercise preference`() {
        val userExercisePreference = UserExercisePreference(
            userId = 1, 
            exerciseName = "Bench Press", 
            shouldAvoid = true,
            createdAt = now
        )
        whenever(
            postgresClient.selectIndividual<UserExercisePreference>(
                "SELECT * FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
                1,
                "Bench Press",
            ),
        ).thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceDAL.selectUserExercisePreference(1, "Bench Press")
        StepVerifier.create(result).expectNext(userExercisePreference).verifyComplete()
        verify(
            postgresClient,
        ).selectIndividual<UserExercisePreference>("SELECT * FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2", 1, "Bench Press")
    }

    @Test
    fun `selectUserExercisePreferencesByUser should return list of user exercise preferences`() {
        val userExercisePreferenceList = listOf(UserExercisePreference(
            userId = 1, 
            exerciseName = "Bench Press", 
            shouldAvoid = true,
            createdAt = now
        ))
        whenever(
            postgresClient.select<UserExercisePreference>("SELECT * FROM user_exercise_preference WHERE user_id=$1", 1),
        ).thenReturn(Mono.just(userExercisePreferenceList))
        val result = userExercisePreferenceDAL.selectUserExercisePreferencesByUser(1)
        StepVerifier.create(result).expectNext(userExercisePreferenceList).verifyComplete()
        verify(postgresClient).select<UserExercisePreference>("SELECT * FROM user_exercise_preference WHERE user_id=$1", 1)
    }

    @Test
    fun `insertUserExercisePreference should return inserted user exercise preference`() {
        val userExercisePreference = UserExercisePreference(
            userId = 1, 
            exerciseName = "Bench Press", 
            shouldAvoid = true,
            createdAt = now
        )
        whenever(
            postgresClient.update<UserExercisePreference>(
                """
                INSERT INTO user_exercise_preference
                    (user_id, exercise_name, should_avoid)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                userExercisePreference.userId,
                userExercisePreference.exerciseName,
                userExercisePreference.shouldAvoid,
            ),
        ).thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceDAL.insertUserExercisePreference(userExercisePreference.userId, userExercisePreference.exerciseName, userExercisePreference.shouldAvoid)
        StepVerifier.create(result).expectNext(userExercisePreference).verifyComplete()
        verify(postgresClient).update<UserExercisePreference>(
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

    @Test
    fun `updateUserExercisePreference should return updated user exercise preference`() {
        val userExercisePreference = UserExercisePreference(
            userId = 1, 
            exerciseName = "Bench Press", 
            shouldAvoid = false,
            createdAt = now
        )
        whenever(
            postgresClient.update<UserExercisePreference>(
                """
                UPDATE user_exercise_preference
                SET should_avoid=$3
                WHERE user_id=$1 AND exercise_name=$2
                """.trimIndent(),
                userExercisePreference.userId,
                userExercisePreference.exerciseName,
                userExercisePreference.shouldAvoid,
            ),
        ).thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceDAL.updateUserExercisePreference(userExercisePreference.userId, userExercisePreference.exerciseName, userExercisePreference.shouldAvoid)
        StepVerifier.create(result).expectNext(userExercisePreference).verifyComplete()
        verify(postgresClient).update<UserExercisePreference>(
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

    @Test
    fun `deleteUserExercisePreference should return deleted user exercise preference`() {
        val userExercisePreference = UserExercisePreference(
            userId = 1, 
            exerciseName = "Bench Press", 
            shouldAvoid = true,
            createdAt = now
        )
        whenever(
            postgresClient.update<UserExercisePreference>(
                "DELETE FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
                1,
                "Bench Press",
            ),
        ).thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceDAL.deleteUserExercisePreference(1, "Bench Press")
        StepVerifier.create(result).expectNext(userExercisePreference).verifyComplete()
        verify(
            postgresClient,
        ).update<UserExercisePreference>(
            "DELETE FROM user_exercise_preference WHERE user_id=$1 AND exercise_name=$2",
            1,
            "Bench Press",
        )
    }
}
