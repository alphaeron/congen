package com.congen.service

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.model.WeightUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * Service for selecting and rounding weights to match available plate and equipment sizes.
 *
 * This service ensures that calculated weights can be achieved using standard
 * weightlifting equipment by rounding to the nearest available plate or equipment weight.
 *
 * ## Equipment Types
 *
 * - **Barbell exercises**: Use standard weight plates (45lb/20kg bar + plates)
 * - **Kettlebell exercises**: Use standard kettlebell weights
 * - **Dumbbell exercises**: Round to nearest 5lb increment
 *
 * ## Standard Gym Math
 *
 * **Barbell Plates (Pounds)**: 2.5, 5, 10, 25, 35, 45 lbs
 * **Barbell Plates (Kilograms)**: 1.25, 2.5, 5, 10, 15, 20, 25 kg
 * **Bar Weight**: 45 lbs / 20 kg
 *
 * **Kettlebell Weights (Pounds)**: 9, 13, 18, 26, 35, 40, 44, 53, 62, 70, 80, 88, 97, 106, 124, 150, 176 lb
 * **Kettlebell Weights (Kilograms)**: 4, 6, 8, 12, 16, 18, 20, 24, 28, 32, 36, 40, 44, 48, 56, 68, 80 kg
 *
 * **Dumbbell Increments**: 5 lb / 2.5 kg
 *
 * @property exerciseEquipmentDAL Data access layer for exercise equipment relationships
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class WeightSelectionService(
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WeightSelectionService::class.java)

        // Barbell equipment names that require plate-based weight selection
        private val BARBELL_EQUIPMENT =
            setOf(
                "power bar",
                "safety squat bar",
                "trap bar",
                "landmine"
            )

        // Kettlebell equipment
        private val KETTLEBELL_EQUIPMENT = setOf("kettlebell")

        // Dumbbell equipment
        private val DUMBBELL_EQUIPMENT = setOf("dumbbells")

        // Standard plate sizes in pounds (excluding bar weight) - updated to include 35lb plates
        private val POUND_PLATES = listOf(2.5, 5.0, 10.0, 25.0, 35.0, 45.0)

        // Standard plate sizes in kilograms (excluding bar weight)
        private val KILOGRAM_PLATES = listOf(1.25, 2.5, 5.0, 10.0, 15.0, 20.0, 25.0)

        // Standard kettlebell weights in pounds
        private val POUND_KETTLEBELLS = listOf(9, 13, 18, 26, 35, 40, 44, 53, 62, 70, 80, 88, 97, 106, 124, 150, 176)

        // Standard kettlebell weights in kilograms
        private val KILOGRAM_KETTLEBELLS = listOf(4, 6, 8, 12, 16, 18, 20, 24, 28, 32, 36, 40, 44, 48, 56, 68, 80)

        // Bar weights
        private const val POUND_BAR_WEIGHT = 45.0
        private const val KILOGRAM_BAR_WEIGHT = 20.0

        // Dumbbell rounding increment
        private const val DUMBBELL_ROUNDING_INCREMENT = 5.0
        private const val DUMBBELL_KG_ROUNDING_INCREMENT = 2.5
    }

    /**
     * Rounds a weight to the nearest achievable weight based on the exercise's equipment.
     *
     * @param exerciseName The name of the exercise
     * @param targetWeight The target weight to round
     * @param weightUnit The unit of the target weight
     * @return Mono containing the rounded weight that can be achieved with available equipment
     */
    fun roundWeightForExercise(
        exerciseName: String,
        targetWeight: BigDecimal,
        weightUnit: WeightUnit
    ): Mono<BigDecimal> {
        return exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)
            .map { equipment ->
                val equipmentNames = equipment.map { it.equipmentName }.toSet()

                when {
                    equipmentNames.any { it in BARBELL_EQUIPMENT } -> {
                        roundWeightForBarbell(targetWeight, weightUnit)
                    }
                    equipmentNames.any { it in KETTLEBELL_EQUIPMENT } -> {
                        roundWeightForKettlebell(targetWeight, weightUnit)
                    }
                    equipmentNames.any { it in DUMBBELL_EQUIPMENT } -> {
                        roundWeightForDumbbell(targetWeight, weightUnit)
                    }
                    else -> {
                        // For exercises without specific equipment requirements, return the original weight
                        logger.debug("No specific equipment found for exercise: {}, returning original weight", exerciseName)
                        targetWeight.setScale(2, RoundingMode.HALF_UP)
                    }
                }
            }
            .onErrorReturn(targetWeight.setScale(2, RoundingMode.HALF_UP)) // Return original weight if equipment lookup fails
    }

    /**
     * Rounds a weight for barbell exercises using standard plate sizes.
     *
     * Standard gym math: Uses 45lb bar and finds the closest achievable weight
     * using standard plates (2.5, 5, 10, 25, 35, 45 lbs).
     *
     * @param targetWeight The target weight to round
     * @param weightUnit The unit of the target weight
     * @return The rounded weight that can be achieved with standard plates
     */
    private fun roundWeightForBarbell(
        targetWeight: BigDecimal,
        weightUnit: WeightUnit
    ): BigDecimal {
        val (barWeight, plates) =
            when (weightUnit) {
                WeightUnit.LBS -> Pair(POUND_BAR_WEIGHT, POUND_PLATES)
                WeightUnit.KG -> Pair(KILOGRAM_BAR_WEIGHT, KILOGRAM_PLATES)
            }

        // If target weight is less than bar weight, return bar weight
        if (targetWeight <= BigDecimal(barWeight)) {
            return BigDecimal(barWeight).setScale(2, RoundingMode.HALF_UP)
        }

        // Calculate the weight that needs to be added to the bar
        val weightToAdd = targetWeight - BigDecimal(barWeight)

        // Find the closest achievable weight using available plates
        val achievableWeight = findClosestAchievableWeight(weightToAdd, plates)

        // Return bar weight + achievable plate weight
        return (BigDecimal(barWeight) + achievableWeight).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Rounds a weight for kettlebell exercises using standard kettlebell weights.
     *
     * @param targetWeight The target weight to round
     * @param weightUnit The unit of the target weight
     * @return The rounded weight that matches a standard kettlebell weight
     */
    private fun roundWeightForKettlebell(
        targetWeight: BigDecimal,
        weightUnit: WeightUnit
    ): BigDecimal {
        val kettlebellWeights =
            when (weightUnit) {
                WeightUnit.LBS -> POUND_KETTLEBELLS
                WeightUnit.KG -> KILOGRAM_KETTLEBELLS
            }

        val targetWeightDouble = targetWeight.toDouble()
        val closestWeight =
            kettlebellWeights.minByOrNull {
                kotlin.math.abs(it - targetWeightDouble)
            } ?: kettlebellWeights.first()

        return BigDecimal(closestWeight).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Rounds a weight for dumbbell exercises to the nearest standard increment.
     *
     * Standard gym math: Rounds to nearest 5lb increment for pounds,
     * or nearest 2.5kg increment for kilograms.
     *
     * @param targetWeight The target weight to round
     * @param weightUnit The unit of the target weight
     * @return The rounded weight to the nearest standard increment
     */
    private fun roundWeightForDumbbell(
        targetWeight: BigDecimal,
        weightUnit: WeightUnit
    ): BigDecimal {
        return when (weightUnit) {
            WeightUnit.LBS -> {
                // Round to nearest 5lb increment
                val rounded = (targetWeight.toDouble() / DUMBBELL_ROUNDING_INCREMENT).roundToInt() * DUMBBELL_ROUNDING_INCREMENT
                BigDecimal.valueOf(rounded).setScale(2, RoundingMode.HALF_UP)
            }
            WeightUnit.KG -> {
                // For kg, round to nearest 2.5kg increment (equivalent to 5lb rounding)
                val rounded = (targetWeight.toDouble() / DUMBBELL_KG_ROUNDING_INCREMENT).roundToInt() * DUMBBELL_KG_ROUNDING_INCREMENT
                BigDecimal.valueOf(rounded).setScale(2, RoundingMode.HALF_UP)
            }
        }
    }

    /**
     * Finds the closest achievable weight using available plates.
     *
     * This method uses a comprehensive algorithm to find the combination of plates
     * that gets closest to the target weight, considering all possible combinations
     * up to a reasonable limit.
     *
     * @param targetWeight The target weight to achieve
     * @param availablePlates List of available plate weights
     * @return The closest achievable weight using available plates
     */
    private fun findClosestAchievableWeight(
        targetWeight: BigDecimal,
        availablePlates: List<Double>
    ): BigDecimal {
        val targetDouble = targetWeight.toDouble()
        val sortedPlates = availablePlates.sortedDescending() // Start with heaviest plates

        // Try to find exact match or closest achievable weight
        var bestWeight = 0.0
        var bestDifference = Double.MAX_VALUE

        // Try single plates
        for (plate in sortedPlates) {
            val difference = kotlin.math.abs(targetDouble - plate)
            if (difference < bestDifference) {
                bestWeight = plate
                bestDifference = difference
            }
        }

        // Try combinations of two plates (most common in gyms)
        for (i in sortedPlates.indices) {
            for (j in i until sortedPlates.size) {
                val combinedWeight = sortedPlates[i] + sortedPlates[j]
                val difference = kotlin.math.abs(targetDouble - combinedWeight)

                if (difference < bestDifference) {
                    bestWeight = combinedWeight
                    bestDifference = difference
                }
            }
        }

        // Try combinations of three plates (for heavier weights)
        for (i in sortedPlates.indices) {
            for (j in i until sortedPlates.size) {
                for (k in j until sortedPlates.size) {
                    val combinedWeight = sortedPlates[i] + sortedPlates[j] + sortedPlates[k]
                    val difference = kotlin.math.abs(targetDouble - combinedWeight)

                    if (difference < bestDifference) {
                        bestWeight = combinedWeight
                        bestDifference = difference
                    }
                }
            }
        }

        // Try combinations of four plates (for very heavy weights)
        for (i in sortedPlates.indices) {
            for (j in i until sortedPlates.size) {
                for (k in j until sortedPlates.size) {
                    for (l in k until sortedPlates.size) {
                        val combinedWeight = sortedPlates[i] + sortedPlates[j] + sortedPlates[k] + sortedPlates[l]
                        val difference = kotlin.math.abs(targetDouble - combinedWeight)

                        if (difference < bestDifference) {
                            bestWeight = combinedWeight
                            bestDifference = difference
                        }
                    }
                }
            }
        }

        return BigDecimal(bestWeight).setScale(2, RoundingMode.HALF_UP)
    }
}
