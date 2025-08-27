package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Model for GDPR privacy policy information.
 *
 * This model represents the complete privacy policy and data processing
 * information as required by GDPR Articles 13 and 14. It provides
 * transparency about data collection, processing, and user rights.
 *
 * @property dataController Information about the data controller
 * @property dataProcessing Details about data processing activities
 * @property userRights Information about user rights under GDPR
 * @property lastUpdated When the privacy policy was last updated
 * @property version The version of the privacy policy
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "Complete privacy policy and GDPR compliance information"
)
data class PrivacyPolicy(
    /** Information about the data controller. */
    @Schema(description = "Data controller information")
    @param:JsonProperty("data_controller") val dataController: DataController,
    /** Details about data processing activities. */
    @Schema(description = "Data processing details")
    @param:JsonProperty("data_processing") val dataProcessing: DataProcessing,
    /** Information about user rights under GDPR. */
    @Schema(description = "User rights under GDPR")
    @param:JsonProperty("user_rights") val userRights: UserRights,
    /** When the privacy policy was last updated. */
    @Schema(
        description = "When the privacy policy was last updated",
        example = "2025-08-08T00:00:00Z"
    )
    @param:JsonProperty("last_updated") val lastUpdated: String,
    /** The version of the privacy policy. */
    @Schema(
        description = "Privacy policy version",
        example = "1.0.0"
    )
    @param:JsonProperty("version") val version: String
)

/**
 * Data controller information for GDPR compliance.
 *
 * @param name Name of the data controller organization
 * @param contact Contact information for privacy inquiries
 * @param dpo Data Protection Officer contact (optional)
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Data controller information")
data class DataController(
    @Schema(description = "Name of the data controller", example = "Congen Fitness Application")
    @param:JsonProperty("name") val name: String,
    @Schema(description = "Contact email for privacy inquiries", example = "privacy@congen.com")
    @param:JsonProperty("contact") val contact: String,
    @Schema(description = "Data Protection Officer contact", example = "dpo@congen.com")
    @param:JsonProperty("dpo") val dpo: String?
)

/**
 * Data processing information for GDPR transparency.
 *
 * @param purposes List of purposes for data processing
 * @param legalBasis Legal basis for data processing
 * @param dataTypes Types of data collected
 * @param retentionPeriods Data retention periods by type
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Data processing information")
data class DataProcessing(
    @Schema(description = "Purposes for data processing")
    @param:JsonProperty("purposes") val purposes: List<String>,
    @Schema(description = "Legal basis for processing")
    @param:JsonProperty("legal_basis") val legalBasis: List<String>,
    @Schema(description = "Types of data collected")
    @param:JsonProperty("data_types") val dataTypes: List<String>,
    @Schema(description = "Data retention periods by data type")
    @param:JsonProperty("retention_periods") val retentionPeriods: Map<String, String>
)

/**
 * User rights information under GDPR.
 *
 * @param access Right of access information
 * @param rectification Right to rectification information
 * @param erasure Right to erasure information
 * @param portability Right to data portability information
 * @param objection Right to object information
 * @param complaint Right to file a complaint information
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "User rights under GDPR")
data class UserRights(
    @Schema(description = "Right of access information")
    @param:JsonProperty("access") val access: String,
    @Schema(description = "Right to rectification information")
    @param:JsonProperty("rectification") val rectification: String,
    @Schema(description = "Right to erasure information")
    @param:JsonProperty("erasure") val erasure: String,
    @Schema(description = "Right to data portability information")
    @param:JsonProperty("portability") val portability: String,
    @Schema(description = "Right to object information")
    @param:JsonProperty("objection") val objection: String,
    @Schema(description = "Right to file a complaint information")
    @param:JsonProperty("complaint") val complaint: String
)
