package com.congen.service.conjugate

import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service for selecting exercises based on various criteria including rotation history,
 * user preferences, equipment availability, and target muscles.
 */
@Service
class ExerciseSelectionService {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseSelectionService::class.java)
    }

    /**
     * Determines weak muscles based on user's 1RM data and exercise history.
     *
     * @param oneRepMaxes List of user's one rep max values
     * @param rotationHistory List of exercise rotation history
     * @return List of weak muscle groups to target
     */
    fun determineWeakMuscles(
        oneRepMaxes: List<com.congen.model.UserOneRepMax>,
        rotationHistory: List<ExerciseRotationHistory>
    ): List<String> {
        // For now, return default weak muscles
        // In a real implementation, this would analyze 1RM data and exercise history
        // to identify areas that need more attention
        return ConjugateConstants.DEFAULT_WEAK_MUSCLES
    }

    /**
     * Selects a rotating exercise based on various criteria.
     *
     * @param userId The user ID
     * @param targetMuscles List of target muscles to focus on
     * @param userEquipment List of user's available equipment
     * @param preferences List of user's exercise preferences
     * @param exercises List of available exercises
     * @param isAccessory Whether this is for an accessory exercise
     * @param rotationHistory List of exercise rotation history
     * @return Selected exercise or null if none available
     */
    fun selectRotatingExercise(
        userId: Int,
        targetMuscles: List<String>,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        isAccessory: Boolean,
        rotationHistory: List<ExerciseRotationHistory>
    ): Exercise? {
        // Filter exercises based on preferences (exercises are already filtered by is_accessory)
        val availableExercises =
            exercises.filter { exercise ->
                !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }

        if (availableExercises.isEmpty()) {
            logger.warn("No available exercises found for isAccessory: {}", isAccessory)
            return null
        }

        // Filter by equipment availability
        val equipmentFilteredExercises =
            availableExercises.filter { exercise ->
                // Check if user has any equipment for this exercise
                userEquipment.any { userEq ->
                    // This would need to be implemented with actual equipment checking
                    true // For now, assume all equipment is available
                }
            }

        if (equipmentFilteredExercises.isEmpty()) {
            logger.warn("No exercises available with user's equipment for isAccessory: {}", isAccessory)
            return availableExercises.firstOrNull() // Fallback to any available exercise
        }

        // Get exercise rotation history for this category
        val categoryHistory = rotationHistory.filter { it.isAccessory == isAccessory }

        // Get all exercises that have been used in this category
        val usedExercises = categoryHistory.map { it.exerciseName }.toSet()

        // Get exercises that haven't been used yet in this category
        val unusedExercises =
            equipmentFilteredExercises.filter { exercise ->
                !usedExercises.contains(exercise.name)
            }

        // If we have unused exercises, use them first
        val exercisesToChooseFrom =
            if (unusedExercises.isNotEmpty()) {
                unusedExercises
            } else {
                // If all exercises have been used, find the least recently used one
                val exerciseUsageCount =
                    equipmentFilteredExercises.associateWith { exercise ->
                        categoryHistory.count { it.exerciseName == exercise.name }
                    }

                val minUsageCount = exerciseUsageCount.values.minOrNull() ?: 0
                equipmentFilteredExercises.filter { exercise ->
                    exerciseUsageCount[exercise] == minUsageCount
                }
            }

        // Sort by number of equipment options (desc), targeted muscles (desc), exercise name
        val sortedExercises =
            exercisesToChooseFrom.sortedWith(
                compareByDescending<Exercise> { exercise ->
                    // Count equipment options (would need actual implementation)
                    1
                }.thenByDescending { exercise ->
                    // Count targeted muscles (would need actual implementation)
                    targetMuscles.size
                }.thenBy { exercise ->
                    exercise.name
                }
            )

        return sortedExercises.firstOrNull()
    }

    /**
     * Filters exercises by accessory status.
     *
     * @param exercises List of all exercises
     * @param isAccessory Whether to filter for accessory exercises
     * @return Filtered list of exercises
     */
    fun filterExercisesByAccessoryStatus(
        exercises: List<Exercise>,
        isAccessory: Boolean
    ): List<Exercise> {
        return exercises.filter { it.isAccessory == isAccessory }
    }

    /**
     * Filters exercises to exclude a specific exercise name.
     *
     * @param exercises List of exercises
     * @param excludeExerciseName Name of exercise to exclude
     * @return Filtered list of exercises
     */
    fun filterExercisesExcluding(
        exercises: List<Exercise>,
        excludeExerciseName: String
    ): List<Exercise> {
        return exercises.filter { it.name != excludeExerciseName }
    }
}
