package com.congen.generator

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
        template: List<com.congen.generator.DayTemplate>,
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
        dayTemplate: com.congen.generator.DayTemplate,
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

        return when {
            conjugateTemplates.isCombinedMEDay(dayType) -> {
                // Handle combined ME+DE days (2 and 3 day programs)
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
                // Handle full body DE day (3 day programs)
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
                // Handle traditional 4-day program structure
                generateTraditionalDay(
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
        }
    }

    /**
     * Generates workout stages for a combined ME+DE day (2 and 3 day programs).
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
        val primaryMovementType = conjugateTemplates.getPrimaryMovementType(dayType)
        val secondaryMovementType = conjugateTemplates.getSecondaryMovementType(dayType)

        // Select primary ME exercise
        val primaryExerciseMono =
            exerciseSelectionService.filterExercisesByWorkoutType(
                exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                "maximal_effort"
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

        // Select secondary DE exercise (plyometric or banded exercises)
        val secondaryExerciseMono =
            exerciseSelectionService.filterExercisesForDEWorkout(exercises)
                .flatMap { filteredExercises ->
                    exerciseSelectionService.selectRotatingExercise(
                        targetMuscles = weakMuscles,
                        userEquipment = userEquipment,
                        preferences = preferences,
                        exercises = filteredExercises,
                        isAccessory = false,
                        rotationHistory = rotationHistory
                    )
                }

        // Generate set schemes for both exercises
        val primarySetSchemesMono: Mono<List<SetSchemeParams>> =
            primaryExerciseMono.flatMap { primaryExercise ->
                if (primaryExercise != null) {
                    workoutStageGenerator.generatePrilepinBasedScheme(
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

        val secondarySetSchemesMono: Mono<List<SetSchemeParams>> =
            secondaryExerciseMono.flatMap { secondaryExercise ->
                if (secondaryExercise != null) {
                    workoutStageGenerator.generatePrilepinBasedScheme(
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
                    sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                        sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = primarySetSchemes,
                        secondarySetSchemes = secondarySetSchemes,
                        dayType = dayType
                    )

                // Create stages sequentially
                var currentMono: Mono<Void> = Mono.empty()

                // Create single primary stage with both ME and DE exercises
                if (primaryExercise != null || secondaryExercise != null) {
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
                                    var exerciseMono: Mono<Void> = Mono.empty()

                                    // Add primary ME exercise if it exists
                                    if (primaryExercise != null) {
                                        exerciseMono =
                                            exerciseMono.then(
                                                workoutStageGenerator.createProgrammedExercise(primaryStage.id, primaryExercise.name)
                                                    .flatMap { primaryProgrammedExercise ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            primaryProgrammedExercise.id,
                                                            primaryExercise.name,
                                                            primarySetSchemes
                                                        )
                                                    }
                                                    .then()
                                            )
                                    }

                                    // Add secondary DE exercise if it exists (in the same stage)
                                    if (secondaryExercise != null) {
                                        exerciseMono =
                                            exerciseMono.then(
                                                workoutStageGenerator.createProgrammedExercise(primaryStage.id, secondaryExercise.name)
                                                    .flatMap { secondaryProgrammedExercise ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            secondaryProgrammedExercise.id,
                                                            secondaryExercise.name,
                                                            secondarySetSchemes
                                                        )
                                                    }
                                                    .then()
                                            )
                                    }

                                    exerciseMono
                                }
                                .then()
                        )
                }

                // Create accessory stage if needed
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

                // Create conditioning stage for dynamic effort workouts
                if (conjugateTemplates.hasConditioning(dayType)) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.CONDITIONING,
                                    WorkoutStageTypeEnum.CONDITIONING.position
                                )
                            }
                                .doOnError { error ->
                                    logger.error("Error creating conditioning stage: {}", error.message)
                                }
                                .flatMap { conditioningStage ->
                                    // Select a conditioning exercise
                                    exerciseSelectionService.selectRotatingExercise(
                                        targetMuscles = weakMuscles,
                                        userEquipment = userEquipment,
                                        preferences = preferences,
                                        exercises =
                                            exerciseSelectionService
                                                .filterExercisesByAccessoryStatus(exercises, true),
                                        isAccessory = true,
                                        rotationHistory = emptyList()
                                    ).flatMap { conditioningExercise ->
                                        if (conditioningExercise != null) {
                                            workoutStageGenerator.createProgrammedExercise(
                                                conditioningStage.id,
                                                conditioningExercise.name
                                            )
                                                .flatMap { conditioningProgrammedExercise ->
                                                    workoutStageGenerator.generateAmrapOrEmomScheme(
                                                        exercise = conditioningExercise,
                                                        oneRepMaxes = oneRepMaxes,
                                                        userId = userId
                                                    ).flatMap { conditioningScheme ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            conditioningProgrammedExercise.id,
                                                            conditioningExercise.name,
                                                            conditioningScheme
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

                currentMono
                    .doOnError { error ->
                        logger.error("Error creating combined ME+DE workout stages for workout '{}': {}", workout.id, error.message)
                    }
            }
    }

    /**
     * Generates workout stages for a full body DE day (3 day programs).
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
        // Select upper body DE exercise (plyometric or banded exercises)
        val upperDEExerciseMono =
            exerciseSelectionService.filterExercisesForDEWorkout(exercises)
                .map { filteredExercises -> filteredExercises.filter { it.isUpper } }
                .flatMap { upperExercises ->
                    exerciseSelectionService.selectRotatingExercise(
                        targetMuscles = weakMuscles,
                        userEquipment = userEquipment,
                        preferences = preferences,
                        exercises = upperExercises,
                        isAccessory = false,
                        rotationHistory = rotationHistory
                    )
                }

        // Select lower body DE exercise (plyometric or banded exercises)
        val lowerDEExerciseMono =
            exerciseSelectionService.filterExercisesForDEWorkout(exercises)
                .map { filteredExercises -> filteredExercises.filter { !it.isUpper } }
                .flatMap { lowerExercises ->
                    exerciseSelectionService.selectRotatingExercise(
                        targetMuscles = weakMuscles,
                        userEquipment = userEquipment,
                        preferences = preferences,
                        exercises = lowerExercises,
                        isAccessory = false,
                        rotationHistory = rotationHistory
                    )
                }

        // Generate set schemes for both exercises
        val upperDESetSchemesMono: Mono<List<SetSchemeParams>> =
            upperDEExerciseMono.flatMap { upperExercise ->
                if (upperExercise != null) {
                    workoutStageGenerator.generatePrilepinBasedScheme(
                        exercise = upperExercise,
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

        val lowerDESetSchemesMono: Mono<List<SetSchemeParams>> =
            lowerDEExerciseMono.flatMap { lowerExercise ->
                if (lowerExercise != null) {
                    workoutStageGenerator.generatePrilepinBasedScheme(
                        exercise = lowerExercise,
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

        // Create workout stages with both exercises in the same primary stage
        return Mono.zip(upperDEExerciseMono, lowerDEExerciseMono, upperDESetSchemesMono, lowerDESetSchemesMono)
            .flatMap { tuple ->
                val upperExercise = tuple.t1
                val lowerExercise = tuple.t2
                val upperSetSchemes = tuple.t3
                val lowerSetSchemes = tuple.t4

                // Calculate number of accessory exercises based on program preferences
                val numAccessoryExercises =
                    sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                        sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes,
                        primarySetSchemes = upperSetSchemes,
                        secondarySetSchemes = lowerSetSchemes,
                        dayType = "DE_Full_Body"
                    )

                // Create stages sequentially
                var currentMono: Mono<Void> = Mono.empty()

                // Create single primary stage with both upper and lower DE exercises
                if (upperExercise != null || lowerExercise != null) {
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
                                    var exerciseMono: Mono<Void> = Mono.empty()

                                    // Add upper DE exercise if it exists
                                    if (upperExercise != null) {
                                        exerciseMono =
                                            exerciseMono.then(
                                                workoutStageGenerator.createProgrammedExercise(primaryStage.id, upperExercise.name)
                                                    .flatMap { upperProgrammedExercise ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            upperProgrammedExercise.id,
                                                            upperExercise.name,
                                                            upperSetSchemes
                                                        )
                                                    }
                                                    .then()
                                            )
                                    }

                                    // Add lower DE exercise if it exists (in the same stage)
                                    if (lowerExercise != null) {
                                        exerciseMono =
                                            exerciseMono.then(
                                                workoutStageGenerator.createProgrammedExercise(primaryStage.id, lowerExercise.name)
                                                    .flatMap { lowerProgrammedExercise ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            lowerProgrammedExercise.id,
                                                            lowerExercise.name,
                                                            lowerSetSchemes
                                                        )
                                                    }
                                                    .then()
                                            )
                                    }

                                    exerciseMono
                                }
                                .then()
                        )
                }

                // Create accessory stage if needed
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
                                                                        dayType = "DE_Full_Body",
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

                // Create conditioning stage for dynamic effort workouts
                if (conjugateTemplates.hasConditioning("DE_Full_Body")) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.CONDITIONING,
                                    WorkoutStageTypeEnum.CONDITIONING.position
                                )
                            }
                                .doOnError { error ->
                                    logger.error("Error creating conditioning stage: {}", error.message)
                                }
                                .flatMap { conditioningStage ->
                                    // Select a conditioning exercise
                                    exerciseSelectionService.selectRotatingExercise(
                                        targetMuscles = weakMuscles,
                                        userEquipment = userEquipment,
                                        preferences = preferences,
                                        exercises =
                                            exerciseSelectionService
                                                .filterExercisesByAccessoryStatus(exercises, true),
                                        isAccessory = true,
                                        rotationHistory = emptyList()
                                    ).flatMap { conditioningExercise ->
                                        if (conditioningExercise != null) {
                                            workoutStageGenerator.createProgrammedExercise(
                                                conditioningStage.id,
                                                conditioningExercise.name
                                            )
                                                .flatMap { conditioningProgrammedExercise ->
                                                    workoutStageGenerator.generateAmrapOrEmomScheme(
                                                        exercise = conditioningExercise,
                                                        oneRepMaxes = oneRepMaxes,
                                                        userId = userId
                                                    ).flatMap { conditioningScheme ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            conditioningProgrammedExercise.id,
                                                            conditioningExercise.name,
                                                            conditioningScheme
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

                currentMono
                    .doOnError { error ->
                        logger.error("Error creating full body DE workout stages for workout '{}': {}", workout.id, error.message)
                    }
            }
    }

    /**
     * Generates workout stages for a traditional 4-day program structure.
     */
    private fun generateTraditionalDay(
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

        // Select primary exercise
        val primaryExerciseMono =
            if (workoutType == "dynamic_effort") {
                // For DE workouts, use the special DE filter that includes plyometric exercises
                exerciseSelectionService.filterExercisesForDEWorkout(exercises)
                    .flatMap { filteredExercises ->
                        exerciseSelectionService.selectRotatingExercise(
                            targetMuscles = weakMuscles,
                            userEquipment = userEquipment,
                            preferences = preferences,
                            exercises = filteredExercises,
                            isAccessory = false,
                            rotationHistory = rotationHistory
                        )
                    }
            } else {
                // For ME workouts, use the standard filter
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
                logger.error("Failed to generate traditional workout: {}, {}, {}", workout.name, workout.id, error.message)
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

                // Create conditioning stage for dynamic effort workouts
                if (conjugateTemplates.hasConditioning(dayType)) {
                    currentMono =
                        currentMono.then(
                            Mono.defer {
                                workoutStageGenerator.createWorkoutStage(
                                    workout.id,
                                    WorkoutStageTypeEnum.CONDITIONING,
                                    WorkoutStageTypeEnum.CONDITIONING.position
                                )
                            }
                                .doOnError { error ->
                                    logger.error("Error creating conditioning stage: {}", error.message)
                                }
                                .flatMap { conditioningStage ->
                                    // Select a conditioning exercise
                                    exerciseSelectionService.selectRotatingExercise(
                                        targetMuscles = weakMuscles,
                                        userEquipment = userEquipment,
                                        preferences = preferences,
                                        exercises =
                                            exerciseSelectionService
                                                .filterExercisesByAccessoryStatus(exercises, true),
                                        isAccessory = true,
                                        rotationHistory = emptyList()
                                    ).flatMap { conditioningExercise ->
                                        if (conditioningExercise != null) {
                                            workoutStageGenerator.createProgrammedExercise(
                                                conditioningStage.id,
                                                conditioningExercise.name
                                            )
                                                .flatMap { conditioningProgrammedExercise ->
                                                    workoutStageGenerator.generateAmrapOrEmomScheme(
                                                        exercise = conditioningExercise,
                                                        oneRepMaxes = oneRepMaxes,
                                                        userId = userId
                                                    ).flatMap { conditioningScheme ->
                                                        workoutStageGenerator.createSetSchemes(
                                                            userId,
                                                            conditioningProgrammedExercise.id,
                                                            conditioningExercise.name,
                                                            conditioningScheme
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

                currentMono
                    .doOnError { error ->
                        logger.error("Error creating workout stages for workout '{}': {}", workout.id, error.message)
                    }
            }
    }
}
