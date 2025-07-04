package com.congen

import com.congen.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should return 422 when user age is 0`() {
        val invalidUser =
            User(
                id = 1,
                name = "Test User",
                age = 0, // Invalid value
                height = BigDecimal("175.0"),
                weight = BigDecimal("70.0"),
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidUser))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 0")
    }

    @Test
    fun `should return 422 when user age is 151`() {
        val invalidUser =
            User(
                id = 1,
                name = "Test User",
                age = 151, // Invalid value
                height = BigDecimal("175.0"),
                weight = BigDecimal("70.0"),
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidUser))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 151")
    }

    @Test
    fun `should return 422 when user height is 0`() {
        val invalidUser =
            User(
                id = 1,
                name = "Test User",
                age = 25,
                height = BigDecimal.ZERO, // Invalid value
                weight = BigDecimal("70.0"),
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidUser))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User height must be between 0.01 and 300 cm, got: 0")
    }

    @Test
    fun `should return 422 when user weight is 0`() {
        val invalidUser =
            User(
                id = 1,
                name = "Test User",
                age = 25,
                height = BigDecimal("175.0"),
                weight = BigDecimal.ZERO, // Invalid value
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidUser))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User weight must be between 0.01 and 1000 kg, got: 0")
    }

    @Test
    fun `should accept valid user data`() {
        val validUser =
            User(
                id = 1,
                name = "Test User",
                age = 25, // Valid value
                height = BigDecimal("175.0"), // Valid value
                weight = BigDecimal("70.0"), // Valid value
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validUser))
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should get user by id`() {
        // First create a user
        val user =
            User(
                id = 2,
                name = "Integration Test User",
                age = 30,
                height = BigDecimal("180.0"),
                weight = BigDecimal("75.0"),
            )

        webTestClient.post()
            .uri("/users/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()

        // Then get the user by id
        webTestClient.get()
            .uri("/users/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(2)
            .jsonPath("$.name").isEqualTo("Integration Test User")
            .jsonPath("$.age").isEqualTo(30)
    }

    @Test
    fun `should get all users`() {
        webTestClient.get()
            .uri("/users/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
