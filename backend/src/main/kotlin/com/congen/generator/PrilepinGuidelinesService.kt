package com.congen.generator

import org.springframework.stereotype.Service
import kotlin.math.abs
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
                        totalRepsRange = 18..30,
                        restSeconds = 60..90
                    ),
                "0.7-0.8" to
                    PrilepinGuidelines(
                        intensityRange = 0.7..0.8,
                        repsPerSetRange = 3..6,
                        totalReps = 18,
                        totalRepsRange = 12..24,
                        restSeconds = 90..120
                    ),
                "0.8-0.9" to
                    PrilepinGuidelines(
                        intensityRange = 0.8..0.9,
                        repsPerSetRange = 2..4,
                        totalReps = 15,
                        totalRepsRange = 10..20,
                        restSeconds = 180..300
                    ),
                "0.9-1.0" to
                    PrilepinGuidelines(
                        intensityRange = 0.9..1.0,
                        repsPerSetRange = 1..2,
                        totalReps = 4,
                        totalRepsRange = 4..4,
                        restSeconds = 180..300
                    )
            )
    }

    /**
     * Rounds rest time to standard intervals for better user experience.
     *
     * Standard rest time intervals:
     * - 30 seconds for very short rest
     * - 60 seconds for short rest
     * - 90 seconds for moderate rest
     * - 120 seconds for longer rest
     * - 180 seconds for long rest
     * - 240 seconds for very long rest
     * - 300 seconds for maximum rest
     *
     * @param restSeconds The raw rest time in seconds
     * @return Rounded rest time to nearest standard interval
     */
    private fun roundRestTimeToStandardInterval(restSeconds: Int): Int {
        val standardIntervals = listOf(30, 45, 60, 90, 120, 180, 300)
        return standardIntervals.minByOrNull { abs(it - restSeconds) } ?: restSeconds
    }

    /**
     * Gets a rest time based on intensity and total reps, rounded to standard intervals.
     *
     * Rest time calculation:
     * - 0.9-1.0 intensity: Always 300 seconds (5 minutes)
     * - Other intensities: Based on how close to the upper end of totalRepsRange
     * - Higher intensity and more reps = longer rest
     *
     * @param restRange The range of rest times
     * @param intensity The exercise intensity (0.0-1.0)
     * @param totalReps The actual total reps being performed
     * @param totalRepsRange The acceptable range of total reps
     * @return A calculated rest time rounded to standard intervals
     */
    fun getRestTimeBasedOnIntensity(
        restRange: IntRange,
        intensity: Double,
        totalReps: Int,
        totalRepsRange: IntRange
    ): Int {
        // For 90%+ intensity, always use maximum rest (300 seconds)
        if (intensity >= 0.9) {
            return 300
        }

        // Calculate how close we are to the upper end of the total reps range
        val rangeSize = totalRepsRange.endInclusive - totalRepsRange.start
        val repsFromStart = totalReps - totalRepsRange.start
        val repsRatio = if (rangeSize > 0) repsFromStart.toDouble() / rangeSize else 0.5

        // Calculate intensity factor (0.0-1.0)
        val intensityFactor = (intensity - 0.5) / 0.4 // Normalize 0.5-0.9 to 0.0-1.0

        // Combine intensity and reps factors
        val combinedFactor = (intensityFactor + repsRatio) / 2.0

        // Calculate rest time within the range
        val restRangeSize = restRange.endInclusive - restRange.start
        val calculatedRest = restRange.start + (combinedFactor * restRangeSize).toInt()

        return roundRestTimeToStandardInterval(calculatedRest)
    }

    /**
     * Gets a random rest time from the given range, rounded to standard intervals.
     *
     * @param restRange The range of rest times
     * @return A random rest time rounded to standard intervals
     */
    fun getRandomRestTime(restRange: IntRange): Int {
        val rawRestTime = restRange.random()
        return roundRestTimeToStandardInterval(rawRestTime)
    }

    /**
     * Gets reps per set and number of sets based on intensity within the given guidelines.
     *
     * Reps calculation:
     * - Higher intensity = lower reps within the range
     * - Lower intensity = higher reps within the range
     * - Linear interpolation between these points
     *
     * Sets calculation:
     * - Based on total reps divided by reps per set
     * - Higher intensity = fewer total reps within the range
     * - Maximum set limits: 5 sets for primary, 4 sets for secondary
     *
     * @param guidelines The Prilepin guidelines containing ranges and total reps
     * @param intensity The actual intensity being used
     * @param movementRole The role of the movement ("primary", "secondary", "accessory")
     * @return Pair of (reps per set, number of sets)
     */
    fun getRepsAndSetsBasedOnIntensity(
        guidelines: PrilepinGuidelines,
        intensity: Double,
        movementRole: String = "primary"
    ): Pair<Int, Int> {
        // Calculate reps based on intensity: higher intensity = lower reps
        val rangeSize = guidelines.intensityRange.endInclusive - guidelines.intensityRange.start
        val intensityPosition = (intensity - guidelines.intensityRange.start) / rangeSize

        val maxReps = guidelines.repsPerSetRange.last
        val minReps = guidelines.repsPerSetRange.first
        val repsPerSet = (maxReps - (intensityPosition * (maxReps - minReps))).toInt().coerceIn(minReps, maxReps)

        // Calculate total reps based on intensity: higher intensity = fewer total reps
        val totalRepsRangeSize = guidelines.totalRepsRange.endInclusive - guidelines.totalRepsRange.start
        val adjustedTotalReps = guidelines.totalRepsRange.endInclusive - (intensityPosition * totalRepsRangeSize).toInt()

        // Calculate sets based on adjusted total reps
        var numSets = (adjustedTotalReps / repsPerSet).toInt()

        // Apply maximum set limits based on movement role
        val maxSets =
            when (movementRole) {
                "primary" -> 5
                "secondary" -> 4
                "accessory" -> 6 // Accessories can have more sets
                else -> 5
            }

        // If calculated sets exceed maximum, adjust reps per set to fit within max sets
        if (numSets > maxSets) {
            numSets = maxSets
            // Recalculate reps per set to maintain total volume as much as possible
            val adjustedRepsPerSet = (adjustedTotalReps / numSets).toInt()
            return Pair(adjustedRepsPerSet.coerceIn(minReps, maxReps), numSets)
        }

        return Pair(repsPerSet, numSets)
    }

    /**
     * Gets undulating periodization guidelines based on day type, week number, and movement role.
     *
     * This function determines the appropriate Prilepin guidelines and intensity based on:
     * - Day type (ME_Upper, ME_Lower, DE_Upper, DE_Lower, etc.)
     * - Current week number (used to calculate week in cycle)
     * - Movement role (primary, secondary, accessory)
     *
     * The function handles the 4-week undulating periodization cycle:
     * - Week 1-2: Build-up phase
     * - Week 3: Peak intensity phase
     * - Week 4: Deload phase
     *
     * @param dayType The type of training day (e.g., "ME_Upper", "DE_Lower")
     * @param currentWeekNumber The current week number (used to calculate week in cycle)
     * @param movementRole The role of the movement ("primary", "secondary", "accessory")
     * @return Pair of (PrilepinGuidelines, intensity)
     */
    fun getUndulatingPeriodizationGuidelines(
        dayType: String,
        currentWeekNumber: Int,
        movementRole: String = "primary"
    ): Pair<PrilepinGuidelines, Double> {
        val weekInCycle = ((currentWeekNumber - 1) % 4) + 1

        val result =
            when {
                // Accessory movements always use accessory guidelines
                movementRole == "accessory" -> getAccessoryGuidelines(weekInCycle)

                // Secondary movements use lower intensity guidelines than primary
                movementRole == "secondary" -> getSecondaryExerciseGuidelines(weekInCycle)

                // Handle combined ME+DE days based on movement role
                dayType == "ME_Upper_DE_Lower" -> {
                    when (movementRole) {
                        "primary" -> getMaxEffortGuidelines(weekInCycle, isUpperBody = true)
                        "secondary" -> getDynamicEffortGuidelines(weekInCycle, isUpperBody = false, isLowerBody = true)
                        else -> getMaxEffortGuidelines(weekInCycle, isUpperBody = true)
                    }
                }

                dayType == "ME_Lower_DE_Upper" -> {
                    when (movementRole) {
                        "primary" -> getMaxEffortGuidelines(weekInCycle, isUpperBody = false)
                        "secondary" -> getDynamicEffortGuidelines(weekInCycle, isUpperBody = true, isLowerBody = false)
                        else -> getMaxEffortGuidelines(weekInCycle, isUpperBody = false)
                    }
                }

                // Handle regular day types
                dayType.contains("ME") -> {
                    val isUpperBody = dayType.contains("Upper")
                    getMaxEffortGuidelines(weekInCycle, isUpperBody)
                }

                dayType.contains("DE") -> {
                    val isUpperBody = dayType.contains("Upper")
                    val isLowerBody = dayType.contains("Lower")
                    getDynamicEffortGuidelines(weekInCycle, isUpperBody, isLowerBody)
                }

                // Default to accessory guidelines
                else -> getAccessoryGuidelines(weekInCycle)
            }

        return result
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
                        totalRepsRange = totalReps..totalReps,
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
                        totalRepsRange = totalReps..totalReps,
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
                        totalRepsRange = totalReps..totalReps,
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
                        totalRepsRange = totalReps..totalReps,
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
                        totalRepsRange = 27..27,
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
                        totalRepsRange = 27..27,
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
                        totalRepsRange = 27..27,
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
                        totalRepsRange = 27..27,
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
     * Gets Secondary Exercise guidelines based on week in cycle.
     *
     * Secondary exercises use lower intensity than primary exercises:
     * - Week 1-2: 70-80% intensity (vs 80-90% for primary)
     * - Week 3: 80-90% intensity (vs 90-100% for primary)
     * - Week 4: 55-65% intensity (deload, same as primary)
     */
    private fun getSecondaryExerciseGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 2 -> {
                // Use 70-80% intensity for secondary (lower than primary's 80-90%)
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            3 -> {
                // Use 80-90% intensity for secondary (lower than primary's 90-100%)
                val guidelines = PRILEPIN_GUIDELINES["0.8-0.9"]!!
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            4 -> {
                // Use 55-65% intensity for deload (same as primary)
                val guidelines = PRILEPIN_GUIDELINES["0.55-0.65"]!!
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

    /**
     * Gets Accessory guidelines based on week in cycle.
     *
     * Accessory undulating periodization with 3-4 sets:
     * - Week 1: 55-65% intensity, 3-4 sets of "good" rep numbers (6, 8, 10, 12, 15)
     * - Week 2-3: 70-80% intensity, 3-4 sets of "good" rep numbers (6, 8, 10, 12, 15)
     * - Week 4: 55-65% intensity (deload), 3-4 sets of "good" rep numbers (6, 8, 10, 12, 15)
     */
    private fun getAccessoryGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 4 -> {
                // Use 55-65% intensity with 3-4 sets of "good" rep numbers for accessories
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.55..0.65,
                        // This will be overridden to use specific "good" numbers
                        repsPerSetRange = 6..15,
                        // 3-4 sets × 6-15 reps = 18-60 total, target ~30
                        totalReps = 30,
                        totalRepsRange = 18..60,
                        restSeconds = 60..90
                    )
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            2, 3 -> {
                // Use 70-80% intensity with 3-4 sets of "good" rep numbers for accessories
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.7..0.8,
                        // This will be overridden to use specific "good" numbers
                        repsPerSetRange = 6..15,
                        // 3-4 sets × 6-15 reps = 18-60 total, target ~24
                        totalReps = 24,
                        totalRepsRange = 18..60,
                        restSeconds = 90..120
                    )
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
            else -> {
                val guidelines =
                    PrilepinGuidelines(
                        intensityRange = 0.7..0.8,
                        // This will be overridden to use specific "good" numbers
                        repsPerSetRange = 6..15,
                        totalReps = 24,
                        totalRepsRange = 18..60,
                        restSeconds = 90..120
                    )
                val intensity = Random.nextDouble(guidelines.intensityRange.start, guidelines.intensityRange.endInclusive)
                Pair(guidelines, intensity)
            }
        }
    }
}
