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

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userOneRepMaxDAL = UserOneRepMaxDAL(postgresClient)
    }

    @Test
    fun `should insert user one rep max successfully`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

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
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should select user one rep max successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val expectedUserOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(
            postgresClient.selectIndividual<UserOneRepMax>(
                "SELECT * FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.just(expectedUserOneRepMax))

        // When
        val result = userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(expectedUserOneRepMax)
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
                ),
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal("150.0"),
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
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("110.0"),
            )

        whenever(
            postgresClient.update<UserOneRepMax>(
                """
                UPDATE user_one_rep_max
                SET one_rep_max=$3, last_updated=CURRENT_TIMESTAMP
                WHERE user_id=$1 AND exercise_name=$2
                """.trimIndent(),
                userOneRepMax.userId,
                userOneRepMax.exerciseName,
                userOneRepMax.oneRepMax,
            )
        ).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxDAL.updateUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

        // Then
        StepVerifier.create(result)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(postgresClient).update<UserOneRepMax>(
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, last_updated=CURRENT_TIMESTAMP
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should return error when updating non-existent user one rep max`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Non-existent Exercise",
                oneRepMax = BigDecimal("110.0"),
            )

        whenever(
            postgresClient.update<UserOneRepMax>(
                """
                UPDATE user_one_rep_max
                SET one_rep_max=$3, last_updated=CURRENT_TIMESTAMP
                WHERE user_id=$1 AND exercise_name=$2
                """.trimIndent(),
                userOneRepMax.userId,
                userOneRepMax.exerciseName,
                userOneRepMax.oneRepMax,
            )
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = userOneRepMaxDAL.updateUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(postgresClient).update<UserOneRepMax>(
            """
            UPDATE user_one_rep_max
            SET one_rep_max=$3, last_updated=CURRENT_TIMESTAMP
            WHERE user_id=$1 AND exercise_name=$2
            """.trimIndent(),
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should delete user one rep max successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val deletedUserOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(
            postgresClient.update<UserOneRepMax>(
                "DELETE FROM user_one_rep_max WHERE user_id=$1 AND exercise_name=$2",
                userId,
                exerciseName,
            )
        ).thenReturn(Mono.just(deletedUserOneRepMax))

        // When
        val result = userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedUserOneRepMax)
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
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("225.5"),
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

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
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should handle special characters in exercise name`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Barbell Bench Press (Incline)",
                oneRepMax = BigDecimal("120.0"),
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

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
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }

    @Test
    fun `should handle large one rep max values`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Heavy Deadlift",
                oneRepMax = BigDecimal("500.0"),
            )

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

        // When
        val result = userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax.userId, userOneRepMax.exerciseName, userOneRepMax.oneRepMax)

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
            userOneRepMax.userId,
            userOneRepMax.exerciseName,
            userOneRepMax.oneRepMax,
        )
    }
}
