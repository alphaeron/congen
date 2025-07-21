package com.congen.controllers

import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import com.congen.service.HealthCheckService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for health check endpoints.
 *
 * This controller provides health check functionality for the Congen API,
 * allowing monitoring systems and load balancers to verify the application's
 * health status. The health check evaluates the application and its dependencies
 * and returns detailed status information.
 *
 * ## Health Check Response
 *
 * The health check returns a comprehensive response including:
 * - Overall health status (PASS, WARN, FAIL)
 * - Individual component health information
 * - Application version and release information
 * - Response times and error details
 *
 * ## Status Mapping
 *
 * - **PASS**: HTTP 200 OK - Application is healthy
 * - **WARN**: HTTP 200 OK - Application is healthy but with warnings
 * - **FAIL**: HTTP 503 Service Unavailable - Application is unhealthy
 *
 * ## Endpoints
 *
 * - `GET /health/` - Performs comprehensive health check
 *
 * @property healthCheckService Service for performing health checks
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/health")
class HealthController(
    private val healthCheckService: HealthCheckService,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(HealthController::class.java)
    }

    /**
     * Performs a comprehensive health check of the application and its dependencies.
     *
     * This endpoint evaluates the health of all monitored components including
     * the application itself and the database connection. The response includes
     * detailed status information for each component and an overall health status.
     *
     * The health check is designed to be used by:
     * - Load balancers for health monitoring
     * - Monitoring systems for alerting
     * - DevOps tools for deployment verification
     * - Manual verification during troubleshooting
     *
     * @return Mono containing the health check response with appropriate HTTP status
     */
    @GetMapping("/")
    @Operation(
        summary = "Health check",
        description = "Performs a health check of the application and its dependencies.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Service is healthy or warning",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = com.congen.model.HealthCheckResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "503",
                description = "Service is unavailable",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = com.congen.model.HealthCheckResponse::class),
                    ),
                ],
            ),
        ],
    )
    fun healthCheck(): Mono<ResponseEntity<HealthCheckResponse>> {
        logger.debug("Health check requested")

        return healthCheckService.performHealthCheck()
            .map { healthResponse ->
                val statusCode =
                    when (healthResponse.status) {
                        HealthStatus.PASS -> HttpStatus.OK
                        HealthStatus.WARN -> HttpStatus.OK
                        HealthStatus.FAIL -> HttpStatus.SERVICE_UNAVAILABLE
                    }

                ResponseEntity.status(statusCode).body(healthResponse)
            }
            .doOnSuccess { response ->
                logger.debug("Health check completed with status: {}", response.body?.status)
            }
            .doOnError { error ->
                logger.error("Health check failed", error)
            }
    }
}
