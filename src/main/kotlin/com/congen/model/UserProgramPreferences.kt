package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserProgramPreferences(
    @param:JsonProperty("user_id") val userId: Int,
    @param:JsonProperty("program_days_per_week") val programDaysPerWeek: Int,
    @param:JsonProperty("session_time_length_in_minutes") val sessionTimeLengthInMinutes: Int,
)
