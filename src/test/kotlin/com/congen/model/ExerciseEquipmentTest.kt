package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseEquipmentTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create exercise equipment with all properties`() {
        // Given & When
        val exerciseEquipment = ExerciseEquipment(
            exerciseName = "Bench Press",
            equipmentName = "Barbell"
        )

        // Then
        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val exerciseEquipment = ExerciseEquipment(
            exerciseName = "Bench Press",
            equipmentName = "Barbell"
        )

        // When
        val json = objectMapper.writeValueAsString(exerciseEquipment)

        // Then
        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"equipment_name\":\"Barbell\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json = """
            {
                "exercise_name": "Bench Press",
                "equipment_name": "Barbell"
            }
        """.trimIndent()

        // When
        val exerciseEquipment = objectMapper.readValue(json, ExerciseEquipment::class.java)

        // Then
        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json = """
            {
                "exercise_name": "Bench Press",
                "equipment_name": "Barbell",
                "unknown_property": "should be ignored"
            }
        """.trimIndent()

        // When
        val exerciseEquipment = objectMapper.readValue(json, ExerciseEquipment::class.java)

        // Then
        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val exerciseEquipment1 = ExerciseEquipment(
            exerciseName = "Bench Press",
            equipmentName = "Barbell"
        )
        val exerciseEquipment2 = ExerciseEquipment(
            exerciseName = "Bench Press",
            equipmentName = "Barbell"
        )
        val exerciseEquipment3 = ExerciseEquipment(
            exerciseName = "Squat",
            equipmentName = "Barbell"
        )

        // Then
        assertEquals(exerciseEquipment1, exerciseEquipment2)
        assertEquals(exerciseEquipment1.hashCode(), exerciseEquipment2.hashCode())
        assertFalse(exerciseEquipment1 == exerciseEquipment3)
        assertFalse(exerciseEquipment1.hashCode() == exerciseEquipment3.hashCode())
    }
} 