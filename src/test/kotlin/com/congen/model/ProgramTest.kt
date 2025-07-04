package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgramTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create program with all properties`() {
        // Given & When
        val program =
            Program(
                id = 1L,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals("A comprehensive conjugate powerlifting program", program.description)
    }

    @Test
    fun `should create program with null description`() {
        // Given & When
        val program =
            Program(
                id = 1L,
                name = "Conjugate Powerlifting Program",
                description = null,
            )

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertNull(program.description)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val program =
            Program(
                id = 1L,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )

        // When
        val json = objectMapper.writeValueAsString(program)

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"name\":\"Conjugate Powerlifting Program\""))
        assertTrue(json.contains("\"description\":\"A comprehensive conjugate powerlifting program\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Conjugate Powerlifting Program",
                "description": "A comprehensive conjugate powerlifting program"
            }
            """.trimIndent()

        // When
        val program = objectMapper.readValue(json, Program::class.java)

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals("A comprehensive conjugate powerlifting program", program.description)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Conjugate Powerlifting Program",
                "description": "A comprehensive conjugate powerlifting program",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val program = objectMapper.readValue(json, Program::class.java)

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals("A comprehensive conjugate powerlifting program", program.description)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val program1 =
            Program(
                id = 1L,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )
        val program2 =
            Program(
                id = 1L,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )
        val program3 =
            Program(
                id = 2L,
                name = "Different Program",
                description = "A different program",
            )

        // Then
        assertEquals(program1, program2)
        assertEquals(program1.hashCode(), program2.hashCode())
        assertFalse(program1 == program3)
        assertFalse(program1.hashCode() == program3.hashCode())
    }
}
