package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class UserConsentTest {
    private val now = Instant.now()

    @Test
    fun `should create user consent with consent given`() {
        val userConsent =
            UserConsent(
                keycloakId = "test-keycloak-id",
                dataProcessingConsent = true,
                consentTimestamp = now,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("test-keycloak-id", userConsent.keycloakId)
        assertTrue(userConsent.dataProcessingConsent)
        assertEquals(now, userConsent.consentTimestamp)
        assertEquals(now, userConsent.createdAt)
        assertEquals(now, userConsent.updatedAt)
    }

    @Test
    fun `should create user consent with consent withdrawn`() {
        val userConsent =
            UserConsent(
                keycloakId = "test-keycloak-id",
                dataProcessingConsent = false,
                consentTimestamp = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals("test-keycloak-id", userConsent.keycloakId)
        assertFalse(userConsent.dataProcessingConsent)
        assertNull(userConsent.consentTimestamp)
        assertEquals(now, userConsent.createdAt)
        assertEquals(now, userConsent.updatedAt)
    }

    @Test
    fun `should handle consent timestamp as nullable`() {
        val userConsentWithTimestamp =
            UserConsent(
                keycloakId = "test-id-1",
                dataProcessingConsent = true,
                consentTimestamp = now,
                createdAt = now,
                updatedAt = now
            )

        val userConsentWithoutTimestamp =
            UserConsent(
                keycloakId = "test-id-2",
                dataProcessingConsent = false,
                consentTimestamp = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(now, userConsentWithTimestamp.consentTimestamp)
        assertNull(userConsentWithoutTimestamp.consentTimestamp)
    }
}
