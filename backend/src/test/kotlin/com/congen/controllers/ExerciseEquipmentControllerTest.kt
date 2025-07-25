package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockExerciseEquipment
import com.congen.model.ExerciseEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseEquipmentControllerTest {
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var exerciseEquipmentController: ExerciseEquipmentController

    companion object {
        private const val EXERCISE_NAME = "Bench Press"
        private const val EQUIPMENT_NAME = "Barbell"
        private const val SQUAT_NAME = "Back Squat"
    }

    @BeforeEach
    fun setUp() {
        exerciseEquipmentDAL = mock()
        exerciseEquipmentController = ExerciseEquipmentController(exerciseEquipmentDAL)
    }

    @Test
    fun `save should return saved exercise equipment`() {
        val exerciseEquipment = mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME)
        whenever(exerciseEquipmentDAL.insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseEquipmentController.save(EXERCISE_NAME, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()
        verify(exerciseEquipmentDAL).insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME)
    }

    @Test
    fun `save should return 409 on duplicate key error`() {
        val ex = DatabaseQueryException("duplicate key value violates unique constraint")
        whenever(exerciseEquipmentDAL.insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME))
            .thenReturn(Mono.error(ex))

        val result = exerciseEquipmentController.save(EXERCISE_NAME, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `save should return 422 on foreign key violation`() {
        val ex = DatabaseQueryException("violates foreign key constraint")
        whenever(exerciseEquipmentDAL.insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME))
            .thenReturn(Mono.error(ex))

        val result = exerciseEquipmentController.save(EXERCISE_NAME, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `save should propagate other database errors`() {
        val ex = DatabaseQueryException("some other db error")
        whenever(exerciseEquipmentDAL.insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME))
            .thenReturn(Mono.error(ex))

        val result = exerciseEquipmentController.save(EXERCISE_NAME, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `save should handle special characters in names`() {
        val specialExercise = "Cable Fly (Smith Machine)"
        val specialEquipment = "EZ-Bar"
        val exerciseEquipment = mockExerciseEquipment(exerciseName = specialExercise, equipmentName = specialEquipment)
        whenever(exerciseEquipmentDAL.insertExerciseEquipment(specialExercise, specialEquipment))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseEquipmentController.save(specialExercise, specialEquipment)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()
        verify(exerciseEquipmentDAL).insertExerciseEquipment(specialExercise, specialEquipment)
    }

    @Test
    fun `getAll should return all exercise equipment`() {
        val exerciseEquipmentList =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME),
                mockExerciseEquipment(exerciseName = SQUAT_NAME, equipmentName = EQUIPMENT_NAME)
            )
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment())
            .thenReturn(Mono.just(exerciseEquipmentList))

        val result = exerciseEquipmentController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipmentList))
            .verifyComplete()
        verify(exerciseEquipmentDAL).selectAllExerciseEquipment()
    }

    @Test
    fun `getAll should return empty list`() {
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment())
            .thenReturn(Mono.just(emptyList()))

        val result = exerciseEquipmentController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList<ExerciseEquipment>()))
            .verifyComplete()
        verify(exerciseEquipmentDAL).selectAllExerciseEquipment()
    }

    @Test
    fun `getAll should return single result`() {
        val single = listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = EQUIPMENT_NAME))
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment())
            .thenReturn(Mono.just(single))

        val result = exerciseEquipmentController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(single))
            .verifyComplete()
        verify(exerciseEquipmentDAL).selectAllExerciseEquipment()
    }

    @Test
    fun `getAll should propagate database errors`() {
        val ex = DatabaseQueryException("db error")
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment())
            .thenReturn(Mono.error(ex))

        val result = exerciseEquipmentController.getAll()

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
