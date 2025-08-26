package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
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
 * @param userWeightUnitPreferenceDAL Data access layer for user weight unit preference operations
 * @param supportedEquipmentWeightRoundingService Service to match desired weights to equipment-supported weights
 * @param bandWeightService Service for band weight calculations
 * @param exerciseMatchingService Service for exercise matching
 * @param exerciseDAL Data access layer for exercise operations
 * @param exerciseEquipmentDAL Data access layer for exercise equipment operations
 * @param exerciseMuscleDAL Data access layer for exercise muscle operations
 * @param userOneRepMaxDAL Data access layer for user one rep max operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WeightSelectionService(
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val supportedEquipmentWeightRoundingService: SupportedEquipmentWeightRoundingService,
    private val bandWeightService: BandWeightService,
    private val exerciseMatchingService: ExerciseMatchingService,
    private val exerciseDAL: ExerciseDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
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
     * @param userId The user ID for weight unit preferences
     * @param isDynamicEffort Whether the exercise is dynamic effort
     * @param currentWeekNumber The current week number in the program (for DE)
     * @return Mono containing the target weight result (bar weight and band for DE, rounded weight for non-DE)
     */
    fun getTargetWeight(
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>,
        userId: String,
        isDynamicEffort: Boolean = false,
        currentWeekNumber: Int = 1
    ): Mono<TargetWeightResult> {
        val oneRepMax = oneRepMaxes.find { it.exerciseName == exerciseName }?.oneRepMax

        return if (oneRepMax != null) {
            // User has a 1RM for this exercise, use it directly
            val calculatedWeight = (oneRepMax * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
            processTargetWeight(exerciseName, calculatedWeight, userId, isDynamicEffort, currentWeekNumber)
        } else {
            // No 1RM found, use exercise matching to estimate weight
            estimateWeightFromSimilarExercises(exerciseName, intensity, oneRepMaxes, userId, isDynamicEffort, currentWeekNumber)
        }
    }

    /**
     * Gets the weight unit preference for an exercise.
     *
     * @param userId The user ID for weight unit preferences
     * @param exerciseName The name of the exercise
     * @return Mono containing the weight unit preference, defaulting to KG if not found
     */
    private fun getWeightUnitForExercise(
        userId: String,
        exerciseName: String
    ): Mono<WeightUnit> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
            .map { it.preferredUnit }
            .onErrorReturn(WeightUnit.KG) // Default to KG if no preference found
    }

    /**
     * Estimates weight for an exercise using similar exercises when no 1RM is available.
     */
    private fun estimateWeightFromSimilarExercises(
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>,
        userId: String,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int
    ): Mono<TargetWeightResult> {
        // Get all exercises and their relationships for matching
        return Mono.zip(
            exerciseDAL.selectExercises(),
            exerciseEquipmentDAL.selectAllExerciseEquipment(),
            exerciseMuscleDAL.selectAllExerciseMuscle()
        ).flatMap { tuple ->
            val allExercises = tuple.t1
            val allEquipment = tuple.t2
            val allMuscles = tuple.t3

            // Create maps for efficient lookup
            val exerciseEquipmentMap = allEquipment.groupBy { it.exerciseName }
            val exerciseMuscleMap = allMuscles.groupBy { it.exerciseName }

            // Find the target exercise
            val targetExercise = allExercises.find { it.name == exerciseName }
            if (targetExercise == null) {
                // Exercise not found, use conservative bodyweight estimate
                return@flatMap getConservativeBodyweightEstimate(exerciseName, intensity, userId, isDynamicEffort, currentWeekNumber)
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
                getBodyweightEstimate(targetExercise, intensity, userId, isDynamicEffort, currentWeekNumber)
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
                    processTargetWeight(exerciseName, calculatedWeight, userId, isDynamicEffort, currentWeekNumber)
                } else {
                    // No reference exercise 1RM available, use conservative bodyweight estimate
                    getConservativeBodyweightEstimate(exerciseName, intensity, userId, isDynamicEffort, currentWeekNumber)
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
        userId: String,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int
    ): Mono<TargetWeightResult> {
        return getWeightUnitForExercise(userId, exerciseName)
            .flatMap { weightUnit ->
                if (isDynamicEffort) {
                    val bandWeightResult =
                        bandWeightService.computeBandAndBarWeights(
                            totalTargetWeight = calculatedWeight,
                            weightUnit = weightUnit,
                            weekInCycle = currentWeekNumber
                        )
                    supportedEquipmentWeightRoundingService.roundWeightForExercise(
                        exerciseName,
                        bandWeightResult.barWeight,
                        weightUnit
                    ).map {
                            roundedWeight ->
                        TargetWeightResult(
                            targetWeight = roundedWeight,
                            band = bandWeightResult.band
                        )
                    }
                } else {
                    supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, calculatedWeight, weightUnit)
                        .map { roundedWeight ->
                            TargetWeightResult(
                                targetWeight = roundedWeight,
                                band = null
                            )
                        }
                }
            }
    }

    /**
     * Gets conservative bodyweight estimate for exercises without reference lifts.
     */
    private fun getConservativeBodyweightEstimate(
        exerciseName: String,
        intensity: Double,
        userId: String,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int
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
        return processTargetWeight(exerciseName, calculatedWeight, userId, isDynamicEffort, currentWeekNumber)
    }

    /**
     * Gets bodyweight estimate for isolation exercises.
     */
    private fun getBodyweightEstimate(
        exercise: Exercise,
        intensity: Double,
        userId: String,
        isDynamicEffort: Boolean,
        currentWeekNumber: Int
    ): Mono<TargetWeightResult> {
        // For now, use a conservative bodyweight percentage
        // In the future, this could be enhanced to use actual user bodyweight
        val estimatedWeight = exerciseMatchingService.estimateIsolationWeight(exercise, BigDecimal("70")) // Assume 70kg user
        val calculatedWeight = (estimatedWeight * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
        return processTargetWeight(exercise.name, calculatedWeight, userId, isDynamicEffort, currentWeekNumber)
    }
}
