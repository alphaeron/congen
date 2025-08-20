package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import java.util.Base64

/**
 * Integration tests for user profile creation with Keycloak OAuth2 integration.
 *
 * These tests verify that authenticated users can create their profiles after
 * registering through Keycloak, without handling passwords in our backend.
 */
@TestPropertySource(
    properties = [
        "spring.profiles.active=integration-test"
    ]
)
class UserKeycloakIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserName: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        testUserName = "UserKeycloakIntegrationTest User $unique"
    }

    @Test
    fun `should create user profile after Keycloak registration`() {
        val token = getValidToken("user")
        val expectedUsername = extractUsernameFromToken(token)

        val keycloakId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                token = token
            )

        // Verify the user profile was created successfully
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.keycloak_id").isEqualTo(keycloakId)
            .jsonPath("$.name").isEqualTo(expectedUsername)
    }

    private fun extractUsernameFromToken(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                throw RuntimeException("Invalid JWT token format")
            }

            val payload = parts[1]
            // Add padding if needed
            val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decodedBytes = Base64.getUrlDecoder().decode(paddedPayload)
            val payloadJson = String(decodedBytes, Charsets.UTF_8)

            val jsonNode = ObjectMapper().readTree(payloadJson)
            // Try to get the name claim, fallback to preferred_username
            jsonNode.get("name")?.asText() ?: jsonNode.get("preferred_username")?.asText() ?: "unknown"
        } catch (e: Exception) {
            throw RuntimeException("Failed to extract username from token", e)
        }
    }

    @Test
    fun `should return 401 when not authenticated`() {
        webTestClient.post()
            .uri("/api/v1/user/?name=$testUserName")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should get user by id after profile creation`() {
        val token = getValidToken("user")
        val expectedUsername = extractUsernameFromToken(token)

        // Create user profile first using helper method
        val keycloakId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                token = token
            )

        // Then get user profile using /me endpoint
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(keycloakId)
            .jsonPath(".name").isEqualTo(expectedUsername)
    }

    @Test
    fun `should return 401 when accessing me endpoint without authentication`() {
        webTestClient.get()
            .uri("/api/v1/user/me")
            .exchange()
            .expectStatus().isUnauthorized()
    }

    @Test
    fun `should automatically create consent record when user profile is created`() {
        val token = getValidToken("user")

        // Create user profile - this should automatically create consent
        IntegrationTestHelpers.createTestUser(
            webTestClient = webTestClient,
            token = token
        )

        // Verify that consent was automatically created
        webTestClient.get()
            .uri("/api/v1/gdpr/consent")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data_processing_consent").isEqualTo(true)
            .jsonPath("$.consent_timestamp").exists()
    }
}
