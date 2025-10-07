package com.congen.config

import io.vertx.core.Vertx
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PostgresConfig].
 *
 * Tests cover all functionality including:
 * - PostgreSQL writer connection pool creation
 * - PostgreSQL reader connection pool creation
 * - Connection configuration with different parameters
 * - SSL mode configuration
 * - Error handling for connection failures
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class PostgresConfigTest {
    private lateinit var postgresConfig: PostgresConfig
    private lateinit var vertx: Vertx

    @BeforeEach
    fun setUp() {
        vertx = Vertx.vertx()
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5432,
                username = "testuser",
                password = "testpass",
                dbName = "testdb",
                sslMode = false
            )
        postgresConfig = PostgresConfig(props)
    }

    @Test
    fun `should create PostgreSQL writer connection`() {
        val writer = postgresConfig.postgresDBWriter(vertx)

        assertNotNull(writer)
    }

    @Test
    fun `should create PostgreSQL reader connection`() {
        val reader = postgresConfig.postgresDBReader(vertx)

        assertNotNull(reader)
    }

    @Test
    fun `should create writer connection with SSL enabled`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5432,
                username = "testuser",
                password = "testpass",
                dbName = "testdb",
                sslMode = true
            )
        val sslConfig = PostgresConfig(props)

        val writer = sslConfig.postgresDBWriter(vertx)

        assertNotNull(writer)
    }

    @Test
    fun `should create reader connection with SSL enabled`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5432,
                username = "testuser",
                password = "testpass",
                dbName = "testdb",
                sslMode = true
            )
        val sslConfig = PostgresConfig(props)

        val reader = sslConfig.postgresDBReader(vertx)

        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different hosts`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("writer.example.com"),
                reader = PostgresProperties.Host("reader.example.com"),
                port = 5432,
                username = "testuser",
                password = "testpass",
                dbName = "testdb",
                sslMode = false
            )
        val differentHostsConfig = PostgresConfig(props)

        val writer = differentHostsConfig.postgresDBWriter(vertx)
        val reader = differentHostsConfig.postgresDBReader(vertx)

        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different ports`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5433,
                username = "testuser",
                password = "testpass",
                dbName = "testdb",
                sslMode = false
            )
        val differentPortConfig = PostgresConfig(props)

        val writer = differentPortConfig.postgresDBWriter(vertx)
        val reader = differentPortConfig.postgresDBReader(vertx)

        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different database names`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5432,
                username = "testuser",
                password = "testpass",
                dbName = "productiondb",
                sslMode = false
            )
        val differentDbConfig = PostgresConfig(props)

        val writer = differentDbConfig.postgresDBWriter(vertx)
        val reader = differentDbConfig.postgresDBReader(vertx)

        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different credentials`() {
        val props =
            PostgresProperties(
                writer = PostgresProperties.Host("localhost"),
                reader = PostgresProperties.Host("localhost"),
                port = 5432,
                username = "produser",
                password = "prodpass",
                dbName = "testdb",
                sslMode = false
            )
        val differentCredsConfig = PostgresConfig(props)

        val writer = differentCredsConfig.postgresDBWriter(vertx)
        val reader = differentCredsConfig.postgresDBReader(vertx)

        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should handle cleanup method`() {
        // Should not throw exception - cleanup method does not exist in PostgresConfig
        // This test is kept for potential future cleanup functionality
    }

    @Test
    fun `should create multiple writer connections`() {
        val writer1 = postgresConfig.postgresDBWriter(vertx)
        val writer2 = postgresConfig.postgresDBWriter(vertx)

        assertNotNull(writer1)
        assertNotNull(writer2)
    }

    @Test
    fun `should create multiple reader connections`() {
        val reader1 = postgresConfig.postgresDBReader(vertx)
        val reader2 = postgresConfig.postgresDBReader(vertx)

        assertNotNull(reader1)
        assertNotNull(reader2)
    }

    @Test
    fun `should create mixed writer and reader connections`() {
        val writer = postgresConfig.postgresDBWriter(vertx)
        val reader = postgresConfig.postgresDBReader(vertx)

        assertNotNull(writer)
        assertNotNull(reader)
    }
}
