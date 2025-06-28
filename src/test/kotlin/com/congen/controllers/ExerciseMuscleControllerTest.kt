package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.model.ExerciseMuscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseMuscleControllerTest {
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseMuscleController: ExerciseMuscleController

    @BeforeEach
    fun setUp() {
        exerciseMuscleDAL = mock()
        exerciseMuscleController = ExerciseMuscleController(exerciseMuscleDAL)
    }

    @Test
    fun `save should return saved exercise muscle`() {
        // Given
        val exerciseMuscle = ExerciseMuscle(
            exerciseName = "Bench Press",
            muscleName = "Chest"
        )
        whenever(exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle)).thenReturn(Mono.just(exerciseMuscle))

        // When
        val result = exerciseMuscleController.save(exerciseMuscle)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<ExerciseMuscle>)
            .expectNext(exerciseMuscle)
            .verifyComplete()
        verify(exerciseMuscleDAL).insertExerciseMuscle(exerciseMuscle)
    }

    @Test
    fun `getAll should return all exercise muscles`() {
        // Given
        val exerciseMuscleList = listOf(
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest"
            ),
            ExerciseMuscle(
                exerciseName = "Squat",
                muscleName = "Legs"
            )
        )
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(exerciseMuscleList))

        // When
        val result = exerciseMuscleController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ExerciseMuscle>>)
            .expectNext(exerciseMuscleList)
            .verifyComplete()
        verify(exerciseMuscleDAL).selectAllExerciseMuscle()
    }
} 