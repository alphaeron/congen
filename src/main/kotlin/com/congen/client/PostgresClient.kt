package com.congen.client

import com.congen.exceptions.DatabaseConnectionException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
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
    final inline fun <reified T : Any> selectIndividual(
        query: String,
        vararg queryArgs: Any?,
    ): Mono<T> = selectIndividual(query, T::class, *queryArgs)

    fun <T : Any> selectIndividual(query: String, cls: KClass<T>, vararg queryArgs: Any?): Mono<T> =
        queryIndividual(postgresDBReader, query, cls, *queryArgs)

    final inline fun <reified T : Any> select(
        query: String,
        vararg queryArgs: Any?
    ): Mono<List<T>> = select(query, T::class, *queryArgs)

    fun <T : Any> select(query: String, cls: KClass<T>, vararg queryArgs: Any?): Mono<List<T>> =
        query(postgresDBReader, query, cls, *queryArgs)

    final inline fun <reified T : Any> update(
        query: String,
        vararg queryArgs: Any?
    ): Mono<T> = update(query, T::class, *queryArgs)

    fun <T : Any> update(query: String, cls: KClass<T>, vararg queryArgs: Any?): Mono<T> =
        queryIndividual(postgresDBWriter, "${query} RETURNING *", cls, *queryArgs)

    private inline fun <T : Any> queryIndividual(
        sqlClient: SqlClient,
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<T> {
        return query(sqlClient, query, cls, *queryArgs)
            .map {
                if (it.size > 1) {
                    throw InvalidResultException(query)
                } else if (it.size == 0) {
                    // TODO log
                    throw NoResultsFoundException(query)
                } else {
                    it[0]
                }
            }
    }

    private inline fun <T : Any> query(
        sqlClient: SqlClient,
        query: String,
        cls: KClass<T>,
        vararg queryArgs: Any?,
    ): Mono<List<T>> {
        return Mono.fromCompletionStage(
            beginQuery(sqlClient, query, *queryArgs)
                .thenApply {rowSet ->
                    rowSet
                        .toList()
                        .map { row ->
                            row.toJson().mapTo(cls.java)
                        }
                }
        )
    }

    private inline fun beginQuery(
        sqlClient: SqlClient,
        query: String,
        vararg queryArgs: Any?
    ): CompletionStage<RowSet<Row>> {
        return sqlClient
            .preparedQuery(query)
            .execute(Tuple.wrap(arrayOf(*queryArgs)))
            .onFailure {
                if (it.cause!!::class.java == ConnectException::class.java) {
                    // TODO log
                    throw DatabaseConnectionException(it.cause!!)
                } else {
                    // Assume the issue was with the query itself if it was not a connection error.
                    // TODO log
                    throw DatabaseQueryException(query, it.cause!!)
                }
            }
            .toCompletionStage()
    }
}
