package com.congen.model

/**
 * Represents different types of movement patterns in strength training.
 *
 * @property displayName Human-readable name for the movement type
 */
enum class MovementType(val displayName: String) {
    HORIZONTAL_PUSH("horizontal push"),
    VERTICAL_PUSH("vertical push"),
    HORIZONTAL_PULL("horizontal pull"),
    VERTICAL_PULL("vertical pull"),
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
