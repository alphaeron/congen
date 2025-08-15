package com.congen

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExceptionHandlingIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should handle no results found exception with 404 status`() {
        val token = getValidToken("service")
        // Test no results found exception by trying to get a non-existent user
        webTestClient.get()
            .uri("/api/v1/user/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Not Found")
    }

    @Test
    fun `should handle user not found after creation`() {
        val token = getValidToken("user")
        // Create a valid user first
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                token = token
            )

        // Verify user exists
        val user = IntegrationTestHelpers.getTestUser(webTestClient, token = token)
        assert(user.keycloakId == userId)

        // Test getting a non-existent user
        webTestClient.get()
            .uri("/api/v1/user/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Not Found")
    }

    @Test
    fun `should handle multiple validation errors`() {
        val token = getValidToken("user")
        // Create a user first
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        
        // Test validation errors by trying to create user program preferences with invalid days per week
        webTestClient.post()
            .uri("/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=5&session_time_length_in_minutes=60")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { errorMessage ->
                errorMessage.contains("Program days per week must be 2, 3, or 4 days")
            }
    }

    @Test
    fun `should handle invalid equipment name`() {
        val token = getValidToken("user")
        // Test getting non-existent equipment
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid muscle name`() {
        val token = getValidToken("user")
        // Test getting non-existent muscle
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid exercise name`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid equipment and invalid equipment`() {
        val token = getValidToken("user")
        // Test getting valid equipment first
        val validEquipmentName = IntegrationTestHelpers.getTestEquipment()
        webTestClient.get()
            .uri("/api/v1/equipment/$validEquipmentName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent equipment
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid muscle and invalid muscle`() {
        val token = getValidToken("user")
        // Test getting valid muscle first
        val validMuscleName = IntegrationTestHelpers.getTestMuscle()
        webTestClient.get()
            .uri("/api/v1/muscle/$validMuscleName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent muscle
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid exercise and invalid exercise`() {
        val token = getValidToken("user")
        // Test getting valid exercise first
        val validExerciseName = IntegrationTestHelpers.getTestExercise()
        webTestClient.get()
            .uri("/api/v1/exercise/$validExerciseName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent exercise
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid program id`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/program/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid workout id`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/programmed_workout/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid set scheme id`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/set_scheme/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid program and invalid program`() {
        val token = getValidToken("user")
        // Create a user first
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // Create a valid program
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)

        // Test getting valid program
        webTestClient.get()
            .uri("/api/v1/program/$programId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent program
        webTestClient.get()
            .uri("/api/v1/program/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid workout and invalid workout`() {
        val token = getValidToken("user")
        // Create a user and program first
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)

        // Create a valid workout
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, token = token)

        // Test getting valid workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/$workoutId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }
}
