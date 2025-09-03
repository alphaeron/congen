package com.congen.exceptions

/**
 * Base exception for all database-related errors.
 *
 * This exception serves as the parent class for all database-related exceptions
 * in the application. It provides a common structure for handling database
 * errors with optional cause information.
 *
 * @param message Descriptive error message
 * @param cause The underlying cause of the exception
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
open class DatabaseException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Exception for general database issues.
 *
 * This exception is used for database problems that are not specifically
 * related to queries or connections, such as configuration issues or
 * general database errors.
 *
 * @param message Descriptive error message
 * @param cause The underlying cause of the exception
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
open class DatabaseIssueException(
    message: String,
    cause: Throwable? = null,
) : DatabaseException(message, cause)

/**
 * Exception for database query-related errors.
 *
 * This exception is used for errors that occur during database query execution,
 * such as syntax errors, constraint violations, or unexpected query results.
 *
 * @param message Descriptive error message
 * @param cause The underlying cause of the exception
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
open class DatabaseQueryException(
    message: String,
    cause: Throwable? = null,
) : DatabaseException(message, cause)

/**
 * Exception for database connection errors.
 *
 * This exception is thrown when there are problems establishing or maintaining
 * a connection to the database, such as network issues, authentication failures,
 * or connection pool exhaustion.
 *
 * @param cause The underlying cause of the connection failure
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class DatabaseConnectionException(
    cause: Throwable,
) : DatabaseIssueException("Error in database connection", cause)

/**
 * Exception thrown when a database query returns no results.
 *
 * This exception is used when a query that expects to return at least one
 * result returns an empty result set. This is commonly used in scenarios
 * where a specific record is expected to exist.
 *
 * @param query The SQL query that returned no results
 * @param parameters The query parameters that were used
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class NoResultsFoundException(
    query: String,
    parameters: Array<out Any?> = emptyArray(),
) : DatabaseQueryException(
    if (parameters.isNotEmpty()) {
        "No results returned from query $query with parameters: ${parameters.contentToString()}"
    } else {
        "No results returned from query $query"
    }
)

/**
 * Exception thrown when a database query returns an unexpected number of results.
 *
 * This exception is used when a query returns a different number of results
 * than expected. For example, when expecting exactly one result but getting
 * multiple results, or when expecting multiple results but getting none.
 *
 * @param query The SQL query that returned unexpected results
 * @param parameters The query parameters that were used
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
open class InvalidResultException(
    query: String,
    parameters: Array<out Any?> = emptyArray(),
) : DatabaseQueryException(
    if (parameters.isNotEmpty()) {
        "Unexpected number of results from query $query with parameters: ${parameters.contentToString()}"
    } else {
        "Unexpected number of results from query $query"
    }
)
