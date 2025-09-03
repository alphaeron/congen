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
        val message = "Database error occurred"

        val exception = DatabaseException(message)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseException with message and cause`() {
        val message = "Database error occurred"
        val cause = RuntimeException("Root cause")

        val exception = DatabaseException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseIssueException with message only`() {
        val message = "Database issue occurred"

        val exception = DatabaseIssueException(message)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseIssueException with message and cause`() {
        val message = "Database issue occurred"
        val cause = RuntimeException("Root cause")

        val exception = DatabaseIssueException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseQueryException with message only`() {
        val message = "Query execution failed"

        val exception = DatabaseQueryException(message)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create DatabaseQueryException with message and cause`() {
        val message = "Query execution failed"
        val cause = RuntimeException("Root cause")

        val exception = DatabaseQueryException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create DatabaseConnectionException with cause`() {
        val cause = RuntimeException("Connection failed")

        val exception = DatabaseConnectionException(cause)
        assertEquals("Error in database connection", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `should create NoResultsFoundException with query`() {
        val query = "SELECT * FROM users WHERE id = 999"

        val exception = NoResultsFoundException(query)
        assertEquals("No results returned from query $query", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create NoResultsFoundException with query and parameters`() {
        val query = "SELECT * FROM users WHERE id = \$1"
        val parameters = arrayOf(999)

        val exception = NoResultsFoundException(query, parameters)
        assertEquals("No results returned from query $query with parameters: [999]", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create InvalidResultException with query`() {
        val query = "SELECT * FROM users WHERE name = 'John'"

        val exception = InvalidResultException(query)
        assertEquals("Unexpected number of results from query $query", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should create InvalidResultException with query and parameters`() {
        val query = "SELECT * FROM users WHERE name = \$1"
        val parameters = arrayOf("John")

        val exception = InvalidResultException(query, parameters)
        assertEquals("Unexpected number of results from query $query with parameters: [John]", exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should verify exception hierarchy`() {
        val cause = RuntimeException("Test cause")

        val databaseException = DatabaseException("Base exception", cause)
        val databaseIssueException = DatabaseIssueException("Issue exception", cause)
        val databaseQueryException = DatabaseQueryException("Query exception", cause)
        val databaseConnectionException = DatabaseConnectionException(cause)
        val noResultsFoundException = NoResultsFoundException("SELECT * FROM test")
        val invalidResultException = InvalidResultException("SELECT * FROM test")
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
        val message = "Test message"

        val exception = DatabaseException(message, null)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should handle null cause in DatabaseIssueException`() {
        val message = "Test message"

        val exception = DatabaseIssueException(message, null)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }

    @Test
    fun `should handle null cause in DatabaseQueryException`() {
        val message = "Test message"

        val exception = DatabaseQueryException(message, null)
        assertEquals(message, exception.message)
        assertEquals(null, exception.cause)
    }
}
