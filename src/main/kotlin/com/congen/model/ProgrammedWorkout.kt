package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgrammedWorkout(
    @Schema(
        description = "Unique identifier for the programmed workout",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("program_id") val programId: Long,
    @param:JsonProperty("day_number") val dayNumber: Int,
    @param:JsonProperty("name") val name: String?,
)
