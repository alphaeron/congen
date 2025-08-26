package com.congen.config

import io.vertx.core.Vertx
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for PostgreSQL database connections.
 *
 * This data class holds all the configuration properties needed to establish
 * PostgreSQL database connections, including separate configurations for
 * reader and writer connections, authentication details, and SSL settings.
 *
 * @param writer Writer database host configuration
 * @param reader Reader database host configuration
 * @param port Database port number
 * @param username Database username
 * @param password Database password
 * @param dbName Database name
 * @param sslMode Whether SSL is enabled for database connections
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "congen.postgres")
data class PostgresProperties(
    var writer: Host = Host(),
    var reader: Host = Host(),
    var port: Int = 5432,
    var username: String = "",
    var password: String = "",
    var dbName: String = "",
    var sslMode: Boolean = false
) {
    /**
     * Data class representing a database host configuration.
     *
     * @param host The hostname or IP address of the database server.
     */
    data class Host(
        /**
         * The hostname or IP address of the database server.
         */
        var host: String = ""
    )
}

/**
 * Configuration class for PostgreSQL database connections.
 *
 * This class manages the configuration and creation of PostgreSQL database connections
 * for both read and write operations. It uses Vert.x PostgreSQL client for reactive
 * database operations and supports connection pooling with separate configurations
 * for reader and writer connections.
 *
 * The configuration supports SSL connections and includes connection pooling,
 * prepared statement caching, and automatic reconnection capabilities.
 *
 * @param props The properties for configuring PostgreSQL connections.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(PostgresProperties::class)
class PostgresConfig(
    /**
     * The properties for configuring PostgreSQL connections.
     */
    private val props: PostgresProperties
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(PostgresConfig::class.java)

        /** Number of connections in the reader pool. */
        private val CONNECTION_POOL_COUNT_READER = 32

        /** Number of connections in the writer pool. */
        private val CONNECTION_POOL_COUNT_WRITER = 10
    }

    /** Shared Vert.x instance for all database connections. */
    private val vertx: Vertx = Vertx.vertx()

    /**
     * Creates and configures the PostgreSQL writer connection pool.
     *
     * This bean provides a SQL client configured for write operations with
     * a smaller connection pool optimized for write-heavy workloads.
     *
     * @return Configured SqlClient for write operations
     * @throws Exception if the connection cannot be established
     */
    @Bean("postgresDBWriter")
    fun postgresDBWriter(): SqlClient {
        logger.info("Initializing PostgreSQL writer connection on port {}", props.port)
        return try {
            buildSqlClient(props.writer.host, CONNECTION_POOL_COUNT_WRITER)
        } catch (e: Exception) {
            logger.error("Failed to initialize PostgreSQL writer connection", e)
            throw e
        }
    }

    /**
     * Creates and configures the PostgreSQL reader connection pool.
     *
     * This bean provides a SQL client configured for read operations with
     * a larger connection pool optimized for read-heavy workloads.
     *
     * @return Configured SqlClient for read operations
     * @throws Exception if the connection cannot be established
     */
    @Bean("postgresDBReader")
    fun postgresDBReader(): SqlClient {
        logger.info("Initializing PostgreSQL reader connection on port {}", props.port)
        return try {
            buildSqlClient(props.reader.host, CONNECTION_POOL_COUNT_READER)
        } catch (e: Exception) {
            logger.error("Failed to initialize PostgreSQL reader connection", e)
            throw e
        }
    }

    /**
     * Builds a SQL client with the specified configuration.
     *
     * This method creates a PostgreSQL client with connection pooling,
     * prepared statement caching, and automatic reconnection capabilities.
     *
     * @param host Database hostname
     * @param poolSize Number of connections in the pool
     * @return Configured SqlClient instance
     */
    private fun buildSqlClient(
        host: String,
        poolSize: Int,
    ): SqlClient {
        logger.debug("Building SQL client with pool size: {}", poolSize)

        val connectionOptions: PgConnectOptions =
            PgConnectOptions()
                .setPort(props.port)
                .setHost(host)
                .setDatabase(props.dbName)
                .setUser(props.username)
                .setPassword(props.password)
                .setCachePreparedStatements(true)
                .setPipeliningLimit(256)
                .setIdleTimeout(10000)
                .setReconnectAttempts(2)
                .setReconnectInterval(1000)
                .setSsl(props.sslMode)

        val poolOptions: PoolOptions =
            PoolOptions()
                .setMaxSize(poolSize)
                .setMaxLifetime(60000)

        logger.debug("PostgreSQL connection configured - SSL Mode: {}", props.sslMode)

        return PgBuilder
            .client()
            .connectingTo(connectionOptions)
            .with(poolOptions)
            .using(vertx)
            .build()
    }

    /**
     * Cleanup method to properly close the Vert.x instance when the application shuts down.
     */
    @PreDestroy
    fun cleanup() {
        logger.info("Shutting down Vert.x instance")
        vertx.close()
    }
}
