package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    private var userId: String = ""

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        val token = getValidToken("service")
        // Create a single test user to avoid keycloak_user_id conflicts
        userId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User $unique", token = token)
        // Exercises already exist in migrations
    }

    @Test
    fun `should create user one rep max when it does not exist`() {
        val token = getValidToken("service")
        // Create a one rep max record using PUT (upsert)
        webTestClient.put()
            .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=Bench Press&one_rep_max=100.0&unit=KG")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)

        // Verify it was created
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }

    @Test
    fun `should update user one rep max when it already exists`() {
        val token = getValidToken("service")
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 100.0, "KG", token = token)

        // Then update it using PUT (upsert)
        webTestClient.put()
            .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=Bench Press&one_rep_max=150.0&unit=KG")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.one_rep_max").isEqualTo(150.0)

        // Verify it was updated
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.one_rep_max").isEqualTo(150.0)
    }

    @Test
    fun `should get user one rep max by user and exercise`() {
        val token = getValidToken("service")
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 200.0, "KG", token = token)

        // Then retrieve it
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(200.0)
    }

    @Test
    fun `should delete user one rep max`() {
        val token = getValidToken("service")
        // First save a one rep max
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 100.0, "KG", token = token)

        // Then delete it
        webTestClient.delete()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)

        // Verify it's deleted
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return not found when user one rep max not found`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/NonExistent")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all user one rep maxes`() {
        val token = getValidToken("service")
        // Create one rep max records for the existing user
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 100.0, "KG", token = token)
        // Create another exercise and one rep max for the same user - use Safety Bar Squat which exists in migrations
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat", 150.0, "KG", token = token)

        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }
}
