package com.congen.controllers

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockEquipment
import com.congen.mockExerciseEquipment
import com.congen.model.Equipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EquipmentControllerTest {
    private lateinit var equipmentDAL: EquipmentDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var equipmentController: EquipmentController

    companion object {
        private const val EQUIPMENT_NAME = "Barbell"
        private const val NON_EXISTENT_EQUIPMENT = "NonExistent"
        private const val EXERCISE_NAME_1 = "Bench Press"
        private const val EXERCISE_NAME_2 = "Back Squat"
        private const val DUMBBELL_NAME = "Dumbbell"
        private const val DUMBBELL_DESCRIPTION = "A dumbbell for weightlifting"
    }

    @BeforeEach
    fun setUp() {
        equipmentDAL = mock()
        exerciseEquipmentDAL = mock()
        equipmentController = EquipmentController(equipmentDAL, exerciseEquipmentDAL)
    }

    @Test
    fun `save should return saved equipment`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        whenever(equipmentDAL.insertEquipment(EQUIPMENT_NAME, equipment.description)).thenReturn(Mono.just(equipment))

        val result = equipmentController.save(EQUIPMENT_NAME, equipment.description)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<Equipment>
        StepVerifier.create(body).expectNext(equipment).verifyComplete()
        verify(equipmentDAL).insertEquipment(EQUIPMENT_NAME, equipment.description)
    }

    @Test
    fun `get should return equipment when found`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        whenever(equipmentDAL.selectEquipmentByName(EQUIPMENT_NAME)).thenReturn(Mono.just(equipment))

        val result = equipmentController.get(EQUIPMENT_NAME)

        StepVerifier.create(result).expectNext(ResponseEntity.ok(equipment)).verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(EQUIPMENT_NAME)
    }

    @Test
    fun `get should return not found when equipment not found`() {
        whenever(
            equipmentDAL.selectEquipmentByName(NON_EXISTENT_EQUIPMENT)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM equipment WHERE name=$1")))

        val result = equipmentController.get(NON_EXISTENT_EQUIPMENT)

        StepVerifier.create(result).expectNext(ResponseEntity.notFound().build()).verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(NON_EXISTENT_EQUIPMENT)
    }

    @Test
    fun `getExercise should return exercise equipment when found`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = EXERCISE_NAME_1, equipmentName = EQUIPMENT_NAME),
                mockExerciseEquipment(exerciseName = EXERCISE_NAME_2, equipmentName = EQUIPMENT_NAME)
            )
        whenever(equipmentDAL.selectEquipmentByName(EQUIPMENT_NAME)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(EQUIPMENT_NAME)).thenReturn(Mono.just(exerciseEquipment))

        val result = equipmentController.getExercise(EQUIPMENT_NAME)

        StepVerifier.create(result).expectNext(ResponseEntity.ok(exerciseEquipment)).verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(EQUIPMENT_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(EQUIPMENT_NAME)
    }

    @Test
    fun `getExercise should return not found when no exercises found`() {
        val equipment = mockEquipment(name = NON_EXISTENT_EQUIPMENT, description = "A non-existent equipment")
        whenever(equipmentDAL.selectEquipmentByName(NON_EXISTENT_EQUIPMENT)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(NON_EXISTENT_EQUIPMENT)).thenReturn(Mono.just(emptyList()))

        val result = equipmentController.getExercise(NON_EXISTENT_EQUIPMENT)

        StepVerifier.create(result).expectNext(ResponseEntity.notFound().build()).verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(NON_EXISTENT_EQUIPMENT)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(NON_EXISTENT_EQUIPMENT)
    }

    @Test
    fun `getAll should return all equipment`() {
        val equipmentList =
            listOf(
                mockEquipment(name = EQUIPMENT_NAME),
                mockEquipment(name = DUMBBELL_NAME, description = DUMBBELL_DESCRIPTION)
            )
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(equipmentList))

        val result = equipmentController.getAll()

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<List<Equipment>>
        StepVerifier.create(body).expectNext(equipmentList).verifyComplete()
        verify(equipmentDAL).selectEquipment()
    }
}
