package com.congen.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

/**
 * Health check response following RFC specification
 * https://datatracker.ietf.org/doc/html/draft-inadarei-api-health-check-06
 */
data class HealthCheckResponse(
    val status: HealthStatus,
    val version: String,
    val releaseId: String,
    val notes: List<String> = emptyList(),
    val output: String? = null,
    val checks: Map<String, List<HealthCheck>> = emptyMap(),
    val links: Map<String, String> = emptyMap(),
    @JsonProperty("serviceId")
    val serviceId: String = "congen",
    val description: String = "Congen Exercise API Health Check",
)

@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy::class)
enum class HealthStatus {
    PASS, // healthy
    WARN, // degraded
    FAIL, // unhealthy
}

data class HealthCheck(
    val componentId: String? = null,
    val componentType: String? = null,
    val observedValue: Any? = null,
    val observedUnit: String? = null,
    val status: HealthStatus,
    val affectedEndpoints: List<String> = emptyList(),
    val time: String = Instant.now().toString(),
    val output: String? = null,
    val links: Map<String, String> = emptyMap(),
)

data class DatabaseHealthCheck(
    val status: HealthStatus,
    val responseTime: Long? = null,
    val error: String? = null,
    val details: Map<String, Any> = emptyMap(),
)
