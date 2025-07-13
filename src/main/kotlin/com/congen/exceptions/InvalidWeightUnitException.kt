package com.congen.exceptions

/**
 * Exception thrown when an invalid weight unit is provided.
 *
 * This exception is used when a user provides an invalid weight unit
 * in a request. It extends RuntimeException and provides a descriptive
 * error message explaining what weight unit was invalid.
 *
 * @param message Descriptive error message explaining the invalid weight unit
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class InvalidWeightUnitException(message: String) : ValidationException(message)
