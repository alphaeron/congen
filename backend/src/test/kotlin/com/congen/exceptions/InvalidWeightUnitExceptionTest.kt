package com.congen.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [InvalidWeightUnitException].
 *
 * Tests cover all functionality including:
 * - Constructor with message
 * - Message handling
 * - Exception inheritance hierarchy
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class InvalidWeightUnitExceptionTest {
    @Test
    fun `should create InvalidWeightUnitException with message`() {
        val message = "Invalid weight unit: INVALID_UNIT"

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should create InvalidWeightUnitException with empty message`() {
        val message = ""

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should verify InvalidWeightUnitException inheritance hierarchy`() {
        val message = "Test weight unit message"

        val exception = InvalidWeightUnitException(message)

        assertNotNull(exception)
        assert(exception is ValidationException)
        assert(exception is RuntimeException)
        assert(exception is Exception)
        assert(exception is Throwable)
    }

    @Test
    fun `should handle specific weight unit error messages`() {
        val message = "Weight unit 'LB' is not supported. Supported units are: KG, LBS"

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle weight unit with special characters`() {
        val message = "Invalid weight unit: 'kg/lbs' - mixed units not allowed"

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle weight unit with numbers`() {
        val message = "Invalid weight unit: '123' - must be a valid unit string"

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle unicode characters in weight unit message`() {
        val message = "Invalid weight unit: '公斤' - only English units supported"

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `should handle long weight unit error message`() {
        val message =
            "The weight unit provided 'VERY_LONG_INVALID_UNIT_NAME_THAT_EXCEEDS_NORMAL_LENGTH' is not recognized by the system " +
                "and cannot be processed. Please use one of the supported weight units: KG, LBS, or contact support for assistance."

        val exception = InvalidWeightUnitException(message)

        assertEquals(message, exception.message)
    }
}
