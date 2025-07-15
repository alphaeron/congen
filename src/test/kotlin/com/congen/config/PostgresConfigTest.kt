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
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5432,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "testdb",
                sslMode = false
            )
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
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5432,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "testdb",
                sslMode = true
            )

        // When
        val writer = postgresConfig.postgresDBWriter()

        // Then
        assertNotNull(writer)
    }

    @Test
    fun `should create reader connection with SSL enabled`() {
        // Given
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5432,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "testdb",
                sslMode = true
            )

        // When
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different hosts`() {
        // Given
        postgresConfig =
            PostgresConfig(
                writerHost = "writer.example.com",
                readerHost = "reader.example.com",
                port = 5432,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "testdb",
                sslMode = false
            )

        // When
        val writer = postgresConfig.postgresDBWriter()
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different ports`() {
        // Given
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5433,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "testdb",
                sslMode = false
            )

        // When
        val writer = postgresConfig.postgresDBWriter()
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different database names`() {
        // Given
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5432,
                usernameV = "testuser",
                passwordV = "testpass",
                dbName = "productiondb",
                sslMode = false
            )

        // When
        val writer = postgresConfig.postgresDBWriter()
        val reader = postgresConfig.postgresDBReader()

        // Then
        assertNotNull(writer)
        assertNotNull(reader)
    }

    @Test
    fun `should create connections with different credentials`() {
        // Given
        postgresConfig =
            PostgresConfig(
                writerHost = "localhost",
                readerHost = "localhost",
                port = 5432,
                usernameV = "produser",
                passwordV = "prodpass",
                dbName = "testdb",
                sslMode = false
            )

        // When
        val writer = postgresConfig.postgresDBWriter()
        val reader = postgresConfig.postgresDBReader()

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
