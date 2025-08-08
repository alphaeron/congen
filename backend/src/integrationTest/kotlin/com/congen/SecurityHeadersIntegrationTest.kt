package com.congen

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests for security headers functionality.
 *
 * Tests that security headers are properly applied to all responses.
 */
@TestPropertySource(
    properties = [
        "rate.limit.ip.max-requests=10",
        "rate.limit.ip.window-minutes=1",
        "rate.limit.user.max-requests=5",
        "rate.limit.payload.max-size=1KB"
    ]
)
class SecurityHeadersIntegrationTest : BaseIntegrationTest() {
    private fun createWebTestClient() = webTestClient

    @Test
    fun `should apply security headers to health endpoint`() {
        val client = createWebTestClient()

        val response =
            client.get()
                .uri("/api/v1/health/")
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should apply security headers to error responses`() {
        val client = createWebTestClient()

        val response =
            client.get()
                .uri("/api/v1/nonexistent")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should apply security headers to rate limited responses`() {
        val client = createWebTestClient()

        // Make requests to trigger rate limiting (using lower threshold for testing)
        // The rate limit is 50 requests per minute per ip in test configuration
        repeat(10) {
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", "192.168.1.100")
                .exchange()
                .expectStatus().isOk
        }

        val response =
            client.get()
                .uri("/api/v1/health/")
                .header("X-Real-IP", "192.168.1.100")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should apply security headers to payload too large responses`() {
        val client = createWebTestClient()
        val largePayload = "x".repeat(11 * 1024 * 1024) // 11MB payload (exceeds 10MB limit)
        val token = getValidToken("admin") // Get admin token for authenticated endpoint

        val response =
            client.post()
                .uri("/api/v1/exercise/")
                .header("X-Real-IP", "192.168.1.100")
                .header("Authorization", "Bearer $token")
                .bodyValue(largePayload)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should apply security headers to OPTIONS requests`() {
        val client = createWebTestClient()

        val response =
            client.options()
                .uri("/api/v1/health/")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should not apply production headers in test environment`() {
        val client = createWebTestClient()

        val response =
            client.get()
                .uri("/api/v1/health/")
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)

        // Should not have production-specific headers
        val headers = response.responseHeaders
        assert(headers.getFirst("Strict-Transport-Security") == null)
        assert(headers.getFirst("Permissions-Policy") == null)
    }

    @Test
    fun `should apply Content-Security-Policy header`() {
        val client = createWebTestClient()

        val response =
            client.get()
                .uri("/api/v1/health/")
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        val csp = headers.getFirst("Content-Security-Policy")
        assert(csp != null)
        assert(csp!!.contains("default-src"))
    }

    @Test
    fun `should apply security headers to GET requests`() {
        val client = createWebTestClient()

        val response =
            client.get()
                .uri("/api/v1/health/")
                .exchange()
                .expectStatus().is2xxSuccessful
                .returnResult(String::class.java)

        val headers = response.responseHeaders
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }
}
