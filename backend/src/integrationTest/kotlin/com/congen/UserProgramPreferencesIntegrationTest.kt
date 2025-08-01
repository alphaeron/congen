package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserProgramPreferencesIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a single test user to avoid keycloak_user_id conflicts
        val unique = System.nanoTime()
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User $unique", token = token)
    }

    @Test
    fun `should create user program preferences`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60, token = token)

        // Verify the preferences were created correctly
        webTestClient.get()
            .uri("/api/v1/user_program_preferences/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should get user program preferences by user id`() {
        val token = getValidToken("user")
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60, token = token)

        // Then get them by user id
        webTestClient.get()
            .uri("/api/v1/user_program_preferences/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should update user program preferences`() {
        val token = getValidToken("user")
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60, token = token)

        // Then update them
        webTestClient.patch()
            .uri("/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=4&session_time_length_in_minutes=90")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(90)
    }

    @Test
    fun `should delete user program preferences`() {
        val token = getValidToken("user")
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60, token = token)

        // Then delete them
        webTestClient.delete()
            .uri("/api/v1/user_program_preferences/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should handle user program preferences updates`() {
        val token = getValidToken("user")
        // Create initial preferences for the user
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60, token = token)

        // Verify initial preferences
        webTestClient.get()
            .uri("/api/v1/user_program_preferences/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)

        // Update preferences
        webTestClient.patch()
            .uri("/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=4&session_time_length_in_minutes=45")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(45)

        // Verify updated preferences
        webTestClient.get()
            .uri("/api/v1/user_program_preferences/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(45)
    }
}
