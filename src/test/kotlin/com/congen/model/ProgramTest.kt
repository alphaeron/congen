package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.time.LocalDateTime

@SpringBootTest
class ProgramTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = LocalDateTime.now()

    @Test
    fun `should create program with all properties`() {
        // Given & When
        val program =
            Program(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val program =
            Program(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        // When
        val json = objectMapper.writeValueAsString(program)

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"name\":\"Conjugate Powerlifting Program\""))
        assertTrue(json.contains("\"current_week_number\":1"))
        assertTrue(json.contains("\"created_at\":\"${now.toString()}\""))
        assertTrue(json.contains("\"updated_at\":\"${now.toString()}\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "${now.toString()}",
                "updated_at": "${now.toString()}"
            }
            """.trimIndent()

        // When
        val program = objectMapper.readValue(json, Program::class.java)

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "${now.toString()}",
                "updated_at": "${now.toString()}",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val program = objectMapper.readValue(json, Program::class.java)

        // Then
        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val program1 =
            Program(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )
        val program2 =
            Program(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )
        val program3 =
            Program(
                id = 2L,
                userId = 1,
                name = "Different Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now
            )

        // Then
        assertEquals(program1, program2)
        assertEquals(program1.hashCode(), program2.hashCode())
        assertFalse(program1 == program3)
        assertFalse(program1.hashCode() == program3.hashCode())
    }

    @Test
    fun `test program creation with all fields`() {
        val program = Program(
            id = 1L,
            userId = 1,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now,
        )

        assertEquals(1L, program.id)
        assertEquals(1, program.userId)
        assertEquals("Test Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `test program creation with minimal fields`() {
        val program = Program(
            id = 2L,
            userId = 456,
            name = "Minimal Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        assertEquals(2L, program.id)
        assertEquals(456, program.userId)
        assertEquals("Minimal Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
    }

    @Test
    fun `test program equality`() {
        val program1 = Program(
            id = 1L,
            userId = 1,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        val program2 = Program(
            id = 1L,
            userId = 1,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        assertEquals(program1, program2)
        assertEquals(program1.hashCode(), program2.hashCode())
    }

    @Test
    fun `test program inequality`() {
        val program1 = Program(
            id = 1L,
            userId = 1,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        val program2 = Program(
            id = 2L,
            userId = 1,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(program1 == program2)
        assertFalse(program1.hashCode() == program2.hashCode())
    }

    @Test
    fun `test program copy`() {
        val original = Program(
            id = 1L,
            userId = 1,
            name = "Original Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now
        )

        val copied = original.copy(
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
    fun `test program toString`() {
        val program = Program(
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
