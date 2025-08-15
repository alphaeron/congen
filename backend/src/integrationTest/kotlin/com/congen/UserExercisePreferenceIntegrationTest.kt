package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserExercisePreferenceIntegrationTest : BaseIntegrationTest() {
    private var userId1: String = ""
    private var userId2: String = ""
    private var userId3: String = ""
    private var userId4: String = ""
    private var userId5: String = ""
    private var token1: String = ""
    private var token2: String = ""
    private var token3: String = ""
    private var token4: String = ""
    private var token5: String = ""

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        // Create unique tokens for each user to avoid keycloak ID conflicts
        token1 = getValidToken("user")
        token2 = getValidToken("user")
        token3 = getValidToken("user")
        token4 = getValidToken("user")
        token5 = getValidToken("user")

        userId1 = IntegrationTestHelpers.createTestUser(webTestClient, token = token1)
        userId2 = IntegrationTestHelpers.createTestUser(webTestClient, token = token2)
        userId3 = IntegrationTestHelpers.createTestUser(webTestClient, token = token3)
        userId4 = IntegrationTestHelpers.createTestUser(webTestClient, token = token4)
        userId5 = IntegrationTestHelpers.createTestUser(webTestClient, token = token5)
        // Exercises already exist in migrations
    }

    @Test
    fun `should get all user exercise preferences`() {
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId1, "Bench Press", false, token = token1)
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId1")
            .header("Authorization", "Bearer $token1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        // First create user exercise preference
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId2, "Safety Bar Squat", true, token = token2)

        // Then retrieve it - the controller only has GET /{userId} endpoint
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId2")
            .header("Authorization", "Bearer $token2")
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
        // First create user exercise preference
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId4, "Overhead Press", false, token = token4)

        // Then delete it using query parameters with proper URL encoding
        val encodedExerciseName = java.net.URLEncoder.encode("Overhead Press", "UTF-8")
        webTestClient.delete()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId4&exercise_name=$encodedExerciseName")
            .header("Authorization", "Bearer $token4")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId4)
            .jsonPath("$.exercise_name").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        // Add multiple preferences for the same user
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Bench Press", false, token = token5)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Safety Bar Squat", true, token = token5)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Deadlift", false, token = token5)

        // Get all preferences for the user
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId5")
            .header("Authorization", "Bearer $token5")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
