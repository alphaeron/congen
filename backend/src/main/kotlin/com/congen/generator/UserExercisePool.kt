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
 * @property allExercises All available exercises in the system
 * @property preferences User's exercise preferences
 * @property userEquipment User's available equipment
 * @property exerciseEquipmentDAL Data access layer for exercise equipment relationships
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserExercisePool(
    private val allExercises: List<Exercise>,
    private val preferences: List<UserExercisePreference>,
    private val userEquipment: List<UserEquipment>,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL
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
                when {
                    // If user has a preference to avoid this exercise, exclude it
                    preference?.shouldAvoid == true -> false
                    // If user has a preference for this exercise (shouldAvoid = false), include it
                    preference?.shouldAvoid == false -> true
                    // If no preference exists, include the exercise (default behavior)
                    else -> true
                }
            }

        preferenceFilteredExercises.forEach { exercise ->
            availableExercises[exercise.name] = exercise
        }

        logger.debug("Initialized UserExercisePool with {} exercises (filtered by preferences only)", availableExercises.size)
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
            logger.debug(
                "Marked exercise as used and removed from pool: {}. Available exercises remaining: {}",
                exerciseName,
                availableExercises.size
            )
            true
        } else {
            logger.debug("Exercise was already used or not available: {}", exerciseName)
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
     *
     * @return List of available primary exercises
     */
    fun getAvailablePrimaryExercises(): List<Exercise> {
        return availableExercises.values.filter { !it.isAccessory }
    }

    /**
     * Gets accessory exercises that are currently available.
     *
     * @return List of available accessory exercises
     */
    fun getAvailableAccessoryExercises(): List<Exercise> {
        return availableExercises.values.filter { it.isAccessory }
    }

    /**
     * Filters exercises by equipment availability reactively.
     *
     * @param exercises List of exercises to filter
     * @return Mono containing list of exercises that can be performed with available equipment
     */
    fun filterExercisesByEquipment(exercises: List<Exercise>): Mono<List<Exercise>> {
        if (exercises.isEmpty()) {
            return Mono.just(emptyList())
        }

        return Flux.fromIterable(exercises)
            .flatMap { exercise ->
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                    .filter { exerciseEquipment ->
                        val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                        userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                    }
                    .map { exercise }
                    .onErrorReturn(exercise) // Fallback: include exercise if equipment check fails
            }
            .collectList()
            .flatMap { equipmentFilteredExercises ->
                if (equipmentFilteredExercises.isEmpty()) {
                    // Fallback: return all exercises if no equipment matches
                    logger.warn("No exercises available with user's equipment, falling back to all exercises")
                    Mono.just(exercises)
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
        if (exercises.isEmpty() || targetMuscles.isEmpty()) {
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
                    // Fallback: return all exercises if no muscle matches
                    logger.warn("No exercises available for target muscles: {}, falling back to all exercises", targetMuscles)
                    Mono.just(exercises)
                } else {
                    Mono.just(muscleFilteredExercises)
                }
            }
    }
}
