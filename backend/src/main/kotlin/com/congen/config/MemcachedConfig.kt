package com.congen.config

import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.MemcachedClientBuilder
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.aws.AWSElasticCacheClientBuilder
import net.rubyeye.xmemcached.utils.AddrUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.event.ContextClosedEvent
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Configuration properties for Memcached client setup.
 *
 * This data class holds the configuration properties for the Memcached client,
 * including server connection details, performance tuning parameters, and
 * custom scheduler configuration.
 *
 * @property host Memcached server hostname
 * @property port Memcached server port
 * @property connectionPoolSize Number of connections in the pool
 * @property opTimeout Operation timeout in milliseconds
 * @property maxQueuedNoReply Maximum queued no-reply operations
 * @property schedulerThreadPoolSize Number of threads in the custom scheduler pool
 * @property schedulerQueueCapacity Maximum queue capacity for the scheduler
 * @property schedulerThreadNamePrefix Prefix for scheduler thread names
 * @property useElasticache Whether to use AWS ElastiCache client instead of standard Memcached client
 * @property pollConfigIntervalMs Poll interval in milliseconds for AWS ElastiCache configuration updates
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "memcached")
data class MemcachedProperties(
    var host: String = "localhost",
    var port: Int = 11211,
    var connectionPoolSize: Int = 10,
    var opTimeout: Long = 5000,
    var maxQueuedNoReply: Int = 1000,
    var schedulerThreadPoolSize: Int = 15,
    var schedulerQueueCapacity: Int = 1000,
    var schedulerThreadNamePrefix: String = "memcached-scheduler",
    var useElasticache: Boolean = false,
    var pollConfigIntervalMs: Long = 60000
)

/**
 * Configuration for Memcached client setup.
 *
 * This configuration creates and configures the Memcached client for caching
 * DAL query results. It supports both single server and cluster configurations.
 *
 * ## Configuration Properties
 *
 * - `memcached.host`: Memcached server hostname.
 * - `memcached.port`: Memcached server port.
 * - `memcached.connection-pool-size`: Number of connections in the pool.
 * - `memcached.op-timeout`: Operation timeout in milliseconds.
 * - `memcached.max-queued-noreply`: Maximum queued no-reply operations.
 *
 * @property props The properties for configuring Memcached connections.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MemcachedProperties::class)
class MemcachedConfig(
    /**
     * The properties for configuring Memcached connections.
     */
    private val props: MemcachedProperties
) : ApplicationListener<ContextClosedEvent> {
    companion object {
        private val logger = LoggerFactory.getLogger(MemcachedConfig::class.java)
    }

    /**
     * Creates and configures the Memcached client.
     *
     * This method sets up the Memcached client with connection pooling,
     * timeouts, and other performance optimizations. Based on the configuration,
     * it will either create a standard Memcached client or an AWS ElastiCache client.
     *
     * @return Configured Memcached client instance
     * @throws IOException if client creation fails
     */
    @Bean
    @Primary
    fun memcachedClient(): MemcachedClient {
        return if (props.useElasticache) {
            logger.info("Using AWS ElastiCache client configuration")
            createElasticacheClient()
        } else {
            logger.info("Using standard Memcached client configuration")
            createStandardMemcachedClient()
        }
    }

    /**
     * Creates and configures a standard Memcached client.
     *
     * This method sets up a standard Memcached client with connection pooling,
     * timeouts, and other performance optimizations.
     *
     * @return Configured standard Memcached client instance
     * @throws IOException if client creation fails
     */
    private fun createStandardMemcachedClient(): MemcachedClient {
        val serverAddress = "${props.host}:${props.port}"
        logger.info("Initializing standard Memcached client with server: {}", serverAddress)

        val addressList = AddrUtil.getAddresses(serverAddress)
        val builder: MemcachedClientBuilder = XMemcachedClientBuilder(addressList)

        // Configure connection pool
        builder.setConnectionPoolSize(props.connectionPoolSize)

        // Configure timeouts
        builder.setOpTimeout(props.opTimeout)

        // Configure no-reply operations
        builder.setMaxQueuedNoReplyOperations(props.maxQueuedNoReply)

        // Enable failure mode for better reliability
        builder.setFailureMode(true)

        // Enable session healing for reconnection attempts
        builder.setEnableHealSession(true)

        val client = builder.build()

        logger.info("Standard Memcached client initialized successfully")
        return client
    }

    /**
     * Creates and configures an AWS ElastiCache client.
     *
     * This method sets up an AWS ElastiCache client with AWS-specific optimizations
     * including automatic node discovery, failover handling, and AWS-specific
     * connection management.
     *
     * @return Configured AWS ElastiCache client instance
     * @throws IOException if client creation fails
     */
    private fun createElasticacheClient(): MemcachedClient {
        val serverAddress = "${props.host}:${props.port}"
        logger.info("Initializing AWS ElastiCache client with server: {}", serverAddress)

        val addressList = AddrUtil.getAddresses(serverAddress)
        
        // Use AWS ElastiCache client builder for AWS-specific optimizations
        val builder = AWSElasticCacheClientBuilder(addressList)

        // Configure connection pool for ElastiCache
        builder.setConnectionPoolSize(props.connectionPoolSize)

        // Configure timeouts optimized for ElastiCache
        builder.setOpTimeout(props.opTimeout)

        // Configure no-reply operations
        builder.setMaxQueuedNoReplyOperations(props.maxQueuedNoReply)

        // Enable failure mode for better reliability with ElastiCache
        builder.setFailureMode(true)

        // Enable session healing for automatic failover in ElastiCache
        builder.setEnableHealSession(true)

        // Set configuration polling interval for ElastiCache cluster updates
        // Default is 60 seconds, but can be customized for different environments
        builder.setPollConfigIntervalMs(props.pollConfigIntervalMs)
            
        logger.info("AWS ElastiCache configuration polling interval set to {} ms", props.pollConfigIntervalMs)

        // AWS ElastiCache specific optimizations
        // Note: The AWSElasticCacheClient automatically handles:
        // - Node discovery and configuration polling
        // - Automatic failover and load balancing
        // - Cluster configuration updates
        // - Connection management optimized for AWS infrastructure

        val client = builder.build()

        logger.info("AWS ElastiCache client initialized successfully with polling interval: {} ms", props.pollConfigIntervalMs)
        return client
    }

    /**
     * Creates a custom thread factory for Memcached scheduler threads.
     *
     * This factory creates threads with configurable naming and proper
     * daemon status for application lifecycle management.
     *
     * @return ThreadFactory for Memcached scheduler threads
     */
    private fun createMemcachedThreadFactory(): ThreadFactory {
        val threadCounter = AtomicInteger(1)
        return ThreadFactory { runnable ->
            Thread(runnable, "${props.schedulerThreadNamePrefix}-${threadCounter.getAndIncrement()}").apply {
                isDaemon = true
            }
        }
    }

    /**
     * Creates a dedicated scheduler for Memcached operations.
     *
     * This scheduler is optimized for Memcached I/O operations with configurable
     * thread pool size, queue capacity, and thread naming.
     *
     * @return Dedicated scheduler for Memcached operations
     */
    @Bean("memcachedScheduler")
    fun memcachedScheduler(): Scheduler {
        logger.info(
            "Creating dedicated Memcached scheduler with {} threads, queue capacity: {}",
            props.schedulerThreadPoolSize,
            props.schedulerQueueCapacity
        )

        val executorService =
            Executors.newFixedThreadPool(
                props.schedulerThreadPoolSize,
                createMemcachedThreadFactory()
            )

        return Schedulers.fromExecutorService(executorService)
    }

    /**
     * Handles application context shutdown to gracefully close Memcached client.
     * This is called when Spring's application context is closing, which is the proper
     * lifecycle event for cleanup operations.
     */
    override fun onApplicationEvent(event: ContextClosedEvent) {
        try {
            logger.info("Application context closing, shutting down Memcached client...")
            // Get the Memcached client bean and shut it down immediately
            val client = event.applicationContext.getBean(MemcachedClient::class.java)
            client.shutdown()
            logger.info("Memcached client shutdown complete")
        } catch (e: Exception) {
            logger.warn("Error during Memcached client shutdown", e)
        }
    }
}
