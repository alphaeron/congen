package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the relationship between an exercise and a muscle group.
 *
 * This model links exercises to the muscle groups they target.
 *
 * @property exerciseName Name of the exercise (e.g., "Bench Press").
 * @property muscleName Name of the muscle group (e.g., "Chest").
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the relationship between an exercise and a muscle group.")
data class ExerciseMuscle(
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press")
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Name of the muscle group (e.g., "Chest"). */
    @Schema(description = "Name of the muscle group", example = "Chest")
    @param:JsonProperty("muscle_name") val muscleName: String,
)
