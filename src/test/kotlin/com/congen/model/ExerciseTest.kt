package com.congen.model

import com.congen.config.JacksonConfig
import com.congen.mockExercise
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseTest {
    private val objectMapper =
        ObjectMapper().apply {
            JacksonConfig.configureObjectMapper(this)
        }

    @Test
    fun `should create exercise with all properties`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals(MovementType.HORIZONTAL_PUSH, exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertTrue(exercise.isAccessory)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        val json = objectMapper.writeValueAsString(exercise)

        assertTrue(json.contains("\"name\":\"Bench Press\""))
        assertTrue(json.contains("\"description\":\"A compound exercise\""))
        assertTrue(json.contains("\"movement_type\":\"horizontal_push\""))
        assertTrue(json.contains("\"is_unilateral\":false"))
        assertTrue(json.contains("\"is_upper\":true"))
        assertTrue(json.contains("\"is_accessory\":true"))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "name": "Bench Press",
                "description": "A compound exercise",
                "movement_type": "horizontal_push",
                "is_unilateral": false,
                "is_upper": true,
                "is_accessory": true
            }
            """.trimIndent()

        val exercise = objectMapper.readValue(json, Exercise::class.java)

        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals(MovementType.HORIZONTAL_PUSH, exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertTrue(exercise.isAccessory)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "name": "Bench Press",
                "description": "A compound exercise",
                "movement_type": "horizontal_push",
                "is_unilateral": false,
                "is_upper": true,
                "is_accessory": true,
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val exercise = objectMapper.readValue(json, Exercise::class.java)

        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals(MovementType.HORIZONTAL_PUSH, exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertTrue(exercise.isAccessory)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val exercise1 =
            mockExercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exercise2 =
            mockExercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        val exercise3 =
            mockExercise(
                name = "Squat",
                description = "A compound exercise",
                movementType = MovementType.SQUAT,
                isUnilateral = false,
                isUpper = false,
                isAccessory = true
            )

        assertEquals(exercise1, exercise2)
        assertEquals(exercise1.hashCode(), exercise2.hashCode())
        assertFalse(exercise1 == exercise3)
        assertFalse(exercise1.hashCode() == exercise3.hashCode())
    }
}
