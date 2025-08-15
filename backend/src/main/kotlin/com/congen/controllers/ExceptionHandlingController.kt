package com.congen.controllers

import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.InvalidWeightUnitException
import com.congen.exceptions.KeycloakException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.UserConsentException
import com.congen.exceptions.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Global exception handler for the Congen API.
 *
 * This controller advice provides centralized exception handling for the entire
 * application. It converts application-specific exceptions to appropriate HTTP
 * status codes and error responses, ensuring consistent error handling across
 * all endpoints.
 *
 * ## Exception Mapping
 *
 * - **InvalidResultException**: HTTP 409 Conflict - Data integrity violations
 * - **NoResultsFoundException**: HTTP 404 Not Found - Resource not found
 * - **ValidationException**: HTTP 422 Unprocessable Entity - Validation errors
 *
 * ## Features
 *
 * - **Centralized Error Handling**: All exceptions are handled in one place
 * - **Consistent Error Responses**: Standardized error message format
 * - **Comprehensive Logging**: All exceptions are logged with appropriate levels
 * - **HTTP Status Mapping**: Proper HTTP status codes for different error types
 *
 * ## Error Response Format
 *
 * Validation errors return a JSON object with an "error" field containing
 * the validation message. Other exceptions return appropriate HTTP status
 * codes with descriptive error messages.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ControllerAdvice
public class ExceptionHandlingController {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExceptionHandlingController::class.java)
    }

    /**
     * Handles InvalidResultException by returning HTTP 409 Conflict.
     *
     * This method handles data integrity violations that occur when database
     * queries return unexpected results, such as multiple results when expecting
     * a single result or no results when expecting at least one.
     *
     * @throws InvalidResultException when data integrity is violated
     */
    @ResponseStatus(
        value = HttpStatus.CONFLICT,
        reason = "Data integrity violation",
    ) // 409
    @ExceptionHandler(InvalidResultException::class)
    fun conflict() {
        logger.warn("Data integrity violation occurred - InvalidResultException")
    }

    /**
     * Handles NoResultsFoundException by returning HTTP 404 Not Found.
     *
     * This method handles cases where a requested resource does not exist
     * in the database. It returns a user-friendly error message indicating
     * that the resource was not found.
     *
     * @param exception The NoResultsFoundException that was thrown
     * @return ResponseEntity with HTTP 404 status and error message
     */
    @ExceptionHandler(NoResultsFoundException::class)
    fun handleNoResultsFound(exception: NoResultsFoundException): ResponseEntity<Map<String, String>> {
        logger.warn("No results found: {}", exception.message ?: "Unknown error")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Resource not found"))
    }

    /**
     * Handles ValidationException by returning HTTP 422 Unprocessable Entity.
     *
     * This method handles validation errors that occur when input data does
     * not meet the required validation criteria. It returns a JSON response
     * with the validation error message.
     *
     * @param exception The ValidationException that was thrown
     * @return ResponseEntity with HTTP 422 status and error deƒtails
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(exception: ValidationException): ResponseEntity<Map<String, String>> {
        logger.error("Validation error occurred: {}", exception.message ?: "Unknown validation error")
        return ResponseEntity.status(
            HttpStatus.UNPROCESSABLE_ENTITY,
        ).body(mapOf("error" to (exception.message ?: "Unknown validation error")))
    }

    /**
     * Handles UserConsentException by returning HTTP 400 Bad Request.
     *
     * This method handles cases where user consent is required but not provided
     * for data processing operations under GDPR compliance.
     *
     * @param exception The UserConsentException that was thrown
     * @return ResponseEntity with HTTP 400 status and error message
     */
    @ExceptionHandler(UserConsentException::class)
    fun handleUserConsentException(exception: UserConsentException): ResponseEntity<Map<String, String>> {
        logger.warn("User consent required but not provided: {}", exception.message ?: "Missing consent")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to (exception.message ?: "User consent required for data processing")))
    }

    /**
     * Handles InvalidWeightUnitException by returning HTTP 422 Unprocessable Entity.
     *
     * This method handles errors when an invalid weight unit is provided in the request.
     *
     * @param exception The InvalidWeightUnitException that was thrown
     * @return ResponseEntity with HTTP 422 status and error details
     */
    @ExceptionHandler(InvalidWeightUnitException::class)
    fun handleInvalidWeightUnitException(exception: InvalidWeightUnitException): ResponseEntity<Map<String, String>> {
        logger.error("Invalid weight unit: {}", exception.message)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(mapOf("error" to (exception.message ?: "Invalid weight unit")))
    }

    /**
     * Handles AccessDeniedException by returning HTTP 403 Forbidden.
     *
     * This method handles authorization failures when a user attempts to access
     * a resource they don't have permission to access.
     *
     * @param exception The AccessDeniedException that was thrown
     * @return ResponseEntity with HTTP 403 status and error message
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(exception: AccessDeniedException): ResponseEntity<Map<String, String>> {
        logger.warn("Access denied: {}", exception.message ?: "Insufficient permissions")
        logger.warn("Debug message\n\n\n{}\n\n\n", exception.stackTrace)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "Access denied: insufficient permissions"))
    }

    /**
     * Handles AuthenticationException by returning HTTP 401 Unauthorized.
     *
     * This method handles authentication failures when a user provides invalid
     * or missing credentials.
     *
     * @param exception The AuthenticationException that was thrown
     * @return ResponseEntity with HTTP 401 status and error message
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(exception: AuthenticationException): ResponseEntity<Map<String, String>> {
        logger.warn("Authentication failed: {}", exception.message ?: "Invalid credentials")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Authentication failed: invalid or missing credentials"))
    }

    /**
     * Handles KeycloakException by returning appropriate HTTP status codes.
     *
     * Maps Keycloak errors to appropriate HTTP status codes:
     * - 400 Bad Request: Invalid user data (email, password)
     * - 409 Conflict: User already exists
     * - 401 Unauthorized: Authentication failed
     * - 403 Forbidden: Insufficient permissions
     * - 500 Internal Server Error: Other Keycloak errors
     *
     * @param exception The KeycloakException that was thrown
     * @return ResponseEntity with appropriate status and error message
     */
    @ExceptionHandler(KeycloakException::class)
    fun handleKeycloakException(exception: KeycloakException): ResponseEntity<Map<String, String>> {
        logger.warn("Keycloak error ({}): {}", exception.httpStatus.value(), exception.message)
        return ResponseEntity.status(exception.httpStatus)
            .body(mapOf("error" to (exception.message ?: "Keycloak operation failed")))
    }

    /**
     * Handles DatabaseQueryException with custom status and message mapping.
     *
     * - Duplicate key: 409 Conflict
     * - Foreign key violation: 422 Unprocessable Entity
     * - Other: 500 Internal Server Error
     *
     * @param exception The DatabaseQueryException that was thrown
     * @return ResponseEntity with appropriate status and error message
     */
    @ExceptionHandler(DatabaseQueryException::class)
    fun handleDatabaseQueryException(exception: DatabaseQueryException): ResponseEntity<Map<String, String>> {
        val msg = exception.cause?.message ?: exception.message ?: "Database error"
        return when {
            msg.contains("duplicate key", ignoreCase = true) -> {
                logger.warn("Duplicate key error: {}", msg)
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(mapOf("error" to "Relationship already exists"))
            }
            msg.contains("violates foreign key", ignoreCase = true) -> {
                logger.warn("Foreign key violation: {}", msg)
                ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(mapOf("error" to "Input does not exist"))
            }
            else -> {
                logger.error("Unhandled database error: {}", msg)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Database error: $msg"))
            }
        }
    }
}
