package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ExerciseMuscle
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

    @BeforeEach
    fun setUp() {
        muscleDAL = mock()
        exerciseMuscleDAL = mock()
        muscleController = MuscleController(muscleDAL, exerciseMuscleDAL)
    }

    @Test
    fun `save should return saved muscle`() {
        // Given
        val muscle =
            Muscle(
                name = "Chest",
                description = "Chest muscles",
            )

        whenever(muscleDAL.insertMuscle(muscle.name, muscle.description)).thenReturn(Mono.just(muscle))

        // When
        val result = muscleController.save(muscle.name, muscle.description)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<Muscle>)
            .expectNext(muscle)
            .verifyComplete()

        verify(muscleDAL).insertMuscle(muscle.name, muscle.description)
    }

    @Test
    fun `get should return muscle when found`() {
        // Given
        val muscleName = "Chest"
        val muscle =
            Muscle(
                name = muscleName,
                description = "Chest muscles",
            )

        whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.just(muscle))

        // When
        val result = muscleController.get(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(muscle))
            .verifyComplete()

        verify(muscleDAL).selectMuscleByName(muscleName)
    }

    @Test
    fun `get should return not found when muscle not found`() {
        // Given
        val muscleName = "NonExistent"

        whenever(
            muscleDAL.selectMuscleByName(muscleName),
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM muscle WHERE name=$1")))

        // When
        val result = muscleController.get(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(muscleDAL).selectMuscleByName(muscleName)
    }

    @Test
    fun `getExercise should return exercise muscles when found`() {
        // Given
        val muscleName = "Chest"
        val muscle =
            Muscle(
                name = muscleName,
                description = "Chest muscles",
            )
        val exerciseMuscles =
            listOf(
                ExerciseMuscle(
                    exerciseName = "Bench Press",
                    muscleName = muscleName,
                ),
                ExerciseMuscle(
                    exerciseName = "Push-Up",
                    muscleName = muscleName,
                ),
            )

        whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.just(muscle))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = muscleController.getExercise(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()

        verify(muscleDAL).selectMuscleByName(muscleName)
        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(muscleName)
    }

    @Test
    fun `getExercise should return not found when no exercises found`() {
        // Given
        val muscleName = "NonExistent"
        val muscle =
            Muscle(
                name = muscleName,
                description = "A non-existent muscle",
            )

        whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.just(muscle))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)).thenReturn(Mono.just(emptyList()))

        // When
        val result = muscleController.getExercise(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(muscleDAL).selectMuscleByName(muscleName)
        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(muscleName)
    }

    @Test
    fun `getAll should return all muscles`() {
        // Given
        val muscles =
            listOf(
                Muscle(
                    name = "Chest",
                    description = "Chest muscles",
                ),
                Muscle(
                    name = "Back",
                    description = "Back muscles",
                ),
            )

        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(muscles))

        // When
        val result = muscleController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Muscle>>)
            .expectNext(muscles)
            .verifyComplete()

        verify(muscleDAL).selectMuscles()
    }
}
