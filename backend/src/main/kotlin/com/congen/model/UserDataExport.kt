package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's exportable data for GDPR compliance.
 *
 * This model contains all personal data that can be exported for a user
 * in response to a data portability request under GDPR Article 20. It provides
 * a complete snapshot of the user's data at the time of export.
 *
 * @property keycloakId Unique identifier from Keycloak
 * @property name User's full name (encrypted at rest)
 * @property createdAt When the user account was created
 * @property updatedAt When the user account was last updated
 * @property dataProcessingConsent Current consent status for data processing
 * @property consentTimestamp When consent was last given or withdrawn
 * @property exportTimestamp When this data export was generated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "Complete user data export for GDPR data portability requests",
    example =
        "UserDataExport(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
            "name=\"John Doe\", dataProcessingConsent=true, " +
            "exportTimestamp=\"2023-08-09T10:15:30Z\")"
)
data class UserDataExport(
    /** The user's unique Keycloak identifier. */
    @Schema(
        description = "User's Keycloak ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    /** User's full name. */
    @Schema(
        description = "User's full name",
        example = "John Doe"
    )
    @param:JsonProperty("name") val name: String,
    /** When the user account was created. */
    @Schema(
        description = "Timestamp when the user account was created",
        example = "2023-01-15T08:30:00Z"
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** When the user account was last updated. */
    @Schema(
        description = "Timestamp when the user account was last updated",
        example = "2023-08-09T09:45:30Z"
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    /** Current consent status for data processing. */
    @Schema(
        description = "Whether the user has given consent for data processing",
        example = "true"
    )
    @param:JsonProperty("data_processing_consent") val dataProcessingConsent: Boolean,
    /** When consent was last given or withdrawn. */
    @Schema(
        description = "Timestamp when consent was last given or withdrawn",
        example = "2023-08-09T10:00:00Z"
    )
    @param:JsonProperty("consent_timestamp") val consentTimestamp: Instant?,
    /** When this data export was generated. */
    @Schema(
        description = "Timestamp when this data export was generated",
        example = "2023-08-09T10:15:30Z"
    )
    @param:JsonProperty("export_timestamp") val exportTimestamp: Instant,
    /** User's equipment preferences. */
    @param:JsonProperty("user_equipment") val userEquipment: List<Any>,
    /** User's exercise preferences. */
    @param:JsonProperty("user_exercise_preferences") val userExercisePreferences: List<Any>,
    /** User's program preferences. */
    @param:JsonProperty("user_program_preferences") val userProgramPreferences: Any?,
    /** User's one-rep-max records. */
    @param:JsonProperty("user_one_rep_max") val userOneRepMax: List<Any>,
    /** User's weight unit preferences. */
    @param:JsonProperty("user_weight_unit_preferences") val userWeightUnitPreferences: List<Any>,
    /** User's exercise rotation history. */
    @param:JsonProperty("exercise_rotation_history") val exerciseRotationHistory: List<Any>,
    /** User's training programs. */
    @param:JsonProperty("training_programs") val trainingPrograms: List<TrainingProgramExport>,
    /** Audit logs for data access. */
    @param:JsonProperty("audit_logs") val auditLogs: List<Any>,
    /** Data retention policies. */
    @param:JsonProperty("data_retention_policies") val dataRetentionPolicies: List<Any>
)

/**
 * Represents a training program in the user data export.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TrainingProgramExport(
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("current_week_number") val currentWeekNumber: Int,
    @param:JsonProperty("created_at") val createdAt: Instant,
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    @param:JsonProperty("workouts") val workouts: List<WorkoutExport>
)

/**
 * Represents a workout in the user data export.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkoutExport(
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("day_number") val dayNumber: Int,
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("created_at") val createdAt: Instant,
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    @param:JsonProperty("stages") val stages: List<WorkoutStageExport>
)

/**
 * Represents a workout stage in the user data export.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkoutStageExport(
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("stage_type_id") val stageTypeId: Long,
    @param:JsonProperty("position") val position: Int,
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("created_at") val createdAt: Instant,
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    @param:JsonProperty("exercises") val exercises: List<ProgrammedExerciseExport>
)

/**
 * Represents a programmed exercise in the user data export.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgrammedExerciseExport(
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("exercise_name") val exerciseName: String,
    @param:JsonProperty("position") val position: Int,
    @param:JsonProperty("notes") val notes: String?,
    @param:JsonProperty("created_at") val createdAt: Instant,
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    @param:JsonProperty("set_schemes") val setSchemes: List<SetSchemeExport>
)

/**
 * Represents a set scheme in the user data export.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SetSchemeExport(
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("set_number") val setNumber: Int,
    @param:JsonProperty("is_amrap") val isAmrap: Boolean,
    @param:JsonProperty("is_emom") val isEmom: Boolean,
    @param:JsonProperty("use_tempo") val useTempo: Boolean,
    @param:JsonProperty("eccentric_tempo") val eccentricTempo: String?,
    @param:JsonProperty("isometric_tempo") val isometricTempo: String?,
    @param:JsonProperty("concentric_tempo") val concentricTempo: String?,
    @param:JsonProperty("target_weight") val targetWeight: java.math.BigDecimal?,
    @param:JsonProperty("performed_weight") val performedWeight: java.math.BigDecimal?,
    @param:JsonProperty("target_rep_count") val targetRepCount: Int?,
    @param:JsonProperty("performed_rep_count") val performedRepCount: Int?,
    @param:JsonProperty("rest_seconds") val restSeconds: Int?,
    @param:JsonProperty("created_at") val createdAt: Instant,
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
