package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockExercise
import com.congen.mockExerciseEquipment
import com.congen.mockExerciseMuscle
import com.congen.model.Exercise
import com.congen.model.MovementType
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

    companion object {
        private const val EXERCISE_NAME = "Bench Press"
        private const val NON_EXISTENT_EXERCISE = "NonExistent"
        private const val EXERCISE_DESCRIPTION = "A compound exercise"
        private val MOVEMENT_TYPE = MovementType.HORIZONTAL_PUSH
        private const val MUSCLE_NAME_1 = "Chest"
        private const val MUSCLE_NAME_2 = "Triceps"
        private const val EQUIPMENT_NAME_1 = "Barbell"
        private const val EQUIPMENT_NAME_2 = "Bench"
        private const val SQUAT_NAME = "Squat"
    }

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseEquipmentDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseController = ExerciseController(exerciseDAL, exerciseEquipmentDAL, exerciseMuscleDAL)
    }

    @Test
    fun `save should return saved exercise`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
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

        val result =
            exerciseController.save(
                exercise.name,
                exercise.description,
                exercise.movementType,
                exercise.isUnilateral,
                exercise.isUpper,
                exercise.isAccessory
            )

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<Exercise>)
            .expectNext(exercise)
            .verifyComplete()

        verify(exerciseDAL).insertExercise(
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
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))

        val result = exerciseController.get(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
    }

    @Test
    fun `get should return not found when exercise not found`() {
        whenever(exerciseDAL.selectExerciseByName(NON_EXISTENT_EXERCISE))
            .thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM exercise WHERE name=$1")))

        val result = exerciseController.get(NON_EXISTENT_EXERCISE)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `getMuscle should return exercise muscles when found`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exerciseMuscles =
            listOf(
                mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME_1),
                mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME_2)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(exerciseMuscles))

        val result = exerciseController.getMuscle(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getMuscle should return not found when no muscles found`() {
        val exercise =
            mockExercise(
                name = NON_EXISTENT_EXERCISE,
                description = "A non-existent exercise",
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(exerciseDAL.selectExerciseByName(NON_EXISTENT_EXERCISE)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(NON_EXISTENT_EXERCISE)).thenReturn(Mono.just(emptyList()))

        val result = exerciseController.getMuscle(NON_EXISTENT_EXERCISE)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `getEquipment should return exercise equipment when found`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME_1),
                mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME_2)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME)).thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseController.getEquipment(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getEquipment should return not found when no equipment found`() {
        val exercise =
            mockExercise(
                name = NON_EXISTENT_EXERCISE,
                description = "A non-existent exercise",
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(exerciseDAL.selectExerciseByName(NON_EXISTENT_EXERCISE)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(NON_EXISTENT_EXERCISE)).thenReturn(Mono.just(emptyList()))

        val result = exerciseController.getEquipment(NON_EXISTENT_EXERCISE)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `getAll should return all exercises`() {
        val exercises =
            listOf(
                mockExercise(
                    name = EXERCISE_NAME,
                    description = EXERCISE_DESCRIPTION,
                    movementType = MOVEMENT_TYPE,
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true
                ),
                mockExercise(
                    name = SQUAT_NAME,
                    description = EXERCISE_DESCRIPTION,
                    movementType = MOVEMENT_TYPE,
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = true
                )
            )

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        val result = exerciseController.getAll()

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Exercise>>)
            .expectNext(exercises)
            .verifyComplete()

        verify(exerciseDAL).selectExercises()
    }
}
