package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a stage within a programmed workout.
 *
 * A workout stage is a component of a programmed workout that groups related exercises
 * together. Each stage has a specific type (warm-up, main, cool-down, etc.) and position
 * within the workout sequence. Stages help organize workouts into logical phases and
 * provide structure for exercise progression.
 *
 * ## Usage
 *
 * Workout stages are used to:
 * - Organize exercises into logical groups within a workout
 * - Define the sequence and flow of a workout
 * - Categorize exercises by their purpose (warm-up, main, accessory, etc.)
 * - Enable structured workout generation and presentation
 *
 * ## Relationships
 *
 * - **ProgrammedWorkout**: Each stage belongs to a specific programmed workout
 * - **WorkoutStageType**: Each stage has a type that defines its purpose
 * - **ProgrammedExercise**: Stages contain multiple programmed exercises
 *
 * @property id Unique identifier for the workout stage
 * @property programmedWorkoutId ID of the programmed workout this stage belongs to
 * @property stageTypeId ID of the workout stage type (warm-up, main, cool-down, etc.)
 * @property position Order of this stage within the workout (1-based)
 * @property name Name of the workout stage
 * @property createdAt Created at timestamp
 * @property updatedAt Updated at timestamp
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A stage within a programmed workout",
    example = "WorkoutStage(id=1, programmedWorkoutId=5, stageTypeId=1, position=1)",
)
data class WorkoutStage(
    /** Unique identifier for the workout stage. */
    @Schema(
        description = "Unique identifier for the workout stage",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the programmed workout this stage belongs to. */
    @Schema(
        description = "ID of the programmed workout this stage belongs to",
        example = "5",
        required = true,
    )
    @param:JsonProperty("programmed_workout_id") val programmedWorkoutId: Long,
    /** ID of the workout stage type (warm-up, main, cool-down, etc.). */
    @Schema(
        description = "ID of the workout stage type (warm-up, main, cool-down, etc.)",
        example = "1",
        required = true,
    )
    @param:JsonProperty("stage_type_id") val stageTypeId: Int,
    /** Order of this stage within the workout (1-based). */
    @Schema(
        description = "Order of this stage within the workout (1-based)",
        example = "1",
        required = true,
        minimum = "1",
    )
    @param:JsonProperty("position") val position: Int,
    @Schema(description = "Name of the workout stage", example = "Warm-up", required = true)
    @param:JsonProperty("name") val name: String,
    @Schema(description = "Created at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("created_at") val createdAt: Instant,
    @Schema(description = "Updated at timestamp", example = "2024-07-06T12:00:00Z", required = true)
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
