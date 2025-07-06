package com.congen.service.conjugate

/**
 * Data class for day templates.
 */
data class DayTemplate(
    /** The type of day template. */
    val type: String
)

/**
 * Data class for Prilepin guidelines.
 */
data class PrilepinGuidelines(
    /** The intensity range as a percentage of 1RM. */
    val intensityRange: ClosedFloatingPointRange<Double>,
    /** The range of reps per set. */
    val repsPerSetRange: IntRange,
    /** The total number of reps to perform. */
    val totalReps: Int,
    /** The range of rest seconds between sets. */
    val restSeconds: IntRange
)

/**
 * Constants for conjugate workout generation.
 */
object ConjugateConstants {
    /** Default weak muscles for new users */
    val DEFAULT_WEAK_MUSCLES = listOf("hamstrings", "glutes", "upper_back", "core")

    /** Default weight for new users without 1RM data */
    val DEFAULT_WEIGHT = "50.0"

    /** Time allocation for different workout components (in minutes) */
    object TimeAllocation {
        /** Time allocated for primary movement exercises in minutes. */
        const val PRIMARY_MOVEMENT_TIME_IN_MINUTES = 10
        /** Time allocated for secondary movement exercises in minutes. */
        const val SECONDARY_MOVEMENT_TIME_IN_MINUTES = 8
        /** Time allocated for conditioning exercises in minutes. */
        const val CONDITIONING_TIME_IN_MINUTES = 10
        /** Time allocated for a single accessory exercise in minutes. */
        const val SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES = 5
    }

    /** Default session time length in minutes */
    const val DEFAULT_SESSION_TIME_MINUTES = 60
}
