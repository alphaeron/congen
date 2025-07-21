package com.congen.components

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rate limiting filter for CORS violations.
 *
 * This filter implements rate limiting specifically for CORS violations to prevent
 * abuse and protect against potential security threats. It tracks CORS violations
 * by IP address and applies rate limiting when violations exceed a threshold.
 *
 * ## Rate Limiting Strategy
 *
 * - **Violation Tracking**: Counts CORS violations per IP address
 * - **Time Window**: Uses a sliding 5-minute window for violation counting
 * - **Threshold**: Maximum 10 violations per IP within the time window
 * - **Response**: Returns HTTP 429 (Too Many Requests) when threshold is exceeded
 *
 * ## Features
 *
 * - **IP Detection**: Supports various IP detection methods (X-Forwarded-For, X-Real-IP)
 * - **Automatic Cleanup**: Removes old violation records to prevent memory leaks
 * - **Concurrent Safety**: Uses thread-safe data structures for tracking
 * - **Detailed Logging**: Logs rate limit violations for monitoring
 * - **Shared Configuration**: Uses the same allowed origins as the CORS filter
 *
 * ## Configuration
 *
 * The filter uses the same configuration as the CORS filter:
 * - `cors.allowed-origins`: Comma-separated list of allowed origins
 * - `MAX_VIOLATIONS_PER_IP`: Maximum violations per IP (default: 10)
 * - `VIOLATION_WINDOW_MINUTES`: Time window for counting violations (default: 5 minutes)
 *
 * @property allowedOriginsConfig Comma-separated allowed origins from configuration
 * @property activeProfile Active Spring profile for environment detection
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class CorsRateLimitFilter(
    @Value("\${cors.allowed-origins}")
    private val allowedOriginsConfig: String,
    @Value("\${spring.profiles.active}")
    private val activeProfile: String,
) : WebFilter {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(CorsRateLimitFilter::class.java)

        /** Maximum number of CORS violations allowed per IP address. */
        private const val MAX_VIOLATIONS_PER_IP = 10

        /** Time window in minutes for counting violations. */
        private const val VIOLATION_WINDOW_MINUTES = 5L
    }

    /** Map tracking violation counts per IP address. */
    private val violationCounts = ConcurrentHashMap<String, AtomicInteger>()

    /** Map tracking violation timestamps per IP address. */
    private val violationTimestamps = ConcurrentHashMap<String, Long>()

    /** Set of allowed origins parsed from configuration. */
    private val allowedOrigins: Set<String> = allowedOriginsConfig.split(",").map { it.trim() }.toSet()

    /** Whether the application is running in production mode. */
    private val isProduction = activeProfile.contains("prod") || activeProfile.contains("production")

    init {
        logger.info("CORS Rate Limit Filter initialized with profile: {}", activeProfile)
        logger.info("Allowed origins for rate limiting: {}", allowedOrigins)
        logger.info("Production mode: {}", isProduction)
    }

    /**
     * Filters requests to apply rate limiting for CORS violations.
     *
     * This method checks if the request is a CORS violation and applies rate
     * limiting if the IP address has exceeded the violation threshold within
     * the time window.
     *
     * @param exchange The web exchange containing request and response
     * @param chain The filter chain to continue processing
     * @return Mono<Void> indicating completion of the filter
     */
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val origin = exchange.request.headers.getFirst("Origin")
        val clientIp = getClientIp(exchange)

        // Only apply rate limiting to CORS violations
        if (origin != null && !isAllowedOrigin(origin)) {
            val currentTime = System.currentTimeMillis()
            val windowStart = currentTime - Duration.ofMinutes(VIOLATION_WINDOW_MINUTES).toMillis()

            // Clean up old entries
            cleanupOldEntries(windowStart)

            // Check rate limit
            val violationCount = violationCounts.computeIfAbsent(clientIp) { AtomicInteger(0) }
            val timestamp = violationTimestamps.computeIfAbsent(clientIp) { currentTime }

            if (timestamp < windowStart) {
                // Reset counter for new window
                violationCount.set(1)
                violationTimestamps[clientIp] = currentTime
            } else {
                val count = violationCount.incrementAndGet()
                if (count > MAX_VIOLATIONS_PER_IP) {
                    logger.warn(
                        "Rate limit exceeded for CORS violations from IP: {} ({} violations in {} minutes)",
                        clientIp,
                        count,
                        VIOLATION_WINDOW_MINUTES,
                    )
                    exchange.response.statusCode = org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
                    return Mono.empty()
                }
            }
        }

        return chain.filter(exchange)
    }

    /**
     * Cleans up old violation records to prevent memory leaks.
     *
     * This method removes violation records that are older than the current
     * time window, ensuring that the tracking maps don't grow indefinitely.
     *
     * @param windowStart The start time of the current window in milliseconds
     */
    private fun cleanupOldEntries(windowStart: Long) {
        // Clean up old timestamps
        val keysToRemove = mutableListOf<String>()
        for ((key, timestamp) in violationTimestamps) {
            if (timestamp < windowStart) {
                keysToRemove.add(key)
            }
        }

        // Remove old entries
        for (key in keysToRemove) {
            violationTimestamps.remove(key)
            violationCounts.remove(key)
        }
    }

    /**
     * Extracts the client IP address from the request.
     *
     * This method checks various headers to determine the real client IP address,
     * supporting proxy scenarios where the request may be forwarded through
     * load balancers or reverse proxies.
     *
     * @param exchange The web exchange containing the request
     * @return The client IP address as a string
     */
    private fun getClientIp(exchange: ServerWebExchange): String {
        // Check for forwarded headers first (for proxy scenarios)
        val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
        if (!forwardedFor.isNullOrBlank()) {
            return forwardedFor.split(",").first().trim()
        }

        val realIp = exchange.request.headers.getFirst("X-Real-IP")
        if (!realIp.isNullOrBlank()) {
            return realIp
        }

        // Fallback to remote address
        return exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    /**
     * Determines if an origin is allowed based on configuration and security rules.
     *
     * This method checks if the provided origin is in the allowed origins list
     * and enforces additional security rules for production environments.
     * The logic matches the CORS filter to ensure consistency.
     *
     * @param origin The origin to check
     * @return true if the origin is allowed, false otherwise
     */
    private fun isAllowedOrigin(origin: String): Boolean {
        // In production, enforce HTTPS for all origins
        if (isProduction && origin.startsWith("http://")) {
            logger.warn("Rejecting HTTP origin in production: {}", origin)
            return false
        }

        return allowedOrigins.contains(origin)
    }
}
