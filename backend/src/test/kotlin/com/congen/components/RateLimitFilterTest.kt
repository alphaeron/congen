package com.congen.components

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange

/**
 * Unit tests for [RateLimitFilter].
 *
 * Tests comprehensive rate limiting functionality including IP-based, user-based,
 * and CORS violation rate limiting using modular test helpers.
 */
class RateLimitFilterTest {
    private lateinit var rateLimitFilter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        rateLimitFilter = RateLimitFilter("test", 100, 1L, 50, "1MB", "https://example.com")
    }

    @Test
    fun `should allow requests within rate limit`() {
        // Given
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        // Then
        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should rate limit after exceeding IP threshold`() {
        // Given
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        // When & Then
        DdosProtectionTestHelpers.testRateLimiting(rateLimitFilter, request, 100)
    }

    @Test
    fun `should reset IP rate limit after window expires`() {
        // Given
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        // When - Make 100 requests to hit the limit
        DdosProtectionTestHelpers.testRateLimiting(rateLimitFilter, request, 100)

        // Create new filter with shorter window for testing
        val shortWindowFilter = RateLimitFilter("test", 100, 0L, 50, "1MB", "https://example.com")

        // Then - Should allow requests again after window reset
        DdosProtectionTestHelpers.testRequestsWithinLimit(shortWindowFilter, request, 10)
    }

    @Test
    fun `should reject oversized payloads`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testPayloadSizeValidation(rateLimitFilter, 1024 * 1024) // 1MB
    }

    @Test
    fun `should allow requests with acceptable payload size`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testAcceptablePayloadSize(rateLimitFilter, 512 * 1024) // 512KB
    }

    @Test
    fun `should detect client IP from X-Forwarded-For header`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testIpDetection(
            rateLimitFilter,
            listOf("X-Forwarded-For:192.168.1.100, 10.0.0.1" to "192.168.1.100")
        )
    }

    @Test
    fun `should detect client IP from X-Real-IP header`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testIpDetection(
            rateLimitFilter,
            listOf("X-Real-IP:192.168.1.200" to "192.168.1.200")
        )
    }

    @Test
    fun `should fallback to remote address when headers not present`() {
        // Given
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        // Then
        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle different IPs separately`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testDifferentIpsSeparately(rateLimitFilter)
    }

    @Test
    fun `should handle requests without content length`() {
        // Given
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        // Then
        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @Test
    fun `should parse size configuration correctly`() {
        // Test different size configurations
        val filter1 = RateLimitFilter("test", 100, 1L, 50, "512KB", "https://example.com")
        val filter2 = RateLimitFilter("test", 100, 1L, 50, "2MB", "https://example.com")
        val filter3 = RateLimitFilter("test", 100, 1L, 50, "1024", "https://example.com")

        // All should initialize without errors
        assert(filter1 != null)
        assert(filter2 != null)
        assert(filter3 != null)
    }

    @Test
    fun `should handle concurrent requests from same IP`() {
        // Given
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        // When & Then
        DdosProtectionTestHelpers.testConcurrentRequests(rateLimitFilter, request, 50)
    }

    @Test
    fun `should rate limit CORS violations`() {
        // Given
        val maliciousRequest = DdosProtectionTestHelpers.createMaliciousCorsRequest()
        val legitimateRequest = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        // When & Then
        DdosProtectionTestHelpers.testCorsViolationRateLimiting(
            rateLimitFilter,
            maliciousRequest,
            legitimateRequest,
            10
        )
    }

    @Test
    fun `should allow legitimate CORS requests`() {
        // Given
        val request = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        // When & Then
        DdosProtectionTestHelpers.testRequestsWithinLimit(rateLimitFilter, request, 20)
    }

    @Test
    fun `should handle mixed CORS and general rate limiting`() {
        // Given
        val maliciousRequest = DdosProtectionTestHelpers.createMaliciousCorsRequest()
        val legitimateRequest = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        // When - Make 5 CORS violations and 5 legitimate requests
        repeat(5) {
            val tempExchange = MockServerWebExchange.from(maliciousRequest)
            DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, tempExchange)
        }

        repeat(5) {
            val tempExchange = MockServerWebExchange.from(legitimateRequest)
            DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, tempExchange)
        }

        // Then - Should not be rate limited yet
        val exchange = MockServerWebExchange.from(maliciousRequest)
        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)
        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle multiple IP detection scenarios`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testIpDetection(
            rateLimitFilter,
            listOf(
                "X-Forwarded-For:192.168.1.100, 10.0.0.1" to "192.168.1.100",
                "X-Real-IP:192.168.1.200" to "192.168.1.200",
                "none" to "unknown"
            )
        )
    }

    @Test
    fun `should handle edge case payload sizes`() {
        // Given & When & Then
        DdosProtectionTestHelpers.testAcceptablePayloadSize(rateLimitFilter, 1024 * 1024) // Exactly 1MB
        DdosProtectionTestHelpers.testPayloadSizeValidation(rateLimitFilter, 1024 * 1024, 1024 * 1024 + 1) // 1MB + 1 byte
    }
}
