package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ProgramPreferences
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.service.SetSchemeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.jetbrains.annotations.VisibleForTesting

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
 * @param workoutStageDAL Data access layer for workout stage operations
 * @param workoutStageTypeDAL Data access layer for workout stage type operations
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
 * @param setSchemeDAL Data access layer for set scheme operations
 * @param setSchemeService Service for set scheme operations
 * @param prilepinGuidelinesService Service for Prilepin guidelines
 * @param weightSelectionService Service for weight selection
 * @param userWeightUnitPreferenceDAL Data access layer for user weight unit preferences
 * @param exerciseSelectionService Service for exercise selection
 * @param movementBalanceService Service for movement balance
 * @param sessionTimeCalculator Service for session time calculation
 * @param conjugateTemplates Service for managing workout templates
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

    @VisibleForTesting
    override fun generateStagesForDayType(
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
        val primaryExerciseMono =
            selectPrimaryExercise(
                userExercisePool = userExercisePool,
                workoutType = workoutType,
                weakMuscles = weakMuscles,
                dayType = dayType,
                movementBalanceState = movementBalanceState
            ).cache()

        val primarySetSchemesMono =
            primaryExerciseMono.flatMap { primaryExercise ->
                // Update movement balance state with primary exercise
                movementBalanceState = updateMovementBalanceState(movementBalanceState, primaryExercise, false)
                logMovementBalanceState(movementBalanceState, "Program $programId Day $dayNumber - $dayType")

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
        val secondaryExerciseMono =
            if (conjugateTemplates.hasSecondaryMovement(dayType)) {
                primaryExerciseMono.flatMap { primaryExercise ->
                    selectSecondaryExercise(
                        userExercisePool = userExercisePool,
                        primaryExercise = primaryExercise,
                        workoutType = workoutType,
                        dayType = dayType,
                        movementBalanceState = movementBalanceState
                    ).doOnNext { secondaryExercise ->
                        // Update movement balance state with secondary exercise
                        movementBalanceState = updateMovementBalanceState(movementBalanceState, secondaryExercise, false)
                        logMovementBalanceState(movementBalanceState, "Program $programId Day $dayNumber - $dayType")
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

                // Get secondary exercise and set schemes if available
                val secondaryExerciseAndSchemesMono =
                    secondaryExerciseMono
                        .flatMap { secondaryExercise ->
                            secondarySetSchemesMono.map { secondarySetSchemes ->
                                Triple(secondaryExercise, secondarySetSchemes, true)
                            }
                        }
                        .switchIfEmpty(
                            Mono.just(Triple<Exercise?, List<SetSchemeParams>, Boolean>(null, emptyList(), false))
                        )

                secondaryExerciseAndSchemesMono.flatMap { (secondaryExercise, secondarySetSchemes, hasSecondary) ->
                    val numAccessoryExercises =
                        calculateNumAccessoryExercises(
                            sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                            primarySetSchemes = primarySetSchemes,
                            secondarySetSchemes = secondarySetSchemes,
                            dayType = dayType
                        )

                    // Create all workout stages
                    val primaryStageMono = createPrimaryStage(
                        exercise = primaryExercise,
                        setSchemes = primarySetSchemes,
                        userId = userId
                    )

                    val secondaryStageMono = if (hasSecondary && secondaryExercise != null) {
                        createSecondaryStage(
                            exercise = secondaryExercise,
                            setSchemes = secondarySetSchemes,
                            userId = userId
                        )
                    } else {
                        Mono.empty()
                    }

                    val warmupStageMono = createWarmupStage(
                        userExercisePool = userExercisePool,
                        oneRepMaxes = oneRepMaxes,
                        dayType = dayType,
                        primaryExercise = primaryExercise,
                        secondaryExercise = secondaryExercise,
                        isFourDayTemplate = true,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )

                    val accessoryStageMono = createAccessoryStage(
                        userExercisePool = userExercisePool,
                        oneRepMaxes = oneRepMaxes,
                        dayType = dayType,
                        weakMuscles = weakMuscles,
                        numAccessoryExercises = numAccessoryExercises,
                        userId = userId,
                        currentWeekNumber = currentWeekNumber,
                        movementBalanceState = movementBalanceState
                    )

                    val conditioningStageMono = createConditioningStage(
                        userExercisePool = userExercisePool,
                        oneRepMaxes = oneRepMaxes,
                        dayType = dayType,
                        weakMuscles = weakMuscles,
                        userId = userId,
                        movementBalanceState = movementBalanceState
                    )

                    // Combine all stages - collect non-empty stages
                    Flux.merge(
                        primaryStageMono,
                        secondaryStageMono,
                        warmupStageMono,
                        accessoryStageMono,
                        conditioningStageMono
                    ).collectList()
                        .map { stages ->
                            WorkoutGenerationResult(programId, dayNumber, dayType, userId, stages)
                        }
                }
            }
            .doOnError { error ->
                logger.error("Failed to generate stages for day type: {}. Error: {}", dayType, error.message)
            }
    }
}
