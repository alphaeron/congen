package com.congen.generator

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
 * @param workoutStageGenerationServiceFactory Factory for selecting appropriate business logic service
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageGenerationOrchestrator(
    private val workoutStageGenerationServiceFactory: WorkoutStageGenerationServiceFactory,
) {
    /**
     * Generates complete workout stages for a given workout and day type using prepared data.
     *
     * This method orchestrates the workout stage generation process using pre-prepared data:
     * 1. Selects the appropriate business logic service based on program days
     * 2. Delegates to the service for exercise selection and stage planning using prepared data
     * 3. Returns the complete workout generation result with prepared data
     *
     * @param programId The program ID
     * @param dayNumber The day number
     * @param dayType The type of workout day (e.g., "ME_Upper", "DE_Lower")
     * @param preparedData The prepared data containing all required information
     * @return Mono containing the workout generation result
     */
    fun generateWorkoutStages(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        preparedData: WorkoutGenerationPreparedData,
    ): Mono<WorkoutGenerationResult> {
        // Get the appropriate business logic service based on program days
        val service =
            workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(
                programDaysPerWeek = preparedData.programPreferences.programDaysPerWeek
            )

        // Delegate to the service for business logic using prepared data
        return service.generateWorkoutStages(
            programId = programId,
            dayNumber = dayNumber,
            dayType = dayType,
            preparedData = preparedData
        )
    }
}
