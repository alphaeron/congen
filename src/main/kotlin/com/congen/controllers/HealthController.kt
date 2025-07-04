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

@RestController
@RequestMapping("/health")
class HealthController(
    private val healthCheckService: HealthCheckService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(HealthController::class.java)
    }

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
