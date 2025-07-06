package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
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

class ExerciseControllerTest {
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseController: ExerciseController

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseEquipmentDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseController = ExerciseController(exerciseDAL, exerciseEquipmentDAL, exerciseMuscleDAL)
    }

    @Test
    fun `save should return saved exercise`() {
        // Given
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(
            exerciseDAL.insertExercise(
                exercise.name,
                exercise.description,
                exercise.movementType,
                exercise.isUnilateral,
                exercise.isUpper,
                exercise.isAccessory
            )
        ).thenReturn(Mono.just(exercise))

        // When
        val result =
            exerciseController.save(
                exercise.name,
                exercise.description,
                exercise.movementType,
                exercise.isUnilateral,
                exercise.isUpper,
                exercise.isAccessory
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<Exercise>)
            .expectNext(exercise)
            .verifyComplete()

        verify(
            exerciseDAL
        ).insertExercise(
            exercise.name,
            exercise.description,
            exercise.movementType,
            exercise.isUnilateral,
            exercise.isUpper,
            exercise.isAccessory
        )
    }

    @Test
    fun `get should return exercise when found`() {
        // Given
        val exerciseName = "Bench Press"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(exercise))

        // When
        val result = exerciseController.get(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
    }

    @Test
    fun `get should return not found when exercise not found`() {
        // Given
        val exerciseName = "NonExistent"

        whenever(
            exerciseDAL.selectExerciseByName(exerciseName),
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM exercise WHERE name=$1")))

        // When
        val result = exerciseController.get(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
    }

    @Test
    fun `getMuscle should return exercise muscles when found`() {
        // Given
        val exerciseName = "Bench Press"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )
        val exerciseMuscles =
            listOf(
                ExerciseMuscle(
                    exerciseName = exerciseName,
                    muscleName = "Chest",
                ),
                ExerciseMuscle(
                    exerciseName = exerciseName,
                    muscleName = "Triceps",
                ),
            )

        whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = exerciseController.getMuscle(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(exerciseName)
    }

    @Test
    fun `getMuscle should return not found when no muscles found`() {
        // Given
        val exerciseName = "NonExistent"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A non-existent exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)).thenReturn(Mono.just(emptyList()))

        // When
        val result = exerciseController.getMuscle(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(exerciseName)
    }

    @Test
    fun `getEquipment should return exercise equipment when found`() {
        // Given
        val exerciseName = "Bench Press"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )
        val exerciseEquipment =
            listOf(
                ExerciseEquipment(
                    exerciseName = exerciseName,
                    equipmentName = "Barbell",
                ),
                ExerciseEquipment(
                    exerciseName = exerciseName,
                    equipmentName = "Bench",
                ),
            )

        whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)).thenReturn(Mono.just(exerciseEquipment))

        // When
        val result = exerciseController.getEquipment(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(exerciseName)
    }

    @Test
    fun `getEquipment should return not found when no equipment found`() {
        // Given
        val exerciseName = "NonExistent"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A non-existent exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)).thenReturn(Mono.just(emptyList()))

        // When
        val result = exerciseController.getEquipment(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(exerciseName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(exerciseName)
    }

    @Test
    fun `getAll should return all exercises`() {
        // Given
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound exercise",
                    movementType = "push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true,
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound exercise",
                    movementType = "push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = true,
                ),
            )

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        // When
        val result = exerciseController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Exercise>>)
            .expectNext(exercises)
            .verifyComplete()

        verify(exerciseDAL).selectExercises()
    }
}
