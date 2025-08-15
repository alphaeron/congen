package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's consent record for GDPR compliance.
 *
 * This model contains the consent status and related metadata for a user's
 * data processing consent under GDPR Article 7. It tracks when consent was
 * given or withdrawn and when the record was last updated.
 *
 * @property keycloakId The user's Keycloak ID (primary identifier)
 * @property dataProcessingConsent Whether the user has given consent for data processing
 * @property consentTimestamp When consent was last given/withdrawn
 * @property createdAt When the consent record was created
 * @property updatedAt When the consent record was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "User consent record for GDPR data processing",
    example =
        "UserConsent(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
            "dataProcessingConsent=true, consentTimestamp=\"2023-08-09T10:15:30Z\", " +
            "updatedAt=\"2023-08-09T10:15:30Z\")"
)
data class UserConsent(
    /** The user's Keycloak ID (primary identifier). */
    @Schema(
        description = "User's Keycloak ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    /** Whether the user has given consent for data processing. */
    @Schema(
        description = "Whether consent has been given for data processing",
        example = "true"
    )
    @param:JsonProperty("data_processing_consent") val dataProcessingConsent: Boolean,
    /** When consent was last given/withdrawn. */
    @Schema(
        description = "Timestamp when consent was last given or withdrawn",
        example = "2023-08-09T10:15:30Z"
    )
    @param:JsonProperty("consent_timestamp") val consentTimestamp: Instant?,
    /** When the consent record was created. */
    @Schema(
        description = "Timestamp when the consent record was created",
        example = "2023-08-09T10:15:30Z"
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** When the consent record was last updated. */
    @Schema(
        description = "Timestamp when the consent record was last updated",
        example = "2023-08-09T10:15:30Z"
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
