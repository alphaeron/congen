package com.congen.service

import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.model.UserOneRepMax
import com.congen.model.UserPerformanceScores
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service for managing user one rep max records.
 *
 * This service acts as a thin wrapper around UserOneRepMaxDAL and provides
 * additional business logic such as triggering performance score recalculation
 * when 1RM data changes.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class UserOneRepMaxService(
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val performanceScoringService: PerformanceScoringService,
    private val performanceTrackingService: PerformanceTrackingService,
    private val userPerformanceMetricsDAL: UserPerformanceMetricsDAL,
    private val userPerformanceScoresDAL: UserPerformanceScoresDAL,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val unitConverter: UnitConverter
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserOneRepMaxService::class.java)
    }

    /**
     * Retrieves all one rep max records for a specific user with optional unit conversion.
     *
     * @param userId The Keycloak identifier of the user
     * @param unit Optional unit to convert all weights to (kg or lbs). If null, uses user preferences.
     * @return Mono containing a list of user one rep max records with converted weights
     */
    fun selectUserOneRepMaxByUser(
        userId: String,
        unit: String? = null
    ): Mono<List<UserOneRepMax>> {
        return userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)
            .flatMap { oneRepMaxes ->
                if (oneRepMaxes.isEmpty()) {
                    // Return empty list immediately if there are no records
                    return@flatMap Mono.just(emptyList<UserOneRepMax>())
                }

                if (unit != null) {
                    // Convert all weights to the specified unit
                    val weightUnit = WeightUnit.fromString(unit)
                    val convertedOneRepMaxes = oneRepMaxes.map { oneRepMax ->
                        val convertedWeight = unitConverter.fromKg(oneRepMax.oneRepMax, weightUnit)
                        oneRepMax.copy(oneRepMax = convertedWeight)
                    }
                    Mono.just(convertedOneRepMaxes)
                } else {
                    // Convert each weight to the user's preferred unit for that exercise
                    val conversionPromises = oneRepMaxes.map { oneRepMax ->
                        userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, oneRepMax.exerciseName)
                            .map { preference ->
                                val convertedWeight = unitConverter.fromKg(oneRepMax.oneRepMax, preference.preferredUnit)
                                oneRepMax.copy(oneRepMax = convertedWeight)
                            }
                            .onErrorReturn(oneRepMax) // Return original if no preference found
                    }
                    Mono.zip(conversionPromises) { results ->
                        results.map { it as UserOneRepMax }
                    }
                }
            }
    }

    /**
     * Retrieves a specific one rep max record for a user and exercise with optional unit conversion.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param unit Optional unit to convert the weight to (kg or lbs)
     * @return Mono containing the user one rep max record with converted weight
     */
    fun selectUserOneRepMax(
        userId: String,
        exerciseName: String,
        unit: String? = null,
    ): Mono<UserOneRepMax> {
        return userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)
            .flatMap { oneRepMax ->
                if (unit != null) {
                    // Use the explicitly provided unit
                    val weightUnit = WeightUnit.fromString(unit)
                    val convertedWeight = unitConverter.fromKg(oneRepMax.oneRepMax, weightUnit)
                    Mono.just(oneRepMax.copy(oneRepMax = convertedWeight))
                } else {
                    // Check for user's preferred unit for this exercise
                    userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
                        .map { preference ->
                            val convertedWeight = unitConverter.fromKg(oneRepMax.oneRepMax, preference.preferredUnit)
                            oneRepMax.copy(oneRepMax = convertedWeight)
                        }
                        .onErrorReturn(oneRepMax) // Return original if no preference found
                }
            }
    }

    /**
     * Creates a new user one rep max record.
     * Note: This method assumes the weight is already in kg. Use upsertUserOneRepMax for unit conversion.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value (in kg)
     * @return Mono containing the created user one rep max record
     */
    fun insertUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal
    ): Mono<UserOneRepMax> = userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)

    /**
     * Updates an existing user one rep max record.
     * Note: This method assumes the weight is already in kg. Use upsertUserOneRepMax for unit conversion.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value (in kg)
     * @return Mono containing the updated user one rep max record
     */
    fun updateUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal
    ): Mono<UserOneRepMax> = userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)

    /**
     * Creates or updates a user one rep max record (upsert operation).
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @param oneRepMax The one rep max weight value
     * @param unit The weight unit (kg or lbs)
     * @return Mono containing the created or updated user one rep max record
     */
    fun upsertUserOneRepMax(
        userId: String,
        exerciseName: String,
        oneRepMax: BigDecimal,
        unit: String
    ): Mono<UserOneRepMax> {
        logger.debug("Upserting one rep max for user: {} exercise: {} value: {} unit: {}", userId, exerciseName, oneRepMax, unit)

        // Convert weight to kg for storage
        val weightUnit = WeightUnit.fromString(unit)
        val oneRepMaxInKg = unitConverter.toKg(oneRepMax, weightUnit)

        logger.debug("Converted {} {} to {} kg for storage", oneRepMax, unit, oneRepMaxInKg)

        return userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, oneRepMaxInKg)
            .doOnSuccess {
                logger.info(
                    "Successfully upserted 1RM for user {} exercise {}: {} {} (stored as {} kg)",
                    userId,
                    exerciseName,
                    oneRepMax,
                    unit,
                    oneRepMaxInKg
                )
                // Trigger performance score recalculation
                triggerPerformanceScoreRecalculation(userId)
            }
    }

    /**
     * Deletes a user one rep max record.
     *
     * @param userId The Keycloak identifier of the user
     * @param exerciseName The name of the exercise
     * @return Mono that completes when the deletion is done
     */
    fun deleteUserOneRepMax(
        userId: String,
        exerciseName: String
    ): Mono<UserOneRepMax> = userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)

    /**
     * Triggers performance score recalculation when 1RM data changes.
     * This ensures that strength scores and overall levels are updated.
     */
    private fun triggerPerformanceScoreRecalculation(userId: String) {
        logger.debug("Triggering performance score recalculation for user: {}", userId)

        // Get the user's latest performance metrics and weekly test data
        userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(userId)
            .flatMap { performanceMetrics ->
                // Get the latest weekly test data using PerformanceTrackingService
                performanceTrackingService.getWeeklyTests(userId)
                    .flatMap { weeklyTestResults ->
                        // Convert test results to UserWeeklyTest format using PerformanceTrackingService
                        val weeklyTest = performanceTrackingService.convertTestResultsToWeeklyTest(weeklyTestResults)

                        // Recalculate performance scores with updated 1RM data and weekly test data
                        performanceScoringService.calculatePerformanceScores(
                            performanceMetrics,
                            weeklyTest,
                            "one_rep_max_updated"
                        )
                    }
            }
            .flatMap { scores: UserPerformanceScores ->
                // Upsert the scores with day-based logic
                userPerformanceScoresDAL.upsertUserPerformanceScores(scores)
            }
            .doOnSuccess {
                logger.info("Successfully recalculated and upserted performance scores for user: {}", userId)
            }
            .doOnError { error ->
                logger.warn("Failed to recalculate performance scores for user: {} - {}", userId, error.message)
            }
            .onErrorComplete() // Don't fail the 1RM operation if performance recalculation fails
            .subscribe() // Fire and forget - don't block the 1RM operation
    }
}
