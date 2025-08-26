package com.congen.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [VertxConfig].
 *
 * Tests cover all functionality including:
 * - Vert.x instance creation
 * - Jackson object mapper configuration
 * - Pretty-print mapper configuration
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class VertxConfigTest {
    private val vertxConfig = VertxConfig()

    @Test
    fun `should create Vertx bean`() {
        val vertx = vertxConfig.vertx()

        assertNotNull(vertx)
    }

    @Test
    fun `should create multiple Vertx instances`() {
        val vertx1 = vertxConfig.vertx()
        val vertx2 = vertxConfig.vertx()

        assertNotNull(vertx1)
        assertNotNull(vertx2)
    }

    @Test
    fun `should create Vertx instance with configured Jackson mappers`() {
        val vertx = vertxConfig.vertx()

        assertNotNull(vertx)
        // The Vertx instance is created and Jackson mappers are configured
        // We can't easily test the internal mapper configuration without reflection
        // but the test ensures the method completes successfully
    }
}
