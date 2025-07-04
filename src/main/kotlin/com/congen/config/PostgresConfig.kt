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
        private val logger = LoggerFactory.getLogger(PostgresConfig::class.java)
        private val CONNECTION_POOL_COUNT_READER = 32
        private val CONNECTION_POOL_COUNT_WRITER = 10
    }

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
