package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Exercise(
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("description") val description: String,
    @param:JsonProperty("movement_type") val movementType: String,
    @param:JsonProperty("is_unilateral") val isUnilateral: Boolean,
    @param:JsonProperty("is_upper") val isUpper: Boolean,
    @param:JsonProperty("is_accessory") val isAccessory: Boolean,
) 