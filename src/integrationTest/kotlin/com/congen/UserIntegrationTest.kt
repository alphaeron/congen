package com.congen

import com.congen.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test

class UserIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should return 422 when user age is 0`() {
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=0&height=175.0&weight=70.0")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 0")
    }

    @Test
    fun `should return 422 when user age is 151`() {
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=151&height=175.0&weight=70.0")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 151")
    }

    @Test
    fun `should return 422 when user height is 0`() {
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=25&height=0&weight=70.0")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User height must be between 0.01 and 300 cm, got: 0")
    }

    @Test
    fun `should return 422 when user weight is 0`() {
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=25&height=175.0&weight=0")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User weight must be between 0.01 and 1000 kg, got: 0")
    }

    @Test
    fun `should accept valid user data`() {
        webTestClient.post()
            .uri("/user/?name=Test%20User&age=25&height=175.0&weight=70.0")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should get user by id`() {
        // First create a user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Integration%20Test%20User&age=30&height=180.0&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Then get the user by id
        webTestClient.get()
            .uri("/user/${userResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userResponse.id)
            .jsonPath("$.name").isEqualTo("Integration Test User")
            .jsonPath("$.age").isEqualTo(30)
    }

    @Test
    fun `should get all users`() {
        webTestClient.get()
            .uri("/user/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
