package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a user's one rep max (1RM) for a specific exercise.
 *
 * This model allows users to track their maximum weight for different exercises,
 * which is used for workout generation and progression calculations.
 *
 * @property userId ID of the user.
 * @property exerciseName Name of the exercise (e.g., "Bench Press").
 * @property oneRepMax The one rep max weight in kilograms.
 * @property lastUpdated Timestamp when the 1RM was last updated.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's one rep max (1RM) for a specific exercise.")
data class UserOneRepMax(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1")
    @param:JsonProperty("user_id") val userId: Int,
    /** Name of the exercise (e.g., "Bench Press"). */
    @Schema(description = "Name of the exercise", example = "Bench Press")
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** The one rep max weight in kilograms. */
    @Schema(description = "The one rep max weight in kilograms", example = "100.0")
    @param:JsonProperty("one_rep_max") val oneRepMax: BigDecimal,
    /** Timestamp when the 1RM was last updated. */
    @Schema(description = "Timestamp when the 1RM was last updated", example = "2024-01-01T00:00:00Z")
    @param:JsonProperty("last_updated") val lastUpdated: LocalDateTime? = null,
)
