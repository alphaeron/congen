package com.congen.model

import com.congen.config.MovementTypeDeserializer
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Represents different types of movement patterns in strength training.
 *
 * @property displayName Human-readable name for the movement type
 */
@JsonDeserialize(using = MovementTypeDeserializer::class)
enum class MovementType(
    @JsonValue val displayName: String
) {
    HORIZONTAL_PUSH("horizontal_push"),
    VERTICAL_PUSH("vertical_push"),
    HORIZONTAL_PULL("horizontal_pull"),
    VERTICAL_PULL("vertical_pull"),
    SQUAT("squat"),
    HINGE("hinge"),
    LUNGE("lunge"),
    CORE("core"),
    PLYOMETRIC("plyometric"),
    CARRY("carry"),
    ISOLATION("isolation");

    companion object {
        /**
         * Converts a string to MovementType, case-insensitive.
         * Returns null if the string doesn't match any enum value.
         */
        fun fromString(value: String?): MovementType? {
            return values().find { it.displayName.equals(value, ignoreCase = true) }
        }
    }
}
