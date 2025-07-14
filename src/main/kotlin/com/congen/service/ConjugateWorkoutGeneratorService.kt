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

                    // Get the next week number from the program
                    val nextWeekNumber = program.currentWeekNumber + 1

                    // Validate program days per week
                    ValidationUtil.validateProgramDaysPerWeek(programPreferences.programDaysPerWeek)

                    // Get conjugate template based on program days per week
                    val template = conjugateTemplates.selectTemplate(programPreferences.programDaysPerWeek)

                    // Determine weak muscles based on user preferences and history
                    val weakMuscles = exerciseSelectionService.determineWeakMuscles()

                    // Generate workouts for the week
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
                        currentWeekNumber = nextWeekNumber
                    ).then(
                        programDAL.updateProgram(
                            id = program.id,
                            name = "Conjugate Powerlifting - Week $nextWeekNumber",
                            currentWeekNumber = nextWeekNumber,
                            isActive = program.isActive
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
            .flatMap { tuple ->
                val dayIndex = tuple.t1
                val dayTemplate = tuple.t2
                val dayNumber = (currentWeekNumber - 1) * template.size + dayIndex.toInt() + 1

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
                            currentWeekNumber = currentWeekNumber,
                            userId = program.userId
                        )
                    }
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
        var stagePosition = 1

        // Primary movement stage (ME or DE exercise)
        val primaryExercise =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles = weakMuscles,
                userEquipment = userEquipment,
                preferences = preferences,
                // Only primary exercises (is_accessory = false)
                exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                // Primary movements are not accessories
                isAccessory = false,
                rotationHistory = rotationHistory
            )

        // Generate primary set schemes for time calculation
        val primarySetSchemes: List<SetSchemeParams> =
            if (primaryExercise != null) {
                workoutStageGenerator.generatePrilepinBasedScheme(
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
                    targetMuscles = weakMuscles,
                    userEquipment = userEquipment,
                    preferences = preferences,
                    exercises =
                        exerciseSelectionService.filterExercisesExcluding(
                            exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false),
                            primaryExercise?.name ?: ""
                        ),
                    // Only primary exercises that are different from primary
                    // Secondary movements are not accessories
                    isAccessory = false,
                    rotationHistory = rotationHistory
                )
            } else {
                null
            }

        // Generate secondary set schemes for time calculation
        val secondarySetSchemes: List<SetSchemeParams> =
            if (secondaryExercise != null) {
                workoutStageGenerator.generateSecondaryExerciseScheme(
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
                                workoutStageGenerator.createSetSchemes(
                                    userId,
                                    primaryProgrammedExercise.id,
                                    primaryExercise.name,
                                    primarySetSchemes
                                )
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
                                workoutStageGenerator.createSetSchemes(
                                    userId,
                                    secondaryProgrammedExercise.id,
                                    secondaryExercise.name,
                                    secondarySetSchemes
                                )
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
                        targetMuscles = weakMuscles,
                        userEquipment = userEquipment,
                        preferences = preferences,
                        // Only accessory exercises (is_accessory = true)
                        exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                        // Accessory movements are accessories
                        isAccessory = true,
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
                                            exercise = accessoryExercise,
                                            movementRole = "accessory",
                                            dayType = dayTemplate.type,
                                            oneRepMaxes = oneRepMaxes,
                                            currentWeekNumber = currentWeekNumber
                                        )
                                    workoutStageGenerator.createSetSchemes(
                                        userId,
                                        accessoryProgrammedExercise.id,
                                        accessoryExercise.name,
                                        accessoryScheme
                                    )
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
                        targetMuscles = listOf("full_body"),
                        userEquipment = userEquipment,
                        preferences = preferences,
                        // Use accessory exercises for conditioning (is_accessory = true)
                        exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                        // Conditioning uses accessory exercises
                        isAccessory = true,
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
                                            exercise = conditioningExercise,
                                            oneRepMaxes = oneRepMaxes
                                        )
                                    workoutStageGenerator.createSetSchemes(
                                        userId,
                                        conditioningProgrammedExercise.id,
                                        conditioningExercise.name,
                                        conditioningScheme
                                    )
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
                    targetMuscles = listOf("full_body"),
                    userEquipment = userEquipment,
                    preferences = preferences,
                    exercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true),
                    // Conditioning uses accessory exercises
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
