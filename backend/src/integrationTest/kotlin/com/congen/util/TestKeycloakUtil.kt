package com.congen.util

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Test-specific implementation of KeycloakUtil for integration tests.
 *
 * This component provides mock values for user ID and roles during integration tests,
 * bypassing the need for actual JWT tokens and Keycloak authentication.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
@Primary
@Profile("integration-test")
class TestKeycloakUtil : KeycloakUtil {
    companion object {
        private val logger = LoggerFactory.getLogger(TestKeycloakUtil::class.java)
    }

    init {
        logger.info("TestKeycloakUtil initialized")
    }

    /**
     * Gets a mock user ID for integration tests.
     *
     * @return Mono emitting a mock user ID
     */
    override fun getCurrentUserId(): Mono<String> = Mono.just("test-user-id")

    /**
     * Gets mock roles for integration tests.
     *
     * @return Mono emitting a set of mock roles including admin
     */
    override fun getCurrentUserRoles(): Mono<Set<String>> = Mono.just(setOf("admin", "user"))

    /**
     * Checks if the current user has the given role.
     *
     * @param role The role to check
     * @return Mono emitting true if the user has the role, false otherwise
     */
    override fun hasRole(role: String): Mono<Boolean> = getCurrentUserRoles().map { it.contains(role) }
}
