package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExerciseEquipmentIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should create exercise equipment relationship`() {
        val token = getValidToken("user")
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueEquipment = "Test Equipment ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for equipment relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the equipment
        webTestClient.post()
            .uri("/api/v1/equipment/?name=$uniqueEquipment&description=Test equipment for exercise relationship")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the exercise-equipment relationship
        webTestClient.post()
            .uri("/api/v1/exercise_equipment/?exercise_name=$uniqueExercise&equipment_name=$uniqueEquipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.exercise_name").isEqualTo(uniqueExercise)
            .jsonPath("$.equipment_name").isEqualTo(uniqueEquipment)
    }

    @Test
    fun `should get all exercise equipment`() {
        val token = getValidToken("user")
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueEquipment = "Test Equipment ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/api/v1/exercise/?name=$uniqueExercise&description=Test exercise for equipment relationship" +
                    "&movement_type=horizontal_push&is_unilateral=false&is_upper=true&is_accessory=false"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Then create the equipment
        webTestClient.post()
            .uri("/api/v1/equipment/?name=$uniqueEquipment&description=Test equipment for exercise relationship")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Create the relationship
        webTestClient.post()
            .uri("/api/v1/exercise_equipment/?exercise_name=$uniqueExercise&equipment_name=$uniqueEquipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Get all exercise equipment relationships
        webTestClient.get()
            .uri("/api/v1/exercise_equipment/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").exists()
    }
}
