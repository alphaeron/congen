package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Summary information about a cleanup operation.
 *
 * @param totalDeleted Total number of records deleted across all data types
 * @param dataTypesProcessed Number of data types that were processed
 * @param executionTime When the cleanup was executed
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Summary information about a cleanup operation")
data class CleanupSummary(
    @Schema(description = "Total number of records deleted across all data types")
    @param:JsonProperty("total_deleted") val totalDeleted: Int,
    @Schema(description = "Number of data types that were processed")
    @param:JsonProperty("data_types_processed") val dataTypesProcessed: Int,
    @Schema(description = "When the cleanup was executed")
    @param:JsonProperty("execution_time") val executionTime: Instant
)
