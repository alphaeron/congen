package com.congen.controllers

import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    fun handleNoResultsFound(exception: NoResultsFoundException): ResponseEntity<String> {
        logger.warn("No results found: {}", exception.message ?: "Unknown error")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found")
    }

    /**
     * Handles ValidationException by returning HTTP 422 Unprocessable Entity.
     *
     * This method handles validation errors that occur when input data does
     * not meet the required validation criteria. It returns a JSON response
     * with the validation error message.
     *
     * @param exception The ValidationException that was thrown
     * @return ResponseEntity with HTTP 422 status and error details
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(exception: ValidationException): ResponseEntity<Map<String, String>> {
        logger.error("Validation error occurred: {}", exception.message ?: "Unknown validation error")
        return ResponseEntity.status(
            HttpStatus.UNPROCESSABLE_ENTITY,
        ).body(mapOf("error" to (exception.message ?: "Unknown validation error")))
    }
}
