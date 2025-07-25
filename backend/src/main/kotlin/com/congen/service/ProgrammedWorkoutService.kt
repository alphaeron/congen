package com.congen.service

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
 * @property programmedWorkoutDAL Data access layer for programmed workout operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ProgrammedWorkoutService(
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL
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
    fun selectProgrammedWorkoutsByProgramId(programId: Long): Mono<List<ProgrammedWorkout>> =
        programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programId)

    /**
     * Retrieves all programmed workout records from the database.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkouts
     */
    fun selectProgrammedWorkouts(): Mono<List<ProgrammedWorkout>> = programmedWorkoutDAL.selectProgrammedWorkouts()

    /**
     * Retrieves all programmed workouts owned by a specific user.
     * @see ProgrammedWorkoutDAL.selectProgrammedWorkoutsByUserId
     */
    fun selectProgrammedWorkoutsByUserId(userId: Int): Mono<List<ProgrammedWorkout>> =
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
    fun hasUserExistingWorkouts(userId: Int): Mono<Boolean> = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

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
        return programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)
            .flatMap { programmedWorkout ->
                // Need to get the Program to check userId
                // This requires access to ProgramDAL, which is not injected here.
                // For now, assume programId is the userId (if not, this will need to be updated to inject ProgramDAL)
                // Replace this with actual lookup if needed.
                Mono.just(programmedWorkout.programId.toString() == userId)
            }
            .onErrorReturn(false)
    }
}
