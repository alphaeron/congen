package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response for cleanup estimation operations.
 *
 * @property estimatedDeletions List of estimated deletions by data type
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Response for cleanup estimation")
data class CleanupEstimationResponse(
    @Schema(description = "List of estimated deletions by data type")
    @param:JsonProperty("estimated_deletions") val estimatedDeletions: List<EstimatedDeletion>
)
