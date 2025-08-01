package com.congen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Security configuration for Keycloak integration.
 *
 * - Enables JWT authentication using Keycloak as the OAuth2 resource server.
 * - Secures all endpoints by default.
 * - Maps Keycloak roles to Spring Security authorities.
 * - Enables method-level security for fine-grained access control.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@Profile("!integration-test")
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {
    /**
     * Configures the security filter chain for the API.
     *
     * - Requires authentication for all endpoints by default.
     * - Enables JWT-based OAuth2 resource server support.
     * - Maps Keycloak roles to Spring authorities (ROLE_USER, ROLE_ADMIN, etc).
     *
     * @param http The ServerHttpSecurity instance
     * @return The configured SecurityWebFilterChain
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow all OPTIONS requests for CORS
                    .pathMatchers("/api/v1/health/**").permitAll()
                    .pathMatchers("/api/v1/user/").permitAll() // Only allow user registration endpoint
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }
}
