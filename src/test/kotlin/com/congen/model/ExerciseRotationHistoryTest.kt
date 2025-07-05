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
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary",
                usedAt = usedAt
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals(123L, exerciseRotationHistory.userId)
        assertEquals("Bench Press", exerciseRotationHistory.exerciseName)
        assertEquals("primary", exerciseRotationHistory.category)
        assertEquals(usedAt, exerciseRotationHistory.usedAt)
    }

    @Test
    fun `should create exercise rotation history with null usedAt`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = 123L,
                exerciseName = "Squat",
                category = "secondary"
            )

        assertEquals(1L, exerciseRotationHistory.id)
        assertEquals(123L, exerciseRotationHistory.userId)
        assertEquals("Squat", exerciseRotationHistory.exerciseName)
        assertEquals("secondary", exerciseRotationHistory.category)
        assertEquals(null, exerciseRotationHistory.usedAt)
    }

    @Test
    fun `should create exercise rotation history with accessory category`() {
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 2L,
                userId = 456L,
                exerciseName = "Bicep Curl",
                category = "accessory"
            )

        assertEquals(2L, exerciseRotationHistory.id)
        assertEquals(456L, exerciseRotationHistory.userId)
        assertEquals("Bicep Curl", exerciseRotationHistory.exerciseName)
        assertEquals("accessory", exerciseRotationHistory.category)
    }
} 
