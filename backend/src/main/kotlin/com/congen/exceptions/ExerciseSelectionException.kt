package com.congen.exceptions

/**
 * Custom exception for exercise selection failures.
 * This allows for more robust error handling by checking exception type
 * rather than message contents.
 */
class ExerciseSelectionException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    
    companion object {
        /**
         * Creates an exception for when no exercises are available after workout-type filtering.
         */
        fun noExercisesAfterWorkoutTypeFiltering(workoutType: String, isAccessory: Boolean): ExerciseSelectionException {
            return ExerciseSelectionException(
                "No exercises available after workout-type filtering for " +
                    "workoutType: $workoutType and isAccessory: $isAccessory"
            )
        }
        
        /**
         * Creates an exception for when no suitable exercise is found for given criteria.
         */
        fun noSuitableExerciseFound(
            targetMuscles: List<String>,
            isAccessory: Boolean,
            workoutType: String,
            dayType: String,
            movementBalanceState: Any?
        ): ExerciseSelectionException {
            return ExerciseSelectionException(
                "No suitable exercise found for the given criteria. " +
                    "Parameters: targetMuscles=$targetMuscles, isAccessory=$isAccessory, " +
                    "workoutType=$workoutType, dayType=$dayType, movementBalanceState=$movementBalanceState"
            )
        }
        
        /**
         * Creates an exception for when no exercises are found for target muscles.
         */
        fun noExercisesForTargetMuscles(
            targetMuscles: List<String>,
            isAccessory: Boolean,
            isWarmup: Boolean
        ): ExerciseSelectionException {
            return ExerciseSelectionException(
                "No exercises found for target muscles: $targetMuscles " +
                    "for isAccessory: $isAccessory (isWarmup: $isWarmup)"
            )
        }
    }
}
