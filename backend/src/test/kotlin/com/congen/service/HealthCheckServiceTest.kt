package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.client.PostgresClient
import com.congen.config.VersionConfig
import com.congen.exceptions.DatabaseException
import com.congen.model.HealthStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class HealthCheckServiceTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var keycloakClient: KeycloakClient
    private lateinit var versionConfig: VersionConfig
    private lateinit var healthCheckService: HealthCheckService

    private val defaultVersion = "1.0.0"
    private val defaultReleaseId = "test-release"

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        keycloakClient = mock()
        versionConfig = mock()

        whenever(versionConfig.version).thenReturn(defaultVersion)
        whenever(versionConfig.releaseId).thenReturn(defaultReleaseId)

        // Mock the new checkHealthLive method
        val responseEntity = ResponseEntity.ok().build<Void>()
        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(responseEntity))

        healthCheckService = HealthCheckService(postgresClient, keycloakClient, versionConfig)
    }

    @Test
    fun `performHealthCheck should include all required health check components`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.version == "1.0.0" &&
                    response.releaseId == "test-release" &&
                    response.checks.containsKey("database") &&
                    response.checks.containsKey("keycloak") &&
                    response.checks.containsKey("application") &&
                    response.checks["database"]?.first()?.componentId == "postgres" &&
                    response.checks["keycloak"]?.first()?.componentId == "keycloak" &&
                    response.checks["application"]?.first()?.componentId == "congen-api"
            }
            .verifyComplete()
    }

    @Test
    fun `database health check should include response time when successful`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()
        StepVerifier.create(result)
            .expectNextMatches { response ->
                val dbCheck = response.checks["database"]?.first()
                dbCheck != null &&
                    dbCheck.observedValue != null &&
                    dbCheck.observedUnit == "ms" &&
                    dbCheck.componentId == "postgres" &&
                    dbCheck.componentType == "database" &&
                    dbCheck.status == HealthStatus.PASS
            }
            .verifyComplete()
    }

    @Test
    fun `database health check should include error message when failed`() {
        val errorMessage = "Connection timeout"
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.error(DatabaseException(errorMessage)))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                val dbCheck = response.checks["database"]?.first()
                dbCheck != null &&
                    dbCheck.status == HealthStatus.FAIL &&
                    dbCheck.output?.contains(errorMessage) == true
            }
            .verifyComplete()
    }

    @Test
    fun `keycloak health check should include response time when successful`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                val keycloakCheck = response.checks["keycloak"]?.first()
                keycloakCheck != null &&
                    keycloakCheck.observedValue != null &&
                    keycloakCheck.observedUnit == "ms" &&
                    keycloakCheck.componentId == "keycloak" &&
                    keycloakCheck.componentType == "auth" &&
                    keycloakCheck.status == HealthStatus.PASS
            }
            .verifyComplete()
    }

    @Test
    fun `keycloak health check should include error message when failed`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        val errorMessage = "Connection refused"
        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.error(RuntimeException(errorMessage)))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                val keycloakCheck = response.checks["keycloak"]?.first()
                keycloakCheck != null &&
                    keycloakCheck.status == HealthStatus.FAIL &&
                    keycloakCheck.output?.contains("Keycloak health check failed: Connection refused") == true
            }
            .verifyComplete()
    }

    @Test
    fun `keycloak health check should handle WebClientResponseException`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        val webClientException =
            WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                HttpHeaders(),
                ByteArray(0),
                null
            )
        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.error(webClientException))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                val keycloakCheck = response.checks["keycloak"]?.first()
                keycloakCheck != null &&
                    keycloakCheck.status == HealthStatus.FAIL &&
                    keycloakCheck.output?.contains("Keycloak health check failed: 503 Service Unavailable") == true
            }
            .verifyComplete()
    }

    @Test
    fun `overall status should be FAIL when any component fails`() {
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.error(DatabaseException("Database error")))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.status == HealthStatus.FAIL
            }
            .verifyComplete()
    }

    @Test
    fun `overall status should be PASS when all components pass`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.status == HealthStatus.PASS
            }
            .verifyComplete()
    }

    @Test
    fun `application health check should always pass`() {
        val dbResult = listOf(mapOf("health_check" to 1))
        whenever(postgresClient.select<Map<String, Any>>("SELECT 1 as health_check"))
            .thenReturn(Mono.just(dbResult))

        whenever(keycloakClient.checkHealthLive()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = healthCheckService.performHealthCheck()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                val appCheck = response.checks["application"]?.first()
                appCheck != null &&
                    appCheck.status == HealthStatus.PASS &&
                    appCheck.componentId == "congen-api" &&
                    appCheck.componentType == "service" &&
                    appCheck.output == "Application is running"
            }
            .verifyComplete()
    }
}
