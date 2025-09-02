package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.model.ProgrammedWorkout
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for managing programmed workout operations.
 *
 * This service is a thin wrapper around [ProgrammedWorkoutDAL], exposing the same methods
 * for use by controllers and other services.
 *
 * @param programmedWorkoutDAL Data access layer for programmed workout operations
 * @param programDAL Data access layer for program operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ProgrammedWorkoutService(
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val programDAL: ProgramDAL
) {
    /**
     * Retrieves a programmed workout by its unique identifier.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkoutById
     */
    fun selectProgrammedWorkoutById(id: Long): Mono<ProgrammedWorkout> = programmedWorkoutDAL.selectProgrammedWorkoutById(id)

    /**
     * Retrieves all programmed workouts for a specific program.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkoutsByProgramId
     */
    fun selectProgrammedWorkoutsByProgramId(
        programId: Long,
        weekNumber: Int? = null
    ): Mono<List<ProgrammedWorkout>> = programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programId, weekNumber)

    /**
     * Retrieves all programmed workout records from the database.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkouts
     */
    fun selectProgrammedWorkouts(): Mono<List<ProgrammedWorkout>> = programmedWorkoutDAL.selectProgrammedWorkouts()

    /**
     * Retrieves all programmed workouts owned by a specific user.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkoutsByUserId
     */
    fun selectProgrammedWorkoutsByUserId(userId: String): Mono<List<ProgrammedWorkout>> =
        programmedWorkoutDAL.selectProgrammedWorkoutsByUserId(userId)

    /**
     * Creates a new programmed workout record in the database.
     * @see ProgrammedWorkoutDAL.insertProgrammedWorkout
     */
    fun insertProgrammedWorkout(
        programId: Long,
        dayNumber: Int,
        name: String
    ): Mono<ProgrammedWorkout> = programmedWorkoutDAL.insertProgrammedWorkout(programId, dayNumber, name)

    /**
     * Updates an existing programmed workout record in the database.
     * @see ProgrammedWorkoutDAL.updateProgrammedWorkout
     */
    fun updateProgrammedWorkout(
        id: Long,
        programId: Long,
        dayNumber: Int,
        name: String
    ): Mono<ProgrammedWorkout> = programmedWorkoutDAL.updateProgrammedWorkout(id, programId, dayNumber, name)

    /**
     * Checks if a user has any existing programmed workouts.
     * @see ProgrammedWorkoutDAL.hasUserExistingWorkouts
     */
    fun hasUserExistingWorkouts(userId: String): Mono<Boolean> = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

    /**
     * Deletes a programmed workout record from the database.
     * @see ProgrammedWorkoutDAL.deleteProgrammedWorkout
     */
    fun deleteProgrammedWorkout(id: Long): Mono<ProgrammedWorkout> = programmedWorkoutDAL.deleteProgrammedWorkout(id)

    /**
     * Checks if the given user is the owner of the programmed workout.
     *
     * This traces the relationship chain:
     * ProgrammedWorkout → Program → User
     *
     * @param programmedWorkoutId The ID of the programmed workout
     * @param userId The Keycloak user ID to check ownership against (as String)
     * @return Mono<Boolean> true if the user owns the programmed workout, false otherwise
     */
    fun isOwner(
        programmedWorkoutId: Long,
        userId: String
    ): Mono<Boolean> {
        return getOwner(programmedWorkoutId)
            .map { ownerId -> ownerId == userId }
            .onErrorReturn(false)
    }

    /**
     * Gets the owner of the programmed workout.
     *
     * This traces the relationship chain:
     * ProgrammedWorkout → Program → User
     *
     * @param programmedWorkoutId The ID of the programmed workout
     * @return Mono<String> The Keycloak user ID of the owner
     */
    fun getOwner(programmedWorkoutId: Long): Mono<String> {
        return programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)
            .flatMap { programmedWorkout ->
                programDAL.selectProgramById(programmedWorkout.programId)
            }
            .map { program -> program.userId }
    }
}
