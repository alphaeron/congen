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
import com.congen.util.ValidationUtil
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
 * - **2-day programs**: Condensed conjugate approach (Phil Daru method)
 * - **3-day programs**: Traditional conjugate with ME/DE/accessory split
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
    private val sessionTimeCalculator: SessionTimeCalculator
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorService::class.java)
    }

    /**
     * Generates the next week of workouts for a user's conjugate powerlifting program.
     *
     * This method creates a complete week of workouts based on the conjugate method,
     * incorporating user preferences, available equipment, and exercise rotation history.
     *
     * @param userId The ID of the user
     * @param currentWeekNumber The current week number in the program
     * @return Mono containing the generated program with workouts
     * @throws com.congen.exceptions.ValidationException if the user's program contains an invalid number of days/week
     */
    fun generateNextWeek(
        userId: Int,
        currentWeekNumber: Int
    ): Mono<Program> {
        logger.info("Generating week {} for user {}", currentWeekNumber, userId)

        return Mono.zip(
            exerciseDAL.selectExercises(),
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId),
            userEquipmentDAL.selectUserEquipmentByUser(userId),
            userOneRepMaxDAL.selectUserOneRepMaxByUser(userId),
            userProgramPreferencesDAL.selectUserProgramPreferences(userId),
            exerciseRotationHistoryDAL.selectAll()
        ).flatMap { tuple ->
            val exercises = tuple.t1
            val preferences = tuple.t2
            val userEquipment = tuple.t3
            val oneRepMaxes = tuple.t4
            val programPreferences = tuple.t5
            val rotationHistory = tuple.t6
            val numDaysPerWeek = programPreferences.programDaysPerWeek

            ValidationUtil.validateProgramDaysPerWeek(numDaysPerWeek)
            val template = conjugateTemplates.selectTemplate(numDaysPerWeek)
            val weakMuscles = exerciseSelectionService.determineWeakMuscles(oneRepMaxes, rotationHistory)

            createProgram(userId, currentWeekNumber, numDaysPerWeek)
                .flatMap { program ->
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
                        currentWeekNumber = currentWeekNumber
                    ).thenReturn(program)
                }
        }
    }

    /**
     * Creates a new program for the user.
     */
    private fun createProgram(
        userId: Int,
        currentWeekNumber: Int,
        numDaysPerWeek: Int
    ): Mono<Program> {
        val programName = "Conjugate Powerlifting - Week $currentWeekNumber"
        return programDAL.insertProgram(userId, programName, currentWeekNumber)
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
        return template.foldIndexed(Mono.empty<Void>()) { dayIndex, mono, dayTemplate ->
            mono.flatMap {
                val dayNumber = (currentWeekNumber - 1) * template.size + dayIndex + 1
                programmedWorkoutDAL.insertProgrammedWorkout(program.id, dayNumber, "${dayTemplate.type} Day")
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
                            currentWeekNumber = currentWeekNumber
                        )
                    }
            }
        }
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
        currentWeekNumber: Int
    ): Mono<Void> {
        var stagePosition = 1

        // Primary movement stage (ME or DE exercise)
        val primaryExercise =
            exerciseSelectionService.selectRotatingExercise(
                userId = workout.programId.toInt(), // Using program ID as user ID for now
                targetMuscles = weakMuscles,
                userEquipment = userEquipment,
                preferences = preferences,
                // Only primary exercises (is_accessory = false)
                exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                isAccessory = false, // Primary movements are not accessories
                rotationHistory = rotationHistory
            )

        // Generate primary set schemes for time calculation
        val primarySetSchemes: List<SetSchemeParams> =
            if (primaryExercise != null) {
                workoutStageGenerator.generatePrilepinBasedScheme(
                    userId = workout.programId.toInt(),
                    exercise = primaryExercise,
                    movementRole = "primary",
                    dayType = dayTemplate.type,
                    oneRepMaxes = oneRepMaxes,
                    currentWeekNumber = currentWeekNumber
                )
            } else {
                emptyList()
            }

        // Secondary movement stage (if exists)
        val secondaryExercise =
            if (conjugateTemplates.hasSecondaryMovement(dayTemplate.type)) {
                exerciseSelectionService.selectRotatingExercise(
                    userId = workout.programId.toInt(),
                    targetMuscles = weakMuscles,
                    userEquipment = userEquipment,
                    preferences = preferences,
                    exercises =
                        exerciseSelectionService.filterExercisesExcluding(
                            exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                            primaryExercise?.name ?: ""
                        ),
                    // Only primary exercises that are different from primary
                    isAccessory = false, // Secondary movements are not accessories
                    rotationHistory = rotationHistory
                )
            } else {
                null
            }

        // Generate secondary set schemes for time calculation
        val secondarySetSchemes: List<SetSchemeParams> =
            if (secondaryExercise != null) {
                workoutStageGenerator.generateSecondaryExerciseScheme(
                    userId = workout.programId.toInt(),
                    exercise = secondaryExercise,
                    oneRepMaxes = oneRepMaxes
                )
            } else {
                emptyList()
            }

        // Calculate number of accessory exercises based on dynamic time calculation
        val sessionTimeMinutes = programPreferences.sessionTimeLengthInMinutes ?: ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES
        val numAccessoryExercises =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = secondarySetSchemes,
                dayType = dayTemplate.type
            )

        // Create primary movement stage
        var primaryMono =
            if (primaryExercise != null) {
                workoutStageGenerator.createWorkoutStage(workout.id, WorkoutStageTypeEnum.PRIMARY, stagePosition++)
                    .flatMap { primaryStage ->
                        workoutStageGenerator.createProgrammedExercise(primaryStage.id, primaryExercise.name)
                            .flatMap { primaryProgrammedExercise ->
                                workoutStageGenerator.createSetSchemes(primaryProgrammedExercise.id, primarySetSchemes)
                            }
                    }
            } else {
                Mono.empty<Void>()
            }

        // Create secondary movement stage
        var secondaryMono =
            if (secondaryExercise != null) {
                workoutStageGenerator.createWorkoutStage(
                    workout.id,
                    WorkoutStageTypeEnum.SECONDARY,
                    stagePosition++
                )
                    .flatMap { secondaryStage ->
                        workoutStageGenerator.createProgrammedExercise(secondaryStage.id, secondaryExercise.name)
                            .flatMap { secondaryProgrammedExercise ->
                                workoutStageGenerator.createSetSchemes(secondaryProgrammedExercise.id, secondarySetSchemes)
                            }
                    }
            } else {
                Mono.empty<Void>()
            }

        // Accessory movements (calculated based on dynamic time calculation)
        val accessoryMonos =
            (0 until numAccessoryExercises).map { accessoryIndex ->
                val accessoryExercise =
                    exerciseSelectionService.selectRotatingExercise(
                        userId = workout.programId.toInt(),
                        targetMuscles = weakMuscles,
                        userEquipment = userEquipment,
                        preferences = preferences,
                        // Only accessory exercises (is_accessory = true)
                        exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                        isAccessory = true, // Accessory movements are accessories
                        rotationHistory = rotationHistory
                    )

                if (accessoryExercise != null) {
                    workoutStageGenerator.createWorkoutStage(
                        workout.id,
                        WorkoutStageTypeEnum.ACCESSORY,
                        stagePosition++
                    )
                        .flatMap { accessoryStage ->
                            workoutStageGenerator.createProgrammedExercise(accessoryStage.id, accessoryExercise.name)
                                .flatMap { accessoryProgrammedExercise ->
                                    val accessoryScheme =
                                        workoutStageGenerator.generatePrilepinBasedScheme(
                                            userId = workout.programId.toInt(),
                                            exercise = accessoryExercise,
                                            movementRole = "accessory",
                                            dayType = dayTemplate.type,
                                            oneRepMaxes = oneRepMaxes,
                                            currentWeekNumber = currentWeekNumber
                                        )
                                    workoutStageGenerator.createSetSchemes(accessoryProgrammedExercise.id, accessoryScheme)
                                }
                        }
                } else {
                    Mono.empty<Void>()
                }
            }

        // Conditioning stage (optional) - only if there's time after accessories
        val conditioningMono =
            if (conjugateTemplates.hasConditioning(dayTemplate.type) && numAccessoryExercises > 0) {
                val conditioningExercise =
                    exerciseSelectionService.selectRotatingExercise(
                        userId = workout.programId.toInt(),
                        targetMuscles = listOf("full_body"),
                        userEquipment = userEquipment,
                        preferences = preferences,
                        // Use accessory exercises for conditioning (is_accessory = true)
                        exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                        isAccessory = true, // Conditioning uses accessory exercises
                        rotationHistory = rotationHistory
                    )

                if (conditioningExercise != null) {
                    workoutStageGenerator.createWorkoutStage(
                        workout.id,
                        WorkoutStageTypeEnum.CONDITIONING,
                        stagePosition
                    )
                        .flatMap { conditioningStage ->
                            workoutStageGenerator.createProgrammedExercise(conditioningStage.id, conditioningExercise.name)
                                .flatMap { conditioningProgrammedExercise ->
                                    val conditioningScheme =
                                        workoutStageGenerator.generateAmrapOrEmomScheme(
                                            userId = workout.programId.toInt(),
                                            exercise = conditioningExercise,
                                            oneRepMaxes = oneRepMaxes
                                        )
                                    workoutStageGenerator.createSetSchemes(conditioningProgrammedExercise.id, conditioningScheme)
                                }
                        }
                } else {
                    Mono.empty<Void>()
                }
            } else {
                Mono.empty<Void>()
            }

        // Combine all monos
        val allMonos = mutableListOf<Mono<Void>>()

        // Add non-empty monos
        if (primaryExercise != null) {
            allMonos.add(primaryMono)
        }
        if (secondaryExercise != null) {
            allMonos.add(secondaryMono)
        }
        if (conjugateTemplates.hasConditioning(dayTemplate.type) && numAccessoryExercises > 0) {
            val conditioningExercise =
                exerciseSelectionService.selectRotatingExercise(
                    userId = workout.programId.toInt(),
                    targetMuscles = listOf("full_body"),
                    userEquipment = userEquipment,
                    preferences = preferences,
                    exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                    isAccessory = true,
                    rotationHistory = rotationHistory
                )

            if (conditioningExercise != null) {
                allMonos.add(conditioningMono)
            }
        }

        // Add non-empty accessory monos
        allMonos.addAll(accessoryMonos.filter { it != Mono.empty<Void>() })

        return if (allMonos.isEmpty()) {
            Mono.empty<Void>()
        } else {
            Flux.concat(allMonos).then()
        }
    }
}
