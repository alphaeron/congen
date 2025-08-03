package com.congen.model

import com.congen.mockExerciseRotationHistory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ExerciseRotationHistoryTest {
    private val now = Instant.now()

    @Test
    fun `constructor sets all fields correctly`() {
        val history =
            mockExerciseRotationHistory(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                exerciseName = "Squat",
                isAccessory = true,
                createdAt = now
            )

        assertEquals(1L, history.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", history.userId)
        assertEquals("Squat", history.exerciseName)
        assertEquals(true, history.isAccessory)
        assertEquals(now, history.createdAt)
    }

    @Test
    fun `should create exercise rotation history with correct properties`() {
        val exerciseRotationHistory =
            mockExerciseRotationHistory(
                id = 1L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                exerciseName = "Bench Press",
                isAccessory = false,
                createdAt = now
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", exerciseRotationHistory.userId)
        assertEquals("Bench Press", exerciseRotationHistory.exerciseName)
        assertEquals(false, exerciseRotationHistory.isAccessory)
        assertEquals(now, exerciseRotationHistory.createdAt)
    }

    @Test
    fun `should create exercise rotation history with accessory category`() {
        val exerciseRotationHistory =
            mockExerciseRotationHistory(
                id = 2L,
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                exerciseName = "Bicep Curl",
                isAccessory = true,
                createdAt = now
            )

        assertEquals(2L, exerciseRotationHistory.id)
        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", exerciseRotationHistory.userId)
        assertEquals("Bicep Curl", exerciseRotationHistory.exerciseName)
        assertEquals(true, exerciseRotationHistory.isAccessory)
        assertEquals(now, exerciseRotationHistory.createdAt)
    }
}
