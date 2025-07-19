package com.congen

import com.congen.model.Program
import com.congen.model.WorkoutStage
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
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
            .uri("/api/v1/user_program_preferences/?userId=$userId&programDaysPerWeek=5&sessionTimeLengthInMinutes=60")
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
            .uri("/api/v1/conjugate_workout_generator/999999/generate")
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
            .uri("/api/v1/user_exercise_preference/?userId=$userId&exerciseName=Safety Bar Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/user_exercise_preference/?userId=$userId&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
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
                .uri("/api/v1/conjugate_workout_generator/$programId")
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
                .uri("/api/v1/conjugate_workout_generator/$programId")
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
            .uri("/api/v1/user_program_preferences/?userId=$userId&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
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
    fun `should generate DE set scheme with correct band and bar weights`() {
        // Set up user with 1RM for all three main lifts
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", oneRepMax = 200.0)
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat", oneRepMax = 350.0)
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Deadlift", oneRepMax = 400.0)
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3)

        // Add additional equipment needed for DE exercises
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bands")
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "safety squat bar")

        // Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Fetch programmed workouts for the program and get DE workout ID
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!
        val deWorkout =
            workoutsResponse.find { workout ->
                workout["name"]?.toString()?.contains("DE") == true
            } ?: throw AssertionError("No DE workout found")
        val deWorkoutId = (deWorkout["id"] as Number).toLong()

        // Fetch workout stages for the workout and find Primary stage (which contains the DE exercise)
        val stagesResponse =
            webTestClient.get()
                .uri("/api/v1/workout_stage/workout/$deWorkoutId")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!
        println("Stages response: $stagesResponse")
        val primaryStage =
            stagesResponse.find { stage ->
                stage.name.toString() == "Primary"
            } ?: throw AssertionError("No Primary stage found")
        val primaryStageId = primaryStage.id

        // Fetch programmed exercises for the Primary stage and get first exercise ID
        val exercisesResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_exercise/stage/$primaryStageId")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!
        val programmedExerciseId = (exercisesResponse[0]["id"] as Number).toLong()

        // Fetch set schemes for the programmed exercise and verify DE fields
        val setSchemesResponse =
            webTestClient.get()
                .uri("/api/v1/set_scheme/exercise/$programmedExerciseId")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Verify we have set schemes
        assert(setSchemesResponse.isNotEmpty()) { "No set schemes found for exercise" }

        // Get the first set scheme by its ID to verify band information
        val firstSetSchemeId = setSchemesResponse[0]["id"].toString().toLong()

        val response =
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String::class.java)
                .returnResult()
                .responseBody!!

        println("Response body: $response")

        // Verify that the set scheme has band information (indicating it's a DE exercise)
        webTestClient.get()
            .uri("/api/v1/set_scheme/$firstSetSchemeId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.band_weight_lbs").isNumber()
            .jsonPath("$.target_weight").exists()
            .jsonPath("$.target_weight").isNumber()
    }
}
