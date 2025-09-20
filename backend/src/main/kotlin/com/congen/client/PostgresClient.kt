package com.congen.client

import com.congen.exceptions.DatabaseConnectionException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import io.vertx.core.Future
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnection
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
 * @param postgresDBReader SQL client for read operations
 * @param postgresDBWriter SQL client for write operations
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

        /** Thread-local storage for the current transaction connection. */
        private val currentConnection = ThreadLocal<SqlConnection?>()

        /**
         * Sets the current transaction connection for this thread.
         * This is used internally by the transaction methods.
         */
        internal fun setCurrentConnection(connection: SqlConnection?) {
            currentConnection.set(connection)
        }

        /**
         * Gets the current transaction connection for this thread.
         * Returns null if no transaction is active.
         */
        internal fun getCurrentConnection(): SqlConnection? {
            return currentConnection.get()
        }
    }

    /**
     * Executes a query expecting exactly one result using the reader connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the reader connection pool for optimal
     * performance on read operations.
     *
     * @param T The type of the result to retrieve
     * @param query SQL query to execute
     * @param queryArgs Query parameters
     * @return Mono<T> containing the single result
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
     * @param T The type of the result to retrieve
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<T> containing the single result
     * @throws InvalidResultException if query returns multiple results
     * @throws NoResultsFoundException if query returns no results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> selectIndividual(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> {
        return Mono.deferContextual { contextView ->
            val currentConnection = getCurrentConnection() ?: contextView.getOrEmpty<SqlConnection>("transactionConnection").orElse(null)
            val clientToUse = currentConnection ?: postgresDBReader
            queryIndividual(clientToUse, query, cls, *queryArgs)
        }
    }

    /**
     * Executes a query expecting multiple results using the reader connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the reader connection pool for optimal
     * performance on read operations.
     *
     * @param T The type of the results to retrieve
     * @param query SQL query to execute
     * @param queryArgs Query parameters
     * @return Mono<List<T>> containing the list of results
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
     * @param T The type of the results to retrieve
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<List<T>> containing the list of results
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> select(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<List<T>> {
        return Mono.deferContextual { contextView ->
            val currentConnection = getCurrentConnection() ?: contextView.getOrEmpty<SqlConnection>("transactionConnection").orElse(null)
            val clientToUse = currentConnection ?: postgresDBReader
            query(clientToUse, query, cls, *queryArgs)
        }
    }

    /**
     * Executes an update query using the writer connection.
     *
     * This method is a convenience wrapper that infers the return type from
     * the generic parameter. It uses the writer connection pool and automatically
     * appends "RETURNING *" to the query to return the updated data.
     *
     * @param T The type of the result to retrieve
     * @param query SQL update query to execute
     * @param queryArgs Query parameters
     * @return Mono<T> containing the updated result
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
     * @param T The type of the result to retrieve
     * @param query SQL update query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<T> containing the updated result
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> update(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> {
        return Mono.deferContextual { contextView ->
            val currentConnection = getCurrentConnection() ?: contextView.getOrEmpty<SqlConnection>("transactionConnection").orElse(null)
            if (currentConnection != null) {
                // Use the transactional connection if we're in a transaction
                queryIndividual(currentConnection, "$query RETURNING *", cls, *queryArgs)
            } else {
                // Use the writer connection if not in a transaction
                queryIndividual(postgresDBWriter, "$query RETURNING *", cls, *queryArgs)
            }
        }
    }

    /**
     * Executes an update query using the writer connection.
     *
     * This method executes an update query and returns the updated data. The query
     * is automatically modified to include "RETURNING *" to return the result.
     *
     * @param T The type of the result to retrieve
     * @param query SQL update query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<T> containing the updated result
     * @throws DatabaseConnectionException if connection fails
     * @throws DatabaseQueryException if query execution fails
     */
    fun <T : Any> updateLiteral(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> {
        return Mono.deferContextual { contextView ->
            val currentConnection = getCurrentConnection() ?: contextView.getOrEmpty<SqlConnection>("transactionConnection").orElse(null)
            if (currentConnection != null) {
                // Use the transactional connection if we're in a transaction
                queryIndividual(currentConnection, query, cls, *queryArgs)
            } else {
                // Use the writer connection if not in a transaction
                queryIndividual(postgresDBWriter, query, cls, *queryArgs)
            }
        }
    }

    /**
     * Executes a query expecting exactly one result.
     *
     * This private method handles the execution of queries that expect exactly
     * one result. It validates the result count and throws appropriate exceptions
     * if the expectation is not met.
     *
     * @param T The type of the result to retrieve
     * @param sqlClient SQL client to use for the query
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<T> containing the single result
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
                    throw InvalidResultException(query, queryArgs)
                } else if (it.size == 0) {
                    logger.error("Query returned no results: {}", query)
                    throw NoResultsFoundException(query, queryArgs)
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
     * @param T The type of the results to retrieve
     * @param sqlClient SQL client to use for the query
     * @param query SQL query to execute
     * @param cls Class type for result mapping
     * @param queryArgs Query parameters
     * @return Mono<List<T>> containing the list of results
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
        // Check if we're in a transaction context
        val currentConnection = getCurrentConnection()
        val clientToUse = currentConnection ?: sqlClient

        if (currentConnection != null) {
            logger.debug("Using transactional connection for query: {} with args: {}", query, queryArgs.contentToString())
        } else {
            logger.debug("Using regular connection for query: {} with args: {}", query, queryArgs.contentToString())
        }

        return clientToUse
            .preparedQuery(query)
            .execute(Tuple.wrap(arrayOf(*queryArgs)))
            .recover { throwable ->
                if (throwable::class.java == ConnectException::class.java) {
                    logger.error("Database connection error for query: {}", query, throwable)
                    throw DatabaseConnectionException(throwable)
                } else {
                    // Assume the issue was with the query itself if it was not a connection error.
                    logger.error("Database query error for query: {}", query, throwable)
                    throw DatabaseQueryException(query, throwable)
                }
            }
            .toCompletionStage()
    }

    /**
     * Executes a block of operations within a transaction.
     *
     * This method provides a way to execute multiple database operations
     * atomically within a single transaction. If any operation fails,
     * the entire transaction is rolled back.
     *
     * @param T The type of the result returned by the transaction block
     * @param block The block of operations to execute within the transaction
     * @return Mono<T> containing the result of the transaction block
     */
    fun <T : Any> withTransaction(block: () -> Mono<T>): Mono<T> {
        return withWriterTransaction(block)
    }

    /**
     * Executes a block of operations within a transaction using the writer connection.
     *
     * @param T The type of the result returned by the transaction block
     * @param block The block of operations to execute within the transaction
     * @return Mono<T> containing the result of the transaction block
     */
    fun <T : Any> withWriterTransaction(block: () -> Mono<T>): Mono<T> {
        return withConnectionTransaction(postgresDBWriter, block)
    }

    /**
     * Executes a block of operations within a transaction using the reader connection.
     *
     * @param T The type of the result returned by the transaction block
     * @param block The block of operations to execute within the transaction
     * @return Mono<T> containing the result of the transaction block
     */
    fun <T : Any> withReaderTransaction(block: () -> Mono<T>): Mono<T> {
        return withConnectionTransaction(postgresDBReader, block)
    }

    /**
     * Private helper method to execute a transaction block with a given SQL client.
     *
     * This method uses the pool's built-in withTransaction method which properly
     * handles pipelined pools and connection management. It checks for existing
     * transaction context to avoid nested transactions.
     *
     * @param T The type of the result returned by the transaction block
     * @param sqlClient The SQL client (which is actually a pool) to get connections from
     * @param block The block of operations to execute within the transaction
     * @return Mono<T> containing the result of the transaction block
     */
    private fun <T : Any> withConnectionTransaction(
        sqlClient: SqlClient,
        block: () -> Mono<T>
    ): Mono<T> {
        // Cast to Pool since the SqlClient from PgBuilder.client() is actually a pool
        val pool = sqlClient as Pool

        // Check if we're already in a transaction context to avoid nested transactions
        val existingConnection = getCurrentConnection()
        if (existingConnection != null) {
            // We're already in a transaction, just execute the block
            logger.debug("Reusing existing transaction connection")
            return block()
        }

        // Use pool.withTransaction and ensure the connection context is maintained
        return Mono.fromCompletionStage(
            pool.withTransaction<T> { sqlConnection ->
                logger.debug("Transaction begun with connection: {}", sqlConnection.hashCode())

                // Set the transaction context for this thread
                setCurrentConnection(sqlConnection)

                try {
                    // Execute the transaction block, ensuring context is preserved
                    val result =
                        block()
                            .contextWrite { context ->
                                // Store the connection in the reactive context as well
                                context.put("transactionConnection", sqlConnection)
                            }
                            .doFinally { signal ->
                                // Clear the transaction context on completion or error
                                setCurrentConnection(null)
                                logger.debug("Transaction context cleared due to signal: {}", signal)
                            }
                            .toFuture()

                    result.whenComplete { _, throwable ->
                        if (throwable != null) {
                            logger.warn("Transaction failed: {}", throwable.message)
                        } else {
                            logger.debug("Transaction completed successfully")
                        }
                    }

                    // Convert CompletableFuture to Vert.x Future
                    Future.fromCompletionStage(result)
                } catch (e: Exception) {
                    // Clear the transaction context on exception
                    setCurrentConnection(null)
                    logger.warn("Transaction failed with exception: {}", e.message)
                    Future.failedFuture<T>(e)
                }
            }.toCompletionStage()
        )
            .onErrorMap { throwable: Throwable ->
                when (throwable) {
                    is ConnectException -> {
                        logger.error("Database connection error during transaction", throwable)
                        DatabaseConnectionException(throwable)
                    }
                    else -> {
                        logger.error("Database query error during transaction", throwable)
                        DatabaseQueryException("Transaction failed", throwable)
                    }
                }
            }
    }
}
