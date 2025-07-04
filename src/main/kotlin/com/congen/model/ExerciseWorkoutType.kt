package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the relationship between an exercise, its movement type, and workout type.
 *
 * This model links exercises to their movement and workout types for categorization and filtering.
 *
 * @property exerciseName Name of the exercise (e.g., "Bench Press").
 * @property movementType Type of movement (e.g., "horizontal push").
 * @property workoutType Type of workout (e.g., "dynamic_effort").
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the relationship between an exercise, its movement type, and workout type.")
data class ExerciseWorkoutType(
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press")
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Type of movement (e.g., "horizontal push"). */
    @Schema(description = "Type of movement", example = "horizontal push")
    @param:JsonProperty("movement_type") val movementType: String,
    /** Type of workout (e.g., "dynamic_effort"). */
    @Schema(description = "Type of workout", example = "dynamic_effort")
    @param:JsonProperty("workout_type") val workoutType: String,
)
