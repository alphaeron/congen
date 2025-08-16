package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserConsent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for GdprComplianceDAL.
 *
 * Tests GDPR-specific database operations including consent management,
 * user existence checks, and consent history tracking.
 */
class GdprComplianceDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var gdprComplianceDAL: GdprComplianceDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock(PostgresClient::class.java)
        gdprComplianceDAL = GdprComplianceDAL(postgresClient)
    }

    @Test
    fun `hasUserConsent should return true when user has given consent`() {
        val keycloakId = "test-user-123"
        val mockResult: Map<String, Any> = mapOf("data_processing_consent" to true)

        `when`(
            postgresClient.selectIndividual<Map<String, Any>>(
                "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
                keycloakId
            )
        ).thenReturn(Mono.just(mockResult))

        StepVerifier.create(gdprComplianceDAL.hasUserConsent(keycloakId))
            .assertNext { consent ->
                assertTrue(consent)
            }
            .verifyComplete()

        verify(postgresClient).selectIndividual<Map<String, Any>>(
            "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
            keycloakId
        )
    }

    @Test
    fun `hasUserConsent should return false when user has not given consent`() {
        val keycloakId = "test-user-123"
        val mockResult: Map<String, Any> = mapOf("data_processing_consent" to false)

        `when`(
            postgresClient.selectIndividual<Map<String, Any>>(
                "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
                keycloakId
            )
        ).thenReturn(Mono.just(mockResult))

        StepVerifier.create(gdprComplianceDAL.hasUserConsent(keycloakId))
            .assertNext { consent ->
                assertFalse(consent)
            }
            .verifyComplete()
    }

    @Test
    fun `hasUserConsent should return false when consent field is null`() {
        val keycloakId = "test-user-123"
        // Simulate null value by creating a map with null, which the implementation handles
        val mockResult: Map<String, Any?> = mapOf("data_processing_consent" to null)

        `when`(
            postgresClient.selectIndividual<Map<String, Any>>(
                "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
                keycloakId
            )
        ).thenReturn(Mono.just(mockResult as Map<String, Any>))

        StepVerifier.create(gdprComplianceDAL.hasUserConsent(keycloakId))
            .assertNext { consent ->
                assertFalse(consent)
            }
            .verifyComplete()
    }

    @Test
    fun `hasUserConsent should return false when user does not exist`() {
        val keycloakId = "non-existent-user"

        `when`(
            postgresClient.selectIndividual<Map<String, Any>>(
                "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
                keycloakId
            )
        ).thenReturn(Mono.error(RuntimeException("User not found")))

        StepVerifier.create(gdprComplianceDAL.hasUserConsent(keycloakId))
            .assertNext { consent ->
                assertFalse(consent)
            }
            .verifyComplete()
    }

    @Test
    fun `updateUserConsent should update consent status successfully`() {
        val keycloakId = "test-user-123"
        val consent = true
        val mockUserConsent = UserConsent(keycloakId, consent, Instant.now(), Instant.now(), Instant.now())

        `when`(
            postgresClient.update<UserConsent>(
                """
                INSERT INTO user_consent (keycloak_id, data_processing_consent, consent_timestamp, created_at, updated_at)
                VALUES ($1, $2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (keycloak_id)
                DO UPDATE SET
                    data_processing_consent = EXCLUDED.data_processing_consent,
                    consent_timestamp = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                """.trimIndent(),
                keycloakId,
                consent
            )
        ).thenReturn(Mono.just(mockUserConsent))

        StepVerifier.create(gdprComplianceDAL.updateUserConsent(keycloakId, consent))
            .assertNext { userConsent ->
                assert(userConsent.keycloakId == keycloakId)
                assert(userConsent.dataProcessingConsent == consent)
                assert(userConsent.consentTimestamp != null)
            }
            .verifyComplete()

        verify(postgresClient).update<UserConsent>(
            """
            INSERT INTO user_consent (keycloak_id, data_processing_consent, consent_timestamp, created_at, updated_at)
            VALUES ($1, $2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (keycloak_id)
            DO UPDATE SET
                data_processing_consent = EXCLUDED.data_processing_consent,
                consent_timestamp = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            """.trimIndent(),
            keycloakId,
            consent
        )
    }

    @Test
    fun `updateUserConsent should handle update failure gracefully`() {
        val keycloakId = "test-user-123"
        val consent = true

        `when`(
            postgresClient.update<UserConsent>(
                """
                INSERT INTO user_consent (keycloak_id, data_processing_consent, consent_timestamp, created_at, updated_at)
                VALUES ($1, $2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (keycloak_id)
                DO UPDATE SET
                    data_processing_consent = EXCLUDED.data_processing_consent,
                    consent_timestamp = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                """.trimIndent(),
                keycloakId,
                consent
            )
        ).thenReturn(Mono.error(RuntimeException("Database error")))

        StepVerifier.create(gdprComplianceDAL.updateUserConsent(keycloakId, consent))
            .verifyError(RuntimeException::class.java)
    }
}
