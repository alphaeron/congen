package com.congen.model

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
        // Given & When
        val exerciseMuscle =
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest",
            )

        // Then
        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val exerciseMuscle =
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest",
            )

        // When
        val json = objectMapper.writeValueAsString(exerciseMuscle)

        // Then
        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"muscle_name\":\"Chest\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "muscle_name": "Chest"
            }
            """.trimIndent()

        // When
        val exerciseMuscle = objectMapper.readValue(json, ExerciseMuscle::class.java)

        // Then
        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "muscle_name": "Chest",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val exerciseMuscle = objectMapper.readValue(json, ExerciseMuscle::class.java)

        // Then
        assertEquals("Bench Press", exerciseMuscle.exerciseName)
        assertEquals("Chest", exerciseMuscle.muscleName)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val exerciseMuscle1 =
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest",
            )
        val exerciseMuscle2 =
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest",
            )
        val exerciseMuscle3 =
            ExerciseMuscle(
                exerciseName = "Squat",
                muscleName = "Legs",
            )

        // Then
        assertEquals(exerciseMuscle1, exerciseMuscle2)
        assertEquals(exerciseMuscle1.hashCode(), exerciseMuscle2.hashCode())
        assertFalse(exerciseMuscle1 == exerciseMuscle3)
        assertFalse(exerciseMuscle1.hashCode() == exerciseMuscle3.hashCode())
    }
}
