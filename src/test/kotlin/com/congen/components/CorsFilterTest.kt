package com.congen.components

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for [CorsFilter].
 *
 * Tests cover all functionality including:
 * - CORS header application for allowed origins
 * - Preflight request handling
 * - Production configuration validation
 * - Error handling
 * - Security enforcement
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class CorsFilterTest {
    @Mock
    private lateinit var webFilterChain: WebFilterChain

    private lateinit var corsFilter: CorsFilter

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(webFilterChain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty())
    }

    @Test
    fun `should allow CORS request from allowed origin`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "http://localhost:3000,https://example.com",
                allowedMethodsConfig = "GET,POST,PUT,DELETE",
                allowedHeadersConfig = "Content-Type,Authorization",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "http://localhost:3000")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == "http://localhost:3000")
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == "true")
        assert(responseHeaders.getFirst("Access-Control-Allow-Methods") == "GET,POST,PUT,DELETE")
        assert(responseHeaders.getFirst("Access-Control-Allow-Headers") == "Content-Type,Authorization")
        assert(responseHeaders.getFirst("Access-Control-Expose-Headers") == "X-Total-Count")
    }

    @Test
    fun `should handle preflight OPTIONS request`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .options("/api/test")
                .header("Origin", "https://example.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == "https://example.com")
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == "true")
        assert(responseHeaders.getFirst("Access-Control-Allow-Methods") == "GET,POST")
        assert(responseHeaders.getFirst("Access-Control-Allow-Headers") == "Content-Type")
        assert(responseHeaders.getFirst("Access-Control-Max-Age") == "3600")
        assert(exchange.response.statusCode == org.springframework.http.HttpStatus.NO_CONTENT)
    }

    @Test
    fun `should reject CORS request from disallowed origin`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == null)
    }

    @Test
    fun `should handle request without origin header`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == null)
    }

    @Test
    fun `should reject HTTP origin in production`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "http://localhost:3000,https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "production"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "http://localhost:3000")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == null)
    }

    @Test
    fun `should allow HTTPS origin in production`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "production"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://example.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == "https://example.com")
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == "true")
    }

    @Test
    fun `should throw exception for wildcard origins in production`() {
        // When & Then
        assertThrows<IllegalStateException> {
            CorsFilter(
                allowedOriginsConfig = "*",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "production"
            )
        }
    }

    @Test
    fun `should throw exception for empty origins in production`() {
        // When & Then
        assertThrows<IllegalStateException> {
            CorsFilter(
                allowedOriginsConfig = "",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "production"
            )
        }
    }

    @Test
    fun `should allow wildcard origins in non-production`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "*",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://any-origin.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == "https://any-origin.com")
        assert(responseHeaders.getFirst("Access-Control-Allow-Credentials") == "true")
    }

    @Test
    fun `should handle request with user agent header`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .header("User-Agent", "Mozilla/5.0 Test Browser")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
    }

    @Test
    fun `should handle request without user agent header`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://malicious.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
    }

    @Test
    fun `should handle comma-separated configuration values`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "https://example.com, https://test.com , http://localhost:3000",
                allowedMethodsConfig = "GET, POST , PUT,DELETE",
                allowedHeadersConfig = "Content-Type, Authorization , X-Requested-With",
                exposedHeadersConfig = "X-Total-Count, X-Page-Count ",
                maxAgeConfig = "3600",
                activeProfile = "test"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "https://test.com")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == "https://test.com")
        assert(responseHeaders.getFirst("Access-Control-Allow-Methods") == "GET, POST , PUT,DELETE")
        assert(responseHeaders.getFirst("Access-Control-Allow-Headers") == "Content-Type, Authorization , X-Requested-With")
        assert(responseHeaders.getFirst("Access-Control-Expose-Headers") == "X-Total-Count, X-Page-Count ")
    }

    @Test
    fun `should handle prod profile detection`() {
        // Given
        corsFilter =
            CorsFilter(
                allowedOriginsConfig = "http://localhost:3000",
                allowedMethodsConfig = "GET,POST",
                allowedHeadersConfig = "Content-Type",
                exposedHeadersConfig = "X-Total-Count",
                maxAgeConfig = "3600",
                activeProfile = "prod"
            )

        val request =
            MockServerHttpRequest
                .get("/api/test")
                .header("Origin", "http://localhost:3000")
                .build()

        val exchange = MockServerWebExchange.from(request)

        // When
        val result = corsFilter.filter(exchange, webFilterChain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        val responseHeaders = exchange.response.headers
        assert(responseHeaders.getFirst("Access-Control-Allow-Origin") == null)
    }
}
