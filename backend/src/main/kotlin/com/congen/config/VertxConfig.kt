package com.congen.config

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
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
 * and deserialization across the application. It also configures Vert.x with
 * a custom worker pool to prevent blocking operations from interfering with
 * event loop threads.
 */
@Configuration
class VertxConfig : DisposableBean {
    companion object {
        private val logger = LoggerFactory.getLogger(VertxConfig::class.java)
        
        private const val WORKER_POOL_SIZE = 20
        private const val INTERNAL_BLOCKING_POOL_SIZE = 20
        private const val BLOCKED_THREAD_CHECK_INTERVAL = 5000L
        private const val MAX_EVENT_LOOP_EXECUTE_TIME = 2000000000L
    }

    private var vertxInstance: Vertx? = null

    /**
     * Creates and configures a Vert.x instance with custom Jackson object mappers
     * and optimized worker pools for blocking operations.
     *
     * This method creates a new Vert.x instance with:
     * - Custom Jackson object mappers for consistent JSON handling
     * - Larger worker pool sizes to handle blocking operations (like Jackson serialization)
     * - Increased blocked thread check interval to reduce false positives during warmup
     * - Increased max event loop execute time to allow longer operations
     *
     * Note: Vert.x manages its own thread pools. We configure the sizes but cannot
     * provide custom executors. To prevent blocking operations from interfering with
     * event loop threads, we use publishOn in PostgresClient to move operations to
     * bounded elastic scheduler.
     *
     * @return A configured Vert.x instance
     */
    @Bean
    @Primary
    fun vertx(): Vertx {
        logger.info("Creating Vert.x instance with worker pool size: {}, internal blocking pool size: {}", 
            WORKER_POOL_SIZE, INTERNAL_BLOCKING_POOL_SIZE)
        
        val options = VertxOptions()
            .setWorkerPoolSize(WORKER_POOL_SIZE)
            .setInternalBlockingPoolSize(INTERNAL_BLOCKING_POOL_SIZE)
            .setBlockedThreadCheckInterval(BLOCKED_THREAD_CHECK_INTERVAL)
            .setMaxEventLoopExecuteTime(MAX_EVENT_LOOP_EXECUTE_TIME)
        
        val vertx = Vertx.vertx(options)
        vertxInstance = vertx
        
        val mapper = DatabindCodec.mapper()
        val prettyMapper = DatabindCodec.prettyMapper()

        // Apply the same configuration as JacksonConfig
        JacksonConfig.configureObjectMapper(mapper)
        JacksonConfig.configureObjectMapper(prettyMapper)
        
        logger.info("Vert.x instance created successfully with worker pool size: {}, internal blocking pool size: {}", 
            WORKER_POOL_SIZE, INTERNAL_BLOCKING_POOL_SIZE)

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
