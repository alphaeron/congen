package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.service.SetSchemeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for generating workout stages for 4-day conjugate powerlifting programs.
 *
 * This service handles the specific requirements of 4-day conjugate programs,
 * which feature traditional separate maximal effort and dynamic effort days
 * with dedicated secondary movements.
 *
 * ## 4-Day Program Structure
 *
 * - **Day 1**: ME Upper
 * - **Day 2**: DE Lower
 * - **Day 3**: ME Lower
 * - **Day 4**: DE Upper
 *
 * ## Stage Generation
 *
 * Each workout includes:
 * - **Primary Stage**: Main ME or DE exercise
 * - **Secondary Stage**: Supporting compound movement (for ME_Upper and DE_Upper)
 * - **Accessory Stage**: Multiple accessory exercises based on available time
 * - **Conditioning Stage**: AMRAP/EMOM exercises (for DE days)
 *
 * @property conjugateTemplates Service for managing workout templates
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class FourDayWorkoutStageGenerationService(
    workoutStageDAL: WorkoutStageDAL,
    workoutStageTypeDAL: WorkoutStageTypeDAL,
    programmedExerciseDAL: ProgrammedExerciseDAL,
    setSchemeDAL: SetSchemeDAL,
    setSchemeService: SetSchemeService,
    prilepinGuidelinesService: PrilepinGuidelinesService,
    weightSelectionService: WeightSelectionService,
    userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    exerciseSelectionService: ExerciseSelectionService,
    movementBalanceService: MovementBalanceService,
    sessionTimeCalculator: SessionTimeCalculator,
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
        private val logger = LoggerFactory.getLogger(FourDayWorkoutStageGenerationService::class.java)
    }

    override fun generateStagesForDayType(
        workout: ProgrammedWorkout,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<Void> {
        // Initialize movement balance state for this workout
        var movementBalanceState = createInitialMovementBalanceState()

        // Determine workout type based on day template
        val workoutType =
            when {
                dayType.startsWith("ME_") -> "maximal_effort"
                dayType.startsWith("DE_") -> "dynamic_effort"
                else -> "maximal_effort" // Default fallback
            }

        // Select primary exercise and generate set schemes
        val primaryExerciseMono = selectPrimaryExercise(
            userExercisePool = userExercisePool,
            workoutType = workoutType,
            weakMuscles = weakMuscles,
            movementBalanceState = movementBalanceState
        )

        val primarySetSchemesMono =
            primaryExerciseMono.flatMap { primaryExercise ->
                // Update movement balance state with primary exercise
                movementBalanceState = updateMovementBalanceState(movementBalanceState, primaryExercise, false)
                logMovementBalanceState(movementBalanceState, "${workout.id} - $dayType")
                
                generateSetSchemes(
                    exercise = primaryExercise,
                    movementRole = "primary",
                    dayType = dayType,
                    oneRepMaxes = oneRepMaxes,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }.onErrorResume { error ->
                logger.error("Failed to generate set schemes for primary exercise. Error: {}", error.message)
                Mono.just(emptyList())
            }

        // Select secondary exercise if applicable
        val secondaryExerciseMono = if (conjugateTemplates.hasSecondaryMovement(dayType)) {
            primaryExerciseMono.flatMap { primaryExercise ->
                selectSecondaryExercise(
                    userExercisePool = userExercisePool,
                    primaryExercise = primaryExercise,
                    movementBalanceState = movementBalanceState
                ).doOnNext { secondaryExercise ->
                    // Update movement balance state with secondary exercise
                    movementBalanceState = updateMovementBalanceState(movementBalanceState, secondaryExercise, false)
                    logMovementBalanceState(movementBalanceState, "${workout.id} - $dayType")
                }
            }
        } else {
            Mono.empty()
        }

        val secondarySetSchemesMono =
            secondaryExerciseMono.flatMap { secondaryExercise ->
                generateSetSchemes(
                    exercise = secondaryExercise,
                    movementRole = "secondary",
                    dayType = dayType,
                    oneRepMaxes = oneRepMaxes,
                    currentWeekNumber = currentWeekNumber,
                    userId = userId
                )
            }.onErrorResume { error ->
                logger.error("Failed to generate set schemes for secondary exercise. Error: {}", error.message)
                Mono.just(emptyList())
            }

        // Combine all operations and create stages
        return Mono.zip(primaryExerciseMono, primarySetSchemesMono)
            .flatMap { tuple ->
                val primaryExercise = tuple.t1
                val primarySetSchemes = tuple.t2
                
                // Handle secondary exercise separately since it might not exist
                secondaryExerciseMono.flatMap { secondaryExercise ->
                    secondarySetSchemesMono.flatMap { secondarySetSchemes ->
                        val numAccessoryExercises =
                            calculateNumAccessoryExercises(
                                sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                                primarySetSchemes = primarySetSchemes,
                                secondarySetSchemes = secondarySetSchemes,
                                dayType = dayType
                            )

                        // Create stages sequentially
                        createStagesSequentially(
                            stageCreators = listOf(
                                // Warmup stage
                                {
                                    createWarmupStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        primaryExercise = primaryExercise,
                                        isFourDayTemplate = true,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId
                                    )
                                },
                                // Primary stage
                                {
                                    createPrimaryStage(
                                        workout = workout,
                                        exercise = primaryExercise,
                                        setSchemes = primarySetSchemes
                                    )
                                },
                                // Secondary stage
                                {
                                    createSecondaryStage(
                                        workout = workout,
                                        exercise = secondaryExercise,
                                        setSchemes = secondarySetSchemes
                                    )
                                },
                                // Accessory stage
                                {
                                    createAccessoryStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        numAccessoryExercises = numAccessoryExercises,
                                        userId = userId,
                                        currentWeekNumber = currentWeekNumber,
                                        movementBalanceState = movementBalanceState
                                    )
                                },
                                // Conditioning stage (for DE days)
                                {
                                    createConditioningStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        userId = userId,
                                        movementBalanceState = movementBalanceState
                                    )
                                }
                            )
                        )
                    }
                }.switchIfEmpty(
                    // No secondary exercise - handle case without secondary
                    Mono.defer {
                        val numAccessoryExercises =
                            calculateNumAccessoryExercises(
                                sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                                primarySetSchemes = primarySetSchemes,
                                secondarySetSchemes = emptyList(),
                                dayType = dayType
                            )

                        // Create stages sequentially
                        createStagesSequentially(
                            stageCreators = listOf(
                                // Warmup stage
                                {
                                    createWarmupStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        primaryExercise = primaryExercise,
                                        isFourDayTemplate = true,
                                        currentWeekNumber = currentWeekNumber,
                                        userId = userId
                                    )
                                },
                                // Primary stage
                                {
                                    createPrimaryStage(
                                        workout = workout,
                                        exercise = primaryExercise,
                                        setSchemes = primarySetSchemes
                                    )
                                },
                                // Accessory stage
                                {
                                    createAccessoryStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        numAccessoryExercises = numAccessoryExercises,
                                        userId = userId,
                                        currentWeekNumber = currentWeekNumber,
                                        movementBalanceState = movementBalanceState
                                    )
                                },
                                // Conditioning stage (for DE days)
                                {
                                    createConditioningStage(
                                        workout = workout,
                                        userExercisePool = userExercisePool,
                                        oneRepMaxes = oneRepMaxes,
                                        dayType = dayType,
                                        weakMuscles = weakMuscles,
                                        userId = userId,
                                        movementBalanceState = movementBalanceState
                                    )
                                }
                            )
                        )
                    }
                )
            }
            .onErrorResume { error ->
                logger.error("Failed to generate stages for day type: {}. Error: {}", dayType, error.message)
                Mono.empty()
            }
    }
}
