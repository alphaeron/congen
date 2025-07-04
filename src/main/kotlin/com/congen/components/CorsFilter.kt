package com.congen.components

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/*
@CrossOrigin(
originPatterns = ["http://localhost:[*]"]
allowCredentials = true,
 */
@Component
class CorsFilter(
    @Value("\${cors.allowed-origins}")
    private val allowedOriginsConfig: String,
    @Value("\${cors.allowed-methods}")
    private val allowedMethodsConfig: String,
    @Value("\${cors.allowed-headers}")
    private val allowedHeadersConfig: String,
    @Value("\${cors.exposed-headers}")
    private val exposedHeadersConfig: String,
    @Value("\${cors.max-age}")
    private val maxAgeConfig: String,
    @Value("\${spring.profiles.active}")
    private val activeProfile: String,
) : WebFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(CorsFilter::class.java)
    }

    // Parse configuration strings into sets
    private val allowedOrigins: Set<String> = allowedOriginsConfig.split(",").map { it.trim() }.toSet()
    private val allowedMethods: Set<String> = allowedMethodsConfig.split(",").map { it.trim() }.toSet()
    private val allowedHeaders: Set<String> = allowedHeadersConfig.split(",").map { it.trim() }.toSet()
    private val exposedHeaders: Set<String> = exposedHeadersConfig.split(",").map { it.trim() }.toSet()
    private val maxAge: String = maxAgeConfig

    // Production security checks
    private val isProduction = activeProfile.contains("prod") || activeProfile.contains("production")

    init {
        logger.info("CORS Filter initialized with profile: {}", activeProfile)
        logger.info("Allowed origins: {}", allowedOrigins)
        logger.info("Production mode: {}", isProduction)

        // Validate configuration in production
        if (isProduction) {
            validateProductionConfig()
        }
    }

    private fun validateProductionConfig() {
        val httpOrigins = allowedOrigins.filter { it.startsWith("http://") }
        if (httpOrigins.isNotEmpty()) {
            logger.warn("Production environment contains HTTP origins: {}", httpOrigins)
        }

        if (allowedOrigins.contains("*")) {
            logger.error("Production environment should not use wildcard origins")
            throw IllegalStateException("Wildcard origins not allowed in production")
        }

        if (allowedOrigins.isEmpty()) {
            logger.error("No allowed origins configured for production")
            throw IllegalStateException("At least one allowed origin must be configured")
        }
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val origin = exchange.request.headers.getFirst("Origin")
        val requestMethod = exchange.request.method
        val requestPath = exchange.request.path.value()
        val userAgent = exchange.request.headers.getFirst("User-Agent") ?: "Unknown"

        try {
            // Validate origin
            if (origin != null && isOriginAllowed(origin)) {
                exchange.response.headers.add("Access-Control-Allow-Origin", origin)
                exchange.response.headers.add("Access-Control-Allow-Credentials", "true")
                logger.debug("CORS request allowed from origin: {} for path: {}", origin, requestPath)
            } else {
                // Log CORS violations with more detail
                if (origin != null) {
                    logger.warn(
                        "CORS violation: Request from disallowed origin '{}' to path '{}' from User-Agent: {}",
                        origin,
                        requestPath,
                        userAgent,
                    )
                } else {
                    logger.debug(
                        "CORS request without origin header to path: {} from User-Agent: {}",
                        requestPath,
                        userAgent,
                    )
                }
                // For requests without origin or from disallowed origins, don't set CORS headers
                return chain.filter(exchange)
            }

            // Set CORS headers
            exchange.response.headers.add("Access-Control-Allow-Methods", allowedMethods.joinToString(", "))
            exchange.response.headers.add("Access-Control-Allow-Headers", allowedHeaders.joinToString(", "))

            if (requestMethod == HttpMethod.OPTIONS) {
                exchange.response.headers.add("Access-Control-Max-Age", maxAge)
                exchange.response.statusCode = HttpStatus.NO_CONTENT
                logger.debug("CORS preflight request handled for path: {}", requestPath)
                return Mono.empty()
            } else {
                exchange.response.headers.add("Access-Control-Expose-Headers", exposedHeaders.joinToString(", "))
                return chain.filter(exchange)
            }
        } catch (e: Exception) {
            logger.error(
                "Error processing CORS request for path: {} from origin: {} with User-Agent: {}",
                requestPath,
                origin,
                userAgent,
                e,
            )
            return chain.filter(exchange)
        }
    }

    private fun isOriginAllowed(origin: String): Boolean {
        // In production, enforce HTTPS for all origins
        if (isProduction && origin.startsWith("http://")) {
            logger.warn("Rejecting HTTP origin in production: {}", origin)
            return false
        }

        return allowedOrigins.contains(origin)
    }
}
