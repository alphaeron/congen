package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a reference exercise configuration for weight estimation.
 *
 * Reference exercises are used as benchmarks for estimating weights of other exercises
 * based on their movement patterns and similarity.
 *
 * @param id Unique identifier for the reference exercise configuration
 * @param referenceType The type of reference lift (e.g., "SQUAT", "BENCH", "DEADLIFT")
 * @param exerciseName The name of the exercise that serves as this reference
 * @param isActive Whether this reference configuration is currently active
 * @param priority Priority order for this reference (lower numbers = higher priority)
 * @param createdAt When this reference was created
 * @param updatedAt When this reference was last updated
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a reference exercise configuration for weight estimation.")
data class ReferenceExercise(
    /** Unique identifier for the reference exercise configuration */
    @Schema(description = "Unique identifier", example = "1")
    @param:JsonProperty("id") val id: Long,
    /** The type of reference lift (e.g., "SQUAT", "BENCH", "DEADLIFT") */
    @Schema(description = "Type of reference lift", example = "SQUAT")
    @param:JsonProperty("reference_type") val referenceType: String,
    /** The name of the exercise that serves as this reference */
    @Schema(description = "Name of the reference exercise", example = "Safety Bar Squat")
    @param:JsonProperty("exercise_name") val exerciseName: String,
    /** Whether this reference configuration is currently active */
    @Schema(description = "Whether this reference is active", example = "true")
    @param:JsonProperty("is_active") val isActive: Boolean,
    /** Priority order for this reference (lower numbers = higher priority) */
    @Schema(description = "Priority order", example = "1")
    @param:JsonProperty("priority") val priority: Int,
    /** When this reference was created */
    @Schema(description = "Creation timestamp")
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** When this reference was last updated */
    @Schema(description = "Last update timestamp")
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
