package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents an exercise assigned to a specific workout stage.
 *
 * A programmed exercise is an exercise that has been assigned to a particular workout stage
 * within a workout. It includes the exercise to be performed and optional notes or instructions
 * for the exercise execution.
 *
 * ## Usage
 *
 * Programmed exercises are used to:
 * - Assign specific exercises to workout stages
 * - Provide exercise-specific instructions or notes
 * - Organize exercises within the workout structure
 * - Enable detailed workout planning and execution
 *
 * ## Relationships
 *
 * - **WorkoutStage**: Each programmed exercise belongs to a specific workout stage
 * - **Exercise**: References the exercise to be performed by name
 * - **SetScheme**: Programmed exercises contain multiple set schemes
 *
 * ## Notes
 *
 * The notes field allows for exercise-specific instructions, modifications, or
 * coaching cues that are relevant to this particular instance of the exercise
 * within the workout.
 *
 * @param id Unique identifier for the programmed exercise
 * @param workoutStageId ID of the workout stage this exercise belongs to
 * @param exerciseName Name of the exercise to be performed
 * @param position Position of the exercise within the stage
 * @param notes Optional notes or instructions for the exercise
 * @param createdAt Created at timestamp
 * @param updatedAt Updated at timestamp
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "An exercise assigned to a specific workout stage",
    example = "ProgrammedExercise(id=1, workoutStageId=5, exerciseName=\"Bench Press\", notes=\"Focus on controlled descent\")",
)
data class ProgrammedExercise(
    /** Unique identifier for the programmed exercise. */
    @Schema(
        description = "Unique identifier for the programmed exercise",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the workout stage this exercise belongs to. */
    @Schema(
        description = "ID of the workout stage this exercise belongs to",
        example = "5",
        required = true,
    )
    @param:JsonProperty("workout_stage_id") val workoutStageId: Long,
    /** Name of the exercise to be performed. */
    @Schema(
        description = "Name of the exercise to be performed",
        example = "Bench Press",
        required = true,
    )
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Position of the exercise within the stage. */
    @Schema(
        description = "Position of the exercise within the stage",
        example = "1",
        required = true,
    )
    @param:JsonProperty("position") val position: Int,
    /** Optional notes or instructions for the exercise. */
    @Schema(
        description = "Optional notes or instructions for the exercise",
        example = "Focus on controlled descent",
    )
    @param:JsonProperty("notes") val notes: String?,
    /** Created at timestamp. */
    @Schema(
        description = "Created at timestamp",
        example = "2024-07-06T12:00:00Z",
        required = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Updated at timestamp. */
    @Schema(
        description = "Updated at timestamp",
        example = "2024-07-06T12:00:00Z",
        required = true,
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
