package com.congen.config

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Vertx configuration for JSON serialization/deserialization.
 *
 * This configuration sets up Vertx's JSON handling with proper modules for:
 * - Kotlin data class support
 * - Java 8 date/time types.
 * - Proper date formatting
 * - Property naming strategy support for @JsonNaming annotations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class VertxConfig {
    /**
     * Configures Vertx's JSON handling to match the application's Jackson configuration.
     *
     * This method sets up the DatabindCodec used by Vertx to ensure consistent
     * JSON serialization/deserialization across the application.
     */
    @PostConstruct
    fun configureVertxJson() {
        // Configure the global DatabindCodec with our custom ObjectMapper settings
        DatabindCodec.mapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

        DatabindCodec.prettyMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    /**
     * Provides a Vertx instance for the application.
     *
     * @return Configured Vertx instance
     */
    @Bean
    fun vertx(): Vertx {
        return Vertx.vertx()
    }
}
