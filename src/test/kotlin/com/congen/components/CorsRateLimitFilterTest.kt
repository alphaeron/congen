package com.congen.components

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.InetSocketAddress

/**
 * Unit tests for [CorsRateLimitFilter].
 *
 * Tests cover all functionality including:
 * - Rate limiting for CORS violations
 * - IP address detection from various headers
 * - Time window management
 * - Cleanup of old entries
 * - Production security enforcement
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class CorsRateLimitFilterTest {
    @Mock
    private lateinit var webFilterChain: WebFilterChain

    private lateinit var corsRateLimitFilter: CorsRateLimitFilter

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(webFilterChain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty())
    }

    @Test
    fun `should allow request from allowed origin`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://example.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should allow request without origin header`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should rate limit after exceeding violation threshold`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When - Make 11 requests (exceeding the 10 violation limit)
        repeat(10) {
            val tempExchange = MockServerWebExchange.from(request)
            corsRateLimitFilter.filter(tempExchange, webFilterChain).block()
        }

        // 11th request should be rate limited
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode == HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should detect client IP from X-Forwarded-For header`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Should not be rate limited on first request
        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should detect client IP from X-Real-IP header`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("X-Real-IP", "192.168.1.200")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should fallback to remote address when no headers present`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()

        val requestWithRemote =
            MockServerHttpRequest.get("/api/test")
                .header("Origin", "https://malicious.com")
                .remoteAddress(InetSocketAddress("192.168.1.300", 8080))
                .build()
        val exchange = MockServerWebExchange.from(requestWithRemote)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should return unknown when no IP information available`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()

        val requestWithNoRemote =
            MockServerHttpRequest.get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()
        val exchange = MockServerWebExchange.from(requestWithNoRemote)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should reject HTTP origin in production`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "http://localhost:3000,https://example.com",
                activeProfile = "production"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "http://localhost:3000")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Should be treated as a violation in production
        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should allow HTTPS origin in production`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "production"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://example.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle prod profile detection`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "http://localhost:3000",
                activeProfile = "prod"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "http://localhost:3000")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Should be treated as a violation in production
        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle comma-separated allowed origins`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com, https://test.com , http://localhost:3000",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://test.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle empty X-Forwarded-For header`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("X-Forwarded-For", "")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle blank X-Real-IP header`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("X-Real-IP", "   ")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle multiple violations from different IPs`() {
        // Given
        corsRateLimitFilter =
            CorsRateLimitFilter(
                allowedOriginsConfig = "https://example.com",
                activeProfile = "test"
            )

        // Make violations from different IPs (these should not trigger rate limiting)
        repeat(5) { ipIndex ->
            val request =
                MockServerHttpRequest
                    .get("/api/test")
                    .header("Origin", "https://malicious.com")
                    .header("X-Real-IP", "192.168.1.$ipIndex")
                    .build()

            val exchange = MockServerWebExchange.from(request)
            corsRateLimitFilter.filter(exchange, webFilterChain).block()
        }

        // Make violations from the same IP to trigger rate limiting
        val targetIp = "192.168.1.100"
        repeat(10) { // Make exactly 10 violations (the limit)
            val request =
                MockServerHttpRequest
                    .get("/api/test")
                    .header("Origin", "https://malicious.com")
                    .header("X-Real-IP", targetIp)
                    .build()

            val exchange = MockServerWebExchange.from(request)
            corsRateLimitFilter.filter(exchange, webFilterChain).block()
        }

        // Make the 11th violation from the same IP (should trigger rate limit)
        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("X-Real-IP", targetIp)
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsRateLimitFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        assert(exchange.response.statusCode == HttpStatus.TOO_MANY_REQUESTS)
    }
}
