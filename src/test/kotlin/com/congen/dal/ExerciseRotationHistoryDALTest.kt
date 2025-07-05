package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseRotationHistoryDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseRotationHistoryDAL = ExerciseRotationHistoryDAL(postgresClient)
    }

    @Test
    fun `should select exercise rotation history by id`() {
        val id = 1L
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary"
            )

        whenever(
            postgresClient.selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", id)
        ).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryDAL.selectById(id)

        StepVerifier.create(result)
            .expectNext(expectedRecord)
            .verifyComplete()

        verify(postgresClient).selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", id)
    }

    @Test
    fun `should select exercise rotation history by user id`() {
        val userId = 123L
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    category = "primary"
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = userId,
                    exerciseName = "Squat",
                    category = "secondary"
                )
            )

        whenever(
            postgresClient.select<ExerciseRotationHistory>(
                "SELECT * FROM exercise_rotation_history WHERE user_id=$1 ORDER BY used_at DESC",
                userId
            )
        ).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryDAL.selectByUserId(userId)

        StepVerifier.create(result)
            .expectNext(expectedRecords)
            .verifyComplete()

        verify(
            postgresClient
        ).select<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE user_id=$1 ORDER BY used_at DESC", userId)
    }

    @Test
    fun `should select exercise rotation history by user id and category`() {
        val userId = 123L
        val category = "primary"
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    category = category
                )
            )

        whenever(
            postgresClient.select<ExerciseRotationHistory>(
                "SELECT * FROM exercise_rotation_history WHERE user_id=$1 AND category=$2 ORDER BY used_at DESC",
                userId,
                category
            )
        ).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryDAL.selectByUserIdAndCategory(userId, category)

        StepVerifier.create(result)
            .expectNext(expectedRecords)
            .verifyComplete()

        verify(
            postgresClient
        ).select<ExerciseRotationHistory>(
            "SELECT * FROM exercise_rotation_history WHERE user_id=$1 AND category=$2 ORDER BY used_at DESC",
            userId,
            category
        )
    }

    @Test
    fun `should select all exercise rotation history records`() {
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 123L,
                    exerciseName = "Bench Press",
                    category = "primary"
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 456L,
                    exerciseName = "Squat",
                    category = "secondary"
                )
            )

        whenever(
            postgresClient.select<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history ORDER BY used_at DESC")
        ).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryDAL.selectAll()

        StepVerifier.create(result)
            .expectNext(expectedRecords)
            .verifyComplete()

        verify(postgresClient).select<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history ORDER BY used_at DESC")
    }

    @Test
    fun `should insert exercise rotation history`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 0L,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary"
            )
        val expectedRecord = exerciseRotationHistory.copy(id = 1L)

        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                INSERT INTO exercise_rotation_history
                    (user_id, exercise_name, category)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                123L,
                "Bench Press",
                "primary"
            )
        ).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryDAL.insert(exerciseRotationHistory)

        StepVerifier.create(result)
            .expectNext(expectedRecord)
            .verifyComplete()

        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            INSERT INTO exercise_rotation_history
                (user_id, exercise_name, category)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            123L,
            "Bench Press",
            "primary"
        )
    }

    @Test
    fun `should update exercise rotation history`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "secondary"
            )

        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                UPDATE exercise_rotation_history
                SET user_id=$2, exercise_name=$3, category=$4
                WHERE id=$1
                """.trimIndent(),
                1L,
                123L,
                "Bench Press",
                "secondary"
            )
        ).thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryDAL.update(exerciseRotationHistory)

        StepVerifier.create(result)
            .expectNext(exerciseRotationHistory)
            .verifyComplete()

        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            UPDATE exercise_rotation_history
            SET user_id=$2, exercise_name=$3, category=$4
            WHERE id=$1
            """.trimIndent(),
            1L,
            123L,
            "Bench Press",
            "secondary"
        )
    }

    @Test
    fun `should delete exercise rotation history by id`() {
        val id = 1L
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary"
            )

        whenever(
            postgresClient.update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", id)
        ).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryDAL.deleteById(id)

        StepVerifier.create(result)
            .expectNext(expectedRecord)
            .verifyComplete()

        verify(postgresClient).update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", id)
    }

    @Test
    fun `should delete exercise rotation history by user id`() {
        val userId = 123L
        val deletedCount = 2

        whenever(
            postgresClient.update<Int>("DELETE FROM exercise_rotation_history WHERE user_id=$1", userId)
        ).thenReturn(Mono.just(deletedCount))

        val result = exerciseRotationHistoryDAL.deleteByUserId(userId)

        StepVerifier.create(result)
            .expectNext(deletedCount)
            .verifyComplete()

        verify(postgresClient).update<Int>("DELETE FROM exercise_rotation_history WHERE user_id=$1", userId)
    }
} 
