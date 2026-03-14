package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseMuscle
import com.congen.model.UserOneRepMax
import com.congen.model.WorkoutStageTypeEnum
import org.jetbrains.annotations.VisibleForTesting
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
 * @property prilepinGuidelinesService Service for Prilepin-based guidelines
 * @property weightSelectionService Service for conjugate-specific weight selection
 * @property exerciseSelectionService Service for exercise selection logic
 * @property movementBalanceService Service for movement balance
 * @property sessionTimeCalculator Service for session time calculations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
abstract class WorkoutStageGenerationService(
    protected val prilepinGuidelinesService: PrilepinGuidelinesService,
    protected val weightSelectionService: WeightSelectionService,
    protected val exerciseSelectionService: ExerciseSelectionService,
    protected val movementBalanceService: MovementBalanceService,
    protected val sessionTimeCalculator: SessionTimeCalculator,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(WorkoutStageGenerationService::class.java)
    }

    /**
     * Generates workout stages for a specific workout using prepared data.
     *
     * This method orchestrates the creation of all workout stages based on the
     * specific program type implementation using pre-prepared data.
     *
     * @param programId The program ID
     * @param dayNumber The day number
     * @param dayType The type of workout day
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the workout generation result
     */
    fun generateWorkoutStages(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<WorkoutGenerationResult> {
        return generateStagesForDayType(
            programId = programId,
            dayNumber = dayNumber,
            dayType = dayType,
            preparedData = preparedData
        ).map { stages ->
            WorkoutGenerationResult(
                programId = programId,
                dayNumber = dayNumber,
                dayType = dayType,
                userId = preparedData.userId,
                stages = stages,
                preparedData = preparedData
            )
        }.doOnError { error ->
            logger.error("Error generating workout stages for program {} day {}: {}", programId, dayNumber, error.message)
        }
    }

    /**
     * Generates stages for a specific day type using prepared data.
     *
     * This method must be implemented by subclasses to provide specific
     * stage generation logic for different program types using pre-prepared data.
     *
     * @param programId The program ID
     * @param dayNumber The day number
     * @param dayType The type of workout day
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the list of workout stage data
     */
    @VisibleForTesting
    abstract fun generateStagesForDayType(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<List<WorkoutStageData>>

    /**
     * Creates a primary stage with the given exercise.
     *
     * @param exercise The primary exercise
     * @param setSchemes The set schemes for the exercise
     * @return Mono containing the workout stage data
     */
    protected fun createPrimaryStage(
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>
    ): Mono<WorkoutStageData> {
        val exerciseData =
            ProgrammedExerciseData(
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
     * @return Mono containing the workout stage data
     */
    protected fun createSecondaryStage(
        exercise: Exercise,
        setSchemes: List<SetSchemeParams>
    ): Mono<WorkoutStageData> {
        val exerciseData =
            ProgrammedExerciseData(
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
     * @return Mono containing the workout stage data
     */
    protected fun createCombinedPrimaryStage(
        primaryExercise: Exercise?,
        secondaryExercise: Exercise?,
        primarySetSchemes: List<SetSchemeParams>,
        secondarySetSchemes: List<SetSchemeParams>
    ): Mono<WorkoutStageData> {
        if (primaryExercise == null && secondaryExercise == null) {
            logger.warn("Both primary and secondary exercises are null for combined primary stage")
            return Mono.just(
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.PRIMARY,
                    position = WorkoutStageTypeEnum.PRIMARY.position,
                    name = WorkoutStageTypeEnum.PRIMARY.displayName,
                    exercises = emptyList()
                )
            )
        }

        val primaryExerciseMono =
            if (primaryExercise != null) {
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

        val secondaryExerciseMono =
            if (secondaryExercise != null) {
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
                Mono.just(
                    WorkoutStageData(
                        stageType = WorkoutStageTypeEnum.PRIMARY,
                        position = WorkoutStageTypeEnum.PRIMARY.position,
                        name = WorkoutStageTypeEnum.PRIMARY.displayName,
                        exercises = emptyList()
                    )
                )
            )
            .doOnError { error ->
                logger.error("Error creating combined primary stage: {}", error.message)
            }
    }

    /**
     * Creates an accessory stage with multiple exercises using prepared data.
     *
     * @param preparedData The prepared data containing all required information
     * @param dayType The type of workout day
     * @param numAccessoryExercises Number of accessory exercises to create
     * @param movementBalanceState Current movement balance state
     * @return Mono containing the workout stage data
     */
    protected fun createAccessoryStage(
        preparedData: WorkoutGenerationPreparedData,
        dayType: String,
        numAccessoryExercises: Int,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<WorkoutStageData> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (numAccessoryExercises <= 0) {
            logger.info("No accessory exercises requested for dayType: {}", dayType)
            return Mono.just(
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.ACCESSORY,
                    position = WorkoutStageTypeEnum.ACCESSORY.position,
                    name = WorkoutStageTypeEnum.ACCESSORY.displayName,
                    exercises = emptyList()
                )
            )
        }

        // Generate a consistent rest time for all accessory exercises
        val guidelines =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = preparedData.currentWeekNumber,
                movementRole = "accessory"
            ).first
        val consistentRestSeconds = prilepinGuidelinesService.getRandomRestTime(guidelines.restSeconds)

        val isMixedDay = dayType in listOf("ME_Upper_DE_Lower", "ME_Lower_DE_Upper")
        val (upperSlotDayType, lowerSlotDayType) =
            when (dayType) {
                "ME_Upper_DE_Lower" -> Pair("ME_Upper", "DE_Lower")
                "ME_Lower_DE_Upper" -> Pair("DE_Upper", "ME_Lower")
                else -> Pair("ME_Upper", "DE_Lower")
            }
        val upperWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(upperSlotDayType)
                .filter { muscle -> preparedData.weakMuscles.contains(muscle) }
        val lowerWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(lowerSlotDayType)
                .filter { muscle -> preparedData.weakMuscles.contains(muscle) }
        val dayTypeAwareWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(dayType)
                .filter { muscle -> preparedData.weakMuscles.contains(muscle) }

        return Flux.range(1, numAccessoryExercises)
            .concatMap { slotIndex ->
                val (targetMusclesForSlot, effectiveDayTypeForFiltering) =
                    if (isMixedDay) {
                        val isUpperSlot = (slotIndex % 2) == 1
                        if (isUpperSlot) {
                            val muscles =
                                if (upperWeakMuscles.isEmpty()) {
                                    preparedData.weakMuscles
                                } else {
                                    val muscleIndex = ((slotIndex - 1) / 2) % upperWeakMuscles.size
                                    listOf(upperWeakMuscles[muscleIndex])
                                }
                            Pair(muscles, upperSlotDayType)
                        } else {
                            val muscles =
                                if (lowerWeakMuscles.isEmpty()) {
                                    preparedData.weakMuscles
                                } else {
                                    val muscleIndex = ((slotIndex - 1) / 2) % lowerWeakMuscles.size
                                    listOf(lowerWeakMuscles[muscleIndex])
                                }
                            Pair(muscles, lowerSlotDayType)
                        }
                    } else {
                        val muscles =
                            if (dayTypeAwareWeakMuscles.isEmpty()) {
                                preparedData.weakMuscles
                            } else {
                                val muscleIndex = (slotIndex - 1) % dayTypeAwareWeakMuscles.size
                                listOf(dayTypeAwareWeakMuscles[muscleIndex])
                            }
                        Pair(muscles, null)
                    }
                selectAccessoryExercise(
                    userExercisePool = preparedData.userExercisePool,
                    weakMuscles = targetMusclesForSlot,
                    workoutType = workoutType,
                    dayType = dayType,
                    movementBalanceState = movementBalanceState,
                    exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                    currentWeekNumber = preparedData.currentWeekNumber,
                    effectiveDayTypeForFiltering = effectiveDayTypeForFiltering
                ).flatMap { accessoryExercise ->
                    generateAccessorySchemeWithConsistentRest(
                        exercise = accessoryExercise,
                        dayType = dayType,
                        oneRepMaxes = preparedData.oneRepMaxes,
                        currentWeekNumber = preparedData.currentWeekNumber,
                        consistentRestSeconds = consistentRestSeconds,
                        preparedData = preparedData
                    ).map { accessoryScheme ->
                        val setSchemeData = accessoryScheme

                        ProgrammedExerciseData(
                            exerciseName = accessoryExercise.name,
                            position = slotIndex,
                            notes = null,
                            setSchemes = setSchemeData
                        )
                    }
                }.onErrorResume { error ->
                    logger.error("Failed to create accessory exercise for stage. Error: {}", error.message)
                    // Return empty to skip this exercise and continue with others
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
            .switchIfEmpty(
                Mono.just(
                    WorkoutStageData(
                        stageType = WorkoutStageTypeEnum.ACCESSORY,
                        position = WorkoutStageTypeEnum.ACCESSORY.position,
                        name = WorkoutStageTypeEnum.ACCESSORY.displayName,
                        exercises = emptyList()
                    )
                )
            )
    }

    /**
     * Creates a conditioning stage if applicable using prepared data.
     *
     * @param preparedData The prepared data containing all required information
     * @param dayType The type of workout day
     * @param movementBalanceState Current movement balance state
     * @return Mono containing the workout stage data
     */
    protected fun createConditioningStage(
        preparedData: WorkoutGenerationPreparedData,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<WorkoutStageData> {
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        if (!hasConditioning(dayType)) {
            logger.info("No conditioning required for dayType: {}", dayType)
            return Mono.just(
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.CONDITIONING,
                    position = WorkoutStageTypeEnum.CONDITIONING.position,
                    name = WorkoutStageTypeEnum.CONDITIONING.displayName,
                    exercises = emptyList()
                )
            )
        }

        return selectConditioningExercise(
            userExercisePool = preparedData.userExercisePool,
            weakMuscles = preparedData.weakMuscles,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
            currentWeekNumber = preparedData.currentWeekNumber
        ).flatMap { conditioningExercise ->
            generateAmrapOrEmomScheme(
                exercise = conditioningExercise,
                oneRepMaxes = preparedData.oneRepMaxes,
                preparedData = preparedData
            ).map { conditioningScheme ->
                val setSchemeData = conditioningScheme

                val exerciseData =
                    ProgrammedExerciseData(
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
        }.onErrorResume { error ->
            logger.error("Failed to create conditioning exercise for stage. Error: {}", error.message)
            // Return empty conditioning stage to continue workout generation
            Mono.just(
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.CONDITIONING,
                    position = WorkoutStageTypeEnum.CONDITIONING.position,
                    name = WorkoutStageTypeEnum.CONDITIONING.displayName,
                    exercises = emptyList()
                )
            )
        }.switchIfEmpty(
            Mono.just(
                WorkoutStageData(
                    stageType = WorkoutStageTypeEnum.CONDITIONING,
                    position = WorkoutStageTypeEnum.CONDITIONING.position,
                    name = WorkoutStageTypeEnum.CONDITIONING.displayName,
                    exercises = emptyList()
                )
            )
        )
    }

    /**
     * Creates a warmup stage with multiple exercises using prepared data.
     *
     * @param preparedData The prepared data containing all required information
     * @param dayType The type of workout day
     * @param primaryExercise The primary exercise for the day (if available)
     * @param secondaryExercise The secondary exercise for the day (if available, for 2 and 3 day templates)
     * @param isFourDayTemplate Whether this is a 4-day template
     * @return Mono containing the workout stage data
     */
    protected fun createWarmupStage(
        preparedData: WorkoutGenerationPreparedData,
        dayType: String,
        primaryExercise: Exercise?,
        secondaryExercise: Exercise? = null,
        isFourDayTemplate: Boolean,
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
            userExercisePool = preparedData.userExercisePool,
            primaryExercise = primaryExercise,
            secondaryExercise = secondaryExercise,
            isFourDayTemplate = isFourDayTemplate,
            dayType = dayType,
            workoutType = workoutType,
            exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
            exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
            exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
            currentWeekNumber = preparedData.currentWeekNumber
        )
            .flatMap { warmupExercises ->
                if (warmupExercises.isEmpty()) {
                    logger.info("No warmup exercises found for dayType: {}", dayType)
                    return@flatMap Mono.just(
                        WorkoutStageData(
                            stageType = WorkoutStageTypeEnum.WARMUP,
                            position = WorkoutStageTypeEnum.WARMUP.position,
                            name = WorkoutStageTypeEnum.WARMUP.displayName,
                            exercises = emptyList()
                        )
                    )
                }

                Flux.fromIterable(warmupExercises)
                    .concatMap { warmupExercise ->
                        // Generate simple warmup set schemes (light weight, higher reps)
                        generateWarmupSetSchemes(
                            exercise = warmupExercise,
                            dayType = dayType,
                            oneRepMaxes = preparedData.oneRepMaxes,
                            currentWeekNumber = preparedData.currentWeekNumber,
                            preparedData = preparedData
                        ).map { warmupScheme ->
                            val setSchemeData = warmupScheme

                            ProgrammedExerciseData(
                                exerciseName = warmupExercise.name,
                                position = warmupExercises.indexOf(warmupExercise) + 1,
                                notes = null,
                                setSchemes = setSchemeData
                            )
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
                // Return empty warmup stage to continue with other stages instead of failing the entire workout generation
                Mono.just(
                    WorkoutStageData(
                        stageType = WorkoutStageTypeEnum.WARMUP,
                        position = WorkoutStageTypeEnum.WARMUP.position,
                        name = WorkoutStageTypeEnum.WARMUP.displayName,
                        exercises = emptyList()
                    )
                )
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
     * @param currentWeekNumber Current week number
     * @param preparedData The prepared data containing all required information
     * @return Mono containing list of set scheme parameters
     */
    protected fun generateWarmupSetSchemes(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData
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
            isDynamicEffort = dayType.contains("DE"),
            currentWeekNumber = currentWeekNumber,
            preparedData = preparedData
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
     * Equipment filtering is applied inside the pool (built with exerciseEquipmentMappings in the factory).
     *
     * Primary exercises are not currently filtered by weak muscles, but accessory exercises are.
     *
     * @param userExercisePool The user's exercise pool
     * @param workoutType The workout type (e.g., "maximal_effort", "dynamic_effort")
     * @param dayType The day type (e.g., "ME_Upper", "DE_Lower")
     * @param movementBalanceState Current movement balance state (optional)
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param currentWeekNumber Current program week for cycle index; used with preferredDeExerciseName for DE. Callers must always pass this.
     * @param preferredDeExerciseName DE exercise name from prepared data for 4-week cycle reuse; null means no preference
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectPrimaryExercise(
        userExercisePool: UserExercisePool,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        currentWeekNumber: Int,
        preferredDeExerciseName: String? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            isAccessory = false,
            workoutType = workoutType,
            dayType = dayType,
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            currentWeekNumber = currentWeekNumber,
            preferredDeExerciseName = preferredDeExerciseName
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
     * @param preparedData The prepared data containing exercise mappings
     * @param movementBalanceState Current movement balance state (optional)
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectSecondaryExercise(
        userExercisePool: UserExercisePool,
        primaryExercise: Exercise,
        workoutType: String,
        dayType: String,
        preparedData: WorkoutGenerationPreparedData,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null
    ): Mono<Exercise> {
        return exerciseSelectionService.selectSimilarSecondaryExercise(
            primaryExercise = primaryExercise,
            userExercisePool = userExercisePool,
            workoutType = workoutType,
            dayType = dayType,
            exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings
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
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param currentWeekNumber Current program week (1-based). Callers must always pass this.
     * @param effectiveDayTypeForFiltering When set (e.g. for mixed days), used for body-type filtering instead of dayType
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectAccessoryExercise(
        userExercisePool: UserExercisePool,
        weakMuscles: List<String>,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        currentWeekNumber: Int,
        effectiveDayTypeForFiltering: String? = null
    ): Mono<Exercise> {
        val dayTypeForFiltering = effectiveDayTypeForFiltering ?: dayType
        val dayTypeAwareWeakMuscles =
            ConjugateConstants.getWeakMusclesForDayType(dayTypeForFiltering)
                .filter { muscle -> weakMuscles.contains(muscle) }
        val musclesForSelection =
            if (dayTypeAwareWeakMuscles.isEmpty()) weakMuscles else dayTypeAwareWeakMuscles

        val allowBandedExercises =
            if (effectiveDayTypeForFiltering != null) dayType.contains("DE", ignoreCase = true) else null
        return exerciseSelectionService.selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = musclesForSelection,
            isAccessory = true,
            workoutType = workoutType,
            dayType = dayTypeForFiltering,
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            currentWeekNumber = currentWeekNumber,
            allowBandedExercises = allowBandedExercises
        ).onErrorResume { error ->
            if (error.message?.contains("No exercises found for target muscles") == true ||
                error.message?.contains("No suitable exercise found") == true
            ) {
                logger.warn(
                    "No exercises found for target muscles: {} in dayType: {} for accessory exercise. " +
                        "Skipping this accessory exercise and continuing workout generation.",
                    musclesForSelection,
                    dayTypeForFiltering
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
     * @param exerciseWorkoutTypeMappings Pre-computed mappings of exercise names to their workout types
     * @param exerciseMuscleMappings Pre-computed mappings of exercise names to their muscle targets
     * @param currentWeekNumber Current program week (1-based). Callers must always pass this.
     * @return Mono containing the selected exercise or null if none available
     */
    protected fun selectConditioningExercise(
        userExercisePool: UserExercisePool,
        weakMuscles: List<String>,
        workoutType: String,
        dayType: String,
        movementBalanceState: MovementBalanceService.MovementBalanceState? = null,
        exerciseWorkoutTypeMappings: Map<String, List<String>>,
        exerciseMuscleMappings: Map<String, List<ExerciseMuscle>>,
        currentWeekNumber: Int
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
            movementBalanceState = movementBalanceState,
            exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            exerciseMuscleMappings = exerciseMuscleMappings,
            currentWeekNumber = currentWeekNumber
        ).onErrorResume { error ->
            logger.error("Failed to select conditioning exercise for dayType: {}. Error: {}", dayType, error.message)
            // Return empty to indicate no conditioning exercise available
            Mono.empty()
        }
    }

    /**
     * Generates set schemes for an exercise.
     *
     * @param exercise The exercise to generate schemes for
     * @param movementRole The role of the movement (primary, secondary, accessory)
     * @param dayType The type of workout day
     * @param oneRepMaxes User's one rep max values
     * @param currentWeekNumber Current week number
     * @param preparedData The prepared data containing all required information
     * @return Mono containing list of set scheme parameters
     */
    protected fun generateSetSchemes(
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<List<SetSchemeParams>> {
        return generatePrilepinBasedScheme(
            exercise = exercise,
            movementRole = movementRole,
            dayType = dayType,
            oneRepMaxes = oneRepMaxes,
            currentWeekNumber = currentWeekNumber,
            preparedData = preparedData
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
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the generated set schemes
     */
    protected fun generatePrilepinBasedScheme(
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData,
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
            isDynamicEffort = isDynamicEffort,
            currentWeekNumber = currentWeekNumber,
            preparedData = preparedData
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
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the generated set schemes
     */
    protected fun generateSecondaryExerciseScheme(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<List<SetSchemeParams>> {
        return generatePrilepinBasedScheme(
            exercise = exercise,
            movementRole = "secondary",
            dayType = dayType,
            oneRepMaxes = oneRepMaxes,
            currentWeekNumber = currentWeekNumber,
            preparedData = preparedData
        )
    }

    /**
     * Generates AMRAP or EMOM set schemes for conditioning exercises.
     *
     * @param exercise The exercise to generate schemes for
     * @param oneRepMaxes User's one rep max values
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the generated set schemes
     */
    protected fun generateAmrapOrEmomScheme(
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>,
        preparedData: WorkoutGenerationPreparedData,
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
            isDynamicEffort = false,
            preparedData = preparedData
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
     * @param consistentRestSeconds The consistent rest time in seconds
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the generated set schemes
     */
    protected fun generateAccessorySchemeWithConsistentRest(
        exercise: Exercise,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int,
        consistentRestSeconds: Int,
        preparedData: WorkoutGenerationPreparedData
    ): Mono<List<SetSchemeParams>> {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber,
                movementRole = "accessory"
            )
        val weekInCycle = (currentWeekNumber % 4) + 1
        val (repsPerSet, numSets) =
            prilepinGuidelinesService.getAccessoryRepsAndSets(
                guidelines = guidelines,
                intensity = intensity,
                weekInCycle = weekInCycle
            )

        val isDynamicEffort = dayType.startsWith("DE_")
        // For non-DE exercises, use standard weight calculation
        return weightSelectionService.getTargetWeight(
            exercise.name,
            intensity,
            oneRepMaxes,
            isDynamicEffort = isDynamicEffort,
            currentWeekNumber = currentWeekNumber,
            preparedData = preparedData
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
        return Flux.fromIterable(stageCreators)
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
}
