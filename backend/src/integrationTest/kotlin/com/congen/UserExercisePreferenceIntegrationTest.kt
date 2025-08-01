package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserExercisePreferenceIntegrationTest : BaseIntegrationTest() {
    private var userId1: Int = 0
    private var userId2: Int = 0
    private var userId3: Int = 0
    private var userId4: Int = 0
    private var userId5: Int = 0
    private val exerciseNames = listOf("Bench Press", "Safety Bar Squat", "Deadlift", "Overhead Press")

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        val token = getValidToken("user")
        userId1 = IntegrationTestHelpers.createTestUser(webTestClient, "Test User 1 $unique", token = token)
        userId2 = IntegrationTestHelpers.createTestUser(webTestClient, "Test User 2 $unique", token = token)
        userId3 = IntegrationTestHelpers.createTestUser(webTestClient, "Test User 3 $unique", token = token)
        userId4 = IntegrationTestHelpers.createTestUser(webTestClient, "Test User 4 $unique", token = token)
        userId5 = IntegrationTestHelpers.createTestUser(webTestClient, "Test User 5 $unique", token = token)
        // Exercises already exist in migrations
    }

    @Test
    fun `should get all user exercise preferences`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId1, "Bench Press", false, token = token)
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        val token = getValidToken("user")
        // First create user exercise preference
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId2, "Safety Bar Squat", true, token = token)

        // Then retrieve it - the controller only has GET /{userId} endpoint
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId2")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].user_id").isEqualTo(userId2)
            .jsonPath("$[0].exercise_name").isEqualTo("Safety Bar Squat")
            .jsonPath("$[0].should_avoid").isEqualTo(true)
    }

    @Test
    fun `should delete user exercise preference`() {
        val token = getValidToken("user")
        // First create user exercise preference
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId4, "Overhead Press", false, token = token)

        // Then delete it using query parameters with proper URL encoding
        val encodedExerciseName = java.net.URLEncoder.encode("Overhead Press", "UTF-8")
        webTestClient.delete()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId4&exercise_name=$encodedExerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId4)
            .jsonPath("$.exercise_name").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        val token = getValidToken("user")
        // Add multiple preferences for the same user
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Bench Press", false, token = token)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Safety Bar Squat", true, token = token)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Deadlift", false, token = token)

        // Get all preferences for the user
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId5")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
