package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a user's program preferences, such as days per week and session length.
 *
 * This model allows users to specify their preferred workout frequency and session duration.
 *
 * @property userId ID of the user.
 * @property programDaysPerWeek Number of program days per week.
 * @property sessionTimeLengthInMinutes Session time length in minutes.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a user's program preferences, such as days per week and session length.")
data class UserProgramPreferences(
    /** ID of the user. */
    @Schema(description = "ID of the user", example = "1")
    @param:JsonProperty("user_id") val userId: Int,
    /** Number of program days per week. */
    @Schema(description = "Number of program days per week", example = "4")
    @param:JsonProperty("program_days_per_week") val programDaysPerWeek: Int,
    /** Session time length in minutes. */
    @Schema(description = "Session time length in minutes", example = "60")
    @param:JsonProperty("session_time_length_in_minutes") val sessionTimeLengthInMinutes: Int,
)
