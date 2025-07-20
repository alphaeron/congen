package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.service.SetSchemeService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Central orchestrator for workout stage generation that coordinates between
 * the infrastructure layer (DALs and services) and business logic layer
 * (modular workout stage generation services).
 *
 * This class serves as the unified entry point for workout stage generation,
 * eliminating duplication and providing a clean separation of concerns:
 * - Infrastructure concerns: handled by DALs and services
 * - Business logic concerns: handled by modular services
 * - Orchestration concerns: handled by this class
 *
 * @property workoutStageDAL Data access layer for workout stage operations
 * @property workoutStageTypeDAL Data access layer for workout stage type operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property setSchemeDAL Data access layer for set scheme operations
 * @property setSchemeService Service for set scheme operations
 * @property prilepinGuidelinesService Service for Prilepin-based guidelines
 * @property weightSelectionService Service for conjugate-specific weight selection
 * @property userWeightUnitPreferenceDAL Data access layer for user weight unit preferences
 * @property workoutStageGenerationServiceFactory Factory for selecting appropriate business logic service
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageGenerationOrchestrator(
    private val workoutStageDAL: WorkoutStageDAL,
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val setSchemeService: SetSchemeService,
    private val prilepinGuidelinesService: PrilepinGuidelinesService,
    private val weightSelectionService: WeightSelectionService,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val workoutStageGenerationServiceFactory: WorkoutStageGenerationServiceFactory
) {
    /**
     * Generates complete workout stages for a given workout and day type.
     *
     * This method orchestrates the entire workout stage generation process:
     * 1. Selects the appropriate business logic service based on program days
     * 2. Delegates to the service for exercise selection and stage planning
     * 3. Uses the infrastructure layer for actual creation of stages, exercises, and set schemes
     *
     * @param workout The workout to generate stages for
     * @param dayType The type of workout day (e.g., "ME_Upper", "DE_Lower")
     * @param exercises Available exercises for selection
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param oneRepMaxes User's one rep max values
     * @param programPreferences User's program preferences
     * @param rotationHistory Exercise rotation history
     * @param weakMuscles User's weak muscle groups
     * @param currentWeekNumber Current week in the program
     * @param userId User ID
     * @return Flux of created workout stages
     */
    fun generateWorkoutStages(
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
        // Get the appropriate business logic service based on program days
        val service = workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(programPreferences.programDaysPerWeek)

        // Delegate to the service for business logic (exercise selection, stage planning)
        return service.generateWorkoutStages(
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
