package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserOneRepMax
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

/**
 * Unit tests for UserOneRepMaxDAL.
 *
 * These tests verify the database operations for UserOneRepMax entities,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserOneRepMaxDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    private val userOneRepMax = mockUserOneRepMax()
    private val userOneRepMaxList =
        listOf(
            userOneRepMax,
            mockUserOneRepMax(exerciseName = "Squat", oneRepMax = BigDecimal("150.0"))
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userOneRepMaxDAL = UserOneRepMaxDAL(postgresClient)
    }

    @Test
    fun `should insert user one rep max successfully`() {
        whenever(
            postgresClient.update<UserOneRepMax>(
                """
                INSERT INTO user_one_rep_max
                    (user_id, exercise_name, one_rep_max)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                userOneRepMax.userId,
                userOneRepMax.exerciseName,
                userOneRepMax.oneRepMax,
            )
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
            """
            INSERT INTO user_one_rep_max
                (user_id, exercise_name, one_rep_max)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should select user one rep max successfully`() {
        whenever(
            postgresClient.selectIndividual<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userOneRepMax.userId,
                userOneRepMax.exerciseName,
            )
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.selectUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).selectIndividual<UserOneRepMax>(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
        )
    }

    @Test
    fun `should return error when user one rep max not found`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Non-existent Exercise"
        whenever(
            postgresClient.selectIndividual<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(postgresClient).selectIndividual<UserOneRepMax>(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    @Test
    fun `should select all user one rep maxes by user id successfully`() {
        whenever(
            postgresClient.select<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 ORDER BY exercise_name",
                userOneRepMax.userId,
            )
        ).thenReturn(Mono.just(userOneRepMaxList))
        val result = userOneRepMaxDAL.selectUserOneRepMaxByUser(userOneRepMax.userId)
        StepVerifier.create(result)
            .expectNext(userOneRepMaxList)
            .verifyComplete()
        verify(postgresClient).select<UserOneRepMax>(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 ORDER BY exercise_name",
            userOneRepMax.userId,
        )
    }

    @Test
    fun `should update user one rep max successfully`() {
        val updatedOneRepMax = mockUserOneRepMax(oneRepMax = BigDecimal("250.0"))
        val expectedQuery =
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, updated_at=NOW()
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent()
        whenever(
            postgresClient.update<UserOneRepMax>(
                expectedQuery,
                updatedOneRepMax.userId,
                updatedOneRepMax.exerciseName,
                updatedOneRepMax.oneRepMax,
            )
        ).thenReturn(Mono.just(updatedOneRepMax))
        val result =
            userOneRepMaxDAL.updateUserOneRepMax(
                updatedOneRepMax.userId,
                updatedOneRepMax.exerciseName,
                updatedOneRepMax.oneRepMax
            )
        StepVerifier.create(result)
            .expectNext(updatedOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
            expectedQuery,
            updatedOneRepMax.userId,
            updatedOneRepMax.exerciseName,
            updatedOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should return error when updating non-existent user one rep max`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Non-existent Exercise"
        val oneRepMax = BigDecimal("250.0")
        whenever(
            postgresClient.update<UserOneRepMax>(
                """
                UPDATE user_one_rep_max
                SET one_rep_max=$3, updated_at=NOW()
                WHERE user_id=$1 AND exercise_name=$2
                """.trimIndent(),
                userId,
                exerciseName,
                oneRepMax,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(postgresClient).update<UserOneRepMax>(
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

    @Test
    fun `should delete user one rep max successfully`() {
        whenever(
            postgresClient.update<UserOneRepMax>(
                "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userOneRepMax.userId,
                userOneRepMax.exerciseName,
            )
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.deleteUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
            "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
        )
    }

    @Test
    fun `should return error when deleting non-existent user one rep max`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Non-existent Exercise"
        whenever(
            postgresClient.update<UserOneRepMax>(
                "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(postgresClient).update<UserOneRepMax>(
            "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    @Test
    fun `should upsert user one rep max successfully`() {
        val updatedOneRepMax = mockUserOneRepMax(oneRepMax = BigDecimal("250.0"))
        val expectedQuery =
            """
            INSERT INTO user_one_rep_max
                (user_id, exercise_name, one_rep_max)
            VALUES
                ($1, $2, $3)
            ON CONFLICT (user_id, exercise_name)
            DO UPDATE SET
                one_rep_max = EXCLUDED.one_rep_max,
                updated_at = NOW()
            """.trimIndent()
        whenever(
            postgresClient.update<UserOneRepMax>(
                expectedQuery,
                updatedOneRepMax.userId,
                updatedOneRepMax.exerciseName,
                updatedOneRepMax.oneRepMax,
            )
        ).thenReturn(Mono.just(updatedOneRepMax))
        val result =
            userOneRepMaxDAL.upsertUserOneRepMax(
                updatedOneRepMax.userId,
                updatedOneRepMax.exerciseName,
                updatedOneRepMax.oneRepMax
            )
        StepVerifier.create(result)
            .expectNext(updatedOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
            expectedQuery,
            updatedOneRepMax.userId,
            updatedOneRepMax.exerciseName,
            updatedOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should handle decimal one rep max values`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Deadlift"
        val oneRepMax = BigDecimal("225.5")
        val userOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = oneRepMax)
        whenever(
            postgresClient.update<UserOneRepMax>(
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
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
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

    @Test
    fun `should handle special characters in exercise name`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Barbell Bench Press (Incline)"
        val oneRepMax = BigDecimal("120.0")
        val userOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = oneRepMax)
        whenever(
            postgresClient.update<UserOneRepMax>(
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
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
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

    @Test
    fun `should handle large one rep max values`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "Heavy Deadlift"
        val oneRepMax = BigDecimal("500.0")
        val userOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = oneRepMax)
        whenever(
            postgresClient.update<UserOneRepMax>(
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
        ).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
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
}
