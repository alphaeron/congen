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

@Component
class PostgresClient(
    private val postgresDBReader: SqlClient,
    private val postgresDBWriter: SqlClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PostgresClient::class.java)
    }

    final inline fun <reified T : Any> selectIndividual(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<T> = selectIndividual(query, T::class, *queryArgs)

    fun <T : Any> selectIndividual(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> = queryIndividual(postgresDBReader, query, cls, *queryArgs)

    final inline fun <reified T : Any> select(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<List<T>> = select(query, T::class, *queryArgs)

    fun <T : Any> select(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<List<T>> = query(postgresDBReader, query, cls, *queryArgs)

    final inline fun <reified T : Any> update(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<T> = update(query, T::class, *queryArgs)

    fun <T : Any> update(
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> = queryIndividual(postgresDBWriter, "$query RETURNING *", cls, *queryArgs)

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
