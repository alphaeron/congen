package com.congen

import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.math.BigDecimal

/**
 * Integration tests for UserOneRepMax functionality.
 *
 * These tests verify the complete flow from HTTP requests to database operations
 * for user one rep max management, including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserOneRepMaxIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create user one rep max successfully`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        // When & Then
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }

    @Test
    fun `should get all one rep max values for user successfully`() {
        // Given
        val userOneRepMax1 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )
        val userOneRepMax2 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Squat",
                oneRepMax = BigDecimal("150.0"),
            )

        // Create test data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax1)
            .exchange()
            .expectStatus().isOk

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax2)
            .exchange()
            .expectStatus().isOk

        // When & Then
        webTestClient.get()
            .uri("/user-one-rep-max/1")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(UserOneRepMax::class.java)
            .hasSize(2)
            .contains(userOneRepMax1, userOneRepMax2)
    }

    @Test
    fun `should get specific one rep max for user and exercise successfully`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        // Create test data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // When & Then
        webTestClient.get()
            .uri("/user-one-rep-max/1/Bench Press")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }

    @Test
    fun `should return 404 when one rep max not found`() {
        // When & Then
        webTestClient.get()
            .uri("/user-one-rep-max/1/Non-existent Exercise")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should update user one rep max successfully`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )
        val updatedUserOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("110.0"),
            )

        // Create test data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // When & Then
        webTestClient.patch()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedUserOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(110.0)
    }

    @Test
    fun `should delete user one rep max successfully`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        // Create test data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // When & Then
        webTestClient.delete()
            .uri("/user-one-rep-max/1/Bench Press")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)

        // Verify deletion
        webTestClient.get()
            .uri("/user-one-rep-max/1/Bench Press")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should handle decimal one rep max values`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("225.5"),
            )

        // When & Then
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Deadlift")
            .jsonPath("$.one_rep_max").isEqualTo(225.5)
    }

    @Test
    fun `should handle special characters in exercise name`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Barbell Bench Press (Incline)",
                oneRepMax = BigDecimal("120.0"),
            )

        // When & Then
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Barbell Bench Press (Incline)")
            .jsonPath("$.one_rep_max").isEqualTo(120.0)
    }

    @Test
    fun `should handle large one rep max values`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Heavy Deadlift",
                oneRepMax = BigDecimal("500.0"),
            )

        // When & Then
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Heavy Deadlift")
            .jsonPath("$.one_rep_max").isEqualTo(500.0)
    }

    @Test
    fun `should handle zero one rep max values`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Push-up",
                oneRepMax = BigDecimal("0.0"),
            )

        // When & Then
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Push-up")
            .jsonPath("$.one_rep_max").isEqualTo(0.0)
    }

    @Test
    fun `should return empty list when user has no one rep max values`() {
        // When & Then
        webTestClient.get()
            .uri("/user-one-rep-max/999")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(UserOneRepMax::class.java)
            .hasSize(0)
    }

    @Test
    fun `should handle URL encoding for exercise names with spaces`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        // Create test data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // When & Then - Test with URL encoded exercise name
        webTestClient.get()
            .uri("/user-one-rep-max/1/Bench%20Press")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }
}
