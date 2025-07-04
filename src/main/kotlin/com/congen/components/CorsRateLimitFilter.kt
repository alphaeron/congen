package com.congen.components

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class CorsRateLimitFilter : WebFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(CorsRateLimitFilter::class.java)
        private const val MAX_VIOLATIONS_PER_IP = 10
        private const val VIOLATION_WINDOW_MINUTES = 5L
    }

    // Track CORS violations by IP address
    private val violationCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val violationTimestamps = ConcurrentHashMap<String, Long>()

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

    private fun isAllowedOrigin(origin: String): Boolean {
        // This should match the logic in CorsFilter
        val allowedOrigins =
            setOf(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://your-production-domain.com",
            )
        return allowedOrigins.contains(origin)
    }
}
