package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
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
 * @property exerciseDAL Data access layer for exercise operations
 * @property userExercisePreferenceDAL Data access layer for user exercise preferences
 * @property userEquipmentDAL Data access layer for user equipment
 * @property userOneRepMaxDAL Data access layer for user one rep max values
 * @property userProgramPreferencesDAL Data access layer for user program preferences
 * @property exerciseRotationHistoryDAL Data access layer for exercise rotation history
 * @property programService Service for program operations
 * @property programmedWorkoutDAL Data access layer for programmed workout operations
 * @property conjugateTemplates Service for managing workout templates
 * @property workoutStageGenerationServiceFactory Factory for selecting appropriate workout stage generation services
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
    private val programService: ProgramService,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val conjugateTemplates: ConjugateTemplates,
    private val workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator,
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
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

        return programService.getProgramById(programId)
            .flatMap { program ->
                Mono.zip(
                    exerciseDAL.selectExercises(),
                    userExercisePreferenceDAL.selectUserExercisePreferencesByUser(program.userId),
                    userEquipmentDAL.selectUserEquipmentByUser(program.userId),
                    userOneRepMaxDAL.selectUserOneRepMaxByUser(program.userId),
                    userProgramPreferencesDAL.selectUserProgramPreferences(program.userId),
                    exerciseRotationHistoryDAL.selectAll(),
                    userWeakMuscleDAL.selectUserWeakMusclesByUser(program.userId)
                ).flatMap { tuple ->
                    val exercises = tuple.t1
                    val preferences = tuple.t2
                    val userEquipment = tuple.t3
                    val oneRepMaxes = tuple.t4
                    val programPreferences = tuple.t5
                    val rotationHistory = tuple.t6
                    val userWeakMuscles = tuple.t7

                    val weakMuscles =
                        if (userWeakMuscles.isNotEmpty()) {
                            userWeakMuscles.map { it.muscleName }
                        } else {
                            ConjugateConstants.DEFAULT_WEAK_MUSCLES
                        }
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
                        programService.updateProgram(
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
                        workoutStageGenerationOrchestrator.generateWorkoutStages(
                            workout = createdWorkout,
                            dayType = dayTemplate.type,
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
}
