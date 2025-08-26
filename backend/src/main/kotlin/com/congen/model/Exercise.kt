package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents an exercise that can be included in a workout.
 *
 * Exercises are the core building blocks of workouts and can be categorized by movement type, body part, and other attributes.
 *
 * @param name Name of the exercise (e.g., "Bench Press").
 * @param description Description of the exercise and its purpose.
 * @param movementType Type of movement (e.g., "horizontal_push").
 * @param isUnilateral Whether the exercise is performed one side at a time.
 * @param isUpper Whether the exercise targets the upper body.
 * @param isAccessory Whether the exercise is considered an accessory movement.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents an exercise that can be included in a workout.")
data class Exercise(
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press")
    @param:JsonProperty("name") val name: String,
    /** Description of the exercise and its purpose. */
    @Schema(description = "Description of the exercise", example = "A compound upper body exercise.")
    @param:JsonProperty("description") val description: String,
    /** Type of movement (e.g., "horizontal_push"). */
    @Schema(description = "Type of movement", example = "horizontal_push")
    @param:JsonProperty("movement_type") val movementType: MovementType,
    /** Whether the exercise is performed one side at a time. */
    @Schema(description = "Whether the exercise is unilateral (one side at a time)", example = "false")
    @param:JsonProperty("is_unilateral")
    @get:JsonProperty("is_unilateral") val isUnilateral: Boolean,
    /** Whether the exercise targets the upper body. */
    @Schema(description = "Whether the exercise targets upper body", example = "true")
    @param:JsonProperty("is_upper")
    @get:JsonProperty("is_upper") val isUpper: Boolean,
    /** Whether the exercise is considered an accessory movement. */
    @Schema(description = "Whether the exercise is an accessory movement", example = "false")
    @param:JsonProperty("is_accessory")
    @get:JsonProperty("is_accessory") val isAccessory: Boolean,
)
