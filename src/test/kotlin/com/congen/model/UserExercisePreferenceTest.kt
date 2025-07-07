package com.congen.model

import com.congen.mockUserExercisePreference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class UserExercisePreferenceTest {
    private val now = Instant.now()

    @Test
    fun `should create user exercise preference with correct properties`() {
        val pref =
            mockUserExercisePreference(
                userId = 1,
                exerciseName = "Bench Press",
                shouldAvoid = true,
                createdAt = now
            )

        assertEquals(1, pref.userId)
        assertEquals("Bench Press", pref.exerciseName)
        assertEquals(true, pref.shouldAvoid)
        assertEquals(now, pref.createdAt)
    }
}
