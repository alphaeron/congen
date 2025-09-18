package com.congen.generator

import com.congen.model.Band
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for selecting and calculating target weights for exercises in conjugate workouts.
 *
 * This service handles:
 * - Weight calculation based on user's 1RM and intensity
 * - Weight estimation for exercises without 1RM using similar exercises
 * - Dynamic effort weight calculations with bands
 * - Bodyweight exercise estimations
 * - Unit conversion and weight rounding
 *
 * @param supportedEquipmentWeightRoundingService Service to match desired weights to equipment-supported weights
 * @param bandWeightService Service for band weight calculations
 * @param exerciseMatchingService Service for exercise matching
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WeightSelectionService(
    private val supportedEquipmentWeightRoundingService: SupportedEquipmentWeightRoundingService,
    private val bandWeightService: BandWeightService,
    private val exerciseMatchingService: ExerciseMatchingService,
) {
    /**
     * Result containing target weight and optional band information.
     */
    data class TargetWeightResult(
        /** The calculated target weight for the exercise, rounded to achievable equipment weights */
        val targetWeight: BigDecimal,
        /** Optional band to be used with the exercise (for dynamic effort exercises) */
        val band: Band?
    )

    /**
     * Gets the target weight for an exercise based on user's 1RM, rounded to achievable equipment weights.
     *
     * If the exercise is dynamic effort, compute bar and band weights.
     * If no 1RM is found, uses exercise matching to estimate weight from similar exercises.
     *
     * @param exerciseName The name of the exercise
     * @param intensity The intensity as a percentage of 1RM
     * @param oneRepMaxes List of user's one rep max values
     * @param isDynamicEffort Whether the exercise is dynamic effort
     * @param currentWeekNumber The current week number in the program (for DE)
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the target weight result (bar weight and band for DE, rounded weight for non-DE)
     */
    fun getTargetWeight(
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>,
        isDynamicEffort: Boolean = false,
        currentWeekNumber: Int = 1,
        preparedData: WorkoutGenerationPreparedData
    ): Mono<TargetWeightResult> {
        val oneRepMax = oneRepMaxes.find { it.exerciseName == exerciseName }?.oneRepMax

        return if (oneRepMax != null) {
            // User has a 1RM for this exercise, use it directly
            val calculatedWeight = (oneRepMax * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
            processTargetWeight(exerciseName, calculatedWeight, preparedData, isDynamicEffort, currentWeekNumber)
        } else {
            // No 1RM found, use exercise matching to estimate weight
            estimateWeightFromSimilarExercises(exerciseName, intensity, oneRepMaxes, isDynamicEffort, currentWeekNumber, preparedData)
        }
    }

    /**
     * Estimates weight for an exercise using similar exercises when no 1RM is available.
     */
    private fun estimateWeightFromSimilarExercises(
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData
    ): Mono<TargetWeightResult> {
        // Use prepared data for all exercises and their relationships
        val allExercises = preparedData.allExercises
        val exerciseEquipmentMap = preparedData.exerciseEquipmentMappings
        val exerciseMuscleMap = preparedData.exerciseMuscleMappings

        return Mono.just(Unit).flatMap {

            // Find the target exercise
            val targetExercise = allExercises.find { it.name == exerciseName }
            if (targetExercise == null) {
                // Exercise not found, use conservative bodyweight estimate
                return@flatMap getConservativeBodyweightEstimate(exerciseName, intensity, isDynamicEffort, currentWeekNumber, preparedData)
            }

            // Find best matching reference exercise
            val match =
                exerciseMatchingService.findBestReferenceExercise(
                    targetExercise,
                    allExercises,
                    exerciseEquipmentMap,
                    exerciseMuscleMap,
                    oneRepMaxes
                )

            // Check if the reference exercise is a bodyweight/isolation exercise
            val isBodyweightExercise =
                match.referenceExercise.name.lowercase().contains("bodyweight") ||
                    match.movementPattern == MovementType.ISOLATION

            if (isBodyweightExercise) {
                // Use bodyweight-based estimation for isolation exercises
                getBodyweightEstimate(targetExercise, intensity, isDynamicEffort, currentWeekNumber, preparedData)
            } else {
                // Find 1RM for the reference exercise
                val referenceOneRepMax = oneRepMaxes.find { it.exerciseName == match.referenceExercise.name }?.oneRepMax

                if (referenceOneRepMax != null) {
                    // Estimate weight based on reference exercise and similarity
                    val estimatedWeight =
                        exerciseMatchingService.estimateWeightFromReference(
                            targetExercise,
                            match.referenceExercise,
                            referenceOneRepMax,
                            match.similarityScore
                        )
                    val calculatedWeight = (estimatedWeight * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
                    processTargetWeight(exerciseName, calculatedWeight, preparedData, isDynamicEffort, currentWeekNumber)
                } else {
                    // No reference exercise 1RM available, use conservative bodyweight estimate
                    getConservativeBodyweightEstimate(exerciseName, intensity, isDynamicEffort, currentWeekNumber, preparedData)
                }
            }
        }
    }

    /**
     * Processes the target weight with unit conversion and rounding.
     */
    private fun processTargetWeight(
        exerciseName: String,
        calculatedWeight: BigDecimal,
        preparedData: WorkoutGenerationPreparedData,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int
    ): Mono<TargetWeightResult> {
        val weightUnit = preparedData.weightUnitPreferences[exerciseName] ?: WeightUnit.KG
        
        return if (isDynamicEffort) {
            val bandWeightResult =
                bandWeightService.computeBandAndBarWeights(
                    totalTargetWeight = calculatedWeight,
                    weightUnit = weightUnit,
                    weekInCycle = currentWeekNumber
                )
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                exerciseName,
                bandWeightResult.barWeight,
                weightUnit,
                preparedData.exerciseEquipmentMappings
            ).map { roundedWeight ->
                TargetWeightResult(
                    targetWeight = roundedWeight,
                    band = bandWeightResult.band
                )
            }
        } else {
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                exerciseName, 
                calculatedWeight, 
                weightUnit,
                preparedData.exerciseEquipmentMappings
            ).map { roundedWeight ->
                TargetWeightResult(
                    targetWeight = roundedWeight,
                    band = null
                )
                }
        }
    }

    /**
     * Gets conservative bodyweight estimate for exercises without reference lifts.
     */
    private fun getConservativeBodyweightEstimate(
        exerciseName: String,
        intensity: Double,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData
    ): Mono<TargetWeightResult> {
        // Use a conservative estimate based on exercise type
        val estimatedWeight =
            when {
                exerciseName.lowercase().contains("curl") -> BigDecimal("45") // Empty bar
                exerciseName.lowercase().contains("extension") -> BigDecimal("45") // Empty bar
                exerciseName.lowercase().contains("raise") -> BigDecimal("20") // Light dumbbells
                else -> BigDecimal("45") // Default to empty bar
            }

        val calculatedWeight = (estimatedWeight * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
        return processTargetWeight(exerciseName, calculatedWeight, preparedData, isDynamicEffort, currentWeekNumber)
    }

    /**
     * Gets bodyweight estimate for isolation exercises.
     */
    private fun getBodyweightEstimate(
        exercise: Exercise,
        intensity: Double,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData
    ): Mono<TargetWeightResult> {
        // For now, use a conservative bodyweight percentage
        // In the future, this could be enhanced to use actual user bodyweight
        val estimatedWeight = exerciseMatchingService.estimateIsolationWeight(exercise, BigDecimal("70")) // Assume 70kg user
        val calculatedWeight = (estimatedWeight * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
        return processTargetWeight(exercise.name, calculatedWeight, preparedData, isDynamicEffort, currentWeekNumber)
    }
}
