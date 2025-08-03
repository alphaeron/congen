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
                userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                exerciseName = "Bench Press",
                shouldAvoid = true,
                createdAt = now
            )

        assertEquals("b226d772-c063-4974-ae08-ab64134abbcf", pref.userId)
        assertEquals("Bench Press", pref.exerciseName)
        assertEquals(true, pref.shouldAvoid)
        assertEquals(now, pref.createdAt)
    }
}
