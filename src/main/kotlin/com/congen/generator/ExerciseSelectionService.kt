package com.congen.generator

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
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
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
     * @return Mono containing selected exercise or null if none available
     */
    fun selectRotatingExercise(
        targetMuscles: List<String>,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        isAccessory: Boolean,
        rotationHistory: List<ExerciseRotationHistory>
    ): Mono<Exercise?> {
        // Filter exercises based on preferences (exercises are already filtered by is_accessory)
        val availableExercises =
            exercises.filter { exercise ->
                !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }

        if (availableExercises.isEmpty()) {
            logger.warn("No available exercises found for isAccessory: {}", isAccessory)
            return Mono.justOrEmpty(null)
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
            return Mono.justOrEmpty(availableExercises.firstOrNull()) // Fallback to any available exercise
        }

        // Filter exercises by target muscles - this is the key fix!
        return Flux.fromIterable(equipmentFilteredExercises)
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
            .flatMap { muscleFilteredExercises ->
                if (muscleFilteredExercises.isEmpty()) {
                    logger.warn("No exercises found matching target muscles: {} for isAccessory: {}", targetMuscles, isAccessory)
                    return@flatMap Mono.justOrEmpty(null)
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

                val selectedExercise = sortedExercises.firstOrNull()
                return@flatMap Mono.justOrEmpty(selectedExercise)
            }
    }

    /**
     * Selects a secondary exercise similar to the primary exercise in terms of movement type and muscles worked.
     *
     * @param primaryExercise The primary exercise to find a similar secondary exercise for
     * @param userEquipment List of user's available equipment
     * @param preferences List of user's exercise preferences
     * @param exercises List of available exercises (already filtered to exclude primary exercise)
     * @param rotationHistory List of exercise rotation history
     * @return Selected secondary exercise or null if none available
     */
    fun selectSimilarSecondaryExercise(
        primaryExercise: Exercise,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        rotationHistory: List<ExerciseRotationHistory>
    ): Mono<Exercise?> {
        val availableExercises =
            exercises.filter { exercise ->
                !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
            }
        if (availableExercises.isEmpty()) {
            logger.warn("No available exercises found for secondary movement")
            return Mono.justOrEmpty(null)
        }
        val equipmentFilteredExercises =
            availableExercises.filter { exercise ->
                userEquipment.any { _ -> true }
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
                                        rotationHistory = rotationHistory
                                    )
                                exercise to exerciseScore
                            }
                            .onErrorReturn(exercise to 0.0)
                    }
                if (exerciseScoringMonos.isEmpty()) {
                    return@flatMap Mono.justOrEmpty(null)
                }
                @Suppress("UNCHECKED_CAST")
                return@flatMap Mono.zip(exerciseScoringMonos) { results: Array<Any?> ->
                    val scoredExercises = results.map { it as Pair<Exercise, Double> }
                    scoredExercises
                        .sortedByDescending { it.second }
                        .firstOrNull()
                        ?.first
                } as Mono<Exercise?>
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
     * @return Similarity score (higher is more similar)
     */
    private fun calculateExerciseSimilarityScore(
        exercise: Exercise,
        primaryMovementType: MovementType,
        primaryMuscles: Set<String>,
        exerciseMuscles: Set<String>,
        rotationHistory: List<ExerciseRotationHistory>
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
}
