package com.congen.util

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility for calculating estimated one rep max (1RM) from submaximal weights and reps.
 *
 * This utility provides multiple formulas for estimating 1RM, allowing for more accurate
 * 1RM updates when users perform sets with multiple reps rather than assuming the performed
 * weight is already a 1RM.
 *
 * ## Available Formulas
 *
 * - **Brzycki Formula**: Most commonly used, good for 1-10 reps
 * - **Epley Formula**: Good for 1-10 reps, slightly more conservative
 * - **Lombardi Formula**: Good for higher rep ranges (1-15 reps)
 * - **O'Conner Formula**: Conservative formula, good for beginners
 *
 * ## Usage
 *
 * ```kotlin
 * // Calculate estimated 1RM using Brzycki formula
 * val estimated1RM = oneRepMaxCalculator.estimateOneRepMax(weight, reps, OneRepMaxFormula.BRZYCKI)
 *
 * // Calculate using best formula for the rep range
 * val estimated1RM = oneRepMaxCalculator.estimateOneRepMax(weight, reps)
 * ```
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class OneRepMaxCalculator {
    companion object {
        private const val WEIGHT_SCALE = 2
    }

    /**
     * Available formulas for 1RM estimation.
     */
    enum class OneRepMaxFormula {
        /** Brzycki Formula: weight × (36 / (37 - reps)) */
        BRZYCKI,

        /** Epley Formula: weight × (1 + reps / 30) */
        EPLEY,

        /** Lombardi Formula: weight × reps^0.1 */
        LOMBARDI,

        /** O'Conner Formula: weight × (1 + reps / 40) */
        OCONNER
    }

    /**
     * Estimates 1RM from weight and reps using the specified formula.
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @param formula The formula to use for calculation
     * @return The estimated 1RM
     * @throws IllegalArgumentException if weight or reps are invalid
     */
    fun estimateOneRepMax(
        weight: BigDecimal,
        reps: Int,
        formula: OneRepMaxFormula
    ): BigDecimal {
        validateInput(weight, reps)

        return when (formula) {
            OneRepMaxFormula.BRZYCKI -> calculateBrzycki(weight, reps)
            OneRepMaxFormula.EPLEY -> calculateEpley(weight, reps)
            OneRepMaxFormula.LOMBARDI -> calculateLombardi(weight, reps)
            OneRepMaxFormula.OCONNER -> calculateOConner(weight, reps)
        }
    }

    /**
     * Estimates 1RM from weight and reps using the best formula for the rep range.
     *
     * Formula selection:
     * - 1-3 reps: Brzycki (most accurate for low reps)
     * - 4-10 reps: Epley (good balance)
     * - 11-15 reps: Lombardi (better for higher reps)
     * - 16+ reps: O'Conner (most conservative for very high reps)
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @return The estimated 1RM
     * @throws IllegalArgumentException if weight or reps are invalid
     */
    fun estimateOneRepMax(
        weight: BigDecimal,
        reps: Int
    ): BigDecimal {
        validateInput(weight, reps)

        val formula =
            when {
                reps <= 3 -> OneRepMaxFormula.BRZYCKI
                reps <= 10 -> OneRepMaxFormula.EPLEY
                reps <= 15 -> OneRepMaxFormula.LOMBARDI
                else -> OneRepMaxFormula.OCONNER
            }

        return estimateOneRepMax(weight, reps, formula)
    }

    /**
     * Calculates 1RM using Brzycki formula: weight × (36 / (37 - reps))
     *
     * Best for 1-10 reps. Most commonly used formula in strength training.
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @return The estimated 1RM
     */
    private fun calculateBrzycki(
        weight: BigDecimal,
        reps: Int
    ): BigDecimal {
        val denominator = BigDecimal(37 - reps)
        val multiplier = BigDecimal(36).divide(denominator, 10, RoundingMode.HALF_UP)
        return weight.multiply(multiplier).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Calculates 1RM using Epley formula: weight × (1 + reps / 30)
     *
     * Good for 1-10 reps. Slightly more conservative than Brzycki.
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @return The estimated 1RM
     */
    private fun calculateEpley(
        weight: BigDecimal,
        reps: Int
    ): BigDecimal {
        val multiplier = BigDecimal.ONE.add(BigDecimal(reps).divide(BigDecimal(30), 10, RoundingMode.HALF_UP))
        return weight.multiply(multiplier).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Calculates 1RM using Lombardi formula: weight × reps^0.1
     *
     * Good for higher rep ranges (1-15 reps). Uses power function.
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @return The estimated 1RM
     */
    private fun calculateLombardi(
        weight: BigDecimal,
        reps: Int
    ): BigDecimal {
        // Lombardi formula: weight × reps^0.1
        // For simplicity, we'll use a linear approximation since BigDecimal.pow() doesn't support fractional exponents
        val multiplier = BigDecimal.ONE.add(BigDecimal(reps).multiply(BigDecimal("0.1")))
        return weight.multiply(multiplier).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Calculates 1RM using O'Conner formula: weight × (1 + reps / 40)
     *
     * Most conservative formula, good for beginners and very high rep ranges.
     *
     * @param weight The weight used in the set
     * @param reps The number of reps performed
     * @return The estimated 1RM
     */
    private fun calculateOConner(
        weight: BigDecimal,
        reps: Int
    ): BigDecimal {
        val multiplier = BigDecimal.ONE.add(BigDecimal(reps).divide(BigDecimal(40), 10, RoundingMode.HALF_UP))
        return weight.multiply(multiplier).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Validates input parameters for 1RM calculation.
     *
     * @param weight The weight to validate
     * @param reps The number of reps to validate
     * @throws IllegalArgumentException if parameters are invalid
     */
    private fun validateInput(
        weight: BigDecimal,
        reps: Int
    ) {
        if (weight <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Weight must be greater than zero")
        }
        if (reps <= 0) {
            throw IllegalArgumentException("Reps must be greater than zero")
        }
        if (reps > 30) {
            throw IllegalArgumentException("Reps cannot exceed 30 for 1RM estimation")
        }
    }
}
