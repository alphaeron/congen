package com.congen

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExceptionHandlingIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should handle validation exception with 422 status`() {
        // Test validation exception by providing invalid user data
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=0&height=175.0&weight=70.0")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 0")
    }

    @Test
    fun `should handle no results found exception with 404 status`() {
        // Test no results found exception by trying to get a non-existent user
        webTestClient.get()
            .uri("/user/999")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$").isEqualTo("Resource not found")
    }

    @Test
    fun `should handle multiple validation errors`() {
        // Test multiple validation errors
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=0&height=0&weight=0")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { errorMessage ->
                errorMessage.contains("User age must be between 1 and 150")
            }
    }

    @Test
    fun `should handle invalid equipment name`() {
        // Test getting non-existent equipment
        webTestClient.get()
            .uri("/equipment/NonExistentEquipment")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid muscle name`() {
        // Test getting non-existent muscle
        webTestClient.get()
            .uri("/muscle/NonExistentMuscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid exercise name`() {
        webTestClient.get()
            .uri("/exercise/NonExistentExercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid program id`() {
        webTestClient.get()
            .uri("/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid workout id`() {
        webTestClient.get()
            .uri("/programmed_workout/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid set scheme id`() {
        webTestClient.get()
            .uri("/set_scheme/999")
            .exchange()
            .expectStatus().isNotFound()
    }
}
