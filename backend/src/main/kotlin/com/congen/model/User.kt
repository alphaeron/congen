package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant

/**
 * Represents a user in the workout generation system.
 *
 * Users are individuals who use the system to generate personalized workout programs.
 * Each user has a profile with basic information and can have associated preferences,
 * equipment, and program selections.
 *
 * ## User Profile Information
 *
 * Users contain:
 * - **Personal Details**: Name, age, height, weight
 * - **Fitness Preferences**: Equipment preferences, exercise preferences
 * - **Program Data**: Associated workout programs and preferences
 * - **Authentication**: Linked to Keycloak for secure access
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string (1-255 characters)
 * - **Age**: 1-150 years
 * - **Height**: 0.01-300 cm
 * - **Weight**: 0.01-1000 kg
 *
 * ## Keycloak Integration
 *
 * Users are linked to Keycloak for authentication:
 * - Each user has a unique Keycloak ID as their primary identifier
 * - This ensures seamless integration with the authentication system
 * - Authorization checks use the Keycloak ID for security validation
 *
 * @property keycloakId Unique Keycloak identifier for the user (primary key)
 * @property name User's full name
 * @property age User's age in years
 * @property height User's height in centimeters
 * @property weight User's weight in kilograms
 * @property createdAt Timestamp when the user was created
 * @property updatedAt Timestamp when the user was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A user profile in the workout generation system",
    example = "User(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", name=\"John Doe\", age=30, height=175.5, weight=80.0)",
)
data class User(
    /** Unique Keycloak identifier for the user (primary key). */
    @Schema(
        description = "Unique Keycloak identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        readOnly = true,
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    /** User's full name. */
    @Schema(
        description = "User's full name",
        example = "John Doe",
        minLength = 1,
        maxLength = 255,
    )
    @param:JsonProperty("name") val name: String,
    /** User's age in years. */
    @Schema(
        description = "User's age in years",
        example = "30",
    )
    @param:JsonProperty("age") val age: Int,
    /** User's height in centimeters. */
    @Schema(
        description = "User's height in centimeters",
        example = "175.5",
    )
    @param:JsonProperty("height") val height: BigDecimal,
    /** User's weight in kilograms. */
    @Schema(
        description = "User's weight in kilograms",
        example = "80.0",
    )
    @param:JsonProperty("weight") val weight: BigDecimal,
    /** Timestamp when the user was created. */
    @Schema(
        description = "Timestamp when the user was created",
        example = "2024-01-01T00:00:00Z",
        required = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the user was last updated. */
    @Schema(
        description = "Timestamp when the user was last updated",
        example = "2024-01-01T00:00:00Z",
        required = true,
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
)
