package com.congen.service.conjugate

/**
 * Data class for day templates.
 */
data class DayTemplate(
    val type: String
)

/**
 * Data class for Prilepin guidelines.
 */
data class PrilepinGuidelines(
    val intensityRange: ClosedFloatingPointRange<Double>,
    val repsPerSetRange: IntRange,
    val totalReps: Int,
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
        const val PRIMARY_MOVEMENT_TIME_IN_MINUTES = 10
        const val SECONDARY_MOVEMENT_TIME_IN_MINUTES = 8
        const val CONDITIONING_TIME_IN_MINUTES = 10
        const val SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES = 5
    }
    
    /** Default session time length in minutes */
    const val DEFAULT_SESSION_TIME_MINUTES = 60
} 