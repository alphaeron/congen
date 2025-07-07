package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.SetScheme
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service for managing set scheme operations with automatic 1RM updates.
 *
 * This service provides business logic for set scheme operations, including
 * automatic updates to user one rep max values when performed weights exceed
 * current 1RM values. The service coordinates between multiple DALs to
 * maintain data consistency.
 *
 * ## 1RM Update Logic
 *
 * When a set scheme is inserted or updated:
 * 1. Retrieve the programmed exercise to get the exercise name
 * 2. Trace the relationship chain to get the user ID:
 *    - ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
 * 3. Check if the user has a 1RM for the exercise
 * 4. If performed weight > current 1RM, update the 1RM
 * 5. Only update if the set has a targeted weight
 *
 * ## Operations
 *
 * - **Insert**: Create new set scheme and check for 1RM updates
 * - **Update**: Update existing set scheme and check for 1RM updates
 * - **Delete**: Remove set scheme (no 1RM impact)
 * - **Read**: Standard read operations (no 1RM impact)
 *
 * @property setSchemeDAL Data access layer for set scheme operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property programDAL Data access layer for program operations
 * @property userOneRepMaxDAL Data access layer for user one rep max operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class SetSchemeService(
    private val setSchemeDAL: SetSchemeDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val programDAL: ProgramDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(SetSchemeService::class.java)
    }

    /**
     * Inserts a new set scheme and checks for 1RM updates.
     *
     * This method creates a new set scheme and automatically updates the user's
     * one rep max if the performed weight exceeds the current 1RM for the exercise.
     *
     * @param programmedExerciseId The ID of the programmed exercise this set belongs to
     * @param setNumber The set number within the exercise
     * @param targetWeight The target weight for this set
     * @param targetReps The target number of reps for this set
     * @param targetTempo The target tempo for this set
     * @param restSeconds The rest period in seconds after this set
     * @param performedWeight The actual weight performed (optional)
     * @param performedReps The actual reps performed (optional)
     * @param performedTempo The actual tempo performed (optional)
     * @return Mono containing the inserted set scheme
     */
    fun insertSetScheme(
        programmedExerciseId: Long,
        setNumber: Int,
        isAmrap: Boolean,
        isEmom: Boolean,
        useTempo: Boolean,
        eccentricTempo: String?,
        isometricTempo: String?,
        concentricTempo: String?,
        targetWeight: BigDecimal?,
        performedWeight: BigDecimal?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
    ): Mono<SetScheme> {
        logger.debug("Inserting set scheme for exercise: {}", programmedExerciseId)

        return setSchemeDAL.insertSetScheme(
            programmedExerciseId,
            setNumber,
            isAmrap,
            isEmom,
            useTempo,
            eccentricTempo,
            isometricTempo,
            concentricTempo,
            targetWeight,
            performedWeight,
            targetRepCount,
            performedRepCount,
            restSeconds
        )
            .flatMap { insertedSetScheme ->
                // Check for 1RM update if the set has a target weight
                if (insertedSetScheme.targetWeight != null) {
                    checkAndUpdateOneRepMax(insertedSetScheme)
                        .thenReturn(insertedSetScheme)
                } else {
                    Mono.just(insertedSetScheme)
                }
            }
    }

    /**
     * Updates an existing set scheme and checks for 1RM updates.
     *
     * This method updates an existing set scheme and automatically updates the user's
     * one rep max if the performed weight exceeds the current 1RM for the exercise.
     *
     * @param id The unique identifier of the set scheme to update
     * @param programmedExerciseId ID of the programmed exercise this set belongs to
     * @param setNumber Order of this set within the exercise (1-based)
     * @param isAmrap As Many Reps As Possible flag
     * @param isEmom Every Minute On the Minute flag
     * @param useTempo Whether to use tempo timing
     * @param eccentricTempo Eccentric phase tempo (0-9 seconds)
     * @param isometricTempo Isometric phase tempo (0-9 seconds)
     * @param concentricTempo Concentric phase tempo (0-9 seconds)
     * @param targetWeight Target weight for the set in kg
     * @param performedWeight Actual weight used in kg
     * @param targetRepCount Target number of repetitions
     * @param performedRepCount Actual number of repetitions completed
     * @param restSeconds Rest period after the set in seconds
     * @return Mono containing the updated set scheme
     */
    fun updateSetScheme(
        id: Long,
        programmedExerciseId: Long,
        setNumber: Int,
        isAmrap: Boolean,
        isEmom: Boolean,
        useTempo: Boolean,
        eccentricTempo: String?,
        isometricTempo: String?,
        concentricTempo: String?,
        targetWeight: BigDecimal?,
        performedWeight: BigDecimal?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
    ): Mono<SetScheme> {
        logger.debug("Updating set scheme: {} for exercise: {}", id, programmedExerciseId)

        return setSchemeDAL.updateSetScheme(
            id,
            programmedExerciseId,
            setNumber,
            isAmrap,
            isEmom,
            useTempo,
            eccentricTempo,
            isometricTempo,
            concentricTempo,
            targetWeight,
            performedWeight,
            targetRepCount,
            performedRepCount,
            restSeconds
        )
            .flatMap { updatedSetScheme ->
                // Check for 1RM update if the set has a performed weight
                if (updatedSetScheme.performedWeight != null) {
                    checkAndUpdateOneRepMax(updatedSetScheme)
                        .thenReturn(updatedSetScheme)
                } else {
                    Mono.just(updatedSetScheme)
                }
            }
    }

    /**
     * Retrieves a set scheme by its unique identifier.
     *
     * This method delegates to the DAL for read operations, which don't affect 1RM values.
     *
     * @param id The unique identifier of the set scheme to retrieve
     * @return Mono containing the set scheme if found
     */
    fun selectSetSchemeById(id: Long): Mono<SetScheme> {
        return setSchemeDAL.selectSetSchemeById(id)
    }

    /**
     * Retrieves all set schemes for a specific programmed exercise.
     *
     * This method delegates to the DAL for read operations, which don't affect 1RM values.
     *
     * @param programmedExerciseId The unique identifier of the programmed exercise
     * @return Mono containing a list of set schemes for the exercise
     */
    fun selectSetSchemesByProgrammedExerciseId(programmedExerciseId: Long): Mono<List<SetScheme>> {
        return setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)
    }

    /**
     * Retrieves all set schemes from the database.
     *
     * This method delegates to the DAL for read operations, which don't affect 1RM values.
     *
     * @return Mono containing a list of all set schemes
     */
    fun selectSetSchemes(): Mono<List<SetScheme>> {
        return setSchemeDAL.selectSetSchemes()
    }

    /**
     * Deletes a set scheme from the database.
     *
     * This method delegates to the DAL for delete operations, which don't affect 1RM values.
     *
     * @param id The unique identifier of the set scheme to delete
     * @return Mono containing the deleted set scheme
     */
    fun deleteSetScheme(id: Long): Mono<SetScheme> {
        return setSchemeDAL.deleteSetScheme(id)
    }

    /**
     * Checks if the performed weight exceeds the current 1RM and updates if necessary.
     *
     * This method traces the relationship chain from set scheme to user to determine
     * if the performed weight should update the user's 1RM for the exercise.
     *
     * @param setScheme The set scheme to check for 1RM updates
     * @return Mono that completes when the 1RM check/update is done
     */
    private fun checkAndUpdateOneRepMax(setScheme: SetScheme): Mono<Void> {
        return programmedExerciseDAL.selectProgrammedExerciseById(setScheme.programmedExerciseId)
            .flatMap { programmedExercise ->
                // Get the user ID by tracing the relationship chain
                getUserIdFromProgrammedExercise(programmedExercise.id)
                    .flatMap { userId ->
                        val exerciseName = programmedExercise.exerciseName
                        val performedWeight = setScheme.performedWeight!!

                        // Check if user has a 1RM for this exercise
                        userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)
                            .flatMap { currentOneRepMax ->
                                // Update 1RM if performed weight is greater
                                if (performedWeight > currentOneRepMax.oneRepMax) {
                                    userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, performedWeight)
                                        .doOnSuccess {
                                            logger.info(
                                                "Updated 1RM for user {} exercise {} from {} to {}",
                                                userId,
                                                exerciseName,
                                                currentOneRepMax.oneRepMax,
                                                performedWeight
                                            )
                                        }
                                        .then()
                                } else {
                                    Mono.empty<Void>()
                                }
                            }
                            .onErrorResume(NoResultsFoundException::class.java) {
                                // No existing 1RM, create new one
                                userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, performedWeight)
                                    .doOnSuccess {
                                        logger.info(
                                            "Created new 1RM for user {} exercise {}: {}",
                                            userId,
                                            exerciseName,
                                            performedWeight
                                        )
                                    }
                                    .then()
                            }
                    }
            }
            .then()
    }

    /**
     * Gets the user ID by tracing the relationship chain from programmed exercise to user.
     *
     * This method follows the relationship chain:
     * ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param programmedExerciseId The ID of the programmed exercise
     * @return Mono containing the user ID
     */
    private fun getUserIdFromProgrammedExercise(programmedExerciseId: Long): Mono<Int> {
        return programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)
    }
}
