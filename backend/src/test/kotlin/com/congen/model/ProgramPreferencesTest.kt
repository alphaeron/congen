package com.congen.model

import com.congen.mockProgramPreferences
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ProgramPreferencesTest {
    private val now = Instant.now()

    @Test
    fun `should create program preferences with correct properties`() {
        val prefs =
            mockProgramPreferences(
                programId = 1L,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, prefs.programId)
        assertEquals(4, prefs.programDaysPerWeek)
        assertEquals(60, prefs.sessionTimeLengthInMinutes)
        assertEquals(now, prefs.createdAt)
        assertEquals(now, prefs.updatedAt)
    }
}
