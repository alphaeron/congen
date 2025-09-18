package com.congen.generator

import com.congen.exceptions.ExerciseSelectionException
import com.congen.generator.ConjugateConstants.MAX_MUSCLES_FOR_WARMUP
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service for selecting exercises based on various criteria including
 * user preferences, equipment availability, and target muscles.
 */
@Service
class ExerciseSelectionService(
    private val movementBalanceService: MovementBalanceService,
    private val exerciseMatchingService: ExerciseMatchingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseSelectionService::class.java)
    }

    /**
     * Determines weak muscles based on user's 1RM data and exercise history.
     *
     * @param dayType The type of day to determine appropriate weak muscles for
     * @return List of weak muscle groups to target for the given day type
     */
    fun determineWeakMuscles(dayType: String): List<String> {
        // For now, return day-type aware default weak muscles
        // In a real implementation, this would analyze 1RM data and exercise history
        // to identify areas that need more attention, then filter by day type
        return ConjugateConstants.getWeakMusclesForDayType(dayType)
    }

    /**
     * Main entry point for exercise selection. This method handles all exercise selection
     * and ensures that exercises are properly removed from the pool after selection.
     *
     * @param userExercisePool The user's exercise pool
     * @param targetMuscles List of target muscles to focus on
     * @param isAccessory Whether this is for an accessory exercise
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @param isWarmup Whether this is for a warmup exercise (optional, defaults to false)
     * @return Mono containing selected exercise or null if none available
     */
    fun selectExercise(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        isAccessory: Boolean,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        isWarmup: Boolean = false,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<Exercise> {
        return selectRotatingExerciseInternal(
            userExercisePool = userExercisePool,
            targetMuscles = targetMuscles,
            isAccessory = isAccessory,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            isWarmup = isWarmup,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings
        ).flatMap { selectedExercise ->
            if (selectedExercise != null) {
                // Mark the exercise as used and removed from the pool
                userExercisePool.markExerciseAsUsed(selectedExercise.name)

                Mono.just(selectedExercise)
            } else {
                logger.error(
                    "No suitable exercise found for the given criteria. " +
                        "Parameters: targetMuscles={}, isAccessory={}, workoutType={}, dayType={}, movementBalanceState={}",
                    targetMuscles,
                    isAccessory,
                    workoutType,
                    dayType,
                    movementBalanceState
                )
                // No exercise found - this indicates a problem with filtering or pool management
                Mono.error(
                    ExerciseSelectionException.noSuitableExerciseFound(
                        targetMuscles = targetMuscles,
                        isAccessory = isAccessory,
                        workoutType = workoutType,
                        dayType = dayType,
                        movementBalanceState = movementBalanceState
                    )
                )
            }
        }
    }

    /**
     * Internal method for selecting a rotating exercise based on various criteria.
     * This method does NOT handle exercise removal - that's done by the main entry point.
     *
     * @param userExercisePool The user's exercise pool
     * @param targetMuscles List of target muscles to focus on
     * @param isAccessory Whether this is for an accessory exercise
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @param isWarmup Whether this is for a warmup exercise (optional, defaults to false)
     * @return Mono containing selected exercise or null if none available
     */
    private fun selectRotatingExerciseInternal(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        isAccessory: Boolean,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        isWarmup: Boolean = false,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
    ): Mono<Exercise?> {
        return selectRotatingExerciseInternalImpl(
            userExercisePool = userExercisePool,
            targetMuscles = targetMuscles,
            isAccessory = isAccessory,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            isWarmup = isWarmup,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings,
            isSecondary = false
        ).onErrorResume { error ->
            if (error is ExerciseSelectionException) {
                logger.info("Pool refresh needed, refreshing and retrying exercise selection...")
                val poolRefreshed = userExercisePool.refreshPool()
                if (poolRefreshed) {
                    logger.info("Pool refreshed successfully, retrying exercise selection...")
                    selectRotatingExerciseInternalImpl(
                        userExercisePool = userExercisePool,
                        targetMuscles = targetMuscles,
                        isAccessory = isAccessory,
                        workoutType = workoutType,
                        dayType = dayType,
                        movementBalanceState = movementBalanceState,
                        isWarmup = isWarmup,
                        exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                        exerciseMuscleMappings = exerciseMuscleMappings,
                        exerciseEquipmentMappings = exerciseEquipmentMappings,
                        isSecondary = false
                    )
                } else {
                    logger.warn("Pool refresh failed, skipping exercise selection to continue workout generation")
                    Mono.empty()
                }
            } else {
                Mono.error(error)
            }
        }
    }

    private fun selectRotatingExerciseInternalImpl(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        isAccessory: Boolean,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        isWarmup: Boolean = false,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        isSecondary: Boolean = false
    ): Mono<Exercise?> {
        return Mono.defer {
            // Get fresh available exercises from the pool each time this method is called
            val availableExercises =
                if (isAccessory) {
                    userExercisePool.getAvailableAccessoryExercises()
                } else {
                    userExercisePool.getAvailablePrimaryExercises()
                }

            if (availableExercises.isEmpty()) {
                logger.error("No available exercises found for isAccessory: {}", isAccessory)
                return@defer Mono.error(
                    ExerciseSelectionException(
                        "No available exercises found for isAccessory: $isAccessory"
                    )
                )
            }

            // Apply day-type filtering first
            var dayTypeFilteredExercises = filterExercisesByDayType(availableExercises, dayType)
            logger.info(
                "Day-type filtering for dayType '{}': {} available exercises -> {} filtered exercises",
                dayType,
                availableExercises.size,
                dayTypeFilteredExercises.size
            )
            if (dayTypeFilteredExercises.isEmpty()) {
                logger.warn(
                    "No exercises available after day-type filtering for dayType: {} and isAccessory: {}. " +
                        "Attempting to refresh pool...",
                    dayType,
                    isAccessory
                )

                // Let the pool handle the refresh logic
                val poolRefreshed = userExercisePool.refreshPool()
                if (poolRefreshed) {
                    logger.info("Pool refreshed successfully, retrying exercise selection...")
                    // Retry with refreshed pool
                    val refreshedAvailableExercises = if (isAccessory) {
                        userExercisePool.getAvailableAccessoryExercises()
                    } else {
                        userExercisePool.getAvailablePrimaryExercises()
                    }
                    val refreshedDayTypeFilteredExercises = filterExercisesByDayType(refreshedAvailableExercises, dayType)
                    if (refreshedDayTypeFilteredExercises.isEmpty()) {
                        // For primary exercises, we must have at least one exercise available
                        // If day-type filtering fails even after pool refresh, fall back to any available primary exercise
                        if (!isAccessory) {
                            logger.warn("No exercises available after day-type filtering for primary exercise even after pool refresh, falling back to any available primary exercise")
                            val fallbackExercises = refreshedAvailableExercises.filter { !it.isAccessory }
                            if (fallbackExercises.isNotEmpty()) {
                                dayTypeFilteredExercises = fallbackExercises
                                logger.info("Using fallback primary exercise: {}", fallbackExercises.first().name)
                            } else {
                                logger.error("No primary exercises available even after fallback")
                                return@defer Mono.error(
                                    ExerciseSelectionException.noSuitableExerciseFound(
                                        targetMuscles = targetMuscles,
                                        isAccessory = isAccessory,
                                        workoutType = workoutType,
                                        dayType = dayType,
                                        movementBalanceState = movementBalanceState
                                    )
                                )
                            }
                        } else {
                            logger.error("No exercises available after day-type filtering for dayType: {} and isAccessory: {} even after pool refresh", dayType, isAccessory)
                            return@defer Mono.error(
                                ExerciseSelectionException.noSuitableExerciseFound(
                                    targetMuscles = targetMuscles,
                                    isAccessory = isAccessory,
                                    workoutType = workoutType,
                                    dayType = dayType,
                                    movementBalanceState = movementBalanceState
                                )
                            )
                        }
                    } else {
                        // Continue with refreshed exercises
                        dayTypeFilteredExercises = refreshedDayTypeFilteredExercises
                    }
                } else {
                    // For primary exercises, we must have at least one exercise available
                    // If pool refresh fails, fall back to any available primary exercise
                    if (!isAccessory) {
                        logger.warn("Pool refresh failed for primary exercise, falling back to any available primary exercise")
                        val fallbackExercises = availableExercises.filter { !it.isAccessory }
                        if (fallbackExercises.isNotEmpty()) {
                            dayTypeFilteredExercises = fallbackExercises
                            logger.info("Using fallback primary exercise: {}", fallbackExercises.first().name)
                        } else {
                            logger.error("No primary exercises available even after fallback")
                            return@defer Mono.error(
                                ExerciseSelectionException.noSuitableExerciseFound(
                                    targetMuscles = targetMuscles,
                                    isAccessory = isAccessory,
                                    workoutType = workoutType,
                                    dayType = dayType,
                                    movementBalanceState = movementBalanceState
                                )
                            )
                        }
                    } else {
                        logger.error("Pool refresh failed for dayType: {} and isAccessory: {}", dayType, isAccessory)
                        return@defer Mono.error(
                            ExerciseSelectionException.noSuitableExerciseFound(
                                targetMuscles = targetMuscles,
                                isAccessory = isAccessory,
                                workoutType = workoutType,
                                dayType = dayType,
                                movementBalanceState = movementBalanceState
                            )
                        )
                    }
                }
            }

            // Filter out plyometric exercises only for warmup selection
            val exercisesAfterPlyometricFiltering =
                if (isWarmup) {
                    val nonPlyometricExercises = dayTypeFilteredExercises.filter { it.movementType != MovementType.PLYOMETRIC }
                    if (nonPlyometricExercises.isEmpty()) {
                        logger.error(
                            "No non-plyometric exercises available after filtering for dayType: {} and isAccessory: {} (warmup)",
                            dayType,
                            isAccessory
                        )
                        return@defer Mono.error(
                            IllegalStateException(
                                "No non-plyometric exercises available after filtering for " +
                                    "dayType: $dayType and isAccessory: $isAccessory (warmup)"
                            )
                        )
                    }
                    nonPlyometricExercises
                } else {
                    dayTypeFilteredExercises
                }

            // Apply workout-type filtering only for non-accessory exercises
            val exercisesAfterWorkoutTypeFiltering =
                if (isAccessory) {
                    // For accessory exercises, skip workout-type filtering
                    Mono.just(exercisesAfterPlyometricFiltering)
                } else {
                    // For primary/secondary exercises, apply workout-type filtering
                    filterExercisesByWorkoutType(exercisesAfterPlyometricFiltering, workoutType, exerciseWorkoutTypeMappings)
                }

            exercisesAfterWorkoutTypeFiltering
                .flatMap { workoutTypeFilteredExercises ->
                    if (workoutTypeFilteredExercises.isEmpty()) {
                        logger.warn(
                            "No exercises available after workout-type filtering for workoutType: {} and isAccessory: {}",
                            workoutType,
                            isAccessory
                        )
                        Mono.error(
                            ExerciseSelectionException.noExercisesAfterWorkoutTypeFiltering(
                                workoutType = workoutType,
                                isAccessory = isAccessory
                            )
                        )
                    } else {
                        Mono.just(workoutTypeFilteredExercises)
                    }
                }
                .flatMap { workoutTypeFilteredExercises ->
                    // Apply banded exercise restrictions
                    val bandedFilteredExercises = applyBandedExerciseRestrictions(
                        workoutTypeFilteredExercises,
                        dayType = dayType,
                        isSecondary = isSecondary,
                        isAccessory = isAccessory
                    )

                    // Filter exercises by equipment and muscles reactively
                    userExercisePool
                        .filterExercisesByEquipment(
                            bandedFilteredExercises,
                            isPrimaryExercise = !isAccessory,
                            isUpperBody = dayType.contains("Upper")
                        )
                        .flatMap { equipmentFilteredExercises ->
                            // Apply muscle count filtering for warmup exercises
                            if (isWarmup) {
                                filterExercisesByMuscleCountForWarmupReactive(equipmentFilteredExercises, exerciseMuscleMappings)
                            } else {
                                Mono.just(equipmentFilteredExercises)
                            }
                        }
                        .flatMap { muscleCountFilteredExercises ->
                            // Handle empty target muscles for both warmup and non-warmup exercises
                            if (targetMuscles.isEmpty()) {
                                // No target muscles specified, use all available exercises
                                logger.debug(
                                    "No target muscles specified, using all {} available exercises (isWarmup: {})",
                                    muscleCountFilteredExercises.size,
                                    isWarmup
                                )
                                Mono.just(muscleCountFilteredExercises)
                            } else {
                                // Try to find exercises with target muscles first
                                userExercisePool.filterExercisesByMuscles(
                                    muscleCountFilteredExercises,
                                    targetMuscles
                                )
                            }
                        }
                        .flatMap { filteredExercises ->
                            logger.debug(
                                "Muscle filtering result: {} exercises found for target muscles: {} (isAccessory: {})",
                                filteredExercises.size,
                                targetMuscles,
                                isAccessory
                            )
                            // UserExercisePool.filterExercisesByMuscles should never return empty list due to fallback logic
                            // No rotation logic - use all filtered exercises
                            val exercisesToChooseFrom = filteredExercises

                            // Apply movement balance constraints if available
                            val finalExercises =
                                if (movementBalanceState != null) {
                                    movementBalanceService.prioritizeExercisesForBalance(
                                        exercises = exercisesToChooseFrom,
                                        currentState = movementBalanceState
                                    )
                                } else {
                                    exercisesToChooseFrom
                                }

                            if (finalExercises.isEmpty()) {
                                logger.error(
                                    "No exercises available after movement balance constraints for isAccessory: {}",
                                    isAccessory
                                )
                                // No exercises available after movement balance constraints
                                Mono.error(
                                    IllegalStateException(
                                        "No exercises available after movement balance constraints for isAccessory: $isAccessory"
                                    )
                                )
                            } else {
                                // Apply sliding window logic to select the least recently used exercise
                                selectLeastRecentlyUsedExercise(finalExercises, userExercisePool, isAccessory)
                            }
                        }
                }
        }
    }

    /**
     * Selects the least recently used exercise from the available exercises using sliding window logic.
     * 
     * The sliding window works by:
     * 1. Getting all exercises the user has used in previous workouts (sorted oldest to newest)
     * 2. Filtering to only include exercises of the same type (accessory vs primary)
     * 3. The sliding window size is the total number of available exercises of the same type
     * 4. If we have fewer programmed exercises than available, return the first available exercise
     * 5. Otherwise, return the exercise that was scheduled longest ago (first in the sorted list)
     * 
     * @param availableExercises List of exercises available for selection
     * @param userExercisePool The user's exercise pool
     * @param isAccessory Whether we're selecting accessory exercises (affects sliding window logic)
     * @return Mono containing the least recently used exercise
     */
    private fun selectLeastRecentlyUsedExercise(
        availableExercises: List<Exercise>,
        userExercisePool: UserExercisePool,
        isAccessory: Boolean
    ): Mono<Exercise> {
        val userId = userExercisePool.getUserId()
        
        if (userId.isEmpty()) {
            // No user ID, return first available exercise
            return Mono.just(availableExercises.first())
        }

        // Use prepared data from UserExercisePool instead of making database calls
        val previouslyUsedExerciseNames = userExercisePool.getPreviouslyUsedExercises()
        
        if (previouslyUsedExerciseNames.isEmpty()) {
            // No previous exercises, return the first available exercise
            return Mono.just(availableExercises.first())
        } else {
            // Filter previously used exercises to only those that match our available exercises
            // AND are of the same type (accessory vs primary)
            val matchingPreviouslyUsedExercises = previouslyUsedExerciseNames.filter { exerciseName ->
                availableExercises.any { availableExercise ->
                    availableExercise.name == exerciseName &&
                    availableExercise.isAccessory == isAccessory
                }
            }

            // The sliding window size is the total number of available exercises
            val windowSize = availableExercises.size

            // Implement robust cycling logic that ensures all exercises are used before repetition
            // but never runs out of exercises to choose from
            return if (matchingPreviouslyUsedExercises.size < windowSize) {
                // Not all exercises have been used yet - prioritize unused exercises
                val unusedExercises = availableExercises.filter { exercise ->
                    !matchingPreviouslyUsedExercises.contains(exercise.name)
                }
                
                val selectedExercise = if (unusedExercises.isNotEmpty()) {
                    // Prefer unused exercises to ensure variety
                    unusedExercises.first()
                } else {
                    // If all available exercises have been used, select the least recently used one
                    val leastRecentlyUsedExerciseName = matchingPreviouslyUsedExercises.first()
                    availableExercises.find { exercise ->
                        exercise.name == leastRecentlyUsedExerciseName
                    } ?: availableExercises.first()
                }
                
                logger.debug("Selected exercise for variety: {} from {} available exercises, {} unused exercises, {} matching previously used exercises",
                    selectedExercise.name, availableExercises.size, unusedExercises.size, matchingPreviouslyUsedExercises.size)
                
                Mono.just(selectedExercise)
            } else {
                // All exercises have been used - implement proper cycling
                // Find the exercise that was scheduled longest ago (first in the sorted list)
                val leastRecentlyUsedExerciseName = matchingPreviouslyUsedExercises.first()
                val leastRecentlyUsedExercise = availableExercises.find { exercise ->
                    exercise.name == leastRecentlyUsedExerciseName
                } ?: availableExercises.first()

                logger.debug("Selected least recently used exercise for cycling: {} from {} available exercises, {} matching previously used exercises",
                    leastRecentlyUsedExercise.name, availableExercises.size, matchingPreviouslyUsedExercises.size)

                Mono.just(leastRecentlyUsedExercise)
            }
        }
    }

    /**
     * Selects a similar secondary exercise based on the primary exercise.
     * This method finds exercises that work similar muscle groups and movement patterns.
     *
     * @param primaryExercise The primary exercise to find a similar secondary exercise for
     * @param userExercisePool The user's exercise pool
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param movementBalanceState Current movement balance state (optional)
     * @return Selected secondary exercise or empty if none available
     */
    fun selectSimilarSecondaryExercise(
        primaryExercise: Exercise,
        userExercisePool: UserExercisePool,
        workoutType: String,
        dayType: String,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<Exercise> {
        // Get primary exercise muscles to use as target muscles from prepared data
        val primaryExerciseMuscles = exerciseMuscleMappings[primaryExercise.name] ?: emptyList()
        val primaryMuscles = primaryExerciseMuscles.map { it.muscleName }
        
        // Use the specialized secondary exercise selection method that applies banded exercise restrictions
        return selectSecondaryExerciseInternal(
            userExercisePool = userExercisePool,
            targetMuscles = primaryMuscles,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings
        ).flatMap { selectedExercise ->
            // Mark the exercise as used and removed from the pool
            userExercisePool.markExerciseAsUsed(selectedExercise.name)
            Mono.just(selectedExercise)
        }
            .onErrorResume { error ->
                logger.error(
                    "Failed to select similar secondary exercise for primary exercise: {}. Error: {}",
                    primaryExercise.name,
                    error.message
                )
                Mono.error(error)
            }
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
     * Filters exercises based on day type to ensure upper body days only get upper body exercises
     * and lower body days only get lower body exercises.
     *
     * @param exercises List of exercises to filter
     * @param dayType The type of workout day (e.g., "ME_Upper", "DE_Lower")
     * @return Filtered list of exercises appropriate for the day type
     */
    fun filterExercisesByDayType(
        exercises: List<Exercise>,
        dayType: String
    ): List<Exercise> {
        val filteredExercises =
            when {
                dayType.contains("Upper") -> {
                    // For upper body days, only include exercises that primarily target upper body muscles
                    val upperExercises =
                        exercises.filter { exercise ->
                            exercise.isUpper
                        }
                    logger.info(
                        "Upper body filtering: {} exercises -> {} upper body exercises. Lower body exercises found: {}",
                        exercises.size,
                        upperExercises.size,
                        exercises.filter { !it.isUpper }.map { it.name }
                    )
                    upperExercises
                }
                dayType.contains("Lower") -> {
                    // For lower body days, only include exercises that primarily target lower body muscles
                    val lowerExercises =
                        exercises.filter { exercise ->
                            !exercise.isUpper
                        }
                    logger.info(
                        "Lower body filtering: {} exercises -> {} lower body exercises. Upper body exercises found: {}",
                        exercises.size,
                        lowerExercises.size,
                        exercises.filter { it.isUpper }.map { it.name }
                    )
                    lowerExercises
                }
                else -> {
                    // For full body or other day types, include all exercises
                    logger.info("Full body filtering: {} exercises -> {} exercises (no filtering)", exercises.size, exercises.size)
                    exercises
                }
            }

        // If no exercises are available for the specific day type, fall back to all exercises
        // This prevents the algorithm from failing when the exercise pool is limited
        if (filteredExercises.isEmpty()) {
            logger.warn(
                "No exercises available for day type '{}', falling back to all {} available exercises to prevent algorithm failure",
                dayType, exercises.size
            )
            return exercises
        }

        return filteredExercises
    }

    /**
     * Filters exercises for Dynamic Effort workouts.
     *
     * For DE workouts, we want exercises that are either:
     * 1. Marked as dynamic_effort workout type (like banded exercises)
     * 2. Plyometric exercises (regardless of accessory status)
     *
     * @param exercises List of all exercises
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @return Mono containing filtered list of exercises suitable for DE workouts
     */
    fun filterExercisesForDEWorkout(
        exercises: List<Exercise>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>
    ): Mono<List<Exercise>> {
        val dynamicEffortExerciseNames = exerciseWorkoutTypeMappings
            .filter { (_, workoutTypes) -> workoutTypes.contains("dynamic_effort") }
            .keys
            .toSet()

        val filteredExercises = exercises.filter { exercise ->
            // Include exercises marked as dynamic_effort
            dynamicEffortExerciseNames.contains(exercise.name) ||
                // Include plyometric exercises (regardless of accessory status)
                exercise.movementType == MovementType.PLYOMETRIC
        }

        logger.debug("Filtered {} exercises for DE workout from {} total exercises", filteredExercises.size, exercises.size)
        return Mono.just(filteredExercises)
    }

    /**
     * Filters exercises by workout type (dynamic_effort or maximal_effort).
     *
     * @param exercises List of all exercises
     * @param workoutType The workout type to filter for (dynamic_effort or maximal_effort)
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @return Filtered list of exercises suitable for the specified workout type
     */
    fun filterExercisesByWorkoutType(
        exercises: List<Exercise>,
        workoutType: String,
        exerciseWorkoutTypeMappings: Map<String, List<String>>
    ): Mono<List<Exercise>> {
        val suitableExerciseNames = exerciseWorkoutTypeMappings
            .filter { (_, workoutTypes) -> workoutTypes.contains(workoutType) }
            .keys
            .toSet()

        val filteredExercises = exercises.filter { exercise ->
            suitableExerciseNames.contains(exercise.name)
        }

        // If no exercises are available for the specific workout type, fall back to all exercises
        // This prevents the algorithm from failing when the exercise pool is limited
        if (filteredExercises.isEmpty()) {
            logger.warn(
                "No exercises available for workout type '{}', falling back to all {} available exercises to prevent algorithm failure",
                workoutType, exercises.size
            )
            return Mono.just(exercises)
        }

        logger.debug("Filtered {} exercises for workout type '{}' from {} total exercises", filteredExercises.size, workoutType, exercises.size)
        return Mono.just(filteredExercises)
    }

    /**
     * Filters out banded exercises for secondary movements.
     * Banded exercises should only be used for primary movements.
     *
     * @param exercises List of exercises to filter
     * @param isSecondary Whether this is for a secondary movement
     * @return Filtered list of exercises
     */
    fun filterBandedExercisesForSecondary(
        exercises: List<Exercise>,
        isSecondary: Boolean
    ): List<Exercise> {
        if (!isSecondary) {
            return exercises
        }

        val bandedExercises = exercises.filter { exercise ->
            exercise.name.contains("Banded", ignoreCase = true)
        }
        
        val filteredExercises = exercises.filter { exercise ->
            !exercise.name.contains("Banded", ignoreCase = true)
        }

        // Only filter out banded exercises if we have enough non-banded exercises available
        if (filteredExercises.isNotEmpty()) {
            logger.info(
                "Filtered out {} banded exercises from secondary movement selection: {}",
                bandedExercises.size,
                bandedExercises.map { it.name }
            )
            return filteredExercises
        } else {
            logger.warn(
                "All {} exercises are banded for secondary movement selection, allowing banded exercises to prevent algorithm failure: {}",
                exercises.size,
                exercises.map { it.name }
            )
            return exercises
        }
    }

    /**
     * Filters banded exercises to only be used on DE (Dynamic Effort) day types.
     * Banded exercises should only be used on DE days, not on ME (Maximal Effort) days.
     *
     * @param exercises List of exercises to filter
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @return Filtered list of exercises
     */
    fun filterBandedExercisesForDayType(
        exercises: List<Exercise>,
        dayType: String
    ): List<Exercise> {
        val isDEDay = dayType.contains("DE", ignoreCase = true)
        
        if (isDEDay) {
            return exercises
        }

        val bandedExercises = exercises.filter { exercise ->
            exercise.name.contains("Banded", ignoreCase = true)
        }
        
        val filteredExercises = exercises.filter { exercise ->
            !exercise.name.contains("Banded", ignoreCase = true)
        }

        // Only filter out banded exercises if we have enough non-banded exercises available
        if (filteredExercises.isNotEmpty()) {
            logger.info(
                "Filtered out {} banded exercises from non-DE day type '{}': {}",
                bandedExercises.size,
                dayType,
                bandedExercises.map { it.name }
            )
            return filteredExercises
        } else {
            logger.warn(
                "All {} exercises are banded for non-DE day type '{}', allowing banded exercises to prevent algorithm failure: {}",
                exercises.size,
                dayType,
                exercises.map { it.name }
            )
            return exercises
        }
    }

    /**
     * Applies banded exercise restrictions based on day type and movement role.
     * 
     * Rules:
     * 1. Banded exercises should only be used on DE (Dynamic Effort) day types
     * 2. Banded exercises should only be used for primary movements, never secondary or accessory
     *
     * @param exercises List of exercises to filter
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param isSecondary Whether this is for a secondary movement
     * @param isAccessory Whether this is for an accessory movement
     * @return Filtered list of exercises
     */
    private fun applyBandedExerciseRestrictions(
        exercises: List<Exercise>,
        dayType: String,
        isSecondary: Boolean,
        isAccessory: Boolean
    ): List<Exercise> {
        var filteredExercises = exercises

        // Rule 1: Banded exercises only on DE days (applies to all exercise types)
        filteredExercises = filterBandedExercisesForDayType(filteredExercises, dayType)

        // Rule 2: Banded exercises only for primary movements (not secondary or accessory)
        if (isSecondary || isAccessory) {
            filteredExercises = filterBandedExercisesForSecondary(filteredExercises, isSecondary = true)
        }

        return filteredExercises
    }

    /**
     * Internal method for selecting secondary exercises with proper banded exercise restrictions.
     * This method applies the same filtering logic as the main selection method but with
     * isSecondary = true to ensure banded exercises are excluded from secondary movements.
     *
     * @param userExercisePool The user's exercise pool
     * @param targetMuscles List of target muscles to focus on
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param exerciseEquipmentMappings Pre-computed mappings of exercise names to their equipment requirements
     * @return Mono containing selected exercise or null if none available
     */
    private fun selectSecondaryExerciseInternal(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<Exercise> {
        return selectRotatingExerciseInternalImpl(
            userExercisePool = userExercisePool,
            targetMuscles = targetMuscles,
            isAccessory = false,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            isWarmup = false,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings,
            isSecondary = true // This ensures banded exercises are filtered out
        ).flatMap { selectedExercise ->
            if (selectedExercise != null) {
                Mono.just(selectedExercise)
            } else {
                Mono.error(
                    ExerciseSelectionException.noSuitableExerciseFound(
                        targetMuscles = targetMuscles,
                        isAccessory = false,
                        workoutType = workoutType,
                        dayType = dayType,
                        movementBalanceState = movementBalanceState
                    )
                )
            }
        }
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

    /**
     * Selects warmup exercises based on the workout day type and template guidelines.
     *
     * For 4-day templates:
     * - Select 2 exercises that focus on the main muscles that the primary workout for the day requires
     * - Select 1 exercise whose movement pattern is close to the primary exercise, but requires less
     *
     * For 2 and 3 day templates:
     * - Select 3 exercises that focus on the common muscles used for the ME and DE exercises that day
     *
     * @param userExercisePool The user's exercise pool
     * @param primaryExercise The primary exercise for the day (if available)
     * @param secondaryExercise The secondary exercise for the day (if available, for 2 and 3 day templates)
     * @param isFourDayTemplate Whether this is a 4-day template
     * @param dayType The type of workout day
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @return Mono containing list of selected warmup exercises
     */
    fun selectWarmupExercises(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise?,
        secondaryExercise: Exercise? = null,
        isFourDayTemplate: Boolean,
        dayType: String,
        workoutType: String,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>
    ): Mono<List<Exercise>> {
        return if (isFourDayTemplate) {
            // 4-day template: 2 muscle-focused + 1 movement pattern exercise
            selectFourDayWarmupExercises(
                userExercisePool = userExercisePool,
                primaryExercise = primaryExercise,
                dayType = dayType,
                workoutType = workoutType,
                exerciseMuscleMappings = exerciseMuscleMappings,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings
            )
        } else {
            // 2 and 3 day templates: 3 exercises for common muscles
            selectTwoThreeDayWarmupExercises(
                userExercisePool = userExercisePool,
                primaryExercise = primaryExercise,
                secondaryExercise = secondaryExercise,
                dayType = dayType,
                workoutType = workoutType,
                exerciseMuscleMappings = exerciseMuscleMappings,
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                exerciseEquipmentMappings = exerciseEquipmentMappings
            )
        }
    }

    /**
     * Selects warmup exercises for 4-day templates.
     *
     * @param userExercisePool The user's exercise pool
     * @param primaryExercise The primary exercise for the day
     * @param dayType The type of workout day
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectFourDayWarmupExercises(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise?,
        dayType: String,
        workoutType: String,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>
    ): Mono<List<Exercise>> {
        return if (primaryExercise != null) {
            // Get muscles for the primary exercise from prepared data
            val primaryExerciseMuscles = exerciseMuscleMappings[primaryExercise.name] ?: emptyList()
            val primaryMuscleNames = primaryExerciseMuscles.map { it.muscleName.lowercase() }

            // Use primary exercise muscles for warmup selection
            val adjustedTargetMuscles = primaryMuscleNames

            // Select muscle-focused accessory exercises
            val muscleFocusedMono =
                selectMuscleFocusedWarmupExercises(
                    userExercisePool = userExercisePool,
                    targetMuscles = adjustedTargetMuscles,
                    count = 2,
                    dayType = dayType,
                    workoutType = workoutType,
                    exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = exerciseMuscleMappings,
                    exerciseEquipmentMappings = exerciseEquipmentMappings
                )

            // Select 1 movement pattern exercise
            val movementPatternMono =
                selectMovementPatternWarmupExercise(
                    userExercisePool = userExercisePool,
                    primaryExercise = primaryExercise,
                    dayType = dayType,
                    workoutType = workoutType,
                    exerciseEquipmentMappings = exerciseEquipmentMappings,
                    exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = exerciseMuscleMappings
                )

            muscleFocusedMono.flatMap { muscleExercises ->
                movementPatternMono
                    .map { movementExercise ->
                        muscleExercises + movementExercise
                    }
                    .switchIfEmpty(
                        // If no movement pattern exercise found, just return the muscle-focused exercises
                        Mono.just(muscleExercises)
                    )
            }
        } else {
            // Fallback: select 3 general warmup exercises
            selectGeneralWarmupExercises(
                userExercisePool = userExercisePool,
                count = 3,
                dayType = dayType,
                workoutType = workoutType,
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                exerciseEquipmentMappings = exerciseEquipmentMappings
            )
        }
    }

    /**
     * Selects warmup exercises for 2 and 3 day templates.
     *
     * @param userExercisePool The user's exercise pool
     * @param primaryExercise The primary exercise for the day (if available)
     * @param secondaryExercise The secondary exercise for the day (if available)
     * @param dayType The type of workout day
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectTwoThreeDayWarmupExercises(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise?,
        secondaryExercise: Exercise?,
        dayType: String,
        workoutType: String,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<List<Exercise>> {
        // For 2 and 3 day templates, use muscles from the actual primary exercises selected
        val primaryMuscles = if (primaryExercise != null) {
            val primaryExerciseMuscles = exerciseMuscleMappings[primaryExercise.name] ?: emptyList()
            primaryExerciseMuscles.map { it.muscleName.lowercase() }
        } else {
            emptyList<String>()
        }

        val secondaryMuscles = if (secondaryExercise != null) {
            val secondaryExerciseMuscles = exerciseMuscleMappings[secondaryExercise.name] ?: emptyList()
            secondaryExerciseMuscles.map { it.muscleName.lowercase() }
        } else {
            emptyList<String>()
        }

        val allMuscles = (primaryMuscles + secondaryMuscles).toSet().toList()

        return selectMuscleFocusedWarmupExercises(
            userExercisePool = userExercisePool,
            targetMuscles = allMuscles,
            count = 3,
            dayType = dayType,
            workoutType = workoutType,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings
        )
    }

    /**
     * Selects muscle-focused warmup exercises.
     *
     * @param userExercisePool The user's exercise pool
     * @param targetMuscles Target muscles to focus on
     * @param count Number of exercises to select
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectMuscleFocusedWarmupExercises(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        count: Int,
        dayType: String,
        workoutType: String,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<List<Exercise>> {
        if (count <= 0) {
            return Mono.just(emptyList())
        }

        return selectMuscleFocusedWarmupExercisesImpl(
            userExercisePool = userExercisePool,
            targetMuscles = targetMuscles,
            count = count,
            dayType = dayType,
            workoutType = workoutType,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings
        ).onErrorResume { error ->
            if (error.message?.contains("No exercises available after workout-type filtering") == true) {
                logger.info("Pool refresh needed for warmup exercises, refreshing and retrying...")
                val poolRefreshed = userExercisePool.refreshPool()
                if (poolRefreshed) {
                    logger.info("Pool refreshed successfully, retrying warmup exercise selection...")
                    selectMuscleFocusedWarmupExercisesImpl(
                        userExercisePool = userExercisePool,
                        targetMuscles = targetMuscles,
                        count = count,
                        dayType = dayType,
                        workoutType = workoutType,
                        exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                        exerciseMuscleMappings = exerciseMuscleMappings,
                        exerciseEquipmentMappings = exerciseEquipmentMappings
                    )
                } else {
                    logger.error("Pool refresh failed for warmup exercises, returning empty list")
                    Mono.just(emptyList())
                }
            } else {
                logger.error(
                    "Failed to select muscle-focused warmup exercises. Parameters: targetMuscles={}, count={}, dayType={}. Error: {}",
                    targetMuscles,
                    count,
                    dayType,
                    error.message
                )
                // For other errors, return empty list to allow workout generation to continue
                Mono.just(emptyList())
            }
        }
    }

    private fun selectMuscleFocusedWarmupExercisesImpl(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        count: Int,
        dayType: String,
        workoutType: String,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<List<Exercise>> {
        // Use Flux.range to select multiple exercises sequentially
        return Flux.range(1, count)
            .concatMap {
                selectExercise(
                    userExercisePool = userExercisePool,
                    targetMuscles = targetMuscles,
                    // Warmup exercises are accessory
                    isAccessory = true,
                    // Use correct workoutType derived from dayType
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = null,
                    isWarmup = true,
                    exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = exerciseMuscleMappings,
                    exerciseEquipmentMappings = exerciseEquipmentMappings
                )
            }
            .collectList()
    }

    /**
     * Selects a movement pattern warmup exercise similar to the primary exercise.
     * This can be either accessory or primary exercises, prioritizing movement pattern similarity.
     */
    private fun selectMovementPatternWarmupExercise(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise,
        dayType: String,
        workoutType: String,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>
    ): Mono<Exercise> {
        return selectMovementPatternWarmupExerciseImpl(
            userExercisePool = userExercisePool,
            primaryExercise = primaryExercise,
            dayType = dayType,
            workoutType = workoutType,
            exerciseEquipmentMappings = exerciseEquipmentMappings,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings
        ).onErrorResume { error ->
            if (error.message?.contains("No exercises available after workout-type filtering") == true) {
                logger.info("Pool refresh needed for movement pattern warmup exercise, refreshing and retrying...")
                val poolRefreshed = userExercisePool.refreshPool()
                if (poolRefreshed) {
                    logger.info("Pool refreshed successfully, retrying movement pattern warmup exercise selection...")
                    selectMovementPatternWarmupExerciseImpl(
                        userExercisePool = userExercisePool,
                        primaryExercise = primaryExercise,
                        dayType = dayType,
                        workoutType = workoutType,
                        exerciseEquipmentMappings = exerciseEquipmentMappings,
                        exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                        exerciseMuscleMappings = exerciseMuscleMappings
                    )
                } else {
                    logger.error("Pool refresh failed for movement pattern warmup exercise, returning empty")
                    Mono.empty()
                }
            } else {
                logger.error(
                    "Failed to select movement pattern warmup exercise for primary exercise: {}. Error: {}",
                    primaryExercise.name,
                    error.message
                )
                // For other errors, return empty to allow workout generation to continue
                Mono.empty()
            }
        }
    }

    private fun selectMovementPatternWarmupExerciseImpl(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise,
        dayType: String,
        workoutType: String,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>
    ): Mono<Exercise> {
        // First try to find an accessory exercise with the same movement type
        return selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            // Start with accessory exercises
            isAccessory = true,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = null,
            isWarmup = true,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            exerciseEquipmentMappings = exerciseEquipmentMappings
        ).filter { selectedExercise ->
            selectedExercise.movementType == primaryExercise.movementType
        }.switchIfEmpty(
            // If no accessory exercise found with same movement type, try primary exercises
            selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = emptyList(),
                // Try primary exercises
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = null,
                isWarmup = true,
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                exerciseEquipmentMappings = exerciseEquipmentMappings
            ).flatMap { selectedExercise ->
                // For movement pattern matching, allow primary exercises that:
                // 1. Have the same movement type
                // 2. Use appropriate equipment (dumbbells, bodyweight, etc.)
                // 3. Are not too heavy/intense for warmups
                // 4. Are appropriate for the day type (upper/lower body)
                if (selectedExercise.movementType == primaryExercise.movementType) {
                    isAppropriateForWarmup(selectedExercise, exerciseEquipmentMappings)
                        .flatMap { isAppropriate ->
                            if (isAppropriate) {
                                // Additional safety check: ensure the exercise is appropriate for the day type
                                // This prevents lower body exercises from being selected for upper body days
                                val isDayTypeAppropriate =
                                    when {
                                        dayType.contains("Upper") -> selectedExercise.isUpper
                                        dayType.contains("Lower") -> !selectedExercise.isUpper
                                        else -> true // For other day types, allow any exercise
                                    }

                                if (isDayTypeAppropriate) {
                                    Mono.just(selectedExercise)
                                } else {
                                    logger.info(
                                        "Exercise {} is not appropriate for day type {} (isUpper: {})",
                                        selectedExercise.name,
                                        dayType,
                                        selectedExercise.isUpper
                                    )
                                    Mono.empty()
                                }
                            } else {
                                Mono.empty()
                            }
                        }
                } else {
                    Mono.empty()
                }
            }
        )
    }

    /**
     * Selects general warmup exercises when primary exercise is not available.
     *
     * @param userExercisePool The user's exercise pool
     * @param count Number of exercises to select
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectGeneralWarmupExercises(
        userExercisePool: UserExercisePool,
        count: Int,
        dayType: String,
        workoutType: String,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<List<Exercise>> {
        if (count <= 0) {
            return Mono.just(emptyList())
        }

        // Use Flux.range to select multiple exercises sequentially
        return Flux.range(1, count)
            .concatMap {
                selectExercise(
                    userExercisePool = userExercisePool,
                    targetMuscles = emptyList(),
                    isAccessory = true,
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = null,
                    isWarmup = true,
                    exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = exerciseMuscleMappings,
                    exerciseEquipmentMappings = exerciseEquipmentMappings
                )
            }
            .collectList()
            .onErrorResume { error ->
                logger.error(
                    "Failed to select general warmup exercises. Parameters: count={}, dayType={}. Error: {}",
                    count,
                    dayType,
                    error.message
                )
                // For warmup exercises, we should not fall back to empty list if day-type filtering fails
                // This ensures that lower body exercises are not selected for upper body days
                if (error.message?.contains("day-type filtering") == true) {
                    logger.error(
                        "Day-type filtering failed for general warmup selection. " +
                            "This should not happen - check exercise data and day-type logic."
                    )
                    Mono.error(error)
                } else {
                    // For other errors (like muscle count filtering), we can be more lenient
                    Mono.just(emptyList())
                }
            }
    }

    /**
     * Filters exercises to only include those with muscle count <= MAX_MUSCLES_FOR_WARMUP for warmup selection.
     * This method uses prepared data to check muscle counts for each exercise.
     *
     * @param exercises List of exercises to filter
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @return Mono containing filtered list of exercises with appropriate muscle count for warmup
     */
    private fun filterExercisesByMuscleCountForWarmupReactive(
        exercises: List<Exercise>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>
    ): Mono<List<Exercise>> {
        if (exercises.isEmpty()) {
            return Mono.just(emptyList())
        }

        val filteredExercises = exercises.filter { exercise ->
            val muscleList = exerciseMuscleMappings[exercise.name] ?: emptyList()
            val muscleCount = muscleList.size
            val isAppropriate = muscleCount <= MAX_MUSCLES_FOR_WARMUP

            if (!isAppropriate) {
                logger.debug(
                    "Excluding exercise '{}' from warmup selection due to muscle count: {} (max allowed: {})",
                    exercise.name,
                    muscleCount,
                    MAX_MUSCLES_FOR_WARMUP
                )
            }

            isAppropriate
        }

        return Mono.just(filteredExercises)
    }

    /**
     * Determines if an exercise is appropriate for warmup based on its equipment requirements.
     * This method uses prepared data to check equipment requirements for each exercise.
     *
     * @param exercise The exercise to check
     * @param exerciseEquipmentMappings Pre-computed mappings of exercise names to their equipment requirements
     * @return Mono containing true if the exercise is appropriate for warmup, false otherwise
     */
    private fun isAppropriateForWarmup(
        exercise: Exercise,
        exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>
    ): Mono<Boolean> {
        val equipmentList = exerciseEquipmentMappings[exercise.name] ?: emptyList()
        
        // For warmup exercises, we want exercises that use lighter equipment
        // or bodyweight exercises that are suitable for warming up
        val isAppropriate = equipmentList.any { equipment ->
            equipment.equipmentName.lowercase() in listOf(
                "bodyweight",
                "dumbbell",
                "resistance band",
                "kettlebell",
                "medicine ball"
            )
        } || equipmentList.isEmpty()

        logger.debug(
            "Exercise '{}' is {} for warmup (equipment: {})",
            exercise.name,
            if (isAppropriate) "appropriate" else "not appropriate",
            equipmentList.map { it.equipmentName }
        )

        return Mono.just(isAppropriate)
    }
}
