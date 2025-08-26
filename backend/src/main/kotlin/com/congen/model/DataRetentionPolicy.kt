package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data class representing a data retention policy.
 *
 * @param dataType The type of data the policy applies to
 * @param retentionPeriodDays How long to retain the data in days
 * @param description Optional description of the policy
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Data retention policy")
data class DataRetentionPolicy(
    @Schema(description = "The type of data the policy applies to")
    @param:JsonProperty("data_type") val dataType: String,
    @Schema(description = "How long to retain the data in days")
    @param:JsonProperty("retention_period_days") val retentionPeriodDays: Int,
    @Schema(description = "Optional description of the policy")
    @param:JsonProperty("description") val description: String?
)
