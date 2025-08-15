package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class UserDataExportTest {
    private val now = Instant.now()
    private val createdAt = now.minusSeconds(3600) // 1 hour ago
    private val updatedAt = now.minusSeconds(1800) // 30 minutes ago
    private val consentTimestamp = now.minusSeconds(900) // 15 minutes ago

    @Test
    fun `should create user data export with all fields`() {
        val userDataExport =
            UserDataExport(
                keycloakId = "test-keycloak-id",
                name = "John Doe",
                createdAt = createdAt,
                updatedAt = updatedAt,
                dataProcessingConsent = true,
                consentTimestamp = consentTimestamp,
                exportTimestamp = now
            )

        assertEquals("test-keycloak-id", userDataExport.keycloakId)
        assertEquals("John Doe", userDataExport.name)
        assertEquals(createdAt, userDataExport.createdAt)
        assertEquals(updatedAt, userDataExport.updatedAt)
        assertTrue(userDataExport.dataProcessingConsent)
        assertEquals(consentTimestamp, userDataExport.consentTimestamp)
        assertEquals(now, userDataExport.exportTimestamp)
    }

    @Test
    fun `should create user data export with consent withdrawn`() {
        val userDataExport =
            UserDataExport(
                keycloakId = "test-keycloak-id",
                name = "Jane Smith",
                createdAt = createdAt,
                updatedAt = updatedAt,
                dataProcessingConsent = false,
                consentTimestamp = null,
                exportTimestamp = now
            )

        assertEquals("test-keycloak-id", userDataExport.keycloakId)
        assertEquals("Jane Smith", userDataExport.name)
        assertEquals(createdAt, userDataExport.createdAt)
        assertEquals(updatedAt, userDataExport.updatedAt)
        assertFalse(userDataExport.dataProcessingConsent)
        assertNull(userDataExport.consentTimestamp)
        assertEquals(now, userDataExport.exportTimestamp)
    }

    @Test
    fun `should handle different user names`() {
        val userDataExport =
            UserDataExport(
                keycloakId = "test-id",
                name = "Test User",
                createdAt = createdAt,
                updatedAt = updatedAt,
                dataProcessingConsent = true,
                consentTimestamp = consentTimestamp,
                exportTimestamp = now
            )

        assertEquals("Test User", userDataExport.name)
    }

    @Test
    fun `should handle different user types`() {
        val youngUser =
            UserDataExport(
                keycloakId = "young-user",
                name = "Young User",
                createdAt = createdAt,
                updatedAt = updatedAt,
                dataProcessingConsent = true,
                consentTimestamp = consentTimestamp,
                exportTimestamp = now
            )

        val olderUser =
            UserDataExport(
                keycloakId = "older-user",
                name = "Older User",
                createdAt = createdAt,
                updatedAt = updatedAt,
                dataProcessingConsent = true,
                consentTimestamp = consentTimestamp,
                exportTimestamp = now
            )

        assertEquals("Young User", youngUser.name)
        assertEquals("Older User", olderUser.name)
    }
}
