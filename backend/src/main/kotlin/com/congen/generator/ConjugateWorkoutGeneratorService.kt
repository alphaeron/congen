package com.congen.generator

import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.model.Program
import com.congen.model.ProgramPreferences
import com.congen.model.SetScheme
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.service.ProgramService
import com.congen.service.SetSchemeService
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
 * @param userOneRepMaxDAL Data access layer for user one rep max values
 * @param programPreferencesDAL Data access layer for program preferences
 * @param programService Service for program operations
 * @param programmedWorkoutDAL Data access layer for programmed workout operations
 * @param conjugateTemplates Service for managing workout templates
 * @param workoutStageGenerationOrchestrator Service for orchestrating workout stage generation
 * @param userWeakMuscleDAL Data access layer for user weak muscle data
 * @param exercisePoolFactory Service for managing exercise pools and filtering
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ConjugateWorkoutGeneratorService(
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val programPreferencesDAL: ProgramPreferencesDAL,
    private val programService: ProgramService,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val conjugateTemplates: ConjugateTemplates,
    private val workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator,
    private val atomicWorkoutWriter: AtomicWorkoutWriter,
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
    private val exercisePoolFactory: ExercisePoolFactory,
    private val setSchemeService: SetSchemeService,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorService::class.java)
    }

    /**
     * Generates the next week of workouts for an existing conjugate powerlifting program.
     *
     * This method generates a complete week of workouts for an existing program based on the conjugate method,
     * incorporating user preferences and available equipment.
     *
     * @param programId The ID of the existing program
     * @return Mono containing the updated program with new workouts
     * @throws ValidationException if the user's program contains an invalid number of days/week
     * @throws NoResultsFoundException if the program is not found
     */
    fun generateNextWeek(programId: Long): Mono<Program> {
        logger.info("Generating next week for program {}", programId)

        return programService.selectProgramById(programId)
            .flatMap { program ->
                Mono.zip(
                    userOneRepMaxDAL.selectUserOneRepMaxByUser(program.userId),
                    programPreferencesDAL.selectProgramPreferences(program.id),
                    userWeakMuscleDAL.selectUserWeakMusclesByUser(program.userId)
                ).flatMap { tuple ->
                    val oneRepMaxes = tuple.t1
                    val programPreferences = tuple.t2
                    val userWeakMuscles = tuple.t3

                    val weakMuscles =
                        if (userWeakMuscles.isNotEmpty()) {
                            userWeakMuscles.map { it.muscleName }
                        } else {
                            ConjugateConstants.DEFAULT_WEAK_MUSCLES
                        }
                    val template = conjugateTemplates.selectTemplate(programPreferences.programDaysPerWeek)

                    exercisePoolFactory.createPoolForUser(program.userId)
                        .flatMap { userExercisePool ->
                            generateWorkoutsForWeek(
                                program = program,
                                userExercisePool = userExercisePool,
                                oneRepMaxes = oneRepMaxes,
                                programPreferences = programPreferences,
                                template = template,
                                weakMuscles = weakMuscles,
                                currentWeekNumber = program.currentWeekNumber
                            ).then(
                                programService.updateProgram(
                                    program.id,
                                    program.name,
                                    program.currentWeekNumber + 1,
                                    program.isActive
                                )
                            )
                        }
                }
            }
    }

    /**
     * Generates workouts for the specified week.
     *
     * This method processes each workout day sequentially, ensuring that exercise selection
     * is properly tracked and no duplicates occur within the week.
     *
     * @param program The program to generate workouts for
     * @param userExercisePool The user's exercise pool for the week
     * @param oneRepMaxes User's one rep max values
     * @param programPreferences User's program preferences
     * @param template The workout template for the week
     * @param weakMuscles Target weak muscles
     * @param currentWeekNumber Current week number
     * @return Mono<Void> indicating completion
     */
    private fun generateWorkoutsForWeek(
        program: Program,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: ProgramPreferences,
        template: List<DayTemplate>,
        weakMuscles: List<String>,
        currentWeekNumber: Int
    ): Mono<Void> {
        logger.info("Generating workouts atomically for program {}", program.id)
        
        return Flux.fromIterable(template)
            .index()
            .concatMap { tuple ->
                val dayIndex = tuple.t1
                val dayTemplate = tuple.t2
                val dayNumber = currentWeekNumber * template.size + dayIndex.toInt() + 1

                logger.debug("Generating workout for day {} of program {}", dayNumber, program.id)

                // Generate workout stages and write everything atomically
                workoutStageGenerationOrchestrator.generateWorkoutStages(
                    programId = program.id,
                    dayNumber = dayNumber,
                    dayType = dayTemplate.type,
                    userExercisePool = userExercisePool,
                    oneRepMaxes = oneRepMaxes,
                    programPreferences = programPreferences,
                    weakMuscles = weakMuscles,
                    currentWeekNumber = currentWeekNumber,
                    userId = program.userId
                ).flatMap { workoutResult ->
                    // Write all workout data atomically (including programmed workout creation)
                    atomicWorkoutWriter.writeWorkoutAtomically(workoutResult)
                }.doOnError { error ->
                    logger.error("Error generating workout for day {}: {}", dayNumber, error.message)
                }
            }
            .doOnError { error ->
                logger.error("Error generating workouts for week: {}", error.message)
            }
            .then()
    }


    /**
     * Updates a generated workout with user's 1RM data to tailor weights appropriately.
     *
     * This method takes 1RM input data and updates the workout to reflect the user's
     * current abilities by adjusting weights for programmed sets/reps.
     *
     * @param programId The ID of the program to update
     * @return Mono containing the updated program
     * @throws ValidationException if the input data is invalid
     * @throws NoResultsFoundException if the program is not found
     */
    fun updateWorkoutWithOneRepMax(
        programId: Long
    ): Mono<Program> {
        logger.info("Updating workout with 1RM data for program {}", programId)

        return programService.selectProgramById(programId)
            .flatMap { program ->
                // Get existing 1RM values for the user
                userOneRepMaxDAL.selectUserOneRepMaxByUser(program.userId)
                    .map { existingOneRepMaxes ->
                        // Create a map of exercise names to their 1RM values
                        existingOneRepMaxes.associate { it.exerciseName to it.oneRepMax.toDouble() }
                    }
                    .flatMap { oneRepMaxValues ->
                        // Get all set schemes for this program and update their target weights
                        updateSetSchemeWeightsForProgram(programId, oneRepMaxValues, program.userId)
                            .then(Mono.just(program))
                    }
            }
            .doOnError { error ->
                logger.error("Error updating workout with 1RM data for program {}: {}", programId, error.message)
            }
    }

    /**
     * Updates set scheme target weights for all exercises in a program based on 1RM percentages.
     *
     * @param programId The ID of the program
     * @param oneRepMaxValues Map of exercise names to their 1RM values
     * @return Mono indicating completion
     */
    private fun updateSetSchemeWeightsForProgram(
        programId: Long,
        oneRepMaxValues: Map<String, Double>,
        userId: String
    ): Mono<Void> {
        logger.info("Updating set scheme weights for program {} with {} 1RM values", programId, oneRepMaxValues.size)

        return setSchemeDAL.selectSetSchemesByProgramId(programId)
            .flatMap { setSchemes ->
                if (setSchemes.isEmpty()) {
                    logger.info("No set schemes found for program {}", programId)
                    return@flatMap Mono.empty<Void>()
                }

                // Process each set scheme individually to update target weights

                // Update each set scheme with new target weight based on 1RM percentage
                Flux.fromIterable(setSchemes)
                    .flatMap { setScheme ->
                        updateSetSchemeWeight(setScheme, oneRepMaxValues, userId)
                    }
                    .then()
            }
            .doOnError { error ->
                logger.error("Error updating set scheme weights for program {}: {}", programId, error.message)
            }
    }

    /**
     * Updates a single set scheme's target weight based on 1RM percentage.
     *
     * @param setScheme The set scheme to update
     * @param oneRepMaxValues Map of exercise names to their 1RM values
     * @return Mono indicating completion
     */
    private fun updateSetSchemeWeight(
        setScheme: SetScheme,
        oneRepMaxValues: Map<String, Double>,
        userId: String
    ): Mono<Void> {
        // Get the exercise name from the programmed exercise
        return programmedExerciseDAL.selectProgrammedExerciseById(setScheme.programmedExerciseId)
            .flatMap { programmedExercise ->
                val exerciseName = programmedExercise.exerciseName
                val oneRepMax = oneRepMaxValues[exerciseName]

                if (oneRepMax == null) {
                    logger.debug("No 1RM found for exercise {}, skipping weight update", exerciseName)
                    return@flatMap Mono.empty<Void>()
                }

                // Calculate target weight based on 1RM percentage
                // For now, we'll use a default percentage based on the set number
                // In a more sophisticated implementation, this would be based on the workout type and set scheme
                val percentage = calculateTargetPercentage(setScheme)
                val newTargetWeight = (oneRepMax * percentage).toBigDecimal()

                logger.debug("Updating set scheme {} for exercise {}: {}% of {}kg = {}kg", 
                    setScheme.id, exerciseName, (percentage * 100).toInt(), oneRepMax, newTargetWeight)

                // Get the user's exercise unit preference and update the set scheme
                getWeightUnitForExercise(userId, exerciseName)
                    .flatMap { weightUnit ->
                        setSchemeService.updateSetSchemeWithUnit(
                            id = setScheme.id,
                            programmedExerciseId = setScheme.programmedExerciseId,
                            setNumber = setScheme.setNumber,
                            isAmrap = setScheme.isAmrap,
                            isEmom = setScheme.isEmom,
                            useTempo = setScheme.useTempo,
                            eccentricTempo = setScheme.eccentricTempo,
                            isometricTempo = setScheme.isometricTempo,
                            concentricTempo = setScheme.concentricTempo,
                            targetWeight = newTargetWeight.toString(),
                            performedWeight = setScheme.performedWeight?.toString(),
                            targetRepCount = setScheme.targetRepCount,
                            performedRepCount = setScheme.performedRepCount,
                            restSeconds = setScheme.restSeconds,
                            unit = weightUnit.name, // Use user's preferred unit for this exercise
                            band = setScheme.band
                        )
                    }
                    .then()
            }
            .onErrorResume { error ->
                logger.error("Error updating set scheme {}: {}", setScheme.id, error.message)
                Mono.empty<Void>()
            }
    }

    /**
     * Calculates the target percentage of 1RM based on set scheme characteristics.
     *
     * @param setScheme The set scheme to calculate percentage for
     * @return The percentage as a decimal (e.g., 0.85 for 85%)
     */
    private fun calculateTargetPercentage(setScheme: SetScheme): Double {
        // This is a simplified implementation
        // In a more sophisticated system, this would be based on:
        // - Workout type (ME vs DE vs accessory)
        // - Set number and rep count
        // - Exercise type and movement pattern
        // - Program phase and periodization

        return when {
            setScheme.isAmrap -> 0.75 // AMRAP sets typically at 75% 1RM
            setScheme.isEmom -> 0.70 // EMOM sets typically at 70% 1RM
            setScheme.targetRepCount != null -> {
                // Base percentage on rep count (simplified Prilepin table)
                when (setScheme.targetRepCount) {
                    1 -> 0.95
                    2 -> 0.90
                    3 -> 0.85
                    4, 5 -> 0.80
                    6, 7 -> 0.75
                    8, 9 -> 0.70
                    10, 11 -> 0.65
                    else -> 0.60
                }
            }
            else -> 0.75 // Default to 75% if no other indicators
        }
    }

    /**
     * Gets the weight unit preference for an exercise.
     *
     * @param userId The user ID for weight unit preferences
     * @param exerciseName The name of the exercise
     * @return Mono containing the weight unit preference, defaulting to KG if not found
     */
    private fun getWeightUnitForExercise(
        userId: String,
        exerciseName: String
    ): Mono<WeightUnit> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
            .map { it.preferredUnit }
            .switchIfEmpty(Mono.just(WeightUnit.KG))
            .onErrorResume {
                logger.debug("No weight unit preference found for user {} and exercise {}, using KG", userId, exerciseName)
                Mono.just(WeightUnit.KG)
            }
    }
}
