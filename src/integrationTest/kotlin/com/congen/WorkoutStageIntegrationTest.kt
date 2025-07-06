package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.User
import com.congen.model.WorkoutStage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorkoutStageIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Database cleanup happens in BaseIntegrationTest.setUp()
    }

    private fun createTestProgram(id: Long): Long {
        // First create a user
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test%20User%20$id&age=30&height=180.5&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        // Then create a program for that user
        val response =
            webTestClient.post()
                .uri("/program/?userId=${userResponse.id}&name=Test Program $id&description=Test program for integration tests $id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        return response.id!!
    }

    @Test
    fun `should return 422 when position is 0`() {
        val programId = createTestProgram(1)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=101&name=Test Workout for Position 0 Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=0")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when position is negative`() {
        val programId = createTestProgram(2)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=102&name=Test Workout for Negative Position Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=-1")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: -1")
    }

    @Test
    fun `should accept valid workout stage data`() {
        val programId = createTestProgram(1)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=101&name=Test Workout for Valid Stage Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$.stage_type_id").isEqualTo(1)
            .jsonPath("$.position").isEqualTo(1)
    }

    @Test
    fun `should get workout stage by id`() {
        val programId = createTestProgram(4)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=104&name=Test Workout for Get Stage Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageResponse =
            webTestClient.post()
                .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=2&position=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.get()
            .uri("/workout_stage/${stageResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageResponse.id)
            .jsonPath("$.programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$.stage_type_id").isEqualTo(2)
            .jsonPath("$.position").isEqualTo(5)
    }

    @Test
    fun `should get workout stages by programmed workout id`() {
        val programId = createTestProgram(5)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=105&name=Test Workout for Multiple Stages Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=1")
            .exchange()
            .expectStatus().isOk()
        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=2&position=2")
            .exchange()
            .expectStatus().isOk()
        webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=3&position=3")
            .exchange()
            .expectStatus().isOk()
        webTestClient.get()
            .uri("/workout_stage/workout/${workoutResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$[1].programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$[2].programmed_workout_id").isEqualTo(workoutResponse.id)
    }

    @Test
    fun `should get all workout stages`() {
        webTestClient.get()
            .uri("/workout_stage/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update workout stage`() {
        val programId = createTestProgram(6)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=106&name=Test Workout for Update Stage Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageResponse =
            webTestClient.post()
                .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.patch()
            .uri("/workout_stage/?id=${stageResponse.id}&programmedWorkoutId=${workoutResponse.id}&stageTypeId=2&position=15")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageResponse.id)
            .jsonPath("$.stage_type_id").isEqualTo(2)
            .jsonPath("$.position").isEqualTo(15)
    }

    @Test
    fun `should delete workout stage`() {
        val programId = createTestProgram(7)
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed_workout/?programId=$programId&dayNumber=107&name=Test Workout for Delete Stage Test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageResponse =
            webTestClient.post()
                .uri("/workout_stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.delete()
            .uri("/workout_stage/${stageResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageResponse.id)
            .jsonPath("$.position").isEqualTo(20)
    }
}
