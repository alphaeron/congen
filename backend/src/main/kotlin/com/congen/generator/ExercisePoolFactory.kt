package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Factory for creating user exercise pools, handling all filtering logic
 * including equipment availability, user preferences, and exercise characteristics.
 *
 * This class consolidates all exercise filtering logic that was previously scattered
 * across multiple services, providing a single source of truth for determining
 * which exercises are available to a user.
 *
 * @param exerciseEquipmentDAL Data access layer for exercise equipment relationships
 * @param exerciseMuscleDAL Data access layer for exercise muscle relationships
 * @param exerciseWorkoutTypeDAL Data access layer for exercise workout type relationships
 * @param exerciseMatchingService Service for exercise matching and scoring
 * @param exerciseDAL Data access layer for exercise operations
 * @param userEquipmentDAL Data access layer for user equipment operations
 * @param userExercisePreferenceDAL Data access layer for user exercise preference operations
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExercisePoolFactory(
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
    private val exerciseMatchingService: ExerciseMatchingService,
    private val exerciseDAL: ExerciseDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExercisePoolFactory::class.java)
    }

    /**
     * Creates a user exercise pool for the specified user by fetching all necessary data.
     * Implements sliding window logic to prevent exercise reuse based on available exercise pool size.
     *
     * @param userId The user ID to create the pool for
     * @return Mono containing the user's exercise pool
     */
    fun createPoolForUser(userId: String): Mono<UserExercisePool> {
        return Mono.zip(
            exerciseDAL.selectExercises(),
            userEquipmentDAL.selectUserEquipmentByUser(userId),
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId),
            programmedExerciseDAL.selectProgrammedExercisesByUserId(userId)
        ).map { tuple ->
            val allExercises = tuple.t1
            val userEquipment = tuple.t2
            val preferences = tuple.t3
            val previouslyUsedExercises = tuple.t4

            // Don't apply sliding window logic at pool initialization level
            // The sliding window logic will be applied during exercise selection
            UserExercisePool(
                allExercises = allExercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL,
                previouslyUsedExercises = emptyList(), // No exclusions at pool level
                userId = userId
            )
        }
    }

    /**
     * Applies sliding window logic to determine which exercises should be available for selection.
     * 
     * The sliding window works as follows:
     * 1. First, select all exercises possible for a user by filtering all exercises for user's constraints
     * 2. The length of this array is the size of the sliding window
     * 3. Get the user's programmed exercise history (sorted oldest to newest)
     * 4. If we have fewer programmed exercises than available, we take the first exercise out of the available exercises
     * 5. Otherwise, we take exercises from the end of the user's programmed exercises that meet the criteria
     * 6. If we get to the point we have the same count as the number of available exercises, we can stop
     * 7. If after this we still have less than the number of available exercises, we can still return the first available exercise
     * 8. Otherwise, we return the exercise that was scheduled longest ago for the user to promote rotation
     *
     * @param allExercises All available exercises in the system
     * @param preferences User's exercise preferences
     * @param previouslyUsedExercises List of previously used exercises
     * @return List of exercise names that should be excluded from the current pool
     */
    private fun applySlidingWindowLogic(
        allExercises: List<com.congen.model.Exercise>,
        preferences: List<com.congen.model.UserExercisePreference>,
        previouslyUsedExercises: List<com.congen.model.ProgrammedExercise>
    ): List<String> {
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
        val availablePrimaryUpperExercises = preferenceFilteredExercises.filter { exercise ->
            !exercise.isAccessory && exercise.isUpper && !(exercise.name.lowercase().contains("dumbbell"))
        }
        val availablePrimaryLowerExercises = preferenceFilteredExercises.filter { !it.isAccessory && !it.isUpper }
        val availableAccessoryUpperExercises = preferenceFilteredExercises.filter { it.isAccessory && it.isUpper }
        val availableAccessoryLowerExercises = preferenceFilteredExercises.filter { it.isAccessory && !it.isUpper }

        // Group previously used exercises by category
        val allUsedPrimaryUpperExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == false && exerciseModel?.isUpper == true
                }
                .map { it.exerciseName }
                .distinct()

        val allUsedPrimaryLowerExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == false && exerciseModel?.isUpper == false
                }
                .map { it.exerciseName }
                .distinct()

        val allUsedAccessoryUpperExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == true && exerciseModel?.isUpper == true
                }
                .map { it.exerciseName }
                .distinct()

        val allUsedAccessoryLowerExercises =
            previouslyUsedExercises
                .filter { exercise ->
                    val exerciseModel = allExercises.find { it.name == exercise.exerciseName }
                    exerciseModel?.isAccessory == true && exerciseModel?.isUpper == false
                }
                .map { it.exerciseName }
                .distinct()

        // Sliding window logic: Apply sliding window per category to ensure sufficient exercises remain
        // This ensures we always have variety while allowing older exercises to cycle back

        // Define sliding window sizes per category
        // The sliding window size is the number of available exercises in each category
        val primaryUpperWindowSize = availablePrimaryUpperExercises.size
        val primaryLowerWindowSize = availablePrimaryLowerExercises.size
        val accessoryUpperWindowSize = availableAccessoryUpperExercises.size
        val accessoryLowerWindowSize = availableAccessoryLowerExercises.size

        // Apply sliding window logic per category
        // The sliding window determines which exercises should be excluded to allow cycling
        // This implements the proper sliding window logic as described:
        // 1. If user has fewer programmed exercises than available, exclude all used exercises
        // 2. If user has more programmed exercises than available, exclude only the most recent ones (up to window size)
        // 3. This allows older exercises to cycle back in as new ones are used
        
        val excludedPrimaryUpperExercises =
            if (allUsedPrimaryUpperExercises.size <= primaryUpperWindowSize) {
                allUsedPrimaryUpperExercises
            } else {
                allUsedPrimaryUpperExercises.takeLast(primaryUpperWindowSize)
            }

        val excludedPrimaryLowerExercises =
            if (allUsedPrimaryLowerExercises.size <= primaryLowerWindowSize) {
                allUsedPrimaryLowerExercises
            } else {
                allUsedPrimaryLowerExercises.takeLast(primaryLowerWindowSize)
            }

        val excludedAccessoryUpperExercises =
            if (allUsedAccessoryUpperExercises.size <= accessoryUpperWindowSize) {
                allUsedAccessoryUpperExercises
            } else {
                allUsedAccessoryUpperExercises.takeLast(accessoryUpperWindowSize)
            }

        val excludedAccessoryLowerExercises =
            if (allUsedAccessoryLowerExercises.size <= accessoryLowerWindowSize) {
                allUsedAccessoryLowerExercises
            } else {
                allUsedAccessoryLowerExercises.takeLast(accessoryLowerWindowSize)
            }

        val excludedExercises =
            excludedPrimaryUpperExercises + excludedPrimaryLowerExercises +
                excludedAccessoryUpperExercises + excludedAccessoryLowerExercises

        logger.info(
            "Applied sliding window logic per category: Primary Upper: {}/{} (window: {}), " +
                "Primary Lower: {}/{} (window: {}), Accessory Upper: {}/{} (window: {}), " +
                "Accessory Lower: {}/{} (window: {}), total excluded: {}",
            excludedPrimaryUpperExercises.size,
            allUsedPrimaryUpperExercises.size,
            primaryUpperWindowSize,
            excludedPrimaryLowerExercises.size,
            allUsedPrimaryLowerExercises.size,
            primaryLowerWindowSize,
            excludedAccessoryUpperExercises.size,
            allUsedAccessoryUpperExercises.size,
            accessoryUpperWindowSize,
            excludedAccessoryLowerExercises.size,
            allUsedAccessoryLowerExercises.size,
            accessoryLowerWindowSize,
            excludedExercises.size
        )

        return excludedExercises
    }
}
