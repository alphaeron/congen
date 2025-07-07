package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
class ProgrammedWorkoutTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = Instant.parse("2024-01-15T10:30:00Z")

    @Test
    fun `test programmed workout creation with all fields`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, programmedWorkout.id)
        assertEquals(123L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Week 1 - Upper Body", programmedWorkout.name)
        assertEquals(now, programmedWorkout.createdAt)
        assertEquals(now, programmedWorkout.updatedAt)
    }

    @Test
    fun `test programmed workout creation with minimal fields`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 2L,
                programId = 456L,
                dayNumber = 2,
                name = "Week 2 - Lower Body",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(2L, programmedWorkout.id)
        assertEquals(456L, programmedWorkout.programId)
        assertEquals(2, programmedWorkout.dayNumber)
        assertEquals("Week 2 - Lower Body", programmedWorkout.name)
        assertEquals(now, programmedWorkout.createdAt)
        assertEquals(now, programmedWorkout.updatedAt)
    }

    @Test
    fun `test programmed workout serialization`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        val json = objectMapper.writeValueAsString(programmedWorkout)
        val nowString = now.toString()

        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"program_id\":123"))
        assertTrue(json.contains("\"day_number\":1"))
        assertTrue(json.contains("\"name\":\"Week 1 - Upper Body\""))
        assertTrue(json.contains("\"created_at\":\"${nowString}\""))
        assertTrue(json.contains("\"updated_at\":\"${nowString}\""))
    }

    @Test
    fun `test programmed workout deserialization`() {
        val json =
            """
            {
                "id": 1,
                "program_id": 123,
                "day_number": 1,
                "name": "Week 1 - Upper Body",
                "created_at": "$now",
                "updated_at": "$now"
            }
            """.trimIndent()

        val programmedWorkout = objectMapper.readValue(json, ProgrammedWorkout::class.java)

        assertEquals(1L, programmedWorkout.id)
        assertEquals(123L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Week 1 - Upper Body", programmedWorkout.name)
        assertEquals(now, programmedWorkout.createdAt)
        assertEquals(now, programmedWorkout.updatedAt)
    }

    @Test
    fun `test programmed workout deserialization with null description`() {
        val json =
            """
            {
                "id": 1,
                "program_id": 123,
                "day_number": 1,
                "name": "Week 1 - Upper Body",
                "created_at": "$now",
                "updated_at": "$now"
            }
            """.trimIndent()

        val programmedWorkout = objectMapper.readValue(json, ProgrammedWorkout::class.java)

        assertEquals(1L, programmedWorkout.id)
        assertEquals(123L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Week 1 - Upper Body", programmedWorkout.name)
        assertEquals(now, programmedWorkout.createdAt)
        assertEquals(now, programmedWorkout.updatedAt)
    }

    @Test
    fun `test programmed workout deserialization with unknown properties`() {
        val json =
            """
            {
                "id": 1,
                "program_id": 123,
                "day_number": 1,
                "name": "Week 1 - Upper Body",
                "created_at": "$now",
                "updated_at": "$now",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val programmedWorkout = objectMapper.readValue(json, ProgrammedWorkout::class.java)

        assertEquals(1L, programmedWorkout.id)
        assertEquals(123L, programmedWorkout.programId)
        assertEquals(1, programmedWorkout.dayNumber)
        assertEquals("Week 1 - Upper Body", programmedWorkout.name)
        assertEquals(now, programmedWorkout.createdAt)
        assertEquals(now, programmedWorkout.updatedAt)
    }

    @Test
    fun `test programmed workout equality`() {
        val workout1 =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        val workout2 =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        val workout3 =
            ProgrammedWorkout(
                id = 2L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(workout1, workout2)
        assertEquals(workout1.hashCode(), workout2.hashCode())
        assertFalse(workout1 == workout3)
        assertFalse(workout1.hashCode() == workout3.hashCode())
    }

    @Test
    fun `test programmed workout copy`() {
        val original =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Original Workout",
                createdAt = now,
                updatedAt = now
            )

        val copied =
            original.copy(
                name = "Copied Workout"
            )

        assertEquals(1L, copied.id)
        assertEquals(123L, copied.programId)
        assertEquals(1, copied.dayNumber)
        assertEquals("Copied Workout", copied.name)
        assertEquals(now, copied.createdAt)
        assertEquals(now, copied.updatedAt)
    }

    @Test
    fun `test programmed workout toString`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 123L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )

        val toString = programmedWorkout.toString()
        assertTrue(toString.contains("id=1"))
        assertTrue(toString.contains("programId=123"))
        assertTrue(toString.contains("dayNumber=1"))
        assertTrue(toString.contains("name=Week 1 - Upper Body"))
    }
}
