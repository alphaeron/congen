package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.model.Exercise
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
    private val exerciseDAL: ExerciseDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val movementBalanceService: MovementBalanceService,
    private val exerciseMatchingService: ExerciseMatchingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseSelectionService::class.java)
    }

    /**
     * Determines weak muscles based on user's 1RM data and exercise history.
     *
     * @return List of weak muscle groups to target
     */
    fun determineWeakMuscles(): List<String> {
        // For now, return default weak muscles
        // In a real implementation, this would analyze 1RM data and exercise history
        // to identify areas that need more attention
        return ConjugateConstants.DEFAULT_WEAK_MUSCLES
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
     * @return Mono containing selected exercise or null if none available
     */
    fun selectExercise(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        isAccessory: Boolean,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        isWarmup: Boolean = false
    ): Mono<Exercise> {
        return selectRotatingExerciseInternal(
            userExercisePool = userExercisePool,
            targetMuscles = targetMuscles,
            isAccessory = isAccessory,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            isWarmup = isWarmup
        ).flatMap { selectedExercise ->
            if (selectedExercise != null) {
                // Mark the exercise as used and removed from the pool
                userExercisePool.markExerciseAsUsed(selectedExercise.name)

                Mono.just(selectedExercise)
            } else {
                logger.error(
                    "Failed to select exercise - no suitable exercise found. " +
                        "Parameters: targetMuscles={}, isAccessory={}, movementBalanceState={}",
                    targetMuscles,
                    isAccessory,
                    movementBalanceState
                )
                Mono.error(IllegalStateException("No suitable exercise found for the given criteria"))
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
     * @return Mono containing selected exercise or null if none available
     */
    private fun selectRotatingExerciseInternal(
        userExercisePool: UserExercisePool,
        targetMuscles: List<String>,
        isAccessory: Boolean,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        isWarmup: Boolean = false
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
                return@defer Mono.error(IllegalStateException("No available exercises found for isAccessory: $isAccessory"))
            }

            // Apply day-type filtering first
            val dayTypeFilteredExercises = filterExercisesByDayType(availableExercises, dayType)
            if (dayTypeFilteredExercises.isEmpty()) {
                logger.error("No exercises available after day-type filtering for dayType: {} and isAccessory: {}", dayType, isAccessory)
                return@defer Mono.error(
                    IllegalStateException(
                        "No exercises available after day-type filtering for dayType: $dayType and isAccessory: $isAccessory"
                    )
                )
            }

            // Filter out plyometric exercises only for warmup selection
            val exercisesAfterPlyometricFiltering = if (isWarmup) {
                val nonPlyometricExercises = dayTypeFilteredExercises.filter { it.movementType != MovementType.PLYOMETRIC }
                if (nonPlyometricExercises.isEmpty()) {
                    logger.error(
                        "No non-plyometric exercises available after filtering for dayType: {} and isAccessory: {} (warmup)",
                        dayType,
                        isAccessory
                    )
                    return@defer Mono.error(
                        IllegalStateException(
                            "No non-plyometric exercises available after filtering for dayType: $dayType and isAccessory: $isAccessory (warmup)"
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
                    filterExercisesByWorkoutType(exercisesAfterPlyometricFiltering, workoutType)
                }

            exercisesAfterWorkoutTypeFiltering
                .flatMap { workoutTypeFilteredExercises ->
                    if (workoutTypeFilteredExercises.isEmpty()) {
                        logger.error(
                            "No exercises available after workout-type filtering for workoutType: {} and isAccessory: {}",
                            workoutType,
                            isAccessory
                        )
                        return@flatMap Mono.error(
                            IllegalStateException(
                                "No exercises available after workout-type filtering for " +
                                    "workoutType: $workoutType and isAccessory: $isAccessory"
                            )
                        )
                    }

                    // Filter exercises by equipment and muscles reactively
                    userExercisePool
                        .filterExercisesByEquipment(
                            workoutTypeFilteredExercises
                        )
                        .flatMap { equipmentFilteredExercises ->
                            userExercisePool.filterExercisesByMuscles(
                                equipmentFilteredExercises,
                                targetMuscles,
                                exerciseMuscleDAL
                            )
                        }
                        .flatMap { filteredExercises ->
                            if (filteredExercises.isEmpty()) {
                                logger.error("No exercises found for target muscles: {} for isAccessory: {}", targetMuscles, isAccessory)
                                // No related muscles found, use final fallback
                                val fallbackExercise = availableExercises.firstOrNull()
                                if (fallbackExercise != null) {
                                    logger.warn(
                                        "Using final fallback exercise: {} for weak muscles: {}",
                                        fallbackExercise.name,
                                        targetMuscles
                                    )
                                    Mono.just(fallbackExercise)
                                } else {
                                    Mono.error(
                                        IllegalStateException(
                                            "No exercises found for target muscles: $targetMuscles for isAccessory: $isAccessory"
                                        )
                                    )
                                }
                            } else {
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
                                    // Fallback to any available exercise
                                    val fallbackExercise = availableExercises.firstOrNull()
                                    if (fallbackExercise != null) {
                                        logger.warn(
                                            "Using fallback exercise after movement balance constraints: {} for isAccessory: {}",
                                            fallbackExercise.name,
                                            isAccessory
                                        )
                                        Mono.just(fallbackExercise)
                                    } else {
                                        Mono.error(
                                            IllegalStateException(
                                                "No exercises available after movement balance constraints for isAccessory: $isAccessory"
                                            )
                                        )
                                    }
                                } else {
                                    // Select a random exercise from the filtered list
                                    val selectedExercise = finalExercises.random()
                                    Mono.just(selectedExercise)
                                }
                            }
                        }
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
     * @param movementBalanceState Current movement balance state (optional)
     * @return Selected secondary exercise or empty if none available
     */
    fun selectSimilarSecondaryExercise(
        primaryExercise: Exercise,
        userExercisePool: UserExercisePool,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        // Get primary exercise muscles to use as target muscles
        return exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
            .map { primaryExerciseMuscles ->
                primaryExerciseMuscles.map { it.muscleName }
            }
            .flatMap { primaryMuscles ->
                // Use the main entry point for exercise selection (handles removal automatically)
                selectExercise(
                    userExercisePool = userExercisePool,
                    targetMuscles = primaryMuscles,
                    isAccessory = false,
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = movementBalanceState
                )
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
                    upperExercises
                }
                dayType.contains("Lower") -> {
                    // For lower body days, only include exercises that primarily target lower body muscles
                    val lowerExercises =
                        exercises.filter { exercise ->
                            !exercise.isUpper
                        }
                    lowerExercises
                }
                else -> {
                    // For full body or other day types, include all exercises
                    exercises
                }
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
     * @return Mono containing filtered list of exercises suitable for DE workouts
     */
    fun filterExercisesForDEWorkout(exercises: List<Exercise>): Mono<List<Exercise>> {
        return exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()
            .map { workoutTypes ->
                val dynamicEffortExerciseNames =
                    workoutTypes
                        .filter { it.workoutType == "dynamic_effort" }
                        .map { it.exerciseName }
                        .toSet()

                val filteredExercises =
                    exercises.filter { exercise ->
                        // Include exercises marked as dynamic_effort
                        dynamicEffortExerciseNames.contains(exercise.name) ||
                            // Include plyometric exercises (regardless of accessory status)
                            exercise.movementType == MovementType.PLYOMETRIC
                    }

                filteredExercises
            }
    }

    /**
     * Filters exercises by workout type (dynamic_effort or maximal_effort).
     *
     * @param exercises List of all exercises
     * @param workoutType The workout type to filter for (dynamic_effort or maximal_effort)
     * @return Filtered list of exercises suitable for the specified workout type
     */
    fun filterExercisesByWorkoutType(
        exercises: List<Exercise>,
        workoutType: String
    ): Mono<List<Exercise>> {
        return exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()
            .map { workoutTypes ->
                val suitableExerciseNames =
                    workoutTypes
                        .filter { it.workoutType == workoutType }
                        .map { it.exerciseName }
                        .toSet()

                val filteredExercises =
                    exercises.filter { exercise ->
                        suitableExerciseNames.contains(exercise.name)
                    }

                filteredExercises
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
        workoutType: String
    ): Mono<List<Exercise>> {
        return if (isFourDayTemplate) {
            // 4-day template: 2 muscle-focused + 1 movement pattern exercise
            selectFourDayWarmupExercises(
                userExercisePool = userExercisePool,
                primaryExercise = primaryExercise,
                dayType = dayType,
                workoutType = workoutType
            )
        } else {
            // 2 and 3 day templates: 3 exercises for common muscles
            selectTwoThreeDayWarmupExercises(
                userExercisePool = userExercisePool,
                primaryExercise = primaryExercise,
                secondaryExercise = secondaryExercise,
                dayType = dayType,
                workoutType = workoutType
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
        workoutType: String
    ): Mono<List<Exercise>> {
        return if (primaryExercise != null) {
            // Get muscles for the primary exercise
            exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
                .flatMap { primaryMuscles ->
                    val primaryMuscleNames = primaryMuscles.map { it.muscleName.lowercase() }

                    // Use primary exercise muscles for warmup selection
                    val adjustedTargetMuscles = primaryMuscleNames

                    // Select 3 muscle-focused accessory exercises
                    val muscleFocusedMono =
                        selectMuscleFocusedWarmupExercises(
                            userExercisePool = userExercisePool,
                            targetMuscles = adjustedTargetMuscles,
                            count = 3,
                            dayType = dayType,
                            workoutType = workoutType
                        )

                    // Select 1 movement pattern exercise
                    val movementPatternMono =
                        selectMovementPatternWarmupExercise(
                            userExercisePool = userExercisePool,
                            primaryExercise = primaryExercise,
                            dayType = dayType,
                            workoutType = workoutType
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
                }
        } else {
            // Fallback: select 3 general warmup exercises
            selectGeneralWarmupExercises(
                userExercisePool = userExercisePool,
                count = 3,
                dayType = dayType,
                workoutType = workoutType
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
        workoutType: String
    ): Mono<List<Exercise>> {
        // For 2 and 3 day templates, use muscles from the actual primary exercises selected
        val primaryMusclesMono =
            if (primaryExercise != null) {
                exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
                    .map { muscles -> muscles.map { it.muscleName.lowercase() } }
            } else {
                Mono.just(emptyList<String>())
            }

        val secondaryMusclesMono =
            if (secondaryExercise != null) {
                exerciseMuscleDAL.selectExerciseMuscleByExercise(secondaryExercise.name)
                    .map { muscles -> muscles.map { it.muscleName.lowercase() } }
            } else {
                Mono.just(emptyList<String>())
            }

        return Mono.zip(primaryMusclesMono, secondaryMusclesMono)
            .map { tuple ->
                val primaryMuscles = tuple.t1
                val secondaryMuscles = tuple.t2
                val allMuscles = (primaryMuscles + secondaryMuscles).toSet().toList()

                allMuscles
            }
            .flatMap { targetMuscles ->
                selectMuscleFocusedWarmupExercises(
                    userExercisePool = userExercisePool,
                    targetMuscles = targetMuscles,
                    count = 3,
                    dayType = dayType,
                    workoutType = workoutType
                )
            }
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
        workoutType: String
    ): Mono<List<Exercise>> {
        if (count <= 0) {
            return Mono.just(emptyList())
        }

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
                    isWarmup = true
                )
            }
            .collectList()
            .onErrorResume { error ->
                logger.error(
                    "Failed to select muscle-focused warmup exercises. Parameters: targetMuscles={}, count={}. Error: {}",
                    targetMuscles,
                    count,
                    error.message
                )
                Mono.just(emptyList())
            }
    }

    /**
     * Selects a movement pattern warmup exercise similar to the primary exercise.
     * This can be either accessory or primary exercises, prioritizing movement pattern similarity.
     */
    private fun selectMovementPatternWarmupExercise(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise,
        dayType: String,
        workoutType: String
    ): Mono<Exercise> {
        // First try to find an accessory exercise with the same movement type
        return selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            isAccessory = true, // Start with accessory exercises
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = null,
            isWarmup = true
        ).filter { selectedExercise ->
            selectedExercise.movementType == primaryExercise.movementType
        }.switchIfEmpty(
            // If no accessory exercise found with same movement type, try primary exercises
            selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = emptyList(),
                isAccessory = false, // Try primary exercises
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = null,
                isWarmup = true
            ).filter { selectedExercise ->
                // For movement pattern matching, allow primary exercises that:
                // 1. Have the same movement type
                // 2. Use appropriate equipment (dumbbells, bodyweight, etc.)
                // 3. Are not too heavy/intense for warmups
                selectedExercise.movementType == primaryExercise.movementType &&
                isAppropriateForWarmup(selectedExercise)
            }
        ).onErrorResume { error ->
            logger.error(
                "Failed to select movement pattern warmup exercise for primary exercise: {}. Error: {}",
                primaryExercise.name,
                error.message
            )
            Mono.error(error)
        }
    }

    /**
     * Determines if a primary exercise is appropriate for warmup use.
     */
    private fun isAppropriateForWarmup(exercise: Exercise): Boolean {
        // Allow exercises that use lighter equipment or are bodyweight
        val warmupAppropriateEquipment = setOf(
            "dumbbells", "bodyweight", "bands", "sled", "kettlebell"
        )
        
        // Check if the exercise uses any warmup-appropriate equipment
        // This would need to be implemented based on your equipment filtering logic
        return true // For now, allow all primary exercises - you can refine this logic
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
        workoutType: String
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
                    isWarmup = true
                )
            }
            .collectList()
            .onErrorResume { error ->
                logger.error("Failed to select general warmup exercises. Parameters: count={}. Error: {}", count, error.message)
                Mono.just(emptyList())
            }
    }
}
