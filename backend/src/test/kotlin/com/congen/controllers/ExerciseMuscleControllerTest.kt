package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockExerciseMuscle
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

    companion object {
        private const val EXERCISE_NAME = "Bench Press"
        private const val MUSCLE_NAME = "Chest"
        private const val NON_EXISTENT_EXERCISE = "NonExistent"
        private const val NON_EXISTENT_MUSCLE = "NonExistent"
        private const val SQUAT_NAME = "Back Squat"
        private const val LEGS_MUSCLE = "Legs"
    }

    @BeforeEach
    fun setUp() {
        exerciseMuscleDAL = mock()
        exerciseMuscleController = ExerciseMuscleController(exerciseMuscleDAL)
    }

    @Test
    fun `getExerciseMuscle should return exercise muscle when found`() {
        val exerciseMuscle = mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME)
        whenever(exerciseMuscleDAL.selectExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME)).thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleController.getExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscle))
            .verifyComplete()

        verify(exerciseMuscleDAL).selectExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME)
    }

    @Test
    fun `getExerciseMuscle should return not found when exercise muscle not found`() {
        whenever(exerciseMuscleDAL.selectExerciseMuscle(NON_EXISTENT_EXERCISE, NON_EXISTENT_MUSCLE))
            .thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2")))

        val result = exerciseMuscleController.getExerciseMuscle(NON_EXISTENT_EXERCISE, NON_EXISTENT_MUSCLE)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseMuscleDAL).selectExerciseMuscle(NON_EXISTENT_EXERCISE, NON_EXISTENT_MUSCLE)
    }

    @Test
    fun `save should return saved exercise muscle`() {
        val exerciseMuscle = mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME)
        whenever(exerciseMuscleDAL.insertExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME))
            .thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleController.save(EXERCISE_NAME, MUSCLE_NAME)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<ExerciseMuscle>)
            .expectNext(exerciseMuscle)
            .verifyComplete()
        verify(exerciseMuscleDAL).insertExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME)
    }

    @Test
    fun `getAll should return all exercise muscles`() {
        val exerciseMuscleList =
            listOf(
                mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME),
                mockExerciseMuscle(exerciseName = SQUAT_NAME, muscleName = LEGS_MUSCLE)
            )
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(exerciseMuscleList))

        val result = exerciseMuscleController.getAll()

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ExerciseMuscle>>)
            .expectNext(exerciseMuscleList)
            .verifyComplete()
        verify(exerciseMuscleDAL).selectAllExerciseMuscle()
    }
}
