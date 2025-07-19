package com.congen.model

import com.fasterxml.jackson.annotation.JsonCreator
import java.math.BigDecimal

/**
 * Represents a resistance band used in Dynamic Effort exercises.
 *
 * The band is constructed from its weight in pounds, and the color is determined
 * based on the weight range.
 */
data class Band(
    /** The weight of the band in pounds. */
    val weightLbs: BigDecimal
) {
    /**
     * The color of the band based on its weight.
     */
    val color: String
        get() =
            when {
                weightLbs >= BigDecimal("100") -> "Black"
                weightLbs >= BigDecimal("65") -> "Green"
                weightLbs >= BigDecimal("50") -> "Blue"
                weightLbs >= BigDecimal("30") -> "Red"
                weightLbs >= BigDecimal("15") -> "Orange"
                else -> "Unknown"
            }

    companion object {
        /** Set of allowed band weights in pounds. */
        val allowedWeights =
            setOf(
                // Orange
                BigDecimal("15"),
                // Red
                BigDecimal("30"),
                // Blue
                BigDecimal("50"),
                // Green
                BigDecimal("65"),
                // Black
                BigDecimal("100")
            )

        /**
         * Creates a Band from its weight if valid.
         * @param weightLbs The band weight in pounds
         * @return The corresponding Band, or null if not allowed
         */
        @JsonCreator
        fun fromWeight(weightLbs: BigDecimal): Band? {
            // Normalize the weight to remove trailing zeros for comparison
            val normalizedWeight = weightLbs.stripTrailingZeros()

            // Check if the normalized weight matches any allowed weight
            val matchingAllowedWeight =
                allowedWeights.find { allowedWeight ->
                    allowedWeight.stripTrailingZeros().compareTo(normalizedWeight) == 0
                }

            return matchingAllowedWeight?.let { Band(weightLbs) }
        }

        /**
         * Creates a Band from its weight string if valid.
         * @param weightLbs The band weight in pounds as string
         * @return The corresponding Band, or null if not allowed
         */
        @JsonCreator
        fun fromWeight(weightLbs: String): Band? =
            try {
                fromWeight(BigDecimal(weightLbs))
            } catch (e: NumberFormatException) {
                null
            }
    }
}
