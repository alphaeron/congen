package com.congen.controllers

import com.congen.mockHealthCheckResponse
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
        val healthResponse = mockHealthCheckResponse(status = HealthStatus.PASS)
        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))
        val result = healthController.healthCheck()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `health check should return SERVICE_UNAVAILABLE when database is unhealthy`() {
        val healthResponse = mockHealthCheckResponse(status = HealthStatus.FAIL, databaseStatus = HealthStatus.FAIL)
        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))
        val result = healthController.healthCheck()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.SERVICE_UNAVAILABLE)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `health check should return SERVICE_UNAVAILABLE when keycloak is unhealthy`() {
        val healthResponse = mockHealthCheckResponse(status = HealthStatus.FAIL, keycloakStatus = HealthStatus.FAIL)
        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))
        val result = healthController.healthCheck()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.SERVICE_UNAVAILABLE)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `health check should return OK when status is warn`() {
        val healthResponse = mockHealthCheckResponse(status = HealthStatus.WARN, databaseStatus = HealthStatus.WARN)
        whenever(healthCheckService.performHealthCheck()).thenReturn(Mono.just(healthResponse))
        val result = healthController.healthCheck()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == healthResponse)
            }
            .verifyComplete()
    }
}
