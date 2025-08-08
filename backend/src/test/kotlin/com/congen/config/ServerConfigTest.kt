package com.congen.config

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import reactor.netty.http.server.HttpServer
import kotlin.test.assertNotNull

/**
 * Unit tests for [ServerConfig].
 *
 * Tests Netty server customization for DDoS protection.
 */
@SpringBootTest(classes = [ServerConfig::class])
@TestPropertySource(
    properties = [
        "spring.profiles.active=test"
    ]
)
class ServerConfigTest {
    @Test
    fun `should create netty server customizer`() {
        // Given
        val serverConfig = ServerConfig()

        // When
        val customizer = serverConfig.nettyServerCustomizer()

        // Then
        assertNotNull(customizer)
    }

    @Test
    fun `should apply customizer to http server`() {
        // Given
        val serverConfig = ServerConfig()
        val customizer = serverConfig.nettyServerCustomizer()
        val httpServer = HttpServer.create()

        // When
        val customizedServer = customizer.apply(httpServer)

        // Then
        assertNotNull(customizedServer)
    }

    @Test
    fun `should configure server with protection settings`() {
        // Given
        val serverConfig = ServerConfig()
        val customizer = serverConfig.nettyServerCustomizer()

        // When
        val httpServer = HttpServer.create()
        val customizedServer = customizer.apply(httpServer)

        // Then - Should not throw exceptions during configuration
        assertNotNull(customizedServer)
    }
}
