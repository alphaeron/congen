package com.congen.service.conjugate

import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * Service for managing Prilepin's Chart guidelines and undulating periodization.
 *
 * Implements the conjugate method's periodization approach with 4-week cycles
 * for different movement types (Max Effort, Dynamic Effort, Accessory).
 */
@Service
class PrilepinGuidelinesService {
    companion object {
        // Updated Prilepin's Chart guidelines for different intensity ranges
        private val PRILEPIN_GUIDELINES =
            mapOf(
                "0.55-0.65" to
                    PrilepinGuidelines(
                        intensityRange = 0.55..0.65,
                        repsPerSetRange = 3..6,
                        totalReps = 24,
                        restSeconds = 60..90
                    ),
                "0.7-0.8" to
                    PrilepinGuidelines(
                        intensityRange = 0.7..0.8,
                        repsPerSetRange = 3..6,
                        totalReps = 18,
                        restSeconds = 90..120
                    ),
                "0.8-0.9" to
                    PrilepinGuidelines(
                        intensityRange = 0.8..0.9,
                        repsPerSetRange = 2..4,
                        totalReps = 15,
                        restSeconds = 180..300
                    ),
                "0.9-1.0" to
                    PrilepinGuidelines(
                        intensityRange = 0.9..1.0,
                        repsPerSetRange = 1..2,
                        totalReps = 4,
                        restSeconds = 180..300
                    )
            )
    }

    /**
     * Gets undulating periodization guidelines based on week number and movement type.
     *
     * Undulating periodization works on a 4-week cycle:
     * - Week 1-2: Build intensity
     * - Week 3: Peak intensity
     * - Week 4: Deload
     *
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param currentWeekNumber The current week number in the program
     * @param exercise The exercise being programmed
     * @return Pair of PrilepinGuidelines and target intensity
     */
    fun getUndulatingPeriodizationGuidelines(
        dayType: String,
        currentWeekNumber: Int
    ): Pair<PrilepinGuidelines, Double> {
        val weekInCycle = ((currentWeekNumber - 1) % 4) + 1
        val isUpperBody = dayType.contains("Upper")
        val isLowerBody = dayType.contains("Lower")

        return when {
            // Max Effort movements
            dayType.contains("ME") -> getMaxEffortGuidelines(weekInCycle, isUpperBody)

            // Dynamic Effort movements
            dayType.contains("DE") -> getDynamicEffortGuidelines(weekInCycle, isUpperBody, isLowerBody)

            // Accessory movements
            else -> getAccessoryGuidelines(weekInCycle)
        }
    }

    /**
     * Gets Max Effort guidelines based on week in cycle.
     *
     * Max Effort undulating periodization:
     * - Week 1-2: 80-90% intensity
     * - Week 3: 90-100% intensity (95% max for upper body, 100% for lower body)
     * - Week 4: Deload (55-65% intensity)
     */
    private fun getMaxEffortGuidelines(
        weekInCycle: Int,
        isUpperBody: Boolean
    ): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 2 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.8-0.9"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            3 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.9-1.0"]!!
                val maxIntensity = if (isUpperBody) 0.95 else 1.0
                val intensity = Random.nextDouble(0.9, maxIntensity)
                Pair(guidelines, intensity)
            }
            4 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.55-0.65"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.8-0.9"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Dynamic Effort guidelines based on week in cycle.
     *
     * Dynamic Effort undulating periodization:
     * Lower Body:
     * - Week 1: 12 sets of 2 reps or 5 sets of 5 reps, 75% intensity
     * - Week 2: 10 sets of 2 reps or 5 sets of 5 reps, 80% intensity
     * - Week 3: 8 sets of 2 reps or 5 sets of 5 reps, 85% intensity
     * - Week 4: 12 sets of 2 reps or 5 sets of 5 reps, 50% intensity (deload)
     *
     * Upper Body:
     * - Week 1: 9 sets of 3 reps, 50% intensity + bands
     * - Week 2: 9 sets of 3 reps, 55% intensity + bands
     * - Week 3: 9 sets of 3 reps, 60% intensity + bands
     * - Week 4: 9 sets of 3 reps, 50% intensity, no bands (deload)
     */
    private fun getDynamicEffortGuidelines(
        weekInCycle: Int,
        isUpperBody: Boolean,
        isLowerBody: Boolean
    ): Pair<PrilepinGuidelines, Double> {
        return when {
            isLowerBody -> getLowerBodyDynamicEffortGuidelines(weekInCycle)
            isUpperBody -> getUpperBodyDynamicEffortGuidelines(weekInCycle)
            else -> {
                // Default to lower body guidelines
                getLowerBodyDynamicEffortGuidelines(weekInCycle)
            }
        }
    }

    /**
     * Gets Lower Body Dynamic Effort guidelines.
     */
    private fun getLowerBodyDynamicEffortGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1 -> {
                // 12 sets of 2 reps or 5 sets of 5 reps, 75% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 24 else 25 // 12*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.75..0.75,
                        repsPerSetRange = repsPerSet..repsPerSet,
                        totalReps = totalReps,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.75)
            }
            2 -> {
                // 10 sets of 2 reps or 5 sets of 5 reps, 80% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 20 else 25 // 10*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.8..0.8,
                        repsPerSetRange = repsPerSet..repsPerSet,
                        totalReps = totalReps,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.8)
            }
            3 -> {
                // 8 sets of 2 reps or 5 sets of 5 reps, 85% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 16 else 25 // 8*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.85..0.85,
                        repsPerSetRange = repsPerSet..repsPerSet,
                        totalReps = totalReps,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.85)
            }
            4 -> {
                // 12 sets of 2 reps or 5 sets of 5 reps, 50% intensity (deload)
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 24 else 25 // 12*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.5..0.5,
                        repsPerSetRange = repsPerSet..repsPerSet,
                        totalReps = totalReps,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.5)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Upper Body Dynamic Effort guidelines.
     */
    private fun getUpperBodyDynamicEffortGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1 -> {
                // 9 sets of 3 reps, 50% intensity + bands
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.5..0.5,
                        repsPerSetRange = 3..3,
                        // 9*3
                        totalReps = 27,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.5)
            }
            2 -> {
                // 9 sets of 3 reps, 55% intensity + bands
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.55..0.55,
                        repsPerSetRange = 3..3,
                        // 9*3
                        totalReps = 27,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.55)
            }
            3 -> {
                // 9 sets of 3 reps, 60% intensity + bands
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.6..0.6,
                        repsPerSetRange = 3..3,
                        // 9*3
                        totalReps = 27,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.6)
            }
            4 -> {
                // 9 sets of 3 reps, 50% intensity, no bands (deload)
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.5..0.5,
                        repsPerSetRange = 3..3,
                        // 9*3
                        totalReps = 27,
                        restSeconds = 60..90
                    )
                Pair(guidelines, 0.5)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Accessory guidelines based on week in cycle.
     *
     * Accessory undulating periodization:
     * - Week 1: 55-65% intensity
     * - Week 2-3: 70-80% intensity
     * - Week 4: 55-65% intensity (deload)
     */
    private fun getAccessoryGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 4 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.55-0.65"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            2, 3 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
        }
    }
}
