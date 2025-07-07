package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a user profile in the workout generation system.
 *
 * Users are the primary entities in the system and contain personal information
 * used to generate personalized workout programs. Each user can have associated
 * preferences, equipment, and program selections.
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string
 * - **Age**: Must be between 1 and 150 years
 * - **Height**: Must be between 0.01 and 300 cm
 * - **Weight**: Must be between 0.01 and 1000 kg
 *
 * ## Usage
 *
 * Users are created through the `/user/` endpoint and can be updated or deleted
 * as needed. User data is validated before persistence to ensure data integrity.
 *
 * @property id Unique identifier for the user (auto-generated)
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
    description = "User profile information for workout generation",
)
data class User(
    /** Unique identifier for the user (auto-generated). */
    @Schema(
        description = "Unique identifier for the user",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Int,
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
    @param:JsonProperty("created_at") val createdAt: LocalDateTime,
    /** Timestamp when the user was last updated. */
    @Schema(
        description = "Timestamp when the user was last updated",
        example = "2024-01-01T00:00:00Z",
        required = true,
    )
    @param:JsonProperty("updated_at") val updatedAt: LocalDateTime,
)
