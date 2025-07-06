package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Represents the history of exercise rotations for users in the workout generation system.
 *
 * This entity tracks when users have used specific exercises in their workout programs,
 * categorized by exercise type (primary/secondary vs accessory). This information
 * is used to ensure exercise variety and prevent overuse of the same exercises.
 *
 * ## Validation Rules
 *
 * - **ExerciseName**: Required, must reference an existing exercise
 * - **IsAccessory**: Required, indicates if the exercise was used as an accessory movement
 *
 * ## Usage
 *
 * Exercise rotation history is automatically created when users complete workouts
 * and can be queried to determine exercise variety and usage patterns.
 *
 * @property id Unique identifier for the exercise rotation history record (auto-generated)
 * @property exerciseName The name of the exercise that was used
 * @property isAccessory Whether the exercise was used as an accessory movement
 * @property usedAt Timestamp when the exercise was used
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
    /** The name of the exercise that was used. */
    @Schema(
        description = "The name of the exercise that was used",
        example = "Bench Press",
        minLength = 1,
        maxLength = 255,
    )
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Whether the exercise was used as an accessory movement. */
    @Schema(
        description = "Whether the exercise was used as an accessory movement",
        example = "false",
    )
    @param:JsonProperty("is_accessory") val isAccessory: Boolean,
    /** Timestamp when the exercise was used. */
    @Schema(
        description = "Timestamp when the exercise was used",
        example = "2024-01-01T00:00:00Z",
        readOnly = true,
    )
    @param:JsonProperty("used_at") val usedAt: LocalDateTime? = null,
)
