package com.congen.controllers

import com.congen.model.HealthCheck
import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import com.congen.service.HealthCheckService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class HealthControllerTest {
    private lateinit var healthCheckService: HealthCheckService
    private lateinit var healthController: HealthController

    @BeforeEach
    fun setUp() {
        healthCheckService = mock()
        healthController = HealthController(healthCheckService)
    }

    @Test
    fun `health check should return OK when all components are healthy`() {
        // Given
        val healthResponse =
            HealthCheckResponse(
                status = HealthStatus.PASS,
                version = "1.2.3",
                releaseId = "abc123",
                checks =
                    mapOf(
                        "database" to
                            listOf(
                                HealthCheck(
                                    componentId = "postgres",
                                    componentType = "database",
                                    status = HealthStatus.PASS,
                                    output = "Database connection successful",
                                ),
                            ),
                        "application" to
                            listOf(
                                HealthCheck(
                                    componentId = "congen-api",
                                    componentType = "service",
                                    status = HealthStatus.PASS,
                                    output = "Application is running",
                                ),
                            ),
                    ),
            )

        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))

        // When
        val result = healthController.healthCheck()

        // Then
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `health check should return SERVICE_UNAVAILABLE when database is unhealthy`() {
        // Given
        val healthResponse =
            HealthCheckResponse(
                status = HealthStatus.FAIL,
                version = "1.2.3",
                releaseId = "abc123",
                checks =
                    mapOf(
                        "database" to
                            listOf(
                                HealthCheck(
                                    componentId = "postgres",
                                    componentType = "database",
                                    status = HealthStatus.FAIL,
                                    output = "Database connection failed",
                                ),
                            ),
                        "application" to
                            listOf(
                                HealthCheck(
                                    componentId = "congen-api",
                                    componentType = "service",
                                    status = HealthStatus.PASS,
                                    output = "Application is running",
                                ),
                            ),
                    ),
            )

        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))

        // When
        val result = healthController.healthCheck()

        // Then
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.SERVICE_UNAVAILABLE)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `health check should return OK when status is warn`() {
        // Given
        val healthResponse =
            HealthCheckResponse(
                status = HealthStatus.WARN,
                version = "1.2.3",
                releaseId = "abc123",
                checks =
                    mapOf(
                        "database" to
                            listOf(
                                HealthCheck(
                                    componentId = "postgres",
                                    componentType = "database",
                                    status = HealthStatus.WARN,
                                    output = "Database connection slow",
                                ),
                            ),
                        "application" to
                            listOf(
                                HealthCheck(
                                    componentId = "congen-api",
                                    componentType = "service",
                                    status = HealthStatus.PASS,
                                    output = "Application is running",
                                ),
                            ),
                    ),
            )

        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))

        // When
        val result = healthController.healthCheck()

        // Then
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }
}
