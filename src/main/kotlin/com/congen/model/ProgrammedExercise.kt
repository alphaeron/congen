package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgrammedExercise(
    @Schema(
        description = "Unique identifier for the programmed exercise",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("workout_stage_id") val workoutStageId: Long,
    @param:JsonProperty("exercise_name") val exerciseName: String,
    @param:JsonProperty("notes") val notes: String?,
)
