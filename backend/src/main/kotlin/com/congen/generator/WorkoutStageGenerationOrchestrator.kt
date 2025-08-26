package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.ProgrammedWorkout
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
    private val workoutStageGenerationServiceFactory: WorkoutStageGenerationServiceFactory,
) {
    /**
     * Generates complete workout stages for a given workout and day type.
     *
     * This method orchestrates the entire workout stage generation process:
     * 1. Creates a UserExercisePool for thread-safe exercise management
     * 2. Selects the appropriate business logic service based on program days
     * 3. Delegates to the service for exercise selection and stage planning
     * 4. Uses the infrastructure layer for actual creation of stages, exercises, and set schemes
     *
     * @param workout The workout to generate stages for
     * @param dayType The type of workout day (e.g., "ME_Upper", "DE_Lower")
     * @param exercises Available exercises for selection
     * @param preferences User exercise preferences
     * @param userEquipment User's available equipment
     * @param oneRepMaxes User's one rep max values
     * @param programPreferences User's program preferences
     * @param weakMuscles User's weak muscle groups
     * @param currentWeekNumber Current week in the program
     * @param userId User ID
     * @return Mono indicating completion
     */
    fun generateWorkoutStages(
        workout: ProgrammedWorkout,
        dayType: String,
        userExercisePool: UserExercisePool,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        weakMuscles: List<String>,
        currentWeekNumber: Int,
        userId: String,
    ): Mono<Void> {
        // Get the appropriate business logic service based on program days
        val service =
            workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(
                programDaysPerWeek = programPreferences.programDaysPerWeek
            )

        // Delegate to the service for business logic (exercise selection, stage planning)
        return service.generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId,
        )
    }
}
