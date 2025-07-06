package com.congen.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProgrammedExerciseTest {
    @Test
    fun `ProgrammedExercise should be created with all required fields`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertNull(programmedExercise.notes)
    }

    @Test
    fun `ProgrammedExercise should be created with all fields`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        assertEquals(1L, programmedExercise.id)
        assertEquals(5L, programmedExercise.workoutStageId)
        assertEquals("Bench Press", programmedExercise.exerciseName)
        assertEquals("Focus on controlled descent", programmedExercise.notes)
    }

    @Test
    fun `ProgrammedExercise should handle different exercise names`() {
        val benchPress =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        val squat =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Back Squat",
                notes = null
            )

        val deadlift =
            ProgrammedExercise(
                id = 3L,
                workoutStageId = 5L,
                exerciseName = "Deadlift",
                notes = null
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
                notes = null
            )

        val exercise2 =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 100L,
                exerciseName = "Bench Press",
                notes = null
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
                notes = null
            )

        val exercise2 =
            ProgrammedExercise(
                id = 999L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
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
                notes = "Focus on controlled descent and proper form"
            )

        val exerciseWithoutNotes =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Squat",
                notes = null
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
                notes = ""
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
                notes = null
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
                notes = null
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
                notes = null
            )

        assertEquals(1L, exercise.id)
        assertEquals(1L, exercise.workoutStageId)
        assertEquals("A", exercise.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should handle maximum reasonable values`() {
        val exercise =
            ProgrammedExercise(
                id = Long.MAX_VALUE,
                workoutStageId = Long.MAX_VALUE,
                exerciseName = "Exercise with maximum ID values",
                notes = null
            )

        assertEquals(Long.MAX_VALUE, exercise.id)
        assertEquals(Long.MAX_VALUE, exercise.workoutStageId)
        assertEquals("Exercise with maximum ID values", exercise.exerciseName)
    }

    @Test
    fun `ProgrammedExercise should support data class copy`() {
        val originalExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Original notes"
            )

        val updatedExercise =
            originalExercise.copy(
                exerciseName = "Squat",
                notes = "Updated notes"
            )

        assertEquals(1L, updatedExercise.id)
        assertEquals(5L, updatedExercise.workoutStageId)
        assertEquals("Squat", updatedExercise.exerciseName)
        assertEquals("Updated notes", updatedExercise.notes)
    }

    @Test
    fun `ProgrammedExercise should support data class equality`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
            )

        val exercise2 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
            )

        val exercise3 =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
            )

        assertEquals(exercise1, exercise2)
        assertNotNull(exercise1 != exercise3)
    }

    @Test
    fun `ProgrammedExercise should support data class toString`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
            )

        val toString = exercise.toString()
        assertNotNull(toString)
        assert(toString.contains("ProgrammedExercise"))
        assert(toString.contains("id=1"))
        assert(toString.contains("workoutStageId=5"))
        assert(toString.contains("exerciseName=Bench Press"))
        assert(toString.contains("notes=Focus on form"))
    }

    @Test
    fun `ProgrammedExercise should support data class hashCode`() {
        val exercise1 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
            )

        val exercise2 =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on form"
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
                notes = "Focus on form"
            )

        val (id, workoutStageId, exerciseName, notes) = exercise

        assertEquals(1L, id)
        assertEquals(5L, workoutStageId)
        assertEquals("Bench Press", exerciseName)
        assertEquals("Focus on form", notes)
    }

    @Test
    fun `ProgrammedExercise should handle null notes`() {
        val exercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
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
                notes = null
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
                notes = null
            )

        assertEquals("5x5 Bench Press", exercise.exerciseName)
    }
} 
