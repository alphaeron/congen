package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.TestStatus
import com.congen.model.UserTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserTestResultDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userTestResultDAL: UserTestResultDAL

    private val testKeycloakId = "test-keycloak-id"
    private val now = Instant.now()
    private val weekStart =
        now.atZone(ZoneOffset.UTC).toLocalDate().let { date ->
            val dayOfWeek = date.dayOfWeek.value
            val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1
            date.minusDays(daysToSubtract.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant()
        }
    private val testResult =
        UserTestResult(
            keycloakId = testKeycloakId,
            weekStartTimestamp = weekStart,
            testName = "vertical_jump",
            status = TestStatus.COMPLETED,
            resultValue = 60.0,
            createdAt = now,
            updatedAt = now
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userTestResultDAL = UserTestResultDAL(postgresClient)
    }

    @Test
    fun `getUserTestResultsForWeek should return test results for week`() {
        val testResults = listOf(testResult)

        whenever(postgresClient.select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResults))

        val result = userTestResultDAL.getUserTestResultsForWeek(testKeycloakId, weekStart)

        StepVerifier.create(result)
            .expectNext(testResults)
            .verifyComplete()

        verify(
            postgresClient
        ).select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), eq(testKeycloakId), anyOrNull())
    }

    @Test
    fun `getUserTestResultsInRange should return test results in range`() {
        val startTimestamp = now.minusSeconds(86400)
        val endTimestamp = now
        val testResults = listOf(testResult)

        whenever(postgresClient.select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResults))

        val result = userTestResultDAL.getUserTestResultsInRange(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(testResults)
            .verifyComplete()

        verify(
            postgresClient
        ).select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), eq(testKeycloakId), anyOrNull(), anyOrNull())
    }

    @Test
    fun `getUserTestResultsInRange should return all test results when no range specified`() {
        val testResults = listOf(testResult)

        whenever(postgresClient.select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResults))

        val result = userTestResultDAL.getUserTestResultsInRange(testKeycloakId, null, null)

        StepVerifier.create(result)
            .expectNext(testResults)
            .verifyComplete()

        verify(postgresClient).select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), eq(testKeycloakId))
    }

    @Test
    fun `getUserTestResult should return specific test result`() {
        whenever(
            postgresClient.selectIndividual<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>())
        )
            .thenReturn(Mono.just(testResult))

        val result = userTestResultDAL.getUserTestResult(testKeycloakId, weekStart, "vertical_jump")

        StepVerifier.create(result)
            .expectNext(testResult)
            .verifyComplete()

        verify(
            postgresClient
        ).selectIndividual<UserTestResult>(
            any<String>(),
            any<kotlin.reflect.KClass<UserTestResult>>(),
            eq(testKeycloakId),
            anyOrNull(),
            eq("vertical_jump")
        )
    }

    @Test
    fun `upsertUserTestResult should upsert test result`() {
        whenever(postgresClient.update<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResult))

        val result = userTestResultDAL.upsertUserTestResult(testResult)

        StepVerifier.create(result)
            .expectNext(testResult)
            .verifyComplete()

        verify(
            postgresClient
        ).update<UserTestResult>(
            any<String>(),
            any<kotlin.reflect.KClass<UserTestResult>>(),
            eq(testKeycloakId),
            anyOrNull(),
            eq("vertical_jump"),
            eq("COMPLETED"),
            eq(60.0)
        )
    }

    @Test
    fun `deleteUserTestResult should delete test result`() {
        whenever(postgresClient.update<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResult))

        val result = userTestResultDAL.deleteUserTestResult(testKeycloakId, weekStart, "vertical_jump")

        StepVerifier.create(result)
            .expectNext(testResult)
            .verifyComplete()

        verify(
            postgresClient
        ).update<UserTestResult>(
            any<String>(),
            any<kotlin.reflect.KClass<UserTestResult>>(),
            eq(testKeycloakId),
            anyOrNull(),
            eq("vertical_jump")
        )
    }

    @Test
    fun `getUserTestResultsForWeek should handle empty results`() {
        whenever(postgresClient.select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(emptyList()))

        val result = userTestResultDAL.getUserTestResultsForWeek(testKeycloakId, weekStart)

        StepVerifier.create(result)
            .expectNext(emptyList())
            .verifyComplete()

        verify(
            postgresClient
        ).select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), eq(testKeycloakId), anyOrNull())
    }

    @Test
    fun `getUserTestResultsInRange should handle empty results`() {
        val startTimestamp = now.minusSeconds(86400)
        val endTimestamp = now

        whenever(postgresClient.select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(emptyList()))

        val result = userTestResultDAL.getUserTestResultsInRange(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(emptyList())
            .verifyComplete()

        verify(
            postgresClient
        ).select<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), eq(testKeycloakId), anyOrNull(), anyOrNull())
    }

    @Test
    fun `upsertUserTestResult should handle null result value`() {
        val testResultWithNullValue = testResult.copy(resultValue = null)

        whenever(postgresClient.update<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testResultWithNullValue))

        val result = userTestResultDAL.upsertUserTestResult(testResultWithNullValue)

        StepVerifier.create(result)
            .expectNext(testResultWithNullValue)
            .verifyComplete()

        verify(
            postgresClient
        ).update<UserTestResult>(
            any<String>(),
            any<kotlin.reflect.KClass<UserTestResult>>(),
            eq(testKeycloakId),
            anyOrNull(),
            eq("vertical_jump"),
            eq("COMPLETED"),
            eq(null)
        )
    }

    @Test
    fun `upsertUserTestResult should handle pending status`() {
        val pendingTestResult = testResult.copy(status = TestStatus.PENDING, resultValue = null)

        whenever(postgresClient.update<UserTestResult>(any<String>(), any<kotlin.reflect.KClass<UserTestResult>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(pendingTestResult))

        val result = userTestResultDAL.upsertUserTestResult(pendingTestResult)

        StepVerifier.create(result)
            .expectNext(pendingTestResult)
            .verifyComplete()

        verify(
            postgresClient
        ).update<UserTestResult>(
            any<String>(),
            any<kotlin.reflect.KClass<UserTestResult>>(),
            eq(testKeycloakId),
            anyOrNull(),
            eq("vertical_jump"),
            eq("PENDING"),
            eq(null)
        )
    }
}
