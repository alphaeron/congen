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
 * Service for generating workout stages for 2-day conjugate powerlifting programs.
 *
 * This service handles the specific requirements of 2-day conjugate programs,
 * which feature combined ME+DE days where both maximal effort and dynamic effort
 * exercises are performed in the same workout.
 *
 * ## 2-Day Program Structure
 *
 * - **Day 1**: ME Upper + DE Lower
 * - **Day 2**: ME Lower + DE Upper
 *
 * ## Stage Generation
 *
 * Each workout includes:
 * - **Primary Stage**: Contains both ME and DE exercises
 * - **Accessory Stage**: Multiple accessory exercises based on available time
 * - **Conditioning Stage**: AMRAP/EMOM exercises (for DE components)
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
class TwoDayWorkoutStageGenerationService(
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
        private val logger = LoggerFactory.getLogger(TwoDayWorkoutStageGenerationService::class.java)
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
        // Initialize movement balance state for this workout
        var movementBalanceState = createInitialMovementBalanceState()

        // Only handle combined ME+DE days for 2-day programs
        if (!conjugateTemplates.isCombinedMEDay(dayType)) {
            logger.warn("TwoDayWorkoutStageGenerationService received non-combined ME day: {}", dayType)
            return Mono.empty()
        }

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
}
