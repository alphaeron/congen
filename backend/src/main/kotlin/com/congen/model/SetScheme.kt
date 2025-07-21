package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant

/**
 * Represents a set scheme for a programmed exercise.
 *
 * A set scheme defines the specific parameters for individual sets within a
 * programmed exercise, including weight, reps, tempo, and rest periods.
 *
 * @property id Unique identifier for the set scheme
 * @property programmedExerciseId The ID of the programmed exercise this set belongs to
 * @property setNumber The set number within the exercise
 * @property isAmrap As Many Reps As Possible flag
 * @property isEmom Every Minute On the Minute flag
 * @property useTempo Whether to use tempo timing
 * @property eccentricTempo Eccentric phase tempo (0-9 seconds)
 * @property isometricTempo Isometric phase tempo (0-9 seconds)
 * @property concentricTempo Concentric phase tempo (0-9 seconds)
 * @property targetWeight Target weight for the set in kg
 * @property performedWeight Actual weight used in kg
 * @property targetRepCount Target number of repetitions
 * @property performedRepCount Actual number of repetitions completed
 * @property restSeconds Rest period after the set in seconds
 * @property createdAt Timestamp when the set scheme was created
 * @property updatedAt Timestamp when the set scheme was last updated
 * @property band The band information for Dynamic Effort exercises
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A set within a programmed exercise with performance parameters",
    example =
        "SetScheme(id=1, programmedExerciseId=5, setNumber=1, isAmrap=false, isEmom=false, " +
            "useTempo=true, eccentricTempo=\"3\", isometricTempo=\"1\", concentricTempo=\"1\", " +
            "targetWeight=100.0, performedWeight=100.0, targetRepCount=5, performedRepCount=5, " +
            "restSeconds=180)",
)
data class SetScheme(
    /** Unique identifier for the set scheme. */
    @Schema(
        description = "Unique identifier for the set scheme",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    /** ID of the programmed exercise this set belongs to. */
    @Schema(
        description = "ID of the programmed exercise this set belongs to",
        example = "5",
        required = true,
    )
    @param:JsonProperty("programmed_exercise_id") val programmedExerciseId: Long,
    /** Order of this set within the exercise (1-based). */
    @Schema(
        description = "Order of this set within the exercise (1-based)",
        example = "1",
        required = true,
    )
    @param:JsonProperty("set_number") val setNumber: Int,
    /** Whether this set scheme is AMRAP (As Many Reps As Possible). */
    @Schema(description = "Whether this set scheme is AMRAP", example = "false")
    @param:JsonProperty("is_amrap")
    @get:JsonProperty("is_amrap") val isAmrap: Boolean,
    /** Whether this set scheme is EMOM (Every Minute On the Minute). */
    @Schema(description = "Whether this set scheme is EMOM", example = "false")
    @param:JsonProperty("is_emom")
    @get:JsonProperty("is_emom") val isEmom: Boolean,
    /** Whether tempo should be used for this set scheme. */
    @Schema(description = "Whether tempo should be used for this set scheme", example = "true")
    @param:JsonProperty("use_tempo")
    @get:JsonProperty("use_tempo") val useTempo: Boolean,
    /** Eccentric phase tempo (0-9 seconds). */
    @Schema(
        description = "Eccentric phase tempo (0-9 seconds)",
        example = "3",
        pattern = "^[0-9]$",
    )
    @param:JsonProperty("eccentric_tempo") val eccentricTempo: String?,
    /** Isometric phase tempo (0-9 seconds). */
    @Schema(
        description = "Isometric phase tempo (0-9 seconds)",
        example = "1",
        pattern = "^[0-9]$",
    )
    @param:JsonProperty("isometric_tempo") val isometricTempo: String?,
    /** Concentric phase tempo (0-9 seconds). */
    @Schema(
        description = "Concentric phase tempo (0-9 seconds)",
        example = "1",
        pattern = "^[0-9]$",
    )
    @param:JsonProperty("concentric_tempo") val concentricTempo: String?,
    /** Target weight for the set in kg. */
    @Schema(
        description = "Target weight for the set in kg",
        example = "100.0",
        minimum = "0.01",
    )
    @param:JsonProperty("target_weight") val targetWeight: BigDecimal?,
    /** Actual weight used in kg. */
    @Schema(
        description = "Actual weight used in kg",
        example = "100.0",
        minimum = "0.01",
    )
    @param:JsonProperty("performed_weight") val performedWeight: BigDecimal?,
    /** Target number of repetitions. */
    @Schema(
        description = "Target number of repetitions",
        example = "5",
        minimum = "1",
        maximum = "1000",
    )
    @param:JsonProperty("target_rep_count") val targetRepCount: Int?,
    /** Actual number of repetitions completed. */
    @Schema(
        description = "Actual number of repetitions completed",
        example = "5",
        minimum = "1",
        maximum = "1000",
    )
    @param:JsonProperty("performed_rep_count") val performedRepCount: Int?,
    /** Rest period after the set in seconds. */
    @Schema(
        description = "Rest period after the set in seconds",
        example = "180",
        minimum = "0",
        maximum = "3600",
    )
    @param:JsonProperty("rest_seconds") val restSeconds: Int?,
    /** Timestamp when the set scheme was created. */
    @Schema(
        description = "Timestamp when the set scheme was created",
        example = "2024-01-01T12:00:00Z",
        readOnly = true,
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the set scheme was last updated. */
    @Schema(
        description = "Timestamp when the set scheme was last updated",
        example = "2024-01-01T12:00:00Z",
        readOnly = true,
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant,
    /** Band information for Dynamic Effort exercises. */
    @param:JsonProperty("band_weight_lbs") val band: Band?,
)
