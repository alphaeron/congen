package com.congen.controllers

import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import com.congen.service.HealthCheckService
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
