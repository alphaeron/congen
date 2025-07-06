package com.congen

import com.congen.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserProgramPreferencesValidationIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // No-op, user creation will be handled in each test
    }

    @Test
    fun `should return 422 when program_days_per_week is 1`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=1&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 1")
    }

    @Test
    fun `should return 422 when program_days_per_week is 5`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=5&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should return 422 when program_days_per_week is 0`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=0&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0")
    }

    @Test
    fun `should return 422 when program_days_per_week is 8`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=8&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8")
    }

    @Test
    fun `should accept valid program_days_per_week value 2`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=2&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 3`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 4`() {
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should allow changing session time when user has existing workouts`() {
        // Create user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Create program preferences
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate-workout-generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing session time
        webTestClient.patch()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=90")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should prevent changing program days per week when user has existing workouts`() {
        // Create user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Create program preferences with 3 days per week
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate-workout-generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 3 to 4
        webTestClient.patch()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo(
                "Cannot change program days per week from 3 to 4 for user ${userResponse.id} because they have existing workouts. Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts and maintain program consistency. To change program frequency, the user must start a new program."
            )
    }

    @Test
    fun `should prevent changing program days per week from 4 to 3 when user has existing workouts`() {
        // Create user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Create program preferences with 4 days per week
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate-workout-generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Should prevent changing program days per week from 4 to 3
        webTestClient.patch()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo(
                "Cannot change program days per week from 4 to 3 for user ${userResponse.id} because they have existing workouts. Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts and maintain program consistency. To change program frequency, the user must start a new program."
            )
    }

    @Test
    fun `should allow changing program days per week when user has no existing workouts`() {
        // Create user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Create program preferences with 3 days per week
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Should allow changing program days per week when no workouts exist
        webTestClient.patch()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should allow changing program days per week after deleting all workouts`() {
        // Create user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Create program preferences with 3 days per week
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // Generate a workout to create existing workouts
        webTestClient.post()
            .uri("/conjugate-workout-generator/${userResponse.id}/generate")
            .exchange()
            .expectStatus().isOk()

        // Delete the program (which cascades to delete workouts)
        webTestClient.get()
            .uri("/program/user/${userResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").value { programId: Long ->
                webTestClient.delete()
                    .uri("/program/$programId")
                    .exchange()
                    .expectStatus().isOk()
            }

        // Should now allow changing program days per week after workouts are deleted
        webTestClient.patch()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }
}
