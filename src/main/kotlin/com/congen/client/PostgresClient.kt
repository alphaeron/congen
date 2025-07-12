package com.congen.client

import com.congen.exceptions.DatabaseConnectionException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

/**
 * Client for PostgreSQL database operations using Vert.x reactive SQL client.
 *
 * This class provides a reactive interface for database operations using Vert.x
 * PostgreSQL client. It supports both read and write operations with separate
 * connection pools for optimal performance. The client automatically handles
 * connection errors, query errors, and result validation.
 *
 * The client provides three main operation types:
 * - **Select**: Read operations using the reader connection pool
 * - **Select Individual**: Read operations expecting exactly one result
 * - **Update**: Write operations using the writer connection pool
 *
 * All operations return [Mono] for reactive programming and include
 * comprehensive error handling and logging.
 *
 * @property postgresDBReader SQL client for read operations
 * @property postgresDBWriter SQL client for write operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class PostgresClient(
    private val postgresDBReader: SqlClient,
    private val postgresDBWriter: SqlClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(PostgresClient::class.java)
    }

    /**
     * Executes a query expecting exactly one result using the reader connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the reader connection pool for optimal
     * performance on read operations.
     *
     * @param query SQL query to execute
     * @param queryArgs Query parameters
     * @return Mono containing the single result
     * @throws InvalidResultException if query returns multiple results
     * @throws NoResultsFoundException if query returns no results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    final inline fun <reified T : Any> selectIndividual(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<T> = selectIndividual(query, T::class, *queryArgs)

    /**
     * Executes a query expecting exactly one result using the reader connection.
     *
     * This method executes a query and expects exactly one result. If the query
     * returns multiple results or no results, appropriate exceptions are thrown.
     *
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the single result
     * @throws InvalidResultException if query returns multiple results
     * @throws NoResultsFoundException if query returns no results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> selectIndividual(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> = queryIndividual(postgresDBReader, query, cls, *queryArgs)

    /**
     * Executes a query expecting multiple results using the reader connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the reader connection pool for optimal
     * performance on read operations.
     *
     * @param query SQL query to execute
     * @param queryArgs Query parameters
     * @return Mono containing the list of results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    final inline fun <reified T : Any> select(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<List<T>> = select(query, T::class, *queryArgs)

    /**
     * Executes a query expecting multiple results using the reader connection.
     *
     * This method executes a query and returns all results as a list. The results
     * are automatically mapped to the specified class type using JSON mapping.
     *
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the list of results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> select(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<List<T>> = query(postgresDBReader, query, cls, *queryArgs)

    /**
     * Executes an update query using the writer connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the writer connection pool and automatically
     * appends "RETURNING *" to the query to return the updated data.
     *
     * @param query SQL update query to execute
     * @param queryArgs Query parameters
     * @return Mono containing the updated result
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    final inline fun <reified T : Any> update(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<T> = update(query, T::class, *queryArgs)

    /**
     * Executes an update query using the writer connection.
     *
     * This method executes an update query and returns the updated data. The query
     * is automatically modified to include "RETURNING *" to return the result.
     *
     * @param query SQL update query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the updated result
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> update(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> = queryIndividual(postgresDBWriter, "$query RETURNING *", cls, *queryArgs)

    /**
     * Executes an update query using the writer connection.
     *
     * This method executes an update query and returns the updated data. The query
     * is automatically modified to include "RETURNING *" to return the result.
     *
     * @param query SQL update query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the updated result
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> updateLiteral(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> = queryIndividual(postgresDBWriter, query, cls, *queryArgs)

    /**
     * Executes a query expecting exactly one result.
     *
     * This private method handles the execution of queries that expect exactly
     * one result. It validates the result count and throws appropriate exceptions
     * if the expectation is not met.
     *
     * @param sqlClient SQL client to use for the query
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the single result
     * @throws InvalidResultException if query returns multiple results
     * @throws NoResultsFoundException if query returns no results
     */
    private inline fun <T : Any> queryIndividual(
        sqlClient: SqlClient,
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> {
        logger.debug("Executing individual query: {}", query)
        return query(sqlClient, query, cls, *queryArgs)
            .map {
                if (it.size > 1) {
                    logger.error("Query returned multiple results when expecting single: {}", query)
                    throw InvalidResultException(query)
                } else if (it.size == 0) {
                    logger.error("Query returned no results: {}", query)
                    throw NoResultsFoundException(query)
                } else {
                    logger.debug("Query returned single result: {}", query)
                    it.first()
                }
            }
    }

    /**
     * Executes a query and returns all results.
     *
     * This private method handles the execution of queries and maps the results
     * to the specified class type using JSON mapping.
     *
     * @param sqlClient SQL client to use for the query
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono containing the list of results
     */
    private inline fun <T : Any> query(
        sqlClient: SqlClient,
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<List<T>> {
        logger.debug("Executing query: {}", query)
        return Mono.fromCompletionStage(
            beginQuery(sqlClient, query, *queryArgs)
                .thenApply { rowSet ->
                    rowSet
                        .toList()
                        .map { row ->
                            row.toJson().mapTo(cls.java)
                        }
                },
        )
    }

    /**
     * Begins execution of a prepared query.
     *
     * This private method handles the low-level query execution using Vert.x
     * prepared statements. It includes comprehensive error handling for
     * connection and query errors.
     *
     * @param sqlClient SQL client to use for the query
     * @param query SQL query to execute
     * @param queryArgs Query parameters
     * @return CompletionStage containing the query results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    private inline fun beginQuery(
        sqlClient: SqlClient,
        query: String,
        vararg queryArgs: Any?,
    ): CompletionStage<RowSet<Row>> {
        return sqlClient
            .preparedQuery(query)
            .execute(Tuple.wrap(arrayOf(*queryArgs)))
            .onFailure { throwable ->
                val cause = throwable.cause
                if (cause != null && cause::class.java == ConnectException::class.java) {
                    logger.error("Database connection error for query: {}", query, cause)
                    throw DatabaseConnectionException(cause)
                } else {
                    // Assume the issue was with the query itself if it was not a connection error.
                    logger.error("Database query error for query: {}", query, throwable)
                    throw DatabaseQueryException(query, throwable)
                }
            }
            .toCompletionStage()
    }
}
