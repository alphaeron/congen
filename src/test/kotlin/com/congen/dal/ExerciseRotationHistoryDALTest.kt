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
                exerciseName = "Bench Press",
                isAccessory = false
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
    fun `should select exercise rotation history by isAccessory`() {
        val isAccessory = false
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    exerciseName = "Bench Press",
                    isAccessory = isAccessory
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    exerciseName = "Squat",
                    isAccessory = isAccessory
                )
            )

        whenever(
            postgresClient.select<ExerciseRotationHistory>(
                "SELECT * FROM exercise_rotation_history WHERE is_accessory=$1 ORDER BY used_at DESC",
                isAccessory
            )
        ).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryDAL.selectByIsAccessory(isAccessory)

        StepVerifier.create(result)
            .expectNext(expectedRecords)
            .verifyComplete()

        verify(
            postgresClient
        ).select<ExerciseRotationHistory>(
            "SELECT * FROM exercise_rotation_history WHERE is_accessory=$1 ORDER BY used_at DESC",
            isAccessory
        )
    }

    @Test
    fun `should select all exercise rotation history records`() {
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    exerciseName = "Bench Press",
                    isAccessory = false
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    exerciseName = "Squat",
                    isAccessory = true
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
                exerciseName = "Bench Press",
                isAccessory = false
            )
        val expectedRecord = exerciseRotationHistory.copy(id = 1L)

        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                INSERT INTO exercise_rotation_history
                    (exercise_name, is_accessory)
                VALUES
                    ($1, $2)
                """.trimIndent(),
                "Bench Press",
                false
            )
        ).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryDAL.insert(exerciseRotationHistory)

        StepVerifier.create(result)
            .expectNext(expectedRecord)
            .verifyComplete()

        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            INSERT INTO exercise_rotation_history
                (exercise_name, is_accessory)
            VALUES
                ($1, $2)
            """.trimIndent(),
            "Bench Press",
            false
        )
    }

    @Test
    fun `should update exercise rotation history`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                exerciseName = "Bench Press",
                isAccessory = true
            )

        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                UPDATE exercise_rotation_history
                SET exercise_name=$2, is_accessory=$3
                WHERE id=$1
                """.trimIndent(),
                1L,
                "Bench Press",
                true
            )
        ).thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryDAL.update(exerciseRotationHistory)

        StepVerifier.create(result)
            .expectNext(exerciseRotationHistory)
            .verifyComplete()

        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            UPDATE exercise_rotation_history
            SET exercise_name=$2, is_accessory=$3
            WHERE id=$1
            """.trimIndent(),
            1L,
            "Bench Press",
            true
        )
    }

    @Test
    fun `should delete exercise rotation history by id`() {
        val id = 1L
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                exerciseName = "Bench Press",
                isAccessory = false
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
}
