package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserName: String
    private var userId: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Only create minimal data needed for user tests
        val unique = System.nanoTime()
        val userName = "UserIntegrationTest User $unique"
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, userName, token = token)
        // Use minimal reference data instead of full data for faster tests
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId, token = token)
        this.testUserName = userName
    }

    @Test
    fun `should get user by id`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(testUserName)
            .jsonPath(".age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
    }

    @Test
    fun `should get all users`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/user/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
