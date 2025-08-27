package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a type or category of workout stage.
 *
 * Workout stage types define the different categories of stages that can be part of a workout,
 * such as warm-up, main exercises, cool-down, or accessory work. These types help organize
 * and structure workouts by providing clear categorization for different phases of training.
 *
 * ## Usage
 *
 * Workout stage types are used to:
 * - Categorize different phases of a workout
 * - Organize exercises by their purpose in the workout
 * - Provide structure for workout generation algorithms
 * - Enable filtering and grouping of workout components
 *
 * ## Examples
 *
 * Common workout stage types include:
 * - **Warm-up**: Preparatory exercises to increase body temperature and mobility
 * - **Primary**: Primary strength or conditioning exercises
 * - **Secondary**: Supporting compound movements
 * - **Accessory**: Supplementary exercises for muscle balance and development
 * - **Cool-down**: Recovery exercises to reduce heart rate and promote flexibility
 * - **Mobility**: Flexibility and mobility work
 * - **Conditioning**: Cardio and conditioning work
 *
 * @property id Unique identifier for the workout stage type
 * @property name The name of the workout stage
 * @property createdAt Created at timestamp
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A type or category of workout stage",
    example = "WorkoutStageType(id=1, name=WARMUP)",
)
data class WorkoutStageType(
    /** Unique identifier for the workout stage type. */
    @Schema(
        description = "Unique identifier for the workout stage type",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Int,
    /** The name of the workout stage type. */
    @Schema(
        description = "The name of the workout stage type",
        example = "WARMUP",
        required = true,
    )
    @param:JsonProperty("name") val name: WorkoutStageTypeEnum,
    @Schema(
        description = "Created at timestamp",
        example = "2024-07-06T12:00:00Z",
        required = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
)
