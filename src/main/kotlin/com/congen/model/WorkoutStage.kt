package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkoutStage(
    @param:JsonProperty("id") val id: Long? = null,
    @param:JsonProperty("programmed_workout_id") val programmedWorkoutId: Long,
    @param:JsonProperty("stage_type_id") val stageTypeId: Int,
    @param:JsonProperty("position") val position: Int,
)
