package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents the history of exercise rotations for users.
 *
 * This entity tracks when exercises were last used in workouts for each user,
 * allowing the system to implement exercise rotation to prevent accommodation
 * and ensure variety in programming.
 *
 * @property id Unique identifier for the exercise rotation history
 * @property userId The ID of the user
 * @property exerciseName The name of the exercise
 * @property isAccessory Whether the exercise is an accessory movement
 * @property createdAt Timestamp when the rotation history was created
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "Exercise rotation history for tracking exercise usage patterns",
)
data class ExerciseRotationHistory(
    /** Unique identifier for the exercise rotation history record (auto-generated). */
    @Schema(
        description = "Unique identifier for the exercise rotation history record",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1", required = true)
    @param:JsonProperty("user_id") val userId: Int,
    /** The name of the exercise that was used. */
    @Schema(
        description = "The name of the exercise that was used",
        example = "Bench Press",
        minLength = 1,
        maxLength = 255,
        required = true,
    )
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Whether the exercise is an accessory movement. */
    @Schema(description = "Whether the exercise is an accessory movement", example = "false")
    @param:JsonProperty("is_accessory")
    @get:JsonProperty("is_accessory") val isAccessory: Boolean,
    /** Timestamp when the exercise was used. */
    @Schema(
        description = "Timestamp when the exercise was used",
        example = "2024-01-01T00:00:00Z",
        required = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
)
