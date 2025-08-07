package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.client.PostgresClient
import com.congen.config.VersionConfig
import com.congen.model.HealthCheck
import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

/**
 * Service for performing health checks on the application and its dependencies.
 *
 * This service implements health check functionality following the RFC specification
 * for API health checks. It monitors the health of the application and its database
 * connection, providing detailed status information for monitoring and debugging.
 *
 * The service performs checks on:
 * - **Database**: Verifies connectivity and response time to PostgreSQL
 * - **Keycloak**: Verifies connectivity and authentication to Keycloak
 * - **Application**: Confirms the application is running and responsive
 *
 * Health check results include:
 * - Overall status (PASS, WARN, FAIL)
 * - Component-specific health information
 * - Response times and error details
 * - Version and release information
 *
 * @property postgresClient Client for database operations
 * @property versionConfig Configuration containing version information
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class HealthCheckService(
    private val postgresClient: PostgresClient,
    private val keycloakClient: KeycloakClient,
    private val versionConfig: VersionConfig,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(HealthCheckService::class.java)
    }

    /**
     * Performs a comprehensive health check of the application and its dependencies.
     *
     * This method executes health checks for all monitored components and returns
     * a consolidated health check response. The overall status is determined by
     * the most severe status among all components (FAIL > WARN > PASS).
     *
     * The health check includes:
     * - Database connectivity and response time
     * - Keycloak connectivity and authentication
     * - Application status
     * - Version and release information
     *
     * @return Mono containing the health check response
     */
    fun performHealthCheck(): Mono<HealthCheckResponse> {
        logger.debug("Performing health check")

        return Mono.zip(
            checkDatabaseHealth().onErrorResume { error ->
                logger.error("Database health check failed in performHealthCheck", error)
                Mono.just(
                    HealthCheck(
                        componentId = "postgres",
                        componentType = "database",
                        status = HealthStatus.FAIL,
                        output = "Database connection failed: ${error.message}",
                        links = mapOf("self" to "/health"),
                        time = Instant.now()
                    )
                )
            },
            checkKeycloakHealth().onErrorResume { error ->
                logger.error("Keycloak health check failed in performHealthCheck", error)
                Mono.just(
                    HealthCheck(
                        componentId = "keycloak",
                        componentType = "auth",
                        status = HealthStatus.FAIL,
                        output = "Keycloak connection failed: ${error.message}",
                        links = mapOf("self" to "/health"),
                        time = Instant.now()
                    )
                )
            },
            checkApplicationHealth().onErrorResume { error ->
                logger.error("Application health check failed in performHealthCheck", error)
                Mono.just(
                    HealthCheck(
                        componentId = "congen-api",
                        componentType = "service",
                        status = HealthStatus.FAIL,
                        output = "Application health check failed: ${error.message}",
                        links = mapOf("self" to "/health"),
                        time = Instant.now()
                    )
                )
            }
        ).map { tuple ->
            val dbHealth = tuple.t1
            val keycloakHealth = tuple.t2
            val appHealth = tuple.t3
            val overallStatus = determineOverallStatus(listOf(dbHealth.status, keycloakHealth.status, appHealth.status))

            HealthCheckResponse(
                status = overallStatus,
                version = versionConfig.version,
                releaseId = versionConfig.releaseId,
                serviceId = "congen",
                description = "Congen Exercise API Health Check",
                checks =
                    mapOf(
                        "database" to listOf(dbHealth),
                        "keycloak" to listOf(keycloakHealth),
                        "application" to listOf(appHealth),
                    ),
            )
        }
    }

    /**
     * Checks the health of the database connection.
     *
     * This method performs a simple query to verify database connectivity
     * and measures the response time. It returns detailed health information
     * including response time and any error messages.
     *
     * @return Mono containing the database health check result
     */
    private fun checkDatabaseHealth(): Mono<HealthCheck> {
        val startTime = Instant.now()

        return postgresClient.select<Map<String, Any>>("SELECT 1 as health_check")
            .map {
                val responseTime = Duration.between(startTime, Instant.now()).toMillis()
                logger.debug("Database health check passed in {}ms", responseTime)

                HealthCheck(
                    componentId = "postgres",
                    componentType = "database",
                    observedValue = responseTime,
                    observedUnit = "ms",
                    status = HealthStatus.PASS,
                    output = "Database connection successful",
                    links =
                        mapOf(
                            "self" to "/health",
                        ),
                    time = Instant.now()
                )
            }
            .onErrorResume { error ->
                logger.error("Database health check failed", error)
                Mono.just(
                    HealthCheck(
                        componentId = "postgres",
                        componentType = "database",
                        status = HealthStatus.FAIL,
                        output = "Database connection failed: ${error.message}",
                        links =
                            mapOf(
                                "self" to "/health",
                            ),
                        time = Instant.now()
                    ),
                )
            }
    }

    /**
     * Checks the health of the Keycloak authentication service.
     *
     * This method performs a health check by calling Keycloak's health endpoint
     * to verify that the authentication service is accessible and responsive.
     * It uses the health endpoint that Keycloak provides for monitoring.
     *
     * @return Mono containing the Keycloak health check result
     */
    private fun checkKeycloakHealth(): Mono<HealthCheck> {
        val startTime = Instant.now()

        return keycloakClient.checkHealthLive()
            .map {
                val responseTime = Duration.between(startTime, Instant.now()).toMillis()
                logger.debug("Keycloak health check passed in {}ms", responseTime)

                HealthCheck(
                    componentId = "keycloak",
                    componentType = "auth",
                    observedValue = responseTime,
                    observedUnit = "ms",
                    status = HealthStatus.PASS,
                    output = "Keycloak connection successful",
                    links = mapOf("self" to "/health"),
                    time = Instant.now()
                )
            }
            .onErrorResume { error ->
                logger.error("Keycloak health check failed: {}", error.message)

                Mono.just(
                    HealthCheck(
                        componentId = "keycloak",
                        componentType = "auth",
                        status = HealthStatus.FAIL,
                        output = "Keycloak health check failed: ${error.message}",
                        links = mapOf("self" to "/health"),
                        time = Instant.now()
                    )
                )
            }
    }

    /**
     * Checks the health of the application itself.
     *
     * This method verifies that the application is running and responsive.
     * Since this method is called within the application, a successful
     * execution indicates the application is healthy.
     *
     * @return Mono containing the application health check result
     */
    private fun checkApplicationHealth(): Mono<HealthCheck> {
        return Mono.just(
            HealthCheck(
                componentId = "congen-api",
                componentType = "service",
                status = HealthStatus.PASS,
                output = "Application is running",
                links =
                    mapOf(
                        "self" to "/health",
                    ),
                time = Instant.now()
            ),
        )
    }

    /**
     * Determines the overall health status based on individual component statuses.
     *
     * This method implements a priority-based status determination where:
     * - FAIL status takes precedence over all others
     * - WARN status takes precedence over PASS
     * - PASS is the default when all components are healthy
     *
     * @param statuses List of health statuses from individual components
     * @return Overall health status
     */
    private fun determineOverallStatus(statuses: List<HealthStatus>): HealthStatus {
        return when {
            statuses.any { it == HealthStatus.FAIL } -> HealthStatus.FAIL
            statuses.any { it == HealthStatus.WARN } -> HealthStatus.WARN
            else -> HealthStatus.PASS
        }
    }
}
