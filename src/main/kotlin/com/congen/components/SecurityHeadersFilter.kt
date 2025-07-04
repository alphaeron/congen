package com.congen.components

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SecurityHeadersFilter(
    @Value("\${spring.profiles.active:local}")
    private val activeProfile: String,
) : WebFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(SecurityHeadersFilter::class.java)
    }

    private val isProduction = activeProfile.contains("prod") || activeProfile.contains("production")

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val response = exchange.response

        // Security headers for all environments
        response.headers.add("X-Content-Type-Options", "nosniff")
        response.headers.add("X-Frame-Options", "DENY")
        response.headers.add("X-XSS-Protection", "1; mode=block")
        response.headers.add("Referrer-Policy", "strict-origin-when-cross-origin")

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
