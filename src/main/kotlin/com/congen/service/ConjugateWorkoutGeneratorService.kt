package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.conjugate.ConjugateConstants
import com.congen.service.conjugate.ConjugateTemplates
import com.congen.service.conjugate.ExerciseSelectionService
import com.congen.service.conjugate.SessionTimeCalculator
import com.congen.service.conjugate.SetSchemeParams
import com.congen.service.conjugate.WorkoutStageGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service for generating conjugate powerlifting workout programs.
 *
 * This service implements the Westside Barbell conjugate method for powerlifting,
 * incorporating undulating periodization, exercise rotation, and personalized
 * programming based on user preferences and available equipment.
 *
 * ## Conjugate Method Principles
 *
 * - **Max Effort (ME)**: Heavy singles, doubles, or triples at 85-92% 1RM
 * - **Dynamic Effort (DE)**: Speed work at 60-70% 1RM with explosive intent
 * - **Accessory Work**: Targeted muscle development and weak point training
 * - **Exercise Rotation**: Prevent accommodation by rotating exercises every 1-3 weeks
 *
 * ## Program Structure
 *
 * - **2-day programs**: DE+ME same day, alternating lower and upper
 * - **3-day programs**: same as 2-day programs, but with an additional full body DE day
 * - **4-day programs**: Extended conjugate with additional volume
 *
 * ## Exercise Categories
 *
 * - **Primary**: Main compound movements (squat, bench, deadlift variations)
 * - **Secondary**: Supporting compound movements
 * - **Accessory**: Isolation and weak point training
 * - **Conditioning**: Cardio and recovery work
 *
 * @property exerciseDAL Data access layer for exercise operations
 * @property userExercisePreferenceDAL Data access layer for user exercise preferences
 * @property userEquipmentDAL Data access layer for user equipment
 * @property userOneRepMaxDAL Data access layer for user one rep max values
 * @property userProgramPreferencesDAL Data access layer for user program preferences
 * @property exerciseRotationHistoryDAL Data access layer for exercise rotation history
 * @property programDAL Data access layer for program operations
 * @property programmedWorkoutDAL Data access layer for programmed workout operations
 * @property conjugateTemplates Service for managing workout templates
 * @property exerciseSelectionService Service for exercise selection logic
 * @property workoutStageGenerator Service for generating workout stages
 * @property sessionTimeCalculator Service for session time calculations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ConjugateWorkoutGeneratorService(
    private val exerciseDAL: ExerciseDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
    private val exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL,
    private val programDAL: ProgramDAL,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val conjugateTemplates: ConjugateTemplates,
    private val exerciseSelectionService: ExerciseSelectionService,
    private val workoutStageGenerator: WorkoutStageGenerator,
    private val sessionTimeCalculator: SessionTimeCalculator,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorService::class.java)
    }

    /**
     * Generates the next week of workouts for an existing conjugate powerlifting program.
     *
     * This method generates a complete week of workouts for an existing program based on the conjugate method,
     * incorporating user preferences, available equipment, and exercise rotation history.
     *
     * @param programId The ID of the existing program
     * @return Mono containing the updated program with new workouts
     * @throws com.congen.exceptions.ValidationException if the user's program contains an invalid number of days/week
     * @throws com.congen.exceptions.NoResultsFoundException if the program is not found
     */
    fun generateNextWeek(programId: Long): Mono<Program> {
        logger.info("Generating next week for program {}", programId)

        return programDAL.selectProgramById(programId)
            .flatMap { program ->
                Mono.zip(
                    exerciseDAL.selectExercises(),
                    userExercisePreferenceDAL.selectUserExercisePreferencesByUser(program.userId),
                    userEquipmentDAL.selectUserEquipmentByUser(program.userId),
                    userOneRepMaxDAL.selectUserOneRepMaxByUser(program.userId),
                    userProgramPreferencesDAL.selectUserProgramPreferences(program.userId),
                    exerciseRotationHistoryDAL.selectAll()
                ).flatMap { tuple ->
                    val exercises = tuple.t1
                    val preferences = tuple.t2
                    val userEquipment = tuple.t3
                    val oneRepMaxes = tuple.t4
                    val programPreferences = tuple.t5
                    val rotationHistory = tuple.t6

                    val weakMuscles = ConjugateConstants.DEFAULT_WEAK_MUSCLES
                    val template = conjugateTemplates.selectTemplate(programPreferences.programDaysPerWeek)

                    generateWorkoutsForWeek(
                        program = program,
                        exercises = exercises,
                        preferences = preferences,
                        userEquipment = userEquipment,
                        oneRepMaxes = oneRepMaxes,
                        programPreferences = programPreferences,
                        rotationHistory = rotationHistory,
                        template = template,
                        weakMuscles = weakMuscles,
                        currentWeekNumber = program.currentWeekNumber
                    ).then(
                        programDAL.updateProgram(
                            program.id,
                            "Conjugate Powerlifting - Week ${program.currentWeekNumber + 1}",
                            program.currentWeekNumber + 1,
                            program.isActive
                        )
                    )
                }
            }
    }

    /**
     * Generates workouts for the specified week.
     */
    private fun generateWorkoutsForWeek(
        program: Program,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>,
        template: List<com.congen.service.conjugate.DayTemplate>,
        weakMuscles: List<String>,
        currentWeekNumber: Int
    ): Mono<Void> {
        return Flux.fromIterable(template)
            .index()
            .concatMap { tuple ->
                val dayIndex = tuple.t1
                val dayTemplate = tuple.t2
                val dayNumber = (currentWeekNumber - 1) * template.size + dayIndex.toInt() + 1

                programmedWorkoutDAL.insertProgrammedWorkout(program.id, dayNumber, "${dayTemplate.type} Day")
                    .doOnError { error ->
                        logger.error("Error inserting programmed workout: {}", error.message)
                    }
                    .flatMap { createdWorkout ->
                        generateWorkoutStages(
                            workout = createdWorkout,
                            dayTemplate = dayTemplate,
                            exercises = exercises,
                            preferences = preferences,
                            userEquipment = userEquipment,
                            oneRepMaxes = oneRepMaxes,
                            programPreferences = programPreferences,
                            rotationHistory = rotationHistory,
                            weakMuscles = weakMuscles,
                            currentWeekNumber = currentWeekNumber,
                            userId = program.userId
                        ).doOnError { error ->
                            logger.error("Error generating workout stages: {}", error.message)
                        }
                    }
                    .doOnError { error ->
                        logger.error("Error processing programmed workout: {}", error.message)
                    }
            }
            .doOnError { error ->
                logger.error("Error generating workouts for week: {}", error.message)
            }
            .then()
    }

    /**
     * Generates workout stages for a specific workout.
     */
    private fun generateWorkoutStages(
        workout: ProgrammedWorkout,
        dayTemplate: com.congen.service.conjugate.DayTemplate,
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
        val dayType = dayTemplate.type

        // Determine workout type based on day template
        val workoutType =
            when {
                dayType.startsWith("ME_") -> "maximal_effort"
                dayType.startsWith("DE_") -> "dynamic_effort"
                else -> "maximal_effort" // Default fallback
            }

        // Select primary exercise
        val primaryExerciseMono =
            exerciseSelectionService.filterExercisesByWorkoutType(
                exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                workoutType
            ).flatMap { filteredExercises ->
                exerciseSelectionService.selectRotatingExercise(
                    targetMuscles = weakMuscles,
                    userEquipment = userEquipment,
                    preferences = preferences,
                    exercises = filteredExercises,
                    isAccessory = false,
                    rotationHistory = rotationHistory
                )
            }

        // Generate primary set schemes for time calculation
        val primarySetSchemesMono: Mono<List<SetSchemeParams>> =
            primaryExerciseMono.flatMap { primaryExercise ->
                if (primaryExercise != null) {
                    workoutStageGenerator.generatePrilepinBasedScheme(
                        exercise = primaryExercise,
                        movementRole = "primary",
                        dayType = dayType,
                        oneRepMaxes = oneRepMaxes,
                        currentWeekNumber = currentWeekNumber,
                        userId = userId
                    )
                } else {
                    Mono.just(emptyList())
                }
            }

        // Process primary exercise and generate workout stages
        return primaryExerciseMono
            .flatMap { primaryExercise ->
                // Handle secondary exercise selection - for DE workouts, this might be empty
                val secondaryExerciseMono =
                    if (conjugateTemplates.hasSecondaryMovement(dayType) && primaryExercise != null) {
                        exerciseSelectionService.selectSimilarSecondaryExercise(
                            primaryExercise = primaryExercise,
                            userEquipment = userEquipment,
                            preferences = preferences,
                            exercises =
                                exerciseSelectionService.filterExercisesExcluding(
                                    exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                                    primaryExercise.name
                                ),
                            rotationHistory = rotationHistory
                        )
                    } else {
                        Mono.empty()
                    }

                // Handle the case where secondary exercise is empty - still create primary exercise stages
                secondaryExerciseMono
                    .flatMap { secondaryExercise ->
                        createWorkoutStagesWithSecondary(
                            primaryExercise,
                            secondaryExercise,
                            primarySetSchemesMono,
                            workout,
                            dayType,
                            exercises,
                            preferences,
                            userEquipment,
                            oneRepMaxes,
                            programPreferences,
                            weakMuscles,
                            currentWeekNumber,
                            userId
                        )
                    }
                    .switchIfEmpty(
                        // When secondary exercise is empty, still create primary exercise stages
                        Mono.defer {
                            createWorkoutStagesWithSecondary(
                                primaryExercise,
                                null,
                                primarySetSchemesMono,
                                workout,
                                dayType,
                                exercises,
                                preferences,
                                userEquipment,
                                oneRepMaxes,
                                programPreferences,
                                weakMuscles,
                                currentWeekNumber,
                                userId
                            )
                        }
                    )
            }
            .doOnError { error ->
                logger.error("Failed to generate workout: {}, {}, {}", workout.name, workout.id, error.message)
            }
    }

    /**
     * Creates workout stages with the given primary and secondary exercises.
     * This method ensures that each stage is created exactly once and in the correct order.
     */
    private fun createWorkoutStagesWithSecondary(
        primaryExercise: Exercise?,
        secondaryExercise: Exercise?,
        primarySetSchemesMono: Mono<List<SetSchemeParams>>,
        workout: ProgrammedWorkout,
        dayType: String,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: Int
    ): Mono<Void> {
        // Generate secondary set schemes for time calculation
        val secondarySetSchemesMono: Mono<List<SetSchemeParams>> =
            if (secondaryExercise != null) {
                workoutStageGenerator.generateSecondaryExerciseScheme(
                    exercise = secondaryExercise,
                    oneRepMaxes = oneRepMaxes,
                    userId = userId
                )
            } else {
                Mono.just(emptyList())
            }

        // Combine primary and secondary schemes to calculate accessory exercises
        return Mono.zip(primarySetSchemesMono, secondarySetSchemesMono)
            .flatMap { schemesTuple ->
                val primarySetSchemes = schemesTuple.t1
                val secondarySetSchemes = schemesTuple.t2

                // Calculate number of accessory exercises based on program preferences
                val numAccessoryExercises =
                    sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                        sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = primarySetSchemes,
                        secondarySetSchemes = secondarySetSchemes,
                        dayType = dayType
                    )

                // Create stages sequentially
                var currentMono: Mono<Void> = Mono.empty()

                // Add primary stage if exercise exists
                if (primaryExercise != null) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.PRIMARY,
                                    WorkoutStageTypeEnum.PRIMARY.position
                                )
                            }
                                .doOnError { error ->
                                    logger.error("Error creating primary stage: {}", error.message)
                                }
                                .flatMap { primaryStage ->
                                    workoutStageGenerator.createProgrammedExercise(primaryStage.id, primaryExercise.name)
                                        .flatMap { primaryProgrammedExercise ->
                                            workoutStageGenerator.createSetSchemes(
                                                userId,
                                                primaryProgrammedExercise.id,
                                                primaryExercise.name,
                                                primarySetSchemes
                                            )
                                        }
                                }
                                .then()
                        )
                }

                // Add secondary stage if exercise exists
                if (secondaryExercise != null) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.SECONDARY,
                                    WorkoutStageTypeEnum.SECONDARY.position
                                )
                            }
                                .flatMap { secondaryStage ->
                                    workoutStageGenerator.createProgrammedExercise(secondaryStage.id, secondaryExercise.name)
                                        .flatMap { secondaryProgrammedExercise ->
                                            workoutStageGenerator.createSetSchemes(
                                                userId,
                                                secondaryProgrammedExercise.id,
                                                secondaryExercise.name,
                                                secondarySetSchemes
                                            )
                                        }
                                }
                                .then()
                        )
                }

                // Create ONE accessory stage and add multiple exercises to it
                if (numAccessoryExercises > 0) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.ACCESSORY,
                                    WorkoutStageTypeEnum.ACCESSORY.position
                                )
                            }
                                .doOnError { error ->
                                    logger.error("Error creating accessory stage: {}", error.message)
                                }
                                .flatMap { accessoryStage ->
                                    // Add multiple accessory exercises to this single stage
                                    var exerciseMono: Mono<Void> = Mono.empty()
                                    for (accessoryIndex in 0 until numAccessoryExercises) {
                                        val finalAccessoryIndex = accessoryIndex
                                        exerciseMono =
                                            exerciseMono.then(
                                                Mono.defer {
                                                    exerciseSelectionService.selectRotatingExercise(
                                                        targetMuscles = weakMuscles,
                                                        userEquipment = userEquipment,
                                                        preferences = preferences,
                                                        exercises =
                                                            exerciseSelectionService
                                                                .filterExercisesByAccessoryStatus(exercises, true),
                                                        isAccessory = true,
                                                        rotationHistory = emptyList()
                                                    ).flatMap { accessoryExercise ->
                                                        if (accessoryExercise != null) {
                                                            workoutStageGenerator.createProgrammedExercise(
                                                                accessoryStage.id,
                                                                accessoryExercise.name
                                                            )
                                                                .flatMap { accessoryProgrammedExercise ->
                                                                    workoutStageGenerator.generatePrilepinBasedScheme(
                                                                        exercise = accessoryExercise,
                                                                        movementRole = "accessory",
                                                                        dayType = dayType,
                                                                        oneRepMaxes = oneRepMaxes,
                                                                        currentWeekNumber = currentWeekNumber,
                                                                        userId = userId
                                                                    ).flatMap { accessoryScheme ->
                                                                        workoutStageGenerator.createSetSchemes(
                                                                            userId,
                                                                            accessoryProgrammedExercise.id,
                                                                            accessoryExercise.name,
                                                                            accessoryScheme
                                                                        )
                                                                    }
                                                                }
                                                                .then()
                                                        } else {
                                                            Mono.empty()
                                                        }
                                                    }
                                                }
                                            )
                                    }
                                    exerciseMono
                                }
                        )
                }

                currentMono
                    .doOnError { error ->
                        logger.error("Error creating workout stages for workout '{}': {}", workout.id, error.message)
                    }
            }
    }
}
