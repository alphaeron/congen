package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test

class UserExercisePreferenceIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should save user exercise preference`() {
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=1&exerciseName=Bench Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(1)
            .jsonPath("$.exerciseName").isEqualTo("Bench Press")
            .jsonPath("$.shouldAvoid").isEqualTo(false)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=2&exerciseName=Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk()

        // Then get preferences for the user
        webTestClient.get()
            .uri("/user-exercise-preferences/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].userId").isEqualTo(2)
            .jsonPath("$[0].exerciseName").isEqualTo("Squat")
            .jsonPath("$[0].shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should update user exercise preference`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=3&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        // Then update the preference
        webTestClient.post()
            .uri("/user-exercise-preferences/update?userId=3&exerciseName=Deadlift&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(3)
            .jsonPath("$.exerciseName").isEqualTo("Deadlift")
            .jsonPath("$.shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should delete user exercise preference`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=4&exerciseName=Overhead Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        // Then delete the preference
        webTestClient.delete()
            .uri("/user-exercise-preferences/4/Overhead Press")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(4)
            .jsonPath("$.exerciseName").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        // Add multiple preferences for the same user
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=5&exerciseName=Bench Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=5&exerciseName=Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=5&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        // Get all preferences for the user
        webTestClient.get()
            .uri("/user-exercise-preferences/5")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
