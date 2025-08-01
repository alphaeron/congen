package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

class UserWeakMuscleIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0
    private val muscleNames = listOf("hamstrings", "glutes", "lats")

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        val token = getValidToken("user")
        // Create a single test user to avoid keycloak_user_id conflicts
        userId = IntegrationTestHelpers.createTestUser(webTestClient, "WeakMuscle User $unique", token = token)
    }

    @Test
    fun `should add and retrieve user weak muscle`() {
        val token = getValidToken("user")
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[0])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // Retrieve weak muscles
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].muscle_name").isEqualTo(muscleNames[0])
    }

    @Test
    fun `should get user weak muscles by user id`() {
        val token = getValidToken("user")
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[1])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // Retrieve
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].muscle_name").isEqualTo(muscleNames[1])
    }

    @Test
    fun `should delete user weak muscle`() {
        val token = getValidToken("user")
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // Delete
        webTestClient.method(HttpMethod.DELETE)
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.muscle_name").isEqualTo(muscleNames[2])

        // Should be empty after delete
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should handle multiple weak muscles for same user`() {
        val token = getValidToken("user")
        // Add multiple weak muscles for the same user
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[0])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[1])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // Get all weak muscles for the user
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
