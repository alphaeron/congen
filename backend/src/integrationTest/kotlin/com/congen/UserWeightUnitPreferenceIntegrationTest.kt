package com.congen

import com.congen.model.UserWeightUnitPreference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Integration tests for UserWeightUnitPreference endpoints.
 *
 * Tests the CRUD operations for user weight unit preferences,
 * including creating, reading, updating, and deleting preferences.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserWeightUnitPreferenceIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var userId: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
    }

    @Test
    fun `should create weight unit preference`() {
        val token = getValidToken("user")
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$preferredUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userId)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should create weight unit preference with KG`() {
        val token = getValidToken("user")
        val exerciseName = "Deadlift"
        val preferredUnit = "KG"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$preferredUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userId)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should return 422 for invalid weight unit`() {
        val token = getValidToken("user")
        val exerciseName = "Bench Press"
        val invalidUnit = "INVALID"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$invalidUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Invalid weight unit"))
            }
    }

    @Test
    fun `should update existing weight unit preference`() {
        val token = getValidToken("user")
        val exerciseName = "Safety Bar Squat"
        val initialUnit = "LBS"
        val updatedUnit = "KG"

        // Create initial preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$exerciseName&preferred_unit=$initialUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Update preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$exerciseName&preferred_unit=$updatedUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userId)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == updatedUnit)
            }
    }

    @Test
    fun `should get all weight unit preferences for user`() {
        val token = getValidToken("user")
        val exercise1 = "Bench Press"
        val exercise2 = "Deadlift"
        val unit1 = "LBS"
        val unit2 = "KG"

        // Create preferences
        val encodedExercise1 = java.net.URLEncoder.encode(exercise1, "UTF-8")
        val encodedExercise2 = java.net.URLEncoder.encode(exercise2, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExercise1&preferred_unit=$unit1"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExercise2&preferred_unit=$unit2"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Get all preferences
        val preferences =
            webTestClient.get()
                .uri("/api/v1/user_weight_unit_preference/$userId")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserWeightUnitPreference::class.java)
                .returnResult()
                .responseBody!!
        assert(preferences.size == 2)
        assert(preferences.any { it.exerciseName == exercise1 && it.preferredUnit.name == unit1 })
        assert(preferences.any { it.exerciseName == exercise2 && it.preferredUnit.name == unit2 })
    }

    @Test
    fun `should get specific weight unit preference`() {
        val token = getValidToken("user")
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"

        // Create the preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$exerciseName&preferred_unit=$preferredUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Get the specific preference
        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/$userId/$exerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userId)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should return 404 for non-existent weight unit preference`() {
        val token = getValidToken("user")
        val exerciseName = "NonExistentExercise"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/$userId/$encodedExerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete weight unit preference`() {
        val token = getValidToken("user")
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"

        // Create the preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$exerciseName&preferred_unit=$preferredUnit"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Delete the preference
        webTestClient.delete()
            .uri("/api/v1/user_weight_unit_preference/$userId/$exerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Verify it's deleted
        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/$userId/$exerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }
}
