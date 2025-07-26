package com.congen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

/**
 * Configuration for Keycloak client beans.
 */
@Configuration
class KeycloakClientConfig {
    companion object {
        private const val KEYCLOAK_WEBCLIENT_TIMEOUT_SECONDS = 5L

        // 1MB
        private const val KEYCLOAK_WEBCLIENT_MAX_MEMORY_BYTES = 1024 * 1024
    }

    /**
     * Creates a WebClient bean for HTTP requests to Keycloak.
     */
    @Bean
    fun keycloakWebClient(): WebClient {
        return WebClient.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(KEYCLOAK_WEBCLIENT_MAX_MEMORY_BYTES)
            }
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(KEYCLOAK_WEBCLIENT_TIMEOUT_SECONDS))
                )
            )
            .build()
    }
}
