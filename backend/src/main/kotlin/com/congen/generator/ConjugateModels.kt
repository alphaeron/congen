package com.congen.generator

import com.congen.model.Band
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserWeakMuscle
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

    /**
     * Equipment types that identify conditioning exercises (sandbag, sled, kettlebell, etc.).
     */
    val CONDITIONING_EQUIPMENT_NAMES =
        setOf(
            "sandbag",
            "sled",
            "kettlebell",
            "battle rope",
            "box",
            "hurdle",
            "rope",
            "tire",
            "med ball"
        )

    /**
     * Returns whether an exercise uses at least one piece of conditioning equipment.
     *
     * @param exerciseEquipment Equipment required for the exercise
     * @return true if the exercise uses conditioning equipment
     */
    fun exerciseUsesConditioningEquipment(exerciseEquipment: List<ExerciseEquipment>): Boolean {
        if (exerciseEquipment.isEmpty()) {
            return false
        }
        val conditioningNamesLower = CONDITIONING_EQUIPMENT_NAMES.map { it.lowercase() }.toSet()
        return exerciseEquipment.any { equipment ->
            conditioningNamesLower.contains(equipment.equipmentName.lowercase())
        }
    }
}

/**
 * Represents prepared data for workout generation.
 */
data class WorkoutGenerationPreparedData(
    /** The user's exercise pool containing available exercises. */
    val userExercisePool: UserExercisePool,
    /** The user's one rep max records for strength calculations. */
    val oneRepMaxes: List<UserOneRepMax>,
    /** The program preferences including days per week and other settings. */
    val programPreferences: ProgramPreferences,
    /** User-configured weak muscles; empty when the user has not specified any. */
    val weakMuscles: List<UserWeakMuscle>,
    /** The current week number in the training program. */
    val currentWeekNumber: Int,
    /** The ID of the user for whom the workout is being generated. */
    val userId: String,
    /** Weight unit preferences for different exercises. */
    val weightUnitPreferences: Map<String, WeightUnit>,
    /** Mapping of exercise names to their associated muscles. */
    val exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
    /** Mapping of exercise names to their workout type classifications. */
    val exerciseWorkoutTypeMappings: Map<String, List<String>>,
    /** Mapping of exercise names to their required equipment. */
    val exerciseEquipmentMappings: Map<String, List<ExerciseEquipment>>,
    /** List of exercises that were previously programmed to avoid repetition. */
    val previouslyProgrammedExercises: List<String>,
    /** All available exercises in the system. */
    val allExercises: List<Exercise>,
    /** The user's available equipment. */
    val userEquipment: List<UserEquipment>,
    /** The user's exercise preferences and settings. */
    val userExercisePreferences: List<UserExercisePreference>,
    /** DE primary exercise name per day-type key (e.g. "DE_Lower", "DE_Upper") from the current 4-week cycle start. */
    val dePrimaryExerciseByDayType: Map<String, String> = emptyMap()
)

/**
 * Represents a complete workout generation result with all associated data.
 */
data class WorkoutGenerationResult(
    /** The ID of the program this workout belongs to. */
    val programId: Long,
    /** The day number within the program. */
    val dayNumber: Int,
    /** The type of day (e.g., "DE_Upper", "ME_Lower", "Full_Body"). */
    val dayType: String,
    /** The ID of the user for whom the workout was generated. */
    val userId: String,
    /** The workout stages containing exercises and set schemes. */
    val stages: List<WorkoutStageData>,
    /** The prepared data used for workout generation. */
    val preparedData: WorkoutGenerationPreparedData
)

/**
 * Represents workout stage data for atomic writes.
 */
data class WorkoutStageData(
    /** The type of workout stage (e.g., WARMUP, PRIMARY, SECONDARY, ACCESSORY, CONDITIONING). */
    val stageType: WorkoutStageTypeEnum,
    /** The position of this stage within the workout. */
    val position: Int,
    /** The name of the workout stage. */
    val name: String,
    /** The exercises programmed for this stage. */
    val exercises: List<ProgrammedExerciseData>
)

/**
 * Represents programmed exercise data for atomic writes.
 */
data class ProgrammedExerciseData(
    /** The name of the exercise. */
    val exerciseName: String,
    /** The position of this exercise within the stage. */
    val position: Int,
    /** Optional notes for the exercise. */
    val notes: String?,
    /** The set schemes defining the exercise parameters. */
    val setSchemes: List<SetSchemeParams>
)
