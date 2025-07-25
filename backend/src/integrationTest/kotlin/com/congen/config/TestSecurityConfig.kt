package com.congen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Test security configuration for integration tests.
 *
 * This configuration disables security for integration tests to allow
 * unauthenticated access to all endpoints. This is necessary because
 * the main SecurityConfig is not active during integration tests
 * (due to @Profile("!integration-test")), but Spring Boot still
 * requires a ServerHttpSecurity bean for actuator endpoints.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@Profile("integration-test")
@EnableWebFluxSecurity
class TestSecurityConfig {
    /**
     * Configures the security filter chain for integration tests.
     *
     * - Disables CSRF protection
     * - Allows all requests without authentication
     * - Provides the required ServerHttpSecurity bean for actuator endpoints
     *
     * @param http The ServerHttpSecurity instance
     * @return The configured SecurityWebFilterChain
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges.anyExchange().permitAll()
            }
            .build()
    }
}
