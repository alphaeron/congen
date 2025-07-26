package com.congen.config

import com.congen.util.KeycloakUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import reactor.core.publisher.Mono

/**
 * Test configuration for Keycloak utilities in integration tests.
 *
 * Provides mock implementations of KeycloakUtil for testing purposes.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@Profile("integration-test")
class TestKeycloakUtilConfig {
    /**
     * Provides a mock KeycloakUtil implementation for integration tests.
     *
     * @return Mock KeycloakUtil that returns test user data
     */
    @Bean
    fun keycloakUtil(): KeycloakUtil {
        return object : KeycloakUtil {
            override fun getCurrentUserId(): Mono<String> = Mono.just("test-user-id")

            override fun getCurrentUserRoles(): Mono<Set<String>> = Mono.just(setOf("admin", "user"))

            override fun hasRole(role: String): Mono<Boolean> = Mono.just(true)
        }
    }
}
