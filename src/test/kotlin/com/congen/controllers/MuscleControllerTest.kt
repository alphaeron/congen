package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.model.ExerciseMuscle
import com.congen.model.Muscle
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val muscle = Muscle(
            name = "Chest",
            description = "Chest muscles"
        )

        whenever(muscleDAL.insertMuscle(muscle)).thenReturn(Mono.just(muscle))

        // When
        val result = muscleController.save(muscle)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<Muscle>)
            .expectNext(muscle)
            .verifyComplete()

        verify(muscleDAL).insertMuscle(muscle)
    }

    @Test
    fun `get should return muscle when found`() {
        // Given
        val muscleName = "Chest"
        val muscle = Muscle(
            name = muscleName,
            description = "Chest muscles"
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

        whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.empty())

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
        val exerciseMuscles = listOf(
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = muscleName
            ),
            ExerciseMuscle(
                exerciseName = "Push-up",
                muscleName = muscleName
            )
        )

        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = muscleController.getExercise(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()

        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(muscleName)
    }

    @Test
    fun `getExercise should return not found when no exercises found`() {
        // Given
        val muscleName = "NonExistent"

        whenever(exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)).thenReturn(Mono.empty())

        // When
        val result = muscleController.getExercise(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseMuscleDAL).selectExerciseMuscleByMuscle(muscleName)
    }

    @Test
    fun `getAll should return all muscles`() {
        // Given
        val muscles = listOf(
            Muscle(
                name = "Chest",
                description = "Chest muscles"
            ),
            Muscle(
                name = "Back",
                description = "Back muscles"
            )
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