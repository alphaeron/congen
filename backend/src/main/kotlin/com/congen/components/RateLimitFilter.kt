package com.congen.components

import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Comprehensive rate limiting filter for API protection.
 *
 * Implements rate limiting for all API requests including CORS violations to prevent DDoS attacks.
 * Uses Spring Boot built-ins and minimal configuration for maximum simplicity.
 *
 * ## Rate Limiting Features
 * - **IP-based rate limiting**: General API requests per IP
 * - **User-based rate limiting**: Authenticated user requests
 * - **CORS violation rate limiting**: Malicious origin requests
 * - **Payload size validation**: Request size limits
 * - **Automatic cleanup**: Memory leak prevention
 * - **Thread safety**: Concurrent request handling
 *
 * @param activeProfile Active Spring profile for environment detection
 * @param maxRequestsPerIp Maximum requests per IP per window
 * @param rateLimitWindowMinutes Time window for rate limiting
 * @param maxRequestsPerUser Maximum requests per authenticated user
 * @param maxPayloadSize Maximum request payload size
 * @param allowedOriginsConfig Comma-separated allowed origins for CORS
 */
@Component
@Order(2) // Run after SecurityHeadersFilter
class RateLimitFilter(
    @Value("\${spring.profiles.active}")
    private val activeProfile: String,
    @Value("\${rate.limit.ip.max-requests}")
    private val maxRequestsPerIp: Int,
    @Value("\${rate.limit.ip.window-minutes}")
    private val rateLimitWindowMinutes: Long,
    @Value("\${rate.limit.user.max-requests}")
    private val maxRequestsPerUser: Int,
    @Value("\${rate.limit.payload.max-size}")
    private val maxPayloadSize: String,
    @Value("\${cors.allowed-origins}")
    private val allowedOriginsConfig: String,
) : WebFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(RateLimitFilter::class.java)

        // CORS violation rate limiting constants
        private const val MAX_CORS_VIOLATIONS_PER_IP = 10
        private const val CORS_VIOLATION_WINDOW_MINUTES = 5L
    }

    private val maxPayloadSizeBytes = parseSize(maxPayloadSize)
    private val allowedOrigins: Set<String> = allowedOriginsConfig.split(",").map { it.trim() }.toSet()
    private val isProduction = activeProfile.contains("production")

    // General rate limiting maps
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val requestTimestamps = ConcurrentHashMap<String, Long>()
    private val userRequestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val userRequestTimestamps = ConcurrentHashMap<String, Long>()

    // CORS violation rate limiting maps
    private val corsViolationCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val corsViolationTimestamps = ConcurrentHashMap<String, Long>()

    init {
        logger.info("Rate Limit Filter initialized with profile: {}", activeProfile)
        logger.info("IP rate limit: {} requests per {} minutes", maxRequestsPerIp, rateLimitWindowMinutes)
        logger.info("User rate limit: {} requests per {} minutes", maxRequestsPerUser, rateLimitWindowMinutes)
        logger.info("CORS violation limit: {} violations per {} minutes", MAX_CORS_VIOLATIONS_PER_IP, CORS_VIOLATION_WINDOW_MINUTES)
        logger.info("Allowed origins for CORS: {}", allowedOrigins)
        logger.info("Production mode: {}", isProduction)
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val clientIp = getClientIp(exchange)
        val userId = getUserId(exchange)
        val origin = exchange.request.headers.getFirst("Origin")
        val currentTime = System.currentTimeMillis()

        // Clean up old entries for all rate limiting maps
        cleanupOldEntries(currentTime)

        // Check payload size
        val contentLength = exchange.request.headers.contentLength
        if (contentLength != null && contentLength > maxPayloadSizeBytes) {
            logger.warn("Request payload too large from IP: {} ({} bytes)", clientIp, contentLength)
            exchange.response.statusCode = HttpStatus.PAYLOAD_TOO_LARGE
            return exchange.response.setComplete()
        }

        // Check CORS violations first (before general rate limiting)
        if (origin != null && !isAllowedOrigin(origin)) {
            if (isCorsViolationRateLimited(clientIp, currentTime)) {
                logger.warn(
                    "CORS violation rate limit exceeded from IP: {} ({} violations in {} minutes)",
                    clientIp,
                    corsViolationCounts[clientIp]?.get() ?: 0,
                    CORS_VIOLATION_WINDOW_MINUTES
                )
                exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                return exchange.response.setComplete()
            }
        }

        // General IP-based rate limiting
        if (isIpRateLimited(clientIp, currentTime)) {
            logger.warn("IP rate limit exceeded: {} ({} requests)", clientIp, requestCounts[clientIp]?.get() ?: 0)
            exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            return exchange.response.setComplete()
        }

        // User-based rate limiting (if authenticated)
        if (userId != null && isUserRateLimited(userId, currentTime)) {
            logger.warn("User rate limit exceeded: {} ({} requests)", userId, userRequestCounts[userId]?.get() ?: 0)
            exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            return exchange.response.setComplete()
        }

        return chain.filter(exchange)
    }

    private fun cleanupOldEntries(currentTime: Long) {
        val generalWindowStart = currentTime - Duration.ofMinutes(rateLimitWindowMinutes).toMillis()
        val corsWindowStart = currentTime - Duration.ofMinutes(CORS_VIOLATION_WINDOW_MINUTES).toMillis()

        // Clean general rate limiting entries
        cleanupMapEntries(requestTimestamps, requestCounts, generalWindowStart)
        cleanupMapEntries(userRequestTimestamps, userRequestCounts, generalWindowStart)

        // Clean CORS violation entries
        cleanupMapEntries(corsViolationTimestamps, corsViolationCounts, corsWindowStart)
    }

    private fun cleanupMapEntries(
        timestamps: ConcurrentHashMap<String, Long>,
        counts: ConcurrentHashMap<String, AtomicInteger>,
        windowStart: Long
    ) {
        val keysToRemove = mutableListOf<String>()
        for ((key, timestamp) in timestamps) {
            if (timestamp < windowStart) keysToRemove.add(key)
        }
        for (key in keysToRemove) {
            timestamps.remove(key)
            counts.remove(key)
        }
    }

    private fun isCorsViolationRateLimited(
        clientIp: String,
        currentTime: Long
    ): Boolean {
        val windowStart = currentTime - Duration.ofMinutes(CORS_VIOLATION_WINDOW_MINUTES).toMillis()
        val violationCount = corsViolationCounts.computeIfAbsent(clientIp) { AtomicInteger(0) }
        val timestamp = corsViolationTimestamps.computeIfAbsent(clientIp) { currentTime }

        if (timestamp < windowStart) {
            violationCount.set(1)
            corsViolationTimestamps[clientIp] = currentTime
            return false
        } else {
            val count = violationCount.incrementAndGet()
            return count > MAX_CORS_VIOLATIONS_PER_IP
        }
    }

    private fun isIpRateLimited(
        clientIp: String,
        currentTime: Long
    ): Boolean {
        val windowStart = currentTime - Duration.ofMinutes(rateLimitWindowMinutes).toMillis()
        val ipCount = requestCounts.computeIfAbsent(clientIp) { AtomicInteger(0) }
        val ipTimestamp = requestTimestamps.computeIfAbsent(clientIp) { currentTime }

        if (ipTimestamp < windowStart) {
            ipCount.set(1)
            requestTimestamps[clientIp] = currentTime
            return false
        } else {
            val count = ipCount.incrementAndGet()
            return count > maxRequestsPerIp
        }
    }

    private fun isUserRateLimited(
        userId: String,
        currentTime: Long
    ): Boolean {
        val windowStart = currentTime - Duration.ofMinutes(rateLimitWindowMinutes).toMillis()
        val userCount = userRequestCounts.computeIfAbsent(userId) { AtomicInteger(0) }
        val userTimestamp = userRequestTimestamps.computeIfAbsent(userId) { currentTime }

        if (userTimestamp < windowStart) {
            userCount.set(1)
            userRequestTimestamps[userId] = currentTime
            return false
        } else {
            val count = userCount.incrementAndGet()
            return count > maxRequestsPerUser
        }
    }

    private fun getClientIp(exchange: ServerWebExchange): String {
        return exchange.request.headers.getFirst("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: exchange.request.headers.getFirst("X-Real-IP")
            ?: exchange.request.remoteAddress?.address?.hostAddress
            ?: "unknown"
    }

    private fun getUserId(exchange: ServerWebExchange): String? {
        return exchange.getAttribute<SecurityContext>("SPRING_SECURITY_CONTEXT")?.let { context ->
            context.authentication?.name
        }
    }

    private fun isAllowedOrigin(origin: String): Boolean {
        // In production, enforce HTTPS for all origins
        if (isProduction && origin.startsWith("http://")) {
            logger.warn("Rejecting HTTP origin in production: {}", origin)
            return false
        }

        return allowedOrigins.contains(origin)
    }

    private fun parseSize(size: String): Long {
        return when {
            size.endsWith("KB") -> size.removeSuffix("KB").toLong() * 1024
            size.endsWith("MB") -> size.removeSuffix("MB").toLong() * 1024 * 1024
            size.endsWith("GB") -> size.removeSuffix("GB").toLong() * 1024 * 1024 * 1024
            else -> size.toLong()
        }
    }

    /**
     * Resets all rate limiting state for testing purposes.
     * This method should only be used in test environments.
     */
    @VisibleForTesting
    fun resetRateLimitState() {
        requestCounts.clear()
        requestTimestamps.clear()
        userRequestCounts.clear()
        userRequestTimestamps.clear()
        corsViolationCounts.clear()
        corsViolationTimestamps.clear()
        logger.info("Rate limit state reset for testing")
    }
}
