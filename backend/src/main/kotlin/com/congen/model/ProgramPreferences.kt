package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents program preferences for a specific program.
 *
 * Program preferences define the workout frequency and session duration
 * for a specific training program. These preferences are used to generate
 * workouts that match the user's desired training schedule.
 *
 * ## Usage
 *
 * Program preferences are used to:
 * - Define workout frequency (days per week)
 * - Set session duration for workout generation
 * - Customize program structure based on user needs
 * - Ensure consistent workout scheduling
 *
 * ## Relationships
 *
 * - **Program**: Each program preference belongs to a specific training program
 *
 * ## Training Frequency
 *
 * Supported training frequencies:
 * - **2-day programs**: Two workouts per week
 * - **3-day programs**: Three workouts per week (most common)
 * - **4-day programs**: Four workouts per week
 * - **5-day programs**: Five workouts per week
 * - **6-day programs**: Six workouts per week
 *
 * @property programId ID of the program these preferences belong to
 * @property programDaysPerWeek Number of workout days per week
 * @property sessionTimeLengthInMinutes Length of each workout session in minutes
 * @property createdAt Timestamp when the preferences were created
 * @property updatedAt Timestamp when the preferences were last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "Program preferences for workout frequency and duration",
    example = "ProgramPreferences(programId=1, programDaysPerWeek=3, sessionTimeLengthInMinutes=60)",
)
data class ProgramPreferences(
    /** ID of the program these preferences belong to. */
    @Schema(
        description = "ID of the program these preferences belong to",
        example = "1",
        required = true,
    )
    @param:JsonProperty("program_id") val programId: Long,
    /** Number of workout days per week. */
    @Schema(
        description = "Number of workout days per week",
        example = "4",
        required = true,
    )
    @param:JsonProperty("program_days_per_week") val programDaysPerWeek: Int,
    /** Length of each workout session in minutes. */
    @Schema(
        description = "Length of each workout session in minutes",
        example = "60",
        required = true,
    )
    @param:JsonProperty("session_time_length_in_minutes") val sessionTimeLengthInMinutes: Int,
    /** Timestamp when the preferences were created. */
    @Schema(
        description = "Created at timestamp",
        example = "2024-07-06T12:00:00Z",
        required = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the preferences were last updated. */
    @Schema(
        description = "Updated at timestamp",
        example = "2024-07-06T12:00:00Z",
        required = true,
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
