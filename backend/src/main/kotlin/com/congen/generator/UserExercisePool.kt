package com.congen.generator

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.model.Exercise
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
 * @param exerciseEquipmentDAL Data access layer for exercise equipment relationships
 * @param previouslyUsedExercises List of exercise names that have been used in previous weeks
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserExercisePool(
    private val allExercises: List<Exercise>,
    private val preferences: List<UserExercisePreference>,
    private val userEquipment: List<UserEquipment>,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val previouslyUsedExercises: List<String> = emptyList()
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserExercisePool::class.java)
    }

    /** Thread-safe map of available exercises by name. Exercises are removed immediately when used. */
    private val availableExercises = ConcurrentHashMap<String, Exercise>()

    /** Thread-safe set of used exercise names for tracking. */
    private val usedExerciseNames = ConcurrentHashMap.newKeySet<String>()

    init {
        // Initialize available exercises with all exercises that match user preferences
        // Equipment filtering will be done reactively during exercise selection
        val preferenceFilteredExercises =
            allExercises.filter { exercise ->
                val preference = preferences.find { pref -> pref.exerciseName == exercise.name }
                val shouldIncludeByPreference = when {
                    // If user has a preference to avoid this exercise, exclude it
                    preference?.shouldAvoid == true -> false
                    // If user has a preference for this exercise (shouldAvoid = false), include it
                    preference?.shouldAvoid == false -> true
                    // If no preference exists, include the exercise (default behavior)
                    else -> true
                }
                
                // Also exclude exercises that were used in previous weeks
                val notPreviouslyUsed = !previouslyUsedExercises.contains(exercise.name)
                
                shouldIncludeByPreference && notPreviouslyUsed
            }

        preferenceFilteredExercises.forEach { exercise ->
            availableExercises[exercise.name] = exercise
        }

        logger.info(
            "Initialized UserExercisePool with {} exercises (filtered by preferences and {} previously used exercises excluded)", 
            availableExercises.size, 
            previouslyUsedExercises.size
        )
        
        // Log available exercise names for debugging
        val availableExerciseNames = availableExercises.keys.sorted()
        logger.info("Available exercises: {}", availableExerciseNames)
        
        // Log excluded exercise names for debugging
        val excludedExerciseNames = allExercises.filter { !availableExercises.containsKey(it.name) }.map { it.name }.sorted()
        logger.info("Excluded exercises: {}", excludedExerciseNames)
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
        if (primaryExercises.size == 0 && usedExerciseNames.isNotEmpty()) {
            logger.info("Auto-refreshing pool: only {} primary exercises available, attempting refresh...", primaryExercises.size)
            refreshPool()
            val refreshedPrimaryExercises = availableExercises.values.filter { !it.isAccessory }
            logger.info("Available primary exercises after auto-refresh ({}): {}", refreshedPrimaryExercises.size, refreshedPrimaryExercises.map { it.name }.sorted())
            return refreshedPrimaryExercises
        }
        
        logger.info("Available primary exercises ({}): {}", primaryExercises.size, primaryExercises.map { it.name }.sorted())
        return primaryExercises
    }

    /**
     * Gets accessory exercises that are currently available.
     * Automatically refreshes the pool if it's running low on exercises.
     *
     * @return List of available accessory exercises
     */
    fun getAvailableAccessoryExercises(): List<Exercise> {
        val accessoryExercises = availableExercises.values.filter { it.isAccessory }
        
        // Auto-refresh if we're running low on accessory exercises
        if (accessoryExercises.size <= 5 && usedExerciseNames.isNotEmpty()) {
            logger.info("Auto-refreshing pool: only {} accessory exercises available, attempting refresh...", accessoryExercises.size)
            refreshPool()
            val refreshedAccessoryExercises = availableExercises.values.filter { it.isAccessory }
            logger.info("Available accessory exercises after auto-refresh ({}): {}", refreshedAccessoryExercises.size, refreshedAccessoryExercises.map { it.name }.sorted())
            return refreshedAccessoryExercises
        }
        
        logger.info("Available accessory exercises ({}): {}", accessoryExercises.size, accessoryExercises.map { it.name }.sorted())
        return accessoryExercises
    }

    /**
     * Refreshes the exercise pool by adding back all exercises except those used in the current week.
     * This is called when the pool is depleted for a specific day type or category.
     *
     * @return true if the pool was refreshed, false if no exercises were available to refresh
     */
    fun refreshPool(): Boolean {
        val currentUsedCount = usedExerciseNames.size
        
        // Add back exercises that are not currently available, but respect the sliding window exclusions
        // This allows exercise cycling across weeks while preventing duplicates within the same week
        val exercisesToRefresh = allExercises.filter { exercise ->
            val preference = preferences.find { pref -> pref.exerciseName == exercise.name }
            val shouldInclude = when {
                preference?.shouldAvoid == true -> false
                preference?.shouldAvoid == false -> true
                else -> true
            }
            // Add back exercises that are not currently available AND not used in the current week
            // We allow exercises from the previously used list to be added back (sliding window cycling)
            // but we prevent duplicates within the same week
            shouldInclude && 
            !availableExercises.containsKey(exercise.name) && 
            !usedExerciseNames.contains(exercise.name)
        }

        exercisesToRefresh.forEach { exercise ->
            availableExercises[exercise.name] = exercise
        }

        logger.info(
            "Refreshed exercise pool: added back {} exercises. Pool now has {} available exercises ({} used this week, {} total exercises in database)",
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
     * Gets the number of exercises that have been used in the current week.
     *
     * @return Number of used exercises
     */
    fun getUsedExerciseCount(): Int = usedExerciseNames.size

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
            .flatMap { exercise ->
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                    .filter { exerciseEquipment ->
                        val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                        
                        // Check if user has required equipment
                        val hasRequiredEquipment = userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                        
                        // Apply dumbbell restriction for primary upper body exercises
                        val isDumbbellRestricted = isPrimaryExercise && isUpperBody && exerciseEquipmentNames.contains("dumbbells")
                        
                        if (isDumbbellRestricted) {
                            logger.info("Excluding dumbbell exercise '{}' from primary upper body selection", exercise.name)
                        }
                        
                        hasRequiredEquipment && !isDumbbellRestricted
                    }
                    .map { exercise }
                    .onErrorReturn(exercise) // Fallback: include exercise if equipment check fails
            }
            .collectList()
            .flatMap { equipmentFilteredExercises ->
                if (equipmentFilteredExercises.isEmpty()) {
                    // Fallback: return all exercises if no equipment matches, but still respect dumbbell restrictions
                    logger.warn("No exercises available with user's equipment, falling back to all exercises")
                    if (isPrimaryExercise && isUpperBody) {
                        // For primary upper body exercises, we need to check equipment for each exercise
                        // to properly filter out dumbbell exercises even in fallback
                        Flux.fromIterable(exercises)
                            .flatMap { exercise ->
                                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                                    .map { exerciseEquipment ->
                                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                                        val isDumbbellRestricted = exerciseEquipmentNames.contains("dumbbells")
                                        if (isDumbbellRestricted) {
                                            Mono.empty<Exercise>() // Filter out dumbbell exercises
                                        } else {
                                            Mono.just(exercise)
                                        }
                                    }
                                    .flatMap { it }
                                    .onErrorResume { Mono.empty<Exercise>() } // Exclude exercise if equipment check fails
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
     * @param exerciseMuscleDAL Data access layer for exercise muscle relationships
     * @return Mono containing list of exercises that target the specified muscles
     */
    fun filterExercisesByMuscles(
        exercises: List<Exercise>,
        targetMuscles: List<String>,
        exerciseMuscleDAL: ExerciseMuscleDAL
    ): Mono<List<Exercise>> {
        if (exercises.isEmpty()) {
            return Mono.just(emptyList())
        }

        // If no target muscles specified, return all exercises (for primary exercises)
        if (targetMuscles.isEmpty()) {
            logger.info("No target muscles specified, returning all {} exercises", exercises.size)
            return Mono.just(exercises)
        }

        return Flux.fromIterable(exercises)
            .flatMap { exercise ->
                exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                    .filter { exerciseMuscles ->
                        val exerciseMuscleNames = exerciseMuscles.map { it.muscleName.lowercase() }.toSet()
                        val targetMuscleNames = targetMuscles.map { it.lowercase() }.toSet()
                        exerciseMuscleNames.any { muscle -> targetMuscleNames.contains(muscle) }
                    }
                    .map { exercise }
                    .onErrorReturn(exercise) // Fallback: include exercise if muscle check fails
            }
            .collectList()
            .flatMap { muscleFilteredExercises ->
                if (muscleFilteredExercises.isEmpty()) {
                    // For accessory exercises with weak muscles, be more strict about targeting
                    // Only fall back if there are truly no exercises available
                    logger.warn("No exercises available for target muscles: {}, returning empty list to force fallback", targetMuscles)
                    Mono.just(emptyList())
                } else {
                    Mono.just(muscleFilteredExercises)
                }
            }
    }
}
