package com.congen

import com.congen.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserProgramPreferencesValidationIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private lateinit var userResponse: User

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User%20$unique&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!
    }

    @Test
    fun `should return 422 when program_days_per_week is 1`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=1&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Program days per week must be 2, 3, or 4 days"))
            }
    }

    @Test
    fun `should return 422 when program_days_per_week is 5`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=5&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should return 422 when program_days_per_week is 0`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=0&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0")
    }

    @Test
    fun `should return 422 when program_days_per_week is 8`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=8&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8")
    }

    @Test
    fun `should accept valid program_days_per_week value 2`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=2&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 3`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 4`() {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should allow changing session time when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userResponse.id, 3)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate_workout_generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing session time
        webTestClient.patch()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=90")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should prevent changing program days per week when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userResponse.id, 3)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate_workout_generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 3 to 4
        webTestClient.patch()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo(
                "Cannot change program days per week from 3 to 4 for user ${userResponse.id} because they have existing workouts. Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts and maintain program consistency. To change program frequency, the user must start a new program."
            )
    }

    @Test
    fun `should prevent changing program days per week from 4 to 3 when user has existing workouts`() {
        // Create user
        // Create all reference data (exercises, equipment, etc.) before generating workouts
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userResponse.id, 4)

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate_workout_generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 4 to 3
        webTestClient.patch()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo(
                "Cannot change program days per week from 4 to 3 for user ${userResponse.id} because they have existing workouts. Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts and maintain program consistency. To change program frequency, the user must start a new program."
            )
    }

    @Test
    fun `should allow changing program days per week when no workouts exist`() {
        // Create user
        // Create program preferences with 3 days per week
        webTestClient.post()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing program days per week when no workouts exist
        webTestClient.patch()
            .uri("/user_program_preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }
}
