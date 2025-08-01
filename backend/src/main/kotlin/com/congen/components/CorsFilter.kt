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

/**
 * Cross-Origin Resource Sharing (CORS) filter for the Congen API.
 *
 * This filter implements CORS functionality to allow controlled access to the API
 * from web applications running on different origins. It provides configurable
 * origin, method, and header restrictions with enhanced security for production
 * environments.
 *
 * ## Features
 *
 * - **Configurable Origins**: Supports multiple allowed origins via configuration
 * - **Method Restrictions**: Controls which HTTP methods are allowed
 * - **Header Management**: Manages allowed and exposed headers
 * - **Production Security**: Enforces HTTPS and validates configuration in production
 * - **Preflight Support**: Handles OPTIONS preflight requests automatically
 * - **Detailed Logging**: Comprehensive logging for debugging and monitoring
 *
 * ## Configuration
 *
 * The filter is configured via application properties:
 * - `cors.allowed-origins`: Comma-separated list of allowed origins
 * - `cors.allowed-methods`: Comma-separated list of allowed HTTP methods
 * - `cors.allowed-headers`: Comma-separated list of allowed request headers
 * - `cors.exposed-headers`: Comma-separated list of exposed response headers
 * - `cors.max-age`: Maximum age for preflight responses
 *
 * ## Production Security
 *
 * In production environments, the filter enforces additional security measures:
 * - Rejects HTTP origins (HTTPS only)
 * - Prohibits wildcard origins
 * - Requires at least one allowed origin
 * - Validates configuration on startup
 *
 * @property allowedOriginsConfig Comma-separated allowed origins from configuration
 * @property allowedMethodsConfig Comma-separated allowed methods from configuration
 * @property allowedHeadersConfig Comma-separated allowed headers from configuration
 * @property exposedHeadersConfig Comma-separated exposed headers from configuration
 * @property maxAgeConfig Maximum age for preflight responses
 * @property activeProfile Active Spring profile
 *
 * @author Congen Development Team
 * @since 1.0.0
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
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(CorsFilter::class.java)
    }

    /** Set of allowed origins parsed from configuration. */
    private val allowedOrigins: Set<String> = allowedOriginsConfig.split(",").map { it.trim() }.toSet()

    /** Set of allowed HTTP methods parsed from configuration. */
    private val allowedMethods: String = allowedMethodsConfig

    /** Set of allowed request headers parsed from configuration. */
    private val allowedHeaders: String = allowedHeadersConfig

    /** Set of exposed response headers parsed from configuration. */
    private val exposedHeaders: String = exposedHeadersConfig

    /** Maximum age for preflight responses. */
    private val maxAge: String = maxAgeConfig

    /** Whether the application is running in production mode. */
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

    /**
     * Validates CORS configuration for production environments.
     *
     * This method enforces security requirements for production deployments:
     * - Warns about HTTP origins (should use HTTPS)
     * - Prohibits wildcard origins for security
     * - Ensures at least one allowed origin is configured
     *
     * @throws IllegalStateException if configuration violates production security requirements
     */
    private fun validateProductionConfig() {
        val httpOrigins = allowedOrigins.filter { it.startsWith("http://") }
        if (httpOrigins.isNotEmpty()) {
            logger.warn("Production environment contains HTTP origins: {}", httpOrigins)
        }

        if (allowedOrigins.contains("*")) {
            logger.error("Production environment should not use wildcard origins")
            throw IllegalStateException("Wildcard origins not allowed in production")
        }

        if (allowedOrigins.isEmpty() || (allowedOrigins.size == 1 && allowedOrigins.first().isEmpty())) {
            logger.error("No allowed origins configured for production")
            throw IllegalStateException("At least one allowed origin must be configured")
        }
    }

    /**
     * Filters incoming requests to apply CORS headers and handle preflight requests.
     *
     * This method processes each request to:
     * - Validate the origin against allowed origins
     * - Set appropriate CORS headers for allowed requests
     * - Handle OPTIONS preflight requests
     * - Log CORS violations and errors
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
                // For requests without origin or from disallowed origins, still set CORS headers for OPTIONS requests
                if (requestMethod == HttpMethod.OPTIONS) {
                    exchange.response.headers.add("Access-Control-Allow-Methods", allowedMethods)
                    exchange.response.headers.add("Access-Control-Allow-Headers", allowedHeaders)
                    exchange.response.headers.add("Access-Control-Max-Age", maxAge)
                    exchange.response.statusCode = HttpStatus.NO_CONTENT
                    logger.debug("CORS preflight request handled for path: {}", requestPath)
                    return Mono.empty()
                }
                return chain.filter(exchange)
            }

            // Set CORS headers
            exchange.response.headers.add("Access-Control-Allow-Methods", allowedMethods)
            exchange.response.headers.add("Access-Control-Allow-Headers", allowedHeaders)

            if (requestMethod == HttpMethod.OPTIONS) {
                exchange.response.headers.add("Access-Control-Max-Age", maxAge)
                exchange.response.statusCode = HttpStatus.NO_CONTENT
                logger.debug("CORS preflight request handled for path: {}", requestPath)
                return Mono.empty()
            } else {
                exchange.response.headers.add("Access-Control-Expose-Headers", exposedHeaders)
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

    /**
     * Determines if an origin is allowed based on configuration and security rules.
     *
     * This method checks if the provided origin is in the allowed origins list
     * and enforces additional security rules for production environments.
     *
     * @param origin The origin to validate
     * @return true if the origin is allowed, false otherwise
     */
    private fun isOriginAllowed(origin: String): Boolean {
        // In production, enforce HTTPS for all origins
        if (isProduction && origin.startsWith("http://")) {
            logger.warn("Rejecting HTTP origin in production: {}", origin)
            return false
        }

        // Handle wildcard origins in non-production environments
        if (allowedOrigins.contains("*") && !isProduction) {
            return true
        }

        return allowedOrigins.contains(origin)
    }
}
