package com.congen.exceptions

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

/**
 * Unit tests for KeycloakException.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class KeycloakExceptionTest {
    @Test
    fun `should create KeycloakException with message and status`() {
        val message = "Invalid email format"
        val httpStatus = HttpStatus.BAD_REQUEST
        val exception = KeycloakException(message, httpStatus)

        assertEquals(message, exception.message)
        assertEquals(httpStatus, exception.httpStatus)
    }

    @Test
    fun `should create KeycloakException with message, status and cause`() {
        val message = "Password does not meet requirements"
        val httpStatus = HttpStatus.BAD_REQUEST
        val cause = RuntimeException("Original error")
        val exception = KeycloakException(message, httpStatus, cause)

        assertEquals(message, exception.message)
        assertEquals(httpStatus, exception.httpStatus)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should handle null cause`() {
        val message = "User already exists"
        val httpStatus = HttpStatus.CONFLICT
        val exception = KeycloakException(message, httpStatus)

        assertEquals(message, exception.message)
        assertEquals(httpStatus, exception.httpStatus)
        assertEquals(null, exception.cause)
    }
}
