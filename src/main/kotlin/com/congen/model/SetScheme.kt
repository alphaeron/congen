package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SetScheme(
    @Schema(
        description = "Unique identifier for the set scheme",
        example = "1",
        readOnly = true,
    )
    @param:JsonProperty("id") val id: Long,
    @param:JsonProperty("programmed_exercise_id") val programmedExerciseId: Long,
    @param:JsonProperty("set_number") val setNumber: Int,
    @param:JsonProperty("was_set_performed") val wasSetPerformed: Boolean = true,
    @param:JsonProperty("is_amrap") val isAmrap: Boolean = false,
    @param:JsonProperty("is_emom") val isEmom: Boolean = false,
    @param:JsonProperty("use_tempo") val useTempo: Boolean = false,
    @param:JsonProperty("eccentric_tempo") val eccentricTempo: String?,
    @param:JsonProperty("isometric_tempo") val isometricTempo: String?,
    @param:JsonProperty("concentric_tempo") val concentricTempo: String?,
    @param:JsonProperty("target_weight") val targetWeight: BigDecimal?,
    @param:JsonProperty("performed_weight") val performedWeight: BigDecimal?,
    @param:JsonProperty("target_rep_count") val targetRepCount: Int?,
    @param:JsonProperty("performed_rep_count") val performedRepCount: Int?,
    @param:JsonProperty("rest_seconds") val restSeconds: Int?,
)
