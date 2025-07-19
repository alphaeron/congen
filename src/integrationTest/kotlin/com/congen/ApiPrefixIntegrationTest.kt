package com.congen

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration test for API prefix functionality.
 *
 * This test verifies that all controller endpoints are properly prefixed
 * with `/api/v1/` as configured in the WebConfig.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("integration-test")
class ApiPrefixIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    /**
     * Tests that health endpoint is accessible with API prefix.
     */
    @Test
    fun `health endpoint should be accessible with API prefix`() {
        webTestClient.get()
            .uri("/api/v1/health/")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that user endpoint is accessible with API prefix.
     */
    @Test
    fun `user endpoint should be accessible with API prefix`() {
        webTestClient.get()
            .uri("/api/v1/user/")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that program endpoint is accessible with API prefix.
     */
    @Test
    fun `program endpoint should be accessible with API prefix`() {
        webTestClient.get()
            .uri("/api/v1/program/")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that exercise endpoint is accessible with API prefix.
     */
    @Test
    fun `exercise endpoint should be accessible with API prefix`() {
        webTestClient.get()
            .uri("/api/v1/exercise/")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that endpoints without API prefix return 404.
     */
    @Test
    fun `endpoints without API prefix should return 404`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isNotFound

        webTestClient.get()
            .uri("/user/")
            .exchange()
            .expectStatus().isNotFound

        webTestClient.get()
            .uri("/program/")
            .exchange()
            .expectStatus().isNotFound
    }
}
