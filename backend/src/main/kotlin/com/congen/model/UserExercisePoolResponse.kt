package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a user's exercise pool for API responses.
 *
 * This model provides a structured representation of the user's available exercises
 * based on their preferences, equipment, and previous usage. It includes both
 * the available exercises and metadata about the pool.
 *
 * @property userId The ID of the user
 * @property totalExercises Total number of exercises in the system
 * @property availableExercises Number of exercises available to the user
 * @property primaryExercises List of available primary exercises
 * @property accessoryExercises List of available accessory exercises
 * @property userEquipment User's available equipment
 * @property userPreferences User's exercise preferences
 * @property previouslyUsedExercises List of exercises used in previous weeks
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "User's exercise pool with available exercises and metadata")
data class UserExercisePoolResponse(
    /** ID of the user (Keycloak ID). */
    @Schema(description = "ID of the user (Keycloak ID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @param:JsonProperty("user_id") val userId: String,
    
    /** Total number of exercises in the system. */
    @Schema(description = "Total number of exercises in the system", example = "150", required = true)
    @param:JsonProperty("total_exercises") val totalExercises: Int,
    
    /** Number of exercises available to the user. */
    @Schema(description = "Number of exercises available to the user", example = "120", required = true)
    @param:JsonProperty("available_exercises") val availableExercises: Int,
    
    /** List of available primary exercises. */
    @Schema(description = "List of available primary exercises", required = true)
    @param:JsonProperty("primary_exercises") val primaryExercises: List<Exercise>,
    
    /** List of available accessory exercises. */
    @Schema(description = "List of available accessory exercises", required = true)
    @param:JsonProperty("accessory_exercises") val accessoryExercises: List<Exercise>,
    
    /** User's available equipment. */
    @Schema(description = "User's available equipment", required = true)
    @param:JsonProperty("user_equipment") val userEquipment: List<UserEquipment>,
    
    /** User's exercise preferences. */
    @Schema(description = "User's exercise preferences", required = true)
    @param:JsonProperty("user_preferences") val userPreferences: List<UserExercisePreference>,
    
    /** List of exercises used in previous weeks. */
    @Schema(description = "List of exercises used in previous weeks", required = true)
    @param:JsonProperty("previously_used_exercises") val previouslyUsedExercises: List<String>
)
