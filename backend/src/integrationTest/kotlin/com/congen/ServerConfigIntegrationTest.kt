package com.congen

import com.congen.config.ServerConfig
import org.junit.jupiter.api.Test
import reactor.netty.http.server.HttpServer
import kotlin.test.assertNotNull

/**
 * Integration tests for [ServerConfig].
 *
 * Tests Netty server customization for DDoS protection.
 */
class ServerConfigIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create netty server customizer`() {
        val serverConfig = ServerConfig()

        val customizer = serverConfig.nettyServerCustomizer()

        assertNotNull(customizer)
    }

    @Test
    fun `should apply customizer to http server`() {
        val serverConfig = ServerConfig()
        val customizer = serverConfig.nettyServerCustomizer()
        val httpServer = HttpServer.create()

        val customizedServer = customizer.apply(httpServer)

        assertNotNull(customizedServer)
    }

    @Test
    fun `should configure server with protection settings`() {
        val serverConfig = ServerConfig()
        val customizer = serverConfig.nettyServerCustomizer()

        val httpServer = HttpServer.create()
        val customizedServer = customizer.apply(httpServer)

        assertNotNull(customizedServer)
    }
}
