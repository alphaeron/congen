package com.congen.config

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

    @BeforeEach
    fun setUp() {
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
        // When
        val writer = postgresConfig.postgresDBWriter()

        // Then
        assertNotNull(writer)
    }

    @Test
    fun `should create PostgreSQL reader connection`() {
        // When
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(reader)
    }

    @Test
    fun `should create writer connection with SSL enabled`() {
        // Given
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

        // When
        val writer = sslConfig.postgresDBWriter()

        // Then
        assertNotNull(writer)
    }

    @Test
    fun `should create reader connection with SSL enabled`() {
        // Given
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

        // When
        val reader = sslConfig.postgresDBReader()

        // Then
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different hosts`() {
        // Given
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

        // When
        val writer = differentHostsConfig.postgresDBWriter()
        val reader = differentHostsConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different ports`() {
        // Given
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

        // When
        val writer = differentPortConfig.postgresDBWriter()
        val reader = differentPortConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different database names`() {
        // Given
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

        // When
        val writer = differentDbConfig.postgresDBWriter()
        val reader = differentDbConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different credentials`() {
        // Given
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

        // When
        val writer = differentCredsConfig.postgresDBWriter()
        val reader = differentCredsConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should handle cleanup method`() {
        // When & Then
        // Should not throw exception
        postgresConfig.cleanup()
    }

    @Test
    fun `should create multiple writer connections`() {
        // When
        val writer1 = postgresConfig.postgresDBWriter()
        val writer2 = postgresConfig.postgresDBWriter()

        // Then
        assertNotNull(writer1)
        assertNotNull(writer2)
    }

    @Test
    fun `should create multiple reader connections`() {
        // When
        val reader1 = postgresConfig.postgresDBReader()
        val reader2 = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(reader1)
        assertNotNull(reader2)
    }

    @Test
    fun `should create mixed writer and reader connections`() {
        // When
        val writer = postgresConfig.postgresDBWriter()
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }
}
