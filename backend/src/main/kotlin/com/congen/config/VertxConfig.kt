package com.congen.config

import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
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
class VertxConfig : DisposableBean {
    companion object {
        private val logger = LoggerFactory.getLogger(VertxConfig::class.java)
    }

    private var vertxInstance: Vertx? = null

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
        vertxInstance = vertx
        val mapper = DatabindCodec.mapper()
        val prettyMapper = DatabindCodec.prettyMapper()

        // Apply the same configuration as JacksonConfig
        JacksonConfig.configureObjectMapper(mapper)
        JacksonConfig.configureObjectMapper(prettyMapper)

        return vertx
    }

    /**
     * Closes the Vert.x instance during bean destruction.
     *
     * This ensures the Vert.x instance and its event loop threads are properly
     * shut down during application shutdown, preventing resource leaks.
     */
    override fun destroy() {
        vertxInstance?.let { vertx ->
            try {
                logger.info("Shutting down Vert.x instance...")
                vertx.close()
                logger.info("Vert.x instance shutdown complete")
            } catch (e: Exception) {
                logger.warn("Error during Vert.x instance shutdown", e)
            }
        }
    }
}
