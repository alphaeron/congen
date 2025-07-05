package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * Represents a set within a programmed exercise.
 *
 * A set scheme defines the specific parameters for a single set within an exercise,
 * including performance flags, tempo timing, weight, reps, and rest periods. This
 * provides detailed control over how each set is performed and tracked.
 *
 * ## Performance Flags
 *
 * - **AMRAP**: As Many Reps As Possible - perform maximum reps with given weight
 * - **EMOM**: Every Minute On the Minute - perform work at specific intervals
 * - **Tempo**: Use specific timing for eccentric, isometric, and concentric phases
 *
 * ## Tempo Format
 *
 * Tempo values use a single digit (0-9) representing seconds for each phase:
 * - **Eccentric**: Lowering phase (e.g., "3" = 3 seconds down)
 * - **Isometric**: Hold phase (e.g., "1" = 1 second hold)
 * - **Concentric**: Lifting phase (e.g., "1" = 1 second up)
 *
 * Example: "3-1-1" means 3 seconds down, 1 second hold, 1 second up.
 *
 * @property id Unique identifier for the set scheme
 * @property programmedExerciseId ID of the programmed exercise this set belongs to
 * @property setNumber Order of this set within the exercise (1-based)
 * @property wasSetPerformed Whether the set was completed
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
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "A set within a programmed exercise with performance parameters",
    example =
        "SetScheme(id=1, programmedExerciseId=5, setNumber=1, wasSetPerformed=true, " +
            "isAmrap=false, isEmom=false, useTempo=true, eccentricTempo=\"3\", " +
            "isometricTempo=\"1\", concentricTempo=\"1\", targetWeight=100.0, " +
            "performedWeight=100.0, targetRepCount=5, performedRepCount=5, restSeconds=180)",
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
    /** Whether the set was completed. */
    @Schema(
        description = "Whether the set was completed",
        example = "true",
        defaultValue = "true",
    )
    @param:JsonProperty("was_set_performed") val wasSetPerformed: Boolean = true,
    /** As Many Reps As Possible flag. */
    @Schema(
        description = "As Many Reps As Possible flag",
        example = "false",
        defaultValue = "false",
    )
    @param:JsonProperty("is_amrap") val isAmrap: Boolean = false,
    /** Every Minute On the Minute flag. */
    @Schema(
        description = "Every Minute On the Minute flag",
        example = "false",
        defaultValue = "false",
    )
    @param:JsonProperty("is_emom") val isEmom: Boolean = false,
    /** Whether to use tempo timing. */
    @Schema(
        description = "Whether to use tempo timing",
        example = "false",
        defaultValue = "false",
    )
    @param:JsonProperty("use_tempo") val useTempo: Boolean = false,
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
)
