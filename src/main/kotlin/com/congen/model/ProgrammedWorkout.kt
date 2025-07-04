package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgrammedWorkout(
    @param:JsonProperty("id") val id: Long? = null,
    @param:JsonProperty("program_id") val programId: Long,
    @param:JsonProperty("day_number") val dayNumber: Int,
    @param:JsonProperty("name") val name: String?,
)
