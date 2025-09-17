package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ProgramPreferences
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.SetSchemeService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.random.Random
import org.jetbrains.annotations.VisibleForTesting

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
     * @return Mono containing the workout generation result
     */
    fun generateWorkoutStages(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: ProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<WorkoutGenerationResult> {
        return generateStagesForDayType(
            programId = programId,
            dayNumber = dayNumber,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        ).doOnError { error ->
            logger.error("Error generating workout stages for program {} day {}: {}", programId, dayNumber, error.message)
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
     * @return Mono containing the workout generation result
     */
    @VisibleForTesting
    abstract fun generateStagesForDayType(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: ProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<WorkoutGenerationResult>

    /**
     * Creates a primary stage with the given exercise.
     *
     * @param exercise The primary exercise
     * @param setSchemes The set schemes for the exercise
     * @param userId The user ID for weight unit preferences
     * @return Mono containing the workout stage data
     */
    protected fun createPrimaryStage(
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>,
        userId: String,
    ): Mono<WorkoutStageData> {
        val exerciseData = ProgrammedExerciseData(
            exerciseName = exercise.name,
            position = 1,
            notes = null,
            setSchemes = setSchemes
        )
        
        return Mono.just(
            WorkoutStageData(
                stageType = WorkoutStageTypeEnum.PRIMARY,
                position = WorkoutStageTypeEnum.PRIMARY.position,
                name = WorkoutStageTypeEnum.PRIMARY.displayName,
                exercises = listOf(exerciseData)
            )
        )
            .doOnError { error ->
                logger.error("Error creating primary stage: {}", error.message)
            }
    }

    /**
     * Creates a secondary stage with the given exercise.
     *
     * @param exercise The secondary exercise
     * @param setSchemes The set schemes for the exercise
     * @param userId The user ID for weight unit preferences
     * @return Mono containing the workout stage data
     */
    protected fun createSecondaryStage(
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>,
        userId: String,
    ): Mono<WorkoutStageData> {
        val exerciseData = ProgrammedExerciseData(
            exerciseName = exercise.name,
            position = 1,
            notes = null,
            setSchemes = setSchemes
        )
        
        return Mono.just(
            WorkoutStageData(
                stageType = WorkoutStageTypeEnum.SECONDARY,
                position = WorkoutStageTypeEnum.SECONDARY.position,
                name = WorkoutStageTypeEnum.SECONDARY.displayName,
                exercises = listOf(exerciseData)
            )
        )
            .doOnError { error ->
                logger.error("Error creating secondary stage: {}", error.message)
            }
    }

    /**
     * Creates a primary stage with multiple exercises (for combined ME+DE days).
     *
     * @param primaryExercise The primary exercise (can be null)
     * @param secondaryExercise The secondary exercise (can be null)
     * @param primarySetSchemes The set schemes for the primary exercise
     * @param secondarySetSchemes The set schemes for the secondary exercise
     * @param userId The user ID for weight unit preferences
     * @return Mono containing the workout stage data
     */
    protected fun createCombinedPrimaryStage(
        primaryExercise: Exercise?,
        secondaryExercise: Exercise?,
        primarySetSchemes: List<SetSchemeParams>,
        secondarySetSchemes: List<SetSchemeParams>,
        userId: String,
    ): Mono<WorkoutStageData> {
        if (primaryExercise == null && secondaryExercise == null) {
            return Mono.empty()
        }

        val primaryExerciseMono = if (primaryExercise != null) {
            Mono.just(
                ProgrammedExerciseData(
                    exerciseName = primaryExercise.name,
                    position = 1,
                    notes = null,
                    setSchemes = primarySetSchemes
                )
            )
        } else {
            Mono.empty()
        }

        val secondaryExerciseMono = if (secondaryExercise != null) {
            Mono.just(
                ProgrammedExerciseData(
                    exerciseName = secondaryExercise.name,
                    position = 2,
                    notes = null,
                    setSchemes = secondarySetSchemes
                )
            )
        } else {
            Mono.empty()
        }

        return Mono.zip(primaryExerciseMono, secondaryExerciseMono)
            .map { tuple ->
                val exercises = listOfNotNull(tuple.t1, tuple.t2)
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.PRIMARY,
                    position = WorkoutStageTypeEnum.PRIMARY.position,
                    name = WorkoutStageTypeEnum.PRIMARY.displayName,
                    exercises = exercises
                )
            }
            .switchIfEmpty(
                Mono.just(WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.PRIMARY,
                    position = WorkoutStageTypeEnum.PRIMARY.position,
                    name = WorkoutStageTypeEnum.PRIMARY.displayName,
                    exercises = emptyList()
                ))
            )
            .doOnError { error ->
                logger.error("Error creating combined primary stage: {}", error.message)
            }
    }

    /**
     * Creates an accessory stage with multiple exercises.
     *
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param weakMuscles Target weak muscles
     * @param numAccessoryExercises Number of accessory exercises to create
     * @param userId User ID
     * @param currentWeekNumber Current week number
     * @param movementBalanceState Current movement balance state
     * @return Mono containing the workout stage data
     */
    protected fun createAccessoryStage(
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        weakMuscles: List<String>,
        numAccessoryExercises: Int,
        userId: String,
        currentWeekNumber: Int,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<WorkoutStageData> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (numAccessoryExercises <= 0) {
            return Mono.empty()
        }

        // Generate a consistent rest time for all accessory exercises
        val guidelines =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber,
                movementRole = "accessory"
            ).first
        val consistentRestSeconds = prilepinGuidelinesService.getRandomRestTime(guidelines.restSeconds)

        return Flux.range(1, numAccessoryExercises)
            .concatMap {
                selectAccessoryExercise(
                    userExercisePool = userExercisePool,
                    weakMuscles = weakMuscles,
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = movementBalanceState
                ).flatMap { accessoryExercise ->
                    generateAccessorySchemeWithConsistentRest(
                        exercise = accessoryExercise,
                        dayType = dayType,
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId,
                        consistentRestSeconds = consistentRestSeconds
                    ).flatMap { accessoryScheme ->
                        getWeightUnitForExercise(userId, accessoryExercise.name)
                            .map { weightUnit ->
                                val setSchemeData = accessoryScheme
                                
                                ProgrammedExerciseData(
                                    exerciseName = accessoryExercise.name,
                                    position = it,
                                    notes = null,
                                    setSchemes = setSchemeData
                                )
                            }
                    }
                }.onErrorResume { error ->
                    logger.error("Failed to create accessory exercise for stage. Error: {}", error.message)
                    Mono.empty<ProgrammedExerciseData>()
                }
            }
            .collectList()
            .map { exercises ->
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.ACCESSORY,
                    position = WorkoutStageTypeEnum.ACCESSORY.position,
                    name = WorkoutStageTypeEnum.ACCESSORY.displayName,
                    exercises = exercises
                )
            }
    }

    /**
     * Creates a conditioning stage if applicable.
     *
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param weakMuscles Target weak muscles
     * @param userId User ID
     * @param movementBalanceState Current movement balance state
     * @return Mono containing the workout stage data
     */
    protected fun createConditioningStage(
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        weakMuscles: List<String>,
        userId: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<WorkoutStageData> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (!hasConditioning(dayType)) {
            return Mono.empty()
        }

        return selectConditioningExercise(
            userExercisePool = userExercisePool,
            weakMuscles = weakMuscles,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        ).flatMap { conditioningExercise ->
            generateAmrapOrEmomScheme(
                exercise = conditioningExercise,
                oneRepMaxes = oneRepMaxes,
                userId = userId
            ).flatMap { conditioningScheme ->
                getWeightUnitForExercise(userId, conditioningExercise.name)
                    .map { weightUnit ->
                        val setSchemeData = conditioningScheme
                        
                        val exerciseData = ProgrammedExerciseData(
                            exerciseName = conditioningExercise.name,
                            position = 1,
                            notes = null,
                            setSchemes = setSchemeData
                        )
                        
                        WorkoutStageData(
                            stageType = WorkoutStageTypeEnum.CONDITIONING,
                            position = WorkoutStageTypeEnum.CONDITIONING.position,
                            name = WorkoutStageTypeEnum.CONDITIONING.displayName,
                            exercises = listOf(exerciseData)
                        )
                    }
            }
        }.onErrorResume { error ->
            logger.error("Failed to create conditioning exercise for stage. Error: {}", error.message)
            Mono.empty()
        }
    }

    /**
     * Creates a warmup stage with multiple exercises.
     *
     * @param userExercisePool Pool of available exercises for the user
     * @param oneRepMaxes User's one rep max values
     * @param dayType The type of workout day
     * @param primaryExercise The primary exercise for the day (if available)
     * @param secondaryExercise The secondary exercise for the day (if available, for 2 and 3 day templates)
     * @param isFourDayTemplate Whether this is a 4-day template
     * @param currentWeekNumber Current week number
     * @param userId User ID
     * @return Mono containing the workout stage data
     */
    protected fun createWarmupStage(
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        dayType: String,
        primaryExercise: Exercise?,
        secondaryExercise: Exercise? = null,
        isFourDayTemplate: Boolean,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<WorkoutStageData> {
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
            secondaryExercise = secondaryExercise,
            isFourDayTemplate = isFourDayTemplate,
            dayType = dayType,
            workoutType = workoutType
        )
            .flatMap { warmupExercises ->
                if (warmupExercises.isEmpty()) {
                    return@flatMap Mono.empty()
                }

                Flux.fromIterable(warmupExercises)
                    .concatMap { warmupExercise ->
                        // Generate simple warmup set schemes (light weight, higher reps)
                        generateWarmupSetSchemes(
                            exercise = warmupExercise,
                            dayType = dayType,
                            oneRepMaxes = oneRepMaxes,
                            userId = userId,
                            currentWeekNumber = currentWeekNumber
                        ).flatMap { warmupScheme ->
                            getWeightUnitForExercise(userId, warmupExercise.name)
                                .map { weightUnit ->
                                    val setSchemeData = warmupScheme
                                    
                                    ProgrammedExerciseData(
                                        exerciseName = warmupExercise.name,
                                        position = warmupExercises.indexOf(warmupExercise) + 1,
                                        notes = null,
                                        setSchemes = setSchemeData
                                    )
                                }
                        }
                    }
                    .collectList()
                    .map { exercises ->
                        WorkoutStageData(
                            stageType = WorkoutStageTypeEnum.WARMUP,
                            position = WorkoutStageTypeEnum.WARMUP.position,
                            name = WorkoutStageTypeEnum.WARMUP.displayName,
                            exercises = exercises
                        )
                    }
            }
            .onErrorResume { error ->
                logger.error("Failed to create warmup stage for dayType '{}'. Error: {}", dayType, error.message)
                // Return empty Mono to continue with other stages instead of failing the entire workout generation
                Mono.empty()
            }
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
     * Primary exercises are not currently filtered by weak muscles, but accessory exercises are.
     *
     * @param userExercisePool The user's exercise pool
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param weakMuscles Target weak muscles (used for accessory exercises to target weak points)
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
            targetMuscles = emptyList(),
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
        // Filter weak muscles based on day type to ensure we only target appropriate muscles
        val dayTypeAwareWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(dayType)
                .filter { muscle -> weakMuscles.contains(muscle) }

        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = dayTypeAwareWeakMuscles,
            isAccessory = true,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState
        ).onErrorResume { error ->
            if (error.message?.contains("No exercises found for target muscles") == true ||
                error.message?.contains("No suitable exercise found") == true
            ) {
                logger.warn(
                    "No exercises found for target muscles: {} in dayType: {} for accessory exercise. " +
                        "Skipping this accessory exercise and continuing workout generation.",
                    dayTypeAwareWeakMuscles,
                    dayType
                )
                // Return empty Mono to skip this exercise, allowing workout generation to continue
                Mono.empty()
            } else {
                // Re-throw other errors as they indicate different issues
                Mono.error(error)
            }
        }
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
        // Filter weak muscles based on day type to ensure we only target appropriate muscles
        val dayTypeAwareWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(dayType)
                .filter { muscle -> weakMuscles.contains(muscle) }

        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = dayTypeAwareWeakMuscles,
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
                intensity = intensity,
                movementRole = movementRole
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
     * Creates workout stages sequentially using the provided stage creation functions.
     *
     * @param stageCreators List of stage creation functions to execute sequentially
     * @return Mono containing the list of workout stage data
     */
    protected fun createStagesSequentially(stageCreators: List<() -> Mono<WorkoutStageData>>): Mono<List<WorkoutStageData>> {
        return reactor.core.publisher.Flux.fromIterable(stageCreators)
            .concatMap { stageCreator ->
                stageCreator()
            }
            .collectList()
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

    /**
     * Gets the weight unit for an exercise based on user preferences.
     *
     * @param userId The user ID
     * @param exerciseName The name of the exercise
     * @return Mono containing the weight unit preference, defaulting to KG if no preference exists
     */
    private fun getWeightUnitForExercise(
        userId: String,
        exerciseName: String
    ): Mono<WeightUnit> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
            .map { it.preferredUnit }
            .switchIfEmpty(Mono.just(WeightUnit.KG))
            .onErrorResume { error ->
                logger.debug("No weight unit preference found for user {} and exercise {}, using KG", userId, exerciseName)
                Mono.just(WeightUnit.KG)
            }
    }
}
