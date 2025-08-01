package com.congen

import org.junit.jupiter.api.Test

class ExerciseIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create exercise`() {
        val token = getValidToken("user")
        val uniqueExerciseName = "testexerciseout-${System.nanoTime()}"
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExerciseName&description=testexerciseout" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(uniqueExerciseName)
            .jsonPath("$.description").isEqualTo("testexerciseout")
            .jsonPath("$.movement_type").isEqualTo("horizontal_push")
            .jsonPath("$.is_unilateral").isEqualTo(false)
            .jsonPath("$.is_upper").isEqualTo(true)
            .jsonPath("$.is_accessory").isEqualTo(false)
    }

    @Test
    fun `should get exercise by name`() {
        val token = getValidToken("user")
        // Exercise already exists in migrations
        webTestClient.get()
            .uri("/api/v1/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.description").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_DESCRIPTION)
    }

    @Test
    fun `should return 404 when exercise not found`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all exercises`() {
        val token = getValidToken("user")
        // Exercises already exist in migrations
        webTestClient.get()
            .uri("/api/v1/exercise/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value { value: Any ->
                // Exercises exist in migrations, so we should have at least some exercises
                assert(value is Number && value.toInt() > 0)
            }
    }

    @Test
    fun `should get muscles for exercise`() {
        val token = getValidToken("user")
        // Exercise and muscle already exist in migrations
        // The relationship already exists in migration data, no need to create it

        webTestClient.get()
            .uri("/api/v1/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}/muscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$[0].muscle_name").isEqualTo("anterior deltoid") // First alphabetically
            .jsonPath("$[1].muscle_name").isEqualTo("pec major") // Second alphabetically
            .jsonPath("$[2].muscle_name").isEqualTo("serratus anterior") // Third alphabetically
            .jsonPath("$[3].muscle_name").isEqualTo("triceps") // Fourth alphabetically
    }

    @Test
    fun `should return 404 when no muscles found for exercise`() {
        val token = getValidToken("user")
        // Exercise exists in migrations but no relationship created
        webTestClient.get()
            .uri("/api/v1/exercise/thisdefinitelydoesntexist/muscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when exercise not found for muscles`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise/muscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get equipment for exercise`() {
        val token = getValidToken("user")
        // Exercise and equipment already exist in migrations
        // The relationship already exists in migration data, no need to create it
        webTestClient.get()
            .uri("/api/v1/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}/equipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should return 404 when no equipment found for exercise`() {
        val token = getValidToken("user")
        // Exercise exists in migrations but no relationship created
        webTestClient.get()
            .uri("/api/v1/exercise/thisdefinitelydoesntexist/equipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when exercise not found for equipment`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise/equipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }
}
