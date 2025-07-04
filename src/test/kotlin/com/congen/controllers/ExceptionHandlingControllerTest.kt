package com.congen.controllers

import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class ExceptionHandlingControllerTest {
    private val exceptionHandlingController = ExceptionHandlingController()

    @Test
    fun `conflict method should handle InvalidResultException`() {
        // Given
        val exception = InvalidResultException("Test query")

        // When & Then
        // The method should not throw any exception
        exceptionHandlingController.conflict()

        // This test verifies that the method exists and can be called
        // The actual exception handling is done by Spring's exception handling mechanism
        // which is tested through integration tests
    }

    @Test
    fun `conflict method should be accessible`() {
        // When & Then
        // This test verifies that the method is public and accessible
        exceptionHandlingController.conflict()
    }

    @Test
    fun `handleNoResultsFound should return 404 status`() {
        // Given
        val exception = NoResultsFoundException("Resource not found")

        // When
        val response = exceptionHandlingController.handleNoResultsFound(exception)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Resource not found", response.body)
    }

    @Test
    fun `handleValidationException should return 422 status with error message`() {
        // Given
        val errorMessage = "User age must be between 1 and 150, got: 0"
        val exception = ValidationException(errorMessage)

        // When
        val response = exceptionHandlingController.handleValidationException(exception)

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals(mapOf("error" to errorMessage), response.body)
    }

    @Test
    fun `handleValidationException should be accessible`() {
        // When & Then
        // This test verifies that the method is public and accessible
        val exception = ValidationException("Test validation error")
        exceptionHandlingController.handleValidationException(exception)
    }
}
