package com.congen.config

import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer
import java.time.Duration

/**
 * Server configuration for DDoS protection using Spring Boot built-ins.
 *
 * Configures Netty server with connection limits and timeouts to prevent
 * resource exhaustion attacks.
 */
@Configuration
class ServerConfig {
    /**
     * Creates a Netty server customizer for DDoS protection.
     *
     * Configures the Netty HTTP server with:
     * - Connection backlog limits to prevent connection flooding
     * - Socket options for better connection handling
     * - Request decoder limits to prevent buffer overflow attacks
     * - Timeout settings to prevent resource exhaustion
     *
     * @return A NettyServerCustomizer that applies DDoS protection settings
     */
    @Bean
    fun nettyServerCustomizer(): NettyServerCustomizer {
        return NettyServerCustomizer { httpServer: HttpServer ->
            httpServer
                .option(io.netty.channel.ChannelOption.SO_BACKLOG, 1000)
                .option(io.netty.channel.ChannelOption.SO_REUSEADDR, true)
                .childOption(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .childOption(io.netty.channel.ChannelOption.TCP_NODELAY, true)
                .httpRequestDecoder { decoder ->
                    decoder.maxInitialLineLength(4096)
                        .maxHeaderSize(8192)
                        .maxChunkSize(8192)
                }
                .idleTimeout(Duration.ofSeconds(30))
                .requestTimeout(Duration.ofSeconds(10))
        }
    }
}
