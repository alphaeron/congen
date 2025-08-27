package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Individual estimated deletion for a specific data type.
 *
 * @property dataType The type of data
 * @property estimatedRecordsToDelete Number of records estimated to be deleted
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Individual estimated deletion for a data type")
data class EstimatedDeletion(
    @Schema(description = "The type of data")
    @param:JsonProperty("data_type") val dataType: String,
    @Schema(description = "Number of records estimated to be deleted")
    @param:JsonProperty("estimated_records_to_delete") val estimatedRecordsToDelete: Int
)
