package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserTestResult
import com.congen.model.TestProtocol
import com.congen.service.GdprComplianceService
import com.congen.service.PerformanceTrackingService
import com.congen.service.WilksCalculationService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/performance")
@Tag(name = "Performance Tracking", description = "APIs for gamified performance tracking and metrics")
class PerformanceTrackingController(
    private val performanceTrackingService: PerformanceTrackingService,
    private val wilksCalculationService: WilksCalculationService,
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
     * @param metrics The performance metrics to submit
     * @return The updated performance scores
     *
     * @throws ValidationException if metrics data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PutMapping("/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Submit performance metrics",
        description = "Submit performance test results and wearable data. Automatically calculates updated scores."
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
    fun submitPerformanceMetrics(@RequestBody metrics: UserPerformanceMetrics): Mono<ResponseEntity<UserPerformanceScores>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == metrics.keycloakId) {
                val consentUserIdMono = if (isAdminOrService) {
                    Mono.just(metrics.keycloakId)
                } else {
                    Mono.just(currentUserId)
                }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        performanceTrackingService.submitPerformanceMetrics(metrics)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only submit metrics for themselves"))
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
     * @param weeklyTest The weekly test results to submit
     * @return The updated weekly test
     *
     * @throws ValidationException if weekly test data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PutMapping("/weekly_test")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Submit weekly test results",
        description = "Submit results from the weekly testing protocol. Automatically integrates into performance tracking."
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
    fun submitWeeklyTest(@RequestBody testResults: List<UserTestResult>): Mono<ResponseEntity<List<UserTestResult>>> {
        if (testResults.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build())
        }
        
        val keycloakId = testResults.first().keycloakId
        
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == keycloakId) {
                val consentUserIdMono = if (isAdminOrService) {
                    Mono.just(keycloakId)
                } else {
                    Mono.just(currentUserId)
                }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        performanceTrackingService.submitWeeklyTest(testResults)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only submit test results for themselves"))
            }
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
        @RequestParam startTimestamp: Instant,
        @RequestParam endTimestamp: Instant
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
     * Retrieves the Wilks score for the authenticated user.
     *
     * This endpoint calculates the Wilks score based on the user's 1RM data
     * for the big three lifts (squat, bench press, deadlift) and their body weight.
     *
     * @param bodyWeightKg The user's body weight in kilograms
     * @param isMale Whether the user is male (true) or female (false)
     * @return The calculated Wilks score, or null if insufficient data
     */
    @GetMapping("/wilks")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get Wilks score",
        description = "Calculate Wilks score based on 1RM data for big three lifts."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Wilks score calculated successfully"
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
    fun getWilksScore(
        @RequestParam("body_weight_kg") bodyWeightKg: Double,
        @RequestParam("is_male") isMale: Boolean
    ): Mono<ResponseEntity<Double?>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                gdprComplianceService.withUserConsent(keycloakId) {
                    wilksCalculationService.calculateWilksScore(keycloakId, bodyWeightKg, isMale)
                        .map { ResponseEntity.ok(it) }
                        .doOnSuccess { logger.debug("Wilks score calculated successfully") }
                        .doOnError { logger.error("Failed to calculate Wilks score", it) }
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
