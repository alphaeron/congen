package com.congen.config

import com.congen.exceptions.ValidationException
import com.congen.model.WorkoutStageTypeEnum
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for converting String URL parameters to WorkoutStageTypeEnum.
 *
 * This converter allows Spring to automatically convert URL parameters like
 * "Warmup" or "Primary" to the corresponding WorkoutStageTypeEnum value. It uses
 * the existing fromDisplayName method in the WorkoutStageTypeEnum for case-insensitive matching.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WorkoutStageTypeEnumConverter : Converter<String, WorkoutStageTypeEnum> {
    /**
     * Converts a string to WorkoutStageTypeEnum using the fromDisplayName method.
     *
     * @param source The string value from the URL parameter
     * @return The corresponding WorkoutStageTypeEnum value
     * @throws ValidationException if the string doesn't match any enum value
     */
    override fun convert(source: String): WorkoutStageTypeEnum {
        return WorkoutStageTypeEnum.fromDisplayName(source)
            ?: throw ValidationException(
                "Invalid workout stage type: $source. Valid values are: ${WorkoutStageTypeEnum.values().joinToString { it.displayName }}"
            )
    }
}
