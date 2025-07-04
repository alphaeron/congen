package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgrammedExercise(
    @param:JsonProperty("id") val id: Long? = null,
    @param:JsonProperty("workout_stage_id") val workoutStageId: Long,
    @param:JsonProperty("exercise_name") val exerciseName: String,
    @param:JsonProperty("notes") val notes: String?,
)
