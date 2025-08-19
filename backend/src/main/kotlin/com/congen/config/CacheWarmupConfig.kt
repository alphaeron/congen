package com.congen.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for cache warmup functionality.
 *
 * This configuration allows controlling the cache warmup behavior,
 * including enabling/disabling the feature and configuring which data
 * should be warmed up.
 *
 * ## Configuration Properties
 *
 * - `enabled`: Whether cache warmup is enabled (default: true)
 * - `warmup-reference-data`: Whether to warm up reference data (default: true)
 * - `warmup-lists`: Whether to warm up frequently accessed lists (default: true)
 * - `warmup-relationships`: Whether to warm up relationship data (default: true)
 * - `warmup-user-data`: Whether to warm up user-specific data (default: true)
 * - `max-users-to-warmup`: Maximum number of users to warm up (default: 10)
 * - `popular-exercises`: List of popular exercises to warm up
 * - `popular-equipment`: List of popular equipment to warm up
 * - `popular-muscles`: List of popular muscles to warm up
 *
 * ## Usage
 *
 * Configure in application.properties:
 * ```properties
 * congen.cache.warmup.enabled=true
 * congen.cache.warmup.warmup-reference-data=true
 * congen.cache.warmup.warmup-lists=true
 * congen.cache.warmup.warmup-relationships=true
 * congen.cache.warmup.warmup-user-data=true
 * congen.cache.warmup.max-users-to-warmup=10
 * congen.cache.warmup.popular-exercises=Bench Press,Squat,Deadlift
 * congen.cache.warmup.popular-equipment=Barbell,Dumbbell
 * congen.cache.warmup.popular-muscles=Chest,Back,Legs
 * ```
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "congen.cache.warmup")
class CacheWarmupConfig {

    /**
     * Whether cache warmup is enabled.
     * Default: true
     */
    var enabled: Boolean = true

    /**
     * Whether to warm up reference data (exercises, equipment, muscles).
     * Default: true
     */
    var warmupReferenceData: Boolean = true

    /**
     * Whether to warm up frequently accessed lists.
     * Default: true
     */
    var warmupLists: Boolean = true

    /**
     * Whether to warm up relationship data.
     * Default: true
     */
    var warmupRelationships: Boolean = true

    /**
     * Whether to warm up user-specific data.
     * Default: true
     */
    var warmupUserData: Boolean = true

    /**
     * Maximum number of users to warm up data for.
     * Default: 10
     */
    var maxUsersToWarmup: Int = 10

    /**
     * List of popular exercises to warm up.
     * Default: Common compound exercises
     */
    var popularExercises: List<String> = listOf(
        "Bench Press", "Squat", "Deadlift", "Overhead Press", "Pull-up",
        "Push-up", "Row", "Lunge", "Plank", "Burpee"
    )

    /**
     * List of popular equipment to warm up.
     * Default: Common gym equipment
     */
    var popularEquipment: List<String> = listOf(
        "Barbell", "Dumbbell", "Pull-up Bar", "Bench", "Squat Rack"
    )

    /**
     * List of popular muscles to warm up.
     * Default: Major muscle groups
     */
    var popularMuscles: List<String> = listOf(
        "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    )
}
