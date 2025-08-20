package com.congen.controllers

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockEquipment
import com.congen.mockExerciseEquipment
import com.congen.model.Equipment
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
class EquipmentControllerTest {
    private lateinit var equipmentDAL: EquipmentDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var equipmentController: EquipmentController

    companion object {
        private const val EQUIPMENT_NAME = "power bar"
        private const val EQUIPMENT_DESCRIPTION = "A barbell for weightlifting"
        private const val EXERCISE_NAME_2 = "Back Squat"
        private const val DUMBBELL_NAME = "dumbbells"
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

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipment))
            .verifyComplete()
        verify(equipmentDAL).insertEquipment(EQUIPMENT_NAME, equipment.description)
    }

    @Test
    fun `save should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(equipmentDAL.insertEquipment(EQUIPMENT_NAME, DUMBBELL_DESCRIPTION))
            .thenReturn(Mono.error(databaseException))

        val result = equipmentController.save(EQUIPMENT_NAME, DUMBBELL_DESCRIPTION)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
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
            equipmentDAL.selectEquipmentByName("NonExistent")
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM equipment WHERE name=$1")))

        val result = equipmentController.get("NonExistent")

        StepVerifier.create(result).expectError(NoResultsFoundException::class.java).verify()
        verify(equipmentDAL).selectEquipmentByName("NonExistent")
    }

    @Test
    fun `getExercise should return exercise equipment when found`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = EQUIPMENT_NAME),
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
        val equipment = mockEquipment(name = "NonExistent", description = "A non-existent equipment")
        whenever(equipmentDAL.selectEquipmentByName("NonExistent")).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment("NonExistent")).thenReturn(Mono.just(emptyList()))

        val result = equipmentController.getExercise("NonExistent")

        StepVerifier.create(result).expectNext(ResponseEntity.notFound().build()).verifyComplete()
        verify(equipmentDAL).selectEquipmentByName("NonExistent")
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment("NonExistent")
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

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipmentList))
            .verifyComplete()
        verify(equipmentDAL).selectEquipment()
    }

    @Test
    fun `getAll should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.error(databaseException))

        val result = equipmentController.getAll()

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `get should handle database errors`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(equipmentDAL.selectEquipmentByName(EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = equipmentController.get(EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getExercise should handle equipment not found error`() {
        whenever(equipmentDAL.selectEquipmentByName("NonExistent"))
            .thenReturn(Mono.error(NoResultsFoundException("Equipment not found")))

        val result = equipmentController.getExercise("NonExistent")

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(equipmentDAL).selectEquipmentByName("NonExistent")
    }

    @Test
    fun `getExercise should handle exercise lookup database errors`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        val databaseException = DatabaseException("Database connection failed")
        whenever(equipmentDAL.selectEquipmentByName(EQUIPMENT_NAME)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = equipmentController.getExercise(EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(equipmentDAL).selectEquipmentByName(EQUIPMENT_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(EQUIPMENT_NAME)
    }

    @Test
    fun `getAll should return empty list when no equipment exists`() {
        val emptyEquipmentList = emptyList<Equipment>()
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(emptyEquipmentList))

        val result = equipmentController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyEquipmentList))
            .verifyComplete()
        verify(equipmentDAL).selectEquipment()
    }

    @Test
    fun `getExercise should return single exercise when only one exists`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME)
        val singleExercise =
            listOf(
                mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = EQUIPMENT_NAME)
            )
        whenever(equipmentDAL.selectEquipmentByName(EQUIPMENT_NAME)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(EQUIPMENT_NAME))
            .thenReturn(Mono.just(singleExercise))

        val result = equipmentController.getExercise(EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(singleExercise))
            .verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(EQUIPMENT_NAME)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(EQUIPMENT_NAME)
    }

    @Test
    fun `save should handle empty description`() {
        val equipment = mockEquipment(name = EQUIPMENT_NAME, description = "")
        whenever(equipmentDAL.insertEquipment(EQUIPMENT_NAME, "")).thenReturn(Mono.just(equipment))

        val result = equipmentController.save(EQUIPMENT_NAME, "")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipment))
            .verifyComplete()
        verify(equipmentDAL).insertEquipment(EQUIPMENT_NAME, "")
    }

    @Test
    fun `save should handle long description`() {
        val longDescription =
            "A very long description of the equipment that contains many details about its usage, " +
                "specifications, and intended purpose for various exercises and workout routines"
        val equipment = mockEquipment(name = EQUIPMENT_NAME, description = longDescription)
        whenever(equipmentDAL.insertEquipment(EQUIPMENT_NAME, longDescription)).thenReturn(Mono.just(equipment))

        val result = equipmentController.save(EQUIPMENT_NAME, longDescription)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipment))
            .verifyComplete()
        verify(equipmentDAL).insertEquipment(EQUIPMENT_NAME, longDescription)
    }

    @Test
    fun `get should handle special characters in equipment name`() {
        val specialName = "Cable Machine (Smith)"
        val equipment = mockEquipment(name = specialName)
        whenever(equipmentDAL.selectEquipmentByName(specialName)).thenReturn(Mono.just(equipment))

        val result = equipmentController.get(specialName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(equipment))
            .verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(specialName)
    }

    @Test
    fun `getExercise should handle special characters in equipment name`() {
        val specialName = "Cable Machine (Smith)"
        val equipment = mockEquipment(name = specialName)
        val exerciseEquipment =
            listOf(
                mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = specialName)
            )
        whenever(equipmentDAL.selectEquipmentByName(specialName)).thenReturn(Mono.just(equipment))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(specialName))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = equipmentController.getExercise(specialName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseEquipment))
            .verifyComplete()
        verify(equipmentDAL).selectEquipmentByName(specialName)
        verify(exerciseEquipmentDAL).selectExerciseEquipmentByEquipment(specialName)
    }
}
