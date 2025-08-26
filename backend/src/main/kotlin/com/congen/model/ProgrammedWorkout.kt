package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a scheduled workout within a program.
 *
 * A programmed workout is a specific workout that is scheduled for a particular day
 * within a training program. It contains multiple workout stages that organize the
 * exercises and provide structure for the training session.
 *
 * ## Usage
 *
 * Programmed workouts are used to:
 * - Schedule specific workouts within a training program
 * - Organize training sessions by day number
 * - Group related workout stages together
 * - Provide structure for multi-day training programs
 *
 * ## Relationships
 *
 * - **Program**: Each programmed workout belongs to a specific training program
 * - **WorkoutStage**: Programmed workouts contain multiple workout stages
 * - **ProgrammedExercise**: Stages contain multiple programmed exercises
 *
 * ## Day Number
 *
 * The day number indicates the position of this workout within the program cycle.
 * For example, in a 3-day program:
 * - Day 1: First workout of the cycle
 * - Day 2: Second workout of the cycle
 * - Day 3: Third workout of the cycle
 *
 * @param id Unique identifier for the programmed workout
 * @param programId ID of the training program this workout belongs to
 * @param dayNumber Day number within the program (1-365)
 * @param name Optional name for the workout
 * @param createdAt Timestamp when the workout was created
 * @param updatedAt Timestamp when the workout was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A scheduled workout within a training program",
    example = "ProgrammedWorkout(id=1, programId=5, dayNumber=1, name=\"Upper Body Strength\")",
)
data class ProgrammedWorkout(
    /** Unique identifier for the programmed workout. */
    @Schema(
        description = "Unique identifier for the programmed workout",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the training program this workout belongs to. */
    @Schema(
        description = "ID of the training program this workout belongs to",
        example = "5",
        required = true,
    )
    @param:JsonProperty("program_id") val programId: Long,
    /** Day number within the program (1-365). */
    @Schema(
        description = "Day number within the program (1-365)",
        example = "1",
        required = true,
    )
    @param:JsonProperty("day_number") val dayNumber: Int,
    /** Name for the workout. */
    @Schema(
        description = "Name for the workout",
        example = "Upper Body Strength",
        required = true,
    )
    @param:JsonProperty("name") val name: String,
    @Schema(description = "Created at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
    @Schema(description = "Updated at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
