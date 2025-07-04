package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgrammedWorkoutTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create programmed workout with all properties`() {
        // Given & When
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Day 1 - Max Effort Upper",
            )

        // Then
        assertEquals(1L, programmedWorkout.id)
        assertEquals(1L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Day 1 - Max Effort Upper", programmedWorkout.name)
    }

    @Test
    fun `should create programmed workout without id`() {
        // Given & When
        val programmedWorkout =
            ProgrammedWorkout(
                programId = 1L,
                dayNumber = 1,
                name = "Day 1 - Max Effort Upper",
            )

        // Then
        assertNull(programmedWorkout.id)
        assertEquals(1L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Day 1 - Max Effort Upper", programmedWorkout.name)
    }

    @Test
    fun `should create programmed workout with null name`() {
        // Given & When
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = null,
            )

        // Then
        assertEquals(1L, programmedWorkout.id)
        assertEquals(1L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertNull(programmedWorkout.name)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Day 1 - Max Effort Upper",
            )

        // When
        val json = objectMapper.writeValueAsString(programmedWorkout)

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"program_id\":1"))
        assertTrue(json.contains("\"day_number\":1"))
        assertTrue(json.contains("\"name\":\"Day 1 - Max Effort Upper\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "program_id": 1,
                "day_number": 1,
                "name": "Day 1 - Max Effort Upper"
            }
            """.trimIndent()

        // When
        val programmedWorkout = objectMapper.readValue(json, ProgrammedWorkout::class.java)

        // Then
        assertEquals(1L, programmedWorkout.id)
        assertEquals(1L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Day 1 - Max Effort Upper", programmedWorkout.name)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "program_id": 1,
                "day_number": 1,
                "name": "Day 1 - Max Effort Upper",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val programmedWorkout = objectMapper.readValue(json, ProgrammedWorkout::class.java)

        // Then
        assertEquals(1L, programmedWorkout.id)
        assertEquals(1L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Day 1 - Max Effort Upper", programmedWorkout.name)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val programmedWorkout1 =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Day 1 - Max Effort Upper",
            )
        val programmedWorkout2 =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Day 1 - Max Effort Upper",
            )
        val programmedWorkout3 =
            ProgrammedWorkout(
                id = 2L,
                programId = 1L,
                dayNumber = 2,
                name = "Day 2 - Dynamic Effort Lower",
            )

        // Then
        assertEquals(programmedWorkout1, programmedWorkout2)
        assertEquals(programmedWorkout1.hashCode(), programmedWorkout2.hashCode())
        assertFalse(programmedWorkout1 == programmedWorkout3)
        assertFalse(programmedWorkout1.hashCode() == programmedWorkout3.hashCode())
    }
}
