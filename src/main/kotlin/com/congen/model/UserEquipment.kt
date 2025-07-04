package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the relationship between a user and a piece of equipment they have access to.
 *
 * This model links users to the equipment available to them for workouts.
 *
 * @property userId ID of the user.
 * @property equipmentName Name of the equipment (e.g., "Barbell").
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the relationship between a user and a piece of equipment they have access to.")
data class UserEquipment(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1")
    @param:JsonProperty("user_id") val userId: Int,
    /** Name of the equipment (e.g., "Barbell"). */
    @Schema(description = "Name of the equipment", example = "Barbell")
    @param:JsonProperty("equipment_name") val equipmentName: String,
)
