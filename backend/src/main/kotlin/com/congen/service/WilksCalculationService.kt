package com.congen.service

import com.congen.dal.UserOneRepMaxDAL
import com.congen.model.UserOneRepMax
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.math.pow

/**
 * Service for calculating Wilks score from user 1RM data.
 *
 * The Wilks score is a coefficient used to compare powerlifting totals across different body weights.
 * It's calculated using the user's body weight and their total (squat + bench press + deadlift).
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class WilksCalculationService(
    private val userOneRepMaxDAL: UserOneRepMaxDAL
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WilksCalculationService::class.java)

        /**
         * Wilks coefficient lookup table for men.
         * Based on official Wilks formula coefficients.
         * Source: https://en.wikipedia.org/wiki/Wilks_Coefficient
         */
        private val MALE_WILKS_COEFFICIENTS =
            doubleArrayOf(
                -216.0475144,
                16.2606339,
                -0.002388645,
                -0.00113732,
                7.01863E-06,
                -1.291E-08
            )

        /**
         * Wilks coefficient lookup table for women.
         * Based on official Wilks formula coefficients.
         * Source: https://en.wikipedia.org/wiki/Wilks_Coefficient
         */
        private val FEMALE_WILKS_COEFFICIENTS =
            doubleArrayOf(
                594.31747775582,
                -27.23842536447,
                0.82112226871,
                -0.00930733913,
                4.731582E-05,
                -9.054E-08
            )
    }

    /**
     * Calculates Wilks score for a user based on their 1RM data.
     *
     * @param keycloakId The user's Keycloak ID
     * @param bodyWeightKg The user's body weight in kilograms
     * @param isMale Whether the user is male (true) or female (false)
     * @return The calculated Wilks score, or null if insufficient data
     */
    fun calculateWilksScore(
        keycloakId: String,
        bodyWeightKg: Double,
        isMale: Boolean
    ): Mono<Double?> {
        logger.debug("Calculating Wilks score for user: $keycloakId")

        return userOneRepMaxDAL.selectUserOneRepMaxByUser(keycloakId)
            .map { oneRepMaxes ->
                try {
                    // Find the big three lifts (case-insensitive)
                    val squat = findOneRepMax(oneRepMaxes, listOf("squat", "back squat", "front squat"))
                    val bench = findOneRepMax(oneRepMaxes, listOf("bench press", "bench", "flat bench press"))
                    val deadlift = findOneRepMax(oneRepMaxes, listOf("deadlift", "conventional deadlift", "sumo deadlift"))

                    if (squat == null || bench == null || deadlift == null) {
                        logger.debug("Insufficient 1RM data for Wilks calculation. Missing: ${getMissingLifts(squat, bench, deadlift)}")
                        return@map null
                    }

                    // Calculate total
                    val total = squat + bench + deadlift

                    // Calculate Wilks coefficient
                    val wilksCoefficient = calculateWilksCoefficient(bodyWeightKg, isMale)

                    // Calculate Wilks score
                    val wilksScore = total.toDouble() * wilksCoefficient

                    logger.debug(
                        "Wilks calculation: Total=$total, BodyWeight=$bodyWeightKg, Coefficient=$wilksCoefficient, Score=$wilksScore"
                    )

                    wilksScore
                } catch (e: Exception) {
                    logger.error("Error calculating Wilks score for user $keycloakId", e)
                    null
                }
            }
    }

    /**
     * Finds a 1RM value for exercises matching the given names (case-insensitive).
     */
    private fun findOneRepMax(
        oneRepMaxes: List<UserOneRepMax>,
        exerciseNames: List<String>
    ): BigDecimal? {
        return oneRepMaxes.find { oneRepMax ->
            exerciseNames.any { name ->
                oneRepMax.exerciseName.lowercase().contains(name.lowercase())
            }
        }?.oneRepMax
    }

    /**
     * Gets the names of missing lifts for logging purposes.
     */
    private fun getMissingLifts(
        squat: BigDecimal?,
        bench: BigDecimal?,
        deadlift: BigDecimal?
    ): String {
        val missing = mutableListOf<String>()
        if (squat == null) missing.add("squat")
        if (bench == null) missing.add("bench press")
        if (deadlift == null) missing.add("deadlift")
        return missing.joinToString(", ")
    }

    /**
     * Calculates the Wilks coefficient based on body weight and gender using the official lookup table.
     *
     * Formula: 500 / (a0 + a1*x + a2*x^2 + a3*x^3 + a4*x^4 + a5*x^5)
     * where x is body weight in kg
     *
     * @param bodyWeightKg Body weight in kilograms
     * @param isMale True for male coefficients, false for female
     * @return Wilks coefficient
     */
    private fun calculateWilksCoefficient(
        bodyWeightKg: Double,
        isMale: Boolean
    ): Double {
        val coefficients = if (isMale) MALE_WILKS_COEFFICIENTS else FEMALE_WILKS_COEFFICIENTS

        // Calculate polynomial: a0 + a1*x + a2*x^2 + a3*x^3 + a4*x^4 + a5*x^5
        var polynomial = 0.0
        for (i in coefficients.indices) {
            polynomial += coefficients[i] * bodyWeightKg.pow(i)
        }

        // Wilks coefficient = 500 / polynomial
        return 500.0 / polynomial
    }
}
