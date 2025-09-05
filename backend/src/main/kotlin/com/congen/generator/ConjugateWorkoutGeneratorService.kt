package com.congen.generator

import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.Program
import com.congen.model.ProgramPreferences
import com.congen.model.UserOneRepMax
import com.congen.service.ProgramService
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
    private val conjugateTemplates: ConjugateTemplates,
    private val workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator,
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
    private val exercisePoolFactory: ExercisePoolFactory,
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
        return Flux.fromIterable(template)
            .index()
            .concatMap { tuple ->
                val dayIndex = tuple.t1
                val dayTemplate = tuple.t2
                val dayNumber = currentWeekNumber * template.size + dayIndex.toInt() + 1

                programmedWorkoutDAL.insertProgrammedWorkout(program.id, dayNumber, dayTemplate.type)
                    .doOnError { error ->
                        logger.error("Error inserting programmed workout: {}", error.message)
                    }
                    .flatMap { createdWorkout ->
                        workoutStageGenerationOrchestrator.generateWorkoutStages(
                            workout = createdWorkout,
                            dayType = dayTemplate.type,
                            userExercisePool = userExercisePool,
                            oneRepMaxes = oneRepMaxes,
                            programPreferences = programPreferences,
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
}
