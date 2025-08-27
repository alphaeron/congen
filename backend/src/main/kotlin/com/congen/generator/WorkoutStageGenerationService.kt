package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.SetSchemeService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.random.Random

/**
 * Base service for generating workout stages for conjugate powerlifting programs.
 *
 * This service provides common functionality for creating workout stages across
 * different program types (2-day, 3-day, 4-day). It handles the creation of
 * primary, secondary, accessory, and conditioning stages with proper exercise
 * selection and set scheme generation.
 *
 * ## Stage Types
 *
 * - **Primary**: Main compound movements (ME or DE)
 * - **Secondary**: Supporting compound movements
 * - **Accessory**: Isolation and weak point training
 * - **Conditioning**: Cardio and recovery work (for DE days)
 *
 * ## Common Patterns
 *
 * This service implements common patterns for:
 * - Exercise selection
 * - Set scheme generation using Prilepin guidelines
 * - Time-based accessory exercise allocation
 * - Stage creation and exercise programming
 *
 * @property workoutStageDAL Data access layer for workout stage operations
 * @property workoutStageTypeDAL Data access layer for workout stage type operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property setSchemeDAL Data access layer for set scheme operations
 * @property setSchemeService Service for set scheme operations
 * @property prilepinGuidelinesService Service for Prilepin-based guidelines
 * @property weightSelectionService Service for conjugate-specific weight selection
 * @property userWeightUnitPreferenceDAL Data access layer for user weight unit preferences
 * @property exerciseSelectionService Service for exercise selection logic
 * @property movementBalanceService Service for movement balance
 * @property sessionTimeCalculator Service for session time calculations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
abstract class WorkoutStageGenerationService(
    protected val workoutStageDAL: WorkoutStageDAL,
    protected val workoutStageTypeDAL: WorkoutStageTypeDAL,
    protected val programmedExerciseDAL: ProgrammedExerciseDAL,
    protected val setSchemeDAL: SetSchemeDAL,
    protected val setSchemeService: SetSchemeService,
    protected val prilepinGuidelinesService: PrilepinGuidelinesService,
    protected val weightSelectionService: WeightSelectionService,
    protected val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    protected val exerciseSelectionService: ExerciseSelectionService,
    protected val movementBalanceService: MovementBalanceService,
    protected val sessionTimeCalculator: SessionTimeCalculator,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(WorkoutStageGenerationService::class.java)
    }

    /**
     * Generates workout stages for a specific workout.
     *
     * This method orchestrates the creation of all workout stages based on the
     * specific program type implementation.
     *
     * @param workout The programmed workout to generate stages for
     * @param dayType The type of workout day
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param programPreferences User's program preferences
     * @param weakMuscles Target weak muscles
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono<Void> indicating completion
     */
    fun generateWorkoutStages(
        workout: ProgrammedWorkout,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<Void> {
        return generateStagesForDayType(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        ).doOnError { error ->
            logger.error("Error generating workout stages for workout '{}': {}", workout.id, error.message)
        }
    }

    /**
     * Generates stages for a specific day type.
     *
     * This method must be implemented by subclasses to provide specific
     * stage generation logic for different program types.
     *
     * @param workout The programmed workout
     * @param dayType The type of workout day
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param programPreferences User's program preferences
     * @param weakMuscles Target weak muscles
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono<Void> indicating completion
     */
    protected abstract fun generateStagesForDayType(
        workout: ProgrammedWorkout,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<Void>

    /**
     * Creates a primary stage with the given exercise.
     *
     * @param workout The programmed workout
     * @param exercise The primary exercise
     * @param setSchemes The set schemes for the exercise
     * @return Mono<Void> indicating completion
     */
    protected fun createPrimaryStage(
        workout: ProgrammedWorkout,
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>,
    ): Mono<Void> {
        return createWorkoutStage(
            workout.id,
            WorkoutStageTypeEnum.PRIMARY,
            WorkoutStageTypeEnum.PRIMARY.position
        )
            .doOnError { error ->
                logger.error("Error creating primary stage: {}", error.message)
            }
            .flatMap { primaryStage ->
                createProgrammedExercise(primaryStage.id, exercise.name)
                    .flatMap { primaryProgrammedExercise ->
                        createSetSchemes(
                            primaryProgrammedExercise.id,
                            setSchemes,
                            WeightUnit.KG
                        )
                    }
            }
            .then()
    }

    /**
     * Creates a secondary stage with the given exercise.
     *
     * @param workout The programmed workout
     * @param exercise The secondary exercise
     * @param setSchemes The set schemes for the exercise
     * @return Mono<Void> indicating completion
     */
    protected fun createSecondaryStage(
        workout: ProgrammedWorkout,
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>,
    ): Mono<Void> {
        return createWorkoutStage(
            workout.id,
            WorkoutStageTypeEnum.SECONDARY,
            WorkoutStageTypeEnum.SECONDARY.position
        )
            .flatMap { secondaryStage ->
                createProgrammedExercise(secondaryStage.id, exercise.name)
                    .flatMap { secondaryProgrammedExercise ->
                        createSetSchemes(
                            secondaryProgrammedExercise.id,
                            setSchemes,
                            WeightUnit.KG
                        )
                    }
            }
            .then()
    }

    /**
     * Creates a primary stage with multiple exercises (for combined ME+DE days).
     *
     * @param workout The programmed workout
     * @param primaryExercise The primary exercise (can be null)
     * @param secondaryExercise The secondary exercise (can be null)
     * @param primarySetSchemes The set schemes for the primary exercise
     * @param secondarySetSchemes The set schemes for the secondary exercise
     * @return Mono<Void> indicating completion
     */
    protected fun createCombinedPrimaryStage(
        workout: ProgrammedWorkout,
        primaryExercise: Exercise?,
        secondaryExercise: Exercise?,
        primarySetSchemes: List<SetSchemeParams>,
        secondarySetSchemes: List<SetSchemeParams>,
    ): Mono<Void> {
        if (primaryExercise == null && secondaryExercise == null) {
            return Mono.empty()
        }

        return createWorkoutStage(
            workout.id,
            WorkoutStageTypeEnum.PRIMARY,
            WorkoutStageTypeEnum.PRIMARY.position
        )
            .doOnError { error ->
                logger.error("Error creating combined primary stage: {}", error.message)
            }
            .flatMap { primaryStage ->
                var exerciseMono: Mono<Void> = Mono.empty()

                if (primaryExercise != null) {
                    exerciseMono =
                        exerciseMono.then(
                            createProgrammedExercise(primaryStage.id, primaryExercise.name)
                                .flatMap { primaryProgrammedExercise ->
                                    createSetSchemes(
                                        primaryProgrammedExercise.id,
                                        primarySetSchemes,
                                        WeightUnit.KG
                                    )
                                }
                        )
                }

                if (secondaryExercise != null) {
                    exerciseMono =
                        exerciseMono.then(
                            createProgrammedExercise(primaryStage.id, secondaryExercise.name)
                                .flatMap { secondaryProgrammedExercise ->
                                    createSetSchemes(
                                        secondaryProgrammedExercise.id,
                                        secondarySetSchemes,
                                        WeightUnit.KG
                                    )
                                }
                        )
                }

                exerciseMono
            }
            .then()
    }

    /**
     * Creates an accessory stage with multiple exercises.
     *
     * @param workout The programmed workout
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param weakMuscles Target weak muscles
     * @param numAccessoryExercises Number of accessory exercises to create
     * @param userId User ID
     * @param currentWeekNumber Current week number
     * @param movementBalanceState Current movement balance state
     * @return Mono<Void> indicating completion
     */
    protected fun createAccessoryStage(
        workout: ProgrammedWorkout,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        weakMuscles: List<String>,
        numAccessoryExercises: Int,
        userId: String,
        currentWeekNumber: Int,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Void> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (numAccessoryExercises <= 0) {
            return Mono.empty()
        }

        return createWorkoutStage(
            workout.id,
            WorkoutStageTypeEnum.ACCESSORY,
            WorkoutStageTypeEnum.ACCESSORY.position
        )
            .flatMap { accessoryStage ->
                // Generate a consistent rest time for all accessory exercises
                val guidelines =
                    prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                        dayType = dayType,
                        currentWeekNumber = currentWeekNumber,
                        movementRole = "accessory"
                    ).first
                val consistentRestSeconds = prilepinGuidelinesService.getRandomRestTime(guidelines.restSeconds)

                Flux.range(1, numAccessoryExercises)
                    .concatMap {
                        selectAccessoryExercise(
                            userExercisePool = userExercisePool,
                            weakMuscles = weakMuscles,
                            workoutType = workoutType,
                            dayType = dayType,
                            movementBalanceState = movementBalanceState
                        ).flatMap { accessoryExercise ->
                            createProgrammedExercise(
                                accessoryStage.id,
                                accessoryExercise.name
                            )
                                .flatMap { accessoryProgrammedExercise ->
                                    generateAccessorySchemeWithConsistentRest(
                                        exercise = accessoryExercise,
                                        dayType = dayType,
                                        oneRepMaxes = oneRepMaxes,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId,
                                        consistentRestSeconds = consistentRestSeconds
                                    ).flatMap { accessoryScheme ->
                                        createSetSchemes(
                                            accessoryProgrammedExercise.id,
                                            accessoryScheme,
                                            WeightUnit.KG
                                        )
                                    }
                                }
                        }.onErrorResume { error ->
                            logger.error("Failed to create accessory exercise for stage. Error: {}", error.message)
                            Mono.empty()
                        }
                    }
                    .then()
            }
            .then()
    }

    /**
     * Creates a conditioning stage if applicable.
     *
     * @param workout The programmed workout
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param weakMuscles Target weak muscles
     * @param userId User ID
     * @param movementBalanceState Current movement balance state
     * @return Mono<Void> indicating completion
     */
    protected fun createConditioningStage(
        workout: ProgrammedWorkout,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        weakMuscles: List<String>,
        userId: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Void> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (!hasConditioning(dayType)) {
            return Mono.empty()
        }

        return createWorkoutStage(
            workout.id,
            WorkoutStageTypeEnum.CONDITIONING,
            WorkoutStageTypeEnum.CONDITIONING.position
        )
            .flatMap { conditioningStage ->
                selectConditioningExercise(
                    userExercisePool = userExercisePool,
                    weakMuscles = weakMuscles,
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = movementBalanceState
                ).flatMap { conditioningExercise ->
                    createProgrammedExercise(
                        conditioningStage.id,
                        conditioningExercise.name
                    )
                        .flatMap { conditioningProgrammedExercise ->
                            generateAmrapOrEmomScheme(
                                exercise = conditioningExercise,
                                oneRepMaxes = oneRepMaxes,
                                userId = userId
                            ).flatMap { conditioningScheme ->
                                createSetSchemes(
                                    conditioningProgrammedExercise.id,
                                    conditioningScheme,
                                    WeightUnit.KG
                                )
                            }
                        }
                }.onErrorResume { error ->
                    logger.error("Failed to create conditioning exercise for stage. Error: {}", error.message)
                    Mono.empty()
                }
            }
            .then()
    }

    /**
     * Creates a warmup stage with multiple exercises.
     *
     * @param workout The programmed workout
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param primaryExercise The primary exercise for the day (if available)
     * @param isFourDayTemplate Whether this is a 4-day template
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono<Void> indicating completion
     */
    protected fun createWarmupStage(
        workout: ProgrammedWorkout,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        primaryExercise: Exercise?,
        isFourDayTemplate: Boolean,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<Void> {
        // Determine workout type based on day template
        val workoutType =
            when {
                dayType.contains("_DE_") -> "dynamic_effort"
                dayType.startsWith("ME_") -> "maximal_effort"
                dayType.startsWith("DE_") -> "dynamic_effort"
                else -> "maximal_effort"
            }

        return exerciseSelectionService.selectWarmupExercises(
            userExercisePool = userExercisePool,
            primaryExercise = primaryExercise,
            isFourDayTemplate = isFourDayTemplate,
            dayType = dayType,
            workoutType = workoutType
        )
            .flatMap { warmupExercises ->
                if (warmupExercises.isEmpty()) {
                    return@flatMap Mono.empty()
                }

                createWorkoutStage(
                    workout.id,
                    WorkoutStageTypeEnum.WARMUP,
                    WorkoutStageTypeEnum.WARMUP.position
                )
                    .flatMap { warmupStage ->
                        Flux.fromIterable(warmupExercises)
                            .concatMap { warmupExercise ->
                                createProgrammedExercise(
                                    warmupStage.id,
                                    warmupExercise.name
                                )
                                    .flatMap { warmupProgrammedExercise ->
                                        // Generate simple warmup set schemes (light weight, higher reps)
                                        generateWarmupSetSchemes(
                                            exercise = warmupExercise,
                                            dayType = dayType,
                                            oneRepMaxes = oneRepMaxes,
                                            userId = userId,
                                            currentWeekNumber = currentWeekNumber
                                        ).flatMap { warmupScheme ->
                                            createSetSchemes(
                                                warmupProgrammedExercise.id,
                                                warmupScheme,
                                                WeightUnit.KG
                                            )
                                        }
                                    }
                            }
                            .then()
                    }
            }
            .then()
    }

    /**
     * Generates warmup set schemes for exercises.
     *
     * Warmup exercises typically use light weight and higher reps to prepare the muscles.
     *
     * @param exercise The exercise to generate schemes for
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param userId User ID
     * @param currentWeekNumber Current week number
     * @return Mono containing list of set scheme parameters
     */
    protected fun generateWarmupSetSchemes(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        userId: String,
        currentWeekNumber: Int
    ): Mono<List<SetSchemeParams>> {
        // Warmup exercises: 4 sets of 25 reps at appropriate proportion of weight
        val numSets = 4
        val repsPerSet = 25
        val restSeconds = 60 // Shorter rest for warmup

        return weightSelectionService.getTargetWeight(
            exerciseName = exercise.name,
            // 30% of 1RM for warmup
            intensity = 0.3,
            oneRepMaxes = oneRepMaxes,
            userId = userId,
            isDynamicEffort = dayType.contains("DE"),
            currentWeekNumber = currentWeekNumber
        )
            .map { result ->
                (1..numSets).map { setNumber ->
                    SetSchemeParams(
                        setNumber = setNumber,
                        isAmrap = false,
                        isEmom = false,
                        useTempo = false,
                        eccentricTempo = "0",
                        isometricTempo = "0",
                        concentricTempo = "0",
                        targetWeight = result.targetWeight,
                        performedWeight = null,
                        targetRepCount = repsPerSet,
                        performedRepCount = null,
                        restSeconds = restSeconds,
                        band = null,
                    )
                }
            }
    }

    /**
     * Determines if a day type includes conditioning.
     *
     * @param dayType The type of workout day
     * @return true if the day includes conditioning, false otherwise
     */
    protected fun hasConditioning(dayType: String): Boolean {
        return dayType.contains("DE")
    }

    /**
     * Calculates the number of accessory exercises based on session time and set schemes.
     *
     * @param sessionTimeMinutes The desired session time in minutes
     * @param primarySetSchemes List of set scheme parameters for the primary movement
     * @param secondarySetSchemes List of set scheme parameters for the secondary movement
     * @param dayType The type of workout day
     * @return The number of accessory exercises to include
     */
    protected fun calculateNumAccessoryExercises(
        sessionTimeMinutes: Int,
        primarySetSchemes: List<SetSchemeParams>,
        secondarySetSchemes: List<SetSchemeParams>,
        dayType: String
    ): Int {
        return sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
            sessionTimeMinutes = sessionTimeMinutes,
            primarySetSchemes = primarySetSchemes,
            secondarySetSchemes = secondarySetSchemes,
            dayType = dayType
        )
    }

    /**
     * Selects a primary exercise using the UserExercisePool.
     * This method delegates to ExerciseSelectionService to ensure proper exercise selection and pool management.
     *
     * @param userExercisePool The user's exercise pool
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param weakMuscles Target weak muscles
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectPrimaryExercise(
        userExercisePool: UserExercisePool,
        workoutType: String,
        weakMuscles: List<String>,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = false,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        )
    }

    /**
     * Selects a secondary exercise using the UserExercisePool.
     * This method delegates to ExerciseSelectionService to ensure proper exercise selection and pool management.
     *
     * @param userExercisePool The user's exercise pool
     * @param primaryExercise The primary exercise to base selection on
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectSecondaryExercise(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectSimilarSecondaryExercise(
            primaryExercise = primaryExercise,
            userExercisePool = userExercisePool,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        ).filter { selectedExercise ->
            // Filter out exercises that are the same as the primary
            selectedExercise.name != primaryExercise.name
        }.onErrorResume { error ->
            logger.error("Failed to select secondary exercise for primary exercise: {}. Error: {}", primaryExercise.name, error.message)
            Mono.empty()
        }
    }

    /**
     * Selects an accessory exercise using the UserExercisePool.
     * This method delegates to ExerciseSelectionService to ensure proper exercise selection and pool management.
     *
     * @param userExercisePool The user's exercise pool
     * @param weakMuscles Target weak muscles
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectAccessoryExercise(
        userExercisePool: UserExercisePool,
        weakMuscles: List<String>,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = true,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        )
    }

    /**
     * Selects a conditioning exercise using the UserExercisePool.
     * This method delegates to ExerciseSelectionService to ensure proper exercise selection and pool management.
     *
     * @param userExercisePool The user's exercise pool
     * @param weakMuscles Target weak muscles
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectConditioningExercise(
        userExercisePool: UserExercisePool,
        weakMuscles: List<String>,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = true,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        )
    }

    /**
     * Generates set schemes for an exercise.
     *
     * @param exercise The exercise to generate schemes for
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono containing list of set scheme parameters
     */
    protected fun generateSetSchemes(
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<List<SetSchemeParams>> {
        return generatePrilepinBasedScheme(
            exercise = exercise,
            movementRole = movementRole,
            dayType = dayType,
            oneRepMaxes = oneRepMaxes,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )
    }

    /**
     * Generates Prilepin-based set schemes for exercises.
     *
     * @param exercise The exercise to generate schemes for
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono containing the generated set schemes
     */
    protected fun generatePrilepinBasedScheme(
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<List<SetSchemeParams>> {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber,
                movementRole = movementRole
            )

        val (repsPerSet, numSets) =
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = guidelines,
                intensity = intensity
            )
        val actualTotalReps = numSets * repsPerSet
        val restSeconds =
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = guidelines.restSeconds,
                intensity = intensity,
                totalReps = actualTotalReps,
                totalRepsRange = guidelines.totalRepsRange
            )

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
     * Generates secondary exercise set schemes.
     *
     * @param exercise The exercise to generate schemes for
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono containing the generated set schemes
     */
    protected fun generateSecondaryExerciseScheme(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<List<SetSchemeParams>> {
        return generatePrilepinBasedScheme(
            exercise = exercise,
            movementRole = "secondary",
            dayType = dayType,
            oneRepMaxes = oneRepMaxes,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )
    }

    /**
     * Generates AMRAP or EMOM set schemes for conditioning exercises.
     *
     * @param exercise The exercise to generate schemes for
     * @param oneRepMaxes User's one rep max values
     * @param userId User ID
     * @return Mono containing the generated set schemes
     */
    protected fun generateAmrapOrEmomScheme(
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>,
        userId: String,
    ): Mono<List<SetSchemeParams>> {
        // Conditioning exercises: 3-5 sets, 10-15 reps, 60-90 seconds rest
        val numSets = (3..5).random()
        val repsPerSet = (10..15).random()
        val restSeconds = prilepinGuidelinesService.getRandomRestTime(60..90)

        // Use 50-60% intensity for conditioning
        val intensity = Random.nextDouble(0.5, 0.6)

        return weightSelectionService.getTargetWeight(
            exercise.name,
            intensity,
            oneRepMaxes,
            userId,
            isDynamicEffort = false
        )
            .map { result ->
                val isAmrap = Random.nextBoolean() // 50% chance of AMRAP
                // 50% chance of EMOM if not AMRAP
                val isEmom = !isAmrap && Random.nextBoolean()

                (1..numSets).map { setNumber ->
                    SetSchemeParams(
                        setNumber = setNumber,
                        isAmrap = isAmrap,
                        isEmom = isEmom,
                        // No tempo for conditioning
                        useTempo = false,
                        eccentricTempo = "0",
                        isometricTempo = "0",
                        concentricTempo = "0",
                        targetWeight = result.targetWeight,
                        performedWeight = null,
                        targetRepCount = repsPerSet,
                        performedRepCount = null,
                        restSeconds = restSeconds,
                        // No bands for conditioning
                        band = null,
                    )
                }
            }
    }

    /**
     * Generates accessory set schemes with a consistent rest time.
     *
     * @param exercise The accessory exercise to generate schemes for
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @param consistentRestSeconds The consistent rest time in seconds
     * @return Mono containing the generated set schemes
     */
    protected fun generateAccessorySchemeWithConsistentRest(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        userId: String,
        consistentRestSeconds: Int
    ): Mono<List<SetSchemeParams>> {
        val undulatingPeriodizationGuidelines =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber,
                movementRole = "accessory"
            )
        val intensity = undulatingPeriodizationGuidelines.second

        // For accessories, use "good" rep numbers (6, 8, 10, 12, 15) instead of random ranges
        val goodRepNumbers = listOf(6, 8, 10, 12, 15)
        val repsPerSet = goodRepNumbers.random()
        // For accessories, use a fixed 3-4 set range instead of calculating from total reps
        val numSets = (3..4).random()

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
                val useTempo = Random.nextBoolean() // Random tempo for accessory exercises
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
                        restSeconds = consistentRestSeconds,
                        band = result.band,
                    )
                }
            }
    }

    /**
     * Creates a workout stage using the infrastructure layer.
     *
     * @param workoutId The workout ID
     * @param stageType The stage type
     * @param position The stage position
     * @return Mono containing the created workout stage
     */
    protected fun createWorkoutStage(
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
     * Creates a programmed exercise.
     *
     * @param stageId The stage ID
     * @param exerciseName The exercise name
     * @return Mono containing the created programmed exercise
     */
    protected fun createProgrammedExercise(
        stageId: Long,
        exerciseName: String
    ): Mono<ProgrammedExercise> {
        return programmedExerciseDAL.insertProgrammedExercise(
            workoutStageId = stageId,
            exerciseName = exerciseName,
            position = 1,
            notes = null
        )
    }

    /**
     * Creates set schemes for a programmed exercise.
     *
     * @param programmedExerciseId The programmed exercise ID
     * @param setSchemes The set schemes to create
     * @param weightUnit The weight unit
     * @return Mono<Void> indicating completion
     */
    protected fun createSetSchemes(
        programmedExerciseId: Long,
        setSchemes: List<SetSchemeParams>,
        weightUnit: WeightUnit
    ): Mono<Void> {
        return Flux.fromIterable(setSchemes)
            .concatMap { scheme ->
                setSchemeService.insertSetScheme(
                    programmedExerciseId = programmedExerciseId,
                    setNumber = scheme.setNumber,
                    isAmrap = scheme.isAmrap,
                    isEmom = scheme.isEmom,
                    useTempo = scheme.useTempo,
                    eccentricTempo = scheme.eccentricTempo,
                    isometricTempo = scheme.isometricTempo,
                    concentricTempo = scheme.concentricTempo,
                    targetWeight = scheme.targetWeight?.toString(),
                    performedWeight = scheme.performedWeight?.toString(),
                    targetRepCount = scheme.targetRepCount,
                    performedRepCount = scheme.performedRepCount,
                    restSeconds = scheme.restSeconds,
                    unit = weightUnit.name,
                    band = scheme.band
                )
            }
            .then()
    }

    /**
     * Creates workout stages sequentially using the provided stage creation functions.
     *
     * @param stageCreators List of stage creation functions to execute sequentially
     * @return Mono<Void> indicating completion
     */
    protected fun createStagesSequentially(stageCreators: List<() -> Mono<Void>>): Mono<Void> {
        var currentMono: Mono<Void> = Mono.empty()

        for (stageCreator in stageCreators) {
            currentMono = currentMono.then(stageCreator())
        }

        return currentMono
    }

    /**
     * Updates movement balance state with a new exercise.
     *
     * @param currentState Current movement balance state
     * @param exercise The exercise to add
     * @param isAccessory Whether this is an accessory exercise
     * @return Updated movement balance state
     */
    protected fun updateMovementBalanceState(
        currentState: MovementBalanceService.MovementBalanceState,
        exercise: Exercise,
        isAccessory: Boolean
    ): MovementBalanceService.MovementBalanceState {
        val estimatedVolume = movementBalanceService.estimateExerciseVolume(isAccessory)
        return currentState.addExercise(exercise, estimatedVolume)
    }

    /**
     * Creates an initial movement balance state for a workout.
     *
     * @return Initial movement balance state
     */
    protected fun createInitialMovementBalanceState(): MovementBalanceService.MovementBalanceState {
        return movementBalanceService.createInitialState()
    }

    /**
     * Logs the current movement balance state for debugging.
     *
     * @param state Current movement balance state
     * @param workoutName Name of the workout for logging context
     */
    protected fun logMovementBalanceState(
        state: MovementBalanceService.MovementBalanceState,
        workoutName: String
    ) {
        movementBalanceService.logBalanceState(state, workoutName)
    }
}
