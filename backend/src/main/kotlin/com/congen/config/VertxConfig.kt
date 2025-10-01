package com.congen.config

import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Configuration class for Vert.x instance setup.
 *
 * This configuration class provides a Vert.x instance bean that is configured
 * with custom Jackson object mappers to ensure consistent JSON serialization
 * and deserialization across the application.
 */
@Configuration
class VertxConfig {
    /**
     * Creates and configures a Vert.x instance with custom Jackson object mappers.
     *
     * This method creates a new Vert.x instance and configures both the standard
     * and pretty-print Jackson object mappers with the same configuration used
     * throughout the application to ensure consistent JSON handling.
     *
     * @return A configured Vert.x instance
     */
    @Bean
    @Primary
    fun vertx(): Vertx {
        val vertx = Vertx.vertx()
        val mapper = DatabindCodec.mapper()
        val prettyMapper = DatabindCodec.prettyMapper()

        // Apply the same configuration as JacksonConfig
        JacksonConfig.configureObjectMapper(mapper)
        JacksonConfig.configureObjectMapper(prettyMapper)

        return vertx
    }
}
