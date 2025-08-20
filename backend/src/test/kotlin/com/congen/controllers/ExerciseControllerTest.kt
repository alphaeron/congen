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
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for ExerciseController.
 *
 * Tests the REST API endpoints for exercise management including CRUD operations
 * and relationship queries.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ExerciseControllerTest {
    private lateinit var exerciseController: ExerciseController
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseEquipmentDAL = mock()
        exerciseController = ExerciseController(exerciseDAL, exerciseEquipmentDAL, exerciseMuscleDAL)
    }

    companion object {
        private const val EXERCISE_NAME_1 = "Bench Press"
        private const val EXERCISE_DESCRIPTION_1 = "A compound exercise for chest"
        private val MOVEMENT_TYPE_1 = MovementType.HORIZONTAL_PUSH
        private const val IS_UNILATERAL_1 = false
        private const val IS_UPPER_1 = true
        private const val IS_ACCESSORY_1 = false
        private const val MUSCLE_NAME_1 = "pec major"
        private const val EQUIPMENT_NAME_1 = "power bar"
        private const val SQUAT_NAME = "Back Squat"
    }

    @Test
    fun `save should return saved exercise`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
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
                EXERCISE_NAME_1,
                EXERCISE_DESCRIPTION_1,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )
        ).thenReturn(Mono.error(databaseException))

        val result =
            exerciseController.save(
                EXERCISE_NAME_1,
                EXERCISE_DESCRIPTION_1,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `save should handle empty description`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = "",
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(
            exerciseDAL.insertExercise(
                EXERCISE_NAME_1,
                "",
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                EXERCISE_NAME_1,
                "",
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
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
                name = EXERCISE_NAME_1,
                description = longDescription,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(
            exerciseDAL.insertExercise(
                EXERCISE_NAME_1,
                longDescription,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                EXERCISE_NAME_1,
                longDescription,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
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
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(
            exerciseDAL.insertExercise(
                specialName,
                EXERCISE_DESCRIPTION_1,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseController.save(
                specialName,
                EXERCISE_DESCRIPTION_1,
                MOVEMENT_TYPE_1,
                IS_UNILATERAL_1,
                IS_UPPER_1,
                IS_ACCESSORY_1
            )

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()
    }

    @Test
    fun `get should return exercise when found`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))

        val result = exerciseController.get(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercise))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
    }

    @Test
    fun `get should return not found when exercise not found`() {
        whenever(exerciseDAL.selectExerciseByName(SQUAT_NAME))
            .thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM exercise WHERE name=$1")))

        val result = exerciseController.get(SQUAT_NAME)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(exerciseDAL).selectExerciseByName(SQUAT_NAME)
    }

    @Test
    fun `get should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.get(EXERCISE_NAME_1)

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
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
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
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val exerciseMuscles =
            listOf(
                mockExerciseMuscle(exerciseName = EXERCISE_NAME_1, muscleName = MUSCLE_NAME_1),
                mockExerciseMuscle(exerciseName = EXERCISE_NAME_1, muscleName = MUSCLE_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME_1)).thenReturn(Mono.just(exerciseMuscles))

        val result = exerciseController.getMuscle(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseMuscles))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getMuscle should return not found when no muscles found`() {
        val exercise =
            mockExercise(
                name = SQUAT_NAME,
                description = "A non-existent exercise",
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(exerciseDAL.selectExerciseByName(SQUAT_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(SQUAT_NAME)).thenReturn(Mono.just(emptyList()))

        val result = exerciseController.getMuscle(SQUAT_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(SQUAT_NAME)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(SQUAT_NAME)
    }

    @Test
    fun `getMuscle should handle exercise not found error`() {
        whenever(exerciseDAL.selectExerciseByName(SQUAT_NAME))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        val result = exerciseController.getMuscle(SQUAT_NAME)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(SQUAT_NAME)
    }

    @Test
    fun `getMuscle should handle muscle lookup database errors`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME_1))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.getMuscle(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getMuscle should return single muscle when only one exists`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val singleMuscle =
            listOf(
                mockExerciseMuscle(exerciseName = EXERCISE_NAME_1, muscleName = MUSCLE_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME_1))
            .thenReturn(Mono.just(singleMuscle))

        val result = exerciseController.getMuscle(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleMuscle))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getMuscle should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
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
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME_1, equipmentName = EQUIPMENT_NAME_1),
                mockExerciseEquipment(exerciseName = EXERCISE_NAME_1, equipmentName = EQUIPMENT_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME_1)).thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseController.getEquipment(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getEquipment should return not found when no equipment found`() {
        val exercise =
            mockExercise(
                name = SQUAT_NAME,
                description = "A non-existent exercise",
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )

        whenever(exerciseDAL.selectExerciseByName(SQUAT_NAME)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(SQUAT_NAME)).thenReturn(Mono.just(emptyList()))

        val result = exerciseController.getEquipment(SQUAT_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(exerciseDAL).selectExerciseByName(SQUAT_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(SQUAT_NAME)
    }

    @Test
    fun `getEquipment should handle exercise not found error`() {
        whenever(exerciseDAL.selectExerciseByName(SQUAT_NAME))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        val result = exerciseController.getEquipment(SQUAT_NAME)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(SQUAT_NAME)
    }

    @Test
    fun `getEquipment should handle equipment lookup database errors`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val databaseException = DatabaseException("Database connection failed")
        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME_1))
            .thenReturn(Mono.error(databaseException))

        val result = exerciseController.getEquipment(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getEquipment should return single equipment when only one exists`() {
        val exercise =
            mockExercise(
                name = EXERCISE_NAME_1,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
            )
        val singleEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME_1, equipmentName = EQUIPMENT_NAME_1)
            )

        whenever(exerciseDAL.selectExerciseByName(EXERCISE_NAME_1)).thenReturn(Mono.just(exercise))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME_1))
            .thenReturn(Mono.just(singleEquipment))

        val result = exerciseController.getEquipment(EXERCISE_NAME_1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleEquipment))
            .verifyComplete()
        verify(exerciseDAL).selectExerciseByName(EXERCISE_NAME_1)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(EXERCISE_NAME_1)
    }

    @Test
    fun `getEquipment should handle special characters in exercise name`() {
        val specialName = "Cable Fly (Smith Machine)"
        val exercise =
            mockExercise(
                name = specialName,
                description = EXERCISE_DESCRIPTION_1,
                movementType = MOVEMENT_TYPE_1,
                isUnilateral = IS_UNILATERAL_1,
                isUpper = IS_UPPER_1,
                isAccessory = IS_ACCESSORY_1
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
                    name = EXERCISE_NAME_1,
                    description = EXERCISE_DESCRIPTION_1,
                    movementType = MOVEMENT_TYPE_1,
                    isUnilateral = IS_UNILATERAL_1,
                    isUpper = IS_UPPER_1,
                    isAccessory = IS_ACCESSORY_1
                ),
                mockExercise(
                    name = SQUAT_NAME,
                    description = EXERCISE_DESCRIPTION_1,
                    movementType = MOVEMENT_TYPE_1,
                    isUnilateral = IS_UNILATERAL_1,
                    isUpper = IS_UPPER_1,
                    isAccessory = IS_ACCESSORY_1
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
                    name = EXERCISE_NAME_1,
                    description = EXERCISE_DESCRIPTION_1,
                    movementType = MOVEMENT_TYPE_1,
                    isUnilateral = IS_UNILATERAL_1,
                    isUpper = IS_UPPER_1,
                    isAccessory = IS_ACCESSORY_1
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
