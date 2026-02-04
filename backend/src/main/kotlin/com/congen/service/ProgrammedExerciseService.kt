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
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
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
     * When totalSets is provided, creates or deletes set schemes so the count matches.
     *
     * @param id Programmed exercise ID
     * @param workoutStageId Workout stage ID
     * @param exerciseName Exercise name
     * @param position Position in stage
     * @param notes Optional notes
     * @param totalSets Optional target number of set schemes; when provided, set schemes are created or deleted to match
     * @return Mono containing the updated programmed exercise
     */
    fun updateProgrammedExercise(
        id: Long,
        workoutStageId: Long,
        exerciseName: String,
        position: Int,
        notes: String?,
        totalSets: Int? = null,
    ): Mono<ProgrammedExercise> {
        return programmedExerciseDAL.updateProgrammedExercise(id, workoutStageId, exerciseName, position, notes)
            .flatMap { updated ->
                if (totalSets != null && totalSets > 0) {
                    adjustSetSchemeCount(id, totalSets).thenReturn(updated)
                } else {
                    Mono.just(updated)
                }
            }
    }

    /**
     * Adjusts the number of set schemes for a programmed exercise to match totalSets.
     * Creates new set schemes (copying from the first existing set) or deletes excess set schemes.
     *
     * @param programmedExerciseId The programmed exercise ID
     * @param totalSets The desired number of set schemes
     * @return Mono that completes when the adjustment is done
     */
    fun adjustSetSchemeCount(programmedExerciseId: Long, totalSets: Int): Mono<Void> {
        return setSchemeService.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)
            .flatMap { existing ->
                val currentCount = existing.size
                val sorted = existing.sortedBy { it.setNumber }
                when {
                    totalSets > currentCount -> {
                        val template = sorted.firstOrNull()
                        Flux.range(currentCount + 1, totalSets - currentCount)
                            .flatMap { setNumber ->
                                createSetSchemeFromTemplate(programmedExerciseId, setNumber, template)
                            }
                            .then()
                    }
                    totalSets < currentCount -> {
                        val toDelete = sorted.drop(totalSets)
                        Flux.fromIterable(toDelete)
                            .flatMap { setScheme -> setSchemeService.deleteSetScheme(setScheme.id).then() }
                            .then()
                    }
                    else -> Mono.fromCallable { Unit }.then()
                }
            }
            .then()
    }

    private fun createSetSchemeFromTemplate(
        programmedExerciseId: Long,
        setNumber: Int,
        template: SetScheme?,
    ): Mono<SetScheme> {
        return if (template != null) {
            setSchemeService.insertSetScheme(
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = template.isAmrap,
                isEmom = template.isEmom,
                useTempo = template.useTempo,
                eccentricTempo = template.eccentricTempo,
                isometricTempo = template.isometricTempo,
                concentricTempo = template.concentricTempo,
                targetWeight = template.targetWeight?.toString(),
                performedWeight = template.performedWeight?.toString(),
                targetRepCount = template.targetRepCount,
                performedRepCount = template.performedRepCount,
                restSeconds = template.restSeconds,
                unit = "KG",
            )
        } else {
            setSchemeService.insertSetScheme(
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = "1",
                performedWeight = null,
                targetRepCount = 1,
                performedRepCount = null,
                restSeconds = 60,
                unit = "KG",
            )
        }
    }

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
