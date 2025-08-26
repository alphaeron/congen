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
 * @param keycloakId Unique identifier from Keycloak
 * @param name User's full name (encrypted at rest)
 * @param createdAt When the user account was created
 * @param updatedAt When the user account was last updated
 * @param dataProcessingConsent Current consent status for data processing
 * @param consentTimestamp When consent was last given or withdrawn
 * @param exportTimestamp When this data export was generated
 * @param userEquipment User's equipment preferences
 * @param userExercisePreferences User's exercise preferences
 * @param userProgramPreferences User's program preferences
 * @param userOneRepMax User's one-rep-max records
 * @param userWeightUnitPreferences User's weight unit preferences
 * @param trainingPrograms User's training programs
 * @param auditLogs Audit logs for data access
 * @param dataRetentionPolicies Data retention policies
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
    /** User's training programs. */
    @param:JsonProperty("training_programs") val trainingPrograms: List<ProgramWithWorkouts>,
    /** Audit logs for data access. */
    @param:JsonProperty("audit_logs") val auditLogs: List<Any>,
    /** Data retention policies. */
    @param:JsonProperty("data_retention_policies") val dataRetentionPolicies: List<Any>
)

/**
 * Represents a training program with complete workout hierarchy for export purposes.
 *
 * This model extends the base Program model to include the complete workout structure
 * with stages, exercises, and set schemes for GDPR data portability exports.
 *
 * @param program The base program information
 * @param workouts Complete list of programmed workouts with their stages, exercises, and set schemes
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A training program with complete workout hierarchy for export",
    example = "ProgramWithWorkouts(id=1, name=\"Beginner Strength Program\", workouts=[...])",
)
data class ProgramWithWorkouts(
    /** The base program information. */
    val program: Program,
    /** Complete list of programmed workouts with their stages, exercises, and set schemes. */
    @param:JsonProperty("workouts") val workouts: List<ProgrammedWorkoutWithStages>
)

/**
 * Represents a programmed workout with complete stage hierarchy for export purposes.
 *
 * This model extends the base ProgrammedWorkout model to include the complete stage structure
 * with exercises and set schemes.
 *
 * @param workout The base programmed workout information
 * @param stages Complete list of workout stages with their exercises and set schemes
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A programmed workout with complete stage hierarchy for export",
    example = "ProgrammedWorkoutWithStages(id=1, name=\"Upper Body Strength\", stages=[...])",
)
data class ProgrammedWorkoutWithStages(
    /** The base programmed workout information. */
    val workout: ProgrammedWorkout,
    /** Complete list of workout stages with their exercises and set schemes. */
    @param:JsonProperty("stages") val stages: List<WorkoutStageWithExercises>
)

/**
 * Represents a workout stage with complete exercise hierarchy for export purposes.
 *
 * This model extends the base WorkoutStage model to include the complete exercise structure
 * with set schemes.
 *
 * @param stage The base workout stage information
 * @param exercises Complete list of programmed exercises with their set schemes
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A workout stage with complete exercise hierarchy for export",
    example = "WorkoutStageWithExercises(id=1, name=\"Main\", exercises=[...])",
)
data class WorkoutStageWithExercises(
    /** The base workout stage information. */
    val stage: WorkoutStage,
    /** Complete list of programmed exercises with their set schemes. */
    @param:JsonProperty("exercises") val exercises: List<ProgrammedExerciseWithSetSchemes>
)

/**
 * Represents a programmed exercise with complete set scheme hierarchy for export purposes.
 *
 * This model extends the base ProgrammedExercise model to include the complete set scheme structure.
 *
 * @param exercise The base programmed exercise information
 * @param setSchemes Complete list of set schemes for this exercise
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A programmed exercise with complete set scheme hierarchy for export",
    example = "ProgrammedExerciseWithSetSchemes(id=1, exerciseName=\"Bench Press\", setSchemes=[...])",
)
data class ProgrammedExerciseWithSetSchemes(
    /** The base programmed exercise information. */
    val exercise: ProgrammedExercise,
    /** Complete list of set schemes for this exercise. */
    @param:JsonProperty("set_schemes") val setSchemes: List<SetScheme>
)
