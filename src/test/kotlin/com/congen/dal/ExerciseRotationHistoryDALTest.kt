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
import java.time.LocalDateTime

class ExerciseRotationHistoryDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var dal: ExerciseRotationHistoryDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        dal = ExerciseRotationHistoryDAL(postgresClient)
    }

    @Test
    fun `selectById returns ExerciseRotationHistory`() {
        val history = ExerciseRotationHistory(
            id = 1L,
            userId = 2,
            exerciseName = "Bench Press",
            isAccessory = false,
            createdAt = now
        )
        whenever(postgresClient.selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", 1L)).thenReturn(Mono.just(history))
        val result = dal.selectById(1L)
        StepVerifier.create(result).expectNext(history).verifyComplete()
        verify(postgresClient).selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", 1L)
    }

    @Test
    fun `insert returns inserted ExerciseRotationHistory`() {
        val history = ExerciseRotationHistory(
            id = 0L,
            userId = 2,
            exerciseName = "Bench Press",
            isAccessory = false,
            createdAt = now
        )
        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                INSERT INTO exercise_rotation_history
                    (user_id, exercise_name, is_accessory)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                2, "Bench Press", false
            )
        ).thenReturn(Mono.just(history))
        val result = dal.insert(2, "Bench Press", false)
        StepVerifier.create(result).expectNext(history).verifyComplete()
        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            INSERT INTO exercise_rotation_history
                (user_id, exercise_name, is_accessory)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            2, "Bench Press", false
        )
    }

    @Test
    fun `deleteById returns deleted ExerciseRotationHistory`() {
        val history = ExerciseRotationHistory(
            id = 1L,
            userId = 2,
            exerciseName = "Bench Press",
            isAccessory = false,
            createdAt = now
        )
        whenever(postgresClient.update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", 1L)).thenReturn(Mono.just(history))
        val result = dal.deleteById(1L)
        StepVerifier.create(result).expectNext(history).verifyComplete()
        verify(postgresClient).update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", 1L)
    }
}
