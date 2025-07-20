package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.MovementType
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
 * @property exerciseSelectionService Service for exercise selection logic
 * @property workoutStageOrchestrator Service for generating workout stages
 * @property sessionTimeCalculator Service for session time calculations
 * @property conjugateTemplates Service for managing workout templates
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class FourDayWorkoutStageGenerationService(
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
        sessionTimeCalculator
    ) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(FourDayWorkoutStageGenerationService::class.java)
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
        // Determine workout type based on day template
        val workoutType =
            when {
                dayType.startsWith("ME_") -> "maximal_effort"
                dayType.startsWith("DE_") -> "dynamic_effort"
                else -> "maximal_effort" // Default fallback
            }

        // Select primary exercise and generate set schemes in one cached operation
        val primaryExerciseAndSchemesMono =
            selectPrimaryExercise(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                weakMuscles = weakMuscles,
                rotationHistory = rotationHistory,
                workoutType = workoutType,
                movementType = dayType,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            ).flatMap { primaryExercise ->
                if (primaryExercise != null) {
                    generateSetSchemes(
                        exercise = primaryExercise,
                        movementRole = "primary",
                        dayType = dayType,
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    ).map { schemes -> Triple(primaryExercise, schemes, true) }
                } else {
                    Mono.just(Triple(null as Exercise?, emptyList<SetSchemeParams>(), false))
                }
            }.cache()

        // Process primary exercise and generate workout stages
        return primaryExerciseAndSchemesMono
            .flatMap { (primaryExercise, primarySetSchemes, hasPrimary) ->
                val secondaryExerciseMono: Mono<Exercise> =
                    if (conjugateTemplates.hasSecondaryMovement(dayType) && primaryExercise != null) {
                        selectSecondaryExercise(
                            primaryExercise = primaryExercise,
                            exercises = exercises,
                            preferences = preferences,
                            userEquipment = userEquipment,
                            rotationHistory = rotationHistory
                        )
                    } else {
                        Mono.empty()
                    }

                secondaryExerciseMono
                    .defaultIfEmpty(Exercise("NO_SECONDARY", "No secondary exercise", MovementType.HORIZONTAL_PUSH, false, false, false))
                    .flatMap { secondaryExercise ->
                        if (secondaryExercise.name == "NO_SECONDARY") {
                            // No secondary exercise found - handle case without secondary
                            val numAccessoryExercises =
                                calculateNumAccessoryExercises(
                                    sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                                    primarySetSchemes = primarySetSchemes,
                                    secondarySetSchemes = emptyList(),
                                    dayType = dayType
                                )

                            // Create stages sequentially using the common pattern
                            createStagesSequentially(
                                workout = workout,
                                stageCreators =
                                    listOf(
                                        // Primary stage if exercise exists
                                        {
                                            if (primaryExercise != null) {
                                                createPrimaryStage(
                                                    workout = workout,
                                                    exercise = primaryExercise,
                                                    setSchemes = primarySetSchemes,
                                                    userId = userId
                                                )
                                            } else {
                                                Mono.empty()
                                            }
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
                                                    userId = userId
                                                )
                                            } else {
                                                Mono.empty()
                                            }
                                        },
                                        // Conditioning stage for dynamic effort workouts
                                        {
                                            if (hasConditioning(dayType)) {
                                                createConditioningStage(
                                                    workout = workout,
                                                    dayType = dayType,
                                                    exercises = exercises,
                                                    preferences = preferences,
                                                    userEquipment = userEquipment,
                                                    oneRepMaxes = oneRepMaxes,
                                                    weakMuscles = weakMuscles,
                                                    rotationHistory = rotationHistory,
                                                    currentWeekNumber = currentWeekNumber,
                                                    userId = userId
                                                )
                                            } else {
                                                Mono.empty()
                                            }
                                        }
                                    )
                            )
                        } else {
                            // Secondary exercise found - handle case with secondary
                            val secondarySetSchemesMono =
                                generateSecondaryExerciseScheme(
                                    exercise = secondaryExercise,
                                    dayType = dayType,
                                    oneRepMaxes = oneRepMaxes,
                                    currentWeekNumber = currentWeekNumber,
                                    userId = userId
                                )

                            // Process secondary schemes and create workout stages
                            secondarySetSchemesMono
                                .flatMap { secondarySetSchemes ->
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
                                                // Primary stage if exercise exists
                                                {
                                                    if (primaryExercise != null) {
                                                        createPrimaryStage(
                                                            workout = workout,
                                                            exercise = primaryExercise,
                                                            setSchemes = primarySetSchemes,
                                                            userId = userId
                                                        )
                                                    } else {
                                                        Mono.empty()
                                                    }
                                                },
                                                // Secondary stage with secondary exercise
                                                {
                                                    createSecondaryStage(
                                                        workout = workout,
                                                        exercise = secondaryExercise,
                                                        setSchemes = secondarySetSchemes,
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
                                                            userId = userId
                                                        )
                                                    } else {
                                                        Mono.empty()
                                                    }
                                                },
                                                // Conditioning stage for dynamic effort workouts
                                                {
                                                    if (hasConditioning(dayType)) {
                                                        createConditioningStage(
                                                            workout = workout,
                                                            dayType = dayType,
                                                            exercises = exercises,
                                                            preferences = preferences,
                                                            userEquipment = userEquipment,
                                                            oneRepMaxes = oneRepMaxes,
                                                            weakMuscles = weakMuscles,
                                                            rotationHistory = rotationHistory,
                                                            currentWeekNumber = currentWeekNumber,
                                                            userId = userId
                                                        )
                                                    } else {
                                                        Mono.empty()
                                                    }
                                                }
                                            )
                                    )
                                }
                        }
                    }
            }
            .doOnError { error ->
                logger.error("Failed to generate traditional workout: {}, {}, {}", workout.name, workout.id, error.message)
            }
    }
}
