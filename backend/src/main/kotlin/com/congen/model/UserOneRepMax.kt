package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant

/**
 * Represents a user's one rep max (1RM) for a specific exercise.
 *
 * This model allows users to track their maximum weight for different exercises,
 * which is used for workout generation and progression calculations.
 *
 * @param userId The ID of the user
 * @param exerciseName The name of the exercise
 * @param oneRepMax The one rep max weight value
 * @param updatedAt Timestamp when the one rep max was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's one rep max for a specific exercise.")
data class UserOneRepMax(
    /** ID of the user (Keycloak ID). */
    @Schema(description = "ID of the user (Keycloak ID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @param:JsonProperty("user_id") val userId: String,
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press", required = true)
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** The one rep max weight in kilograms. */
    @Schema(description = "The one rep max weight in kilograms", example = "100.0", required = true)
    @param:JsonProperty("one_rep_max") val oneRepMax: BigDecimal,
    /** Timestamp when the 1RM was last updated. */
    @Schema(description = "Timestamp when the 1RM was last updated", example = "2024-01-01T00:00:00Z", required = true)
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
