package com.congen.util

import org.slf4j.LoggerFactory
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

    /**
     * Gets the user's full name from the JWT token claims.
     * Attempts to construct the name from given_name and family_name,
     * falls back to the name claim if available.
     *
     * @return Mono emitting the user's full name, or empty if not available
     */
    fun getCurrentUserName(): Mono<String>
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

    override fun getCurrentUserName(): Mono<String> =
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull { ctx ->
                val auth = ctx.authentication as? JwtAuthenticationToken
                val jwt = auth?.token as? Jwt
                val claims = jwt?.claims ?: return@mapNotNull null

                // Try to construct name from given_name and family_name
                val givenName = claims["given_name"] as? String
                val familyName = claims["family_name"] as? String

                if (!givenName.isNullOrBlank() && !familyName.isNullOrBlank()) {
                    "$givenName $familyName".trim()
                } else {
                    // Fallback to the name claim
                    claims["name"] as? String
                }
            }
}
