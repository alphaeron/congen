package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.WorkoutStageTypeEnum
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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
 * @param prilepinGuidelinesService Service for Prilepin guidelines
 * @param weightSelectionService Service for weight selection
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
    prilepinGuidelinesService: PrilepinGuidelinesService,
    weightSelectionService: WeightSelectionService,
    exerciseSelectionService: ExerciseSelectionService,
    movementBalanceService: MovementBalanceService,
    sessionTimeCalculator: SessionTimeCalculator,
    private val conjugateTemplates: ConjugateTemplates,
) : WorkoutStageGenerationService(
        prilepinGuidelinesService,
        weightSelectionService,
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
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<List<WorkoutStageData>> {
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
                userExercisePool = preparedData.userExercisePool,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = movementBalanceState,
                exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                preferredDeExerciseName = preparedData.dePrimaryExerciseByDayType[dayType],
                currentWeekNumber = preparedData.currentWeekNumber
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
                    oneRepMaxes = preparedData.oneRepMaxes,
                    currentWeekNumber = preparedData.currentWeekNumber,
                    preparedData = preparedData
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
                        userExercisePool = preparedData.userExercisePool,
                        primaryExercise = primaryExercise,
                        workoutType = workoutType,
                        dayType = dayType,
                        preparedData = preparedData,
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
                    oneRepMaxes = preparedData.oneRepMaxes,
                    currentWeekNumber = preparedData.currentWeekNumber,
                    preparedData = preparedData
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
                            sessionTimeMinutes = preparedData.programPreferences.sessionTimeLengthInMinutes,
                            primarySetSchemes = primarySetSchemes,
                            secondarySetSchemes = secondarySetSchemes,
                            dayType = dayType
                        )

                    // Create all workout stages
                    val primaryStageMono =
                        createPrimaryStage(
                            exercise = primaryExercise,
                            setSchemes = primarySetSchemes
                        )

                    val secondaryStageMono =
                        if (hasSecondary && secondaryExercise != null) {
                            createSecondaryStage(
                                exercise = secondaryExercise,
                                setSchemes = secondarySetSchemes
                            )
                        } else {
                            Mono.empty()
                        }

                    val warmupStageMono =
                        createWarmupStage(
                            preparedData = preparedData,
                            dayType = dayType,
                            primaryExercise = primaryExercise,
                            secondaryExercise = secondaryExercise,
                            isFourDayTemplate = true
                        )

                    val accessoryStageMono =
                        createAccessoryStage(
                            preparedData = preparedData,
                            dayType = dayType,
                            numAccessoryExercises = numAccessoryExercises,
                            movementBalanceState = movementBalanceState
                        )

                    val conditioningStageMono =
                        createConditioningStage(
                            preparedData = preparedData,
                            dayType = dayType,
                            movementBalanceState = movementBalanceState
                        )

                    // Combine all stages sequentially so warmup gets first pick of pool before accessory
                    Flux.concat(
                        primaryStageMono,
                        secondaryStageMono,
                        warmupStageMono,
                        accessoryStageMono,
                        conditioningStageMono
                    ).collectList()
                }
            }
            .switchIfEmpty(
                // If primary exercise selection fails, create a minimal workout with just warmup and accessory stages
                Mono.just(
                    listOf(
                        WorkoutStageData(
                            stageType = WorkoutStageTypeEnum.WARMUP,
                            position = WorkoutStageTypeEnum.WARMUP.position,
                            name = WorkoutStageTypeEnum.WARMUP.displayName,
                            exercises = emptyList()
                        ),
                        WorkoutStageData(
                            stageType = WorkoutStageTypeEnum.ACCESSORY,
                            position = WorkoutStageTypeEnum.ACCESSORY.position,
                            name = WorkoutStageTypeEnum.ACCESSORY.displayName,
                            exercises = emptyList()
                        )
                    )
                )
            )
            .doOnError { error ->
                logger.error("Failed to generate stages for day type: {}. Error: {}", dayType, error.message)
            }
    }
}
