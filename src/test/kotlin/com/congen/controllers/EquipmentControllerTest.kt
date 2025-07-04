package com.congen.controllers

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Equipment
import com.congen.model.ExerciseEquipment
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

    @BeforeEach
    fun setUp() {
        equipmentDAL = mock()
        exerciseEquipmentDAL = mock()
        equipmentController = EquipmentController(equipmentDAL, exerciseEquipmentDAL)
    }

    @Test
    fun `save should return saved equipment`() {
        // Given
        val equipment =
            Equipment(
                name = "Barbell",
                description = "A barbell for weightlifting",
            )

        whenever(equipmentDAL.insertEquipment(equipment)).thenReturn(Mono.just(equipment))

        // When
        val result = equipmentController.save(equipment)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<Equipment>)
            .expectNext(equipment)
            .verifyComplete()

        verify(equipmentDAL).insertEquipment(equipment)
    }

    @Test
    fun `get should return equipment when found`() {
        // Given
        val equipmentName = "Barbell"
        val equipment =
            Equipment(
                name = equipmentName,
                description = "A barbell for weightlifting",
            )

        whenever(equipmentDAL.selectEquipmentByName(equipmentName)).thenReturn(Mono.just(equipment))

        // When
        val result = equipmentController.get(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipment))
            .verifyComplete()

        verify(equipmentDAL).selectEquipmentByName(equipmentName)
    }

    @Test
    fun `get should return not found when equipment not found`() {
        // Given
        val equipmentName = "NonExistent"

        whenever(
            equipmentDAL.selectEquipmentByName(equipmentName),
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM equipment WHERE name=$1")))

        // When
        val result = equipmentController.get(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(equipmentDAL).selectEquipmentByName(equipmentName)
    }

    @Test
    fun `getExercise should return exercise equipment when found`() {
        // Given
        val equipmentName = "Barbell"
        val equipment =
            Equipment(
                name = equipmentName,
                description = "A barbell for weightlifting",
            )
        val exerciseEquipment =
            listOf(
                ExerciseEquipment(
                    exerciseName = "Bench Press",
                    equipmentName = equipmentName,
                ),
                ExerciseEquipment(
                    exerciseName = "Squat",
                    equipmentName = equipmentName,
                ),
            )

        whenever(equipmentDAL.selectEquipmentByName(equipmentName)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(equipmentName)).thenReturn(Mono.just(exerciseEquipment))

        // When
        val result = equipmentController.getExercise(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()

        verify(equipmentDAL).selectEquipmentByName(equipmentName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(equipmentName)
    }

    @Test
    fun `getExercise should return not found when no exercises found`() {
        // Given
        val equipmentName = "NonExistent"
        val equipment =
            Equipment(
                name = equipmentName,
                description = "A non-existent equipment",
            )

        whenever(equipmentDAL.selectEquipmentByName(equipmentName)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(equipmentName)).thenReturn(Mono.just(emptyList()))

        // When
        val result = equipmentController.getExercise(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(equipmentDAL).selectEquipmentByName(equipmentName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(equipmentName)
    }

    @Test
    fun `getAll should return all equipment`() {
        // Given
        val equipmentList =
            listOf(
                Equipment(
                    name = "Barbell",
                    description = "A barbell for weightlifting",
                ),
                Equipment(
                    name = "Dumbbell",
                    description = "A dumbbell for weightlifting",
                ),
            )

        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(equipmentList))

        // When
        val result = equipmentController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Equipment>>)
            .expectNext(equipmentList)
            .verifyComplete()

        verify(equipmentDAL).selectEquipment()
    }
}
