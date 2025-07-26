package com.congen.controllers

import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidWeightUnitException
import com.congen.exceptions.KeycloakException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import kotlin.test.assertEquals

class ExceptionHandlingControllerTest {
    private val exceptionHandlingController = ExceptionHandlingController()

    @Test
    fun `conflict method should handle InvalidResultException`() {
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
        assertEquals("Resource not found", response.body!!["error"])
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
        assertEquals(errorMessage, response.body!!["error"])
    }

    @Test
    fun `handleValidationException should be accessible`() {
        // When & Then
        // This test verifies that the method is public and accessible
        val exception = ValidationException("Test validation error")
        exceptionHandlingController.handleValidationException(exception)
    }

    @Test
    fun `handleInvalidWeightUnitException should return 422 status with error message`() {
        val errorMessage = "Invalid weight unit: foo. Must be KG or LBS."
        val exception = InvalidWeightUnitException(errorMessage)
        val response = exceptionHandlingController.handleInvalidWeightUnitException(exception)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals(errorMessage, response.body!!["error"])
    }

    @Test
    fun `handleAccessDeniedException should return 403 status with error message`() {
        val errorMessage = "User not authorized to access this program"
        val exception = AccessDeniedException(errorMessage)
        val response = exceptionHandlingController.handleAccessDeniedException(exception)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Access denied: insufficient permissions", response.body!!["error"])
    }

    @Test
    fun `handleAccessDeniedException should handle null message`() {
        val exception = AccessDeniedException(null)
        val response = exceptionHandlingController.handleAccessDeniedException(exception)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Access denied: insufficient permissions", response.body!!["error"])
    }

    @Test
    fun `handleAuthenticationException should return 401 status with error message`() {
        val errorMessage = "Invalid JWT token"
        val exception = object : AuthenticationException(errorMessage) {}
        val response = exceptionHandlingController.handleAuthenticationException(exception)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication failed: invalid or missing credentials", response.body!!["error"])
    }

    @Test
    fun `handleAuthenticationException should handle null message`() {
        val exception = object : AuthenticationException(null) {}
        val response = exceptionHandlingController.handleAuthenticationException(exception)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication failed: invalid or missing credentials", response.body!!["error"])
    }

    @Test
    fun `handleDatabaseQueryException should return 409 for duplicate key`() {
        val exception = DatabaseQueryException("duplicate key value violates unique constraint")
        val response = exceptionHandlingController.handleDatabaseQueryException(exception)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("Relationship already exists", response.body!!["error"])
    }

    @Test
    fun `handleDatabaseQueryException should return 422 for foreign key violation`() {
        val exception = DatabaseQueryException("violates foreign key constraint")
        val response = exceptionHandlingController.handleDatabaseQueryException(exception)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals("Input does not exist", response.body!!["error"])
    }

    @Test
    fun `handleDatabaseQueryException should return 500 for other errors`() {
        val exception = DatabaseQueryException("some other db error")
        val response = exceptionHandlingController.handleDatabaseQueryException(exception)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Database error: some other db error", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should return 400 for bad request`() {
        val exception = KeycloakException("Invalid email format", HttpStatus.BAD_REQUEST)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid email format", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should return 409 for conflict`() {
        val exception = KeycloakException("User already exists", HttpStatus.CONFLICT)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("User already exists", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should return 401 for unauthorized`() {
        val exception = KeycloakException("Authentication failed", HttpStatus.UNAUTHORIZED)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication failed", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should return 403 for forbidden`() {
        val exception = KeycloakException("Insufficient permissions", HttpStatus.FORBIDDEN)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Insufficient permissions", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should return 500 for other status codes`() {
        val exception = KeycloakException("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Internal server error", response.body!!["error"])
    }

    @Test
    fun `handleKeycloakException should handle null message`() {
        val exception = KeycloakException(null, HttpStatus.BAD_REQUEST)
        val response = exceptionHandlingController.handleKeycloakException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Keycloak operation failed", response.body!!["error"])
    }
}
