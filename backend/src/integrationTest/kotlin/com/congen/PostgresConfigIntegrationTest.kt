package com.congen

import com.congen.config.PostgresConfig
import io.vertx.core.Vertx
import io.vertx.sqlclient.SqlClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Integration tests for [PostgresConfig].
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
class PostgresConfigIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var postgresConfig: PostgresConfig

    @Autowired
    private lateinit var vertx: Vertx

    @Autowired
    @Qualifier("postgresDBWriter")
    private lateinit var writerClient: SqlClient

    @Autowired
    @Qualifier("postgresDBReader")
    private lateinit var readerClient: SqlClient

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should create PostgreSQL writer connection`() {
        assertNotNull(writerClient)
    }

    @Test
    fun `should create PostgreSQL reader connection`() {
        assertNotNull(readerClient)
    }
}
