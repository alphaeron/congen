package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data class representing a program with its preferences.
 *
 * This class combines a Program entity with its associated ProgramPreferences,
 * providing a convenient way to access both program data and its configuration
 * in a single object.
 *
 * @property program The program entity
 * @property programPreferences The program preferences.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
    description = "Program with its associated preferences",
    example = "ProgramWithPreferences(program=Program(...), programPreferences=ProgramPreferences(...))",
)
data class ProgramWithPreferences(
    /** The program entity. */
    @Schema(
        description = "The program entity",
        required = true,
    )
    @param:JsonProperty("program") val program: Program,
    /** The program preferences (required). */
    @Schema(
        description = "The program preferences (required)",
        required = true,
    )
    @param:JsonProperty("program_preferences") val programPreferences: ProgramPreferences
)
