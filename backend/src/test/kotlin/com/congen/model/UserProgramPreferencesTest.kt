package com.congen.model

import com.congen.mockUserProgramPreferences
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class UserProgramPreferencesTest {
    private val now = Instant.now()

    @Test
    fun `should create user program preferences with correct properties`() {
        val prefs =
            mockUserProgramPreferences(
                userId = "test-keycloak-user-id",
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("test-keycloak-user-id", prefs.userId)
        assertEquals(4, prefs.programDaysPerWeek)
        assertEquals(60, prefs.sessionTimeLengthInMinutes)
        assertEquals(now, prefs.createdAt)
        assertEquals(now, prefs.updatedAt)
    }
}
