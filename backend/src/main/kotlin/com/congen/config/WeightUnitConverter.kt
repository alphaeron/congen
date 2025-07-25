package com.congen.config

import com.congen.exceptions.ValidationException
import com.congen.model.WeightUnit
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for converting String URL parameters to WeightUnit enum.
 *
 * This converter allows Spring to automatically convert URL parameters like
 * "KG" or "LBS" to the corresponding WeightUnit enum value. It uses
 * the existing fromString method in the WeightUnit enum for matching.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WeightUnitConverter : Converter<String, WeightUnit> {
    /**
     * Converts a string to WeightUnit using the fromString method.
     *
     * @param source The string value from the URL parameter
     * @return The corresponding WeightUnit enum value
     * @throws ValidationException if the string doesn't match any enum value
     */
    override fun convert(source: String): WeightUnit {
        return try {
            WeightUnit.fromString(source)
        } catch (e: Exception) {
            throw ValidationException(
                "Invalid weight unit: $source. Valid values are: ${WeightUnit.values().joinToString { it.name }}"
            )
        }
    }
}
