package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data class representing the result of a cleanup operation or estimation.
 *
 * @param dataType The type of data that was processed
 * @param count Number of records (deleted for cleanup, estimated for estimation)
 * @param operationType Type of operation (CLEANUP or ESTIMATION)
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Result of a cleanup operation or estimation")
data class DataCleanupResult(
    @Schema(description = "The type of data that was processed")
    @param:JsonProperty("data_type") val dataType: String,
    @Schema(description = "Number of records (deleted for cleanup, estimated for estimation)")
    @param:JsonProperty("count") val count: Int,
    @Schema(description = "Type of operation performed")
    @param:JsonProperty("operation_type") val operationType: String = "CLEANUP"
)
