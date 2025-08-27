package com.congen.model

import com.congen.exceptions.InvalidWeightUnitException

/**
 * Enum representing weight units supported by the system.
 */
enum class WeightUnit {
    /** Kilograms */
    KG,

    /** Pounds */
    LBS;

    companion object {
        /**
         * Parses a string to a WeightUnit, case-insensitive, with a clear error message if invalid.
         *
         * @param unit The string representation of the weight unit to parse
         * @return The parsed WeightUnit enum value
         * @throws InvalidWeightUnitException if the input is not KG or LBS
         */
        fun fromString(unit: String?): WeightUnit {
            if (unit == null) throw InvalidWeightUnitException("Weight unit must be provided.")
            return try {
                valueOf(unit.uppercase())
            } catch (e: Exception) {
                throw InvalidWeightUnitException("Invalid weight unit: $unit. Must be KG or LBS.")
            }
        }
    }
}
