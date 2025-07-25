package com.congen

import com.congen.model.User
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
    private lateinit var userResponse: User

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        userResponse =
            webTestClient.post()
                .uri("/api/v1/user/?name=Test%20User%20$unique&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!
    }

    @Test
    fun `should create weight unit preference`() {
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$preferredUnit"
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userResponse.id)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should create weight unit preference with KG`() {
        val exerciseName = "Deadlift"
        val preferredUnit = "KG"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$preferredUnit"
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userResponse.id)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should return 422 for invalid weight unit`() {
        val exerciseName = "Bench Press"
        val invalidUnit = "INVALID"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$encodedExerciseName&preferred_unit=$invalidUnit"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Invalid weight unit"))
            }
    }

    @Test
    fun `should update existing weight unit preference`() {
        val exerciseName = "Safety Bar Squat"
        val initialUnit = "LBS"
        val updatedUnit = "KG"

        // Create initial preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$exerciseName&preferred_unit=$initialUnit"
            )
            .exchange()
            .expectStatus().isOk()

        // Update preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$exerciseName&preferred_unit=$updatedUnit"
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userResponse.id)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == updatedUnit)
            }
    }

    @Test
    fun `should get all weight unit preferences for user`() {
        val exercise1 = "Bench Press"
        val exercise2 = "Deadlift"
        val unit1 = "LBS"
        val unit2 = "KG"

        // Create preferences
        val encodedExercise1 = java.net.URLEncoder.encode(exercise1, "UTF-8")
        val encodedExercise2 = java.net.URLEncoder.encode(exercise2, "UTF-8")

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$encodedExercise1&preferred_unit=$unit1"
            )
            .exchange()
            .expectStatus().isOk()

        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$encodedExercise2&preferred_unit=$unit2"
            )
            .exchange()
            .expectStatus().isOk()

        // Get all preferences
        val preferences =
            webTestClient.get()
                .uri("/api/v1/user_weight_unit_preference/${userResponse.id}")
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
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"

        // Create the preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$exerciseName&preferred_unit=$preferredUnit"
            )
            .exchange()
            .expectStatus().isOk()

        // Get the specific preference
        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/${userResponse.id}/$exerciseName")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserWeightUnitPreference::class.java)
            .value { preference ->
                assert(preference.userId == userResponse.id)
                assert(preference.exerciseName == exerciseName)
                assert(preference.preferredUnit.name == preferredUnit)
            }
    }

    @Test
    fun `should return 404 for non-existent weight unit preference`() {
        val exerciseName = "NonExistentExercise"
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")

        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/${userResponse.id}/$encodedExerciseName")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete weight unit preference`() {
        val exerciseName = "Bench Press"
        val preferredUnit = "LBS"

        // Create the preference
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=${userResponse.id}" +
                    "&exercise_name=$exerciseName&preferred_unit=$preferredUnit"
            )
            .exchange()
            .expectStatus().isOk()

        // Delete the preference
        webTestClient.delete()
            .uri("/api/v1/user_weight_unit_preference/${userResponse.id}/$exerciseName")
            .exchange()
            .expectStatus().isOk()

        // Verify it's deleted
        webTestClient.get()
            .uri("/api/v1/user_weight_unit_preference/${userResponse.id}/$exerciseName")
            .exchange()
            .expectStatus().isNotFound()
    }
}
