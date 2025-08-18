package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.DatabaseException
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
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration"
    ]
)
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
        private const val SQUAT_NAME = "Back Squat"
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

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
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
    fun `save should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(
            exerciseDAL.insertExercise(
                EXERCISE_NAME,
                EXERCISE_DESCRIPTION,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )
        ).thenReturn(Mono.error(databaseException))

        val result =
            exerciseController.save(
                EXERCISE_NAME,
                EXERCISE_DESCRIPTION,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `save should handle empty description`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = "",
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(
            exerciseDAL.insertExercise(
                EXERCISE_NAME,
                "",
                MOVEMENT_TYPE,
                false,
                true,
                true
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                EXERCISE_NAME,
                "",
                MOVEMENT_TYPE,
                false,
                true,
                true
            )

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()
    }

    @Test
    fun `save should handle long description`() {
        val longDescription =
            "A very long description of the exercise that contains many details about proper form, " +
                "muscle engagement, breathing patterns, and safety considerations for performing this compound movement effectively"
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = longDescription,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(
            exerciseDAL.insertExercise(
                EXERCISE_NAME,
                longDescription,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                EXERCISE_NAME,
                longDescription,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()
    }

    @Test
    fun `save should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(
            exerciseDAL.insertExercise(
                specialName,
                EXERCISE_DESCRIPTION,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                specialName,
                EXERCISE_DESCRIPTION,
                MOVEMENT_TYPE,
                false,
                true,
                true
            )

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()
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
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `get should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.get(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `get should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        whenever(exerciseDAL.selectExerciseByName(specialName)).thenReturn(Mono.just(exercise))

        val result = exerciseController.get(specialName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(specialName)
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
    fun `getMuscle should handle exercise not found error`() {
        whenever(exerciseDAL.selectExerciseByName(NON_EXISTENT_EXERCISE))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        val result = exerciseController.getMuscle(NON_EXISTENT_EXERCISE)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `getMuscle should handle muscle lookup database errors`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.getMuscle(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getMuscle should return single muscle when only one exists`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val singleMuscle =
            listOf(
                mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = MUSCLE_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME))
            .thenReturn(Mono.just(singleMuscle))

        val result = exerciseController.getMuscle(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleMuscle))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getMuscle should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exerciseMuscles =
            listOf(
                mockExerciseMuscle(exerciseName = specialName, muscleName = MUSCLE_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(specialName)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(specialName))
            .thenReturn(Mono.just(exerciseMuscles))

        val result = exerciseController.getMuscle(specialName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(specialName)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(specialName)
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
    fun `getEquipment should handle exercise not found error`() {
        whenever(exerciseDAL.selectExerciseByName(NON_EXISTENT_EXERCISE))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        val result = exerciseController.getEquipment(NON_EXISTENT_EXERCISE)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(NON_EXISTENT_EXERCISE)
    }

    @Test
    fun `getEquipment should handle equipment lookup database errors`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.getEquipment(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getEquipment should return single equipment when only one exists`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val singleEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME))
            .thenReturn(Mono.just(singleEquipment))

        val result = exerciseController.getEquipment(EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleEquipment))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME)
    }

    @Test
    fun `getEquipment should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION,
                movementType = MOVEMENT_TYPE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = specialName, equipmentName = EQUIPMENT_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(specialName)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(specialName))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseController.getEquipment(specialName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(specialName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(specialName)
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

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercises))
            .verifyComplete()

        verify(exerciseDAL).selectExercises()
    }

    @Test
    fun `getAll should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.error(databaseException))

        val result = exerciseController.getAll()

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getAll should return empty list when no exercises exist`() {
        val emptyExerciseList = emptyList<Exercise>()
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(emptyExerciseList))

        val result = exerciseController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyExerciseList))
            .verifyComplete()
        verify(exerciseDAL).selectExercises()
    }

    @Test
    fun `getAll should return single exercise when only one exists`() {
        val singleExercise =
            listOf(
                mockExercise(
                    name = EXERCISE_NAME,
                    description = EXERCISE_DESCRIPTION,
                    movementType = MOVEMENT_TYPE,
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true
                )
            )

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(singleExercise))

        val result = exerciseController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleExercise))
            .verifyComplete()
        verify(exerciseDAL).selectExercises()
    }
}
