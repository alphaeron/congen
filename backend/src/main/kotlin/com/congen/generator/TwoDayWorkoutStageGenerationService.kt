package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.ProgramPreferences
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
 * @param exerciseSelectionService Service for exercise selection logic
 * @param workoutStageDAL Data access layer for workout stage operations
 * @param workoutStageTypeDAL Data access layer for workout stage type operations
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
 * @param setSchemeDAL Data access layer for set scheme operations
 * @param setSchemeService Service for set scheme operations
 * @param prilepinGuidelinesService Service for Prilepin-based guidelines
 * @param weightSelectionService Service for conjugate-specific weight selection
 * @param userWeightUnitPreferenceDAL Data access layer for user weight unit preferences
 * @param sessionTimeCalculator Service for session time calculations
 * @param movementBalanceService Service for movement balance
 * @param conjugateTemplates Service for managing workout templates
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
        workoutStageDAL,
        workoutStageTypeDAL,
        programmedExerciseDAL,
        setSchemeDAL,
        setSchemeService,
        prilepinGuidelinesService,
        weightSelectionService,
        userWeightUnitPreferenceDAL,
        exerciseSelectionService,
        movementBalanceService,
        sessionTimeCalculator
    ) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(TwoDayWorkoutStageGenerationService::class.java)
    }

    override fun generateStagesForDayType(
        workout: ProgrammedWorkout,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: ProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
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
                userExercisePool = userExercisePool,
                workoutType = "maximal_effort",
                weakMuscles = weakMuscles,
                dayType = dayType,
                movementBalanceState = movementBalanceState
            )

        // Select secondary DE exercise
        val workoutType = if (dayType.startsWith("DE_")) "dynamic_effort" else "maximal_effort"
        val secondaryExerciseMono =
            selectConditioningExercise(
                userExercisePool = userExercisePool,
                weakMuscles = weakMuscles,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = movementBalanceState
            )

        // Generate set schemes for both exercises
        val primarySetSchemesMono =
            primaryExerciseMono.flatMap { primaryExercise ->
                generateSetSchemes(
                    exercise = primaryExercise,
                    movementRole = "primary",
                    dayType = primaryMovementType,
                    oneRepMaxes = oneRepMaxes,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }.onErrorResume { error ->
                logger.error("Failed to generate set schemes for primary exercise. Error: {}", error.message)
                Mono.just(emptyList())
            }

        val secondarySetSchemesMono =
            secondaryExerciseMono.flatMap { secondaryExercise ->
                generateSetSchemes(
                    exercise = secondaryExercise,
                    movementRole = "secondary",
                    dayType = secondaryMovementType!!,
                    oneRepMaxes = oneRepMaxes,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }.onErrorResume { error ->
                logger.error("Failed to generate set schemes for secondary exercise. Error: {}", error.message)
                Mono.just(emptyList())
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
                    stageCreators =
                        listOf(
                            // Warmup stage
                            {
                                createWarmupStage(
                                    workout = workout,
                                    userExercisePool = userExercisePool,
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
                                )
                            },
                            // Accessory stage if needed
                            {
                                if (numAccessoryExercises > 0) {
                                    createAccessoryStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        numAccessoryExercises = numAccessoryExercises,
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
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
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
