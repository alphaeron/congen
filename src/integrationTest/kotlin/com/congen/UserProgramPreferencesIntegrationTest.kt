package com.congen

import com.congen.model.UserProgramPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserProgramPreferencesIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0
    private var userId1: Int = 0
    private var userId2: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create unique users for each test
        val unique = System.nanoTime()
        userId = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User $unique")
        userId1 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 1 $unique")
        userId2 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 2 $unique")
    }

    @Test
    fun `should create user program preferences`() {
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60)
        
        // Verify the preferences were created correctly
        webTestClient.get()
            .uri("/user_program_preferences/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should get user program preferences by user id`() {
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60)

        // Then get them by user id
        webTestClient.get()
            .uri("/user_program_preferences/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should update user program preferences`() {
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60)

        // Then update them
        webTestClient.patch()
            .uri("/user_program_preferences/?userId=$userId&programDaysPerWeek=4&sessionTimeLengthInMinutes=90")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(90)
    }

    @Test
    fun `should delete user program preferences`() {
        // First create user program preferences using helper
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3, 60)

        // Then delete them
        webTestClient.delete()
            .uri("/user_program_preferences/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should handle multiple users with different preferences`() {
        // Create different preferences for each user using helpers
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId1, 3, 60)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId2, 4, 45)

        // Verify each user has their own preferences
        webTestClient.get()
            .uri("/user_program_preferences/$userId1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId1)
            .jsonPath("$.program_days_per_week").isEqualTo(3)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)

        webTestClient.get()
            .uri("/user_program_preferences/$userId2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId2)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(45)
    }
} 