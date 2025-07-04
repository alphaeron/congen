package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExerciseWorkoutType(
    @param:JsonProperty("exercise_name") val exerciseName: String,
    @param:JsonProperty("movement_type") val movementType: String,
    @param:JsonProperty("workout_type") val workoutType: String,
)
