package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProgramPreferencesIntegrationTest : BaseIntegrationTest() {
    private var programId: Long = 0L
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a single test user to avoid keycloak_user_id conflicts
        userToken = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)

        // Create a test program for the user
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
    }

    @Test
    fun `should get program preferences by program id`() {
        webTestClient.get()
            .uri("/api/v1/program_preferences/$programId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(60)
    }

    @Test
    fun `should update program preferences session time only`() {
        webTestClient.patch()
            .uri("/api/v1/program_preferences/?program_id=$programId&session_time_length_in_minutes=90")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.program_days_per_week").isEqualTo(4)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(90)
    }
}
