package com.congen

import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

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
        userId1 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 1 $unique")
        userId2 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 2 $unique")
        userId3 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 3 $unique")
        userId4 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 4 $unique")
        userId5 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 5 $unique")
        // Exercises already exist in migrations
    }

    @Test
    fun `should get all user exercise preferences`() {
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId1, "Bench Press", false)
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        // First create user exercise preference
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId2, "Safety Bar Squat", true)

        // Then retrieve it - the controller only has GET /{userId} endpoint
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId2")
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
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId4, "Overhead Press", false)

        // Then delete it using the DELETE / endpoint with request body
        val preferenceToDelete =
            UserExercisePreference(
                userId = userId4,
                exerciseName = "Overhead Press",
                shouldAvoid = false,
                createdAt = java.time.Instant.now()
            )

        webTestClient.method(HttpMethod.DELETE)
            .uri("/api/v1/user_exercise_preference/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(preferenceToDelete)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId4)
            .jsonPath("$.exercise_name").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        // Add multiple preferences for the same user
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Bench Press", false)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Safety Bar Squat", true)
        IntegrationTestHelpers.createTestUserExercisePreference(webTestClient, userId5, "Deadlift", false)

        // Get all preferences for the user
        webTestClient.get()
            .uri("/api/v1/user_exercise_preference/$userId5")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
