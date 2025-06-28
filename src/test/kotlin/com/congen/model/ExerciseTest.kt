package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create exercise with all properties`() {
        // Given & When
        val exercise = Exercise(
            name = "Bench Press",
            description = "A compound exercise",
            movementType = "push",
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )

        // Then
        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals("push", exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertFalse(exercise.isAccessory)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val exercise = Exercise(
            name = "Bench Press",
            description = "A compound exercise",
            movementType = "push",
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )

        // When
        val json = objectMapper.writeValueAsString(exercise)

        // Then
        assertTrue(json.contains("\"name\":\"Bench Press\""))
        assertTrue(json.contains("\"description\":\"A compound exercise\""))
        assertTrue(json.contains("\"movement_type\":\"push\""))
        assertTrue(json.contains("\"is_unilateral\":false"))
        assertTrue(json.contains("\"is_upper\":true"))
        assertTrue(json.contains("\"is_accessory\":false"))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json = """
            {
                "name": "Bench Press",
                "description": "A compound exercise",
                "movement_type": "push",
                "is_unilateral": false,
                "is_upper": true,
                "is_accessory": false
            }
        """.trimIndent()

        // When
        val exercise = objectMapper.readValue(json, Exercise::class.java)

        // Then
        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals("push", exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertFalse(exercise.isAccessory)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json = """
            {
                "name": "Bench Press",
                "description": "A compound exercise",
                "movement_type": "push",
                "is_unilateral": false,
                "is_upper": true,
                "is_accessory": false,
                "unknown_property": "should be ignored"
            }
        """.trimIndent()

        // When
        val exercise = objectMapper.readValue(json, Exercise::class.java)

        // Then
        assertEquals("Bench Press", exercise.name)
        assertEquals("A compound exercise", exercise.description)
        assertEquals("push", exercise.movementType)
        assertFalse(exercise.isUnilateral)
        assertTrue(exercise.isUpper)
        assertFalse(exercise.isAccessory)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val exercise1 = Exercise(
            name = "Bench Press",
            description = "A compound exercise",
            movementType = "push",
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
        val exercise2 = Exercise(
            name = "Bench Press",
            description = "A compound exercise",
            movementType = "push",
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
        val exercise3 = Exercise(
            name = "Squat",
            description = "A compound exercise",
            movementType = "push",
            isUnilateral = false,
            isUpper = false,
            isAccessory = false
        )

        // Then
        assertEquals(exercise1, exercise2)
        assertEquals(exercise1.hashCode(), exercise2.hashCode())
        assertFalse(exercise1 == exercise3)
        assertFalse(exercise1.hashCode() == exercise3.hashCode())
    }
} 