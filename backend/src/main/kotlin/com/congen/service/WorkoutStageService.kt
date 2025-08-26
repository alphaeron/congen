package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.model.WorkoutStage
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for managing workout stage operations.
 *
 * This service is a thin wrapper around [WorkoutStageDAL], exposing the same methods
 * for use by controllers and other services.
 *
 * @param workoutStageDAL Data access layer for workout stage operations
 * @param programmedWorkoutDAL Data access layer for programmed workout operations
 * @param programDAL Data access layer for program operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class WorkoutStageService(
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val programDAL: ProgramDAL
) {
    /**
     * Retrieves a workout stage by its unique identifier.
     * @see WorkoutStageDAL.selectWorkoutStageById
     */
    fun selectWorkoutStageById(id: Long): Mono<WorkoutStage> = workoutStageDAL.selectWorkoutStageById(id)

    /**
     * Retrieves all workout stages for a specific programmed workout.
     * @see WorkoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId
     */
    fun selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId: Long): Mono<List<WorkoutStage>> =
        workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId)

    /**
     * Retrieves all workout stages from the database.
     * @see WorkoutStageDAL.selectWorkoutStages
     */
    fun selectWorkoutStages(): Mono<List<WorkoutStage>> = workoutStageDAL.selectWorkoutStages()

    /**
     * Retrieves all workout stages owned by a specific user.
     * @see WorkoutStageDAL.selectWorkoutStagesByUserId
     */
    fun selectWorkoutStagesByUserId(userId: String): Mono<List<WorkoutStage>> = workoutStageDAL.selectWorkoutStagesByUserId(userId)

    /**
     * Inserts a new workout stage into the database.
     * @see WorkoutStageDAL.insertWorkoutStage
     */
    fun insertWorkoutStage(
        programmedWorkoutId: Long,
        stageTypeId: Int,
        position: Int,
        name: String
    ): Mono<WorkoutStage> = workoutStageDAL.insertWorkoutStage(programmedWorkoutId, stageTypeId, position, name)

    /**
     * Updates an existing workout stage in the database.
     * @see WorkoutStageDAL.updateWorkoutStage
     */
    fun updateWorkoutStage(
        id: Long,
        programmedWorkoutId: Long,
        stageTypeId: Int,
        position: Int,
        name: String
    ): Mono<WorkoutStage> = workoutStageDAL.updateWorkoutStage(id, programmedWorkoutId, stageTypeId, position, name)

    /**
     * Checks if a workout stage exists for a specific workout and position.
     * @see WorkoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition
     */
    fun selectWorkoutStageByWorkoutIdAndPosition(
        programmedWorkoutId: Long,
        position: Int
    ): Mono<WorkoutStage> = workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(programmedWorkoutId, position)

    /**
     * Deletes a workout stage from the database.
     * @see WorkoutStageDAL.deleteWorkoutStage
     */
    fun deleteWorkoutStage(id: Long): Mono<WorkoutStage> = workoutStageDAL.deleteWorkoutStage(id)

    /**
     * Gets the owner of the workout stage.
     *
     * This traces the relationship chain:
     * WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param workoutStageId The ID of the workout stage
     * @return Mono<String> The Keycloak user ID of the owner
     */
    fun getOwner(workoutStageId: Long): Mono<String> {
        return workoutStageDAL.selectWorkoutStageById(workoutStageId)
            .flatMap { workoutStage ->
                programmedWorkoutDAL.selectProgrammedWorkoutById(workoutStage.programmedWorkoutId)
            }
            .flatMap { programmedWorkout ->
                programDAL.selectProgramById(programmedWorkout.programId)
            }
            .map { program -> program.userId.toString() }
    }

    /**
     * Checks if the given user is the owner of the workout stage.
     *
     * This traces the relationship chain:
     * WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param workoutStageId The ID of the workout stage
     * @param userId The Keycloak user ID to check ownership against (as String)
     * @return Mono<Boolean> true if the user owns the workout stage, false otherwise
     */
    fun isOwner(
        workoutStageId: Long,
        userId: String
    ): Mono<Boolean> {
        return getOwner(workoutStageId)
            .map { ownerId -> ownerId == userId }
            .onErrorReturn(false)
    }
}
