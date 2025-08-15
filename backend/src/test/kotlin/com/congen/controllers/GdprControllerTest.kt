package com.congen.controllers

import com.congen.exceptions.ValidationException
import com.congen.model.UserConsent
import com.congen.model.UserDataExport
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for GdprController.
 *
 * Tests GDPR compliance endpoints including consent management,
 * data export, data deletion, and privacy policy information.
 */
class GdprControllerTest {
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprController: GdprController

    @BeforeEach
    fun setUp() {
        gdprComplianceService = mock(GdprComplianceService::class.java)
        keycloakUtil = mock(KeycloakUtil::class.java)
        gdprController = GdprController(gdprComplianceService, keycloakUtil)
    }

    @Test
    fun `recordConsent should record consent successfully`() {
        val keycloakId = "test-user-123"
        val consent = true

        val userConsent =
            UserConsent(
                keycloakId = keycloakId,
                dataProcessingConsent = consent,
                consentTimestamp = Instant.now(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        `when`(
            gdprComplianceService.recordUserConsent(
                eq(keycloakId),
                eq(consent)
            )
        ).thenReturn(Mono.just(userConsent))

        StepVerifier.create(gdprController.recordConsent(consent))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.dataProcessingConsent == true)
                assert(body.keycloakId == keycloakId)
            }
            .verifyComplete()

        verify(keycloakUtil).getCurrentUserId()
        verify(gdprComplianceService).recordUserConsent(
            eq(keycloakId),
            eq(consent)
        )
    }

    @Test
    fun `recordConsent should record consent withdrawal successfully`() {
        val keycloakId = "test-user-123"
        val consent = false

        val userConsent =
            UserConsent(
                keycloakId = keycloakId,
                dataProcessingConsent = consent,
                consentTimestamp = Instant.now(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        `when`(
            gdprComplianceService.recordUserConsent(
                eq(keycloakId),
                eq(consent)
            )
        ).thenReturn(Mono.just(userConsent))

        StepVerifier.create(gdprController.recordConsent(consent))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.dataProcessingConsent == false)
            }
            .verifyComplete()
    }

    @Test
    fun `getConsentStatus should return user consent status`() {
        val keycloakId = "test-user-123"
        val userConsent =
            UserConsent(
                keycloakId = keycloakId,
                dataProcessingConsent = true,
                consentTimestamp = Instant.now(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        `when`(gdprComplianceService.getUserConsent(keycloakId)).thenReturn(Mono.just(userConsent))

        StepVerifier.create(gdprController.getConsentStatus())
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.keycloakId == keycloakId)
                assert(body.dataProcessingConsent == true)
            }
            .verifyComplete()

        verify(keycloakUtil).getCurrentUserId()
        verify(gdprComplianceService).getUserConsent(keycloakId)
    }

    @Test
    fun `exportPersonalData should export user data successfully`() {
        val keycloakId = "test-user-123"
        val userDataExport =
            UserDataExport(
                keycloakId = keycloakId,
                name = "Test User",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                dataProcessingConsent = true,
                consentTimestamp = Instant.now(),
                exportTimestamp = Instant.now()
            )

        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        `when`(gdprComplianceService.exportUserData(keycloakId)).thenReturn(Mono.just(userDataExport))

        StepVerifier.create(gdprController.exportPersonalData())
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.keycloakId == keycloakId)
                assert(body.name == "Test User")
            }
            .verifyComplete()

        verify(keycloakUtil).getCurrentUserId()
        verify(gdprComplianceService).exportUserData(keycloakId)
    }

    @Test
    fun `exportPersonalData should throw SecurityException when user IDs don't match`() {
        val requestingUserId = "requesting-user-123"
        // Different from requesting user
        val exportedUserId = "different-user-456"
        val userDataExport =
            UserDataExport(
                keycloakId = exportedUserId,
                name = "Test User",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                dataProcessingConsent = true,
                consentTimestamp = Instant.now(),
                exportTimestamp = Instant.now()
            )

        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(requestingUserId))
        `when`(gdprComplianceService.exportUserData(requestingUserId)).thenReturn(Mono.just(userDataExport))

        StepVerifier.create(gdprController.exportPersonalData())
            .verifyError(SecurityException::class.java)

        verify(keycloakUtil).getCurrentUserId()
        verify(gdprComplianceService).exportUserData(requestingUserId)
    }

    @Test
    fun `deleteAllPersonalData should delete data with valid confirmation`() {
        val keycloakId = "test-user-123"
        val confirmation = "DELETE_ALL_MY_DATA"

        `when`(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        `when`(
            gdprComplianceService.deleteAllUserData(
                keycloakId = keycloakId,
                reason = "User request via GDPR endpoint - Right to be forgotten"
            )
        ).thenReturn(Mono.empty())

        StepVerifier.create(gdprController.deleteAllPersonalData(confirmation))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                assert(response.body == null)
            }
            .verifyComplete()

        verify(keycloakUtil).getCurrentUserId()
        verify(gdprComplianceService).deleteAllUserData(
            keycloakId = keycloakId,
            reason = "User request via GDPR endpoint - Right to be forgotten"
        )
    }

    @Test
    fun `deleteAllPersonalData should reject invalid confirmation`() {
        val invalidConfirmation = "WRONG_CONFIRMATION"

        StepVerifier.create(gdprController.deleteAllPersonalData(invalidConfirmation))
            .expectError(ValidationException::class.java)
            .verify()

        // Verify that no services were called with invalid confirmation
        verify(keycloakUtil, never()).getCurrentUserId()
        verify(gdprComplianceService, never()).deleteAllUserData(
            any(),
            any()
        )
    }

    @Test
    fun `getPrivacyPolicyInfo should return privacy policy information`() {
        StepVerifier.create(gdprController.getPrivacyPolicyInfo())
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!

                // Verify data controller information
                assert(body.dataController.name == "Congen Fitness Application")
                assert(body.dataController.contact == "privacy@congen.com")
                assert(body.dataController.dpo == "dpo@congen.com")

                // Verify data processing information
                assert(body.dataProcessing.purposes.contains("Personalized workout generation"))
                assert(body.dataProcessing.legalBasis.contains("Consent (GDPR Article 6.1.a)"))
                assert(body.dataProcessing.dataTypes.contains("Personal identifiers (name)"))

                // Verify user rights information
                assert(body.userRights.access == "You can request a complete copy of your personal data")
                assert(body.userRights.erasure == "You can request deletion of your personal data (right to be forgotten)")

                // Verify metadata
                assert(body.lastUpdated == "2025-08-08T00:00:00Z")
                assert(body.version == "1.0.0")
            }
            .verifyComplete()
    }
}
