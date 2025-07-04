package com.congen.service

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

@Service
class HealthCheckService(
    private val postgresClient: PostgresClient,
    private val versionConfig: VersionConfig,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(HealthCheckService::class.java)
    }

    fun performHealthCheck(): Mono<HealthCheckResponse> {
        logger.debug("Performing health check")

        return Mono.zip(
            checkDatabaseHealth(),
            checkApplicationHealth(),
        ).map { tuple ->
            val dbHealth = tuple.t1
            val appHealth = tuple.t2
            val overallStatus = determineOverallStatus(listOf(dbHealth.status, appHealth.status))

            HealthCheckResponse(
                status = overallStatus,
                version = versionConfig.version,
                releaseId = versionConfig.releaseId,
                checks =
                    mapOf(
                        "database" to listOf(dbHealth),
                        "application" to listOf(appHealth),
                    ),
            )
        }.onErrorReturn(
            HealthCheckResponse(
                status = HealthStatus.FAIL,
                version = versionConfig.version,
                releaseId = versionConfig.releaseId,
                output = "Health check failed with error",
                checks = emptyMap(),
            ),
        )
    }

    private fun checkDatabaseHealth(): Mono<HealthCheck> {
        val startTime = Instant.now()

        return postgresClient.select<Map<String, Any>>("SELECT 1 as health_check")
            .map { result ->
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
                    ),
                )
            }
    }

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
            ),
        )
    }

    private fun determineOverallStatus(statuses: List<HealthStatus>): HealthStatus {
        return when {
            statuses.any { it == HealthStatus.FAIL } -> HealthStatus.FAIL
            statuses.any { it == HealthStatus.WARN } -> HealthStatus.WARN
            else -> HealthStatus.PASS
        }
    }
}
