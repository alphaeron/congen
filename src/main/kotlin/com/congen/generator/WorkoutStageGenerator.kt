package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ProgrammedExercise
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.SetSchemeService
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
 * @property workoutStageTypeDAL Data access layer for workout stage type operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property setSchemeDAL Data access layer for set scheme operations
 * @property setSchemeService Service for set scheme operations
 * @property prilepinGuidelinesService Service for Prilepin-based guidelines
 * @property weightSelectionService Service for conjugate-specific weight selection
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageGenerator(
    private val workoutStageDAL: WorkoutStageDAL,
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val setSchemeService: SetSchemeService,
    private val prilepinGuidelinesService: PrilepinGuidelinesService,
    private val weightSelectionService: WeightSelectionService,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
) {
    /**
     * Creates a workout stage if it doesn't already exist.
     *
     * This method first checks if a workout stage already exists for the given workout ID and position.
     * If it exists, it returns the existing stage. If not, it creates a new one.
     *
     * @param workoutId The ID of the workout
     * @param stageType The type of stage
     * @param position The position of the stage in the workout
     * @return Mono containing the workout stage (either existing or newly created)
     */
    fun createWorkoutStage(
        workoutId: Long,
        stageType: WorkoutStageTypeEnum,
        position: Int
    ): Mono<WorkoutStage> {
        return workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(workoutId, position)
            .onErrorResume(NoResultsFoundException::class.java) {
                // Stage doesn't exist, create it
                workoutStageTypeDAL.selectWorkoutStageTypeByEnum(stageType)
                    .flatMap { workoutStageType ->
                        workoutStageDAL.insertWorkoutStage(workoutId, workoutStageType.id, position, stageType.displayName)
                    }
            }
    }

    /**
     * Creates a programmed exercise if it doesn't already exist.
     *
     * This method first checks if a programmed exercise already exists for the given workout stage ID and exercise name.
     * If it exists, it returns the existing exercise. If not, it creates a new one.
     *
     * @param workoutStageId The ID of the workout stage
     * @param exerciseName The name of the exercise
     * @return Mono containing the programmed exercise (either existing or newly created)
     */
    fun createProgrammedExercise(
        workoutStageId: Long,
        exerciseName: String
    ): Mono<ProgrammedExercise> {
        return programmedExerciseDAL.selectProgrammedExerciseByStageIdAndExerciseName(workoutStageId, exerciseName)
            .onErrorResume(NoResultsFoundException::class.java) {
                // Exercise doesn't exist, create it
                programmedExerciseDAL.insertProgrammedExercise(workoutStageId, exerciseName, 1, null)
            }
    }

    /**
     * Creates set schemes for a programmed exercise with unit conversion based on user preferences.
     * Only creates set schemes if they don't already exist for the exercise.
     *
     * @param userId The ID of the user (needed for weight unit preferences)
     * @param programmedExerciseId The ID of the programmed exercise
     * @param exerciseName The name of the exercise (needed for weight unit preferences)
     * @param setSchemeParams List of set scheme parameters to create
     * @return Mono that completes when all set schemes are created
     */
    fun createSetSchemes(
        userId: Int,
        programmedExerciseId: Long,
        exerciseName: String,
        setSchemeParams: List<SetSchemeParams>
    ): Mono<Void> {
        // First check if set schemes already exist for this exercise
        return setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)
            .flatMap { existingSchemes ->
                if (existingSchemes.isNotEmpty()) {
                    // Set schemes already exist, skip creation
                    Mono.empty()
                } else {
                    // No set schemes exist, create them
                    Flux.fromIterable(setSchemeParams)
                        .concatMap { params ->
                            // Get user's weight unit preference for this exercise, default to KG if not found
                            val unitMono =
                                userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
                                    .map { it.preferredUnit.name }
                                    .onErrorResume(NoResultsFoundException::class.java) {
                                        // Default to KG if no preference found
                                        Mono.just(WeightUnit.KG.name)
                                    }

                            unitMono.flatMap { unit ->
                                // Convert target weight to string for the service method
                                val targetWeightString = params.targetWeight?.toString()

                                setSchemeService.createSetScheme(
                                    programmedExerciseId,
                                    params.setNumber,
                                    params.isAmrap,
                                    params.isEmom,
                                    params.useTempo,
                                    params.eccentricTempo,
                                    params.isometricTempo,
                                    params.concentricTempo,
                                    targetWeightString,
                                    params.performedWeight?.toString(),
                                    params.targetRepCount,
                                    params.performedRepCount,
                                    params.restSeconds,
                                    unit,
                                    params.band
                                ).then()
                            }
                        }
                        .then()
                }
            }
            .then()
    }

    /**
     * Generates a Prilepin-based set scheme for an exercise with undulating periodization.
     *
     * @param exercise The exercise to generate a scheme for
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @param oneRepMaxes List of user's one rep max values
     * @param currentWeekNumber The current week number in the program
     * @param userId The user ID for weight unit preferences
     * @return Mono containing list of set scheme parameters
     */
    fun generatePrilepinBasedScheme(
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        userId: Int
    ): Mono<List<SetSchemeParams>> {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber
            )

        val repsPerSet = guidelines.repsPerSetRange.random()
        val numSets = (guidelines.totalReps / repsPerSet).toInt()
        val restSeconds = guidelines.restSeconds.random()

        val isDynamicEffort = dayType.startsWith("DE_")
        // For non-DE exercises, use standard weight calculation
        return weightSelectionService.getTargetWeight(
            exercise.name,
            intensity,
            oneRepMaxes,
            userId,
            isDynamicEffort = isDynamicEffort,
            currentWeekNumber = currentWeekNumber
        )
            .map { result ->
                val useTempo = movementRole != "primary" && !isDynamicEffort && Random.nextBoolean()
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
                (1..numSets).map { setNumber ->
                    SetSchemeParams(
                        setNumber = setNumber,
                        isAmrap = false,
                        isEmom = false,
                        useTempo = useTempo,
                        eccentricTempo = eccentric,
                        isometricTempo = isometric,
                        concentricTempo = concentric,
                        targetWeight = result.targetWeight,
                        performedWeight = null,
                        targetRepCount = repsPerSet,
                        performedRepCount = null,
                        restSeconds = restSeconds,
                        band = result.band,
                    )
                }
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
     * @param exercise The exercise to generate a scheme for
     * @param oneRepMaxes List of user's one rep max values
     * @param userId The user ID for weight unit preferences
     * @return Mono containing list of set scheme parameters
     */
    fun generateSecondaryExerciseScheme(
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>,
        userId: Int
    ): Mono<List<SetSchemeParams>> {
        // Secondary exercise guidelines: 80-90% intensity, 3-4 sets of 5-8 reps
        val intensity = Random.nextDouble(0.8, 0.9)
        val repsPerSet = (5..8).random()
        val numSets = (3..4).random()
        val restSeconds = (180..300).random()

        // Determine target weight
        return weightSelectionService.getTargetWeight(exercise.name, intensity, oneRepMaxes, userId, isDynamicEffort = false)
            .map { result ->
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

                (1..numSets).map { setNumber ->
                    SetSchemeParams(
                        setNumber = setNumber,
                        isAmrap = false,
                        isEmom = false,
                        useTempo = useTempo,
                        eccentricTempo = eccentric,
                        isometricTempo = isometric,
                        concentricTempo = concentric,
                        targetWeight = result.targetWeight,
                        performedWeight = null,
                        targetRepCount = repsPerSet,
                        performedRepCount = null,
                        restSeconds = restSeconds,
                        band = result.band,
                    )
                }
            }
    }

    /**
     * Generates an AMRAP or EMOM set scheme for conditioning exercises.
     *
     * @param exercise The exercise to generate a scheme for
     * @param oneRepMaxes List of user's one rep max values
     * @param userId The user ID for weight unit preferences
     * @return Mono containing list of set scheme parameters
     */
    fun generateAmrapOrEmomScheme(
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>,
        userId: Int
    ): Mono<List<SetSchemeParams>> {
        val isAmrap = Random.nextBoolean()

        return weightSelectionService.getTargetWeight(exercise.name, 0.5, oneRepMaxes, userId, isDynamicEffort = false)
            .map { result ->
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

                listOf(
                    SetSchemeParams(
                        // TODO Don't hardcode the set number
                        setNumber = 3,
                        isAmrap = isAmrap,
                        isEmom = !isAmrap,
                        useTempo = useTempo,
                        eccentricTempo = eccentric,
                        isometricTempo = isometric,
                        concentricTempo = concentric,
                        targetWeight = result.targetWeight,
                        performedWeight = null,
                        // Varies per person for AMRAP/EMOM
                        targetRepCount = null,
                        performedRepCount = null,
                        // You always rest between sets, so we don't need to check if it's AMRAP or EMOM
                        restSeconds = 60,
                        band = result.band,
                    )
                )
            }
    }
}
