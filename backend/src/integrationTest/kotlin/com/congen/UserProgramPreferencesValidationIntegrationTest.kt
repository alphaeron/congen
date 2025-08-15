package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class UserProgramPreferencesValidationIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
    }

    @Test
    fun `should return 422 when program_days_per_week is 1`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=1" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Program days per week must be 2, 3, or 4 days"))
            }
    }

    @Test
    fun `should return 422 when program_days_per_week is 5`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=5" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should return 422 when program_days_per_week is 0`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=0" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0")
    }

    @Test
    fun `should return 422 when program_days_per_week is 8`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=8" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8")
    }

    @Test
    fun `should accept valid program_days_per_week value 2`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=2" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.program_days_per_week").isEqualTo(2)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should accept valid program_days_per_week value 3`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=3" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 4`() {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=4" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should allow changing session time when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3, token = userToken)

        // Create a program first
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program", token = userToken)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/api/v1/conjugate_workout_generator/$programId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing session time
        webTestClient.patch()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=3" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should prevent changing program days per week when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3, token = userToken)

        // Create a program first
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program", token = userToken)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/api/v1/conjugate_workout_generator/$programId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 3 to 4
        webTestClient.patch()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=4" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo(
                "Cannot change program days per week from 3 to 4 for user " +
                    "$userId because they have existing workouts. " +
                    "Program days per week becomes immutable once workouts are " +
                    "generated to prevent day numbering conflicts and maintain program consistency. " +
                    "To change program frequency, the user must start a new program."
            )
    }

    @Test
    fun `should prevent changing program days per week from 4 to 3 when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 4, token = userToken)

        // Create a program first
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program", token = userToken)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/api/v1/conjugate_workout_generator/$programId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 4 to 3
        val userIdStr = userId.toString()
        val errorPart1 = "Cannot change program days per week from 4 to 3 for user "
        val errorPart2 = userIdStr + " because they have existing workouts. "
        val errorPart3 = "Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts "
        val errorPart4 = "and maintain program consistency. To change program frequency, the user must start a new program."
        val expectedError = errorPart1 + errorPart2 + errorPart3 + errorPart4
        webTestClient.patch()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=3" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error")
            .isEqualTo(expectedError)
    }

    @Test
    fun `should allow changing program days per week when no workouts exist`() {
        // Create user
        // Create program preferences with 3 days per week
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=3" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing program days per week when no workouts exist
        webTestClient.patch()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=4" +
                    "&session_time_length_in_minutes=60"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }
}
