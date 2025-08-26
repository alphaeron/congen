package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the relationship between an exercise and a piece of equipment.
 *
 * This model links exercises to the equipment required to perform them.
 *
 * @param exerciseName Name of the exercise (e.g., "Bench Press").
 * @param equipmentName Name of the equipment (e.g., "Barbell").
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the relationship between an exercise and a piece of equipment.")
data class ExerciseEquipment(
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press", required = true)
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Name of the equipment (e.g., "Barbell"). */
    @Schema(description = "Name of the equipment", example = "Barbell", required = true)
    @param:JsonProperty("equipment_name") val equipmentName: String,
)
