package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.service.SetSchemeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for generating workout stages for 3-day conjugate powerlifting programs.
 *
 * This service handles the specific requirements of 3-day conjugate programs,
 * which feature both combined ME+DE days and a dedicated full body dynamic effort day.
 *
 * ## 3-Day Program Structure
 *
 * - **Day 1**: ME Upper + DE Lower
 * - **Day 2**: ME Lower + DE Upper
 * - **Day 3**: Full Body Dynamic Effort
 *
 * ## Stage Generation
 *
 * Combined ME+DE days include:
 * - **Primary Stage**: Contains both ME and DE exercises
 * - **Accessory Stage**: Multiple accessory exercises based on available time
 * - **Conditioning Stage**: AMRAP/EMOM exercises (for DE components)
 *
 * Full Body DE days include:
 * - **Primary Stage**: Contains both upper and lower DE exercises
 * - **Accessory Stage**: Multiple accessory exercises based on available time
 * - **Conditioning Stage**: AMRAP/EMOM exercises
 *
 * @property exerciseSelectionService Service for exercise selection logic
 * @property workoutStageOrchestrator Service for generating workout stages
 * @property sessionTimeCalculator Service for session time calculations
 * @property conjugateTemplates Service for managing workout templates
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ThreeDayWorkoutStageGenerationService(
    exerciseSelectionService: ExerciseSelectionService,
    workoutStageDAL: WorkoutStageDAL,
    workoutStageTypeDAL: WorkoutStageTypeDAL,
    programmedExerciseDAL: ProgrammedExerciseDAL,
    setSchemeDAL: SetSchemeDAL,
    setSchemeService: SetSchemeService,
    prilepinGuidelinesService: PrilepinGuidelinesService,
    weightSelectionService: WeightSelectionService,
    userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    sessionTimeCalculator: SessionTimeCalculator,
    movementBalanceService: MovementBalanceService,
    private val conjugateTemplates: ConjugateTemplates,
) : WorkoutStageGenerationService(
        exerciseSelectionService,
        workoutStageDAL,
        workoutStageTypeDAL,
        programmedExerciseDAL,
        setSchemeDAL,
        setSchemeService,
        prilepinGuidelinesService,
        weightSelectionService,
        userWeightUnitPreferenceDAL,
        sessionTimeCalculator,
        movementBalanceService
    ) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ThreeDayWorkoutStageGenerationService::class.java)
    }

    override fun generateStagesForDayType(
        workout: ProgrammedWorkout,
        dayType: String,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: Int
    ): Mono<Void> {
        return when {
            conjugateTemplates.isCombinedMEDay(dayType) -> {
                generateCombinedMEDay(
                    workout = workout,
                    dayType = dayType,
                    exercises = exercises,
                    preferences = preferences,
                    userEquipment = userEquipment,
                    oneRepMaxes = oneRepMaxes,
                    programPreferences = programPreferences,
                    rotationHistory = rotationHistory,
                    weakMuscles = weakMuscles,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }
            conjugateTemplates.isFullBodyDE(dayType) -> {
                generateFullBodyDEDay(
                    workout = workout,
                    exercises = exercises,
                    preferences = preferences,
                    userEquipment = userEquipment,
                    oneRepMaxes = oneRepMaxes,
                    programPreferences = programPreferences,
                    rotationHistory = rotationHistory,
                    weakMuscles = weakMuscles,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }
            else -> {
                logger.warn("ThreeDayWorkoutStageGenerationService received unsupported day type: {}", dayType)
                Mono.empty()
            }
        }
    }

    /**
     * Generates workout stages for a combined ME+DE day.
     */
    private fun generateCombinedMEDay(
        workout: ProgrammedWorkout,
        dayType: String,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: Int
    ): Mono<Void> {
        // Initialize movement balance state for this workout
        var movementBalanceState = createInitialMovementBalanceState()
        
        val primaryMovementType = conjugateTemplates.getPrimaryMovementType(dayType)
        val secondaryMovementType = conjugateTemplates.getSecondaryMovementType(dayType)

        // Select primary ME exercise
        val primaryExerciseMono =
            selectPrimaryExercise(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                weakMuscles = weakMuscles,
                rotationHistory = rotationHistory,
                workoutType = "maximal_effort",
                movementType = primaryMovementType,
                currentWeekNumber = currentWeekNumber,
                userId = userId,
                movementBalanceState = movementBalanceState
            )

        // Select secondary DE exercise
        val secondaryExerciseMono =
            selectDEExercise(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                weakMuscles = weakMuscles,
                rotationHistory = rotationHistory,
                movementBalanceState = movementBalanceState
            )

        // Generate set schemes for both exercises
        val primarySetSchemesMono =
            primaryExerciseMono.flatMap { primaryExercise ->
                if (primaryExercise != null) {
                    generateSetSchemes(
                        exercise = primaryExercise,
                        movementRole = "primary",
                        dayType = primaryMovementType,
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )
                } else {
                    Mono.just(emptyList())
                }
            }

        val secondarySetSchemesMono =
            secondaryExerciseMono.flatMap { secondaryExercise ->
                if (secondaryExercise != null) {
                    generateSetSchemes(
                        exercise = secondaryExercise,
                        movementRole = "secondary",
                        dayType = secondaryMovementType!!,
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )
                } else {
                    Mono.just(emptyList())
                }
            }

        // Create workout stages with both exercises in the same primary stage
        return Mono.zip(primaryExerciseMono, secondaryExerciseMono, primarySetSchemesMono, secondarySetSchemesMono)
            .flatMap { tuple ->
                val primaryExercise = tuple.t1
                val secondaryExercise = tuple.t2
                val primarySetSchemes = tuple.t3
                val secondarySetSchemes = tuple.t4

                // Calculate number of accessory exercises based on program preferences
                val numAccessoryExercises =
                    calculateNumAccessoryExercises(
                        sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = primarySetSchemes,
                        secondarySetSchemes = secondarySetSchemes,
                        dayType = dayType
                    )

                // Create stages sequentially using the common pattern
                createStagesSequentially(
                    workout = workout,
                    stageCreators =
                        listOf(
                            // Warmup stage
                            {
                                createWarmupStage(
                                    workout = workout,
                                    exercises = exercises,
                                    preferences = preferences,
                                    userEquipment = userEquipment,
                                    oneRepMaxes = oneRepMaxes,
                                    dayType = dayType,
                                    primaryExercise = primaryExercise,
                                    isFourDayTemplate = false,
                                    currentWeekNumber = currentWeekNumber,
                                    userId = userId
                                )
                            },
                            // Primary stage with both ME and DE exercises
                            {
                                createCombinedPrimaryStage(
                                    workout = workout,
                                    primaryExercise = primaryExercise,
                                    secondaryExercise = secondaryExercise,
                                    primarySetSchemes = primarySetSchemes,
                                    secondarySetSchemes = secondarySetSchemes,
                                    userId = userId
                                )
                            },
                            // Accessory stage if needed
                            {
                                if (numAccessoryExercises > 0) {
                                    createAccessoryStage(
                                        workout = workout,
                                        exercises = exercises,
                                        preferences = preferences,
                                        userEquipment = userEquipment,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        numAccessoryExercises = numAccessoryExercises,
                                        rotationHistory = rotationHistory,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId,
                                        movementBalanceState = movementBalanceState
                                    )
                                } else {
                                    Mono.empty()
                                }
                            },
                            // Conditioning stage if needed
                            {
                                if (hasConditioning(dayType)) {
                                    createConditioningStage(
                                        workout = workout,
                                        exercises = exercises,
                                        preferences = preferences,
                                        userEquipment = userEquipment,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        rotationHistory = rotationHistory,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId,
                                        movementBalanceState = movementBalanceState
                                    )
                                } else {
                                    Mono.empty()
                                }
                            }
                        )
                )
            }
            .doOnError { error ->
                logger.error("Error creating combined ME+DE workout stages for workout '{}': {}", workout.id, error.message)
            }
    }

    /**
     * Generates workout stages for a full body DE day.
     */
    private fun generateFullBodyDEDay(
        workout: ProgrammedWorkout,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: Int
    ): Mono<Void> {
        // Initialize movement balance state for this workout
        var movementBalanceState = createInitialMovementBalanceState()
        
        // Select upper body DE exercise
        val upperDEExerciseMono =
            selectPrimaryExercise(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                weakMuscles = weakMuscles,
                rotationHistory = rotationHistory,
                workoutType = "dynamic_effort",
                movementType = "DE_Upper",
                currentWeekNumber = currentWeekNumber,
                userId = userId,
                movementBalanceState = movementBalanceState
            )

        // Select lower body DE exercise
        val lowerDEExerciseMono =
            selectPrimaryExercise(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                weakMuscles = weakMuscles,
                rotationHistory = rotationHistory,
                workoutType = "dynamic_effort",
                movementType = "DE_Lower",
                currentWeekNumber = currentWeekNumber,
                userId = userId,
                movementBalanceState = movementBalanceState
            )

        // Generate set schemes for both exercises
        val upperDESetSchemesMono =
            upperDEExerciseMono.flatMap { upperDEExercise ->
                if (upperDEExercise != null) {
                    generateSetSchemes(
                        exercise = upperDEExercise,
                        movementRole = "primary",
                        dayType = "DE_Upper",
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )
                } else {
                    Mono.just(emptyList())
                }
            }

        val lowerDESetSchemesMono =
            lowerDEExerciseMono.flatMap { lowerDEExercise ->
                if (lowerDEExercise != null) {
                    generateSetSchemes(
                        exercise = lowerDEExercise,
                        movementRole = "secondary",
                        dayType = "DE_Lower",
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )
                } else {
                    Mono.just(emptyList())
                }
            }

        // Create workout stages with both DE exercises in the same primary stage
        return Mono.zip(upperDEExerciseMono, lowerDEExerciseMono, upperDESetSchemesMono, lowerDESetSchemesMono)
            .flatMap { tuple ->
                val upperDEExercise = tuple.t1
                val lowerDEExercise = tuple.t2
                val upperDESetSchemes = tuple.t3
                val lowerDESetSchemes = tuple.t4

                // Calculate number of accessory exercises based on program preferences
                val numAccessoryExercises =
                    calculateNumAccessoryExercises(
                        sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = upperDESetSchemes,
                        secondarySetSchemes = lowerDESetSchemes,
                        dayType = "DE_Full_Body"
                    )

                // Create stages sequentially using the common pattern
                createStagesSequentially(
                    workout = workout,
                    stageCreators =
                        listOf(
                            // Warmup stage
                            {
                                createWarmupStage(
                                    workout = workout,
                                    exercises = exercises,
                                    preferences = preferences,
                                    userEquipment = userEquipment,
                                    oneRepMaxes = oneRepMaxes,
                                    dayType = "DE_Full_Body",
                                    primaryExercise = upperDEExercise,
                                    isFourDayTemplate = false,
                                    currentWeekNumber = currentWeekNumber,
                                    userId = userId
                                )
                            },
                            // Primary stage with both upper and lower DE exercises
                            {
                                createCombinedPrimaryStage(
                                    workout = workout,
                                    primaryExercise = upperDEExercise,
                                    secondaryExercise = lowerDEExercise,
                                    primarySetSchemes = upperDESetSchemes,
                                    secondarySetSchemes = lowerDESetSchemes,
                                    userId = userId
                                )
                            },
                            // Accessory stage if needed
                            {
                                if (numAccessoryExercises > 0) {
                                    createAccessoryStage(
                                        workout = workout,
                                        exercises = exercises,
                                        preferences = preferences,
                                        userEquipment = userEquipment,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = "DE_Full_Body",
                                        weakMuscles = weakMuscles,
                                        numAccessoryExercises = numAccessoryExercises,
                                        rotationHistory = rotationHistory,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId,
                                        movementBalanceState = movementBalanceState
                                    )
                                } else {
                                    Mono.empty()
                                }
                            },
                            // Conditioning stage for dynamic effort workouts
                            {
                                createConditioningStage(
                                    workout = workout,
                                    dayType = "DE_Full_Body",
                                    exercises = exercises,
                                    preferences = preferences,
                                    userEquipment = userEquipment,
                                    oneRepMaxes = oneRepMaxes,
                                    weakMuscles = weakMuscles,
                                    rotationHistory = rotationHistory,
                                    currentWeekNumber = currentWeekNumber,
                                    userId = userId,
                                    movementBalanceState = movementBalanceState
                                )
                            }
                        )
                )
            }
            .doOnError { error ->
                logger.error("Error creating full body DE workout stages for workout '{}': {}", workout.id, error.message)
            }
    }
}
