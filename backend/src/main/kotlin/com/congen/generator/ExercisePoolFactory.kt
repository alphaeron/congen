package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ProgrammedExercise
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Factory for creating user exercise pools, handling all filtering logic
 * including equipment availability, user preferences, and exercise characteristics.
 *
 * This class consolidates all exercise filtering logic that was previously scattered
 * across multiple services, providing a single source of truth for determining
 * which exercises are available to a user.
 *
 * @param exerciseMatchingService Service for exercise matching and scoring
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExercisePoolFactory(
    private val exerciseMatchingService: ExerciseMatchingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExercisePoolFactory::class.java)
    }

    /**
     * Creates a user exercise pool using prepared data instead of making database calls.
     * This method should be used in the generation stage (Stage 2) with data prepared in Stage 1.
     *
     * @param allExercises All exercises in the system
     * @param userEquipment User's available equipment
     * @param userExercisePreferences User's exercise preferences
     * @param previouslyUsedExercises Previously programmed exercises
     * @param exerciseEquipmentMappings Pre-computed mappings of exercise names to their equipment requirements
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param userId The user ID to create the pool for
     * @param excludedForWeek Exercise names already used in other workouts this week (no duplicates across workouts)
     * @param minAvailablePerCategory Minimum exercises to leave per category (2-day/3-day need 2x for ME+DE per workout)
     * @param deCycleReuseExerciseNames DE primary exercise names to keep available for 4-week cycle reuse (excluded from sliding window)
     * @return The user's exercise pool
     */
    fun createPoolFromPreparedData(
        allExercises: List<Exercise>,
        userEquipment: List<UserEquipment>,
        userExercisePreferences: List<UserExercisePreference>,
        previouslyUsedExercises: List<ProgrammedExercise>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        userId: String,
        excludedForWeek: Set<String> = emptySet(),
        minAvailablePerCategory: Int = 1,
        deCycleReuseExerciseNames: Set<String> = emptySet()
    ): UserExercisePool {
        val slidingWindowExcluded =
            applySlidingWindowLogic(
                allExercises = allExercises,
                preferences = userExercisePreferences,
                previouslyUsedExercises = previouslyUsedExercises,
                minAvailablePerCategory = minAvailablePerCategory
            )
        val excludedExercises = (slidingWindowExcluded + excludedForWeek).toSet() - deCycleReuseExerciseNames

        logger.info(
            "Created exercise pool with sliding window logic: {} total exercises, {} excluded, {} available",
            allExercises.size,
            excludedExercises.size,
            allExercises.size - excludedExercises.size
        )

        return UserExercisePool(
            allExercises = allExercises,
            preferences = userExercisePreferences,
            userEquipment = userEquipment,
            exerciseEquipmentMappings = exerciseEquipmentMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            previouslyUsedExercises = previouslyUsedExercises.map { it.exerciseName },
            userId = userId,
            excludedExercises = excludedExercises
        )
    }

    /**
     * Applies sliding window logic to determine which exercises should be available for selection.
     *
     * The key principle: ensure all exercises in a category are used before any repetition.
     *
     * @param allExercises All available exercises in the system
     * @param preferences User's exercise preferences
     * @param previouslyUsedExercises List of previously used exercises
     * @param minAvailablePerCategory Minimum to leave per category (2-day/3-day use 4/6 for ME+DE; 4-day uses 4)
     * @return List of exercise names that should be excluded from the current pool
     */
    private fun applySlidingWindowLogic(
        allExercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        previouslyUsedExercises: List<ProgrammedExercise>,
        minAvailablePerCategory: Int = 1
    ): List<String> {
        val minAvailable = maxOf(1, minAvailablePerCategory)
        // Filter exercises by user preferences first
        val preferenceFilteredExercises =
            allExercises.filter { exercise ->
                val preference = preferences.find { pref -> pref.exerciseName == exercise.name }
                when {
                    preference?.shouldAvoid == true -> false
                    preference?.shouldAvoid == false -> true
                    else -> true
                }
            }

        // Calculate available exercises per category (is_upper + is_accessory combinations)
        // Apply dumbbell restriction for primary upper body exercises
        val availablePrimaryUpperExercises =
            preferenceFilteredExercises.filter { exercise ->
                !exercise.isAccessory && exercise.isUpper && !(exercise.name.lowercase().contains("dumbbell"))
            }
        val availablePrimaryLowerExercises = preferenceFilteredExercises.filter { !it.isAccessory && !it.isUpper }
        val availableAccessoryUpperExercises = preferenceFilteredExercises.filter { it.isAccessory && it.isUpper }
        val availableAccessoryLowerExercises = preferenceFilteredExercises.filter { it.isAccessory && !it.isUpper }

        // Group previously used exercises by category and sort by most recent first
        val allUsedPrimaryUpperExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == false && exerciseModel?.isUpper == true
                }
                .sortedByDescending { it.createdAt }
                .map { it.exerciseName }
                .distinct()

        val allUsedPrimaryLowerExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == false && exerciseModel?.isUpper == false
                }
                .sortedByDescending { it.createdAt }
                .map { it.exerciseName }
                .distinct()

        val allUsedAccessoryUpperExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == true && exerciseModel?.isUpper == true
                }
                .sortedByDescending { it.createdAt }
                .map { it.exerciseName }
                .distinct()

        val allUsedAccessoryLowerExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == true && exerciseModel?.isUpper == false
                }
                .sortedByDescending { it.createdAt }
                .map { it.exerciseName }
                .distinct()

        val excludeCount = { available: Int -> maxOf(0, available - minAvailable) }
        val excludedPrimaryUpperExercises = allUsedPrimaryUpperExercises.take(excludeCount(availablePrimaryUpperExercises.size))
        val excludedPrimaryLowerExercises = allUsedPrimaryLowerExercises.take(excludeCount(availablePrimaryLowerExercises.size))
        val excludedAccessoryUpperExercises = allUsedAccessoryUpperExercises.take(excludeCount(availableAccessoryUpperExercises.size))
        val excludedAccessoryLowerExercises = allUsedAccessoryLowerExercises.take(excludeCount(availableAccessoryLowerExercises.size))

        val excludedExercises =
            excludedPrimaryUpperExercises + excludedPrimaryLowerExercises +
                excludedAccessoryUpperExercises + excludedAccessoryLowerExercises

        logger.info(
            "Applied sliding window logic per category: Primary Upper: {}/{} (window: {}), " +
                "Primary Lower: {}/{} (window: {}), Accessory Upper: {}/{} (window: {}), " +
                "Accessory Lower: {}/{} (window: {}), total excluded: {}",
            excludedPrimaryUpperExercises.size,
            availablePrimaryUpperExercises.size,
            availablePrimaryUpperExercises.size,
            excludedPrimaryLowerExercises.size,
            availablePrimaryLowerExercises.size,
            availablePrimaryLowerExercises.size,
            excludedAccessoryUpperExercises.size,
            availableAccessoryUpperExercises.size,
            availableAccessoryUpperExercises.size,
            excludedAccessoryLowerExercises.size,
            availableAccessoryLowerExercises.size,
            availableAccessoryLowerExercises.size,
            excludedExercises.size
        )

        return excludedExercises
    }
}
