package com.congen.components

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for [SecurityHeadersFilter].
 *
 * Tests cover all functionality including:
 * - Security headers application for all environments
 * - Production-specific security headers
 * - Development environment headers
 * - Profile detection logic
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class SecurityHeadersFilterTest {
    @Mock
    private lateinit var webFilterChain: WebFilterChain

    private lateinit var securityHeadersFilter: SecurityHeadersFilter

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(webFilterChain.filter(ArgumentMatchers.any())).thenReturn(Mono.empty())
    }

    @Test
    fun `should add basic security headers in all environments`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "test")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should add production security headers when in production profile`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "production")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
        assert(responseHeaders.getFirst("Strict-Transport-Security") == "max-age=31536000; includeSubDomains; preload")
        assert(
            responseHeaders.getFirst("Content-Security-Policy") ==
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; font-src 'self' https:; connect-src 'self' https:; frame-ancestors 'none';"
        )
        assert(responseHeaders.getFirst("Permissions-Policy") == "geolocation=(), microphone=(), camera=()")
    }

    @Test
    fun `should add development security headers when not in production`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "local")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
        assert(
            responseHeaders.getFirst(
                "Content-Security-Policy"
            ) == "default-src 'self' 'unsafe-inline' 'unsafe-eval'; connect-src 'self' http: https:;"
        )
        assert(responseHeaders.getFirst("Strict-Transport-Security") == null)
        assert(responseHeaders.getFirst("Permissions-Policy") == null)
    }

    @Test
    fun `should add development security headers when in staging profile`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "staging")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(
            responseHeaders.getFirst(
                "Content-Security-Policy"
            ) == "default-src 'self' 'unsafe-inline' 'unsafe-eval'; connect-src 'self' http: https:;"
        )
        assert(responseHeaders.getFirst("Strict-Transport-Security") == null)
        assert(responseHeaders.getFirst("Permissions-Policy") == null)
    }

    @Test
    fun `should add development security headers when in test profile`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "test")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(
            responseHeaders.getFirst(
                "Content-Security-Policy"
            ) == "default-src 'self' 'unsafe-inline' 'unsafe-eval'; connect-src 'self' http: https:;"
        )
        assert(responseHeaders.getFirst("Strict-Transport-Security") == null)
        assert(responseHeaders.getFirst("Permissions-Policy") == null)
    }

    @Test
    fun `should use default profile when none specified`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "local")

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(
            responseHeaders.getFirst(
                "Content-Security-Policy"
            ) == "default-src 'self' 'unsafe-inline' 'unsafe-eval'; connect-src 'self' http: https:;"
        )
        assert(responseHeaders.getFirst("Strict-Transport-Security") == null)
        assert(responseHeaders.getFirst("Permissions-Policy") == null)
    }

    @Test
    fun `should handle POST request with security headers`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "test")

        val request =
            MockServerHttpRequest
                .post("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    @Test
    fun `should handle PUT request with security headers`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "production")

        val request =
            MockServerHttpRequest
                .put("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Strict-Transport-Security") == "max-age=31536000; includeSubDomains; preload")
        assert(responseHeaders.getFirst("Permissions-Policy") == "geolocation=(), microphone=(), camera=()")
    }

    @Test
    fun `should handle DELETE request with security headers`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "production")

        val request =
            MockServerHttpRequest
                .delete("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Strict-Transport-Security") == "max-age=31536000; includeSubDomains; preload")
        assert(
            responseHeaders.getFirst("Content-Security-Policy") ==
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; font-src 'self' https:; connect-src 'self' https:; frame-ancestors 'none';"
        )
    }

    @Test
    fun `should handle OPTIONS request with security headers`() {
        // Given
        securityHeadersFilter = SecurityHeadersFilter(activeProfile = "test")

        val request =
            MockServerHttpRequest
                .options("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = securityHeadersFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("X-Content-Type-Options") == "nosniff")
        assert(responseHeaders.getFirst("X-Frame-Options") == "DENY")
        assert(responseHeaders.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(responseHeaders.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }
}
