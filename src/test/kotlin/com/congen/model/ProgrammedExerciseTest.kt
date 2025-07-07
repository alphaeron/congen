package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class ProgrammedExerciseTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = LocalDateTime.now()

    @Test
    fun `ProgrammedExercise should be created with all required fields`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals(1, programmedExercise.position)
        assertNull(programmedExercise.notes)
        assertEquals(now, programmedExercise.createdAt)
        assertEquals(now, programmedExercise.updatedAt)
    }

    @Test
    fun `ProgrammedExercise should be created with all fields`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals(1, programmedExercise.position)
        assertEquals("Focus on controlled descent", programmedExercise.notes)
        assertEquals(now, programmedExercise.createdAt)
        assertEquals(now, programmedExercise.updatedAt)
    }

    @Test
    fun `ProgrammedExercise should handle different exercise names`() {
        val benchPress =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        val squat =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Back Squat",
                position = 2,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        val deadlift =
            ProgrammedExercise(
                id = 3L,
                workoutStageId = 5L,
                exerciseName = "Deadlift",
                position = 3,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("Bench Press", benchPress.exerciseName)
        assertEquals("Back Squat", squat.exerciseName)
        assertEquals("Deadlift", deadlift.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should handle different workout stage IDs`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        val exercise2 =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 100L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, exercise1.workoutStageId)
        assertEquals(100L, exercise2.workoutStageId)
    }

    @Test
    fun `ProgrammedExercise should handle different IDs`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        val exercise2 =
            ProgrammedExercise(
                id = 999L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, exercise1.id)
        assertEquals(999L, exercise2.id)
    }

    @Test
    fun `ProgrammedExercise should handle notes`() {
        val exerciseWithNotes =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent and proper form",
                createdAt = now,
                updatedAt = now
            )

        val exerciseWithoutNotes =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Squat",
                position = 2,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("Focus on controlled descent and proper form", exerciseWithNotes.notes)
        assertNull(exerciseWithoutNotes.notes)
    }

    @Test
    fun `ProgrammedExercise should handle empty notes`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "",
                createdAt = now,
                updatedAt = now
            )

        assertEquals("", exercise.notes)
    }

    @Test
    fun `ProgrammedExercise should handle long exercise names`() {
        val longName = "Very Long Exercise Name That Exceeds Normal Length"
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = longName,
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(longName, exercise.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should handle special characters in exercise names`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press (Barbell)",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("Bench Press (Barbell)", exercise.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should handle minimum valid values`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 1L,
                exerciseName = "A",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, exercise.id)
        assertEquals(1L, exercise.workoutStageId)
        assertEquals("A", exercise.exerciseName)
        assertEquals(1, exercise.position)
    }

    @Test
    fun `ProgrammedExercise should handle maximum reasonable values`() {
        val exercise =
            ProgrammedExercise(
                id = Long.MAX_VALUE,
                workoutStageId = Long.MAX_VALUE,
                exerciseName = "Exercise with maximum ID values",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(Long.MAX_VALUE, exercise.id)
        assertEquals(Long.MAX_VALUE, exercise.workoutStageId)
        assertEquals("Exercise with maximum ID values", exercise.exerciseName)
        assertEquals(1, exercise.position)
    }

    @Test
    fun `ProgrammedExercise should support data class copy`() {
        val originalExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Original notes",
                createdAt = now,
                updatedAt = now
            )

        val updatedExercise =
            originalExercise.copy(
                exerciseName = "Squat",
                position = 2,
                notes = "Updated notes"
            )

        assertEquals(1L, updatedExercise.id)
        assertEquals(5L, updatedExercise.workoutStageId)
        assertEquals("Squat", updatedExercise.exerciseName)
        assertEquals(2, updatedExercise.position)
        assertEquals("Updated notes", updatedExercise.notes)
        assertEquals(now, updatedExercise.createdAt)
        assertEquals(now, updatedExercise.updatedAt)
    }

    @Test
    fun `ProgrammedExercise should support data class equality`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        val exercise2 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        val exercise3 =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(exercise1, exercise2)
        assertEquals(exercise1.hashCode(), exercise2.hashCode())
        assertFalse(exercise1 == exercise3)
        assertFalse(exercise1.hashCode() == exercise3.hashCode())
    }

    @Test
    fun `ProgrammedExercise should support data class toString`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        val toString = exercise.toString()
        assertTrue(toString.contains("ProgrammedExercise"))
        assertTrue(toString.contains("id=1"))
        assertTrue(toString.contains("workoutStageId=5"))
        assertTrue(toString.contains("exerciseName=Bench Press"))
        assertTrue(toString.contains("position=1"))
        assertTrue(toString.contains("notes=Focus on form"))
    }

    @Test
    fun `ProgrammedExercise should support data class hashCode`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        val exercise2 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(exercise1.hashCode(), exercise2.hashCode())
    }

    @Test
    fun `ProgrammedExercise should support data class component functions`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on form",
                createdAt = now,
                updatedAt = now
            )

        val (id, workoutStageId, exerciseName, position, notes, createdAt, updatedAt) = exercise

        assertEquals(1L, id)
        assertEquals(5L, workoutStageId)
        assertEquals("Bench Press", exerciseName)
        assertEquals(1, position)
        assertEquals("Focus on form", notes)
        assertEquals(now, createdAt)
        assertEquals(now, updatedAt)
    }

    @Test
    fun `ProgrammedExercise should handle null notes`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertNull(exercise.notes)
    }

    @Test
    fun `ProgrammedExercise should handle whitespace in exercise names`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "  Bench Press  ",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("  Bench Press  ", exercise.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should handle numeric exercise names`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "5x5 Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("5x5 Bench Press", exercise.exerciseName)
    }

    @Test
    fun `test programmed exercise serialization`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent",
                createdAt = now,
                updatedAt = now
            )

        val json = objectMapper.writeValueAsString(programmedExercise)

        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"workout_stage_id\":5"))
        assertTrue(json.contains("\"exercise_name\":\"Bench Press\""))
        assertTrue(json.contains("\"position\":1"))
        assertTrue(json.contains("\"notes\":\"Focus on controlled descent\""))
        assertTrue(json.contains("\"created_at\":\"$now\""))
        assertTrue(json.contains("\"updated_at\":\"$now\""))
    }

    @Test
    fun `test programmed exercise deserialization`() {
        val json =
            """
            {
                "id": 1,
                "workout_stage_id": 5,
                "exercise_name": "Bench Press",
                "position": 1,
                "notes": "Focus on controlled descent",
                "created_at": "$now",
                "updated_at": "$now"
            }
            """.trimIndent()

        val programmedExercise = objectMapper.readValue(json, ProgrammedExercise::class.java)

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals(1, programmedExercise.position)
        assertEquals("Focus on controlled descent", programmedExercise.notes)
        assertEquals(now, programmedExercise.createdAt)
        assertEquals(now, programmedExercise.updatedAt)
    }

    @Test
    fun `test programmed exercise deserialization with null notes`() {
        val json =
            """
            {
                "id": 1,
                "workout_stage_id": 5,
                "exercise_name": "Bench Press",
                "position": 1,
                "notes": null,
                "created_at": "$now",
                "updated_at": "$now"
            }
            """.trimIndent()

        val programmedExercise = objectMapper.readValue(json, ProgrammedExercise::class.java)

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals(1, programmedExercise.position)
        assertNull(programmedExercise.notes)
        assertEquals(now, programmedExercise.createdAt)
        assertEquals(now, programmedExercise.updatedAt)
    }

    @Test
    fun `test programmed exercise deserialization with unknown properties`() {
        val json =
            """
            {
                "id": 1,
                "workout_stage_id": 5,
                "exercise_name": "Bench Press",
                "position": 1,
                "notes": "Focus on controlled descent",
                "created_at": "$now",
                "updated_at": "$now",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val programmedExercise = objectMapper.readValue(json, ProgrammedExercise::class.java)

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals(1, programmedExercise.position)
        assertEquals("Focus on controlled descent", programmedExercise.notes)
        assertEquals(now, programmedExercise.createdAt)
        assertEquals(now, programmedExercise.updatedAt)
    }
}
