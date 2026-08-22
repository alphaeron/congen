package com.congen.generator

import com.congen.model.WorkoutStageTypeEnum
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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
 * @param prilepinGuidelinesService Service for Prilepin-based guidelines
 * @param weightSelectionService Service for conjugate-specific weight selection
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
    prilepinGuidelinesService: PrilepinGuidelinesService,
    weightSelectionService: WeightSelectionService,
    sessionTimeCalculator: SessionTimeCalculator,
    movementBalanceService: MovementBalanceService,
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
        private val logger = LoggerFactory.getLogger(TwoDayWorkoutStageGenerationService::class.java)
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

        // Only handle combined ME+DE days for 2-day programs
        if (!conjugateTemplates.isCombinedMEDay(dayType)) {
            logger.warn("TwoDayWorkoutStageGenerationService received non-combined ME day: {}", dayType)
            return Mono.just(emptyList())
        }

        val primaryMovementType = conjugateTemplates.getPrimaryMovementType(dayType)
        val secondaryMovementType = conjugateTemplates.getSecondaryMovementType(dayType)

        // Select primary ME exercise
        val primaryExerciseMono =
            selectPrimaryExercise(
                userExercisePool = preparedData.userExercisePool,
                workoutType = "maximal_effort",
                dayType = primaryMovementType,
                movementBalanceState = movementBalanceState,
                exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                preferredDeExerciseName = preparedData.dePrimaryExerciseByDayType[primaryMovementType],
                currentWeekNumber = preparedData.currentWeekNumber
            )

        // Select secondary DE exercise (this is a primary movement, not an accessory)
        val secondaryExerciseMono =
            if (secondaryMovementType != null) {
                selectPrimaryExercise(
                    userExercisePool = preparedData.userExercisePool,
                    workoutType = "dynamic_effort",
                    dayType = secondaryMovementType,
                    movementBalanceState = movementBalanceState,
                    exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                    exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                    preferredDeExerciseName = preparedData.dePrimaryExerciseByDayType[secondaryMovementType],
                    currentWeekNumber = preparedData.currentWeekNumber
                )
            } else {
                Mono.error(IllegalStateException("Secondary movement type is null for dayType: $dayType"))
            }

        // Generate set schemes for both exercises
        val primarySetSchemesMono =
            primaryExerciseMono.flatMap { primaryExercise ->
                generateSetSchemes(
                    exercise = primaryExercise,
                    movementRole = "primary",
                    dayType = primaryMovementType,
                    oneRepMaxes = preparedData.oneRepMaxes,
                    currentWeekNumber = preparedData.currentWeekNumber,
                    preparedData = preparedData,
                    applyDeBandWeight = primaryMovementType.startsWith("DE_"),
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
                    oneRepMaxes = preparedData.oneRepMaxes,
                    currentWeekNumber = preparedData.currentWeekNumber,
                    preparedData = preparedData,
                    applyDeBandWeight = secondaryMovementType.startsWith("DE_"),
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
                        sessionTimeMinutes = preparedData.programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = primarySetSchemes,
                        secondarySetSchemes = secondarySetSchemes,
                        dayType = dayType
                    )

                // Create all workout stages
                val primaryStageMono =
                    createCombinedPrimaryStage(
                        primaryExercise = primaryExercise,
                        secondaryExercise = secondaryExercise,
                        primarySetSchemes = primarySetSchemes,
                        secondarySetSchemes = secondarySetSchemes
                    )

                val warmupStageMono =
                    createWarmupStage(
                        preparedData = preparedData,
                        dayType = dayType,
                        primaryExercise = primaryExercise,
                        secondaryExercise = secondaryExercise,
                        isFourDayTemplate = false
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
                    warmupStageMono,
                    accessoryStageMono,
                    conditioningStageMono
                ).collectList()
            }
            .switchIfEmpty(
                // If any exercise selection fails, create a minimal workout with just warmup and accessory stages
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
                logger.error("Error creating combined ME+DE workout stages for program {} day {}: {}", programId, dayNumber, error.message)
            }
    }
}
