package com.congen.exceptions

/**
 * Exception thrown when user consent is required but not provided.
 *
 * This exception is used throughout the application to indicate that
 * a user has not given consent for data processing as required by GDPR.
 * It extends [RuntimeException] and provides a descriptive error message.
 *
 * User consent exceptions are typically caught by the global exception handler
 * and returned as HTTP 400 (Bad Request) responses to the client.
 *
 * @param message Descriptive error message explaining the consent requirement
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
open class UserConsentException(message: String) : RuntimeException(message)
