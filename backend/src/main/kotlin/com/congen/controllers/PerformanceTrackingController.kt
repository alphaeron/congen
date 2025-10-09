package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.TestProtocol
import com.congen.model.TestStatus
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserTestResult
import com.congen.service.GdprComplianceService
import com.congen.service.PerformanceTrackingService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * REST controller for performance tracking and gamified metrics.
 *
 * Provides endpoints for managing performance metrics, calculating scores,
 * tracking weekly test protocols, and retrieving gamified fitness data.
 * All operations require proper authentication and authorization.
 *
 * ## Features
 *
 * - **Performance Metrics**: Submit and retrieve raw performance data
 * - **Score Calculation**: Get calculated HP/MP/Fatigue and athleticism levels
 * - **Weekly Tests**: Manage structured testing protocol
 * - **Wearable Integration**: Support for Whoop and Oura data
 *
 * @param performanceTrackingService Service for performance tracking operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/performance")
@Tag(name = "Performance Tracking", description = "APIs for gamified performance tracking and metrics")
class PerformanceTrackingController(
    private val performanceTrackingService: PerformanceTrackingService,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PerformanceTrackingController::class.java)
    }

    /**
     * Submits performance metrics for the current user.
     *
     * This endpoint allows users to submit their performance test results and wearable data.
     * The system will automatically calculate updated scores and HP/MP/Fatigue values.
     *
     * @param vo2Max VO2 Max value
     * @param strain Training strain value
     * @param recovery Recovery score
     * @param hrv Heart rate variability
     * @param sleepScore Sleep quality score
     * @param remSleepMinutes REM sleep duration in minutes
     * @param deepSleepMinutes Deep sleep duration in minutes
     * @param subjectiveTiredness Subjective tiredness level (1-10)
     * @return The updated performance scores
     *
     * @throws ValidationException if metrics data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PutMapping("/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Submit performance metrics",
        description = "Submit performance test results and wearable data. Automatically calculates updated scores.",
        parameters = [
            Parameter(name = "vo2_max", description = "VO2 Max value", required = false),
            Parameter(name = "strain", description = "Training strain value", required = false),
            Parameter(name = "recovery", description = "Recovery score", required = false),
            Parameter(name = "hrv", description = "Heart rate variability", required = false),
            Parameter(name = "sleep_score", description = "Sleep quality score", required = false),
            Parameter(name = "rem_sleep_minutes", description = "REM sleep duration in minutes", required = false),
            Parameter(name = "deep_sleep_minutes", description = "Deep sleep duration in minutes", required = false),
            Parameter(name = "subjective_tiredness", description = "Subjective tiredness level (1-10)", required = false)
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Performance metrics submitted successfully",
                content = [Content(schema = Schema(implementation = UserPerformanceScores::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun submitPerformanceMetrics(
        @RequestParam(value = "vo2_max", required = false) vo2Max: Double?,
        @RequestParam(value = "strain", required = false) strain: Double?,
        @RequestParam(value = "recovery", required = false) recovery: Double?,
        @RequestParam(value = "hrv", required = false) hrv: Double?,
        @RequestParam(value = "sleep_score", required = false) sleepScore: Double?,
        @RequestParam(value = "rem_sleep_minutes", required = false) remSleepMinutes: Double?,
        @RequestParam(value = "deep_sleep_minutes", required = false) deepSleepMinutes: Double?,
        @RequestParam(value = "subjective_tiredness", required = false) subjectiveTiredness: Int?
    ): Mono<ResponseEntity<UserPerformanceScores>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { currentUserId ->
                gdprComplianceService.withUserConsent(currentUserId) {
                    // Create metrics object with only the provided fields
                    val fullMetrics =
                        UserPerformanceMetrics(
                            keycloakId = currentUserId,
                            vo2Max = vo2Max,
                            strain = strain,
                            recovery = recovery,
                            hrv = hrv,
                            sleepScore = sleepScore,
                            remSleepMinutes = remSleepMinutes,
                            deepSleepMinutes = deepSleepMinutes,
                            subjectiveTiredness = subjectiveTiredness,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now()
                        )
                    performanceTrackingService.submitPerformanceMetrics(fullMetrics)
                        .map { ResponseEntity.ok(it) }
                }
            }
    }

    /**
     * Retrieves current performance scores for the authenticated user.
     *
     * This endpoint returns the calculated HP/MP/Fatigue values, athleticism level,
     * individual metric scores, and generated skills.
     *
     * @return The current performance scores
     *
     * @throws NoResultsFoundException if no performance scores exist for the user
     */
    @GetMapping("/scores")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current performance scores",
        description = "Retrieve calculated HP/MP/Fatigue values, athleticism level, and skills."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Performance scores retrieved successfully",
                content = [Content(schema = Schema(implementation = UserPerformanceScores::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - no performance scores exist for user"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getCurrentPerformanceScores(): Mono<ResponseEntity<UserPerformanceScores>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    performanceTrackingService.getCurrentPerformanceScores(keycloakId)
                        .map { ResponseEntity.ok(it) }
                }
            }
    }

    /**
     * Retrieves historical performance scores for the authenticated user within a date range.
     *
     * This endpoint provides access to the complete history of performance score calculations,
     * allowing for trend analysis, level progression tracking, and achievement milestone recognition.
     *
     * @param startDate Optional start date for the range (ISO 8601 format)
     * @param endDate Optional end date for the range (ISO 8601 format)
     * @return List of historical performance scores
     */
    @GetMapping("/scores/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get historical performance scores",
        description = "Retrieve historical performance scores within a date range for trend analysis and progression tracking."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Historical performance scores retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<UserPerformanceScores>::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid date format"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getPerformanceScoresHistory(
        @RequestParam(value = "start_date", required = false) startDate: String?,
        @RequestParam(value = "end_date", required = false) endDate: String?
    ): Mono<ResponseEntity<List<UserPerformanceScores>>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    try {
                        val startTimestamp = startDate?.let { Instant.parse(it) }
                        val endTimestamp = endDate?.let { Instant.parse(it) }

                        performanceTrackingService.getPerformanceScoresHistory(keycloakId, startTimestamp, endTimestamp)
                            .map { ResponseEntity.ok(it) }
                    } catch (e: Exception) {
                        Mono.just(ResponseEntity.badRequest().build())
                    }
                }
            }
    }

    /**
     * Retrieves current performance metrics for the authenticated user.
     *
     * This endpoint returns the raw performance data including test results
     * and wearable device data.
     *
     * @return The current performance metrics
     *
     * @throws NoResultsFoundException if no performance metrics exist for the user
     */
    @GetMapping("/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current performance metrics",
        description = "Retrieve raw performance test results and wearable data."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Performance metrics retrieved successfully",
                content = [Content(schema = Schema(implementation = UserPerformanceMetrics::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - no performance metrics exist for user"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getCurrentPerformanceMetrics(): Mono<ResponseEntity<UserPerformanceMetrics>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    performanceTrackingService.getCurrentPerformanceMetrics(keycloakId)
                        .map { ResponseEntity.ok(it) }
                }
            }
    }

    /**
     * Submits weekly test results for the current user.
     *
     * This endpoint handles the structured weekly testing protocol and automatically
     * integrates results into the overall performance tracking system.
     *
     * @param weekStartTimestamp Start timestamp of the test week
     * @param testName Name of the test performed
     * @param status Status of the test result
     * @param resultValue Numerical result value of the test
     * @return The updated test results
     *
     * @throws ValidationException if weekly test data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PutMapping("/weekly_test")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Submit weekly test results",
        description = "Submit results from the weekly testing protocol. Automatically integrates into performance tracking.",
        parameters = [
            Parameter(name = "week_start_timestamp", description = "Start timestamp of the test week", required = true),
            Parameter(name = "test_name", description = "Name of the test performed", required = true),
            Parameter(name = "status", description = "Status of the test result", required = true),
            Parameter(name = "result_value", description = "Numerical result value of the test", required = false)
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Weekly test submitted successfully",
                content = [Content(schema = Schema(implementation = UserTestResult::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun submitWeeklyTest(
        @RequestParam(value = "week_start_timestamp") weekStartTimestamp: String,
        @RequestParam(value = "test_name") testName: String,
        @RequestParam(value = "status") status: String,
        @RequestParam(value = "result_value", required = false) resultValue: Double?
    ): Mono<ResponseEntity<List<UserTestResult>>> {
        return try {
            // Validate required parameters
            if (weekStartTimestamp.isBlank() || testName.isBlank() || status.isBlank()) {
                logger.error("Missing required parameters: week_start_timestamp=$weekStartTimestamp, test_name=$testName, status=$status")
                return Mono.just(ResponseEntity.badRequest().build())
            }

            val parsedWeekStartTimestamp = Instant.parse(weekStartTimestamp)
            val parsedStatus = TestStatus.valueOf(status)

            keycloakUtil.getCurrentUserId()
                .flatMap { currentUserId ->
                    gdprComplianceService.withUserConsent(currentUserId) {
                        // Create the test result with the authenticated user's keycloak_id
                        val testResult =
                            UserTestResult(
                                id = null,
                                keycloakId = currentUserId,
                                weekStartTimestamp = parsedWeekStartTimestamp,
                                testName = testName,
                                status = parsedStatus,
                                resultValue = resultValue,
                                createdAt = Instant.now(),
                                updatedAt = Instant.now()
                            )

                        performanceTrackingService.submitWeeklyTest(listOf(testResult))
                            .map { ResponseEntity.ok(it) }
                    }
                }
        } catch (e: Exception) {
            logger.error("Error parsing parameters in submitWeeklyTest", e)
            Mono.just(ResponseEntity.badRequest().build())
        }
    }

    /**
     * Retrieves weekly tests for the authenticated user within a date range.
     *
     * This endpoint returns all weekly tests within the specified date range,
     * ordered by week start date (most recent first).
     *
     * @param startTimestamp The start timestamp of the range (inclusive)
     * @param endTimestamp The end timestamp of the range (inclusive)
     * @return List of weekly tests within the date range
     */
    @GetMapping("/weekly_test")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get weekly tests in date range",
        description = "Retrieve all weekly tests within the specified date range."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Weekly tests retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<UserTestResult>::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getWeeklyTestsInRange(
        @RequestParam(required = false) startTimestamp: Instant? = null,
        @RequestParam(required = false) endTimestamp: Instant? = null,
    ): Mono<ResponseEntity<List<UserTestResult>>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    performanceTrackingService.getWeeklyTests(
                        keycloakId,
                        startTimestamp,
                        endTimestamp
                    )
                        .map { ResponseEntity.ok(it) }
                }
            }
    }

    /**
     * Retrieves performance metrics for the authenticated user within a date range.
     *
     * This endpoint returns historical performance metrics data for trend analysis
     * and chart visualization.
     *
     * @param startTimestamp The start timestamp of the range
     * @param endTimestamp The end timestamp of the range
     * @return List of performance metrics within the specified range
     *
     * @throws ValidationException if date range is invalid
     */
    @GetMapping("/metrics/range")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get performance metrics in date range",
        description = "Retrieve historical performance metrics within the specified date range for trend analysis."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Performance metrics retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<UserPerformanceMetrics>::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getPerformanceMetricsInRange(
        @RequestParam startTimestamp: Instant,
        @RequestParam endTimestamp: Instant
    ): Mono<ResponseEntity<List<UserPerformanceMetrics>>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    performanceTrackingService.getPerformanceMetricsInRange(
                        keycloakId,
                        startTimestamp,
                        endTimestamp
                    )
                        .map { ResponseEntity.ok(it) }
                }
            }
    }

    /**
     * Retrieves the current test protocol configuration.
     *
     * This endpoint returns the dynamic test protocol configuration from the database,
     * including display names, descriptions, units, icons, and radar chart metadata.
     *
     * @return List of test protocol configurations
     */
    @GetMapping("/test_protocols")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get weekly test protocols",
        description = "Retrieve the current weekly test protocol configuration."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Test protocols retrieved successfully"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getTestProtocols(): Mono<ResponseEntity<List<TestProtocol>>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    performanceTrackingService.getTestProtocolsFromDatabase()
                        .map { protocols -> ResponseEntity.ok(protocols) }
                        .doOnSuccess { logger.debug("Test protocols retrieved successfully") }
                        .doOnError { logger.error("Failed to retrieve test protocols", it) }
                }
            }
    }
}
