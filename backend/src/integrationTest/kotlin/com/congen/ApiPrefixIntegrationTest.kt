package com.congen

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
class ApiPrefixIntegrationTest : BaseIntegrationTest() {
    @Autowired
    protected override lateinit var webTestClient: WebTestClient

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
     * Tests that user endpoint is accessible with API prefix and authentication.
     */
    @Test
    fun `user endpoint should be accessible with API prefix`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/user/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that program endpoint is accessible with API prefix and authentication.
     */
    @Test
    fun `program endpoint should be accessible with API prefix`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/program/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Tests that exercise endpoint is accessible with API prefix and authentication.
     */
    @Test
    fun `exercise endpoint should be accessible with API prefix`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/exercise/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }
}
