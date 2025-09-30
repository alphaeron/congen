package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's weekly test protocol tracking.
 *
 * This model tracks the completion status and results of weekly performance tests
 * as part of the gamified tracking system. It supports the structured testing
 * protocol with different tests scheduled for specific days of the week.
 *
 * ## Weekly Test Protocol
 *
 * - **Monday**: Vertical Jump (MyJump2 app)
 * - **Wednesday**: Push-ups & Pull-ups (max in 1 minute)
 * - **Friday**: 12-minute Run + VO₂ Max calculation
 * - **Any Day**: HRV & HR Recovery (Oura/Whoop or manual)
 * - **Any Day**: Reflex Speed (Human Benchmark or similar)
 * - **Any Day**: Mobility Assessment (Functional Movement Screen or similar)
 *
 * ## Test Status
 *
 * Each test can have one of three statuses:
 * - **PENDING**: Not yet completed
 * - **COMPLETED**: Successfully completed with results
 * - **SKIPPED**: Intentionally skipped for the week
 *
 * @property keycloakId Unique Keycloak identifier for the user (primary key)
 * @property weekStartTimestamp Start timestamp of the week (Monday)
 * @property verticalJumpStatus Status of vertical jump test
 * @property verticalJumpResult Result in centimeters (if completed)
 * @property hrRecoveryStatus Status of HR recovery test
 * @property hrRecoveryResult Result in bpm drop (if completed)
 * @property reflexStatus Status of reflex test
 * @property reflexResult Result in milliseconds (if completed)
 * @property mobilityStatus Status of mobility test
 * @property mobilityResult Result as percentage (if completed)
 * @property createdAt Timestamp when the weekly test was created
 * @property updatedAt Timestamp when the weekly test was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "Weekly test protocol tracking for performance monitoring",
    example =
        "UserWeeklyTest(keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
            "weekStartDate=\"2023-08-07\", verticalJumpStatus=\"COMPLETED\", verticalJumpResult=52.0)"
)
data class UserWeeklyTest(
    /** Unique Keycloak identifier for the user (primary key). */
    @Schema(
        description = "Unique Keycloak identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        readOnly = true
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    /** Start timestamp of the week (Monday). */
    @Schema(
        description = "Start timestamp of the week (Monday)",
        example = "2023-08-07T00:00:00Z"
    )
    @param:JsonProperty("week_start_timestamp") val weekStartTimestamp: Instant,
    /** Status of vertical jump test. */
    @Schema(
        description = "Status of vertical jump test",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "SKIPPED"]
    )
    @param:JsonProperty("vertical_jump_status") val verticalJumpStatus: TestStatus,
    /** Result in centimeters (if completed). */
    @Schema(
        description = "Result in centimeters (if completed)",
        example = "52.0",
        minimum = "0"
    )
    @param:JsonProperty("vertical_jump_result") val verticalJumpResult: Double?,
    /** Status of HR recovery test. */
    @Schema(
        description = "Status of HR recovery test",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "SKIPPED"]
    )
    @param:JsonProperty("hr_recovery_status") val hrRecoveryStatus: TestStatus,
    /** Result in bpm drop (if completed). */
    @Schema(
        description = "Result in bpm drop (if completed)",
        example = "38.5",
        minimum = "0"
    )
    @param:JsonProperty("hr_recovery_result") val hrRecoveryResult: Double?,
    /** Status of reflex test. */
    @Schema(
        description = "Status of reflex test",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "SKIPPED"]
    )
    @param:JsonProperty("reflex_status") val reflexStatus: TestStatus,
    /** Result in milliseconds (if completed). */
    @Schema(
        description = "Result in milliseconds (if completed)",
        example = "312.5",
        minimum = "0"
    )
    @param:JsonProperty("reflex_result") val reflexResult: Double?,
    /** Status of mobility test. */
    @Schema(
        description = "Status of mobility test",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "SKIPPED"]
    )
    @param:JsonProperty("mobility_status") val mobilityStatus: TestStatus,
    /** Result as percentage (if completed). */
    @Schema(
        description = "Result as percentage (if completed)",
        example = "85.5",
        minimum = "0",
        maximum = "100"
    )
    @param:JsonProperty("mobility_result") val mobilityResult: Double?,
    /** Timestamp when the weekly test was created. */
    @Schema(
        description = "Timestamp when the weekly test was created",
        example = "2023-01-15T08:30:00Z",
        readOnly = true
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the weekly test was last updated. */
    @Schema(
        description = "Timestamp when the weekly test was last updated",
        example = "2023-08-09T09:45:30Z",
        readOnly = true
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant
)

/**
 * Enum representing the status of a weekly test.
 */
@Schema(
    description = "Status of a weekly test",
    example = "COMPLETED"
)
enum class TestStatus {
    /** Test has not been completed yet. */
    PENDING,

    /** Test has been successfully completed with results. */
    COMPLETED,

    /** Test was intentionally skipped for the week. */
    SKIPPED
}
