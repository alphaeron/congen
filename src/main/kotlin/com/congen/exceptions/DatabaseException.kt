package com.congen.exceptions

open class DatabaseException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

open class DatabaseIssueException(
    message: String,
    cause: Throwable? = null,
) : DatabaseException(message, cause)

open class DatabaseQueryException(
    message: String,
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseConnectionException(
    cause: Throwable,
) : DatabaseIssueException("Error in database connection", cause)

class NoResultsFoundException(
    query: String,
) : DatabaseQueryException("No results returned from query ${query}")

open class InvalidResultException(
    query: String,
) : DatabaseQueryException("Unexpected number of results from query ${query}")
