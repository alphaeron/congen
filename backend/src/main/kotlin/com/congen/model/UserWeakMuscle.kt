package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's weak muscle group for targeted accessory selection.
 *
 * This model links a user to a muscle group they wish to focus on as a weak point.
 * Used to personalize accessory exercise selection in workout generation.
 *
 * @param userId The ID of the user
 * @param muscleName The name of the weak muscle group
 * @param createdAt Timestamp when the weak muscle was set
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's weak muscle group for targeted accessory selection.")
data class UserWeakMuscle(
    /** ID of the user (Keycloak ID). */
    @Schema(description = "ID of the user (Keycloak ID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @param:JsonProperty("user_id") val userId: String,
    /** The name of the weak muscle group */
    @Schema(description = "The name of the weak muscle group", example = "Hamstrings", required = true)
    @param:JsonProperty("muscle_name") val muscleName: String,
    /** Timestamp when the weak muscle was set */
    @Schema(description = "Timestamp when the weak muscle was set", example = "2024-07-17T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
)
