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
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueMuscle = "Test Muscle ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for muscle relationship" +
                    "&movementType=horizontal_push&isUnilateral=false&isUpper=true&isAccessory=false"
            )
            .exchange()
            .expectStatus().isOk()

        // Then create the muscle
        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle&description=Test muscle for exercise relationship")
            .exchange()
            .expectStatus().isOk()

        // Then create the exercise-muscle relationship
        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exerciseName=$uniqueExercise&muscleName=$uniqueMuscle")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.exercise_name").isEqualTo(uniqueExercise)
            .jsonPath("$.muscle_name").isEqualTo(uniqueMuscle)
    }

    @Test
    fun `should get all exercise muscle relationships`() {
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueMuscle1 = "Test Muscle 1 ${System.nanoTime()}"
        val uniqueMuscle2 = "Test Muscle 2 ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for muscle relationship" +
                    "&movementType=horizontal_push&isUnilateral=false&isUpper=true&isAccessory=false"
            )
            .exchange()
            .expectStatus().isOk()

        // Then create the muscles
        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle1&description=Test muscle 1 for exercise relationship")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueMuscle2&description=Test muscle 2 for exercise relationship")
            .exchange()
            .expectStatus().isOk()

        // Create the relationships
        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exerciseName=$uniqueExercise&muscleName=$uniqueMuscle1")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/exercise_muscle/?exerciseName=$uniqueExercise&muscleName=$uniqueMuscle2")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/exercise_muscle/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value<Int> { length ->
                assert(length >= 2) { "Expected at least 2 exercise muscle relationships, got $length" }
            }
    }
}
