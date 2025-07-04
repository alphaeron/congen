package com.congen.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Top-level health check response object following RFC specification.
 * https://datatracker.ietf.org/doc/html/draft-inadarei-api-health-check-06
 *
 * @property status Overall health status.
 * @property version API version.
 * @property releaseId Release identifier.
 * @property notes Additional notes.
 * @property output Output message.
 * @property checks Map of checks by component.
 * @property links Related links.
 * @property serviceId Service identifier.
 * @property description Description of the health check response.
 */
@Schema(description = "Top-level health check response object.")
data class HealthCheckResponse(
    /** Overall health status. */
    @Schema(description = "Overall health status.")
    val status: HealthStatus,
    /** API version. */
    @Schema(description = "API version.")
    val version: String,
    /** Release identifier. */
    @Schema(description = "Release identifier.")
    val releaseId: String,
    /** Additional notes. */
    @Schema(description = "Additional notes.")
    val notes: List<String> = emptyList(),
    /** Output message. */
    @Schema(description = "Output message.")
    val output: String? = null,
    /** Map of checks by component. */
    @Schema(description = "Map of checks by component.")
    val checks: Map<String, List<HealthCheck>> = emptyMap(),
    /** Related links. */
    @Schema(description = "Related links.")
    val links: Map<String, String> = emptyMap(),
    /** Service identifier. */
    @Schema(description = "Service identifier.")
    @JsonProperty("serviceId")
    val serviceId: String = "congen",
    /** Description of the health check response. */
    @Schema(description = "Description of the health check response.")
    val description: String = "Congen Exercise API Health Check",
)

/**
 * Possible health status values for a health check.
 */
@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy::class)
@Schema(description = "Possible health status values.")
enum class HealthStatus {
    /** Healthy. */
    PASS, // healthy
    /** Degraded. */
    WARN, // degraded
    /** Unhealthy. */
    FAIL, // unhealthy
}

/**
 * Represents the health check for a specific component.
 *
 * @property componentId Component identifier.
 * @property componentType Type of component.
 * @property observedValue Observed value.
 * @property observedUnit Observed unit.
 * @property status Status of the component.
 * @property affectedEndpoints Endpoints affected by this component.
 * @property time Timestamp of the check.
 * @property output Output message.
 * @property links Related links.
 */
@Schema(description = "Represents the health check for a specific component.")
data class HealthCheck(
    /** Component identifier. */
    @Schema(description = "Component identifier.")
    val componentId: String? = null,
    /** Type of component. */
    @Schema(description = "Type of component.")
    val componentType: String? = null,
    /** Observed value. */
    @Schema(description = "Observed value.")
    val observedValue: Any? = null,
    /** Observed unit. */
    @Schema(description = "Observed unit.")
    val observedUnit: String? = null,
    /** Status of the component. */
    @Schema(description = "Status of the component.")
    val status: HealthStatus,
    /** Endpoints affected by this component. */
    @Schema(description = "Endpoints affected by this component.")
    val affectedEndpoints: List<String> = emptyList(),
    /** Timestamp of the check. */
    @Schema(description = "Timestamp of the check.")
    val time: String = Instant.now().toString(),
    /** Output message. */
    @Schema(description = "Output message.")
    val output: String? = null,
    /** Related links. */
    @Schema(description = "Related links.")
    val links: Map<String, String> = emptyMap(),
)

/**
 * Represents the health check for the database.
 *
 * @property status Status of the database.
 * @property responseTime Response time in milliseconds.
 * @property error Error message, if any.
 * @property details Additional details.
 */
@Schema(description = "Represents the health check for the database.")
data class DatabaseHealthCheck(
    /** Status of the database. */
    @Schema(description = "Status of the database.")
    val status: HealthStatus,
    /** Response time in milliseconds. */
    @Schema(description = "Response time in milliseconds.")
    val responseTime: Long? = null,
    /** Error message, if any. */
    @Schema(description = "Error message, if any.")
    val error: String? = null,
    /** Additional details. */
    @Schema(description = "Additional details.")
    val details: Map<String, Any> = emptyMap(),
)
