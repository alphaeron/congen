package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.MovementType
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service for selecting exercises based on various criteria including rotation history,
 * user preferences, equipment availability, and target muscles.
 */
@Service
class ExerciseSelectionService(
    private val exerciseDAL: ExerciseDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val movementBalanceService: MovementBalanceService
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
     * Selects a rotating exercise based on various criteria.
     *
     * @param targetMuscles List of target muscles to focus on
     * @param userEquipment List of user's available equipment
     * @param preferences List of user's exercise preferences
     * @param exercises List of available exercises
     * @param isAccessory Whether this is for an accessory exercise
     * @param rotationHistory List of exercise rotation history
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing selected exercise or null if none available
     */
    fun selectRotatingExercise(
        targetMuscles: List<String>,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        isAccessory: Boolean,
        rotationHistory: List<ExerciseRotationHistory>,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise?> {
        // Get all exercises from the database to ensure we can include user preferences
        return exerciseDAL.selectExercises()
            .flatMap outer@{ allExercises ->
                // Filter exercises based on preferences and ensure preferred exercises are included
                val exercisesToAvoid = preferences.filter { it.shouldAvoid }.map { it.exerciseName }.toSet()
                val preferredExercises = preferences.filter { !it.shouldAvoid }.map { it.exerciseName }.toSet()

                // Start with the provided exercises (already filtered by workout type, accessory status, etc.)
                val baseExercises =
                    exercises.filter { exercise ->
                        !exercisesToAvoid.contains(exercise.name)
                    }

                // Add any preferred exercises that aren't already in the list
                val additionalPreferredExercises =
                    allExercises.filter { exercise ->
                        preferredExercises.contains(exercise.name) &&
                            !baseExercises.any { it.name == exercise.name } &&
                            exercise.isAccessory == isAccessory
                    }

                val availableExercises = baseExercises + additionalPreferredExercises

                if (availableExercises.isEmpty()) {
                    logger.warn("No available exercises found for isAccessory: {}", isAccessory)
                    return@outer Mono.justOrEmpty(null)
                }

                // Filter exercises by target muscles and equipment availability
                Flux.fromIterable(availableExercises)
                    .flatMap { exercise ->
                        // Check if user has equipment for this exercise
                        exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                            .filter { exerciseEquipment ->
                                val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                                val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                                userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                            }
                            .flatMap { _ ->
                                // Check if exercise targets any of the target muscles
                                exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                                    .filter { exerciseMuscles ->
                                        val exerciseMuscleNames = exerciseMuscles?.map { it.muscleName.lowercase() } ?: emptyList()
                                        val targetMusclesLower = targetMuscles.map { it.lowercase() }
                                        val hasMatchingMuscles =
                                            exerciseMuscleNames.any { muscle ->
                                                targetMusclesLower.any { targetMuscle ->
                                                    muscle.contains(targetMuscle) || targetMuscle.contains(muscle)
                                                }
                                            }
                                        hasMatchingMuscles
                                    }
                                    .map { exercise }
                            }
                    }
                    .collectList()
                    .flatMap { muscleFilteredExercises ->
                        if (muscleFilteredExercises.isEmpty()) {
                            logger.warn(
                                "No exercises found matching target muscles and equipment: {} for isAccessory: {}",
                                targetMuscles,
                                isAccessory
                            )
                            // Fallback: try to find exercises that match muscles but don't check equipment
                            return@flatMap Flux.fromIterable(availableExercises)
                                .flatMap { exercise ->
                                    exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                                        .filter { exerciseMuscles ->
                                            val exerciseMuscleNames = exerciseMuscles?.map { it.muscleName.lowercase() } ?: emptyList()
                                            val targetMusclesLower = targetMuscles.map { it.lowercase() }
                                            val hasMatchingMuscles =
                                                exerciseMuscleNames.any { muscle ->
                                                    targetMusclesLower.any { targetMuscle ->
                                                        muscle.contains(targetMuscle) || targetMuscle.contains(muscle)
                                                    }
                                                }
                                            hasMatchingMuscles
                                        }
                                        .map { exercise }
                                }
                                .collectList()
                                .flatMap { fallbackExercises ->
                                    if (fallbackExercises.isEmpty()) {
                                        logger.warn(
                                            "No exercises found matching target muscles: {} for isAccessory: {}",
                                            targetMuscles,
                                            isAccessory
                                        )
                                        Mono.justOrEmpty(null)
                                    } else {
                                        // Select a single exercise from the fallback list using the same logic
                                        val categoryHistory = rotationHistory.filter { it.isAccessory == isAccessory }
                                        val usedExercises = categoryHistory.map { it.exerciseName }.toSet()

                                        val unusedFallbackExercises =
                                            fallbackExercises.filter { exercise ->
                                                !usedExercises.contains(exercise.name)
                                            }

                                        val exerciseToChooseFrom =
                                            if (unusedFallbackExercises.isNotEmpty()) {
                                                unusedFallbackExercises
                                            } else {
                                                fallbackExercises
                                            }

                                        val selectedExercise = exerciseToChooseFrom.firstOrNull()
                                        Mono.justOrEmpty(selectedExercise)
                                    }
                                }
                        }

                        // Get exercise rotation history for this category
                        val categoryHistory = rotationHistory.filter { it.isAccessory == isAccessory }

                        // Get all exercises that have been used in this category
                        val usedExercises = categoryHistory.map { it.exerciseName }.toSet()

                        // Get exercises that haven't been used yet in this category
                        val unusedExercises =
                            muscleFilteredExercises.filter { exercise ->
                                !usedExercises.contains(exercise.name)
                            }

                        // If we have unused exercises, use them first
                        val exercisesToChooseFrom =
                            if (unusedExercises.isNotEmpty()) {
                                unusedExercises
                            } else {
                                // If all exercises have been used, find the least recently used one
                                val exerciseUsageCount =
                                    muscleFilteredExercises.associateWith { exercise ->
                                        categoryHistory.count { it.exerciseName == exercise.name }
                                    }

                                val minUsageCount = exerciseUsageCount.values.minOrNull() ?: 0
                                muscleFilteredExercises.filter { exercise ->
                                    exerciseUsageCount[exercise] == minUsageCount
                                }
                            }

                        // Apply movement balance constraints if state is provided
                        val prioritized =
                            if (movementBalanceState != null) {
                                movementBalanceService.prioritizeExercisesForBalance(
                                    exercisesToChooseFrom,
                                    movementBalanceState
                                )
                            } else {
                                exercisesToChooseFrom
                            }

                        // Sort by number of equipment options (desc), targeted muscles (desc), exercise name
                        val sortedExercises =
                            prioritized.sortedWith(
                                compareByDescending<Exercise> { exercise ->
                                    // Count equipment options - this would need to be implemented with actual equipment counting
                                    // For now, use a simple heuristic based on exercise name
                                    when {
                                        exercise.name.contains("barbell") -> 3
                                        exercise.name.contains("dumbbell") -> 2
                                        exercise.name.contains("bodyweight") -> 1
                                        else -> 1
                                    }
                                }.thenByDescending { exercise ->
                                    // Count targeted muscles - this would need to be implemented with actual muscle counting
                                    // For now, use a simple heuristic
                                    when {
                                        exercise.name.contains("squat") || exercise.name.contains("deadlift") -> 5
                                        exercise.name.contains("bench") || exercise.name.contains("press") -> 4
                                        exercise.name.contains("row") || exercise.name.contains("pull") -> 3
                                        else -> 2
                                    }
                                }.thenBy { exercise ->
                                    exercise.name
                                }
                            )

                        val selectedExercise = sortedExercises.firstOrNull()
                        Mono.justOrEmpty(selectedExercise)
                    }
            }
    }

    /**
     * Selects a similar secondary exercise based on the primary exercise.
     * This method finds exercises that work similar muscle groups and movement patterns.
     *
     * @param primaryExercise The primary exercise to find a similar secondary exercise for
     * @param userEquipment List of user's available equipment
     * @param preferences List of user's exercise preferences
     * @param exercises List of available exercises (already filtered to exclude primary exercise)
     * @param rotationHistory List of exercise rotation history
     * @param movementBalanceState Current movement balance state (optional)
     * @return Selected secondary exercise or empty if none available
     */
    fun selectSimilarSecondaryExercise(
        primaryExercise: Exercise,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        rotationHistory: List<ExerciseRotationHistory>,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        val availableExercises =
            exercises.filter { exercise ->
                !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }
        if (availableExercises.isEmpty()) {
            logger.warn("No available exercises found for secondary movement")
            return Mono.empty()
        }
        val equipmentFilteredExercises =
            availableExercises.filter {
                userEquipment.any { true }
            }
        if (equipmentFilteredExercises.isEmpty()) {
            logger.warn("No exercises available with user's equipment for secondary movement")
            return Mono.justOrEmpty(availableExercises.firstOrNull())
        }
        return exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
            .flatMap { primaryExerciseMuscles ->
                val primaryMuscleNames = primaryExerciseMuscles.map { it.muscleName }.toSet()
                val exerciseScoringMonos =
                    equipmentFilteredExercises.map { exercise ->
                        exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                            .map { exerciseMuscles ->
                                val exerciseMuscleNames = exerciseMuscles.map { it.muscleName }.toSet()
                                val exerciseScore =
                                    calculateExerciseSimilarityScore(
                                        exercise = exercise,
                                        primaryMovementType = primaryExercise.movementType,
                                        primaryMuscles = primaryMuscleNames,
                                        exerciseMuscles = exerciseMuscleNames,
                                        rotationHistory = rotationHistory,
                                        movementBalanceState = movementBalanceState
                                    )
                                exercise to exerciseScore
                            }
                            .onErrorReturn(exercise to 0.0)
                    }
                if (exerciseScoringMonos.isEmpty()) {
                    return@flatMap Mono.empty()
                }
                @Suppress("UNCHECKED_CAST")
                return@flatMap Mono.zip(exerciseScoringMonos) { results: Array<Any?> ->
                    val scoredExercises = results.map { it as Pair<Exercise, Double> }
                    scoredExercises
                        .sortedByDescending { it.second }
                        .firstOrNull()
                        ?.first
                }.flatMap { exercise ->
                    if (exercise != null) {
                        Mono.just(exercise)
                    } else {
                        Mono.empty()
                    }
                }
            }
    }

    /**
     * Calculates a similarity score for an exercise compared to the primary exercise.
     * Higher scores indicate more similarity.
     *
     * @param exercise The exercise to score
     * @param primaryMovementType The movement type of the primary exercise
     * @param primaryMuscles The muscles worked by the primary exercise
     * @param exerciseMuscles The muscles worked by the exercise being scored
     * @param rotationHistory List of exercise rotation history
     * @param movementBalanceState Current movement balance state (optional)
     * @return Similarity score (higher is more similar)
     */
    private fun calculateExerciseSimilarityScore(
        exercise: Exercise,
        primaryMovementType: MovementType,
        primaryMuscles: Set<String>,
        exerciseMuscles: Set<String>,
        rotationHistory: List<ExerciseRotationHistory>,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Double {
        var score = 0.0

        // Movement type similarity (highest weight)
        if (exercise.movementType == primaryMovementType) {
            score += 100.0
        } else {
            // Partial credit for related movement types
            score += calculateMovementTypeSimilarity(exercise.movementType, primaryMovementType)
        }

        // Muscle overlap similarity
        val muscleOverlapScore = calculateMuscleOverlapScore(primaryMuscles, exerciseMuscles)
        score += muscleOverlapScore

        // Rotation history bonus (prefer less recently used exercises)
        val rotationBonus = calculateRotationBonus(exercise, rotationHistory)
        score += rotationBonus

        // Movement balance bonus (if state is provided)
        if (movementBalanceState != null) {
            val balanceScore =
                movementBalanceService.scoreExerciseForBalance(
                    exercise = exercise,
                    currentState = movementBalanceState
                )
            score += balanceScore
        }

        return score
    }

    /**
     * Calculates similarity between movement types.
     *
     * @param movementType1 First movement type
     * @param movementType2 Second movement type
     * @return Similarity score
     */
    private fun calculateMovementTypeSimilarity(
        movementType1: MovementType,
        movementType2: MovementType
    ): Double {
        return when {
            // Same category (push/pull)
            (movementType1 == MovementType.HORIZONTAL_PUSH && movementType2 == MovementType.HORIZONTAL_PUSH) ||
                (movementType1 == MovementType.VERTICAL_PUSH && movementType2 == MovementType.VERTICAL_PUSH) ||
                (movementType1 == MovementType.HORIZONTAL_PULL && movementType2 == MovementType.HORIZONTAL_PULL) ||
                (movementType1 == MovementType.VERTICAL_PULL && movementType2 == MovementType.VERTICAL_PULL) -> 50.0

            // Same plane (horizontal/vertical)
            (movementType1 == MovementType.HORIZONTAL_PUSH && movementType2 == MovementType.HORIZONTAL_PULL) ||
                (movementType1 == MovementType.HORIZONTAL_PULL && movementType2 == MovementType.HORIZONTAL_PUSH) ||
                (movementType1 == MovementType.VERTICAL_PUSH && movementType2 == MovementType.VERTICAL_PULL) ||
                (movementType1 == MovementType.VERTICAL_PULL && movementType2 == MovementType.VERTICAL_PUSH) -> 25.0

            // Same body part focus (upper/lower)
            (movementType1 == MovementType.SQUAT && movementType2 == MovementType.HINGE) ||
                (movementType1 == MovementType.HINGE && movementType2 == MovementType.SQUAT) ||
                (
                    movementType1 == MovementType.LUNGE &&
                        (movementType2 == MovementType.SQUAT || movementType2 == MovementType.HINGE)
                ) -> 15.0

            else -> 0.0
        }
    }

    /**
     * Calculates muscle overlap score between primary and secondary exercise muscles.
     *
     * @param primaryMuscles The muscles worked by the primary exercise
     * @param exerciseMuscles The muscles worked by the exercise being evaluated
     * @return Muscle overlap score
     */
    private fun calculateMuscleOverlapScore(
        primaryMuscles: Set<String>,
        exerciseMuscles: Set<String>
    ): Double {
        if (primaryMuscles.isEmpty() || exerciseMuscles.isEmpty()) {
            return 0.0
        }

        // Calculate intersection (overlapping muscles)
        val overlappingMuscles = primaryMuscles.intersect(exerciseMuscles)

        // Calculate overlap percentage
        val overlapPercentage = overlappingMuscles.size.toDouble() / primaryMuscles.size.toDouble()

        // Score based on overlap percentage (max 50 points for complete overlap)
        return overlapPercentage * 50.0
    }

    /**
     * Calculates rotation bonus based on how recently an exercise was used.
     *
     * @param exercise The exercise to evaluate
     * @param rotationHistory List of exercise rotation history
     * @return Rotation bonus score
     */
    private fun calculateRotationBonus(
        exercise: Exercise,
        rotationHistory: List<ExerciseRotationHistory>
    ): Double {
        val categoryHistory = rotationHistory.filter { !it.isAccessory } // Primary exercises
        val exerciseUsageCount = categoryHistory.count { it.exerciseName == exercise.name }

        // Bonus for less frequently used exercises
        return when (exerciseUsageCount) {
            0 -> 20.0 // Never used - highest bonus
            1 -> 15.0
            2 -> 10.0
            3 -> 5.0
            else -> 0.0 // Frequently used - no bonus
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
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param dayType The type of workout day
     * @param primaryExercise The primary exercise for the day (if available)
     * @param isFourDayTemplate Whether this is a 4-day template
     * @return Mono containing list of selected warmup exercises
     */
    fun selectWarmupExercises(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        dayType: String,
        primaryExercise: Exercise?,
        isFourDayTemplate: Boolean
    ): Mono<List<Exercise>> {
        return if (isFourDayTemplate) {
            // 4-day template: 2 muscle-focused + 1 movement pattern exercise
            selectFourDayWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                dayType = dayType,
                primaryExercise = primaryExercise
            )
        } else {
            // 2 and 3 day templates: 3 exercises for common muscles
            selectTwoThreeDayWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                dayType = dayType
            )
        }
    }

    /**
     * Selects warmup exercises for 4-day templates.
     *
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param dayType The type of workout day
     * @param primaryExercise The primary exercise for the day
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectFourDayWarmupExercises(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        dayType: String,
        primaryExercise: Exercise?
    ): Mono<List<Exercise>> {
        val warmupExercises = mutableListOf<Exercise>()

        return if (primaryExercise != null) {
            // Get muscles for the primary exercise
            exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
                .flatMap { primaryMuscles ->
                    val primaryMuscleNames = primaryMuscles.map { it.muscleName.lowercase() }

                    // Adjust target muscles based on day type for better warmup selection
                    val adjustedTargetMuscles =
                        when {
                            dayType.contains("Upper") -> primaryMuscleNames + listOf("shoulders", "upper_back")
                            dayType.contains("Lower") -> primaryMuscleNames + listOf("core", "glutes")
                            else -> primaryMuscleNames
                        }

                    // Select 2 muscle-focused accessory exercises
                    val muscleFocusedMono =
                        selectMuscleFocusedWarmupExercises(
                            exercises = exercises,
                            preferences = preferences,
                            userEquipment = userEquipment,
                            targetMuscles = adjustedTargetMuscles,
                            count = 2
                        )

                    // Select 1 movement pattern exercise
                    val movementPatternMono =
                        selectMovementPatternWarmupExercise(
                            exercises = exercises,
                            preferences = preferences,
                            userEquipment = userEquipment,
                            primaryExercise = primaryExercise
                        )

                    muscleFocusedMono.flatMap { muscleExercises ->
                        movementPatternMono
                            .map { movementExercise ->
                                warmupExercises.addAll(muscleExercises)
                                warmupExercises.add(movementExercise)
                                warmupExercises
                            }
                            .switchIfEmpty(
                                Mono.just(warmupExercises.apply { addAll(muscleExercises) })
                            )
                    }
                }
        } else {
            // Fallback: select 3 general warmup exercises
            selectGeneralWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                count = 3
            )
        }
    }

    /**
     * Selects warmup exercises for 2 and 3 day templates.
     *
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param dayType The type of workout day
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectTwoThreeDayWarmupExercises(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        dayType: String
    ): Mono<List<Exercise>> {
        // For 2 and 3 day templates, focus on common muscles used in ME and DE exercises
        val commonMuscles =
            when {
                dayType.contains("Upper") -> listOf("chest", "shoulders", "triceps", "upper_back", "biceps")
                dayType.contains("Lower") -> listOf("quadriceps", "hamstrings", "glutes", "calves")
                else -> listOf("core", "shoulders", "upper_back") // Fallback for full body
            }

        return selectMuscleFocusedWarmupExercises(
            exercises = exercises,
            preferences = preferences,
            userEquipment = userEquipment,
            targetMuscles = commonMuscles,
            count = 3
        )
    }

    /**
     * Selects muscle-focused warmup exercises.
     *
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param targetMuscles Target muscles to focus on
     * @param count Number of exercises to select
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectMuscleFocusedWarmupExercises(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        targetMuscles: List<String>,
        count: Int
    ): Mono<List<Exercise>> {
        // Filter for accessory exercises that are good for warmup
        val warmupCandidates =
            exercises.filter { exercise ->
                exercise.isAccessory &&
                    !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }

        if (warmupCandidates.isEmpty()) {
            return Mono.just(emptyList())
        }

        // Filter exercises by equipment availability and target muscles
        return Flux.fromIterable(warmupCandidates)
            .flatMap { exercise ->
                // Check if user has equipment for this exercise
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                    .filter { exerciseEquipment ->
                        val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                        userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                    }
                    .flatMap { _ ->
                        // Check if exercise targets any of the target muscles
                        exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                            .filter { exerciseMuscles ->
                                val exerciseMuscleNames = exerciseMuscles.map { it.muscleName.lowercase() }
                                val targetMusclesLower = targetMuscles.map { it.lowercase() }
                                exerciseMuscleNames.any { muscle ->
                                    targetMusclesLower.any { targetMuscle ->
                                        muscle.contains(targetMuscle) || targetMuscle.contains(muscle)
                                    }
                                }
                            }
                            .map { exercise }
                    }
            }
            .collectList()
            .map { filteredExercises ->
                if (filteredExercises.isEmpty()) {
                    // Fallback to any accessory exercises if no muscle/equipment matches
                    warmupCandidates.take(count)
                } else {
                    filteredExercises.take(count)
                }
            }
    }

    /**
     * Selects a movement pattern warmup exercise similar to the primary exercise.
     *
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param primaryExercise The primary exercise
     * @return Mono containing the selected warmup exercise or null
     */
    private fun selectMovementPatternWarmupExercise(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        primaryExercise: Exercise
    ): Mono<Exercise> {
        // Look for accessory exercises with similar movement pattern but lighter
        val warmupCandidates =
            exercises.filter { exercise ->
                exercise.isAccessory &&
                    exercise.movementType == primaryExercise.movementType &&
                    !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }

        if (warmupCandidates.isEmpty()) {
            return Mono.empty()
        }

        // Filter by equipment availability
        return Flux.fromIterable(warmupCandidates)
            .flatMap { exercise ->
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                    .filter { exerciseEquipment ->
                        val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                        userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                    }
                    .map { exercise }
            }
            .next()
            .switchIfEmpty(Mono.just(warmupCandidates.first())) // Fallback to first candidate if no equipment match
    }

    /**
     * Selects general warmup exercises when primary exercise is not available.
     *
     * @param exercises Available exercises
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param count Number of exercises to select
     * @return Mono containing list of selected warmup exercises
     */
    private fun selectGeneralWarmupExercises(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        count: Int
    ): Mono<List<Exercise>> {
        val warmupCandidates =
            exercises.filter { exercise ->
                exercise.isAccessory &&
                    !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }

        if (warmupCandidates.isEmpty()) {
            return Mono.just(emptyList())
        }

        // Filter by equipment availability
        return Flux.fromIterable(warmupCandidates)
            .flatMap { exercise ->
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exercise.name)
                    .filter { exerciseEquipment ->
                        val userEquipmentNames = userEquipment.map { it.equipmentName.lowercase() }.toSet()
                        val exerciseEquipmentNames = exerciseEquipment.map { it.equipmentName.lowercase() }.toSet()
                        userEquipmentNames.any { userEq -> exerciseEquipmentNames.contains(userEq) }
                    }
                    .map { exercise }
            }
            .collectList()
            .map { filteredExercises ->
                if (filteredExercises.isEmpty()) {
                    // Fallback to any accessory exercises if no equipment matches
                    warmupCandidates.take(count)
                } else {
                    filteredExercises.take(count)
                }
            }
    }
}
