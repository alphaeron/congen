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
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        userToken = getValidToken("user")
        // Create a single test user to avoid keycloak_user_id conflicts
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Exercises already exist in migrations
    }

    @Test
    fun `should create user one rep max when it does not exist`() {
        // Create a one rep max record using PUT (upsert)
        webTestClient.put()
            .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=Bench Press&one_rep_max=100.0&unit=KG")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)

        // Verify it was created
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(100.0)
    }

    @Test
    fun `should update user one rep max when it already exists`() {
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 100.0, "KG", token = userToken)

        // Then update it using PUT (upsert)
        webTestClient.put()
            .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=Bench Press&one_rep_max=150.0&unit=KG")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.one_rep_max").isEqualTo(150.0)

        // Verify it was updated
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.one_rep_max").isEqualTo(150.0)
    }

    @Test
    fun `should get user one rep max by user and exercise`() {
        // First create a one rep max record
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 200.0, "KG", token = userToken)

        // Then retrieve it
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(200.0)
    }

    @Test
    fun `should delete user one rep max`() {
        // First save a one rep max
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 200.0, "KG", token = userToken)

        // Then delete it
        webTestClient.delete()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.one_rep_max").isEqualTo(200.0)

        // Verify it was deleted
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/Bench Press")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(404)
    }

    @Test
    fun `should get all one rep maxes for user`() {
        // First create multiple one rep maxes
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", 100.0, "KG", token = userToken)
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Deadlift", 200.0, "KG", token = userToken)

        // Then get all for the user
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[1].user_id").isEqualTo(userId)
    }
}
