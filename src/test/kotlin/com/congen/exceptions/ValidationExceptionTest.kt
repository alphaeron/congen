package com.congen.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ValidationException].
 *
 * Tests cover all functionality including:
 * - Constructor with message
 * - Message handling
 * - Exception inheritance
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ValidationExceptionTest {
    @Test
    fun `should create ValidationException with message`() {
        // Given
        val message = "Validation failed: field is required"

        // When
        val exception = ValidationException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should create ValidationException with empty message`() {
        // Given
        val message = ""

        // When
        val exception = ValidationException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should verify ValidationException inheritance`() {
        // Given
        val message = "Test validation message"

        // When
        val exception = ValidationException(message)

        // Then
        assertNotNull(exception)
        assert(exception is RuntimeException)
        assert(exception is Exception)
        assert(exception is Throwable)
    }

    @Test
    fun `should handle special characters in message`() {
        // Given
        val message = "Validation failed: field 'user@email.com' contains invalid characters: !@#$%^&*()"

        // When
        val exception = ValidationException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle long message`() {
        // Given
        val message =
            "This is a very long validation message that contains many words and should be handled properly by the " +
                "ValidationException constructor without any issues or truncation of the message content"

        // When
        val exception = ValidationException(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle unicode characters in message`() {
        // Given
        val message = "Validation failed: field contains unicode characters: 你好世界 🌍"

        // When
        val exception = ValidationException(message)

        // Then
        assertEquals(message, exception.message)
    }
}
