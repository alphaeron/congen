package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
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

    @BeforeEach
    fun setUp() {
        dal = mock()
        controller = ExerciseWorkoutTypeController(dal)
    }

    @Test
    fun `getAll should return all relationships`() {
        whenever(dal.selectAllExerciseWorkoutTypes()).thenReturn(
            Mono.just(
                listOf(
                    ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                    ExerciseWorkoutType("Squat", "squat", "maximal_effort"),
                ),
            ),
        )
        val response = controller.getAll()

        // Since getAll returns ResponseEntity<Mono<List<...>>>, unwrap the Mono
        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<List<ExerciseWorkoutType>>)
        StepVerifier.create(body)
            .expectNext(
                listOf(
                    ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                    ExerciseWorkoutType("Squat", "squat", "maximal_effort"),
                ),
            )
            .verifyComplete()
    }

    @Test
    fun `getByExercise should return relationships for an exercise using new mapping`() {
        whenever(dal.selectExerciseWorkoutTypesByExercise("Bench Press")).thenReturn(
            Mono.just(
                listOf(
                    ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                    ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
                ),
            ),
        )
        // Simulate calling the new mapping /exercise/{exerciseName}
        val result = controller.getByExercise("Bench Press")
        StepVerifier.create(result)
            .assertNext {
                assertEquals(
                    ResponseEntity.ok(
                        listOf(
                            ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                            ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
                        ),
                    ),
                    it,
                )
            }
            .verifyComplete()
    }

    @Test
    fun `save should insert and return the relationship`() {
        val input = ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort")
        whenever(dal.insertExerciseWorkoutType(input.exerciseName, input.movementType, input.workoutType)).thenReturn(Mono.just(input))
        val response = controller.save(input.exerciseName, input.movementType, input.workoutType)

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<ExerciseWorkoutType>)
        StepVerifier.create(body)
            .expectNext(input)
            .verifyComplete()
    }

    @Test
    fun `getByMovementType should return relationships for a movementType`() {
        whenever(dal.selectExerciseWorkoutTypesByMovementType("horizontal push")).thenReturn(
            Mono.just(
                listOf(
                    ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                    ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
                ),
            ),
        )
        val result = controller.getByMovementType("horizontal push")
        StepVerifier.create(result)
            .assertNext {
                assertEquals(
                    ResponseEntity.ok(
                        listOf(
                            ExerciseWorkoutType("Bench Press", "horizontal push", "dynamic_effort"),
                            ExerciseWorkoutType("Bench Press", "horizontal push", "maximal_effort"),
                        ),
                    ),
                    it,
                )
            }
            .verifyComplete()
    }
}
