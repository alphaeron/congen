package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's preference for a specific exercise.
 *
 * This model allows users to indicate exercises they want to avoid or prefer.
 *
 * @property userId The ID of the user
 * @property exerciseName The name of the exercise
 * @property shouldAvoid Whether the user should avoid this exercise
 * @property createdAt Timestamp when the preference was created
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's preference for a specific exercise.")
data class UserExercisePreference(
    /** The ID of the user */
    @Schema(description = "The ID of the user", example = "1", required = true)
    @param:JsonProperty("user_id") val userId: Int,
    /** The name of the exercise */
    @Schema(description = "The name of the exercise", example = "Bench Press", required = true)
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Whether the user should avoid this exercise */
    @Schema(description = "Whether the user should avoid this exercise", example = "false", required = true)
    @param:JsonProperty("should_avoid") val shouldAvoid: Boolean,
    /** Timestamp when the preference was created */
    @Schema(description = "Timestamp when the preference was created", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
)
