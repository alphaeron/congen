package com.congen.exceptions

import org.springframework.http.HttpStatus

/**
 * Exception thrown when Keycloak operations fail.
 *
 * This exception is used to handle errors that occur during Keycloak operations
 * such as user creation, updates, or deletions. It provides specific error
 * information from Keycloak and maps to appropriate HTTP status codes.
 *
 * @param message Descriptive error message explaining the Keycloak operation failure
 * @param httpStatus The HTTP status code returned by Keycloak
 * @param cause The underlying cause of the exception
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class KeycloakException(
    message: String?,
    val httpStatus: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
