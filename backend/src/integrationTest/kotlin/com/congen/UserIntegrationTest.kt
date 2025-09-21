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

    @Test
    fun `should update user with physical attributes`() {
        val newName = "Updated User Name"
        val age = 30
        val weight = 180
        val height = 72

        webTestClient.patch()
            .uri("/api/v1/user/me?name=$newName&age=$age&weight=$weight&height=$height")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(newName)
            .jsonPath(".age").isEqualTo(age)
            .jsonPath(".weight").isEqualTo(weight)
            .jsonPath(".height").isEqualTo(height)
    }

    @Test
    fun `should update user with partial physical attributes`() {
        val newName = "Updated User Name"
        val age = 25

        webTestClient.patch()
            .uri("/api/v1/user/me?name=$newName&age=$age")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".keycloak_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(newName)
            .jsonPath(".age").isEqualTo(age)
            .jsonPath(".weight").doesNotExist()
            .jsonPath(".height").doesNotExist()
    }
}
