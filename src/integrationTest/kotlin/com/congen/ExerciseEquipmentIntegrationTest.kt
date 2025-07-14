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
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueEquipment = "Test Equipment ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/exercise/?name=$uniqueExercise&description=Test exercise for equipment relationship" +
                    "&movementType=horizontal_push&isUnilateral=false&isUpper=true&isAccessory=false"
            )
            .exchange()
            .expectStatus().isOk()

        // Then create the equipment
        webTestClient.post()
            .uri("/equipment/?name=$uniqueEquipment&description=Test equipment for exercise relationship")
            .exchange()
            .expectStatus().isOk()

        // Then create the exercise-equipment relationship
        webTestClient.post()
            .uri("/exercise_equipment/?exerciseName=$uniqueExercise&equipmentName=$uniqueEquipment")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.exercise_name").isEqualTo(uniqueExercise)
            .jsonPath("$.equipment_name").isEqualTo(uniqueEquipment)
    }

    @Test
    fun `should get all exercise equipment`() {
        // Use unique names that don't exist in migrations
        val uniqueExercise = "Test Exercise ${System.nanoTime()}"
        val uniqueEquipment = "Test Equipment ${System.nanoTime()}"

        // First create the exercise
        webTestClient.post()
            .uri(
                "/exercise/?name=$uniqueExercise&description=Test exercise for equipment relationship" +
                    "&movementType=horizontal_push&isUnilateral=false&isUpper=true&isAccessory=false"
            )
            .exchange()
            .expectStatus().isOk()

        // Then create the equipment
        webTestClient.post()
            .uri("/equipment/?name=$uniqueEquipment&description=Test equipment for exercise relationship")
            .exchange()
            .expectStatus().isOk()

        // Create the relationship
        webTestClient.post()
            .uri("/exercise_equipment/?exerciseName=$uniqueExercise&equipmentName=$uniqueEquipment")
            .exchange()
            .expectStatus().isOk()

        // Get all exercise equipment relationships
        webTestClient.get()
            .uri("/exercise_equipment/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].exercise_name").exists()
    }
}
