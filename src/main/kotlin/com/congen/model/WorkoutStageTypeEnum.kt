package com.congen.model

/**
 * Enum representing the different types of workout stages.
 *
 * This enum maps to the workout_stage_type table in the database and provides
 * type-safe access to stage types instead of using string literals throughout the codebase.
 *
 * @property displayName The human-readable name for this stage type
 */
enum class WorkoutStageTypeEnum(val displayName: String) {
    WARMUP("Warmup"),
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    ACCESSORY("Accessory"),
    COOLDOWN("Cooldown"),
    MOBILITY("Mobility"),
    CONDITIONING("Conditioning");

    companion object {
        /**
         * Find a WorkoutStageTypeEnum by its display name.
         *
         * @param displayName The display name to search for (case-insensitive)
         * @return The matching WorkoutStageTypeEnum or null if not found
         */
        fun fromDisplayName(displayName: String): WorkoutStageTypeEnum? {
            return values().find { it.displayName.equals(displayName, ignoreCase = true) }
        }
    }
} 