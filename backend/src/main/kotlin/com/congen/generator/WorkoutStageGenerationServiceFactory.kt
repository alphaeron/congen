package com.congen.generator

import org.springframework.stereotype.Service

/**
 * Factory service for selecting the appropriate workout stage generation service
 * based on the program configuration.
 *
 * This service provides a centralized way to select the correct workout stage
 * generation service based on the number of training days per week, ensuring
 * that the appropriate algorithm is used for each program type.
 *
 * ## Supported Program Types
 *
 * - **2-day programs**: Combined ME+DE days
 * - **3-day programs**: Combined ME+DE days + Full Body DE day
 * - **4-day programs**: Traditional separate ME and DE days
 *
 * @property twoDayWorkoutStageGenerationService Service for 2-day program stage generation
 * @property threeDayWorkoutStageGenerationService Service for 3-day program stage generation
 * @property fourDayWorkoutStageGenerationService Service for 4-day program stage generation
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class WorkoutStageGenerationServiceFactory(
    private val twoDayWorkoutStageGenerationService: TwoDayWorkoutStageGenerationService,
    private val threeDayWorkoutStageGenerationService: ThreeDayWorkoutStageGenerationService,
    private val fourDayWorkoutStageGenerationService: FourDayWorkoutStageGenerationService,
) {
    /**
     * Gets the appropriate workout stage generation service based on the number of days per week.
     *
     * @param programDaysPerWeek The number of training days per week (2, 3, or 4)
     * @return The appropriate WorkoutStageGenerationService for the program type
     * @throws IllegalArgumentException if programDaysPerWeek is not 2, 3, or 4
     */
    fun getWorkoutStageGenerationService(programDaysPerWeek: Int): WorkoutStageGenerationService {
        return when (programDaysPerWeek) {
            2 -> twoDayWorkoutStageGenerationService
            3 -> threeDayWorkoutStageGenerationService
            4 -> fourDayWorkoutStageGenerationService
            else -> throw IllegalArgumentException("Number of days per week must be 2, 3, or 4")
        }
    }
}
