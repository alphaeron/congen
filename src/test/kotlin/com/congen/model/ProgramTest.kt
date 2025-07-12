package com.congen.model

import com.congen.mockProgram
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
class ProgramTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = Instant.now()

    @Test
    fun `should create program with all properties`() {
        val program =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val program =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        val json = objectMapper.writeValueAsString(program)

        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"user_id\":1"))
        assertTrue(json.contains("\"name\":\"Conjugate Powerlifting Program\""))
        assertTrue(json.contains("\"current_week_number\":1"))
        assertTrue(json.contains("\"created_at\":\"$now\""))
        assertTrue(json.contains("\"updated_at\":\"$now\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "id": 1,
                "user_id": 1,
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "$now",
                "updated_at": "$now"
            }
            """.trimIndent()

        val program = objectMapper.readValue(json, Program::class.java)

        assertEquals(1L, program.id)
        assertEquals(1, program.userId)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "id": 1,
                "user_id": 1,
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "$now",
                "updated_at": "$now",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val program = objectMapper.readValue(json, Program::class.java)

        assertEquals(1L, program.id)
        assertEquals(1, program.userId)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val program1 =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )
        val program2 =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )
        val program3 =
            mockProgram(
                id = 2L,
                userId = 1,
                name = "Different Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(program1, program2)
        assertEquals(program1.hashCode(), program2.hashCode())
        assertFalse(program1 == program3)
        assertFalse(program1.hashCode() == program3.hashCode())
    }

    @Test
    fun `should copy program correctly`() {
        val original =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Original Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        val copied =
            original.copy(
                name = "Copied Program",
                currentWeekNumber = 2
            )

        assertEquals(1L, copied.id)
        assertEquals(1, copied.userId)
        assertEquals("Copied Program", copied.name)
        assertEquals(2, copied.currentWeekNumber)
        assertEquals(now, copied.createdAt)
        assertEquals(now, copied.updatedAt)
    }

    @Test
    fun `should have meaningful toString`() {
        val program =
            mockProgram(
                id = 1L,
                userId = 1,
                name = "Test Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        val toString = program.toString()

        assertTrue(toString.contains("id=1"))
        assertTrue(toString.contains("userId=1"))
        assertTrue(toString.contains("name=Test Program"))
        assertTrue(toString.contains("currentWeekNumber=1"))
    }
}
