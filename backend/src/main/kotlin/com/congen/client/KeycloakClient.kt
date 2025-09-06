package com.congen.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Client for interacting with Keycloak Admin API using service account authentication.
 *
 * This client provides methods to create, update, and delete users in Keycloak
 * using a dedicated service account instead of admin credentials. This provides
 * better security by following the principle of least privilege.
 *
 * @param keycloakUrl Base URL for Keycloak server
 * @param realm Keycloak realm name
 * @param clientId Client ID for authentication
 * @param serviceAccountUsername Service account username
 * @param clientSecret Client secret for authentication
 * @param managementUrl Management URL for health checks
 * @param keycloakWebClient WebClient for HTTP requests
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class KeycloakClient(
    @Value("\${congen.keycloak.url}")
    private val keycloakUrl: String,
    @Value("\${congen.keycloak.realm}")
    private val realm: String,
    @Value("\${congen.keycloak.client.id}")
    private val clientId: String,
    @Value("\${congen.keycloak.service_account.username}")
    private val serviceAccountUsername: String,
    @Value("\${congen.keycloak.client.secret}")
    private val clientSecret: String,
    @Value("\${congen.keycloak.management.url}")
    private val managementUrl: String,
    private val keycloakWebClient: WebClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(KeycloakClient::class.java)
    }

    /**
     * Deletes a user from Keycloak.
     *
     * @param userId Keycloak user ID to delete
     * @return Mono completing when deletion is successful
     */
    fun deleteUser(userId: String): Mono<Void> {
        logger.info("Deleting Keycloak user: {}", userId)

        return getServiceAccountToken()
            .flatMap { token ->
                deleteUserFromKeycloak(token, userId)
            }
            .doOnSuccess { logger.debug("Deleted Keycloak user: {}", userId) }
            .doOnError { e -> logger.error("Error deleting Keycloak user: {}", userId, e) }
    }

    /**
     * Checks the health of Keycloak using the /health/live endpoint.
     * This endpoint is used for monitoring and health checks.
     *
     * @return Mono emitting a ResponseEntity indicating the health status
     */
    fun checkHealthLive(): Mono<ResponseEntity<Void>> {
        val healthUrl = "$managementUrl/health/live"

        return keycloakWebClient.get()
            .uri(healthUrl)
            .retrieve()
            .toBodilessEntity()
    }

    /**
     * Gets the service account token for Keycloak API access.
     *
     * @return Mono emitting the service account access token
     */
    private fun getServiceAccountToken(): Mono<String> {
        val tokenUrl = "$keycloakUrl/realms/$realm/protocol/openid-connect/token"

        return keycloakWebClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(
                "grant_type=client_credentials&" +
                    "client_id=$clientId&" +
                    "client_secret=$clientSecret"
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .map { it.accessToken }
    }

    /**
     * Deletes a user from Keycloak.
     */
    private fun deleteUserFromKeycloak(
        token: String,
        userId: String
    ): Mono<Void> {
        val userUrl = "$keycloakUrl/admin/realms/$realm/users/$userId"

        return keycloakWebClient.delete()
            .uri(userUrl)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .toBodilessEntity()
            .then()
    }

    // Data classes for API requests and responses

    /**
     * Response from Keycloak token endpoint.
     */
    data class TokenResponse(
        /** The access token returned by Keycloak. */
        @JsonProperty("access_token")
        val accessToken: String
    )
}
