package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a muscle group targeted by exercises.
 *
 * Muscles are used to categorize exercises and track which areas of the body are being worked.
 *
 * @property name Name of the muscle group (e.g., "Chest").
 * @property description Description of the muscle group and its anatomical details.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a muscle group targeted by exercises.")
data class Muscle(
    /** Name of the muscle group (e.g., "Chest"). */
    @Schema(description = "Name of the muscle group", example = "Chest", required = true)
    @param:JsonProperty("name") val name: String,
    /** Description of the muscle group and its anatomical details. */
    @Schema(description = "Description of the muscle group", example = "Pectoralis major and minor.", required = true)
    @param:JsonProperty("description") val description: String,
)
