package com.congen

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
        userToken = getValidToken("user")
        // Create a single test user to avoid keycloak_user_id conflicts
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
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

    @Test
    fun `should handle 3 simultaneous upserts for same user and exercise without duplicate key or crash`() {
        val exerciseName = "Band Pull Aparts"
        val values = listOf(20.0, 22.12, 22.0)
        val maxValue = 22.12
        val executor = Executors.newFixedThreadPool(3)
        val results =
            values.map { value ->
                executor.submit<Int> {
                    val response =
                        webTestClient.put()
                            .uri(
                                "/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=${java.net.URLEncoder.encode(
                                    exerciseName,
                                    Charsets.UTF_8
                                )}&one_rep_max=$value&unit=KG"
                            )
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .returnResult(String::class.java)
                    response.status.value()
                }
            }
        executor.shutdown()
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS)) { "Concurrent requests did not complete in time" }
        results.forEachIndexed { index, future ->
            val status = future.get()
            assertTrue(status in 200..299) {
                "Request ${index + 1} with value ${values[index]} failed with status $status (duplicate key was 23505/500)"
            }
        }
        webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId/exercise/$exerciseName")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.exercise_name").isEqualTo(exerciseName)
            .jsonPath("$.one_rep_max").isEqualTo(maxValue)
    }
}
