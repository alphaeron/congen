package com.congen.model

/**
 * Enum representing the different types of workout stages.
 *
 * This enum maps to the workout_stage_type table in the database and provides
 * type-safe access to stage types instead of using string literals throughout the codebase.
 *
 * @property displayName The human-readable name for this stage type
 * @property position The position order for this stage type in a workout
 */
enum class WorkoutStageTypeEnum(val displayName: String, val position: Int) {
    WARMUP("Warmup", 1),
    PRIMARY("Primary", 2),
    SECONDARY("Secondary", 3),
    ACCESSORY("Accessory", 4),
    CONDITIONING("Conditioning", 5),
    MOBILITY("Mobility", 6),
    COOLDOWN("Cooldown", 7);

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
