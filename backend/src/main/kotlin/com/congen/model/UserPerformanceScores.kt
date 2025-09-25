package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents calculated performance scores and gamified metrics for a user.
 *
 * This model contains all the calculated scores derived from performance metrics,
 * including individual metric scores, HP/MP/Fatigue values, athleticism level,
 * and generated skills. It provides the core data for the RPG-style dashboard.
 *
 * ## Score Components
 *
 * - **Individual Scores**: Each performance metric scored 0-100
 * - **Athleticism Score**: Overall fitness level (1-100)
 * - **Level**: Derived level based on athleticism score (1-20+)
 * - **HP**: Health Points - physical resilience
 * - **MP**: Magic Points - neural readiness
 * - **Fatigue**: Current fatigue level (0-100)
 * - **Skills**: Auto-generated skills based on metric thresholds
 *
 * @property keycloakId Unique Keycloak identifier for the user (primary key)
 * @property explosivenessScore Individual explosiveness score (0-100)
 * @property aerobicCapacityScore Individual aerobic capacity score (0-100)
 * @property recoveryScore Individual recovery score (0-100)
 * @property reactionTimeScore Individual reaction time score (0-100)
 * @property mobilityScore Individual mobility score (0-100)
 * @property level Athleticism level with tanh scaling (1-100)
 * @property hp Health Points - physical resilience (0-100)
 * @property hpLoss HP Loss - current HP reduction from daily stress factors (0-100)
 * @property mp Magic Points - neural readiness (0-100)
 * @property mpLoss MP Loss - current MP reduction from daily stress factors (0-100)
 * @property fatigue Current fatigue level (0-100)
 * @property fatigueLoss Fatigue Loss - current fatigue increase from daily stress factors (0-100)
 * @property skills List of auto-generated skills
 * @property createdAt Timestamp when the scores were calculated
 * @property updatedAt Timestamp when the scores were last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "Calculated performance scores and gamified metrics",
    example = "UserPerformanceScores(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
        "athleticismScore=73.5, level=12, hp=82.0, mp=76.0, fatigue=42.0)"
)
data class UserPerformanceScores(
    /** Unique Keycloak identifier for the user (primary key). */
    @Schema(
        description = "Unique Keycloak identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        readOnly = true
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,

    /** Individual explosiveness score (0-100). */
    @Schema(
        description = "Individual explosiveness score (0-100)",
        example = "68.2",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("explosiveness_score") val explosivenessScore: Double?,
    
    /** Individual aerobic capacity score (0-100). */
    @Schema(
        description = "Individual aerobic capacity score (0-100)",
        example = "71.8",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("aerobic_capacity_score") val aerobicCapacityScore: Double?,
    
    /** Individual recovery score (0-100). */
    @Schema(
        description = "Individual recovery score (0-100)",
        example = "82.4",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("recovery_score") val recoveryScore: Double?,

    /** Individual reaction time score (0-100). */
    @Schema(
        description = "Individual reaction time score (0-100)",
        example = "73.7",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("reaction_time_score") val reactionTimeScore: Double?,

    /** Individual mobility score (0-100). */
    @Schema(
        description = "Individual mobility score (0-100)",
        example = "68.5",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("mobility_score") val mobilityScore: Double?,

    /** Athleticism level with tanh scaling (1-100). */
    @Schema(
        description = "Athleticism level with tanh scaling (1-100)",
        example = "73",
        minimum = "1",
        maximum = "100"
    )
    @param:JsonProperty("level") val level: Int,
    
    /** Health Points - physical resilience (0-100). */
    @Schema(
        description = "Health Points - physical resilience (0-100)",
        example = "82.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("hp") val hp: Double,
    
    /** HP Loss - current HP reduction from daily stress factors (0-100). */
    @Schema(
        description = "HP Loss - current HP reduction from daily stress factors (0-100)",
        example = "15.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("hp_loss") val hpLoss: Double,
    
    /** Magic Points - neural readiness (0-100). */
    @Schema(
        description = "Magic Points - neural readiness (0-100)",
        example = "76.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("mp") val mp: Double,
    
    /** MP Loss - current MP reduction from daily stress factors (0-100). */
    @Schema(
        description = "MP Loss - current MP reduction from daily stress factors (0-100)",
        example = "12.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("mp_loss") val mpLoss: Double,
    
    /** Current fatigue level (0-100). */
    @Schema(
        description = "Current fatigue level (0-100)",
        example = "42.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("fatigue") val fatigue: Double,
    
    /** Fatigue Loss - current fatigue increase from daily stress factors (0-100). */
    @Schema(
        description = "Fatigue Loss - current fatigue increase from daily stress factors (0-100)",
        example = "25.0",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("fatigue_loss") val fatigueLoss: Double,
    
    /** List of auto-generated skills. */
    @Schema(
        description = "List of auto-generated skills",
        example = "[\"Explosive Power\", \"Iron Lungs\", \"Lightning Reflexes\"]"
    )
    @param:JsonProperty("skills") val skills: List<String>,
    
    /** Timestamp when the scores were calculated. */
    @Schema(
        description = "Timestamp when the scores were calculated",
        example = "2023-01-15T08:30:00Z",
        readOnly = true
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    
    /** Timestamp when the scores were last updated. */
    @Schema(
        description = "Timestamp when the scores were last updated",
        example = "2023-08-09T09:45:30Z",
        readOnly = true
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
