package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Represents a user's test result for a specific test protocol.
 *
 * This model tracks individual test results for each test protocol,
 * allowing for dynamic test management without hardcoded columns.
 *
 * @property id Unique identifier for the test result
 * @property keycloakId Unique Keycloak identifier for the user
 * @property weekStartTimestamp Start timestamp of the week (Monday)
 * @property testName Name of the test protocol
 * @property status Status of the test (PENDING, COMPLETED, SKIPPED)
 * @property resultValue The actual test result value (if completed)
 * @property createdAt Timestamp when the test result was created
 * @property updatedAt Timestamp when the test result was last updated
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "Individual test result for a user",
    example =
        "UserTestResult(id=1, keycloakId=\"123e4567-e89b-12d3-a456-426614174000\", " +
            "testName=\"vertical_jump\", status=\"COMPLETED\", resultValue=52.0)"
)
data class UserTestResult(
    /** Unique identifier for the test result. */
    @Schema(
        description = "Unique identifier for the test result",
        example = "1",
        readOnly = true
    )
    @param:JsonProperty("id") val id: Int? = null,
    /** Unique Keycloak identifier for the user. */
    @Schema(
        description = "Unique Keycloak identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @param:JsonProperty("keycloak_id") val keycloakId: String,
    /** Start timestamp of the week (Monday). */
    @Schema(
        description = "Start timestamp of the week (Monday)",
        example = "2023-08-07T00:00:00Z"
    )
    @param:JsonProperty("week_start_timestamp") val weekStartTimestamp: Instant,
    /** Name of the test protocol. */
    @Schema(
        description = "Name of the test protocol",
        example = "vertical_jump"
    )
    @param:JsonProperty("test_name") val testName: String,
    /** Status of the test. */
    @Schema(
        description = "Status of the test",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "SKIPPED"]
    )
    @param:JsonProperty("status") val status: TestStatus,
    /** The actual test result value (if completed). */
    @Schema(
        description = "The actual test result value (if completed)",
        example = "52.0",
        minimum = "0"
    )
    @param:JsonProperty("result_value") val resultValue: Double?,
    /** Timestamp when the test result was created. */
    @Schema(
        description = "Timestamp when the test result was created",
        example = "2023-01-15T08:30:00Z",
        readOnly = true
    )
    @param:JsonProperty("created_at") val createdAt: Instant,
    /** Timestamp when the test result was last updated. */
    @Schema(
        description = "Timestamp when the test result was last updated",
        example = "2023-08-09T09:45:30Z",
        readOnly = true
    )
    @param:JsonProperty("updated_at") val updatedAt: Instant
)
