package com.congen.util

import com.congen.model.WeightUnit
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility for converting between different weight units.
 *
 * This utility provides conversion utilities for weight measurements,
 * primarily between kilograms (kg) and pounds (lbs). All internal
 * calculations are performed in kilograms, but users can input and
 * view weights in their preferred units.
 *
 * ## Conversion Factors
 *
 * - 1 kg = 2.20462 lbs
 * - 1 lb = 0.453592 kg
 *
 * ## Usage
 *
 * ```kotlin
 * // Convert user input to kg for storage
 * val weightInKg = unitConverter.toKg(weightInLbs, WeightUnit.LBS)
 *
 * // Convert stored kg to user's preferred units for display
 * val weightInLbs = unitConverter.fromKg(weightInKg, WeightUnit.LBS)
 * ```
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UnitConverter {
    companion object {
        /** Conversion factor: 1 kg = 2.20462 lbs */
        private val KG_TO_LBS = BigDecimal("2.20462")

        /** Conversion factor: 1 lb = 0.453592 kg */
        private val LBS_TO_KG = BigDecimal("0.453592")

        /** Default scale for weight calculations (2 decimal places) */
        private const val WEIGHT_SCALE = 2
    }

    /**
     * Converts a weight from the specified unit to kilograms.
     *
     * This method is used when users input weights in their preferred units
     * and we need to convert to kg for internal storage and calculations.
     *
     * @param weight The weight value to convert
     * @param fromUnit The unit of the input weight
     * @return The weight converted to kilograms
     */
    fun toKg(
        weight: BigDecimal,
        fromUnit: WeightUnit
    ): BigDecimal {
        return when (fromUnit) {
            WeightUnit.KG -> weight
            WeightUnit.LBS -> weight.multiply(LBS_TO_KG).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
        }
    }

    /**
     * Converts a weight from kilograms to the specified unit.
     *
     * This method is used when we need to display weights to users
     * in their preferred units.
     *
     * @param weightInKg The weight in kilograms to convert
     * @param toUnit The unit to convert to
     * @return The weight converted to the specified unit
     */
    fun fromKg(
        weightInKg: BigDecimal,
        toUnit: WeightUnit
    ): BigDecimal {
        return when (toUnit) {
            WeightUnit.KG -> weightInKg
            WeightUnit.LBS -> weightInKg.multiply(KG_TO_LBS).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
        }
    }

    /**
     * Converts a weight from one unit to another.
     *
     * This method provides direct conversion between any two weight units.
     *
     * @param weight The weight value to convert
     * @param fromUnit The unit of the input weight
     * @param toUnit The unit to convert to
     * @return The weight converted to the target unit
     */
    fun convert(
        weight: BigDecimal,
        fromUnit: WeightUnit,
        toUnit: WeightUnit
    ): BigDecimal {
        if (fromUnit == toUnit) {
            return weight
        }

        val weightInKg = toKg(weight, fromUnit)
        return fromKg(weightInKg, toUnit)
    }
}
