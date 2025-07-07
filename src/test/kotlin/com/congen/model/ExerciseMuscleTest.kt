package com.congen.model

import com.congen.mockExerciseMuscle
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseMuscleTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create exercise muscle with all properties`() {
        val exerciseMuscle = mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")

        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val exerciseMuscle = mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")

        val json = objectMapper.writeValueAsString(exerciseMuscle)

        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"muscle_name\":\"Chest\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "muscle_name": "Chest"
            }
            """.trimIndent()

        val exerciseMuscle = objectMapper.readValue(json, ExerciseMuscle::class.java)

        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "muscle_name": "Chest",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val exerciseMuscle = objectMapper.readValue(json, ExerciseMuscle::class.java)

        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val exerciseMuscle1 = mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
        val exerciseMuscle2 = mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
        val exerciseMuscle3 = mockExerciseMuscle(exerciseName = "Squat", muscleName = "Legs")

        assertEquals(exerciseMuscle1, exerciseMuscle2)
        assertEquals(exerciseMuscle1.hashCode(), exerciseMuscle2.hashCode())
        assertFalse(exerciseMuscle1 == exerciseMuscle3)
        assertFalse(exerciseMuscle1.hashCode() == exerciseMuscle3.hashCode())
    }
}
