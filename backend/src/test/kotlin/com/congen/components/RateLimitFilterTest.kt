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
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should rate limit after exceeding IP threshold`() {
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        DdosProtectionTestHelpers.testRateLimiting(rateLimitFilter, request, 100)
    }

    @Test
    fun `should reset IP rate limit after window expires`() {
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        // Make 100 requests to hit the limit
        DdosProtectionTestHelpers.testRateLimiting(rateLimitFilter, request, 100)

        // Create new filter with shorter window for testing
        val shortWindowFilter = RateLimitFilter("test", 100, 0L, 50, "1MB", "https://example.com")

        DdosProtectionTestHelpers.testRequestsWithinLimit(shortWindowFilter, request, 10)
    }

    @Test
    fun `should reject oversized payloads`() {
        DdosProtectionTestHelpers.testPayloadSizeValidation(rateLimitFilter, 1024 * 1024) // 1MB
    }

    @Test
    fun `should allow requests with acceptable payload size`() {
        DdosProtectionTestHelpers.testAcceptablePayloadSize(rateLimitFilter, 512 * 1024) // 512KB
    }

    @Test
    fun `should detect client IP from X-Forwarded-For header`() {
        DdosProtectionTestHelpers.testIpDetection(
            rateLimitFilter,
            listOf("X-Forwarded-For:192.168.1.100, 10.0.0.1" to "192.168.1.100")
        )
    }

    @Test
    fun `should detect client IP from X-Real-IP header`() {
        DdosProtectionTestHelpers.testIpDetection(
            rateLimitFilter,
            listOf("X-Real-IP:192.168.1.200" to "192.168.1.200")
        )
    }

    @Test
    fun `should fallback to remote address when headers not present`() {
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle different IPs separately`() {
        DdosProtectionTestHelpers.testDifferentIpsSeparately(rateLimitFilter)
    }

    @Test
    fun `should handle requests without content length`() {
        val request = MockServerHttpRequest.get("/api/test").build()
        val exchange = MockServerWebExchange.from(request)

        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)

        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @Test
    fun `should handle concurrent requests from same IP`() {
        val request = DdosProtectionTestHelpers.createRequestWithIp("192.168.1.100")

        DdosProtectionTestHelpers.testConcurrentRequests(rateLimitFilter, request, 50)
    }

    @Test
    fun `should rate limit CORS violations`() {
        val maliciousRequest = DdosProtectionTestHelpers.createMaliciousCorsRequest()
        val legitimateRequest = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        DdosProtectionTestHelpers.testCorsViolationRateLimiting(
            rateLimitFilter,
            maliciousRequest,
            legitimateRequest,
            10
        )
    }

    @Test
    fun `should allow legitimate CORS requests`() {
        val request = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        DdosProtectionTestHelpers.testRequestsWithinLimit(rateLimitFilter, request, 20)
    }

    @Test
    fun `should handle mixed CORS and general rate limiting`() {
        val maliciousRequest = DdosProtectionTestHelpers.createMaliciousCorsRequest()
        val legitimateRequest = DdosProtectionTestHelpers.createLegitimateCorsRequest()

        repeat(5) {
            val tempExchange = MockServerWebExchange.from(maliciousRequest)
            DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, tempExchange)
        }

        repeat(5) {
            val tempExchange = MockServerWebExchange.from(legitimateRequest)
            DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, tempExchange)
        }

        val exchange = MockServerWebExchange.from(maliciousRequest)
        DdosProtectionTestHelpers.executeFilterAndVerifyStatus(rateLimitFilter, exchange)
        DdosProtectionTestHelpers.assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `should handle multiple IP detection scenarios`() {
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
        DdosProtectionTestHelpers.testAcceptablePayloadSize(rateLimitFilter, 1024 * 1024) // Exactly 1MB
        DdosProtectionTestHelpers.testPayloadSizeValidation(rateLimitFilter, 1024 * 1024, 1024 * 1024 + 1) // 1MB + 1 byte
    }
}
