package com.congen.service

import com.congen.client.PostgresClient
import com.congen.model.AuditLog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Test class for AuditService.
 *
 * Verifies GDPR-compliant audit logging functionality including:
 * - Data operation logging
 * - Consent change tracking
 * - Security violation logging
 * - Error handling and database interaction
 */
class AuditServiceTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var auditService: AuditService

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        auditService = AuditService(postgresClient, true)
    }

    @Test
    fun `logDataOperation should insert audit log to database`() {
        val keycloakId = "test-user-id"
        val operation = "DATA_ACCESS"
        val dataType = "USER_PROFILE"
        val userId = "admin-user"
        val additionalInfo = "Profile view"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = operation,
            dataType = dataType,
            performedBy = userId,
            timestamp = Instant.now(),
            additionalInfo = additionalInfo
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, operation, dataType, userId, additionalInfo))
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = operation,
                dataType = dataType,
                userId = userId,
                additionalInfo = additionalInfo
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify that the database was called with the audit log insert
        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, operation, dataType, userId, additionalInfo)
    }

    @Test
    fun `logDataOperation should handle null optional parameters`() {
        val keycloakId = "test-user-id"
        val operation = "DATA_ACCESS"
        val dataType = "USER_PROFILE"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = operation,
            dataType = dataType,
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = null
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, operation, dataType, null, null))
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = operation,
                dataType = dataType
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify that the database was called with nulls for optional parameters
        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, operation, dataType, null, null)
    }

    @Test
    fun `logDataOperation should handle database errors gracefully`() {
        val keycloakId = "test-user-id"
        val operation = "DATA_ACCESS"
        val dataType = "USER_PROFILE"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        // Mock database error
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, operation, dataType, null, null))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        StepVerifier.create(
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = operation,
                dataType = dataType
            )
        )
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `logDataAccess should only log admin access, not user self-access`() {
        val keycloakId = "test-user-id"
        val dataType = "USER_PROFILE"
        val adminUser = "admin-user"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = "DATA_ACCESS",
            dataType = dataType,
            performedBy = adminUser,
            timestamp = Instant.now(),
            additionalInfo = null
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, "DATA_ACCESS", dataType, adminUser, null))
            .thenReturn(Mono.just(mockAuditLog))

        // Admin access should be logged
        StepVerifier.create(
            auditService.logDataAccess(
                keycloakId = keycloakId,
                dataType = dataType,
                accessedBy = adminUser
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify admin access was logged
        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, "DATA_ACCESS", dataType, adminUser, null)
    }

    @Test
    fun `logDataAccess should skip logging for user self-access`() {
        val keycloakId = "test-user-id"
        val dataType = "USER_PROFILE"

        // User self-access should not be logged (no database call should be made)
        StepVerifier.create(
            auditService.logDataAccess(
                keycloakId = keycloakId,
                dataType = dataType,
                accessedBy = keycloakId
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify no database call was made for self-access
        verify(postgresClient, never()).update<AuditLog>(
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `logConsentChange should log consent given correctly`() {
        val keycloakId = "test-user-id"
        val consentType = "data_processing"
        val consentGiven = true

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = "CONSENT_GIVEN",
            dataType = consentType,
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = "Consent: true"
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, "CONSENT_GIVEN", consentType, null, "Consent: true"))
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logConsentChange(
                keycloakId = keycloakId,
                consentType = consentType,
                consentGiven = consentGiven
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, "CONSENT_GIVEN", consentType, null, "Consent: true")
    }

    @Test
    fun `logConsentChange should log consent withdrawn correctly`() {
        val keycloakId = "test-user-id"
        val consentType = "data_processing"
        val consentGiven = false

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = "CONSENT_WITHDRAWN",
            dataType = consentType,
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = "Consent: false"
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, "CONSENT_WITHDRAWN", consentType, null, "Consent: false"))
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logConsentChange(
                keycloakId = keycloakId,
                consentType = consentType,
                consentGiven = consentGiven
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, "CONSENT_WITHDRAWN", consentType, null, "Consent: false")
    }

    @Test
    fun `logDataModification should only log admin modifications, not user self-modifications`() {
        val keycloakId = "test-user-id"
        val dataType = "USER_PROFILE"
        val adminUser = "admin-user"
        val changes = "Updated email address"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = "DATA_MODIFICATION",
            dataType = dataType,
            performedBy = adminUser,
            timestamp = Instant.now(),
            additionalInfo = changes
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, "DATA_MODIFICATION", dataType, adminUser, changes))
            .thenReturn(Mono.just(mockAuditLog))

        // Admin modification should be logged
        StepVerifier.create(
            auditService.logDataModification(
                keycloakId = keycloakId,
                dataType = dataType,
                modifiedBy = adminUser,
                changes = changes
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, "DATA_MODIFICATION", dataType, adminUser, changes)
    }

    @Test
    fun `logDataModification should skip logging for user self-modifications`() {
        val keycloakId = "test-user-id"
        val dataType = "USER_PROFILE"
        val changes = "Updated email address"

        // User self-modification should not be logged
        StepVerifier.create(
            auditService.logDataModification(
                keycloakId = keycloakId,
                dataType = dataType,
                modifiedBy = keycloakId,
                changes = changes
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify no database call was made for self-modification
        verify(postgresClient, never()).update<AuditLog>(
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `logSecurityViolation should log violation with known user`() {
        val keycloakId = "suspicious-user-id"
        val violation = "Multiple failed login attempts"
        val severity = "HIGH"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = "SECURITY_VIOLATION",
            dataType = "SECURITY",
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = "Severity: HIGH - Multiple failed login attempts"
        )

        // Mock the database call to return success
        whenever(
            postgresClient.update<AuditLog>(
                expectedQuery,
                keycloakId,
                "SECURITY_VIOLATION",
                "SECURITY",
                null,
                "Severity: HIGH - Multiple failed login attempts"
            )
        )
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logSecurityViolation(
                keycloakId = keycloakId,
                violation = violation,
                severity = severity
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(
            postgresClient
        ).update<AuditLog>(
            expectedQuery,
            keycloakId,
            "SECURITY_VIOLATION",
            "SECURITY",
            null,
            "Severity: HIGH - Multiple failed login attempts"
        )
    }

    @Test
    fun `logSecurityViolation should handle unknown user`() {
        val violation = "Unauthorized access attempt"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = "UNKNOWN",
            operation = "SECURITY_VIOLATION",
            dataType = "SECURITY",
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = "Severity: HIGH - Unauthorized access attempt"
        )

        // Mock the database call to return success
        whenever(
            postgresClient.update<AuditLog>(
                expectedQuery,
                "UNKNOWN",
                "SECURITY_VIOLATION",
                "SECURITY",
                null,
                "Severity: HIGH - Unauthorized access attempt"
            )
        )
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logSecurityViolation(
                keycloakId = null,
                violation = violation
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(
            postgresClient
        ).update<AuditLog>(expectedQuery, "UNKNOWN", "SECURITY_VIOLATION", "SECURITY", null, "Severity: HIGH - Unauthorized access attempt")
    }

    @Test
    fun `logSecurityViolation should use default severity when not specified`() {
        val violation = "Suspicious activity detected"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = "user-id",
            operation = "SECURITY_VIOLATION",
            dataType = "SECURITY",
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = "Severity: HIGH - Suspicious activity detected"
        )

        // Mock the database call to return success
        whenever(
            postgresClient.update<AuditLog>(
                expectedQuery,
                "user-id",
                "SECURITY_VIOLATION",
                "SECURITY",
                null,
                "Severity: HIGH - Suspicious activity detected"
            )
        )
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logSecurityViolation(
                keycloakId = "user-id",
                violation = violation
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        verify(
            postgresClient
        ).update<AuditLog>(
            expectedQuery,
            "user-id",
            "SECURITY_VIOLATION",
            "SECURITY",
            null,
            "Severity: HIGH - Suspicious activity detected"
        )
    }

    @Test
    fun `service should handle various operation types`() {
        val operations =
            listOf(
                "DATA_ACCESS",
                "DATA_MODIFICATION",
                "DATA_DELETION",
                "CONSENT_GIVEN",
                "CONSENT_WITHDRAWN"
            )

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        // Mock the database call for each operation type
        operations.forEach { operation ->
            val mockAuditLog = AuditLog(
                id = 1L,
                keycloakId = "test-user",
                operation = operation,
                dataType = "USER_DATA",
                performedBy = null,
                timestamp = Instant.now(),
                additionalInfo = null
            )
            whenever(postgresClient.update<AuditLog>(expectedQuery, "test-user", operation, "USER_DATA", null, null))
                .thenReturn(Mono.just(mockAuditLog))
        }

        operations.forEach { operation ->
            StepVerifier.create(
                auditService.logDataOperation(
                    keycloakId = "test-user",
                    operation = operation,
                    dataType = "USER_DATA"
                )
            )
                .expectNext(Unit)
                .verifyComplete()
        }

        // Verify all operations were logged
        operations.forEach { operation ->
            verify(postgresClient).update<AuditLog>(expectedQuery, "test-user", operation, "USER_DATA", null, null)
        }
    }

    @Test
    fun `service should create proper audit log format with minimal data`() {
        // This test ensures the buildAuditLogMessage method works correctly
        // by checking the database parameters include expected structure
        val keycloakId = "test-user-id"
        val operation = "DATA_ACCESS"
        val dataType = "USER_PROFILE"
        val additionalInfo = "Profile update"

        val expectedQuery =
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent()

        val mockAuditLog = AuditLog(
            id = 1L,
            keycloakId = keycloakId,
            operation = operation,
            dataType = dataType,
            performedBy = null,
            timestamp = Instant.now(),
            additionalInfo = additionalInfo
        )

        // Mock the database call to return success
        whenever(postgresClient.update<AuditLog>(expectedQuery, keycloakId, operation, dataType, null, additionalInfo))
            .thenReturn(Mono.just(mockAuditLog))

        StepVerifier.create(
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = operation,
                dataType = dataType,
                additionalInfo = additionalInfo
            )
        )
            .expectNext(Unit)
            .verifyComplete()

        // Verify the database was called with the expected parameters
        verify(postgresClient).update<AuditLog>(expectedQuery, keycloakId, operation, dataType, null, additionalInfo)
    }
}
