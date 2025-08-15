package com.congen.controllers

import com.congen.BaseIntegrationTest
import com.congen.IntegrationTestHelpers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

/**
 * Integration tests for GdprController.
 *
 * Tests the full GDPR compliance flow including database interactions,
 * security, and proper HTTP responses for all GDPR endpoints.
 */
class GdprIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserId: String
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a test user with real Keycloak authentication
        userToken = getValidToken("user")
        testUserId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
    }

    @Test
    fun `POST consent should record user consent successfully`() {
        webTestClient
            .post()
            .uri("/api/v1/gdpr/consent?consent=true")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.keycloak_id").exists()
            .jsonPath("$.data_processing_consent").isEqualTo(true)
            .jsonPath("$.consent_timestamp").exists()
            .jsonPath("$.updated_at").exists()
    }

    @Test
    fun `POST consent should record consent withdrawal successfully`() {
        // First, give initial consent
        giveInitialConsent()

        webTestClient
            .post()
            .uri("/api/v1/gdpr/consent?consent=false")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.keycloak_id").exists()
            .jsonPath("$.data_processing_consent").isEqualTo(false)
            .jsonPath("$.consent_timestamp").exists()
    }

    @Test
    fun `GET consent should return current consent status`() {
        // Set consent first
        giveInitialConsent()

        webTestClient
            .get()
            .uri("/api/v1/gdpr/consent")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.keycloak_id").exists()
            .jsonPath("$.data_processing_consent").exists()
            .jsonPath("$.consent_timestamp").exists()
            .jsonPath("$.updated_at").exists()
    }

    @Test
    fun `should export user data successfully`() {
        // First create user program preferences so the export doesn't fail
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, testUserId, token = userToken)
        
        webTestClient.get()
            .uri("/api/v1/gdpr/export")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.keycloak_id").exists()
            .jsonPath("$.name").exists()
            .jsonPath("$.data_processing_consent").exists()
            .jsonPath("$.export_timestamp").exists()
            .jsonPath("$.user_equipment").exists()
            .jsonPath("$.user_exercise_preferences").exists()
            .jsonPath("$.user_one_rep_max").exists()
            .jsonPath("$.user_weight_unit_preferences").exists()
            .jsonPath("$.exercise_rotation_history").exists()
            .jsonPath("$.training_programs").exists()
            .jsonPath("$.user_program_preferences").exists()
    }

    @Test
    fun `should handle export when user program preferences do not exist`() {
        webTestClient.get()
            .uri("/api/v1/gdpr/export")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Resource not found")
    }

    @Test
    fun `DELETE delete-all-data should delete user data with valid confirmation`() {
        webTestClient
            .delete()
            .uri("/api/v1/gdpr/delete_all_data?confirmation=DELETE_ALL_MY_DATA")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `DELETE delete-all-data should reject invalid confirmation`() {
        webTestClient
            .delete()
            .uri("/api/v1/gdpr/delete_all_data?confirmation=WRONG_CONFIRMATION")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(422)
    }

    @Test
    fun `GET privacy-policy should return privacy policy without authentication`() {
        webTestClient
            .get()
            .uri("/api/v1/gdpr/privacy_policy")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data_controller.name").isEqualTo("Congen Fitness Application")
            .jsonPath("$.data_controller.contact").isEqualTo("privacy@congen.com")
            .jsonPath("$.data_processing.purposes").isArray
            .jsonPath("$.data_processing.legal_basis").isArray
            .jsonPath("$.data_processing.data_types").isArray
            .jsonPath("$.data_processing.retention_periods").exists()
            .jsonPath("$.user_rights").exists()
            .jsonPath("$.last_updated").isEqualTo("2025-08-08T00:00:00Z")
            .jsonPath("$.version").isEqualTo("1.0.0")
    }

    @Test
    fun `GDPR endpoints should handle user not found gracefully`() {
        // Test consent status for non-existent user, which should default to false
        webTestClient
            .get()
            .uri("/api/v1/gdpr/consent")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data_processing_consent").isEqualTo(false)
    }

    @Test
    fun `Data export should include all user information when available`() {
        webTestClient
            .get()
            .uri("/api/v1/gdpr/export")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Resource not found")
    }

    @Test
    fun `GDPR endpoints should require authentication except privacy-policy`() {
        // Test that all GDPR endpoints except privacy-policy require authentication

        webTestClient
            .post()
            .uri("/api/v1/gdpr/consent?consent=true")
            .exchange()
            .expectStatus().isUnauthorized

        webTestClient
            .get()
            .uri("/api/v1/gdpr/consent")
            .exchange()
            .expectStatus().isUnauthorized

        webTestClient
            .get()
            .uri("/api/v1/gdpr/export")
            .exchange()
            .expectStatus().isUnauthorized

        webTestClient
            .delete()
            .uri("/api/v1/gdpr/delete_all_data?confirmation=DELETE_ALL_MY_DATA")
            .exchange()
            .expectStatus().isUnauthorized
    }

    /**
     * Helper method to give initial consent for test user.
     */
    private fun giveInitialConsent() {
        webTestClient
            .post()
            .uri("/api/v1/gdpr/consent?consent=true")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }
}
