package com.congen

import com.congen.model.User
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.math.BigDecimal
import java.time.Instant

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
        val unique = System.nanoTime()
        // Create test users with unique names using helpers
        userId1 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 1 $unique")
        userId2 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User 2 $unique")
        // Exercises already exist in migrations
    }

    @Test
    fun `should save user one rep max`() {
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Bench Press")

        webTestClient.get()
            .uri("/user_one_rep_max/$userId1/Bench Press")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }

    @Test
    fun `should get user one rep max by user and exercise`() {
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Bench Press", 200.0)

        // Then retrieve it
        webTestClient.get()
            .uri("/user_one_rep_max/$userId1/Bench Press")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId1)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(200.0)
    }

    @Test
    fun `should update user one rep max`() {
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Bench Press")

        // Then update it using PATCH with query parameters
        webTestClient.patch()
            .uri("/user_one_rep_max/?userId=$userId1&exerciseName=Bench Press&oneRepMax=110.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.one_rep_max").isEqualTo(110.0)
    }

    @Test
    fun `should delete user one rep max`() {
        // First save a one rep max
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Bench Press")

        // Then delete it
        webTestClient.delete()
            .uri("/user_one_rep_max/$userId1/Bench Press")
            .exchange()
            .expectStatus().isOk()

        // Verify it's deleted
        webTestClient.get()
            .uri("/user_one_rep_max/$userId1/Bench Press")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return not found when user one rep max not found`() {
        webTestClient.get()
            .uri("/user_one_rep_max/$userId1/NonExistent")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all user one rep maxes`() {
        // Create one rep max records for the existing user
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Bench Press")
        // Create another exercise and one rep max for the same user - use Safety Bar Squat which exists in migrations
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId1, "Safety Bar Squat", 150.0)
        
        webTestClient.get()
            .uri("/user_one_rep_max/$userId1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }
}
