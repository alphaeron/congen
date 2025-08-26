package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
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
 * - **Personal Details**: Name
 * - **Fitness Preferences**: Equipment preferences, exercise preferences
 * - **Program Data**: Associated workout programs and preferences
 * - **Authentication**: Linked to Keycloak for secure access
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string (1-255 characters)
 *
 * ## Keycloak Integration
 *
 * Users are linked to Keycloak for authentication:
 * - Each user has a unique Keycloak ID as their primary identifier
 * - This ensures seamless integration with the authentication system
 * - Authorization checks use the Keycloak ID for security validation
 *
 * @param keycloakId Unique Keycloak identifier for the user (primary key)
 * @param name User's full name
 * @param createdAt Timestamp when the user was created
 * @param updatedAt Timestamp when the user was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A user profile in the workout generation system",
    example = "User(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", name=\"John Doe\")",
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
