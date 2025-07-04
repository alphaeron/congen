package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserExercisePreferenceTest {
    @Test
    fun `should create user exercise preference with correct properties`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        assertEquals(1, pref.userId)
        assertEquals("Bench Press", pref.exerciseName)
        assertEquals(true, pref.shouldAvoid)
    }
}
