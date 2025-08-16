package com.congen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.server.ServerWebExchange
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import reactor.core.publisher.Mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.cors.reactive.CorsConfigurationSource

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
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
class SecurityConfig(
    @Value("\${spring.security.csrf.enabled:true}")
    private val csrfEnabled: Boolean
) {
    /**
     * Configures the security filter chain for the API.
     *
     * - Requires authentication for all endpoints by default.
     * - Enables JWT-based OAuth2 resource server support.
     * - Maps Keycloak roles to Spring authorities (ROLE_USER, ROLE_ADMIN, etc).
     *
     * @param http The ServerHttpSecurity instance
     * @param corsConfigurationSource The CORS configuration source
     * @return The configured SecurityWebFilterChain
     */
    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityWebFilterChain {
        return http
            .csrf { csrf -> 
                if (csrfEnabled) {
                    csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .requireCsrfProtectionMatcher { exchange ->
                            val path = exchange.request.path.value()
                            // Ignore CSRF for health and privacy policy endpoints
                            if (path.startsWith("/api/v1/health/") || path == "/api/v1/gdpr/privacy_policy") {
                                ServerWebExchangeMatcher.MatchResult.notMatch()
                            } else {
                                ServerWebExchangeMatcher.MatchResult.match()
                            }
                        }
                } else {
                    csrf.disable()
                }
            }
            .authorizeExchange { exchanges ->
                exchanges
                    // Allow all OPTIONS requests for CORS
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Allow health check endpoints without authentication
                    .pathMatchers("/api/v1/health/**").permitAll()
                    // Allow privacy policy endpoint without authentication
                    .pathMatchers("/api/v1/gdpr/privacy_policy").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .cors { cors -> cors.configurationSource(corsConfigurationSource) }
            .build()
    }

    /**
     * Configures JWT authentication converter to extract roles from Keycloak JWT tokens.
     *
     * Maps Keycloak realm roles to Spring Security authorities.
     *
     * @return ReactiveJwtAuthenticationConverterAdapter configured for Keycloak roles
     */
    @Bean
    fun jwtAuthenticationConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
            val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
            val roles = realmAccess?.get("roles") as? List<*>

            roles?.map { role ->
                SimpleGrantedAuthority("ROLE_$role")
            } ?: emptyList()
        }

        return ReactiveJwtAuthenticationConverterAdapter(converter)
    }
}
