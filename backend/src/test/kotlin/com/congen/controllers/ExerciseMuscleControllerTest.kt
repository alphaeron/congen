package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockExerciseMuscle
import com.congen.model.ExerciseMuscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
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
    fun `save should return saved exercise muscle`() {
        val exerciseMuscle = mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME)
        whenever(exerciseMuscleDAL.insertExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME))
            .thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleController.save(EXERCISE_NAME, MUSCLE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscle))
            .verifyComplete()
        verify(exerciseMuscleDAL).insertExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME)
    }

    @Test
    fun `save should handle special characters in names`() {
        val specialExercise = "Cable Fly (Smith Machine)"
        val specialMuscle = "EZ-Bar"
        val exerciseMuscle = mockExerciseMuscle(exerciseName = specialExercise, muscleName = specialMuscle)
        whenever(exerciseMuscleDAL.insertExerciseMuscle(specialExercise, specialMuscle))
            .thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleController.save(specialExercise, specialMuscle)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscle))
            .verifyComplete()
        verify(exerciseMuscleDAL).insertExerciseMuscle(specialExercise, specialMuscle)
    }

    @Test
    fun `save should propagate database errors`() {
        val ex = DatabaseQueryException("some db error")
        whenever(exerciseMuscleDAL.insertExerciseMuscle(EXERCISE_NAME, MUSCLE_NAME))
            .thenReturn(Mono.error(ex))

        val result = exerciseMuscleController.save(EXERCISE_NAME, MUSCLE_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
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

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscleList))
            .verifyComplete()
        verify(exerciseMuscleDAL).selectAllExerciseMuscle()
    }

    @Test
    fun `getAll should return empty list`() {
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(emptyList()))

        val result = exerciseMuscleController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList<ExerciseMuscle>()))
            .verifyComplete()
        verify(exerciseMuscleDAL).selectAllExerciseMuscle()
    }

    @Test
    fun `getAll should return single result`() {
        val single = listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME))
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(single))

        val result = exerciseMuscleController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(single))
            .verifyComplete()
        verify(exerciseMuscleDAL).selectAllExerciseMuscle()
    }

    @Test
    fun `getAll should propagate database errors`() {
        val ex = DatabaseQueryException("db error")
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.error(ex))

        val result = exerciseMuscleController.getAll()

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
