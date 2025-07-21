package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockExerciseRotationHistory
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
    private lateinit var dal: ExerciseRotationHistoryDAL

    private val history = mockExerciseRotationHistory()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        dal = ExerciseRotationHistoryDAL(postgresClient)
    }

    @Test
    fun `selectById returns ExerciseRotationHistory`() {
        whenever(
            postgresClient.selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", history.id)
        ).thenReturn(Mono.just(history))
        val result = dal.selectById(history.id)
        StepVerifier.create(result).expectNext(history).verifyComplete()
        verify(postgresClient).selectIndividual<ExerciseRotationHistory>("SELECT * FROM exercise_rotation_history WHERE id=$1", history.id)
    }

    @Test
    fun `insert returns inserted ExerciseRotationHistory`() {
        val insertHistory = mockExerciseRotationHistory(id = 0L)
        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                """
                INSERT INTO exercise_rotation_history
                    (user_id, exercise_name, is_accessory)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                insertHistory.userId,
                insertHistory.exerciseName,
                insertHistory.isAccessory
            )
        ).thenReturn(Mono.just(insertHistory))
        val result = dal.insert(insertHistory.userId, insertHistory.exerciseName, insertHistory.isAccessory)
        StepVerifier.create(result).expectNext(insertHistory).verifyComplete()
        verify(postgresClient).update<ExerciseRotationHistory>(
            """
            INSERT INTO exercise_rotation_history
                (user_id, exercise_name, is_accessory)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            insertHistory.userId,
            insertHistory.exerciseName,
            insertHistory.isAccessory
        )
    }

    @Test
    fun `deleteById returns deleted ExerciseRotationHistory`() {
        whenever(
            postgresClient.update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", history.id)
        ).thenReturn(Mono.just(history))
        val result = dal.deleteById(history.id)
        StepVerifier.create(result).expectNext(history).verifyComplete()
        verify(postgresClient).update<ExerciseRotationHistory>("DELETE FROM exercise_rotation_history WHERE id=$1", history.id)
    }

    @Test
    fun `update returns updated ExerciseRotationHistory`() {
        val updated = mockExerciseRotationHistory(exerciseName = "Squat", isAccessory = true)
        val expectedQuery =
            """
            UPDATE exercise_rotation_history
            SET user_id=$2, exercise_name=$3, is_accessory=$4
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ExerciseRotationHistory>(
                expectedQuery,
                updated.id,
                updated.userId,
                updated.exerciseName,
                updated.isAccessory,
            )
        ).thenReturn(Mono.just(updated))
        val result = dal.update(updated.id, updated.userId, updated.exerciseName, updated.isAccessory)
        StepVerifier.create(result).expectNext(updated).verifyComplete()
        verify(postgresClient).update<ExerciseRotationHistory>(
            expectedQuery,
            updated.id,
            updated.userId,
            updated.exerciseName,
            updated.isAccessory,
        )
    }
}
