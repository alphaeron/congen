package com.congen.generator

import com.congen.model.Band
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStageTypeEnum
import java.math.BigDecimal

/**
 * Data class for day templates.
 */
data class DayTemplate(
    /** The type of day template. */
    val type: String
)

/**
 * Data class for Prilepin guidelines.
 */
data class PrilepinGuidelines(
    /** The intensity range as a percentage of 1RM. */
    val intensityRange: ClosedFloatingPointRange<Double>,
    /** The range of reps per set. */
    val repsPerSetRange: IntRange,
    /** The optimal total number of reps to perform. */
    val totalReps: Int,
    /** The acceptable range of total reps to perform. */
    val totalRepsRange: IntRange,
    /** The range of rest seconds between sets. */
    val restSeconds: IntRange
)

/**
 * Data class to hold set scheme parameters without requiring temporary IDs.
 */
data class SetSchemeParams(
    /** The set number within the workout. */
    val setNumber: Int,
    /** Whether this is an "as many reps as possible" set. */
    val isAmrap: Boolean,
    /** Whether this is an "every minute on the minute" set. */
    val isEmom: Boolean,
    /** Whether tempo training should be used for this set. */
    val useTempo: Boolean,
    /** The eccentric tempo (lowering phase) in seconds. */
    val eccentricTempo: String?,
    /** The isometric tempo (pause phase) in seconds. */
    val isometricTempo: String?,
    /** The concentric tempo (lifting phase) in seconds. */
    val concentricTempo: String?,
    /** The target weight for this set in kilograms. */
    val targetWeight: BigDecimal?,
    /** The actual weight performed for this set in kilograms. */
    val performedWeight: BigDecimal?,
    /** The target number of repetitions for this set. */
    val targetRepCount: Int?,
    /** The actual number of repetitions performed for this set. */
    val performedRepCount: Int?,
    /** The rest time in seconds before the next set. */
    val restSeconds: Int?,
    /** The band information for Dynamic Effort exercises. */
    val band: Band?
)

/**
 * Constants for conjugate workout generation.
 */
object ConjugateConstants {
    /** Default weak muscles for new users - upper body muscles */
    val DEFAULT_UPPER_BODY_WEAK_MUSCLES = listOf("rear deltoid", "lats", "triceps")

    /** Default weak muscles for new users - lower body muscles */
    val DEFAULT_LOWER_BODY_WEAK_MUSCLES = listOf("hamstrings", "glutes")

    /** Default weak muscles for new users - all muscles (for full body days) */
    val DEFAULT_WEAK_MUSCLES = listOf("hamstrings", "glutes", "upper back", "lats")

    /**
     * Determines the appropriate weak muscles based on day type.
     *
     * @param dayType The type of day (e.g., "DE_Upper", "ME_Lower", "Full_Body")
     * @return List of weak muscles appropriate for the day type
     */
    fun getWeakMusclesForDayType(dayType: String): List<String> {
        return when {
            dayType.contains("Upper") -> DEFAULT_UPPER_BODY_WEAK_MUSCLES
            dayType.contains("Lower") -> DEFAULT_LOWER_BODY_WEAK_MUSCLES
            else -> DEFAULT_WEAK_MUSCLES // Full body or other day types
        }
    }

    /** Time allocation for different workout components (in minutes) */
    object TimeAllocation {
        /** Time allocated for warmup exercises in minutes. */
        const val WARMUP_TIME_IN_MINUTES = 10

        /** Time allocated for primary movement exercises in minutes. */
        const val PRIMARY_MOVEMENT_TIME_IN_MINUTES = 10

        /** Time allocated for secondary movement exercises in minutes. */
        const val SECONDARY_MOVEMENT_TIME_IN_MINUTES = 8

        /** Time allocated for conditioning exercises in minutes. */
        const val CONDITIONING_TIME_IN_MINUTES = 10

        /** Time allocated for a single accessory exercise in minutes. */
        const val SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES = 5
    }

    /** Default session time length in minutes */
    const val DEFAULT_SESSION_TIME_MINUTES = 60

    /** Maximum number of muscles allowed for warmup exercises */
    const val MAX_MUSCLES_FOR_WARMUP = 3
}

/**
 * Represents prepared data for workout generation.
 */
data class WorkoutGenerationPreparedData(
    val userExercisePool: UserExercisePool,
    val oneRepMaxes: List<UserOneRepMax>,
    val programPreferences: ProgramPreferences,
    val weakMuscles: List<String>,
    val currentWeekNumber: Int,
    val userId: String,
    val weightUnitPreferences: Map<String, WeightUnit>,
    val exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
    val exerciseWorkoutTypeMappings: Map<String, List<String>>,
    val exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
    val previouslyProgrammedExercises: List<String>,
    val allExercises: List<Exercise>,
    val userEquipment: List<UserEquipment>,
    val userExercisePreferences: List<UserExercisePreference>
)

/**
 * Represents a complete workout generation result with all associated data.
 */
data class WorkoutGenerationResult(
    val programId: Long,
    val dayNumber: Int,
    val dayType: String,
    val userId: String,
    val stages: List<WorkoutStageData>,
    val preparedData: WorkoutGenerationPreparedData
)

/**
 * Represents workout stage data for atomic writes.
 */
data class WorkoutStageData(
    val stageType: WorkoutStageTypeEnum,
    val position: Int,
    val name: String,
    val exercises: List<ProgrammedExerciseData>
)

/**
 * Represents programmed exercise data for atomic writes.
 */
data class ProgrammedExerciseData(
    val exerciseName: String,
    val position: Int,
    val notes: String?,
    val setSchemes: List<SetSchemeParams>
)
