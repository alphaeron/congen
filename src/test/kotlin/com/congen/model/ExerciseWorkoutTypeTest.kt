package com.congen.model

import com.congen.config.JacksonConfig
import com.congen.mockExerciseWorkoutType
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExerciseWorkoutTypeTest {
    private val objectMapper =
        ObjectMapper().apply {
            JacksonConfig.configureObjectMapper(this)
        }

    @Test
    fun `should create exercise workout type with all properties`() {
        val exerciseWorkoutType =
            mockExerciseWorkoutType(
                exerciseName = "Bench Press",
                movementType = MovementType.HORIZONTAL_PUSH,
                workoutType = "dynamic_effort"
            )

        assertEquals("Bench Press", exerciseWorkoutType.exerciseName)
        assertEquals(MovementType.HORIZONTAL_PUSH, exerciseWorkoutType.movementType)
        assertEquals("dynamic_effort", exerciseWorkoutType.workoutType)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val exerciseWorkoutType =
            mockExerciseWorkoutType(
                exerciseName = "Bench Press",
                movementType = MovementType.HORIZONTAL_PUSH,
                workoutType = "dynamic_effort"
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
        assertEquals(MovementType.HORIZONTAL_PUSH, exerciseWorkoutType.movementType)
        assertEquals("dynamic_effort", exerciseWorkoutType.workoutType)
    }
}
