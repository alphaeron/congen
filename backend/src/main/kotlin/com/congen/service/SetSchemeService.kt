package com.congen.service

import com.congen.client.PostgresClient
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Band
import com.congen.model.SetScheme
import com.congen.model.WeightUnit
import com.congen.util.OneRepMaxCalculator
import com.congen.util.UnitConverter
import com.congen.util.ValidationUtil
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
 * @param setSchemeDAL Data access layer for set scheme operations
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
 * @param userOneRepMaxService Service for user one rep max operations
 * @param unitConverter Service for unit conversions
 * @param oneRepMaxCalculator Service for one rep max calculations
 * @param postgresClient Database client with transaction support
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class SetSchemeService(
    private val setSchemeDAL: SetSchemeDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val userOneRepMaxService: UserOneRepMaxService,
    private val unitConverter: UnitConverter,
    private val oneRepMaxCalculator: OneRepMaxCalculator,
    private val postgresClient: PostgresClient
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(SetSchemeService::class.java)
    }

    /**
     * Creates a new set scheme with unit conversion and validation.
     *
     * This method accepts set scheme parameters, validates them, converts weights
     * to kg for storage, and saves the set scheme to the database. The method
     * returns the created set scheme with weights converted to the requested unit.
     *
     * @param programmedExerciseId The ID of the programmed exercise this set belongs to
     * @param setNumber The set number within the exercise
     * @param isAmrap As Many Reps As Possible flag
     * @param isEmom Every Minute On the Minute flag
     * @param useTempo Whether to use tempo timing
     * @param eccentricTempo Eccentric phase tempo (0-9 seconds)
     * @param isometricTempo Isometric phase tempo (0-9 seconds)
     * @param concentricTempo Concentric phase tempo (0-9 seconds)
     * @param targetWeight Target weight for the set (as string)
     * @param performedWeight Actual weight used (as string)
     * @param targetRepCount Target number of repetitions
     * @param performedRepCount Actual number of repetitions completed
     * @param restSeconds Rest period after the set in seconds
     * @param unit The unit of the weight values (KG or LBS). Defaults to KG
     * @param band The band information for Dynamic Effort exercises
     * @return Mono containing the created set scheme with weights in requested unit
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
        targetWeight: String?,
        performedWeight: String?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
        unit: String?,
        band: Band? = null,
    ): Mono<SetScheme> {
        logger.info("Creating set scheme for exercise: {}, set: {}", programmedExerciseId, setNumber)

        val weightUnit = WeightUnit.fromString(unit)
        val targetWeightBD =
            targetWeight?.toBigDecimalOrNull()?.let {
                ValidationUtil.validateTargetWeightWithUnit(it, weightUnit, unitConverter)
            }
        val performedWeightBD =
            performedWeight?.toBigDecimalOrNull()?.let {
                ValidationUtil.validatePerformedWeightWithUnit(it, weightUnit, unitConverter)
            }

        return setSchemeDAL.insertSetScheme(
            programmedExerciseId,
            setNumber,
            isAmrap,
            isEmom,
            useTempo,
            eccentricTempo,
            isometricTempo,
            concentricTempo,
            targetWeightBD,
            performedWeightBD,
            targetRepCount,
            performedRepCount,
            restSeconds,
            band,
        )
            .map { savedScheme ->
                logger.debug("Created set scheme with id: {}", savedScheme.id)
                // Convert output weights to requested unit (if not kg)
                if (unit != null && weightUnit != WeightUnit.KG) {
                    savedScheme.copy(
                        targetWeight = savedScheme.targetWeight?.let { unitConverter.fromKg(it, weightUnit) },
                        performedWeight = savedScheme.performedWeight?.let { unitConverter.fromKg(it, weightUnit) }
                    )
                } else {
                    savedScheme
                }
            }
            .doOnError { e ->
                logger.error("Error creating set scheme for exercise: {}, set: {}", programmedExerciseId, setNumber, e)
            }
    }

    /**
     * Updates an existing set scheme with unit conversion and validation.
     *
     * This method updates an existing set scheme, validates the parameters,
     * converts weights to kg for storage, and returns the updated set scheme
     * with weights converted to the requested unit.
     *
     * @param id The unique identifier of the set scheme to update
     * @param programmedExerciseId The ID of the programmed exercise this set belongs to
     * @param setNumber The set number within the exercise
     * @param isAmrap As Many Reps As Possible flag
     * @param isEmom Every Minute On the Minute flag
     * @param useTempo Whether to use tempo timing
     * @param eccentricTempo Eccentric phase tempo (0-9 seconds)
     * @param isometricTempo Isometric phase tempo (0-9 seconds)
     * @param concentricTempo Concentric phase tempo (0-9 seconds)
     * @param targetWeight Target weight for the set (as string)
     * @param performedWeight Actual weight used (as string)
     * @param targetRepCount Target number of repetitions
     * @param performedRepCount Actual number of repetitions completed
     * @param restSeconds Rest period after the set in seconds
     * @param unit The unit of the weight values (KG or LBS). Defaults to KG
     * @param band The band information for Dynamic Effort exercises
     * @return Mono containing the updated set scheme with weights in requested unit
     */
    fun updateSetSchemeWithUnit(
        id: Long,
        programmedExerciseId: Long,
        setNumber: Int,
        isAmrap: Boolean,
        isEmom: Boolean,
        useTempo: Boolean,
        eccentricTempo: String?,
        isometricTempo: String?,
        concentricTempo: String?,
        targetWeight: String?,
        performedWeight: String?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
        unit: String?,
        band: Band? = null,
    ): Mono<SetScheme> {
        logger.info("Updating set scheme: {}", id)

        val weightUnit = WeightUnit.fromString(unit)
        val targetWeightBD =
            targetWeight?.toBigDecimalOrNull()?.let {
                ValidationUtil.validateTargetWeightWithUnit(it, weightUnit, unitConverter)
            }
        val performedWeightBD =
            performedWeight?.toBigDecimalOrNull()?.let {
                ValidationUtil.validatePerformedWeightWithUnit(it, weightUnit, unitConverter)
            }

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
            targetWeightBD,
            performedWeightBD,
            targetRepCount,
            performedRepCount,
            restSeconds,
            band,
        )
            .flatMap { updatedSetScheme ->
                // Check for 1RM update if the set has a performed weight and we're not skipping it
                if (updatedSetScheme.performedWeight != null) {
                    checkAndUpdateOneRepMax(updatedSetScheme)
                        .thenReturn(updatedSetScheme)
                } else {
                    Mono.just(updatedSetScheme)
                }
            }
            .map { updatedScheme ->
                logger.debug("Updated set scheme: {}", id)
                // Convert output weights to requested unit (if not kg)
                if (unit != null && weightUnit != WeightUnit.KG) {
                    updatedScheme.copy(
                        targetWeight = updatedScheme.targetWeight?.let { unitConverter.fromKg(it, weightUnit) },
                        performedWeight = updatedScheme.performedWeight?.let { unitConverter.fromKg(it, weightUnit) }
                    )
                } else {
                    updatedScheme
                }
            }
            .doOnError { e ->
                logger.error("Error updating set scheme: {}", id, e)
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
            .doOnError { e ->
                logger.error("Error getting set scheme: {}", id, e)
            }
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
        logger.debug("Getting set schemes for programmed exercise: {}", programmedExerciseId)
        return setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)
            .doOnError { e ->
                logger.error("Error getting set schemes for programmed exercise: {}", programmedExerciseId, e)
            }
    }

    /**
     * Retrieves all set schemes from the database.
     *
     * This method delegates to the DAL for read operations, which don't affect 1RM values.
     *
     * @return Mono containing a list of all set schemes
     */
    fun selectSetSchemes(): Mono<List<SetScheme>> {
        logger.debug("Getting all set schemes")
        return setSchemeDAL.selectSetSchemes()
            .doOnError { e ->
                logger.error("Error getting all set schemes", e)
            }
    }

    /**
     * Retrieves all set schemes owned by a specific user.
     *
     * This method efficiently fetches all set schemes that belong to programmed exercises
     * owned by the specified user by joining through the relationship chain:
     * SetScheme → ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of set schemes owned by the user
     */
    fun selectSetSchemesByUserId(userId: String): Mono<List<SetScheme>> {
        logger.debug("Getting set schemes for user: {}", userId)
        return setSchemeDAL.selectSetSchemesByUserId(userId)
            .doOnError { e ->
                logger.error("Error getting set schemes for user: {}", userId, e)
            }
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
        logger.info("Deleting set scheme: {}", id)
        return setSchemeDAL.deleteSetScheme(id)
            .doOnSuccess {
                logger.debug("Deleted set scheme: {}", id)
            }
            .doOnError { e ->
                logger.error("Error deleting set scheme: {}", id, e)
            }
    }

    /**
     * Checks if the performed weight and reps should update the current 1RM and updates if necessary.
     *
     * This method traces the relationship chain from set scheme to user to determine
     * if the performed weight and reps should update the user's 1RM for the exercise.
     * It calculates an estimated 1RM from the performed weight and reps using appropriate
     * formulas rather than assuming the performed weight is already a 1RM.
     *
     * @param setScheme The set scheme to check for 1RM updates
     * @return Mono that completes when the 1RM check/update is done
     */
    private fun checkAndUpdateOneRepMax(setScheme: SetScheme): Mono<Void> {
        return programmedExerciseDAL.selectProgrammedExerciseById(setScheme.programmedExerciseId)
            .flatMap { programmedExercise ->
                getUserIdFromProgrammedExercise(programmedExercise.id)
                    .flatMap { userId ->
                        val exerciseName = programmedExercise.exerciseName
                        val performedWeight = setScheme.performedWeight!!
                        val performedReps = setScheme.performedRepCount

                        if (performedReps == null || performedReps <= 0) {
                            logger.debug(
                                "Skipping 1RM update for user {} exercise {} - no valid rep count: {}",
                                userId,
                                exerciseName,
                                performedReps
                            )
                            Mono.empty<Void>()
                        } else {
                            val estimatedOneRepMax =
                                oneRepMaxCalculator.estimateOneRepMax(performedWeight, performedReps)

                            postgresClient.withTransaction {
                                selectAndUpdateOneRepMaxIfHigher(userId, exerciseName, estimatedOneRepMax)
                                    .onErrorResume(NoResultsFoundException::class.java) {
                                        userOneRepMaxService.insertUserOneRepMax(userId, exerciseName, estimatedOneRepMax)
                                            .doOnSuccess {
                                                logger.info(
                                                    "Created new 1RM for user {} exercise {}: {} (calculated from {} × {} reps)",
                                                    userId,
                                                    exerciseName,
                                                    estimatedOneRepMax,
                                                    performedWeight,
                                                    performedReps
                                                )
                                            }
                                            .onErrorResume { e ->
                                                if (e is DatabaseQueryException && e.message?.contains("duplicate key") == true) {
                                                    selectAndUpdateOneRepMaxIfHigher(userId, exerciseName, estimatedOneRepMax)
                                                        .then(userOneRepMaxService.selectUserOneRepMax(userId, exerciseName))
                                                } else {
                                                    Mono.error(e)
                                                }
                                            }
                                            .then()
                                    }
                                    .then()
                            }
                        }
                    }
            }
            .then()
    }

    /**
     * Loads the current 1RM in KG and updates it if the estimated value is higher.
     *
     * Used for both the normal update path and the concurrent-insert race path
     * (duplicate key on insert): in both cases we "update in place" via this single path.
     *
     * @param userId The Keycloak user ID
     * @param exerciseName The exercise name
     * @param estimatedOneRepMax The estimated 1RM in kg
     * @return Mono that completes when the check/update is done
     */
    private fun selectAndUpdateOneRepMaxIfHigher(
        userId: String,
        exerciseName: String,
        estimatedOneRepMax: BigDecimal
    ): Mono<Void> {
        return userOneRepMaxService.selectUserOneRepMax(userId, exerciseName, "KG")
            .flatMap { currentOneRepMax ->
                if (estimatedOneRepMax > currentOneRepMax.oneRepMax) {
                    userOneRepMaxService.updateUserOneRepMax(userId, exerciseName, estimatedOneRepMax)
                        .doOnSuccess {
                            logger.info(
                                "Updated 1RM for user {} exercise {} from {} to {}",
                                userId,
                                exerciseName,
                                currentOneRepMax.oneRepMax,
                                estimatedOneRepMax
                            )
                        }
                        .then()
                } else {
                    logger.debug(
                        "No 1RM update needed for user {} exercise {} - estimated {} not greater than current {}",
                        userId,
                        exerciseName,
                        estimatedOneRepMax,
                        currentOneRepMax.oneRepMax
                    )
                    Mono.empty<Void>()
                }
            }
    }

    /**
     * Gets the user ID by tracing the relationship chain from programmed exercise to user.
     *
     * This method follows the relationship chain:
     * ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param programmedExerciseId The ID of the programmed exercise
     * @return Mono containing the Keycloak user ID
     */
    private fun getUserIdFromProgrammedExercise(programmedExerciseId: Long): Mono<String> {
        return programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    /**
     * Gets the owner of the set scheme.
     *
     * This traces the relationship chain:
     * SetScheme → ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param setSchemeId The ID of the set scheme
     * @return Mono<String> The Keycloak user ID of the owner
     */
    fun getOwner(setSchemeId: Long): Mono<String> {
        return setSchemeDAL.selectSetSchemeById(setSchemeId)
            .flatMap { setScheme ->
                programmedExerciseDAL.getUserIdFromProgrammedExercise(setScheme.programmedExerciseId)
            }
    }

    /**
     * Checks if the given user is the owner of the set scheme.
     *
     * This traces the relationship chain:
     * SetScheme → ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     *
     * @param setSchemeId The ID of the set scheme
     * @param userId The Keycloak user ID to check ownership against (as String)
     * @return Mono<Boolean> true if the user owns the set scheme, false otherwise
     */
    fun isOwner(
        setSchemeId: Long,
        userId: String
    ): Mono<Boolean> {
        return getOwner(setSchemeId)
            .map { ownerId -> ownerId == userId }
            .onErrorReturn(false)
    }
}
