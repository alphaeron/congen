package com.congen.config

import io.vertx.core.Vertx
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
 * @property writerHost Hostname for the writer database connection
 * @property readerHost Hostname for the reader database connection
 * @property port Database port number
 * @property usernameV Database username
 * @property passwordV Database password
 * @property dbName Database name
 * @property sslMode Whether SSL is enabled for database connections
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class PostgresConfig(
    @Value("\${congen.postgres.writer.host}") private val writerHost: String,
    @Value("\${congen.postgres.reader.host}") private val readerHost: String,
    @Value("\${congen.postgres.port}") private val port: Int,
    @Value("\${congen.postgres.username}") private val usernameV: String,
    @Value("\${congen.postgres.password}") private val passwordV: String,
    @Value("\${congen.postgres.db-name}") private val dbName: String,
    @Value("\${congen.postgres.ssl-mode}") private val sslMode: Boolean,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(PostgresConfig::class.java)

        /** Number of connections in the reader pool. */
        private val CONNECTION_POOL_COUNT_READER = 32

        /** Number of connections in the writer pool. */
        private val CONNECTION_POOL_COUNT_WRITER = 10
    }

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
        logger.info("Initializing PostgreSQL writer connection on port {}", port)
        return try {
            buildSqlClient(writerHost, CONNECTION_POOL_COUNT_WRITER)
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
        logger.info("Initializing PostgreSQL reader connection on port {}", port)
        return try {
            buildSqlClient(readerHost, CONNECTION_POOL_COUNT_READER)
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
                .setPort(port)
                .setHost(host)
                .setDatabase(dbName)
                .setUser(usernameV)
                .setPassword(passwordV)
                .setCachePreparedStatements(true)
                .setPipeliningLimit(256)
                .setIdleTimeout(10000)
                .setReconnectAttempts(2)
                .setReconnectInterval(1000)
                .setSsl(sslMode) // TODO True?

        val poolOptions: PoolOptions =
            PoolOptions()
                .setMaxSize(poolSize)
                .setMaxLifetime(60000)

        logger.debug("PostgreSQL connection configured - SSL Mode: {}", sslMode)

        return PgBuilder
            .client()
            .connectingTo(connectionOptions)
            .with(poolOptions)
            .using(Vertx.vertx())
            .build()
    }
}
