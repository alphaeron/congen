package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's weight unit preference for a specific exercise.
 *
 * This model allows users to specify their preferred weight units (kg or lbs)
 * for individual exercises. This enables users to input weights in their
 * preferred units while the system stores all weights internally in kg.
 *
 * ## Usage
 *
 * Users can set different unit preferences for different exercises.
 * For example, they might prefer lbs for bench press but kg for deadlifts.
 * If no preference is set for an exercise, the system will use a default
 * or prompt the user to specify their preference.
 *
 * @param userId The ID of the user
 * @param exerciseName The name of the exercise
 * @param preferredUnit The user's preferred weight unit for this exercise
 * @param createdAt Timestamp when the preference was created
 * @param updatedAt Timestamp when the preference was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's weight unit preference for a specific exercise.")
data class UserWeightUnitPreference(
    /** ID of the user (Keycloak ID). */
    @Schema(description = "ID of the user (Keycloak ID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @param:JsonProperty("user_id") val userId: String,
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press", required = true)
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** The user's preferred weight unit for this exercise. */
    @Schema(description = "The user's preferred weight unit for this exercise", example = "LBS", required = true)
    @param:JsonProperty("preferred_unit") val preferredUnit: WeightUnit,
    /** Timestamp when the preference was created. */
    @Schema(description = "Timestamp when the preference was created", example = "2024-01-01T00:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the preference was last updated. */
    @Schema(description = "Timestamp when the preference was last updated", example = "2024-01-01T00:00:00Z", required = true)
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
