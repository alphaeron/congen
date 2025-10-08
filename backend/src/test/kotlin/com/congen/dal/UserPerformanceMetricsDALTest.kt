package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserPerformanceMetrics
import com.congen.service.AuditService
import com.congen.service.LogSensitivity
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

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserPerformanceMetricsDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var auditService: AuditService
    private lateinit var userPerformanceMetricsDAL: UserPerformanceMetricsDAL

    private val testKeycloakId = "test-keycloak-id"
    private val now = Instant.now()
    private val metrics =
        UserPerformanceMetrics(
            keycloakId = testKeycloakId,
            vo2Max = 45.0,
            strain = 12.5,
            recovery = 75.0,
            hrv = 55.0,
            sleepScore = 80.0,
            remSleepMinutes = 90.0,
            deepSleepMinutes = 120.0,
            subjectiveTiredness = 3,
            createdAt = now,
            updatedAt = now
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        auditService = mock()

        // Mock audit service to return empty Mono
        whenever(auditService.logDataAccess(any(), any(), any(), any())).thenReturn(Mono.empty())

        userPerformanceMetricsDAL = UserPerformanceMetricsDAL(postgresClient, auditService)
    }

    @Test
    fun `selectUserPerformanceMetrics should return latest metrics`() {
        val metricsList = listOf(metrics)

        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(metricsList))

        val result = userPerformanceMetricsDAL.selectUserPerformanceMetrics(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(metrics)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "SELECT_LATEST", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).select<UserPerformanceMetrics>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceMetrics>>(), eq(testKeycloakId))
    }

    @Test
    fun `selectUserPerformanceMetrics should throw NoResultsFoundException when no metrics found`() {
        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(emptyList()))

        val result = userPerformanceMetricsDAL.selectUserPerformanceMetrics(testKeycloakId)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(auditService).logDataAccess("user_performance_metrics", "SELECT_LATEST", testKeycloakId, LogSensitivity.LOW)
    }

    @Test
    fun `upsertUserPerformanceMetrics should update existing metrics for same day`() {
        val existingMetrics = metrics.copy(vo2Max = 40.0)
        val updatedMetrics = metrics.copy(vo2Max = 45.0)

        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(listOf(existingMetrics)))
        whenever(
            postgresClient.update<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(updatedMetrics))

        val result = userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectNext(updatedMetrics)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceMetrics>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceMetrics>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceMetrics should insert new metrics for different day`() {
        val existingMetrics = metrics.copy(createdAt = now.minusSeconds(86400)) // Previous day
        val newMetrics = metrics.copy(createdAt = now)

        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(listOf(existingMetrics)))
        whenever(
            postgresClient.update<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(newMetrics))

        val result = userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectNext(newMetrics)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceMetrics>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceMetrics>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceMetrics should insert new metrics when no existing metrics`() {
        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.error(NoResultsFoundException("No metrics found")))
        whenever(
            postgresClient.update<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(metrics))

        val result = userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectNext(metrics)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceMetrics>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceMetrics>>(), any<Array<Any?>>())
    }

    @Test
    fun `getUserPerformanceMetricsInRange should return metrics in range`() {
        val startTimestamp = now.minusSeconds(86400)
        val endTimestamp = now
        val metricsList = listOf(metrics)

        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(metricsList))

        val result = userPerformanceMetricsDAL.getUserPerformanceMetricsInRange(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(metricsList)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "SELECT_RANGE", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).select<UserPerformanceMetrics>(
            any<String>(),
            any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
            eq(testKeycloakId),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `getLatestUserPerformanceMetrics should return latest metrics`() {
        val metricsList = listOf(metrics)

        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(metricsList))

        val result = userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(metrics)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_metrics", "SELECT_LATEST", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).select<UserPerformanceMetrics>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceMetrics>>(), eq(testKeycloakId))
    }

    @Test
    fun `getLatestUserPerformanceMetrics should throw NoResultsFoundException when no metrics found`() {
        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(emptyList()))

        val result = userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(testKeycloakId)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(auditService).logDataAccess("user_performance_metrics", "SELECT_LATEST", testKeycloakId, LogSensitivity.LOW)
    }

    @Test
    fun `upsertUserPerformanceMetrics should propagate non-NoResultsFoundException errors`() {
        val error = RuntimeException("Database error")
        whenever(
            postgresClient.select<UserPerformanceMetrics>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceMetrics>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.error(error))

        val result = userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(auditService).logDataAccess("user_performance_metrics", "UPSERT", testKeycloakId, LogSensitivity.LOW)
    }
}
