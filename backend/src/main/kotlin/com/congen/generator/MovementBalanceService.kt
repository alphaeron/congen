package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.MovementType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Service for managing movement balance constraints in workout generation.
 *
 * This service implements soft constraints to ensure balanced movement patterns
 * in workouts while never blocking workout generation if constraints cannot be met.
 *
 * ## Movement Balance Constraints
 *
 * 1. **Vertical Push / Horizontal Pull Balance**: If one is included, try to include the other
 * 2. **Horizontal Push / Vertical Pull Balance**: If one is included, try to include the other
 * 3. **Pull-to-Push Volume Ratio**: Aim for 2:1 ratio of pull volume to push volume
 *
 * ## Implementation Notes
 *
 * - All constraints are soft and will not block workout generation
 * - Volume is calculated as sets * reps * weight
 * - Constraints are applied during exercise selection to influence choices
 * - Fallback to original selection logic if constraints cannot be satisfied
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class MovementBalanceService {
    companion object {
        private val logger = LoggerFactory.getLogger(MovementBalanceService::class.java)

        /** Target pull-to-push volume ratio */
        const val TARGET_PULL_TO_PUSH_RATIO = 2.0

        /** Movement type pairs that should be balanced */
        private val BALANCED_MOVEMENT_PAIRS =
            mapOf(
                MovementType.VERTICAL_PUSH to MovementType.HORIZONTAL_PULL,
                MovementType.HORIZONTAL_PUSH to MovementType.VERTICAL_PULL
            )

        /** Push movement types for volume tracking */
        private val PUSH_MOVEMENT_TYPES =
            setOf(
                MovementType.HORIZONTAL_PUSH,
                MovementType.VERTICAL_PUSH
            )

        /** Pull movement types for volume tracking */
        private val PULL_MOVEMENT_TYPES =
            setOf(
                MovementType.HORIZONTAL_PULL,
                MovementType.VERTICAL_PULL
            )
    }

    /**
     * Data class representing the current state of movement balance in a workout.
     *
     * @param selectedExercises List of exercises already selected for the workout
     * @param selectedExercises List of exercises currently selected for the workout
     * @param movementTypeCounts Count of each movement type in the workout
     * @param pushVolume Total volume of push movements (sets * reps * weight)
     * @param pullVolume Total volume of pull movements (sets * reps * weight)
     */
    data class MovementBalanceState(
        val selectedExercises: List<Exercise> = emptyList(),
        val movementTypeCounts: Map<MovementType, Int> = emptyMap(),
        val pushVolume: BigDecimal = BigDecimal.ZERO,
        val pullVolume: BigDecimal = BigDecimal.ZERO
    ) {
        /**
         * Gets the current pull-to-push volume ratio.
         *
         * @return The ratio of pull volume to push volume, or 0.0 if no push volume
         */
        fun getPullToPushRatio(): Double {
            return if (pushVolume > BigDecimal.ZERO) {
                pullVolume.toDouble() / pushVolume.toDouble()
            } else {
                0.0
            }
        }

        /**
         * Checks if a movement type needs its balancing counterpart.
         *
         * @param movementType The movement type to check
         * @return true if the balancing movement type should be prioritized
         */
        fun needsBalancingMovement(movementType: MovementType): Boolean {
            val balancingType = BALANCED_MOVEMENT_PAIRS[movementType] ?: return false
            val currentCount = movementTypeCounts[movementType] ?: 0
            val balancingCount = movementTypeCounts[balancingType] ?: 0

            // If we have one but not the other, we need balancing
            return currentCount > 0 && balancingCount == 0
        }

        /**
         * Checks if pull movements need more volume relative to push movements.
         *
         * @return true if more pull volume is needed
         */
        fun needsMorePullVolume(): Boolean {
            return getPullToPushRatio() < TARGET_PULL_TO_PUSH_RATIO
        }

        /**
         * Updates the state with a new exercise and its estimated volume.
         *
         * @param exercise The exercise to add
         * @param estimatedVolume The estimated volume for this exercise
         * @return Updated MovementBalanceState
         */
        fun addExercise(
            exercise: Exercise,
            estimatedVolume: BigDecimal
        ): MovementBalanceState {
            val newMovementTypeCounts = movementTypeCounts.toMutableMap()
            newMovementTypeCounts[exercise.movementType] =
                (newMovementTypeCounts[exercise.movementType] ?: 0) + 1

            val newPushVolume =
                if (PUSH_MOVEMENT_TYPES.contains(exercise.movementType)) {
                    pushVolume + estimatedVolume
                } else {
                    pushVolume
                }

            val newPullVolume =
                if (PULL_MOVEMENT_TYPES.contains(exercise.movementType)) {
                    pullVolume + estimatedVolume
                } else {
                    pullVolume
                }

            return copy(
                selectedExercises = selectedExercises + exercise,
                movementTypeCounts = newMovementTypeCounts,
                pushVolume = newPushVolume,
                pullVolume = newPullVolume
            )
        }
    }

    /**
     * Calculates estimated volume for an exercise based on typical set schemes.
     *
     * This is a rough estimation used for balance calculations. The actual volume
     * will be determined when set schemes are generated.
     *
     * @param isAccessory Whether this is an accessory exercise
     * @return Estimated volume (sets * reps * weight)
     */
    fun estimateExerciseVolume(isAccessory: Boolean): BigDecimal {
        // Rough estimation based on typical conjugate programming
        val estimatedSets = if (isAccessory) 3 else 4
        val estimatedReps = if (isAccessory) 12 else 6
        val estimatedWeight = if (isAccessory) 50.0 else 100.0 // kg, rough estimate

        return BigDecimal(estimatedSets * estimatedReps * estimatedWeight)
    }

    /**
     * Scores exercises based on movement balance constraints.
     *
     * Higher scores indicate exercises that better satisfy the balance constraints.
     * This method should be used to influence exercise selection without blocking it.
     *
     * @param exercise The exercise to score
     * @param currentState Current movement balance state
     * @return Balance score (higher is better for selection)
     */
    fun scoreExerciseForBalance(
        exercise: Exercise,
        currentState: MovementBalanceState
    ): Double {
        var score = 0.0

        // Check if this exercise would balance an existing movement type
        // Look for any existing movement type that this exercise would balance
        for ((existingType, balancingType) in BALANCED_MOVEMENT_PAIRS) {
            if (exercise.movementType == balancingType) {
                val existingCount = currentState.movementTypeCounts[existingType] ?: 0
                val currentCount = currentState.movementTypeCounts[exercise.movementType] ?: 0

                // If we have the existing type but not this balancing type, this exercise would balance it
                if (existingCount > 0 && currentCount == 0) {
                    score += 10.0
                    logger.debug("Exercise {} balances existing movement type {}", exercise.name, existingType)
                }
            }
        }

        // Check if this exercise helps with pull-to-push volume ratio
        if (PULL_MOVEMENT_TYPES.contains(exercise.movementType) && currentState.needsMorePullVolume()) {
            score += 5.0
            logger.debug("Exercise {} helps increase pull volume ratio", exercise.name)
        }

        // Small penalty for adding more push volume when we already have too much
        if (PUSH_MOVEMENT_TYPES.contains(exercise.movementType) &&
            currentState.getPullToPushRatio() < TARGET_PULL_TO_PUSH_RATIO
        ) {
            score -= 2.0
            logger.debug("Exercise {} adds push volume when pull volume is needed", exercise.name)
        }

        return score
    }

    /**
     * Filters and sorts exercises based on movement balance constraints.
     *
     * This method modifies the exercise selection priority to favor exercises
     * that better satisfy the balance constraints while maintaining all other
     * selection criteria.
     *
     * @param exercises List of exercises to filter and sort
     * @param currentState Current movement balance state
     * @return Sorted list of exercises with balance scores
     */
    fun prioritizeExercisesForBalance(
        exercises: List<Exercise>,
        currentState: MovementBalanceState
    ): List<Exercise> {
        if (exercises.isEmpty()) {
            return exercises
        }

        // Calculate balance scores for all exercises
        val exercisesWithScores =
            exercises.map { exercise ->
                exercise to scoreExerciseForBalance(exercise, currentState)
            }

        // Sort by balance score (descending) and then by exercise name for consistency
        val sortedExercises =
            exercisesWithScores
                .sortedWith(
                    compareByDescending<Pair<Exercise, Double>> { it.second }
                        .thenBy { it.first.name }
                )
                .map { it.first }

        logger.debug(
            "Prioritized {} exercises for balance. Top 3: {}",
            sortedExercises.size,
            sortedExercises.take(3).map { it.name }
        )

        return sortedExercises
    }

    /**
     * Creates an initial movement balance state.
     *
     * @return Empty MovementBalanceState
     */
    fun createInitialState(): MovementBalanceState {
        return MovementBalanceState()
    }

    /**
     * Logs the current movement balance state for debugging.
     *
     * @param state The current movement balance state
     * @param workoutName Name of the workout for logging context
     */
    fun logBalanceState(
        state: MovementBalanceState,
        workoutName: String
    ) {
        logger.debug(
            "Movement balance for {}: Push volume={}, Pull volume={}, Ratio={:.2f}, Movement counts={}",
            workoutName,
            state.pushVolume,
            state.pullVolume,
            state.getPullToPushRatio(),
            state.movementTypeCounts
        )
    }
}
