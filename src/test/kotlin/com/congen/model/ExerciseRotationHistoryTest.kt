package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExerciseRotationHistoryTest {
    @Test
    fun `should create exercise rotation history with correct properties`() {
        val usedAt = LocalDateTime.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                exerciseName = "Bench Press",
                isAccessory = false,
                usedAt = usedAt
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals("Bench Press", exerciseRotationHistory.exerciseName)
        assertEquals(false, exerciseRotationHistory.isAccessory)
        assertEquals(usedAt, exerciseRotationHistory.usedAt)
    }

    @Test
    fun `should create exercise rotation history with null usedAt`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                exerciseName = "Squat",
                isAccessory = false
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals("Squat", exerciseRotationHistory.exerciseName)
        assertEquals(false, exerciseRotationHistory.isAccessory)
        assertEquals(null, exerciseRotationHistory.usedAt)
    }

    @Test
    fun `should create exercise rotation history with accessory category`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 2L,
                exerciseName = "Bicep Curl",
                isAccessory = true
            )

        assertEquals(2L, exerciseRotationHistory.id)
        assertEquals("Bicep Curl", exerciseRotationHistory.exerciseName)
        assertEquals(true, exerciseRotationHistory.isAccessory)
    }
}
