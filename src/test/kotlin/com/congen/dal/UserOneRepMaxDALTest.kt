package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

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
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userOneRepMaxDAL = UserOneRepMaxDAL(postgresClient)
    }

    @Test
    fun `should insert user one rep max successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("225.5")
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                updatedAt = now
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)

        // Then
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
    fun `should select user one rep max successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("225.5"),
                updatedAt = now
            )

        whenever(
            postgresClient.selectIndividual<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(postgresClient).selectIndividual<UserOneRepMax>(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    @Test
    fun `should return error when user one rep max not found`() {
        // Given
        val userId = 1
        val exerciseName = "Non-existent Exercise"

        whenever(
            postgresClient.selectIndividual<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)

        // Then
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
        // Given
        val userId = 1
        val expectedUserOneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0"),
                    updatedAt = now
                ),
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal("150.0"),
                    updatedAt = now
                ),
            )

        whenever(
            postgresClient.select<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 ORDER BY exercise_name",
                userId,
            )
        ).thenReturn(Mono.just(expectedUserOneRepMaxes))

        // When
        val result = userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)

        // Then
        StepVerifier.create(result)
            .expectNext(expectedUserOneRepMaxes)
            .verifyComplete()

        verify(postgresClient).select<UserOneRepMax>(
            "SELECT * FROM user_one_rep_max WHERE user_id=$1 ORDER BY exercise_name",
            userId,
        )
    }

    @Test
    fun `should update user one rep max successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("250.0")
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                updatedAt = now
            )
        val expectedQuery =
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, updated_at=NOW()
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent()
        whenever(
            postgresClient.update<UserOneRepMax>(
                expectedQuery,
                userId,
                exerciseName,
                oneRepMax,
            )
        ).thenReturn(Mono.just(userOneRepMax))
        // When
        val result = userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)
        // Then
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(postgresClient).update<UserOneRepMax>(
            expectedQuery,
            userId,
            exerciseName,
            oneRepMax,
        )
    }

    @Test
    fun `should return error when updating non-existent user one rep max`() {
        // Given
        val userId = 1
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

        // When
        val result = userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)

        // Then
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
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("225.5"),
                updatedAt = now
            )

        whenever(
            postgresClient.update<UserOneRepMax>(
                "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(postgresClient).update<UserOneRepMax>(
            "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
            userId,
            exerciseName,
        )
    }

    @Test
    fun `should return error when deleting non-existent user one rep max`() {
        // Given
        val userId = 1
        val exerciseName = "Non-existent Exercise"

        whenever(
            postgresClient.update<UserOneRepMax>(
                "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)

        // Then
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
    fun `should handle decimal one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "Deadlift"
        val oneRepMax = BigDecimal("225.5")
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                updatedAt = now
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)

        // Then
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
        // Given
        val userId = 1
        val exerciseName = "Barbell Bench Press (Incline)"
        val oneRepMax = BigDecimal("120.0")
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                updatedAt = now
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)

        // Then
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
        // Given
        val userId = 1
        val exerciseName = "Heavy Deadlift"
        val oneRepMax = BigDecimal("500.0")
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                updatedAt = now
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)

        // Then
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
