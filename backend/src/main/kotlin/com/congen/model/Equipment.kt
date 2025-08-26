package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a piece of equipment available for exercises.
 *
 * Equipment is used to perform various exercises and can include items such as barbells, dumbbells, machines, etc.
 *
 * @param name Name of the equipment (e.g., "Barbell").
 * @param description Description of the equipment and its use.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a piece of equipment available for exercises.")
data class Equipment(
    /** Name of the equipment (e.g., "Barbell"). */
    @Schema(description = "Name of the equipment", example = "Barbell", required = true)
    @param:JsonProperty("name") val name: String,
    /** Description of the equipment and its use. */
    @Schema(description = "Description of the equipment", example = "A long bar used for weightlifting.", required = true)
    @param:JsonProperty("description") val description: String,
)
