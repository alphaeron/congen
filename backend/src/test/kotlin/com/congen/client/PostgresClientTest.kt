package com.congen.client

import com.congen.exceptions.DatabaseConnectionException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockProgrammedExercise
import com.congen.mockUser
import com.congen.model.ProgrammedExercise
import com.congen.model.User
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowIterator
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.ConnectException
import java.util.function.Function

/**
 * Unit tests for [PostgresClient].
 *
 * Tests cover all functionality including:
 * - Query execution with parameters
 * - Update operations
 * - Individual result retrieval
 * - Error handling for various scenarios
 * - Connection failures
 * - Invalid queries
 * - Empty result sets
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class PostgresClientTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var postgresDBReader: SqlClient
    private lateinit var postgresDBWriter: SqlClient
    private lateinit var mockRowSet: RowSet<Row>
    private lateinit var mockRow: Row
    private lateinit var mockPreparedQuery: PreparedQuery<RowSet<Row>>

    @BeforeEach
    fun setUp() {
        postgresDBReader = mock()
        postgresDBWriter = mock()
        postgresClient = PostgresClient(postgresDBReader, postgresDBWriter)
        mockRowSet = mock()
        mockRow = mock()
        mockPreparedQuery = mock()
    }

    @Test
    fun `should execute select query successfully`() {
        val query = "SELECT * FROM users WHERE id = \$1"
        val expectedResult = listOf(mockUser(keycloakId = "1"), mockUser(keycloakId = "2"))
        val row1 = mock<Row>()
        val row2 = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row1, row2)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        // Use Jackson to serialize the User to a map and stub row.toJson() accordingly
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        val map1 = objectMapper.convertValue(mockUser(keycloakId = "1"), Map::class.java) as Map<String, Any>
        val map2 = objectMapper.convertValue(mockUser(keycloakId = "2"), Map::class.java) as Map<String, Any>
        whenever(row1.toJson()).thenReturn(JsonObject(map1))
        whenever(row2.toJson()).thenReturn(JsonObject(map2))
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query, 1)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should execute update query successfully`() {
        val query = "UPDATE users SET name = \$1 WHERE id = \$2"
        val expectedResult = mockUser(keycloakId = "1", name = "New Name")
        val row = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        whenever(
            row.toJson()
        ).thenReturn(
            JsonObject(
                (objectMapper.convertValue(mockUser(keycloakId = "1", name = "New Name"), Map::class.java) as Map<String, Any>)
            )
        )
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBWriter.preparedQuery(eq("$query RETURNING *"))).thenReturn(preparedQuery)

        val result = postgresClient.update<User>(query, "New Name", 1)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should execute updateLiteral query successfully`() {
        val query = "UPDATE users SET name = 'New Name' WHERE id = 1 RETURNING *"
        val expectedResult = mockUser(keycloakId = "1", name = "New Name")
        val row = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        whenever(
            row.toJson()
        ).thenReturn(
            JsonObject(
                (objectMapper.convertValue(mockUser(keycloakId = "1", name = "New Name"), Map::class.java) as Map<String, Any>)
            )
        )
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBWriter.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.updateLiteral(query, User::class)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should handle query with multiple parameters`() {
        val query = "SELECT * FROM users WHERE name = \$1 AND city = \$2"
        val expectedResult = listOf(mockUser(keycloakId = "1"))
        val row = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        whenever(
            row.toJson()
        ).thenReturn(JsonObject((objectMapper.convertValue(mockUser(keycloakId = "1"), Map::class.java) as Map<String, Any>)))
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query, 25, "New York")
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should handle empty result set from select query`() {
        val query = "SELECT * FROM users WHERE id = \$1"
        val expectedResult = listOf<User>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = emptyList<Row>()
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query, 999)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should return individual result from selectIndividual`() {
        val query = "SELECT * FROM users WHERE id = \$1"
        val expectedResult = mockUser(keycloakId = "1", name = "John")
        val row = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        whenever(
            row.toJson()
        ).thenReturn(
            JsonObject(
                (objectMapper.convertValue(mockUser(keycloakId = "1", name = "John"), Map::class.java) as Map<String, Any>)
            )
        )
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.selectIndividual(query, User::class, 1)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should throw NoResultsFoundException when selectIndividual returns no results`() {
        val query = "SELECT * FROM users WHERE id = \$1"
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = emptyList<Row>()
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.selectIndividual(query, User::class, 999)
        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is NoResultsFoundException)
                assertEquals("No results returned from query $query with parameters: [999]", ex.message)
            }
            .verify()
    }

    @Test
    fun `should throw InvalidResultException when selectIndividual returns multiple results`() {
        val query = "SELECT * FROM users WHERE name = \$1"
        val row1 = mock<Row>()
        val row2 = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row1, row2)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        whenever(
            row1.toJson()
        ).thenReturn(
            JsonObject((objectMapper.convertValue(mockUser(keycloakId = "1", name = "John"), Map::class.java) as Map<String, Any>))
        )
        whenever(
            row2.toJson()
        ).thenReturn(
            JsonObject((objectMapper.convertValue(mockUser(keycloakId = "2", name = "Jane"), Map::class.java) as Map<String, Any>))
        )
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.selectIndividual(query, User::class, 25)
        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is InvalidResultException)
                assertEquals("Unexpected number of results from query $query with parameters: [25]", ex.message)
            }
            .verify()
    }

    @Test
    fun `should throw DatabaseQueryException when query execution fails`() {
        val query = "SELECT * FROM invalid_table"
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        whenever(preparedQuery.execute(any<Tuple>()))
            .thenReturn(Future.failedFuture(RuntimeException("Table does not exist")))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query)
        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is DatabaseQueryException) { "Expected DatabaseQueryException but got ${ex::class.qualifiedName}" }
                assert(ex.message?.contains(query) == true) { "Expected message to contain query '$query' but got '${ex.message}'" }
                assert(ex.cause is RuntimeException) { "Expected cause to be RuntimeException but got ${ex.cause?.javaClass?.name}" }
                assert(
                    ex.cause?.message == "Table does not exist"
                ) { "Expected cause message to be 'Table does not exist' but got '${ex.cause?.message}'" }
            }
            .verify()
    }

    @Test
    fun `should throw DatabaseConnectionException when connection fails`() {
        val query = "SELECT * FROM users"
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        whenever(preparedQuery.execute(any<Tuple>()))
            .thenReturn(Future.failedFuture(ConnectException("Connection failed")))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query)
        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is DatabaseConnectionException) { "Expected DatabaseConnectionException but got ${ex::class.qualifiedName}" }
                assert(ex.cause is ConnectException) { "Expected cause to be ConnectException but got ${ex.cause?.javaClass?.name}" }
                assert(
                    ex.cause?.message == "Connection failed"
                ) { "Expected cause message to be 'Connection failed' but got '${ex.cause?.message}'" }
            }
            .verify()
    }

    @Test
    fun `should handle null parameters gracefully`() {
        val query = "SELECT * FROM users WHERE name = \$1"
        val expectedResult = listOf<User>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = emptyList<Row>()
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<User>(query, null)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should handle complex data types in results`() {
        val query = "SELECT * FROM complex_data WHERE id = \$1"
        val expectedResult = listOf(mockProgrammedExercise(id = 1, exerciseName = "Test"))
        val row = mock<Row>()
        val rowSet = mock<RowSet<Row>>()
        val preparedQuery = mock<PreparedQuery<RowSet<Row>>>()
        val rows = listOf(row)
        val rowsIterator = rows.iterator()
        val rowIterator = mock<RowIterator<Row>>()
        whenever(rowIterator.hasNext()).thenAnswer { rowsIterator.hasNext() }
        whenever(rowIterator.next()).thenAnswer { rowsIterator.next() }
        whenever(rowSet.iterator()).thenReturn(rowIterator)
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        val map = objectMapper.convertValue(mockProgrammedExercise(id = 1, exerciseName = "Test"), Map::class.java) as Map<String, Any>
        whenever(row.toJson()).thenReturn(JsonObject(map))
        whenever(preparedQuery.execute(any<Tuple>())).thenReturn(Future.succeededFuture(rowSet))
        whenever(postgresDBReader.preparedQuery(eq(query))).thenReturn(preparedQuery)

        val result = postgresClient.select<ProgrammedExercise>(query, 1)
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete()
    }

    @Test
    fun `should execute transaction with connection reuse`() {
        val mockConnection = mock<SqlConnection>()
        val mockPool = mock<Pool>()

        // Mock the pool.withTransaction method to return a successful Future
        whenever(mockPool.withTransaction<String>(any())).thenAnswer { invocation ->
            val function =
                invocation.getArgument<
                    Function<
                        SqlConnection,
                        Future<String>
                    >
                >(0)
            // Execute the function with the mock connection and return the result
            function.apply(mockConnection)
        }

        // Create a PostgresClient with the mocked pool
        val testClient = PostgresClient(mockPool, mockPool)

        val result =
            testClient.withTransaction<String> {
                Mono.just("test result")
            }

        StepVerifier.create(result)
            .expectNext("test result")
            .verifyComplete()
    }

    @Test
    fun `should handle transaction rollback on error`() {
        val mockConnection = mock<SqlConnection>()
        val mockPool = mock<Pool>()

        // Mock the pool.withTransaction method to return a failed Future when an error occurs
        whenever(mockPool.withTransaction<String>(any())).thenAnswer { invocation ->
            val function =
                invocation.getArgument<
                    Function<
                        SqlConnection,
                        Future<String>
                    >
                >(0)
            // Execute the function with the mock connection and return the result
            function.apply(mockConnection)
        }

        // Create a PostgresClient with the mocked pool
        val testClient = PostgresClient(mockPool, mockPool)

        val result =
            testClient.withTransaction<String> {
                Mono.error<String>(RuntimeException("Test error"))
            }

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
