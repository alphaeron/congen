package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockExerciseMuscle
import com.congen.mockMuscle
import com.congen.model.Muscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class MuscleControllerTest {
    private lateinit var muscleDAL: MuscleDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var muscleController: MuscleController

    companion object {
        private const val MUSCLE_NAME = "Chest"
        private const val NON_EXISTENT_MUSCLE = "NonExistent"
        private const val BENCH_PRESS = "Bench Press"
        private const val PUSH_UP = "Push-Up"
        private const val BACK_NAME = "Back"
        private const val BACK_DESCRIPTION = "Back muscles"
    }

    @BeforeEach
    fun setUp() {
        muscleDAL = mock()
        exerciseMuscleDAL = mock()
        muscleController = MuscleController(muscleDAL, exerciseMuscleDAL)
    }

    @Test
    fun `save should return saved muscle`() {
        val muscle = mockMuscle(name = MUSCLE_NAME)
        whenever(muscleDAL.insertMuscle(MUSCLE_NAME, muscle.description)).thenReturn(Mono.just(muscle))
        val result = muscleController.save(MUSCLE_NAME, muscle.description)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<Muscle>
        StepVerifier.create(body).expectNext(muscle).verifyComplete()
        verify(muscleDAL).insertMuscle(MUSCLE_NAME, muscle.description)
    }

    @Test
    fun `get should return muscle when found`() {
        val muscle = mockMuscle(name = MUSCLE_NAME)
        whenever(muscleDAL.selectMuscleByName(MUSCLE_NAME)).thenReturn(Mono.just(muscle))
        val result = muscleController.get(MUSCLE_NAME)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(muscle)).verifyComplete()
        verify(muscleDAL).selectMuscleByName(MUSCLE_NAME)
    }

    @Test
    fun `get should return not found when muscle not found`() {
        whenever(
            muscleDAL.selectMuscleByName(NON_EXISTENT_MUSCLE)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM muscle WHERE name=$1")))
        val result = muscleController.get(NON_EXISTENT_MUSCLE)
        StepVerifier.create(result).expectNext(ResponseEntity.notFound().build()).verifyComplete()
        verify(muscleDAL).selectMuscleByName(NON_EXISTENT_MUSCLE)
    }

    @Test
    fun `getExercise should return exercise muscles when found`() {
        val muscle = mockMuscle(name = MUSCLE_NAME)
        val exerciseMuscles =
            listOf(
                mockExerciseMuscle(exerciseName = BENCH_PRESS, muscleName = MUSCLE_NAME),
                mockExerciseMuscle(exerciseName = PUSH_UP, muscleName = MUSCLE_NAME)
            )
        whenever(muscleDAL.selectMuscleByName(MUSCLE_NAME)).thenReturn(Mono.just(muscle))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(MUSCLE_NAME)).thenReturn(Mono.just(exerciseMuscles))
        val result = muscleController.getExercise(MUSCLE_NAME)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(exerciseMuscles)).verifyComplete()
        verify(muscleDAL).selectMuscleByName(MUSCLE_NAME)
        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(MUSCLE_NAME)
    }

    @Test
    fun `getExercise should return not found when no exercises found`() {
        val muscle = mockMuscle(name = NON_EXISTENT_MUSCLE, description = "A non-existent muscle")
        whenever(muscleDAL.selectMuscleByName(NON_EXISTENT_MUSCLE)).thenReturn(Mono.just(muscle))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(NON_EXISTENT_MUSCLE)).thenReturn(Mono.just(emptyList()))
        val result = muscleController.getExercise(NON_EXISTENT_MUSCLE)
        StepVerifier.create(result).expectNext(ResponseEntity.notFound().build()).verifyComplete()
        verify(muscleDAL).selectMuscleByName(NON_EXISTENT_MUSCLE)
        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(NON_EXISTENT_MUSCLE)
    }

    @Test
    fun `getAll should return all muscles`() {
        val muscles =
            listOf(
                mockMuscle(name = MUSCLE_NAME),
                mockMuscle(name = BACK_NAME, description = BACK_DESCRIPTION)
            )
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(muscles))
        val result = muscleController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<List<Muscle>>
        StepVerifier.create(body).expectNext(muscles).verifyComplete()
        verify(muscleDAL).selectMuscles()
    }
}
