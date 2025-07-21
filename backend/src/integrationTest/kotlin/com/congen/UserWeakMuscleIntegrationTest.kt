package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

class UserWeakMuscleIntegrationTest : BaseIntegrationTest() {
    private var userId1: Int = 0
    private var userId2: Int = 0
    private var userId3: Int = 0
    private var userId4: Int = 0
    private val muscleNames = listOf("hamstrings", "glutes", "lats")

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        userId1 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "WeakMuscle User 1 $unique")
        userId2 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "WeakMuscle User 2 $unique")
        userId3 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "WeakMuscle User 3 $unique")
        userId4 = IntegrationTestHelpers.createTestUserWithId(webTestClient, "WeakMuscle User 4 $unique")
    }

    @Test
    fun `should add and retrieve user weak muscle`() {
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId1)
                    .queryParam("muscle_name", muscleNames[0])
                    .build()
            }
            .exchange()
            .expectStatus().isOk

        // Retrieve weak muscles
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].user_id").isEqualTo(userId1)
            .jsonPath("$[0].muscle_name").isEqualTo(muscleNames[0])
    }

    @Test
    fun `should get user weak muscles by user id`() {
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId2)
                    .queryParam("muscle_name", muscleNames[1])
                    .build()
            }
            .exchange()
            .expectStatus().isOk

        // Retrieve
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].user_id").isEqualTo(userId2)
            .jsonPath("$[0].muscle_name").isEqualTo(muscleNames[1])
    }

    @Test
    fun `should delete user weak muscle`() {
        // Add weak muscle
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId3)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .exchange()
            .expectStatus().isOk

        // Delete
        webTestClient.method(HttpMethod.DELETE)
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId3)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId3)
            .jsonPath("$.muscle_name").isEqualTo(muscleNames[2])

        // Should be empty after delete
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId3")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should handle multiple weak muscles for same user`() {
        // Add multiple weak muscles for the same user
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId4)
                    .queryParam("muscle_name", muscleNames[0])
                    .build()
            }
            .exchange()
            .expectStatus().isOk
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId4)
                    .queryParam("muscle_name", muscleNames[1])
                    .build()
            }
            .exchange()
            .expectStatus().isOk
        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/user_weak_muscle/")
                    .queryParam("user_id", userId4)
                    .queryParam("muscle_name", muscleNames[2])
                    .build()
            }
            .exchange()
            .expectStatus().isOk

        // Get all weak muscles for the user
        webTestClient.get()
            .uri("/api/v1/user_weak_muscle/$userId4")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
