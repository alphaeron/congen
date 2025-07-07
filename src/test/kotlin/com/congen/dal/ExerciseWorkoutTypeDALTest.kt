package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockExerciseWorkoutType
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

    private val relationship = mockExerciseWorkoutType()
    private val relationshipList =
        listOf(
            relationship,
            mockExerciseWorkoutType(workoutType = "maximal_effort")
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        dal = ExerciseWorkoutTypeDAL(postgresClient)
    }

    @Test
    fun `selectExerciseWorkoutType should return the correct relationship`() {
        val expectedQuery = "SELECT * FROM exercise_workout_type WHERE exercise_name=$1 AND movement_type=$2 AND workout_type=$3"
        whenever(
            postgresClient.selectIndividual(
                eq(expectedQuery),
                eq(ExerciseWorkoutType::class),
                eq(relationship.exerciseName),
                eq(relationship.movementType),
                eq(relationship.workoutType)
            ),
        ).thenReturn(Mono.just(relationship))
        val result = dal.selectExerciseWorkoutType(relationship.exerciseName, relationship.movementType, relationship.workoutType)
        StepVerifier.create(result)
            .expectNext(relationship)
            .verifyComplete()
        verify(postgresClient).selectIndividual(
            eq(expectedQuery),
            eq(ExerciseWorkoutType::class),
            eq(relationship.exerciseName),
            eq(relationship.movementType),
            eq(relationship.workoutType)
        )
    }

    @Test
    fun `selectExerciseWorkoutTypesByExercise should return a list`() {
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class), any())).thenReturn(Mono.just(relationshipList))
        val result = dal.selectExerciseWorkoutTypesByExercise(relationship.exerciseName)
        StepVerifier.create(result)
            .expectNext(relationshipList)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class), eq(relationship.exerciseName))
    }

    @Test
    fun `selectAllExerciseWorkoutTypes should return all relationships`() {
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class))).thenReturn(Mono.just(relationshipList))
        val result = dal.selectAllExerciseWorkoutTypes()
        StepVerifier.create(result)
            .expectNext(relationshipList)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class))
    }

    @Test
    fun `insertExerciseWorkoutType should insert and return the relationship`() {
        whenever(postgresClient.update(any(), eq(ExerciseWorkoutType::class), any(), any(), any())).thenReturn(Mono.just(relationship))
        val result = dal.insertExerciseWorkoutType(relationship.exerciseName, relationship.movementType, relationship.workoutType)
        StepVerifier.create(result)
            .expectNext(relationship)
            .verifyComplete()
        verify(
            postgresClient
        ).update(
            any(),
            eq(ExerciseWorkoutType::class),
            eq(relationship.exerciseName),
            eq(relationship.movementType),
            eq(relationship.workoutType)
        )
    }

    @Test
    fun `deleteExerciseWorkoutType should delete and return the relationship`() {
        whenever(postgresClient.update(any(), eq(ExerciseWorkoutType::class), any(), any(), any())).thenReturn(Mono.just(relationship))
        val result = dal.deleteExerciseWorkoutType(relationship.exerciseName, relationship.movementType, relationship.workoutType)
        StepVerifier.create(result)
            .expectNext(relationship)
            .verifyComplete()
        verify(
            postgresClient
        ).update(
            any(),
            eq(ExerciseWorkoutType::class),
            eq(relationship.exerciseName),
            eq(relationship.movementType),
            eq(relationship.workoutType)
        )
    }

    @Test
    fun `selectExerciseWorkoutTypesByMovementType should return a list`() {
        whenever(postgresClient.select(any(), eq(ExerciseWorkoutType::class), any())).thenReturn(Mono.just(relationshipList))
        val result = dal.selectExerciseWorkoutTypesByMovementType(relationship.movementType)
        StepVerifier.create(result)
            .expectNext(relationshipList)
            .verifyComplete()
        verify(postgresClient).select(any(), eq(ExerciseWorkoutType::class), eq(relationship.movementType))
    }
}
