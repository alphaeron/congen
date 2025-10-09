package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import java.time.Instant
import java.time.ZoneOffset

/**
 * Integration tests for PerformanceTrackingController.
 *
 * These tests verify the complete integration of the PerformanceTrackingController
 * with the Spring Boot application context, including HTTP endpoints, authentication,
 * and full request/response cycles.
 *
 * ## Test Coverage
 *
 * - **Performance Metrics**: Submit and retrieve performance data via HTTP
 * - **Score Calculation**: Get calculated HP/MP/Fatigue and athleticism levels
 * - **Weekly Tests**: Manage structured testing protocol
 * - **Test Protocols**: Retrieve test configuration
 * - **Error Handling**: HTTP error responses and status codes
 * - **Authentication**: Security integration
 * - **Request/Response**: Full HTTP request/response cycle validation
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class PerformanceTrackingIntegrationTest : BaseIntegrationTest() {
    // Test Data Constants
    companion object {
        private const val TEST_EXERCISE_NAME = "vertical_jump"

        // Performance Metrics Test Data
        private const val TEST_VO2_MAX = 45.0
        private const val TEST_STRAIN = 12.5
        private const val TEST_RECOVERY = 75.0
        private const val TEST_HRV = 55.0
        private const val TEST_SLEEP_SCORE = 80.0
        private const val TEST_REM_SLEEP_MINUTES = 90.0
        private const val TEST_DEEP_SLEEP_MINUTES = 120.0
        private const val TEST_SUBJECTIVE_TIREDNESS = 3

        // Weekly Test Data
        private const val TEST_RESULT_VALUE = 60.0

        // API Endpoints
        private const val PERFORMANCE_METRICS_ENDPOINT = "/api/v1/performance/metrics"
        private const val PERFORMANCE_SCORES_ENDPOINT = "/api/v1/performance/scores"
        private const val PERFORMANCE_SCORES_HISTORY_ENDPOINT = "/api/v1/performance/scores/history"
        private const val PERFORMANCE_METRICS_RANGE_ENDPOINT = "/api/v1/performance/metrics/range"
        private const val WEEKLY_TEST_ENDPOINT = "/api/v1/performance/weekly_test"
        private const val TEST_PROTOCOLS_ENDPOINT = "/api/v1/performance/test_protocols"
    }

    // Test Data
    private val now = Instant.now()
    private val weekStart =
        now.atZone(ZoneOffset.UTC).toLocalDate().let { date ->
            val dayOfWeek = date.dayOfWeek.value
            val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1
            date.minusDays(daysToSubtract.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant()
        }

    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        // Create test user and get tokens
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        IntegrationTestHelpers.createMinimalReferenceDataForUser(webTestClient, userId, token = userToken)

        // Ensure default performance data is created by making a request to get current scores
        // This will trigger the creation of default performance data if it doesn't exist
        webTestClient.get()
            .uri(PERFORMANCE_SCORES_ENDPOINT)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
    }

    @Nested
    @DisplayName("Performance Metrics Endpoints")
    inner class PerformanceMetricsTests {
        @Test
        fun `PUT performance metrics should submit metrics successfully`() {
            // When & Then
            webTestClient.put()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("vo2_max", TEST_VO2_MAX.toString())
                        .with("strain", TEST_STRAIN.toString())
                        .with("recovery", TEST_RECOVERY.toString())
                        .with("hrv", TEST_HRV.toString())
                        .with("sleep_score", TEST_SLEEP_SCORE.toString())
                        .with("rem_sleep_minutes", TEST_REM_SLEEP_MINUTES.toString())
                        .with("deep_sleep_minutes", TEST_DEEP_SLEEP_MINUTES.toString())
                        .with("subjective_tiredness", TEST_SUBJECTIVE_TIREDNESS.toString())
                )
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.keycloak_id").isEqualTo(userId)
                .jsonPath("$.strength_score").exists()
                .jsonPath("$.wilks_score").exists()
                .jsonPath("$.level").exists()
                .jsonPath("$.level_change_reason").exists()
                .jsonPath("$.hp").exists()
                .jsonPath("$.hp_loss").exists()
                .jsonPath("$.mp").exists()
                .jsonPath("$.mp_loss").exists()
                .jsonPath("$.fatigue").exists()
                .jsonPath("$.fatigue_loss").exists()
                .jsonPath("$.skills").isArray()
                .jsonPath("$.created_at").exists()
        }

        @Test
        fun `PUT performance metrics should handle partial metrics submission`() {
            // When & Then
            webTestClient.put()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("vo2_max", TEST_VO2_MAX.toString())
                        .with("recovery", TEST_RECOVERY.toString())
                )
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.keycloak_id").isEqualTo(userId)
        }

        @Test
        fun `PUT performance metrics should require authentication`() {
            // When & Then
            webTestClient.put()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("vo2_max", TEST_VO2_MAX.toString()))
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `GET performance metrics should return current metrics`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.keycloak_id").isEqualTo(userId)
                .jsonPath("$.created_at").exists()
                .jsonPath("$.updated_at").exists()
        }

        @Test
        fun `GET performance metrics should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `GET performance metrics range should return metrics in range`() {
            val startTimestamp = now.minusSeconds(86400) // 1 day ago
            val endTimestamp = now

            // When & Then
            webTestClient.get()
                .uri { builder ->
                    builder.path(PERFORMANCE_METRICS_RANGE_ENDPOINT)
                        .queryParam("startTimestamp", startTimestamp.toString())
                        .queryParam("endTimestamp", endTimestamp.toString())
                        .build()
                }
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
        }

        @Test
        fun `GET performance metrics range should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_METRICS_RANGE_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("Performance Scores Endpoints")
    inner class PerformanceScoresTests {
        @Test
        fun `GET performance scores should return current scores`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_SCORES_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.keycloak_id").isEqualTo(userId)
                .jsonPath("$.level").exists()
                .jsonPath("$.hp").exists()
                .jsonPath("$.mp").exists()
                .jsonPath("$.fatigue").exists()
                .jsonPath("$.skills").isArray()
                .jsonPath("$.created_at").exists()
        }

        @Test
        fun `GET performance scores should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_SCORES_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `GET performance scores history should return historical scores`() {
            val startDate = now.minusSeconds(604800) // 1 week ago
            val endDate = now

            // When & Then
            webTestClient.get()
                .uri { builder ->
                    builder.path(PERFORMANCE_SCORES_HISTORY_ENDPOINT)
                        .queryParam("start_date", startDate.toString())
                        .queryParam("end_date", endDate.toString())
                        .build()
                }
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
        }

        @Test
        fun `GET performance scores history should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_SCORES_HISTORY_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("Weekly Test Endpoints")
    inner class WeeklyTestTests {
        @Test
        fun `PUT performance weekly test should submit test results successfully`() {
            // When & Then
            webTestClient.put()
                .uri(
                    "$WEEKLY_TEST_ENDPOINT?week_start_timestamp=$weekStart" +
                        "&test_name=$TEST_EXERCISE_NAME&status=COMPLETED&result_value=$TEST_RESULT_VALUE"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].keycloak_id").isEqualTo(userId)
                .jsonPath("$[0].week_start_timestamp").exists()
                .jsonPath("$[0].test_name").isEqualTo(TEST_EXERCISE_NAME)
                .jsonPath("$[0].status").isEqualTo("COMPLETED")
                .jsonPath("$[0].result_value").isEqualTo(TEST_RESULT_VALUE)
        }

        @Test
        fun `PUT performance weekly test should handle pending status`() {
            // When & Then
            webTestClient.put()
                .uri("$WEEKLY_TEST_ENDPOINT?week_start_timestamp=$weekStart&test_name=$TEST_EXERCISE_NAME&status=PENDING")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].keycloak_id").isEqualTo(userId)
                .jsonPath("$[0].test_name").isEqualTo(TEST_EXERCISE_NAME)
                .jsonPath("$[0].status").isEqualTo("PENDING")
        }

        @Test
        fun `PUT performance weekly test should require authentication`() {
            // When & Then
            webTestClient.put()
                .uri(
                    "$WEEKLY_TEST_ENDPOINT?week_start_timestamp=$weekStart" +
                        "&test_name=$TEST_EXERCISE_NAME&status=COMPLETED&result_value=$TEST_RESULT_VALUE"
                )
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `GET performance weekly test should return tests in range`() {
            val startTimestamp = now.minusSeconds(604800) // 1 week ago
            val endTimestamp = now

            // When & Then
            webTestClient.get()
                .uri { builder ->
                    builder.path(WEEKLY_TEST_ENDPOINT)
                        .queryParam("startTimestamp", startTimestamp.toString())
                        .queryParam("endTimestamp", endTimestamp.toString())
                        .build()
                }
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
        }

        @Test
        fun `GET performance weekly test should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(WEEKLY_TEST_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("Test Protocol Endpoints")
    inner class TestProtocolTests {
        @Test
        fun `GET performance test protocols should return test protocols`() {
            // When & Then
            webTestClient.get()
                .uri(TEST_PROTOCOLS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].test_name").exists()
                .jsonPath("$[0].display_name").exists()
                .jsonPath("$[0].description").exists()
                .jsonPath("$[0].unit").exists()
                .jsonPath("$[0].icon_name").exists()
                .jsonPath("$[0].is_required").exists()
                .jsonPath("$[0].display_order").exists()
                .jsonPath("$[0].radar_chart_color").exists()
                .jsonPath("$[0].radar_chart_enabled").exists()
        }

        @Test
        fun `GET performance test protocols should require authentication`() {
            // When & Then
            webTestClient.get()
                .uri(TEST_PROTOCOLS_ENDPOINT)
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {
        @Test
        fun `should handle invalid date format in range queries`() {
            // When & Then
            webTestClient.get()
                .uri { builder ->
                    builder.path(PERFORMANCE_SCORES_HISTORY_ENDPOINT)
                        .queryParam("start_date", "invalid-date")
                        .queryParam("end_date", "invalid-date")
                        .build()
                }
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isBadRequest
        }

        @Test
        fun `should handle invalid test status in weekly test submission`() {
            // When & Then
            webTestClient.put()
                .uri(WEEKLY_TEST_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("week_start_timestamp", weekStart.toString())
                        .with("test_name", TEST_EXERCISE_NAME)
                        .with("status", "INVALID_STATUS")
                )
                .exchange()
                .expectStatus().isBadRequest
        }

        @Test
        fun `should handle missing required parameters`() {
            // When & Then
            webTestClient.put()
                .uri(WEEKLY_TEST_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("test_name", TEST_EXERCISE_NAME))
                .exchange()
                .expectStatus().isBadRequest
        }
    }

    @Nested
    @DisplayName("Content Type Validation")
    inner class ContentTypeTests {
        @Test
        fun `should accept form data for metrics submission`() {
            // When & Then
            webTestClient.put()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("vo2_max", TEST_VO2_MAX.toString()))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
        }

        @Test
        fun `should return JSON for all GET endpoints`() {
            // When & Then
            webTestClient.get()
                .uri(PERFORMANCE_SCORES_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)

            webTestClient.get()
                .uri(PERFORMANCE_METRICS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)

            webTestClient.get()
                .uri(TEST_PROTOCOLS_ENDPOINT)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
        }
    }
}
