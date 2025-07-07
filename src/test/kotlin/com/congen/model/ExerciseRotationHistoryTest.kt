package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExerciseRotationHistoryTest {
    private val now = LocalDateTime.now()

    @Test
    fun `constructor sets all fields correctly`() {
        val history =
            ExerciseRotationHistory(
                id = 1L,
                userId = 42,
                exerciseName = "Squat",
                isAccessory = true,
                createdAt = now
            )
        assertEquals(1L, history.id)
        assertEquals(42, history.userId)
        assertEquals("Squat", history.exerciseName)
        assertEquals(true, history.isAccessory)
        assertEquals(now, history.createdAt)
    }

    @Test
    fun `should create exercise rotation history with correct properties`() {
        val createdAt = LocalDateTime.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = 1,
                exerciseName = "Bench Press",
                isAccessory = false,
                createdAt = createdAt
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals(1, exerciseRotationHistory.userId)
        assertEquals("Bench Press", exerciseRotationHistory.exerciseName)
        assertEquals(false, exerciseRotationHistory.isAccessory)
        assertEquals(createdAt, exerciseRotationHistory.createdAt)
    }

    @Test
    fun `should create exercise rotation history with accessory category`() {
        val createdAt = LocalDateTime.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 2L,
                userId = 3,
                exerciseName = "Bicep Curl",
                isAccessory = true,
                createdAt = createdAt
            )

        assertEquals(2L, exerciseRotationHistory.id)
        assertEquals(3, exerciseRotationHistory.userId)
        assertEquals("Bicep Curl", exerciseRotationHistory.exerciseName)
        assertEquals(true, exerciseRotationHistory.isAccessory)
        assertEquals(createdAt, exerciseRotationHistory.createdAt)
    }
}
