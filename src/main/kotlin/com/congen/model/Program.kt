package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Represents a training program.
 *
 * A program is a structured training plan that contains multiple programmed workouts
 * organized by day number. Programs provide the framework for systematic training
 * and can be customized based on user preferences and goals.
 *
 * ## Usage
 *
 * Programs are used to:
 * - Organize multiple workouts into a cohesive training plan
 * - Provide structure for systematic training progression
 * - Enable program generation based on user preferences
 * - Support different training frequencies (2, 3, or 4 days per week)
 *
 * ## Program Structure
 *
 * Programs contain:
 * - **ProgrammedWorkouts**: Multiple workouts scheduled for specific days
 * - **WorkoutStages**: Each workout contains multiple stages (warm-up, main, cool-down)
 * - **ProgrammedExercises**: Each stage contains multiple exercises
 * - **SetSchemes**: Each exercise contains multiple sets with specific parameters
 *
 * ## Training Frequency
 *
 * Programs support different training frequencies:
 * - **2-day programs**: Two workouts per week
 * - **3-day programs**: Three workouts per week (most common)
 * - **4-day programs**: Four workouts per week
 *
 * @property id Unique identifier for the program
 * @property userId ID of the user who owns this program
 * @property name Human-readable name of the program
 * @property description Optional description of the program
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A training program containing multiple workouts",
    example = "Program(id=1, userId=1, name=\"Beginner Strength Program\", description=\"A 3-day strength program for beginners\")",
)
data class Program(
    /** Unique identifier for the program. */
    @Schema(
        description = "Unique identifier for the program",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the user who owns this program. */
    @Schema(
        description = "ID of the user who owns this program",
        example = "1",
        required = true,
    )
    @param:JsonProperty("user_id") val userId: Int,
    /** Human-readable name of the program. */
    @Schema(
        description = "Human-readable name of the program",
        example = "Beginner Strength Program",
        required = true,
    )
    @param:JsonProperty("name") val name: String,
    @Schema(description = "Current week number", example = "1", required = true)
    @param:JsonProperty("current_week_number") val currentWeekNumber: Int,
    @Schema(description = "Created at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: LocalDateTime,
    @Schema(description = "Updated at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("updated_at") val updatedAt: LocalDateTime,
)
