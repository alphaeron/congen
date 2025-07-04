package com.congen.service

import com.congen.client.PostgresClient
import com.congen.config.VersionConfig
import com.congen.model.HealthStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class HealthCheckServiceTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var versionConfig: VersionConfig
    private lateinit var healthCheckService: HealthCheckService

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        versionConfig = mock()

        // Setup default version config values
        whenever(versionConfig.version).thenReturn("1.0.0")
        whenever(versionConfig.releaseId).thenReturn("test-release")

        healthCheckService = HealthCheckService(postgresClient, versionConfig)
    }

    @Test
    fun `performHealthCheck should return pass status when all components are healthy`() {
        // Given
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        // When
        val result = healthCheckService.performHealthCheck()

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.status == HealthStatus.PASS &&
                    response.version == "1.0.0" &&
                    response.releaseId == "test-release" &&
                    response.checks.containsKey("database") &&
                    response.checks.containsKey("application") &&
                    response.checks["database"]?.first()?.status == HealthStatus.PASS &&
                    response.checks["application"]?.first()?.status == HealthStatus.PASS
            }
            .verifyComplete()
    }

    @Test
    fun `performHealthCheck should return fail status when database is unhealthy`() {
        // Given
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.error(RuntimeException("Database connection failed")))

        // When
        val result = healthCheckService.performHealthCheck()

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.status == HealthStatus.FAIL &&
                    response.version == "1.0.0" &&
                    response.releaseId == "test-release" &&
                    response.checks.containsKey("database") &&
                    response.checks.containsKey("application") &&
                    response.checks["database"]?.first()?.status == HealthStatus.FAIL &&
                    response.checks["application"]?.first()?.status == HealthStatus.PASS
            }
            .verifyComplete()
    }

    @Test
    fun `database health check should include response time when successful`() {
        // Given
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        // When
        val result = healthCheckService.performHealthCheck()

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                val dbCheck = response.checks["database"]?.first()
                dbCheck != null &&
                    dbCheck.observedValue != null &&
                    dbCheck.observedUnit == "ms" &&
                    dbCheck.componentId == "postgres" &&
                    dbCheck.componentType == "database"
            }
            .verifyComplete()
    }

    @Test
    fun `database health check should include error message when failed`() {
        // Given
        val errorMessage = "Connection timeout"
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.error(RuntimeException(errorMessage)))

        // When
        val result = healthCheckService.performHealthCheck()

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                val dbCheck = response.checks["database"]?.first()
                dbCheck != null &&
                    dbCheck.status == HealthStatus.FAIL &&
                    dbCheck.output?.contains(errorMessage) == true
            }
            .verifyComplete()
    }
}
