package com.congen

import com.congen.model.Program
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ConjugateWorkoutGeneratorIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0
    private var programId: Long = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a unique user for each test
        val unique = System.nanoTime()
        userId = IntegrationTestHelpers.createTestUserWithId(webTestClient, "Test User $unique")
        // Always create a program for the user for each test
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = "Test Program $unique")
    }

    @Test
    fun `should generate 3-day conjugate workout program successfully`() {
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3)
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }

    @Test
    fun `should generate 2-day conjugate workout program successfully`() {
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 2)
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should generate 4-day conjugate workout program successfully`() {
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 4)
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(4)
    }

    @Test
    fun `should handle invalid programDaysPerWeek in database`() {
        // Create user program preferences with invalid days per week should fail
        webTestClient.post()
            .uri("/user_program_preferences/?userId=$userId&programDaysPerWeek=5&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should handle non-existent program`() {
        // When & Then - Try to generate for non-existent program
        webTestClient.post()
            .uri("/conjugate_workout_generator/999999/generate")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should generate workout with user exercise preferences`() {
        // Already created in setUp or not needed here
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Exercises already exist in migrations

        // Add exercise preferences (different exercises to avoid duplicate key constraint)
        webTestClient.post()
            .uri("/user_exercise_preference/?userId=$userId&exerciseName=Safety Bar Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user_exercise_preference/?userId=$userId&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user equipment`() {
        // Already created in setUp or not needed here
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Equipment already exists in migrations

        // Add user equipment (only add 'dumbbells' since 'power bar' is already added by reference data)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells")

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user one rep max data`() {
        // Already created in setUp or not needed here
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Exercises already exist in migrations

        // Add one rep max data
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press")
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat")

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user program preferences`() {
        // Add program preferences
        webTestClient.post()
            .uri("/user_program_preferences/?userId=$userId&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }
}
