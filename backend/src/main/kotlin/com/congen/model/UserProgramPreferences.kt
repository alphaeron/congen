package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's program preferences.
 *
 * This entity stores user preferences for workout programs, including workout
 * frequency, duration, and other program-related settings. These preferences
 * are used to generate personalized workout programs.
 *
 * @property userId The ID of the user
 * @property programDaysPerWeek The number of days per week for the program
 * @property sessionTimeLengthInMinutes The session time length in minutes
 * @property createdAt Timestamp when the preferences were created
 * @property updatedAt Timestamp when the preferences were last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's program preferences, such as days per week and session length.")
data class UserProgramPreferences(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1", required = true)
    @param:JsonProperty("user_id") val userId: Int,
    /** Number of program days per week. */
    @Schema(description = "Number of program days per week", example = "4", required = true)
    @param:JsonProperty("program_days_per_week") val programDaysPerWeek: Int,
    /** Session time length in minutes. */
    @Schema(description = "Session time length in minutes", example = "60", required = true)
    @param:JsonProperty("session_time_length_in_minutes") val sessionTimeLengthInMinutes: Int,
    /** Timestamp when the user program preferences were created. */
    @Schema(description = "Timestamp when the user program preferences were created", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the user program preferences were last updated. */
    @Schema(
        description = "Timestamp when the user program preferences were last updated",
        example = "2024-07-06T12:00:00Z",
        required = true
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
