package com.congen.components

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Security headers filter for the Congen API.
 *
 * This filter adds security-related HTTP headers to all responses to enhance
 * the security posture of the application. It implements defense-in-depth
 * by adding multiple layers of security headers that protect against various
 * attack vectors.
 *
 * ## Security Headers Applied
 *
 * ### All Environments
 * - **X-Content-Type-Options**: Prevents MIME type sniffing
 * - **X-Frame-Options**: Prevents clickjacking attacks
 * - **X-XSS-Protection**: Enables browser XSS protection
 * - **Referrer-Policy**: Controls referrer information in requests
 *
 * ### Production Only
 * - **Strict-Transport-Security**: Enforces HTTPS connections
 * - **Content-Security-Policy**: Restricts resource loading
 * - **Permissions-Policy**: Controls browser feature access
 *
 * ## Environment-Specific Behavior
 *
 * The filter applies different security policies based on the active profile:
 * - **Production**: Strict security headers with comprehensive protection
 * - **Development**: Relaxed headers to facilitate development and debugging
 *
 * @param activeProfile Active Spring profile for environment detection
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Run before Spring Security's CORS handling
class SecurityHeadersFilter(
    @Value("\${spring.profiles.active:local}")
    private val activeProfile: String,
) : WebFilter {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(SecurityHeadersFilter::class.java)
    }

    /** Whether the application is running in production mode. */
    private val isProduction = activeProfile.contains("production")

    /**
     * Filters responses to add security headers.
     *
     * This method adds security headers to all HTTP responses based on the
     * current environment. The headers provide protection against common
     * web vulnerabilities and enforce security best practices.
     *
     * @param exchange The web exchange containing request and response
     * @param chain The filter chain to continue processing
     * @return Mono<Void> indicating completion of the filter
     */
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val response = exchange.response

        // Security headers for all environments - force override any existing values
        response.headers.set("X-Content-Type-Options", "nosniff")
        response.headers.set("X-Frame-Options", "DENY")
        response.headers.set("X-XSS-Protection", "1; mode=block")
        response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin")

        // Additional security headers for production
        if (isProduction) {
            response.headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
            response.headers.add(
                "Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
                    "font-src 'self' https:; connect-src 'self' https:; frame-ancestors 'none';",
            )
            response.headers.add("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
        } else {
            // Less restrictive for development
            response.headers.add(
                "Content-Security-Policy",
                "default-src 'self' 'unsafe-inline' 'unsafe-eval'; connect-src 'self' http: https:;",
            )
        }

        return chain.filter(exchange)
    }
}
