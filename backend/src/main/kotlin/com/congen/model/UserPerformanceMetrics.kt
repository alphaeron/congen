package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's performance metrics for gamified tracking.
 *
 * This model contains all the performance indicators used to calculate
 * athleticism scores, HP/MP/Fatigue values, and skill levels. It supports
 * both manual test inputs and wearable device integration.
 *
 * ## Performance Metrics
 *
 * The system tracks six core performance domains:
 * - **Relative Strength**: Wilks score or pull-ups max
 * - **Explosiveness**: Vertical jump height (cm)
 * - **Aerobic Capacity**: VO₂ max estimate (ml/kg/min)
 * - **Recovery**: Heart rate recovery (bpm drop in 1 min)
 * - **Muscular Endurance**: Push-ups or pull-ups max in 1 min
 * - **Reaction Time**: Response time in milliseconds
 *
 * ## Wearable Integration
 *
 * Supports integration with:
 * - **Whoop**: Strain, recovery, HRV, sleep score
 * - **Oura**: HRV, VO₂ max estimate, sleep stages
 * - **Manual Input**: For users without wearables
 *
 * @property keycloakId Unique Keycloak identifier for the user (primary key)
 * @property relativeStrength Wilks score or pull-ups max (optional)
 * @property verticalJumpCm Vertical jump height in centimeters (optional)
 * @property vo2Max VO₂ max estimate in ml/kg/min (optional)
 * @property hrRecovery Heart rate recovery in bpm drop (optional)
 * @property muscularEndurance Push-ups or pull-ups max in 1 minute (optional)
 * @property reactionTimeMs Reaction time in milliseconds (optional)
 * @property whoopStrain Daily strain score from Whoop (optional)
 * @property whoopRecovery Daily recovery score from Whoop (optional)
 * @property whoopHrv Heart rate variability from Whoop (optional)
 * @property whoopSleepScore Sleep score from Whoop (optional)
 * @property ouraHrv Heart rate variability from Oura (optional)
 * @property ouraVo2Max VO₂ max estimate from Oura (optional)
 * @property ouraSleepScore Sleep score from Oura (optional)
 * @property createdAt Timestamp when the metrics were created
 * @property updatedAt Timestamp when the metrics were last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "User performance metrics for gamified tracking",
    example = "UserPerformanceMetrics(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
        "relativeStrength=350, verticalJumpCm=50, vo2Max=48, hrRecovery=40, " +
        "muscularEndurance=40, reactionTimeMs=350)"
)
data class UserPerformanceMetrics(
    /** Unique Keycloak identifier for the user (primary key). */
    @Schema(
        description = "Unique Keycloak identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        readOnly = true
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    
    /** VO₂ max estimate in ml/kg/min (optional). */
    @Schema(
        description = "VO₂ max estimate in ml/kg/min",
        example = "48",
        minimum = "0"
    )
    @param:JsonProperty("vo2_max") val vo2Max: Double?,
    
    /** Daily strain score (optional). If not provided, system will use subjective tiredness with higher weight. */
    @Schema(
        description = "Daily strain score from wearables (e.g., Whoop). " +
                "If not provided, system will rely more heavily on subjective tiredness.",
        example = "15.2",
        minimum = "0",
        maximum = "21"
    )
    @param:JsonProperty("strain") val strain: Double?,
    
    /** Daily recovery score (optional). */
    @Schema(
        description = "Daily recovery score",
        example = "78",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("recovery") val recovery: Double?,
    
    /** Heart rate variability (optional). */
    @Schema(
        description = "Heart rate variability",
        example = "52",
        minimum = "0"
    )
    @param:JsonProperty("hrv") val hrv: Double?,
    
    /** Sleep score (optional). */
    @Schema(
        description = "Sleep score",
        example = "84",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("sleep_score") val sleepScore: Double?,
    
    /** REM sleep duration in minutes (optional). */
    @Schema(
        description = "REM sleep duration in minutes",
        example = "80",
        minimum = "0"
    )
    @param:JsonProperty("rem_sleep_minutes") val remSleepMinutes: Double?,
    
    /** Deep sleep duration in minutes (optional). */
    @Schema(
        description = "Deep sleep duration in minutes",
        example = "120",
        minimum = "0"
    )
    @param:JsonProperty("deep_sleep_minutes") val deepSleepMinutes: Double?,
    
    /** Subjective tiredness rating (1-5 scale) (optional). Has higher impact on HP and fatigue when strain is not available. */
    @Schema(
        description = "Subjective tiredness rating (1=fresh, 5=exhausted). Has higher impact on HP and fatigue calculations when strain data is not available.",
        example = "3",
        minimum = "1",
        maximum = "5"
    )
    @param:JsonProperty("subjective_tiredness") val subjectiveTiredness: Int?,

    /** Timestamp when the metrics were created. */
    @Schema(
        description = "Timestamp when the metrics were created",
        example = "2023-01-15T08:30:00Z",
        readOnly = true
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    
    /** Timestamp when the metrics were last updated. */
    @Schema(
        description = "Timestamp when the metrics were last updated",
        example = "2023-08-09T09:45:30Z",
        readOnly = true
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
