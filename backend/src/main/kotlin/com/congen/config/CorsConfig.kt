package com.congen.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * CORS configuration using Spring's built-in CORS support.
 *
 * This configuration replaces the custom CorsFilter with Spring's standard
 * CORS handling, which integrates better with Spring Security and eliminates
 * the need for duplicate CORS logic.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class CorsConfig(
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
) {

    /**
     * Configures CORS for the entire application using Spring's built-in CORS support.
     *
     * @return CorsConfigurationSource configured with allowed origins, methods, and headers
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // Parse allowed origins from configuration
        val allowedOrigins = allowedOriginsConfig.split(",").map { it.trim() }.toSet()
        configuration.allowedOriginPatterns = allowedOrigins.toList()
        
        // Parse allowed methods from configuration
        val allowedMethods = allowedMethodsConfig.split(",").map { it.trim() }.toList()
        configuration.allowedMethods = allowedMethods
        
        // Parse allowed headers from configuration
        val allowedHeaders = allowedHeadersConfig.split(",").map { it.trim() }.toList()
        configuration.allowedHeaders = allowedHeaders
        
        // Parse exposed headers from configuration
        val exposedHeaders = exposedHeadersConfig.split(",").map { it.trim() }.toList()
        configuration.exposedHeaders = exposedHeaders
        
        // Set max age
        configuration.maxAge = maxAgeConfig.toLong()
        
        // Allow credentials
        configuration.allowCredentials = true
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        
        return source
    }
} 