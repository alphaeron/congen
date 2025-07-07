package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.mockExerciseWorkoutType
import com.congen.model.ExerciseWorkoutType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

class ExerciseWorkoutTypeControllerTest {
    private lateinit var dal: ExerciseWorkoutTypeDAL
    private lateinit var controller: ExerciseWorkoutTypeController

    companion object {
        private const val EXERCISE_NAME = "Bench Press"
        private const val MOVEMENT_TYPE = "horizontal push"
        private const val WORKOUT_TYPE = "dynamic_effort"
        private const val SQUAT_NAME = "Squat"
        private const val SQUAT_MOVEMENT = "squat"
        private const val MAXIMAL_EFFORT = "maximal_effort"
    }

    @BeforeEach
    fun setUp() {
        dal = mock()
        controller = ExerciseWorkoutTypeController(dal)
    }

    @Test
    fun `getAll should return all relationships`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = SQUAT_NAME, movementType = SQUAT_MOVEMENT, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(exerciseWorkoutTypes))
        val response = controller.getAll()

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<List<ExerciseWorkoutType>>)
        StepVerifier.create(body)
            .expectNext(exerciseWorkoutTypes)
            .verifyComplete()
    }

    @Test
    fun `getByExercise should return relationships for an exercise using new mapping`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectExerciseWorkoutTypesByExercise(EXERCISE_NAME)).thenReturn(Mono.just(exerciseWorkoutTypes))
        val result = controller.getByExercise(EXERCISE_NAME)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(exerciseWorkoutTypes), it)
            }
            .verifyComplete()
    }

    @Test
    fun `save should insert and return the relationship`() {
        val input =
            mockExerciseWorkoutType(
                exerciseName = EXERCISE_NAME,
                movementType = MOVEMENT_TYPE,
                workoutType = WORKOUT_TYPE
            )
        whenever(dal.insertExerciseWorkoutType(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE))
            .thenReturn(Mono.just(input))
        val response = controller.save(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE)

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<ExerciseWorkoutType>)
        StepVerifier.create(body)
            .expectNext(input)
            .verifyComplete()
    }

    @Test
    fun `getByMovementType should return relationships for a movementType`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectExerciseWorkoutTypesByMovementType(MOVEMENT_TYPE)).thenReturn(Mono.just(exerciseWorkoutTypes))
        val result = controller.getByMovementType(MOVEMENT_TYPE)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(exerciseWorkoutTypes), it)
            }
            .verifyComplete()
    }
}
