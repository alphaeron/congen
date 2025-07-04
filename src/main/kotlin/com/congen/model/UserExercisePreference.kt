package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserExercisePreference(
    @param:JsonProperty("user_id") val userId: Int,
    @param:JsonProperty("exercise_name") val exerciseName: String,
    @param:JsonProperty("should_avoid") val shouldAvoid: Boolean,
)
