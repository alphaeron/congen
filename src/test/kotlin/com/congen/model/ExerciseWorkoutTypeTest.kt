package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExerciseWorkoutTypeTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create exercise workout type with all properties`() {
        val exerciseWorkoutType =
            ExerciseWorkoutType(
                exerciseName = "Bench Press",
                movementType = "horizontal push",
                workoutType = "dynamic_effort",
            )
        assertEquals("Bench Press", exerciseWorkoutType.exerciseName)
        assertEquals("horizontal push", exerciseWorkoutType.movementType)
        assertEquals("dynamic_effort", exerciseWorkoutType.workoutType)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val exerciseWorkoutType =
            ExerciseWorkoutType(
                exerciseName = "Bench Press",
                movementType = "horizontal push",
                workoutType = "dynamic_effort",
            )
        val json = objectMapper.writeValueAsString(exerciseWorkoutType)
        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"movement_type\":\"horizontal push\""))
        assertTrue(json.contains("\"workout_type\":\"dynamic_effort\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "exercise_name": "Bench Press",
                "movement_type": "horizontal push",
                "workout_type": "dynamic_effort"
            }
            """.trimIndent()
        val exerciseWorkoutType = objectMapper.readValue(json, ExerciseWorkoutType::class.java)
        assertEquals("Bench Press", exerciseWorkoutType.exerciseName)
        assertEquals("horizontal push", exerciseWorkoutType.movementType)
        assertEquals("dynamic_effort", exerciseWorkoutType.workoutType)
    }
}
