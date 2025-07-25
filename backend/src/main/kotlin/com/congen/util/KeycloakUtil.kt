package com.congen.util

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Interface for extracting Keycloak user information from the security context.
 *
 * Provides methods to get the Keycloak user ID (sub claim) and roles from the JWT.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
interface KeycloakUtil {
    /**
     * Gets the Keycloak user ID (sub claim) from the current authentication context.
     *
     * @return Mono emitting the user ID as a String, or empty if not authenticated
     */
    fun getCurrentUserId(): Mono<String>

    /**
     * Gets the Keycloak roles from the current authentication context.
     *
     * @return Mono emitting a set of roles, or empty if not authenticated
     */
    fun getCurrentUserRoles(): Mono<Set<String>>

    /**
     * Checks if the current user has the given role.
     *
     * @param role The role to check (e.g., "admin")
     * @return Mono emitting true if the user has the role, false otherwise
     */
    fun hasRole(role: String): Mono<Boolean>
}

/**
 * Implementation of KeycloakUtil for extracting Keycloak user information from the security context.
 *
 * Provides methods to get the Keycloak user ID (sub claim) and roles from the JWT.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
@Profile("!integration-test")
class KeycloakUtilImpl : KeycloakUtil {
    companion object {
        private val logger = LoggerFactory.getLogger(KeycloakUtilImpl::class.java)
    }

    init {
        logger.info("KeycloakUtilImpl initialized")
    }

    override fun getCurrentUserId(): Mono<String> =
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull { ctx ->
                val auth = ctx.authentication as? JwtAuthenticationToken
                (auth?.token as? Jwt)?.subject
            }

    override fun getCurrentUserRoles(): Mono<Set<String>> =
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull { ctx ->
                val auth = ctx.authentication as? JwtAuthenticationToken
                val jwt = auth?.token as? Jwt
                val realmAccess = jwt?.claims?.get("realm_access") as? Map<*, *>
                @Suppress("UNCHECKED_CAST")
                (realmAccess?.get("roles") as? List<String>)?.toSet() ?: emptySet()
            }

    override fun hasRole(role: String): Mono<Boolean> = getCurrentUserRoles().map { it.contains(role) }
}
