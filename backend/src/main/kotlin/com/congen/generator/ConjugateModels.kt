package com.congen.generator

import com.congen.model.Band
import java.math.BigDecimal

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
    /** The optimal total number of reps to perform. */
    val totalReps: Int,
    /** The acceptable range of total reps to perform. */
    val totalRepsRange: IntRange,
    /** The range of rest seconds between sets. */
    val restSeconds: IntRange
)

/**
 * Data class to hold set scheme parameters without requiring temporary IDs.
 */
data class SetSchemeParams(
    /** The set number within the workout. */
    val setNumber: Int,
    /** Whether this is an "as many reps as possible" set. */
    val isAmrap: Boolean,
    /** Whether this is an "every minute on the minute" set. */
    val isEmom: Boolean,
    /** Whether tempo training should be used for this set. */
    val useTempo: Boolean,
    /** The eccentric tempo (lowering phase) in seconds. */
    val eccentricTempo: String?,
    /** The isometric tempo (pause phase) in seconds. */
    val isometricTempo: String?,
    /** The concentric tempo (lifting phase) in seconds. */
    val concentricTempo: String?,
    /** The target weight for this set in kilograms. */
    val targetWeight: BigDecimal?,
    /** The actual weight performed for this set in kilograms. */
    val performedWeight: BigDecimal?,
    /** The target number of repetitions for this set. */
    val targetRepCount: Int?,
    /** The actual number of repetitions performed for this set. */
    val performedRepCount: Int?,
    /** The rest time in seconds before the next set. */
    val restSeconds: Int?,
    /** The band information for Dynamic Effort exercises. */
    val band: Band?
)

/**
 * Constants for conjugate workout generation.
 */
object ConjugateConstants {
    /** Default weak muscles for new users */
    val DEFAULT_WEAK_MUSCLES = listOf("hamstrings", "glutes", "upper_back", "core")

    /** Time allocation for different workout components (in minutes) */
    object TimeAllocation {
        /** Time allocated for warmup exercises in minutes. */
        const val WARMUP_TIME_IN_MINUTES = 10

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
