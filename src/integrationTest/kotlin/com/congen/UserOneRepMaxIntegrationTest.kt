package com.congen

import com.congen.model.User
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
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
    private var userId1: Int = 0
    private var userId2: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()

        // Create test users
        val user1Response =
            webTestClient.post()
                .uri("/user/?name=Test User 1&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val user2Response =
            webTestClient.post()
                .uri("/user/?name=Test User 2&age=30&height=180.0&weight=85.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        userId1 = user1Response.id
        userId2 = user2Response.id
    }

    @Test
    fun `should save user one rep max`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0")
            )

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.userId").isEqualTo(userId1)
            .jsonPath("$.exerciseName").isEqualTo("Bench Press")
            .jsonPath("$.oneRepMax").isEqualTo(100.0)
    }

    @Test
    fun `should get user one rep max by user and exercise`() {
        // First save a one rep max
        val userOneRepMax =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("200.0")
            )

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // Then retrieve it
        webTestClient.get()
            .uri("/user-one-rep-max/user/$userId1/exercise/Deadlift")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.userId").isEqualTo(userId1)
            .jsonPath("$.exerciseName").isEqualTo("Deadlift")
            .jsonPath("$.oneRepMax").isEqualTo(200.0)
    }

    @Test
    fun `should update user one rep max`() {
        // First save a one rep max
        val userOneRepMax =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0")
            )

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // Then update it
        val updatedOneRepMax =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("110.0")
            )

        webTestClient.put()
            .uri("/user-one-rep-max/user/$userId1/exercise/Bench Press")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedOneRepMax)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.oneRepMax").isEqualTo(110.0)
    }

    @Test
    fun `should delete user one rep max`() {
        // First save a one rep max
        val userOneRepMax =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("200.0")
            )

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userOneRepMax)
            .exchange()
            .expectStatus().isOk

        // Then delete it
        webTestClient.delete()
            .uri("/user-one-rep-max/user/$userId1/exercise/Deadlift")
            .exchange()
            .expectStatus().isOk

        // Verify it's deleted
        webTestClient.get()
            .uri("/user-one-rep-max/user/$userId1/exercise/Deadlift")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return not found when user one rep max not found`() {
        webTestClient.get()
            .uri("/user-one-rep-max/user/$userId1/exercise/NonExistent")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should get all one rep maxes for user`() {
        // Save multiple one rep maxes
        val oneRepMax1 =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0")
            )

        val oneRepMax2 =
            UserOneRepMax(
                userId = userId1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("200.0")
            )

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(oneRepMax1)
            .exchange()
            .expectStatus().isOk

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(oneRepMax2)
            .exchange()
            .expectStatus().isOk

        // Get all for user
        webTestClient.get()
            .uri("/user-one-rep-max/user/$userId1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(2)
    }
}
