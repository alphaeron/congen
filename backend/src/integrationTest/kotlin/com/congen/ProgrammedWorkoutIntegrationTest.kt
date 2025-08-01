package com.congen

import com.congen.model.ProgrammedWorkout
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ProgrammedWorkoutIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var programId: Long = 0
    private var userId: Int = 0
    private val programName = "Test Program"
    private val programDescription = "Test program for integration tests"

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Always create a valid user and program for each test
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = programName, token = token)
    }

    @Test
    fun `should return 422 when day number is 0`() {
        val token = getValidToken("user")
        webTestClient.post()
            .uri("/api/v1/programmed_workout/?program_id=$programId&day_number=0&name=Test Workout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 0")
    }

    @Test
    fun `should return 422 when day number is 366`() {
        val token = getValidToken("user")
        webTestClient.post()
            .uri("/api/v1/programmed_workout/?program_id=$programId&day_number=366&name=Test Workout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 366")
    }

    @Test
    fun `should accept valid programmed workout data`() {
        val token = getValidToken("user")
        webTestClient.post()
            .uri("/api/v1/programmed_workout/?program_id=$programId&day_number=1&name=Test Workout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.day_number").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Test Workout")
    }

    @Test
    fun `should get programmed workout by id`() {
        val token = getValidToken("user")
        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/api/v1/programmed_workout/?program_id=$programId&day_number=5&name=Integration Test Workout")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then get the workout by id
        webTestClient.get()
            .uri("/api/v1/programmed_workout/${workoutResponse.id}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(workoutResponse.id)
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.day_number").isEqualTo(5)
            .jsonPath("$.name").isEqualTo("Integration Test Workout")
    }

    @Test
    fun `should get programmed workouts by program id`() {
        val token = getValidToken("user")
        // Create a second program for this test
        val userId2 = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId2 = IntegrationTestHelpers.createTestProgram(webTestClient, userId2, name = "Test Program 2", token = token)

        // First create multiple workouts for the same program
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 1, name = "Workout 1", token = token)
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 2, name = "Workout 2", token = token)
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 3, name = "Workout 3", token = token)

        // Then get all workouts for the program
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/$programId2")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].program_id").value { value: Any ->
                assert(value.toString() == programId2.toString())
            }
            .jsonPath("$[1].program_id").value { value: Any ->
                assert(value.toString() == programId2.toString())
            }
            .jsonPath("$[2].program_id").value { value: Any ->
                assert(value.toString() == programId2.toString())
            }
    }

    @Test
    fun `should get all programmed workouts`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workout1 = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1", token = token)
        val workout2 = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 2, name = "Workout 2", token = token)
        webTestClient.get()
            .uri("/api/v1/programmed_workout/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update programmed workout`() {
        val token = getValidToken("user")
        // Create a third program for this test
        val userId3 = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId3 = IntegrationTestHelpers.createTestProgram(webTestClient, userId3, name = "Test Program 3", token = token)

        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/api/v1/programmed_workout/?program_id=$programId3&day_number=10&name=Original Workout")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then update the workout
        webTestClient.patch()
            .uri("/api/v1/programmed_workout/${workoutResponse.id}?program_id=$programId3&day_number=15&name=Updated Workout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value { value: Any ->
                assert(value.toString() == workoutResponse.id.toString())
            }
            .jsonPath("$.day_number").isEqualTo(15)
            .jsonPath("$.name").isEqualTo("Updated Workout")
    }

    @Test
    fun `should delete programmed workout`() {
        val token = getValidToken("user")
        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/api/v1/programmed_workout/?program_id=$programId&day_number=20&name=Workout to Delete")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then delete the workout
        webTestClient.delete()
            .uri("/api/v1/programmed_workout/${workoutResponse.id}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(workoutResponse.id)
            .jsonPath("$.name").isEqualTo("Workout to Delete")
    }
}
