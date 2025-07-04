package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseWorkoutType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseWorkoutTypeDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var dal: ExerciseWorkoutTypeDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        dal = ExerciseWorkoutTypeDAL(postgresClient)
    }

    @Test
    fun `selectExerciseWorkoutType should return the correct relationship`() {
        val expected = ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        whenever(
            postgresClient.selectIndividual(any(), eq(ExerciseWorkoutType::class), any(), any(), any()),
        ).thenReturn(Mono.just(expected))
        val result = dal.selectExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete()
        verify(
            postgresClient,
        ).selectIndividual(any(), eq(ExerciseWorkoutType::class), eq("Bench Press"), eq("horizontal push"), eq("dynamic_effort"))
    }

    @Test
    fun `selectExerciseWorkoutTypesByExercise should return a list`() {
        val expected =
            listOf(
                ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
            )
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class), any())).thenReturn(Mono.just(expected))
        val result = dal.selectExerciseWorkoutTypesByExercise("Bench Press")
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class), eq("Bench Press"))
    }

    @Test
    fun `selectAllExerciseWorkoutTypes should return all relationships`() {
        val expected =
            listOf(
                ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                ExerciseWorkoutType("Squat", "squat", "maximal_effort"),
            )
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class))).thenReturn(Mono.just(expected))
        val result = dal.selectAllExerciseWorkoutTypes()
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class))
    }

    @Test
    fun `insertExerciseWorkoutType should insert and return the relationship`() {
        val input = ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        whenever(postgresClient.update(any(), eq(ExerciseWorkoutType::class), any(), any(), any())).thenReturn(Mono.just(input))
        val result = dal.insertExerciseWorkoutType(input)
        StepVerifier.create(result)
            .expectNext(input)
            .verifyComplete()
        verify(postgresClient).update(any(), eq(ExerciseWorkoutType::class), eq("Bench Press"), eq("horizontal push"), eq("dynamic_effort"))
    }

    @Test
    fun `deleteExerciseWorkoutType should delete and return the relationship`() {
        val expected = ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        whenever(postgresClient.update(any(), eq(ExerciseWorkoutType::class), any(), any(), any())).thenReturn(Mono.just(expected))
        val result = dal.deleteExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete()
        verify(postgresClient).update(any(), eq(ExerciseWorkoutType::class), eq("Bench Press"), eq("horizontal push"), eq("dynamic_effort"))
    }

    @Test
    fun `selectExerciseWorkoutTypesByMovementType should return a list`() {
        val expected =
            listOf(
                ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
            )
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class), any())).thenReturn(Mono.just(expected))
        val result = dal.selectExerciseWorkoutTypesByMovementType("horizontal push")
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class), eq("horizontal push"))
    }
}
