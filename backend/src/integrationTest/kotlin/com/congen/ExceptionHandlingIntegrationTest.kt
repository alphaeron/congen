package com.congen

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExceptionHandlingIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should handle validation exception with 422 status`() {
        // Test validation exception by providing invalid user data
        webTestClient.post()
            .uri(
                "/api/v1/user/?name=TestUser&age=0&height=175.0&weight=70.0" +
                    "&email=test.${System.nanoTime()}@example.com&password=testpassword123"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("User age must be between 1 and 150, got: 0")
    }

    @Test
    fun `should handle no results found exception with 404 status`() {
        // Test no results found exception by trying to get a non-existent user
        webTestClient.get()
            .uri("/api/v1/user/999")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Resource not found")
    }

    @Test
    fun `should handle user not found after creation`() {
        // Create a valid user first
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = "Test User for 404",
                age = 25,
                height = 175.0,
                weight = 70.0
            )

        // Verify user exists
        val user = IntegrationTestHelpers.getTestUser(webTestClient, userId)
        assert(user.id == userId)

        // Test getting a non-existent user
        webTestClient.get()
            .uri("/api/v1/user/999")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Resource not found")
    }

    @Test
    fun `should handle multiple validation errors`() {
        // Test multiple validation errors
        webTestClient.post()
            .uri(
                "/api/v1/user/?name=TestUser&age=0&height=0&weight=0" +
                    "&email=test.${System.nanoTime()}@example.com&password=testpassword123"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { errorMessage ->
                errorMessage.contains("User age must be between 1 and 150")
            }
    }

    @Test
    fun `should handle invalid equipment name`() {
        // Test getting non-existent equipment
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid muscle name`() {
        // Test getting non-existent muscle
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid exercise name`() {
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid equipment and invalid equipment`() {
        // Test getting valid equipment first
        val validEquipmentName = IntegrationTestHelpers.getTestEquipment()
        webTestClient.get()
            .uri("/api/v1/equipment/$validEquipmentName")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent equipment
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid muscle and invalid muscle`() {
        // Test getting valid muscle first
        val validMuscleName = IntegrationTestHelpers.getTestMuscle()
        webTestClient.get()
            .uri("/api/v1/muscle/$validMuscleName")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent muscle
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid exercise and invalid exercise`() {
        // Test getting valid exercise first
        val validExerciseName = IntegrationTestHelpers.getTestExercise()
        webTestClient.get()
            .uri("/api/v1/exercise/$validExerciseName")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent exercise
        webTestClient.get()
            .uri("/api/v1/exercise/NonExistentExercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid program id`() {
        webTestClient.get()
            .uri("/api/v1/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid workout id`() {
        webTestClient.get()
            .uri("/api/v1/programmed_workout/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle invalid set scheme id`() {
        webTestClient.get()
            .uri("/api/v1/set_scheme/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid program and invalid program`() {
        // Create a user first
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)

        // Create a valid program
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)

        // Test getting valid program
        webTestClient.get()
            .uri("/api/v1/program/$programId")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent program
        webTestClient.get()
            .uri("/api/v1/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle valid workout and invalid workout`() {
        // Create a user and program first
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)

        // Create a valid workout
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId)

        // Test getting valid workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/$workoutId")
            .exchange()
            .expectStatus().isOk()

        // Test getting non-existent workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/999")
            .exchange()
            .expectStatus().isNotFound()
    }
}
