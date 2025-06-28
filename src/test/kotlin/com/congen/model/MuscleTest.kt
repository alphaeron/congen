package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MuscleTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create muscle with all properties`() {
        // Given & When
        val muscle = Muscle(
            name = "Chest",
            description = "Chest muscles"
        )

        // Then
        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val muscle = Muscle(
            name = "Chest",
            description = "Chest muscles"
        )

        // When
        val json = objectMapper.writeValueAsString(muscle)

        // Then
        assertTrue(json.contains("\"name\":\"Chest\""))
        assertTrue(json.contains("\"description\":\"Chest muscles\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json = """
            {
                "name": "Chest",
                "description": "Chest muscles"
            }
        """.trimIndent()

        // When
        val muscle = objectMapper.readValue(json, Muscle::class.java)

        // Then
        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json = """
            {
                "name": "Chest",
                "description": "Chest muscles",
                "unknown_property": "should be ignored"
            }
        """.trimIndent()

        // When
        val muscle = objectMapper.readValue(json, Muscle::class.java)

        // Then
        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val muscle1 = Muscle(
            name = "Chest",
            description = "Chest muscles"
        )
        val muscle2 = Muscle(
            name = "Chest",
            description = "Chest muscles"
        )
        val muscle3 = Muscle(
            name = "Back",
            description = "Back muscles"
        )

        // Then
        assertEquals(muscle1, muscle2)
        assertEquals(muscle1.hashCode(), muscle2.hashCode())
        assertFalse(muscle1 == muscle3)
        assertFalse(muscle1.hashCode() == muscle3.hashCode())
    }
} 