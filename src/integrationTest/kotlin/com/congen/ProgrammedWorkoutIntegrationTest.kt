package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        userId = IntegrationTestHelpers.createTestUser(webTestClient)
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = programName, currentWeekNumber = 1)
    }

    @Test
    fun `should return 422 when day number is 0`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=0&name=Test Workout")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 0")
    }

    @Test
    fun `should return 422 when day number is 366`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=366&name=Test Workout")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 366")
    }

    @Test
    fun `should accept valid programmed workout data`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=1&name=Test Workout")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.program_id").isEqualTo(programId)
            .jsonPath("$.day_number").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Test Workout")
    }

    @Test
    fun `should get programmed workout by id`() {
        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=5&name=Integration Test Workout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then get the workout by id
        webTestClient.get()
            .uri("/programmed_workout/${workoutResponse.id}")
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
        // Create a second program for this test
        val userId2 = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId2 = IntegrationTestHelpers.createTestProgram(webTestClient, userId2, name = "Test Program 2", currentWeekNumber = 1)

        // First create multiple workouts for the same program
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 1, name = "Workout 1")
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 2, name = "Workout 2")
        IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId2, dayNumber = 3, name = "Workout 3")

        // Then get all workouts for the program
        webTestClient.get()
            .uri("/programmed_workout/program/$programId2")
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
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workout1 = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val workout2 = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 2, name = "Workout 2")
        webTestClient.get()
            .uri("/programmed_workout/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update programmed workout`() {
        // Create a third program for this test
        val userId3 = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId3 = IntegrationTestHelpers.createTestProgram(webTestClient, userId3, name = "Test Program 3", currentWeekNumber = 1)

        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId3&dayNumber=10&name=Original Workout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then update the workout
        webTestClient.patch()
            .uri("/programmed_workout/${workoutResponse.id}?programId=$programId3&dayNumber=15&name=Updated Workout")
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
        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=20&name=Workout to Delete")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Then delete the workout
        webTestClient.delete()
            .uri("/programmed_workout/${workoutResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(workoutResponse.id)
            .jsonPath("$.name").isEqualTo("Workout to Delete")
    }
}
