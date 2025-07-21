package com.congen.model

import com.congen.mockMuscle
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
        val muscle = mockMuscle(name = "Chest", description = "Chest muscles")

        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val muscle = mockMuscle(name = "Chest", description = "Chest muscles")

        val json = objectMapper.writeValueAsString(muscle)

        assertTrue(json.contains("\"name\":\"Chest\""))
        assertTrue(json.contains("\"description\":\"Chest muscles\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "name": "Chest",
                "description": "Chest muscles"
            }
            """.trimIndent()

        val muscle = objectMapper.readValue(json, Muscle::class.java)

        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "name": "Chest",
                "description": "Chest muscles",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val muscle = objectMapper.readValue(json, Muscle::class.java)

        assertEquals("Chest", muscle.name)
        assertEquals("Chest muscles", muscle.description)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val muscle1 = mockMuscle(name = "Chest", description = "Chest muscles")
        val muscle2 = mockMuscle(name = "Chest", description = "Chest muscles")
        val muscle3 = mockMuscle(name = "Back", description = "Back muscles")

        assertEquals(muscle1, muscle2)
        assertEquals(muscle1.hashCode(), muscle2.hashCode())
        assertFalse(muscle1 == muscle3)
        assertFalse(muscle1.hashCode() == muscle3.hashCode())
    }
}
