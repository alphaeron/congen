package com.congen.model

import com.congen.config.JacksonConfig
import com.congen.mockProgram
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ProgramTest {
    private lateinit var objectMapper: ObjectMapper
    private val now = Instant.now()

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        JacksonConfig.configureObjectMapper(objectMapper)
    }

    @Test
    fun `should create program with all properties`() {
        val program =
            mockProgram(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

        assertEquals(1L, program.id)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
        assertTrue(program.isActive)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val program =
            mockProgram(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

        val json = objectMapper.writeValueAsString(program)

        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"user_id\":\"b226d772-c063-4974-ae08-ab64134abbcf\""))
        assertTrue(json.contains("\"name\":\"Conjugate Powerlifting Program\""))
        assertTrue(json.contains("\"current_week_number\":1"))
        assertTrue(json.contains("\"created_at\":\"$now\""))
        assertTrue(json.contains("\"updated_at\":\"$now\""))
        assertTrue(json.contains("\"is_active\":true"))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "id": 1,
                "user_id": "b226d772-c063-4974-ae08-ab64134abbcf",
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "$now",
                "updated_at": "$now",
                "is_active": true
            }
            """.trimIndent()

        val program = objectMapper.readValue(json, Program::class.java)

        assertEquals(1L, program.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", program.userId)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
        assertTrue(program.isActive)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "id": 1,
                "user_id": "b226d772-c063-4974-ae08-ab64134abbcf",
                "name": "Conjugate Powerlifting Program",
                "current_week_number": 1,
                "created_at": "$now",
                "updated_at": "$now",
                "is_active": true,
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val program = objectMapper.readValue(json, Program::class.java)

        assertEquals(1L, program.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", program.userId)
        assertEquals("Conjugate Powerlifting Program", program.name)
        assertEquals(1, program.currentWeekNumber)
        assertEquals(now, program.createdAt)
        assertEquals(now, program.updatedAt)
        assertTrue(program.isActive)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val program1 =
            mockProgram(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        val program2 =
            mockProgram(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        val program3 =
            mockProgram(
                id = 2L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Different Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = false
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
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Original Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

        val copied =
            original.copy(
                name = "Copied Program",
                currentWeekNumber = 2,
                isActive = false
            )

        assertEquals(1L, copied.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", copied.userId)
        assertEquals("Copied Program", copied.name)
        assertEquals(2, copied.currentWeekNumber)
        assertEquals(now, copied.createdAt)
        assertEquals(now, copied.updatedAt)
        assertFalse(copied.isActive)
    }

    @Test
    fun `should have meaningful toString`() {
        val program =
            mockProgram(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                name = "Test Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

        val toString = program.toString()

        assertTrue(toString.contains("id=1"))
        assertTrue(toString.contains("userId=b226d772-c063-4974-ae08-ab64134abbcf"))
        assertTrue(toString.contains("name=Test Program"))
        assertTrue(toString.contains("currentWeekNumber=1"))
        assertTrue(toString.contains("isActive=true"))
    }

    @Test
    fun `should handle isActive field correctly`() {
        val activeProgram = mockProgram(isActive = true)
        val inactiveProgram = mockProgram(isActive = false)

        assertTrue(activeProgram.isActive)
        assertFalse(inactiveProgram.isActive)
    }
}
