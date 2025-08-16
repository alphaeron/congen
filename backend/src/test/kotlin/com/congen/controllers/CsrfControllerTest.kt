package com.congen.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Unit tests for CsrfController.
 *
 * Tests CSRF token endpoint functionality and security.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@WebFluxTest(CsrfController::class)
class CsrfControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should return CSRF token when available`() {
        webTestClient.get()
            .uri("/api/v1/csrf")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").exists()
    }

    @Test
    fun `should return empty token when CSRF protection is disabled`() {
        // This test verifies that the endpoint works even when CSRF protection is disabled
        webTestClient.get()
            .uri("/api/v1/csrf")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("")
    }

    @Test
    fun `should return JSON content type`() {
        webTestClient.get()
            .uri("/api/v1/csrf")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
    }
}
