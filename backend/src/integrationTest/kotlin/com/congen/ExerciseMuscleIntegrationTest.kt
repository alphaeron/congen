package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExerciseMuscleIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should create exercise muscle relationship`() {
        val token = getValidToken("user")
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueMuscle = "Test Muscle ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for muscle relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the muscle
        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle&description=Test muscle for exercise relationship")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the exercise-muscle relationship
        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exercise_name=$uniqueExercise&muscle_name=$uniqueMuscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.exercise_name").isEqualTo(uniqueExercise)
            .jsonPath("$.muscle_name").isEqualTo(uniqueMuscle)
    }

    @Test
    fun `should get all exercise muscle relationships`() {
        val token = getValidToken("user")
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueMuscle1 = "Test Muscle 1 ${System.nanoTime()}"
        val uniqueMuscle2 = "Test Muscle 2 ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for muscle relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the muscles
        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle1&description=Test muscle 1 for exercise relationship")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle2&description=Test muscle 2 for exercise relationship")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create the relationships
        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exercise_name=$uniqueExercise&muscle_name=$uniqueMuscle1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exercise_name=$uniqueExercise&muscle_name=$uniqueMuscle2")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_muscle/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                assert(length >= 2) { "Expected at least 2 exercise muscle relationships, got $length" }
            }
    }
}
