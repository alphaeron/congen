package com.congen.exceptions

/**
 * Exception thrown when data validation fails.
 *
 * This exception is used throughout the application to indicate that
 * input data does not meet the required validation criteria. It extends
 * [RuntimeException] and provides a descriptive error message explaining
 * what validation rule was violated.
 *
 * Validation exceptions are typically caught by the global exception handler
 * and returned as HTTP 422 (Unprocessable Entity) responses to the client.
 *
 * @param message Descriptive error message explaining the validation failure
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ValidationException(message: String) : RuntimeException(message)
