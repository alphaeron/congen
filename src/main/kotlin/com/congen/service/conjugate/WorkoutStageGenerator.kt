package com.congen.service.conjugate

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.model.Exercise
import com.congen.model.ProgrammedExercise
import com.congen.model.UserOneRepMax
import com.congen.model.WorkoutStage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * Service for generating workout stages, programmed exercises, and set schemes
 * for conjugate powerlifting workouts.
 *
 * This service handles the creation of workout components including:
 * - Workout stages (primary, secondary, accessory, conditioning)
 * - Programmed exercises within each stage
 * - Set schemes with Prilepin-based guidelines and undulating periodization
 *
 * ## Workout Structure
 *
 * Each workout consists of:
 * - **Primary movement**: Max Effort (ME) or Dynamic Effort (DE) exercise
 * - **Secondary movement**: Additional compound movement (if applicable)
 * - **Accessory movements**: Targeted muscle development exercises
 * - **Conditioning**: AMRAP or EMOM exercises (for DE days)
 *
 * ## Set Scheme Generation
 *
 * Set schemes are generated using:
 * - **Prilepin's Table**: Optimal volume and intensity guidelines
 * - **Undulating Periodization**: Varying intensity across weeks
 * - **Exercise-Specific Guidelines**: Different parameters for different movement types
 *
 * @property workoutStageDAL Data access layer for workout stage operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property setSchemeDAL Data access layer for set scheme operations
 * @property prilepinGuidelinesService Service for Prilepin-based guidelines
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageGenerator(
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val prilepinGuidelinesService: PrilepinGuidelinesService,
) {
    /**
     * Creates a workout stage.
     *
     * @param workoutId The ID of the workout
     * @param stageType The type of stage (primary, secondary, accessory, conditioning)
     * @param position The position of the stage in the workout
     * @return Mono containing the created workout stage
     */
    fun createWorkoutStage(
        workoutId: Long,
        stageType: String,
        position: Int
    ): Mono<WorkoutStage> {
        val stageTypeId =
            when (stageType) {
                "primary" -> 1
                "secondary" -> 2
                "accessory" -> 3
                "conditioning" -> 4
                else -> 1
            }
        return workoutStageDAL.insertWorkoutStage(workoutId, stageTypeId.toLong(), position)
    }

    /**
     * Creates a programmed exercise.
     *
     * @param workoutStageId The ID of the workout stage
     * @param exerciseName The name of the exercise
     * @return Mono containing the created programmed exercise
     */
    fun createProgrammedExercise(
        workoutStageId: Long,
        exerciseName: String
    ): Mono<ProgrammedExercise> {
        return programmedExerciseDAL.insertProgrammedExercise(workoutStageId, exerciseName, null)
    }

    /**
     * Creates set schemes for a programmed exercise.
     *
     * @param programmedExerciseId The ID of the programmed exercise
     * @param setSchemeParams List of set scheme parameters to create
     * @return Mono that completes when all set schemes are created
     */
    fun createSetSchemes(
        programmedExerciseId: Long,
        setSchemeParams: List<SetSchemeParams>
    ): Mono<Void> {
        return setSchemeParams.fold(Mono.empty<Void>()) { mono, params ->
            mono.flatMap {
                setSchemeDAL.insertSetScheme(
                    programmedExerciseId,
                    params.setNumber,
                    params.wasSetPerformed,
                    params.isAmrap,
                    params.isEmom,
                    params.useTempo,
                    params.eccentricTempo,
                    params.isometricTempo,
                    params.concentricTempo,
                    params.targetWeight,
                    params.performedWeight,
                    params.targetRepCount,
                    params.performedRepCount,
                    params.restSeconds
                ).then()
            }
        }
    }

    /**
     * Generates a Prilepin-based set scheme for an exercise with undulating periodization.
     *
     * @param userId The user ID
     * @param exercise The exercise to generate a scheme for
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @param oneRepMaxes List of user's one rep max values
     * @param currentWeekNumber The current week number in the program
     * @return List of set scheme parameters
     */
    fun generatePrilepinBasedScheme(
        userId: Int,
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int
    ): List<SetSchemeParams> {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                movementRole = movementRole,
                currentWeekNumber = currentWeekNumber,
                exercise = exercise.name
            )

        val repsPerSet = guidelines.repsPerSetRange.random()
        val numSets = (guidelines.totalReps / repsPerSet).toInt()
        val restSeconds = guidelines.restSeconds.random()

        // Determine target weight
        val targetWeight = getTargetWeight(userId, exercise.name, intensity, oneRepMaxes)

        // Tempo: vary if accessory, else default
        val useTempo = movementRole != "primary" && Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(1, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(0, 3).toString() else "0"
        val concentric =
            if (useTempo) {
                if (Random.nextBoolean()) {
                    "1"
                } else {
                    "X"
                }
            } else {
                "0"
            }

        return (1..numSets).map { setNumber ->
            SetSchemeParams(
                setNumber = setNumber,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = repsPerSet,
                performedRepCount = null,
                restSeconds = restSeconds
            )
        }
    }

    /**
     * Generates a set scheme for secondary exercises with specific guidelines.
     *
     * Secondary exercises use:
     * - 80-90% intensity (0.8-0.9)
     * - 3-4 sets
     * - 5-8 reps per set
     * - 180-300 second rest periods
     *
     * @param userId The user ID
     * @param exercise The exercise to generate a scheme for
     * @param oneRepMaxes List of user's one rep max values
     * @return List of set scheme parameters
     */
    fun generateSecondaryExerciseScheme(
        userId: Int,
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>
    ): List<SetSchemeParams> {
        // Secondary exercise guidelines: 80-90% intensity, 3-4 sets of 5-8 reps
        val intensity = Random.nextDouble(0.8, 0.9)
        val repsPerSet = (5..8).random()
        val numSets = (3..4).random()
        val restSeconds = (180..300).random()

        // Determine target weight
        val targetWeight = getTargetWeight(userId, exercise.name, intensity, oneRepMaxes)

        // Tempo: vary for secondary exercises
        val useTempo = Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(1, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(0, 3).toString() else "0"
        val concentric =
            if (useTempo) {
                if (Random.nextBoolean()) {
                    "1"
                } else {
                    "X"
                }
            } else {
                "0"
            }

        return (1..numSets).map { setNumber ->
            SetSchemeParams(
                setNumber = setNumber,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = repsPerSet,
                performedRepCount = null,
                restSeconds = restSeconds
            )
        }
    }

    /**
     * Generates an AMRAP or EMOM set scheme for conditioning exercises.
     *
     * @param userId The user ID
     * @param exercise The exercise to generate a scheme for
     * @param oneRepMaxes List of user's one rep max values
     * @return List of set scheme parameters
     */
    fun generateAmrapOrEmomScheme(
        userId: Int,
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>
    ): List<SetSchemeParams> {
        val isAmrap = Random.nextBoolean()
        val targetWeight = getTargetWeight(userId, exercise.name, 0.5, oneRepMaxes)

        val useTempo = Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(2, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(1, 3).toString() else "0"
        val concentric =
            if (useTempo) {
                if (Random.nextBoolean()) {
                    "1"
                } else {
                    "X"
                }
            } else {
                "0"
            }

        return listOf(
            SetSchemeParams(
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = isAmrap,
                isEmom = !isAmrap,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = null, // Varies per person for AMRAP/EMOM
                performedRepCount = null,
                restSeconds = if (isAmrap) 0 else 60
            )
        )
    }

    /**
     * Gets the target weight for an exercise based on user's 1RM.
     *
     * @param userId The user ID
     * @param exerciseName The name of the exercise
     * @param intensity The intensity as a percentage of 1RM
     * @param oneRepMaxes List of user's one rep max values
     * @return The target weight
     */
    private fun getTargetWeight(
        userId: Int,
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>
    ): BigDecimal {
        val oneRepMax = oneRepMaxes.find { it.exerciseName == exerciseName }?.oneRepMax
        return if (oneRepMax != null) {
            (oneRepMax * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
        } else {
            // Default weight for new users (would need to be configured)
            BigDecimal(ConjugateConstants.DEFAULT_WEIGHT)
        }
    }
}
