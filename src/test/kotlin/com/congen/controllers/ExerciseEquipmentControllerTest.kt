package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
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

    @BeforeEach
    fun setUp() {
        exerciseEquipmentDAL = mock()
        exerciseEquipmentController = ExerciseEquipmentController(exerciseEquipmentDAL)
    }

    @Test
    fun `save should return saved exercise equipment`() {
        // Given
        val exerciseEquipment =
            ExerciseEquipment(
                exerciseName = "Bench Press",
                equipmentName = "Barbell",
            )
        whenever(
            exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)
        ).thenReturn(Mono.just(exerciseEquipment))

        // When
        val result = exerciseEquipmentController.save(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<ExerciseEquipment>)
            .expectNext(exerciseEquipment)
            .verifyComplete()
        verify(exerciseEquipmentDAL).insertExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)
    }

    @Test
    fun `getAll should return all exercise equipment`() {
        // Given
        val exerciseEquipmentList =
            listOf(
                ExerciseEquipment(
                    exerciseName = "Bench Press",
                    equipmentName = "Barbell",
                ),
                ExerciseEquipment(
                    exerciseName = "Squat",
                    equipmentName = "Barbell",
                ),
            )
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(exerciseEquipmentList))

        // When
        val result = exerciseEquipmentController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ExerciseEquipment>>)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()
        verify(exerciseEquipmentDAL).selectAllExerciseEquipment()
    }
}
