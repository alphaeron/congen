package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.mockExerciseEquipment
import com.congen.model.ExerciseEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
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

        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<ExerciseEquipment>)
            .expectNext(exerciseEquipment)
            .verifyComplete()
        verify(exerciseEquipmentDAL).insertExerciseEquipment(EXERCISE_NAME, EQUIPMENT_NAME)
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

        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<List<ExerciseEquipment>>)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()
        verify(exerciseEquipmentDAL).selectAllExerciseEquipment()
    }
}
