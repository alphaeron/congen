package com.congen.client

import com.congen.exceptions.KeycloakException
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
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
     * Creates a new user in Keycloak.
     *
     * @param username Username for the new user
     * @param email Email address for the user
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password Initial password for the user
     * @return Mono emitting the created user ID
     */
    fun createUser(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        password: String,
    ): Mono<String> {
        logger.info("Creating Keycloak user: {}", username)

        return getServiceAccountToken()
            .flatMap { token ->
                createUserInKeycloak(token, username, email, firstName, lastName, password)
            }
            .doOnSuccess { userId -> logger.debug("Created Keycloak user with ID: {}", userId) }
            .doOnError { e -> logger.error("Error creating Keycloak user: {}", username, e) }
    }

    /**
     * Updates an existing user in Keycloak.
     *
     * @param userId Keycloak user ID
     * @param email Updated email address
     * @param firstName Updated first name
     * @param lastName Updated last name
     * @return Mono completing when update is successful
     */
    fun updateUser(
        userId: String,
        email: String,
        firstName: String,
        lastName: String
    ): Mono<Void> {
        logger.info("Updating Keycloak user: {}", userId)

        return getServiceAccountToken()
            .flatMap { token ->
                updateUserInKeycloak(token, userId, email, firstName, lastName)
            }
            .doOnSuccess { logger.debug("Updated Keycloak user: {}", userId) }
            .doOnError { e -> logger.error("Error updating Keycloak user: {}", userId, e) }
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
     * Creates a user in Keycloak using the Admin API.
     */
    private fun createUserInKeycloak(
        token: String,
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): Mono<String> {
        val userUrl = "$keycloakUrl/admin/realms/$realm/users"

        val userRequest =
            CreateUserRequest(
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName,
                enabled = true,
                emailVerified = true,
                credentials =
                    listOf(
                        Credential(
                            type = "password",
                            value = password,
                            temporary = false
                        )
                    )
            )

        return keycloakWebClient.post()
            .uri(userUrl)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userRequest)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .flatMap { errorBody ->
                        val errorMessage =
                            when (response.statusCode()) {
                                HttpStatus.BAD_REQUEST -> {
                                    when {
                                        errorBody.contains("password") -> "Password does not meet requirements"
                                        errorBody.contains("email") -> "Invalid email format"
                                        else -> "Invalid user data: $errorBody"
                                    }
                                }
                                HttpStatus.CONFLICT -> "User already exists"
                                HttpStatus.UNAUTHORIZED -> "Authentication failed"
                                HttpStatus.FORBIDDEN -> "Insufficient permissions"
                                else -> "Keycloak error: $errorBody"
                            }
                        Mono.error(KeycloakException(errorMessage, HttpStatus.valueOf(response.statusCode().value())))
                    }
            }
            .toBodilessEntity()
            .flatMap { response ->
                val location = response.headers.location
                if (location != null) {
                    val userId = location.path.substringAfterLast("/")
                    Mono.just(userId)
                } else {
                    Mono.error(IllegalStateException("No user ID returned from Keycloak"))
                }
            }
    }

    /**
     * Updates a user in Keycloak.
     */
    private fun updateUserInKeycloak(
        token: String,
        userId: String,
        email: String,
        firstName: String,
        lastName: String
    ): Mono<Void> {
        val userUrl = "$keycloakUrl/admin/realms/$realm/users/$userId"

        val userRequest =
            UpdateUserRequest(
                email = email,
                firstName = firstName,
                lastName = lastName
            )

        return keycloakWebClient.put()
            .uri(userUrl)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userRequest)
            .retrieve()
            .toBodilessEntity()
            .then()
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

    /**
     * Request to create a new user in Keycloak.
     */
    data class CreateUserRequest(
        /** The username for the new user. */
        val username: String,
        /** The email address for the new user. */
        val email: String,
        /** The first name of the user. */
        @JsonProperty("firstName")
        val firstName: String,
        /** The last name of the user. */
        @JsonProperty("lastName")
        val lastName: String,
        /** Whether the user account is enabled. */
        val enabled: Boolean,
        /** Whether the user's email has been verified. */
        @JsonProperty("emailVerified")
        val emailVerified: Boolean,
        /** The user's credentials (password). */
        val credentials: List<Credential>
    )

    /**
     * User credential information for Keycloak.
     */
    data class Credential(
        /** The type of credential (e.g., "password"). */
        val type: String,
        /** The credential value (e.g., password hash). */
        val value: String,
        /** Whether the credential is temporary and requires change on first login. */
        val temporary: Boolean
    )

    /**
     * Request to update an existing user in Keycloak.
     */
    data class UpdateUserRequest(
        /** The email address for the user. */
        val email: String,
        /** The first name of the user. */
        @JsonProperty("firstName")
        val firstName: String,
        /** The last name of the user. */
        @JsonProperty("lastName")
        val lastName: String
    )
}
