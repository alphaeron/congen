package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

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

        val keycloakId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                token = token
            )
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // Verify the user profile was created successfully
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.keycloak_id").isEqualTo(keycloakId)
            .jsonPath("$.name").isEqualTo(testUserName)
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

        // Create user profile first using helper method
        val keycloakId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                token = token
            )
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // Then get user by Keycloak ID
        webTestClient.get()
            .uri("/api/v1/user/$keycloakId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(keycloakId)
            .jsonPath(".name").isEqualTo(testUserName)
    }

    @Test
    fun `should return 401 when accessing me endpoint without authentication`() {
        webTestClient.get()
            .uri("/api/v1/user/me")
            .exchange()
            .expectStatus().isUnauthorized()
    }
}
