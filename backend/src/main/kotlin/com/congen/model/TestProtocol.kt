package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a test protocol configuration for weekly testing.
 *
 * This model defines the structure and requirements for each test
 * in the weekly testing protocol, including scheduling, units, and descriptions.
 *
 * @property testName Name of the test
 * @property displayName Display name for the test
 * @property description Detailed description of the test
 * @property unit Unit of measurement for the test result
 * @property iconName Icon identifier for UI display
 * @property isRequired Whether this test is required for the protocol
 * @property displayOrder Order for displaying tests in UI
 * @property radarChartColor Color for radar chart display
 * @property radarChartEnabled Whether this test should appear in radar chart
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "Test protocol configuration for weekly testing",
    example = "TestProtocol(day=\"Monday\", testName=\"Vertical Jump\", description=\"Measure explosive power using MyJump2 app\")"
)
data class TestProtocol(
    /** Name of the test. */
    @Schema(
        description = "Name of the test",
        example = "vertical_jump"
    )
    @param:JsonProperty("test_name") val testName: String,
    
    /** Display name for the test. */
    @Schema(
        description = "Display name for the test",
        example = "Vertical Jump"
    )
    @param:JsonProperty("display_name") val displayName: String,
    
    /** Detailed description of the test. */
    @Schema(
        description = "Detailed description of the test",
        example = "Measure explosive power using MyJump2 app"
    )
    @param:JsonProperty("description") val description: String,
    
    /** Unit of measurement for the test result. */
    @Schema(
        description = "Unit of measurement for the test result",
        example = "cm"
    )
    @param:JsonProperty("unit") val unit: String,
    
    /** Icon identifier for UI display. */
    @Schema(
        description = "Icon identifier for UI display",
        example = "fitness_center"
    )
    @param:JsonProperty("icon_name") val iconName: String,
    
    /** Whether this test is required for the protocol. */
    @Schema(
        description = "Whether this test is required for the protocol",
        example = "true"
    )
    @param:JsonProperty("is_required") val isRequired: Boolean = true,
    
    /** Order for displaying tests in UI. */
    @Schema(
        description = "Order for displaying tests in UI",
        example = "1"
    )
    @param:JsonProperty("display_order") val displayOrder: Int = 0,
    
    /** Color for radar chart display. */
    @Schema(
        description = "Color for radar chart display",
        example = "#FF6B6B"
    )
    @param:JsonProperty("radar_chart_color") val radarChartColor: String = "#00bcd4",
    
    /** Whether this test should appear in radar chart. */
    @Schema(
        description = "Whether this test should appear in radar chart",
        example = "true"
    )
    @param:JsonProperty("radar_chart_enabled") val radarChartEnabled: Boolean = true
)
