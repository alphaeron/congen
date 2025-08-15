package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Only create minimal data needed for user tests
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        // Use minimal reference data instead of full data for faster tests
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId, token = userToken)
    }

    @Test
    fun `should get current user`() {
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(userId)
    }
}
