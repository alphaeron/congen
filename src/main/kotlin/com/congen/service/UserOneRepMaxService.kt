package com.congen.service

import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service for managing user one rep max operations.
 *
 * This service handles all business logic related to user one rep max values,
 * including unit conversions, validation, and preference management. It provides
 * a clean separation between the controller layer and the data access layer.
 *
 * ## Features
 *
 * - **Unit Conversion**: Automatically converts weights between kg and lbs
 * - **Preference Management**: Uses user's preferred units per exercise
 * - **Validation**: Validates weight values and units
 * - **Error Handling**: Provides meaningful error messages
 *
 * ## Usage
 *
 * ```kotlin
 * // Create or update a one rep max
 * val oneRepMax = userOneRepMaxService.upsertOneRepMax(userId, exerciseName, weight, unit)
 *
 * // Get all one rep maxes for a user
 * val oneRepMaxes = userOneRepMaxService.getAllByUser(userId, unit)
 *
 * // Get specific one rep max
 * val oneRepMax = userOneRepMaxService.getByUserAndExercise(userId, exerciseName, unit)
 * ```
 *
 * @property userOneRepMaxDAL Data access layer for user one rep max operations
 * @property userWeightUnitPreferenceDAL Data access layer for user weight unit preferences
 * @property unitConversionService Service for unit conversions
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class UserOneRepMaxService(
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val unitConversionService: UnitConversionService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserOneRepMaxService::class.java)
    }

    /**
     * Creates or updates a user one rep max.
     *
     * This method performs an upsert operation - if a one rep max exists for the specified
     * user and exercise, it will be updated; otherwise, a new one rep max will be created.
     *
     * The weight value is automatically converted to kg for internal storage. If no unit
     * is specified, the system will use the user's preferred unit for this exercise, or
     * default to kg if no preference is set.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @param unit The unit of the weight value (KG or LBS). If not specified, uses user's preference or defaults to KG
     * @return Mono containing the created or updated user one rep max (stored in kg)
     */
    fun upsertOneRepMax(
        userId: Int,
        exerciseName: String,
        oneRepMax: BigDecimal,
        unit: String?
    ): Mono<UserOneRepMax> {
        logger.info("Upserting user one rep max: {} - {} - {} - {}", userId, exerciseName, oneRepMax, unit)

        // Determine the unit to use
        val weightUnit = WeightUnit.fromString(unit)

        // Convert to kg for storage and validate
        val weightInKg = ValidationUtil.validateOneRepMaxWithUnit(oneRepMax, weightUnit, unitConversionService)
        logger.debug("Converted {} {} to {} kg for storage", oneRepMax, weightUnit, weightInKg)

        return userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, weightInKg)
    }

    /**
     * Retrieves all one rep max values for a specific user.
     *
     * This method fetches all one rep max values that are associated with the specified user,
     * returning a list of user-exercise 1RM relationships. Weights are converted to the user's
     * preferred units for each exercise, or displayed in kg if no preference is set.
     *
     * @param userId The unique identifier of the user
     * @param unit Optional unit to convert all weights to (KG or LBS). If not specified, uses each exercise's preferred unit
     * @return Mono containing a list of user one rep max values in preferred units
     */
    fun getAllByUser(
        userId: Int,
        unit: String?
    ): Mono<List<UserOneRepMax>> {
        return userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)
            .flatMap { oneRepMaxes ->
                // Get user's weight unit preferences
                userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)
                    .map { preferences ->
                        val preferenceMap = preferences.associateBy { it.exerciseName }

                        // Convert weights to preferred units
                        oneRepMaxes.map { oneRepMax ->
                            val preferredUnit =
                                if (unit != null) {
                                    try {
                                        WeightUnit.valueOf(unit.uppercase())
                                    } catch (e: IllegalArgumentException) {
                                        logger.warn("Invalid unit parameter: {}, using exercise preference", unit)
                                        preferenceMap[oneRepMax.exerciseName]?.preferredUnit ?: WeightUnit.KG
                                    }
                                } else {
                                    preferenceMap[oneRepMax.exerciseName]?.preferredUnit ?: WeightUnit.KG
                                }

                            val convertedWeight = unitConversionService.fromKg(oneRepMax.oneRepMax, preferredUnit)
                            oneRepMax.copy(oneRepMax = convertedWeight)
                        }
                    }
            }
            .doOnSuccess { oneRepMaxes ->
                logger.debug("Found {} one rep max values for user: {}", oneRepMaxes.size, userId)
            }
            .doOnError { e ->
                logger.error("Error getting one rep max values for user: {}", userId, e)
            }
    }

    /**
     * Retrieves a specific one rep max for a user and exercise.
     *
     * This method fetches the one rep max value for the specified user and exercise.
     * The weight is converted to the user's preferred unit for this exercise, or displayed
     * in kg if no preference is set. If no 1RM exists, a NoResultsFoundException will be thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @param unit Optional unit to convert the weight to (KG or LBS). If not specified, uses exercise's preferred unit
     * @return Mono containing the user one rep max if found
     * @throws NoResultsFoundException if the one rep max is not found
     */
    fun getByUserAndExercise(
        userId: Int,
        exerciseName: String,
        unit: String?
    ): Mono<UserOneRepMax> {
        return userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)
            .flatMap { oneRepMax ->
                // Get user's weight unit preference for this exercise
                val preferredUnitMono =
                    if (unit != null) {
                        try {
                            Mono.just(WeightUnit.valueOf(unit.uppercase()))
                        } catch (e: IllegalArgumentException) {
                            logger.warn("Invalid unit parameter: {}, using exercise preference", unit)
                            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
                                .map { it.preferredUnit }
                                .onErrorResume(NoResultsFoundException::class.java) {
                                    logger.debug(
                                        "No weight unit preference found for user {} and exercise {}, using KG",
                                        userId,
                                        exerciseName
                                    )
                                    Mono.just(WeightUnit.KG)
                                }
                        }
                    } else {
                        userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
                            .map { it.preferredUnit }
                            .onErrorResume(NoResultsFoundException::class.java) {
                                logger.debug("No weight unit preference found for user {} and exercise {}, using KG", userId, exerciseName)
                                Mono.just(WeightUnit.KG)
                            }
                    }

                preferredUnitMono.map { preferredUnit ->
                    val convertedWeight = unitConversionService.fromKg(oneRepMax.oneRepMax, preferredUnit)
                    val convertedOneRepMax = oneRepMax.copy(oneRepMax = convertedWeight)

                    logger.debug(
                        "Found one rep max for user: {} and exercise: {} (converted to {} {})",
                        userId,
                        exerciseName,
                        convertedWeight,
                        preferredUnit
                    )
                    convertedOneRepMax
                }
            }
            .doOnError { e ->
                logger.error("Error getting one rep max for user: {} and exercise: {}", userId, exerciseName, e)
            }
    }

    /**
     * Deletes a user one rep max by user ID and exercise name.
     *
     * This method removes the one rep max for the specified user and exercise.
     * If no 1RM exists, a NoResultsFoundException will be thrown.
     *
     * @param userId The unique identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono containing the deleted user one rep max
     * @throws NoResultsFoundException if the one rep max is not found
     */
    fun deleteOneRepMax(
        userId: Int,
        exerciseName: String
    ): Mono<UserOneRepMax> {
        logger.info("Deleting user one rep max: {} - {}", userId, exerciseName)
        return userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)
            .doOnError { e ->
                logger.error("Error deleting one rep max for user: {} and exercise: {}", userId, exerciseName, e)
            }
    }
}
