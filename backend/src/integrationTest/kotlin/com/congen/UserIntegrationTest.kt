package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserName: String
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Only create minimal data needed for user tests
        val unique = System.nanoTime()
        val userName = "UserIntegrationTest User $unique"
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        // Use minimal reference data instead of full data for faster tests
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId, token = userToken)
        this.testUserName = userName
    }

    @Test
    fun `should get user by id`() {
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(testUserName)
    }

    @Test
    fun `should get all users`() {
        webTestClient.get()
            .uri("/api/v1/user/")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
