package com.congen

import com.congen.model.ExerciseRotationHistory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test

class ExerciseRotationHistoryIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create exercise rotation history record`() {
        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=1&exerciseName=Bench%20Press&category=primary")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.category").isEqualTo("primary")
            .jsonPath("$.used_at").isNotEmpty()
    }

    @Test
    fun `should return 422 when category is invalid`() {
        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=1&exerciseName=Bench%20Press&category=invalid")
            .exchange()
            .expectStatus().isEqualTo(422)
    }

    @Test
    fun `should get exercise rotation history by id`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/exercise-rotation-history/?userId=1&exerciseName=Squat&category=secondary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then get the record by id
        webTestClient.get()
            .uri("/exercise-rotation-history/${response.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Squat")
            .jsonPath("$.category").isEqualTo("secondary")
    }

    @Test
    fun `should get exercise rotation history by user id`() {
        // First create some records for a user
        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=2&exerciseName=Bench%20Press&category=primary")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=2&exerciseName=Squat&category=secondary")
            .exchange()
            .expectStatus().isOk()

        // Then get all records for the user
        webTestClient.get()
            .uri("/exercise-rotation-history/user/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].user_id").isEqualTo(2)
    }

    @Test
    fun `should get exercise rotation history by user id and category`() {
        // First create some records for a user with different categories
        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=3&exerciseName=Bench%20Press&category=primary")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=3&exerciseName=Squat&category=secondary")
            .exchange()
            .expectStatus().isOk()

        // Then get records for the user and primary category
        webTestClient.get()
            .uri("/exercise-rotation-history/user/3/category/primary")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].user_id").isEqualTo(3)
            .jsonPath("$[0].category").isEqualTo("primary")
    }

    @Test
    fun `should get all exercise rotation history records`() {
        webTestClient.get()
            .uri("/exercise-rotation-history/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update exercise rotation history`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/exercise-rotation-history/?userId=4&exerciseName=Bench%20Press&category=primary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then update the record
        webTestClient.put()
            .uri("/exercise-rotation-history/${response.id}?userId=4&exerciseName=Bench%20Press&category=secondary")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.category").isEqualTo("secondary")
    }

    @Test
    fun `should delete exercise rotation history by id`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/exercise-rotation-history/?userId=5&exerciseName=Bench%20Press&category=primary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then delete the record
        webTestClient.delete()
            .uri("/exercise-rotation-history/${response.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)

        // Verify the record is deleted
        webTestClient.get()
            .uri("/exercise-rotation-history/${response.id}")
            .exchange()
            .expectStatus().isEqualTo(404)
    }

    @Test
    fun `should delete exercise rotation history by user id`() {
        // First create some records for a user
        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=6&exerciseName=Bench%20Press&category=primary")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/exercise-rotation-history/?userId=6&exerciseName=Squat&category=secondary")
            .exchange()
            .expectStatus().isOk()

        // Then delete all records for the user
        webTestClient.delete()
            .uri("/exercise-rotation-history/user/6")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isEqualTo(2)

        // Verify no records exist for the user
        webTestClient.get()
            .uri("/exercise-rotation-history/user/6")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$").isEmpty()
    }

    @Test
    fun `should return 404 when getting non-existent record`() {
        webTestClient.get()
            .uri("/exercise-rotation-history/999999")
            .exchange()
            .expectStatus().isEqualTo(404)
    }
} 
