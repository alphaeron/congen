package com.congen.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Jackson configuration for JSON serialization/deserialization.
 *
 * This configuration sets up Jackson with proper modules for:
 * - Kotlin data class support
 * - Java 8 date/time types.
 * - Proper date formatting
 * - Property naming strategy support for @JsonNaming annotations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class JacksonConfig {
    /**
     * Configures the primary ObjectMapper for the application.
     *
     * This bean provides a properly configured ObjectMapper that can handle:
     * - Kotlin data classes with proper null handling
     * - Java 8 date/time types.
     * - Snake case property naming strategy
     * - ISO date formatting
     * - Proper handling of @JsonNaming annotations on enums and classes
     *
     * @return Configured ObjectMapper instance
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }
}
