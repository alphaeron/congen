package com.congen.generator

import org.springframework.stereotype.Component

/**
 * Utility service for calculating session time allocations and determining
 * the number of accessory exercises based on available time.
 */
@Component
class SessionTimeCalculator {
    companion object {
        /** Estimated time per repetition in seconds */
        const val SECONDS_PER_REP = 6
    }

    /**
     * Calculates the number of accessory exercises based on session time and workout type.
     *
     * Time allocation:
     * - Warmup: 10 minutes
     * - Primary movement: 10 minutes
     * - Secondary movement: 8 minutes (if applicable)
     * - Each accessory exercise: 5 minutes
     * - Conditioning: 10 minutes (for DE days)
     *
     * @param sessionTimeMinutes The desired session time in minutes
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @return The number of accessory exercises to include
     */
    fun calculateNumAccessoryExercises(
        sessionTimeMinutes: Int,
        dayType: String
    ): Int {
        // Base time allocation
        var timeAllocated = ConjugateConstants.TimeAllocation.WARMUP_TIME_IN_MINUTES // Warmup (10 minutes)
        timeAllocated += ConjugateConstants.TimeAllocation.PRIMARY_MOVEMENT_TIME_IN_MINUTES // Primary movement (10 minutes)

        // Add secondary movement time if applicable
        if (hasSecondaryMovement(dayType)) {
            timeAllocated += ConjugateConstants.TimeAllocation.SECONDARY_MOVEMENT_TIME_IN_MINUTES // Secondary movement (8 minutes)
        }

        // Add conditioning time for DE days
        if (dayType.contains("DE")) {
            timeAllocated += ConjugateConstants.TimeAllocation.CONDITIONING_TIME_IN_MINUTES // Conditioning (10 minutes)
        }

        // Calculate remaining time for accessories
        val remainingTime = sessionTimeMinutes - timeAllocated

        // Each accessory exercise takes 5 minutes
        val numAccessories = (remainingTime / ConjugateConstants.TimeAllocation.SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES).coerceAtLeast(0)

        return numAccessories
    }

    /**
     * Calculates the number of accessory exercises based on dynamic time calculation from actual set schemes.
     *
     * This method computes the actual time taken by primary and secondary movements based on their set schemes,
     * then determines how many accessory exercises can fit in the remaining time.
     *
     * @param sessionTimeMinutes The total session time in minutes
     * @param primarySetSchemes List of set scheme parameters for the primary movement (can be empty)
     * @param secondarySetSchemes List of set scheme parameters for the secondary movement (can be empty)
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @return The number of accessory exercises to include
     */
    fun calculateNumAccessoryExercisesDynamic(
        sessionTimeMinutes: Int,
        primarySetSchemes: List<SetSchemeParams>,
        secondarySetSchemes: List<SetSchemeParams>,
        dayType: String
    ): Int {
        // Calculate time taken by primary movement
        val primaryExerciseTimeSeconds = calculateExerciseTime(primarySetSchemes)

        // Calculate time taken by secondary movement
        val secondaryExerciseTimeSeconds = calculateExerciseTime(secondarySetSchemes)

        // Convert session time to seconds
        val sessionTimeSeconds = sessionTimeMinutes * 60

        // Calculate remaining time for accessories
        var remainingTimeSeconds = sessionTimeSeconds - primaryExerciseTimeSeconds - secondaryExerciseTimeSeconds

        // Subtract warmup time (10 minutes = 600 seconds)
        remainingTimeSeconds -= ConjugateConstants.TimeAllocation.WARMUP_TIME_IN_MINUTES * 60

        // If conditioning is included, subtract 10 minutes (600 seconds)
        val hasConditioning = dayType.contains("DE")
        if (hasConditioning) {
            remainingTimeSeconds -= ConjugateConstants.TimeAllocation.CONDITIONING_TIME_IN_MINUTES * 60
        }

        // If no time left for accessories, skip conditioning and use that time for accessories
        if (remainingTimeSeconds <= 0 && hasConditioning) {
            remainingTimeSeconds = ConjugateConstants.TimeAllocation.CONDITIONING_TIME_IN_MINUTES * 60
        }

        // Each accessory exercise takes 5 minutes (300 seconds)
        val numAccessories =
            (remainingTimeSeconds / (ConjugateConstants.TimeAllocation.SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES * 60)).coerceAtLeast(
                0
            )

        return numAccessories
    }

    /**
     * Calculates the estimated time for an exercise based on its set schemes.
     *
     * Formula: num_sets * (rest_seconds + reps_per_set * SECONDS_PER_REP)
     *
     * @param setSchemes List of set scheme parameters for the exercise
     * @return Estimated time in seconds
     */
    fun calculateExerciseTime(setSchemes: List<SetSchemeParams>): Int {
        if (setSchemes.isEmpty()) {
            return 0
        }

        return setSchemes.sumOf { setScheme ->
            val repsPerSet = setScheme.targetRepCount ?: 0
            val restSeconds = setScheme.restSeconds ?: 0
            val exerciseTime = repsPerSet * SECONDS_PER_REP
            restSeconds + exerciseTime
        }
    }

    /**
     * Gets the total time allocated for non-accessory components.
     *
     * @param dayType The type of workout day
     * @return The total time allocated for warmup, primary, secondary, and conditioning components
     */
    fun getNonAccessoryTimeAllocation(dayType: String): Int {
        var timeAllocated = ConjugateConstants.TimeAllocation.WARMUP_TIME_IN_MINUTES // Warmup (10 minutes)
        timeAllocated += ConjugateConstants.TimeAllocation.PRIMARY_MOVEMENT_TIME_IN_MINUTES // Primary movement (10 minutes)

        if (hasSecondaryMovement(dayType)) {
            timeAllocated += ConjugateConstants.TimeAllocation.SECONDARY_MOVEMENT_TIME_IN_MINUTES // Secondary movement (8 minutes)
        }

        if (dayType.contains("DE")) {
            timeAllocated += ConjugateConstants.TimeAllocation.CONDITIONING_TIME_IN_MINUTES // Conditioning (10 minutes)
        }

        return timeAllocated
    }

    /**
     * Gets the remaining time available for accessory exercises.
     *
     * @param sessionTimeMinutes The total session time
     * @param dayType The type of workout day
     * @return The remaining time available for accessories
     */
    fun getRemainingTimeForAccessories(
        sessionTimeMinutes: Int,
        dayType: String
    ): Int {
        val nonAccessoryTime = getNonAccessoryTimeAllocation(dayType)
        return (sessionTimeMinutes - nonAccessoryTime).coerceAtLeast(0)
    }

    /**
     * Determines if a day type has a secondary movement.
     *
     * @param dayType The type of workout day
     * @return true if the day includes a secondary movement, false otherwise
     */
    private fun hasSecondaryMovement(dayType: String): Boolean {
        return when (dayType) {
            "ME_Upper", "DE_Upper" -> true // Traditional 4-day program secondary movements
            "ME_Upper_DE_Lower", "ME_Lower_DE_Upper" -> true // Combined ME+DE days have both movements
            "DE_Full_Body" -> true // Full body DE has both upper and lower movements
            else -> false
        }
    }
}
