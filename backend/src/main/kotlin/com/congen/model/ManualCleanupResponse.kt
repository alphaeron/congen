package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response for manual cleanup operations.
 *
 * @property cleanupResults List of cleanup results by data type
 * @property summary Summary information about the cleanup operation
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Response for manual cleanup operations")
data class ManualCleanupResponse(
    @Schema(description = "List of cleanup results by data type")
    @param:JsonProperty("cleanup_results") val cleanupResults: List<DataCleanupResult>,
    @Schema(description = "Summary information about the cleanup operation")
    @param:JsonProperty("summary") val summary: CleanupSummary
)
