package com.congen.service

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.model.ProgrammedExercise
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for managing programmed exercise operations.
 *
 * This service is a thin wrapper around [ProgrammedExerciseDAL], exposing the same methods
 * for use by controllers and other services.
 *
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ProgrammedExerciseService(
    private val programmedExerciseDAL: ProgrammedExerciseDAL
) {
    /**
     * Retrieves a programmed exercise by its unique identifier.
     * @see ProgrammedExerciseDAL.selectProgrammedExerciseById
     */
    fun selectProgrammedExerciseById(id: Long): Mono<ProgrammedExercise> = programmedExerciseDAL.selectProgrammedExerciseById(id)

    /**
     * Retrieves all programmed exercises for a specific workout stage.
     * @see ProgrammedExerciseDAL.selectProgrammedExercisesByWorkoutStageId
     */
    fun selectProgrammedExercisesByWorkoutStageId(workoutStageId: Long): Mono<List<ProgrammedExercise>> =
        programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(workoutStageId)

    /**
     * Retrieves all programmed exercise records from the database.
     * @see ProgrammedExerciseDAL.selectProgrammedExercises
     */
    fun selectProgrammedExercises(): Mono<List<ProgrammedExercise>> = programmedExerciseDAL.selectProgrammedExercises()

    /**
     * Creates a new programmed exercise record in the database.
     * @see ProgrammedExerciseDAL.insertProgrammedExercise
     */
    fun insertProgrammedExercise(
        workoutStageId: Long,
        exerciseName: String,
        position: Int,
        notes: String?
    ): Mono<ProgrammedExercise> = programmedExerciseDAL.insertProgrammedExercise(workoutStageId, exerciseName, position, notes)

    /**
     * Updates an existing programmed exercise record in the database.
     * @see ProgrammedExerciseDAL.updateProgrammedExercise
     */
    fun updateProgrammedExercise(
        id: Long,
        workoutStageId: Long,
        exerciseName: String,
        position: Int,
        notes: String?
    ): Mono<ProgrammedExercise> = programmedExerciseDAL.updateProgrammedExercise(id, workoutStageId, exerciseName, position, notes)

    /**
     * Deletes a programmed exercise record from the database.
     * @see ProgrammedExerciseDAL.deleteProgrammedExercise
     */
    fun deleteProgrammedExercise(id: Long): Mono<ProgrammedExercise> = programmedExerciseDAL.deleteProgrammedExercise(id)

    /**
     * Checks if a programmed exercise exists for a specific workout stage and exercise name.
     * @see ProgrammedExerciseDAL.selectProgrammedExerciseByStageIdAndExerciseName
     */
    fun selectProgrammedExerciseByStageIdAndExerciseName(
        workoutStageId: Long,
        exerciseName: String
    ): Mono<ProgrammedExercise> = programmedExerciseDAL.selectProgrammedExerciseByStageIdAndExerciseName(workoutStageId, exerciseName)

    /**
     * Gets the user ID associated with a programmed exercise.
     * @see ProgrammedExerciseDAL.getUserIdFromProgrammedExercise
     */
    fun getUserIdFromProgrammedExercise(programmedExerciseId: Long): Mono<String> =
        programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)

    /**
     * Retrieves all programmed exercises owned by a specific user.
     * @see ProgrammedExerciseDAL.selectProgrammedExercisesByUserId
     */
    fun selectProgrammedExercisesByUserId(userId: String): Mono<List<ProgrammedExercise>> =
        programmedExerciseDAL.selectProgrammedExercisesByUserId(userId)

    /**
     * Gets the owner of the programmed exercise.
     *
     * @param programmedExerciseId The ID of the programmed exercise
     * @return Mono<String> The Keycloak user ID of the owner
     */
    fun getOwner(programmedExerciseId: Long): Mono<String> = getUserIdFromProgrammedExercise(programmedExerciseId)

    /**
     * Checks if the given user is the owner of the programmed exercise.
     *
     * @param programmedExerciseId The ID of the programmed exercise
     * @param userId The Keycloak user ID to check ownership against (as String)
     * @return Mono<Boolean> true if the user owns the programmed exercise, false otherwise
     */
    fun isOwner(
        programmedExerciseId: Long,
        userId: String
    ): Mono<Boolean> {
        return getOwner(programmedExerciseId)
            .map { ownerId -> ownerId == userId }
            .onErrorReturn(false)
    }
}
