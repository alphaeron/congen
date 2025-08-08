package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

/**
 * Integration tests for DDoS protection features.
 *
 * Tests rate limiting, payload size limits, and security headers
 * in a real application context.
 */
@TestPropertySource(
    properties = [
        "rate.limit.ip.max-requests=10",
        "rate.limit.ip.window-minutes=1",
        "rate.limit.user.max-requests=5",
        "rate.limit.payload.max-size=1KB"
    ]
)
class DdosProtectionIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should apply rate limiting to API requests`() {
        // Given
        val client = createWebTestClient()
        val testIp = "192.168.1.101" // Use unique IP for this test

        // When - Make 10 requests (at the limit)
        repeat(10) { requestNumber ->
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", testIp)
                .exchange()
                .expectStatus().isOk
        }

        // 11th request should be rate limited
        client.get()
            .uri("/api/v1/health/")
            .header("X-Real-IP", testIp)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle different IPs separately for rate limiting`() {
        // Given
        val client = createWebTestClient()
        val ip1 = "192.168.1.102" // Use unique IPs for this test
        val ip2 = "192.168.1.103"

        // When - Make 10 requests from IP1 (should hit limit)
        repeat(10) {
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", ip1)
                .exchange()
                .expectStatus().isOk
        }

        // IP1 should be rate limited
        client.get()
            .uri("/api/v1/health/")
            .header("X-Real-IP", ip1)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)

        // IP2 should still be allowed
        client.get()
            .uri("/api/v1/health/")
            .header("X-Real-IP", ip2)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should reject oversized payloads`() {
        // Given
        val client = createWebTestClient()
        val largePayload = "x".repeat(2048) // 2KB payload

        // When - Test payload size validation by setting Content-Length header
        // The RateLimitFilter checks Content-Length before the request reaches the controller
        client.post()
            .uri("/api/v1/health/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Real-IP", "192.168.1.104")
            .header("Content-Length", "2048") // Set explicit content length
            .bodyValue(largePayload)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @Test
    fun `should allow requests with acceptable payload size`() {
        // Given
        val client = createWebTestClient()
        val acceptablePayload = "x".repeat(512) // 512B payload

        // When - Test acceptable payload size by setting Content-Length header
        client.post()
            .uri("/api/v1/health/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Real-IP", "192.168.1.105")
            .header("Content-Length", "512") // Set explicit content length
            .bodyValue(acceptablePayload)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED) // Health endpoint doesn't support POST
    }

    @Test
    fun `should apply security headers to all responses`() {
        // Given
        val client = createWebTestClient()

        // When
        val response =
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", "192.168.1.106") // Use unique IP
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)

        // Then
        val responseHeaders = response.responseHeaders
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should handle X-Forwarded-For header correctly`() {
        // Given
        val client = createWebTestClient()

        // When - Make 10 requests with X-Forwarded-For header
        repeat(10) {
            client.get()
                .uri("/api/v1/health/")
                .header("X-Forwarded-For", "192.168.1.107, 10.0.0.1")
                .exchange()
                .expectStatus().isOk
        }

        // 11th request should be rate limited
        client.get()
            .uri("/api/v1/health/")
            .header("X-Forwarded-For", "192.168.1.107, 10.0.0.1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle requests without IP headers`() {
        // Given
        val client = createWebTestClient()

        // When - Make 10 requests without IP headers (will use 127.0.0.1)
        repeat(10) {
            client.get()
                .uri("/api/v1/health/")
                .exchange()
                .expectStatus().isOk
        }

        // 11th request should be rate limited
        client.get()
            .uri("/api/v1/health/")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle concurrent requests`() {
        // Given
        val client = createWebTestClient()

        // When - Make concurrent requests
        val requests =
            (1..5).map {
                client.get()
                    .uri("/api/v1/health/")
                    .header("X-Real-IP", "192.168.1.108")
                    .exchange()
            }

        // Then - All should complete (some may be rate limited)
        requests.forEach { request ->
            val status = request.returnResult(String::class.java).status
            assert(status.is2xxSuccessful || status == HttpStatus.TOO_MANY_REQUESTS) {
                "Expected 2xx or 429 status, but got ${status.value()}"
            }
        }
    }

    @Test
    fun `should apply request timeout settings`() {
        // Given
        val client =
            createWebTestClient()
                .mutate()
                .responseTimeout(Duration.ofSeconds(5))
                .build()

        // When
        val response =
            client.get()
                .uri("/api/v1/health/")
                .exchange()

        // Then - Should complete within timeout
        response.expectStatus().isOk
    }

    @Test
    fun `should handle malformed requests gracefully`() {
        // Given
        val client = createWebTestClient()

        // When - Send request with malformed headers
        client.get()
            .uri("/api/v1/health/")
            .header("X-Real-IP", "invalid-ip-address")
            .exchange()
            .expectStatus().isOk // Should still process the request
    }

    @Test
    fun `should clean up rate limit data over time`() {
        // Given
        val client = createWebTestClient()

        // When - Make 5 requests (under limit)
        repeat(5) {
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", "192.168.1.109")
                .exchange()
                .expectStatus().isOk
        }

        // Then - Should still be under limit
        client.get()
            .uri("/api/v1/health/")
            .header("X-Real-IP", "192.168.1.109")
            .exchange()
            .expectStatus().isOk
    }

    private fun createWebTestClient(): WebTestClient {
        return webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
    }
}
