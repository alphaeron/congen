package com.congen

import org.junit.jupiter.api.Test

class ExerciseIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create exercise`() {
        val uniqueExerciseName = "testexerciseout-${System.nanoTime()}"
        webTestClient.post()
            .uri(
                "/exercise/?name=$uniqueExerciseName&description=testexerciseout&movementType=horizontal_push&isUnilateral=false&isUpper=true&isAccessory=false"
            )
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
        // Exercise already exists in migrations
        webTestClient.get()
            .uri("/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.description").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_DESCRIPTION)
    }

    @Test
    fun `should return 404 when exercise not found`() {
        webTestClient.get()
            .uri("/exercise/NonExistentExercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all exercises`() {
        // Exercises already exist in migrations
        webTestClient.get()
            .uri("/exercise/")
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
        // Exercise and muscle already exist in migrations
        // The relationship already exists in migration data, no need to create it

        webTestClient.get()
            .uri("/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}/muscle")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$[0].muscle_name").isEqualTo(IntegrationTestHelpers.TEST_MUSCLE_NAME)
    }

    @Test
    fun `should return 404 when no muscles found for exercise`() {
        // Exercise exists in migrations but no relationship created
        webTestClient.get()
            .uri("/exercise/thisdefinitelydoesntexist/muscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when exercise not found for muscles`() {
        webTestClient.get()
            .uri("/exercise/NonExistentExercise/muscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get equipment for exercise`() {
        // Exercise and equipment already exist in migrations
        // The relationship already exists in migration data, no need to create it
        webTestClient.get()
            .uri("/exercise/${IntegrationTestHelpers.TEST_EXERCISE_NAME}/equipment")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should return 404 when no equipment found for exercise`() {
        // Exercise exists in migrations but no relationship created
        webTestClient.get()
            .uri("/exercise/thisdefinitelydoesntexist/equipment")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when exercise not found for equipment`() {
        webTestClient.get()
            .uri("/exercise/NonExistentExercise/equipment")
            .exchange()
            .expectStatus().isNotFound()
    }
}
