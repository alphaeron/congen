package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserName: String
    private var userId: String = ""

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Only create minimal data needed for user tests
        val unique = System.nanoTime()
        val userName = "UserIntegrationTest User $unique"
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Use minimal reference data instead of full data for faster tests
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId, token = token)
        this.testUserName = userName
    }

    @Test
    fun `should get user by id`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(testUserName)
    }

    @Test
    fun `should get all users`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/user/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
