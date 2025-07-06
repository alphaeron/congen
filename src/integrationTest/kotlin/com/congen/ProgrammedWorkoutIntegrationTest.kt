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
    private val programName = "Test Program"
    private val programDescription = "Test program for integration tests"

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val response =
            webTestClient.post()
                .uri("/program?name=$programName&description=$programDescription")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        programId = response.id
    }

    @Test
    fun `should return 422 when day number is 0`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=0&name=Test%20Workout")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 0")
    }

    @Test
    fun `should return 422 when day number is 366`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=366&name=Test%20Workout")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 366")
    }

    @Test
    fun `should accept valid programmed workout data`() {
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=1&name=Test%20Workout")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programId").isEqualTo(programId)
            .jsonPath("$.dayNumber").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Test Workout")
    }

    @Test
    fun `should get programmed workout by id`() {
        // First create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=5&name=Integration%20Test%20Workout")
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
            .jsonPath("$.programId").isEqualTo(programId)
            .jsonPath("$.dayNumber").isEqualTo(5)
            .jsonPath("$.name").isEqualTo("Integration Test Workout")
    }

    @Test
    fun `should get programmed workouts by program id`() {
        // Create a second program for this test
        val programName2 = "Test Program 2"
        val programDescription2 = "Second test program for integration tests"
        val response2 =
            webTestClient.post()
                .uri("/program?name=$programName2&description=$programDescription2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        val programId2 = response2.id

        // First create multiple workouts for the same program
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId2&dayNumber=1&name=Workout%201")
            .exchange()
            .expectStatus().isOk()
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId2&dayNumber=2&name=Workout%202")
            .exchange()
            .expectStatus().isOk()
        webTestClient.post()
            .uri("/programmed_workout/?programId=$programId2&dayNumber=3&name=Workout%203")
            .exchange()
            .expectStatus().isOk()

        // Then get all workouts for the program
        webTestClient.get()
            .uri("/programmed_workout/program/$programId2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programId").isEqualTo(programId2)
            .jsonPath("$[1].programId").isEqualTo(programId2)
            .jsonPath("$[2].programId").isEqualTo(programId2)
    }

    @Test
    fun `should get all programmed workouts`() {
        webTestClient.get()
            .uri("/programmed_workout/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update programmed workout`() {
        // Create a third program for this test
        val response3 =
            webTestClient.post()
                .uri("/program?name=Test Program 3&description=Third test program for integration tests")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        val programId3 = response3.id!!

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
        webTestClient.put()
            .uri("/programmed_workout/?id=${workoutResponse.id}&programId=$programId3&dayNumber=15&name=Updated Workout")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(workoutResponse.id)
            .jsonPath("$.dayNumber").isEqualTo(15)
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

    private fun createTestProgram(
        programName: String,
        programDescription: String
    ): Long {
        // First create a user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Then create a program for that user
        val response =
            webTestClient.post()
                .uri("/program?userId=${userResponse.id}&name=$programName&description=$programDescription")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        return response.id!!
    }
}
