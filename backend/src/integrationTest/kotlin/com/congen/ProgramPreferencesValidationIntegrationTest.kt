package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProgramPreferencesValidationIntegrationTest : BaseIntegrationTest() {
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
    fun `should return 422 when session_time_length_in_minutes is 0`() {
        // First create a program (program preferences are created automatically)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=0"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Session time length must be between 15 and 180 minutes"))
            }
    }

    @Test
    fun `should return 422 when session_time_length_in_minutes is 14`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=14"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Session time length must be between 15 and 180 minutes. Only valid session times are between 15 and 180 minutes, got: 14")
    }

    @Test
    fun `should return 422 when session_time_length_in_minutes is 181`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=181"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Session time length must be between 15 and 180 minutes. Only valid session times are between 15 and 180 minutes, got: 181")
    }

    @Test
    fun `should return 422 when session_time_length_in_minutes is negative`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=-1"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Session time length must be between 15 and 180 minutes. Only valid session times are between 15 and 180 minutes, got: -1")
    }

    @Test
    fun `should accept valid session_time_length_in_minutes value 15`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=15"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.session_time_length_in_minutes").isEqualTo(15)
    }

    @Test
    fun `should accept valid session_time_length_in_minutes value 180`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=180"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid session_time_length_in_minutes value 90`() {
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should allow changing session time when program has existing workouts`() {
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
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should return 404 when program does not exist`() {
        val nonExistentProgramId = 99999L
        
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$nonExistentProgramId" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 403 when user does not own the program`() {
        // Create a program owned by a different user
        val otherUserToken = getValidToken("user")
        val otherUserId = IntegrationTestHelpers.createTestUser(webTestClient, token = otherUserToken)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, otherUserId, "Other User's Program", token = otherUserToken)
        
        // Try to update program preferences for a program owned by another user
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `should allow changing session time when no workouts exist`() {
        // Create a program (program preferences are created automatically)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = userToken)
        
        // Should allow changing session time when no workouts exist
        webTestClient.patch()
            .uri(
                "/api/v1/program_preferences/?program_id=$programId" +
                    "&session_time_length_in_minutes=90"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
    }
}
