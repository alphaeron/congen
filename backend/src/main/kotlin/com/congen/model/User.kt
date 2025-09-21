package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
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
 * - **Personal Details**: Name, age, weight, height
 * - **Fitness Preferences**: Equipment preferences, exercise preferences
 * - **Program Data**: Associated workout programs and preferences
 * - **Authentication**: Linked to Keycloak for secure access
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string (1-255 characters)
 * - **Age**: Optional integer greater than zero
 * - **Weight**: Optional integer greater than zero (in pounds)
 * - **Height**: Optional integer greater than zero (in inches)
 * - **Gender**: Optional string ("male" or "female")
 *
 * ## GDPR Compliance
 *
 * All personal data (name, age, weight, height, gender) is encrypted at rest using AES-256-GCM
 * encryption for GDPR compliance and data protection.
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
 * @property age User's age in years (optional)
 * @property weight User's weight in pounds (optional)
 * @property height User's height in inches (optional)
 * @property gender User's gender (optional, "male" or "female")
 * @property createdAt Timestamp when the user was created
 * @property updatedAt Timestamp when the user was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "A user profile in the workout generation system",
    example =
        "User(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
            "name=\"John Doe\", age=30, weight=180, height=72, gender=\"male\")",
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
    /** User's age in years (optional). */
    @Schema(
        description = "User's age in years",
        example = "30",
        minimum = "1",
    )
    @param:JsonProperty("age") val age: Int?,
    /** User's weight in pounds (optional). */
    @Schema(
        description = "User's weight in pounds",
        example = "180",
        minimum = "1",
    )
    @param:JsonProperty("weight") val weight: Int?,
    /** User's height in inches (optional). */
    @Schema(
        description = "User's height in inches",
        example = "72",
        minimum = "1",
    )
    @param:JsonProperty("height") val height: Int?,
    /** User's gender (optional). */
    @Schema(
        description = "User's gender",
        example = "male",
        allowableValues = ["male", "female"],
    )
    @param:JsonProperty("gender") val gender: String?,
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
