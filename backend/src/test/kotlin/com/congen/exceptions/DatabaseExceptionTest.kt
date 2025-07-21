package com.congen.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for database exception classes.
 *
 * Tests cover all functionality including:
 * - DatabaseException base class
 * - DatabaseIssueException
 * - DatabaseQueryException
 * - DatabaseConnectionException
 * - NoResultsFoundException
 * - InvalidResultException
 * - All constructors and message handling
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class DatabaseExceptionTest {
    @Test
    fun `should create DatabaseException with message only`() {
        // Given
        val message = "Database error occurred"

        // When
        val exception = DatabaseException(message)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseException with message and cause`() {
        // Given
        val message = "Database error occurred"
        val cause = RuntimeException("Root cause")

        // When
        val exception = DatabaseException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseIssueException with message only`() {
        // Given
        val message = "Database issue occurred"

        // When
        val exception = DatabaseIssueException(message)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseIssueException with message and cause`() {
        // Given
        val message = "Database issue occurred"
        val cause = RuntimeException("Root cause")

        // When
        val exception = DatabaseIssueException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseQueryException with message only`() {
        // Given
        val message = "Query execution failed"

        // When
        val exception = DatabaseQueryException(message)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseQueryException with message and cause`() {
        // Given
        val message = "Query execution failed"
        val cause = RuntimeException("Root cause")

        // When
        val exception = DatabaseQueryException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseConnectionException with cause`() {
        // Given
        val cause = RuntimeException("Connection failed")

        // When
        val exception = DatabaseConnectionException(cause)

        // Then
        assertEquals("Error in database connection", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create NoResultsFoundException with query`() {
        // Given
        val query = "SELECT * FROM users WHERE id = 999"

        // When
        val exception = NoResultsFoundException(query)

        // Then
        assertEquals("No results returned from query $query", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create InvalidResultException with query`() {
        // Given
        val query = "SELECT * FROM users WHERE name = 'John'"

        // When
        val exception = InvalidResultException(query)

        // Then
        assertEquals("Unexpected number of results from query $query", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should verify exception hierarchy`() {
        // Given
        val cause = RuntimeException("Test cause")

        // When
        val databaseException = DatabaseException("Base exception", cause)
        val databaseIssueException = DatabaseIssueException("Issue exception", cause)
        val databaseQueryException = DatabaseQueryException("Query exception", cause)
        val databaseConnectionException = DatabaseConnectionException(cause)
        val noResultsFoundException = NoResultsFoundException("SELECT * FROM test")
        val invalidResultException = InvalidResultException("SELECT * FROM test")

        // Then
        assertNotNull(databaseException)
        assertNotNull(databaseIssueException)
        assertNotNull(databaseQueryException)
        assertNotNull(databaseConnectionException)
        assertNotNull(noResultsFoundException)
        assertNotNull(invalidResultException)

        // Verify inheritance hierarchy
        assert(databaseIssueException is DatabaseException)
        assert(databaseQueryException is DatabaseException)
        assert(databaseConnectionException is DatabaseIssueException)
        assert(noResultsFoundException is DatabaseQueryException)
        assert(invalidResultException is DatabaseQueryException)
    }

    @Test
    fun `should handle null cause in DatabaseException`() {
        // Given
        val message = "Test message"

        // When
        val exception = DatabaseException(message, null)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should handle null cause in DatabaseIssueException`() {
        // Given
        val message = "Test message"

        // When
        val exception = DatabaseIssueException(message, null)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should handle null cause in DatabaseQueryException`() {
        // Given
        val message = "Test message"

        // When
        val exception = DatabaseQueryException(message, null)

        // Then
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }
}
