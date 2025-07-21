package com.congen.model

import com.congen.mockExerciseEquipment
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
        val exerciseEquipment = mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")

        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val exerciseEquipment = mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")

        val json = objectMapper.writeValueAsString(exerciseEquipment)

        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"equipment_name\":\"Barbell\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "equipment_name": "Barbell"
            }
            """.trimIndent()

        val exerciseEquipment = objectMapper.readValue(json, ExerciseEquipment::class.java)

        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "equipment_name": "Barbell",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val exerciseEquipment = objectMapper.readValue(json, ExerciseEquipment::class.java)

        assertEquals("Bench Press", exerciseEquipment.exerciseName)
        assertEquals("Barbell", exerciseEquipment.equipmentName)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val exerciseEquipment1 = mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")
        val exerciseEquipment2 = mockExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")
        val exerciseEquipment3 = mockExerciseEquipment(exerciseName = "Squat", equipmentName = "Barbell")

        assertEquals(exerciseEquipment1, exerciseEquipment2)
        assertEquals(exerciseEquipment1.hashCode(), exerciseEquipment2.hashCode())
        assertFalse(exerciseEquipment1 == exerciseEquipment3)
        assertFalse(exerciseEquipment1.hashCode() == exerciseEquipment3.hashCode())
    }
}
