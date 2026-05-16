package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a simple pool of exercises available to a user.
 *
 * This class maintains a thread-safe pool of exercises that have been filtered
 * based on user preferences and equipment during construction. Exercises are removed from
 * the pool when they are selected to prevent duplicates.
 *
 * @param allExercises All available exercises in the system
 * @param preferences User's exercise preferences
 * @param userEquipment User's available equipment
 * @param exerciseEquipmentMappings Pre-computed mappings of exercise names to their equipment requirements
 * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
 * @param previouslyUsedExercises List of exercise names that have been used in previous weeks
 * @param userId The ID of the user
 * @param excludedExercises Set of exercise names to exclude from the pool
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserExercisePool(
    private val allExercises: List<Exercise>,
    private val preferences: List<UserExercisePreference>,
    private val userEquipment: List<UserEquipment>,
    private val exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
    private val exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
    private val previouslyUsedExercises: List<String> = emptyList(),
    private val userId: String = "",
    private val excludedExercises: Set<String> = emptySet()
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserExercisePool::class.java)
    }

    /** Thread-safe map of available exercises by name. Exercises are removed immediately when used. */
    private val availableExercises = ConcurrentHashMap<String, Exercise>()

    /** Thread-safe set of used exercise names for tracking. */
    private val usedExerciseNames = ConcurrentHashMap.newKeySet<String>()

    /**
     * Gets the user ID for this exercise pool.
     *
     * @return The user ID
     */
    fun getUserId(): String = userId

    init {
        val preferenceFilteredExercises = allExercises.filter { exerciseMatchesPoolPreferences(it) }

        preferenceFilteredExercises.forEach { exercise ->
            availableExercises[exercise.name] = exercise
        }

        logger.info(
            "Initialized UserExercisePool with {} exercises (filtered by preferences and {} excluded by sliding window)",
            availableExercises.size,
            excludedExercises.size
        )
    }

    /**
     * Gets the current number of available exercises.
     *
     * @return Number of available exercises
     */
    fun getAvailableExerciseCount(): Int = availableExercises.size

    /**
     * Marks an exercise as used and immediately removes it from the available pool.
     * This method is thread-safe and package-private - only ExerciseSelectionService should call this.
     *
     * @param exerciseName The name of the exercise to mark as used
     * @return true if the exercise was available and is now marked as used, false if it was already used
     */
    fun markExerciseAsUsed(exerciseName: String): Boolean {
        val exercise = availableExercises.remove(exerciseName)
        return if (exercise != null) {
            usedExerciseNames.add(exerciseName)
            logger.info(
                "Marked exercise as used and removed from pool: {}. Available exercises remaining: {}",
                exerciseName,
                availableExercises.size
            )
            true
        } else {
            logger.info("Exercise was already used or not available: {}", exerciseName)
            false
        }
    }

    /**
     * Gets all currently available exercises (thread-safe).
     *
     * @return List of currently available exercises
     */
    fun getAvailableExercises(): List<Exercise> = availableExercises.values.toList()

    /**
     * Gets primary exercises (non-accessory) that are currently available.
     * Automatically refreshes the pool if it's running low on exercises.
     *
     * @return List of available primary exercises
     */
    fun getAvailablePrimaryExercises(): List<Exercise> {
        val primaryExercises = availableExercises.values.filter { !it.isAccessory }

        // Auto-refresh if we're running low on primary exercises
        if (primaryExercises.isEmpty()) {
            logger.info("Auto-refreshing pool: no primary exercises available, attempting refresh...")
            refreshPool()
            val refreshedPrimaryExercises = availableExercises.values.filter { !it.isAccessory }
            logger.info(
                "Available primary exercises after auto-refresh ({}): {}",
                refreshedPrimaryExercises.size,
                refreshedPrimaryExercises.map { it.name }.sorted()
            )
            return refreshedPrimaryExercises
        }

        logger.info("Available primary exercises ({}): {}", primaryExercises.size, primaryExercises.map { it.name }.sorted())
        return primaryExercises
    }

    /**
     * Gets upper body accessory exercises that are currently available.
     * Automatically refreshes the pool if upper body accessories are exhausted.
     *
     * @return List of available upper body accessory exercises
     */
    fun getAvailableAccessoryUpperExercises(): List<Exercise> {
        val accessoryUpperExercises = availableExercises.values.filter { it.isAccessory && it.isUpper }

        if (accessoryUpperExercises.isEmpty()) {
            logger.info("Auto-refreshing pool: no upper body accessory exercises available, attempting refresh...")
            refreshPool()
            val refreshedAccessoryUpperExercises = availableExercises.values.filter { it.isAccessory && it.isUpper }
            logger.info(
                "Available upper body accessory exercises after auto-refresh ({}): {}",
                refreshedAccessoryUpperExercises.size,
                refreshedAccessoryUpperExercises.map { it.name }.sorted()
            )
            return refreshedAccessoryUpperExercises
        }

        logger.info(
            "Available upper body accessory exercises ({}): {}",
            accessoryUpperExercises.size,
            accessoryUpperExercises.map {
                it.name
            }.sorted()
        )
        return accessoryUpperExercises
    }

    /**
     * Gets lower body accessory exercises that are currently available.
     * Automatically refreshes the pool if lower body accessories are exhausted.
     *
     * @return List of available lower body accessory exercises
     */
    fun getAvailableAccessoryLowerExercises(): List<Exercise> {
        val accessoryLowerExercises = availableExercises.values.filter { it.isAccessory && !it.isUpper }

        if (accessoryLowerExercises.isEmpty()) {
            logger.info("Auto-refreshing pool: no lower body accessory exercises available, attempting refresh...")
            refreshPool()
            val refreshedAccessoryLowerExercises = availableExercises.values.filter { it.isAccessory && !it.isUpper }
            logger.info(
                "Available lower body accessory exercises after auto-refresh ({}): {}",
                refreshedAccessoryLowerExercises.size,
                refreshedAccessoryLowerExercises.map { it.name }.sorted()
            )
            return refreshedAccessoryLowerExercises
        }

        logger.info(
            "Available lower body accessory exercises ({}): {}",
            accessoryLowerExercises.size,
            accessoryLowerExercises.map {
                it.name
            }.sorted()
        )
        return accessoryLowerExercises
    }

    /**
     * Refreshes the exercise pool when selection has depleted available exercises.
     *
     * Applies, in order: same-week reuse for non-sliding-window exercises; sliding-window
     * accessories for mixed-day slots; conditioning accessories for reuse across DE days in the week.
     *
     * @return true if at least one exercise was made available again
     */
    fun refreshPool(): Boolean {
        val currentUsedCount = usedExerciseNames.size
        val exercisesToRefresh = LinkedHashSet<Exercise>()

        exercisesToRefresh.addAll(findStandardRefreshCandidates())
        exercisesToRefresh.addAll(findSlidingWindowExcludedAccessoryRefreshCandidates())
        exercisesToRefresh.addAll(findConditioningAccessoryRefreshCandidates())

        exercisesToRefresh.forEach { exercise ->
            availableExercises[exercise.name] = exercise
        }

        logger.info(
            "Refreshed exercise pool: added back {} exercises. " +
                "Pool now has {} available exercises ({} used this week, {} total exercises in database)",
            exercisesToRefresh.size,
            availableExercises.size,
            currentUsedCount,
            allExercises.size
        )

        if (exercisesToRefresh.isNotEmpty()) {
            logger.info("Exercises added back during refresh: {}", exercisesToRefresh.map { it.name }.sorted())
        }

        return exercisesToRefresh.isNotEmpty()
    }

    /**
     * Returns the set of exercise names that have been used (marked and removed from the pool).
     * Used so the generator can exclude these from the next workout's pool in the same week.
     *
     * @return Copy of the set of used exercise names
     */
    fun getUsedExerciseNames(): Set<String> = usedExerciseNames.toSet()

    /**
     * Filters exercises by equipment availability reactively.
     *
     * @param exercises List of exercises to filter
     * @param isPrimaryExercise Whether this is for a primary exercise (affects dumbbell restrictions)
     * @param isUpperBody Whether this is for an upper body exercise (affects dumbbell restrictions)
     * @return Mono containing list of exercises that can be performed with available equipment
     */
    fun filterExercisesByEquipment(
        exercises: List<Exercise>,
        isPrimaryExercise: Boolean = false,
        isUpperBody: Boolean = false
    ): Mono<List<Exercise>> {
        if (exercises.isEmpty()) {
            return Mono.just(emptyList())
        }

        return Flux.fromIterable(exercises)
            .filter { exercise ->
                val exerciseEquipment = exerciseEquipmentMappings[exercise.name] ?: emptyList()
                val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()

                val hasRequiredEquipment =
                    when {
                        exerciseEquipmentNames.isEmpty() -> true
                        userEquipmentNames.isEmpty() -> true
                        else -> userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                    }

                // Apply dumbbell restriction for primary upper body exercises
                val isDumbbellRestricted = isPrimaryExercise && isUpperBody && exerciseEquipmentNames.contains("dumbbells")

                if (isDumbbellRestricted) {
                    logger.info("Excluding dumbbell exercise '{}' from primary upper body selection", exercise.name)
                }

                hasRequiredEquipment && !isDumbbellRestricted
            }
            .collectList()
            .flatMap { equipmentFilteredExercises ->
                if (equipmentFilteredExercises.isEmpty()) {
                    // Fallback: return all exercises if no equipment matches
                    // But still respect dumbbell restrictions for primary upper body exercises
                    logger.warn("No exercises available with user's equipment, falling back to all exercises")
                    if (isPrimaryExercise && isUpperBody) {
                        // For primary upper body exercises, filter out dumbbell exercises even in fallback
                        Flux.fromIterable(exercises)
                            .filter { exercise ->
                                val exerciseEquipment = exerciseEquipmentMappings[exercise.name] ?: emptyList()
                                val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                                val isDumbbellRestricted = exerciseEquipmentNames.contains("dumbbells")
                                !isDumbbellRestricted
                            }
                            .collectList()
                    } else {
                        Mono.just(exercises)
                    }
                } else {
                    Mono.just(equipmentFilteredExercises)
                }
            }
    }

    /**
     * Filters exercises by target muscles reactively.
     *
     * @param exercises List of exercises to filter
     * @param targetMuscles List of target muscles to focus on
     * @return Mono containing list of exercises that target the specified muscles
     */
    fun filterExercisesByMuscles(
        exercises: List<Exercise>,
        targetMuscles: List<String>
    ): Mono<List<Exercise>> {
        if (exercises.isEmpty()) {
            return Mono.just(emptyList())
        }

        // If no target muscles specified, return all exercises (for primary exercises)
        if (targetMuscles.isEmpty()) {
            logger.info("No target muscles specified, returning all {} exercises", exercises.size)
            return Mono.just(exercises)
        }

        val muscleFilteredExercises =
            exercises.filter { exercise ->
                val exerciseMuscles = exerciseMuscleMappings[exercise.name] ?: emptyList()
                val exerciseMuscleNames = exerciseMuscles.map { it.muscleName.lowercase() }.toSet()
                val targetMuscleNames = targetMuscles.map { it.lowercase() }.toSet()
                val hasTargetMuscle = exerciseMuscleNames.any { muscle -> targetMuscleNames.contains(muscle) }
                hasTargetMuscle
            }

        return if (muscleFilteredExercises.isEmpty()) {
            // If no exact muscle matches found, fall back to all available exercises
            // This ensures we don't get stuck when muscle targeting is too specific
            logger.warn("No exercises available for target muscles: {}, falling back to all available exercises", targetMuscles)
            Mono.just(exercises)
        } else {
            Mono.just(muscleFilteredExercises)
        }
    }

    /**
     * Gets all exercises in the system (not filtered by preferences or usage).
     *
     * @return List of all exercises
     */
    fun getAllExercises(): List<Exercise> = allExercises

    private fun findStandardRefreshCandidates(): List<Exercise> {
        return allExercises.filter { exercise ->
            if (!exerciseMatchesRefreshPreferences(exercise)) {
                return@filter false
            }
            if (excludedExercises.contains(exercise.name)) {
                return@filter false
            }
            if (usedExerciseNames.contains(exercise.name)) {
                return@filter false
            }
            !availableExercises.containsKey(exercise.name)
        }
    }

    private fun findSlidingWindowExcludedAccessoryRefreshCandidates(): List<Exercise> {
        return allExercises.filter { exercise ->
            if (!exercise.isAccessory) {
                return@filter false
            }
            if (!excludedExercises.contains(exercise.name)) {
                return@filter false
            }
            if (!exerciseMatchesRefreshPreferences(exercise)) {
                return@filter false
            }
            !availableExercises.containsKey(exercise.name)
        }
    }

    private fun findConditioningAccessoryRefreshCandidates(): List<Exercise> {
        return allExercises.filter { exercise ->
            if (!exercise.isAccessory) {
                return@filter false
            }
            val exerciseEquipment = exerciseEquipmentMappings[exercise.name] ?: emptyList()
            if (!ConjugateConstants.exerciseUsesConditioningEquipment(exerciseEquipment)) {
                return@filter false
            }
            if (!exerciseMatchesRefreshPreferences(exercise)) {
                return@filter false
            }
            !availableExercises.containsKey(exercise.name)
        }
    }

    private fun exerciseMatchesRefreshPreferences(exercise: Exercise): Boolean {
        val preference = preferences.find { pref -> pref.exerciseName == exercise.name }
        val shouldIncludeByPreference =
            when {
                preference?.shouldAvoid == true -> false
                preference?.shouldAvoid == false -> true
                else -> true
            }

        val isPrimaryUpperBody = !exercise.isAccessory && exercise.isUpper
        val isDumbbellExercise = exercise.name.lowercase().contains("dumbbell")
        val shouldExcludeDumbbell = isPrimaryUpperBody && isDumbbellExercise

        return shouldIncludeByPreference && !shouldExcludeDumbbell
    }

    private fun exerciseMatchesPoolPreferences(exercise: Exercise): Boolean {
        val isExcludedBySlidingWindow = excludedExercises.contains(exercise.name)
        return exerciseMatchesRefreshPreferences(exercise) && !isExcludedBySlidingWindow
    }

    /**
     * Gets the user's equipment.
     *
     * @return List of user equipment
     */
    fun getUserEquipment(): List<UserEquipment> = userEquipment

    /**
     * Gets the user's exercise preferences.
     *
     * @return List of user exercise preferences
     */
    fun getUserPreferences(): List<UserExercisePreference> = preferences

    /**
     * Gets the list of previously used exercises.
     *
     * @return List of previously used exercise names
     */
    fun getPreviouslyUsedExercises(): List<String> = previouslyUsedExercises
}
