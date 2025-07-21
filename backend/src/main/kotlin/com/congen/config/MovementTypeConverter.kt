package com.congen.config

import com.congen.model.MovementType
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for converting String URL parameters to MovementType enum.
 *
 * This converter allows Spring to automatically convert URL parameters like
 * "horizontal_push" to the corresponding MovementType enum value. It uses
 * the displayName property of the enum for matching.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class MovementTypeConverter : Converter<String, MovementType> {
    /**
     * Converts a string to MovementType using the displayName property.
     *
     * @param source The string value from the URL parameter
     * @return The corresponding MovementType enum value
     * @throws IllegalArgumentException if the string doesn't match any enum value
     */
    override fun convert(source: String): MovementType {
        return MovementType.fromString(source)
            ?: throw IllegalArgumentException(
                "Invalid movement type: $source. Valid values are: ${MovementType.values().joinToString { it.displayName }}"
            )
    }
}
