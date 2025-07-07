package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Represents the relationship between a user and a piece of equipment they have access to.
 *
 * This model links users to the equipment available to them for workouts.
 *
 * @property userId The ID of the user
 * @property equipmentName The name of the equipment
 * @property createdAt Timestamp when the association was created
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the relationship between a user and a piece of equipment they have access to.")
data class UserEquipment(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1", required = true)
    @param:JsonProperty("user_id") val userId: Int,
    /** Name of the equipment (e.g., "Barbell"). */
    @Schema(description = "Name of the equipment", example = "Barbell", required = true)
    @param:JsonProperty("equipment_name") val equipmentName: String,
    /** Timestamp when the user equipment relationship was created. */
    @Schema(description = "Timestamp when the user equipment relationship was created", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: LocalDateTime,
)
