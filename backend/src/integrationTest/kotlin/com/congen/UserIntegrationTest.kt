package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Only create minimal data needed for user tests
        val unique = System.nanoTime()
        val userName = "UserIntegrationTest User $unique"
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, userName)
        // Use minimal reference data instead of full data for faster tests
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId)
        this.testUserName = userName
    }

    private lateinit var testUserName: String

    @Test
    fun `should get user by id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(IntegrationTestHelpers.TEST_USER_NAME)
            .jsonPath(".age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
    }

    @Test
    fun `should get all users`() {
        webTestClient.get()
            .uri("/api/v1/user/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
