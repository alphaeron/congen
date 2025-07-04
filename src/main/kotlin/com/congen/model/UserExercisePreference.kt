package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a user's preference for a specific exercise.
 *
 * This model allows users to indicate exercises they want to avoid or prefer.
 *
 * @property userId ID of the user.
 * @property exerciseName Name of the exercise (e.g., "Bench Press").
 * @property shouldAvoid Whether the user should avoid this exercise.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's preference for a specific exercise.")
data class UserExercisePreference(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1")
    @param:JsonProperty("user_id") val userId: Int,
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press")
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Whether the user should avoid this exercise. */
    @Schema(description = "Whether the user should avoid this exercise", example = "false")
    @param:JsonProperty("should_avoid") val shouldAvoid: Boolean,
)
