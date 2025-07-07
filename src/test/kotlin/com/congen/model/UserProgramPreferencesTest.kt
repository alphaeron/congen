package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserProgramPreferencesTest {
    private val now = LocalDateTime.now()

    @Test
    fun `should create user program preferences with correct properties`() {
        val prefs = UserProgramPreferences(
            userId = 1, 
            programDaysPerWeek = 4, 
            sessionTimeLengthInMinutes = 60,
            createdAt = now,
            updatedAt = now
        )
        assertEquals(1, prefs.userId)
        assertEquals(4, prefs.programDaysPerWeek)
        assertEquals(60, prefs.sessionTimeLengthInMinutes)
        assertEquals(now, prefs.createdAt)
        assertEquals(now, prefs.updatedAt)
    }
}
