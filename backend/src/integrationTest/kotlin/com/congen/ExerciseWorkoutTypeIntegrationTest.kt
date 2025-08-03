package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExerciseWorkoutTypeIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should create exercise workout type relationship`() {
        val token = getValidToken("service")
        // Use unique exercise name that doesn't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for workout type relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the exercise-workout-type relationship
        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=dynamic_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.exercise_name").isEqualTo(uniqueExercise)
            .jsonPath("$.movement_type").isEqualTo("horizontal_push")
            .jsonPath("$.workout_type").isEqualTo("dynamic_effort")
    }

    @Test
    fun `should get all exercise workout types`() {
        val token = getValidToken("service")
        // Use unique exercise name that doesn't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for workout type relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create the relationships
        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=dynamic_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=maximal_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_workout_type/exercise/$uniqueExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                assert(length >= 2) { "Expected at least 2 exercise workout types, got $length" }
            }
    }

    @Test
    fun `should get workout types by exercise name`() {
        val token = getValidToken("service")
        // Use unique exercise name that doesn't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for workout type relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create the relationships
        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=dynamic_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=maximal_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_workout_type/exercise/$uniqueExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                assert(length >= 2) { "Expected at least 2 exercise workout types, got $length" }
            }
    }

    @Test
    fun `should get workout types by movement type`() {
        val token = getValidToken("service")
        // Use unique exercise name that doesn't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for workout type relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create the relationship
        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=dynamic_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_workout_type/movement_type/horizontal_push")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                // Should have at least 1 relationship for this movement type
                assert(length >= 1) { "Expected at least 1 exercise workout type, got $length" }
            }
    }

    @Test
    fun `should handle multiple workout types for same exercise`() {
        val token = getValidToken("service")
        // Use unique exercise name that doesn't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for workout type relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create multiple relationships for the same exercise
        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=dynamic_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/exercise_workout_type/?exercise_name=$uniqueExercise&movement_type=horizontal_push&workout_type=maximal_effort")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_workout_type/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                // Should have at least 2 relationships for this exercise
                assert(length >= 2) { "Expected at least 2 exercise workout types, got $length" }
            }
    }
}
