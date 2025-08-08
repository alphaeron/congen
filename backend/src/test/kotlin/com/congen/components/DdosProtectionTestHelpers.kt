package com.congen.components

import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Test helpers for DDoS protection components.
 *
 * Provides common utilities for testing rate limiting, security headers,
 * and other DDoS protection features. Reduces code duplication across tests.
 */
object DdosProtectionTestHelpers {
    /**
     * Creates a simple web filter chain for testing.
     */
    fun createTestWebFilterChain(): WebFilterChain {
        return WebFilterChain { exchange -> Mono.empty() }
    }

    /**
     * Creates a mock request with specified IP address.
     */
    fun createRequestWithIp(
        ip: String,
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .get(path)
            .header("X-Real-IP", ip)
            .build()
    }

    /**
     * Creates a mock request with X-Forwarded-For header.
     */
    fun createRequestWithForwardedFor(
        forwardedFor: String,
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .get(path)
            .header("X-Forwarded-For", forwardedFor)
            .build()
    }

    /**
     * Creates a mock request with Origin header.
     */
    fun createRequestWithOrigin(
        origin: String,
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .get(path)
            .header("Origin", origin)
            .build()
    }

    /**
     * Creates a mock request with payload size.
     */
    fun createRequestWithPayloadSize(
        size: Int,
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .post(path)
            .header("Content-Length", size.toString())
            .build()
    }

    /**
     * Creates a mock request with malicious origin for CORS testing.
     */
    fun createMaliciousCorsRequest(
        ip: String = "192.168.1.100",
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .get(path)
            .header("Origin", "https://malicious.com")
            .header("X-Real-IP", ip)
            .build()
    }

    /**
     * Creates a mock request with legitimate origin for CORS testing.
     */
    fun createLegitimateCorsRequest(
        ip: String = "192.168.1.100",
        path: String = "/api/test"
    ): MockServerHttpRequest {
        return MockServerHttpRequest
            .get(path)
            .header("Origin", "https://example.com")
            .header("X-Real-IP", ip)
            .build()
    }

    /**
     * Executes a filter and verifies the response status.
     */
    fun executeFilterAndVerifyStatus(
        filter: WebFilter,
        exchange: MockServerWebExchange,
        expectedStatus: HttpStatus? = null
    ) {
        val result = filter.filter(exchange, createTestWebFilterChain())
        StepVerifier.create(result).verifyComplete()

        if (expectedStatus != null) {
            assert(exchange.response.statusCode == expectedStatus) {
                "Expected status $expectedStatus but got ${exchange.response.statusCode}"
            }
        }
    }

    /**
     * Executes multiple requests to test rate limiting.
     */
    fun executeMultipleRequests(
        filter: WebFilter,
        request: MockServerHttpRequest,
        count: Int,
        expectedStatus: HttpStatus? = null
    ): List<MockServerWebExchange> {
        val exchanges = mutableListOf<MockServerWebExchange>()

        repeat(count) {
            val exchange = MockServerWebExchange.from(request)
            executeFilterAndVerifyStatus(filter, exchange, expectedStatus)
            exchanges.add(exchange)
        }

        return exchanges
    }

    /**
     * Tests rate limiting by making requests until the limit is reached.
     */
    fun testRateLimiting(
        filter: WebFilter,
        request: MockServerHttpRequest,
        limit: Int,
        expectedStatusAfterLimit: HttpStatus = HttpStatus.TOO_MANY_REQUESTS
    ): MockServerWebExchange {
        // Make requests up to the limit
        executeMultipleRequests(filter, request, limit)

        // Make one more request that should be rate limited
        val rateLimitedExchange = MockServerWebExchange.from(request)
        executeFilterAndVerifyStatus(filter, rateLimitedExchange, expectedStatusAfterLimit)

        return rateLimitedExchange
    }

    /**
     * Tests that requests within the limit are allowed.
     */
    fun testRequestsWithinLimit(
        filter: WebFilter,
        request: MockServerHttpRequest,
        count: Int
    ) {
        val exchanges = executeMultipleRequests(filter, request, count)

        exchanges.forEach { exchange ->
            assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
        }
    }

    /**
     * Tests CORS violation rate limiting.
     */
    fun testCorsViolationRateLimiting(
        filter: WebFilter,
        maliciousRequest: MockServerHttpRequest,
        legitimateRequest: MockServerHttpRequest,
        violationLimit: Int = 10
    ) {
        // Test that malicious requests are rate limited
        testRateLimiting(filter, maliciousRequest, violationLimit, HttpStatus.TOO_MANY_REQUESTS)

        // Test that legitimate requests are not rate limited
        testRequestsWithinLimit(filter, legitimateRequest, 20)
    }

    /**
     * Tests that different IPs are handled separately.
     */
    fun testDifferentIpsSeparately(
        filter: WebFilter,
        ip1: String = "192.168.1.100",
        ip2: String = "192.168.1.200",
        limit: Int = 100
    ) {
        val request1 = createRequestWithIp(ip1)
        val request2 = createRequestWithIp(ip2)

        // Make IP1 hit the limit
        testRateLimiting(filter, request1, limit, HttpStatus.TOO_MANY_REQUESTS)

        // IP2 should still be allowed
        testRequestsWithinLimit(filter, request2, 10)
    }

    /**
     * Tests payload size validation.
     */
    fun testPayloadSizeValidation(
        filter: WebFilter,
        maxSizeBytes: Long,
        oversizedBytes: Long = maxSizeBytes + 1024
    ) {
        val oversizedRequest = createRequestWithPayloadSize(oversizedBytes.toInt())
        val exchange = MockServerWebExchange.from(oversizedRequest)

        executeFilterAndVerifyStatus(filter, exchange, HttpStatus.PAYLOAD_TOO_LARGE)
        assertResponseStatus(exchange, HttpStatus.PAYLOAD_TOO_LARGE)
    }

    /**
     * Tests that acceptable payload sizes are allowed.
     */
    fun testAcceptablePayloadSize(
        filter: WebFilter,
        acceptableSizeBytes: Long
    ) {
        val acceptableRequest = createRequestWithPayloadSize(acceptableSizeBytes.toInt())
        val exchange = MockServerWebExchange.from(acceptableRequest)

        executeFilterAndVerifyStatus(filter, exchange)
        assertResponseStatusNot(exchange, HttpStatus.PAYLOAD_TOO_LARGE)
    }

    /**
     * Verifies that security headers are present in the response.
     */
    fun verifySecurityHeaders(exchange: MockServerWebExchange) {
        val headers = exchange.response.headers
        assert(headers.getFirst("X-Content-Type-Options") == "nosniff")
        assert(headers.getFirst("X-Frame-Options") == "DENY")
        assert(headers.getFirst("X-XSS-Protection") == "1; mode=block")
        assert(headers.getFirst("Referrer-Policy") == "strict-origin-when-cross-origin")
    }

    /**
     * Creates a test payload of specified size.
     */
    fun createTestPayload(size: Int): String {
        return "x".repeat(size)
    }

    /**
     * Asserts that a response has the expected status code.
     */
    fun assertResponseStatus(
        exchange: MockServerWebExchange,
        expectedStatus: HttpStatus
    ) {
        assert(exchange.response.statusCode == expectedStatus) {
            "Expected status $expectedStatus but got ${exchange.response.statusCode}"
        }
    }

    /**
     * Asserts that a response does not have the specified status code.
     */
    fun assertResponseStatusNot(
        exchange: MockServerWebExchange,
        unexpectedStatus: HttpStatus
    ) {
        assert(exchange.response.statusCode != unexpectedStatus) {
            "Expected status to not be $unexpectedStatus but got ${exchange.response.statusCode}"
        }
    }

    /**
     * Tests concurrent request handling.
     */
    fun testConcurrentRequests(
        filter: WebFilter,
        request: MockServerHttpRequest,
        // Default concurrent request count
        concurrentCount: Int = 50
    ) {
        val exchanges = (1..concurrentCount).map { MockServerWebExchange.from(request) }

        exchanges.forEach { exchange ->
            executeFilterAndVerifyStatus(filter, exchange)
        }

        exchanges.forEach { exchange ->
            assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
        }
    }

    /**
     * Tests IP detection from various headers.
     */
    fun testIpDetection(
        filter: WebFilter,
        // List of (header, expectedIp) pairs
        testCases: List<Pair<String, String>>
    ) {
        testCases.forEach { (header, expectedIp) ->
            val request =
                when {
                    header.startsWith("X-Forwarded-For:") ->
                        createRequestWithForwardedFor(header.removePrefix("X-Forwarded-For:"))
                    header.startsWith("X-Real-IP:") ->
                        createRequestWithIp(header.removePrefix("X-Real-IP:"))
                    else -> MockServerHttpRequest.get("/api/test").build()
                }

            val exchange = MockServerWebExchange.from(request)
            executeFilterAndVerifyStatus(filter, exchange)
            assertResponseStatusNot(exchange, HttpStatus.TOO_MANY_REQUESTS)
        }
    }
}
