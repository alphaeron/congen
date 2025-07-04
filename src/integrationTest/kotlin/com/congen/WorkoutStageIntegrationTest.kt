package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.WorkoutStage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class WorkoutStageIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun setUp() {
        // Create a program first
        val program =
            Program(
                id = 1,
                name = "Test Program",
                description = "Test program for integration tests",
            )

        webTestClient.post()
            .uri("/program/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(program))
            .exchange()
            .expectStatus().isOk()

        // Create programmed workouts that the tests will reference
        val programmedWorkout1 =
            ProgrammedWorkout(
                id = 1,
                programId = 1,
                dayNumber = 1,
                name = "Test Workout 1",
            )

        val programmedWorkout2 =
            ProgrammedWorkout(
                id = 2,
                programId = 1,
                dayNumber = 2,
                name = "Test Workout 2",
            )

        val programmedWorkout3 =
            ProgrammedWorkout(
                id = 3,
                programId = 1,
                dayNumber = 3,
                name = "Test Workout 3",
            )

        val programmedWorkout4 =
            ProgrammedWorkout(
                id = 4,
                programId = 1,
                dayNumber = 4,
                name = "Test Workout 4",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedWorkout1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedWorkout2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedWorkout3))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedWorkout4))
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should return 422 when position is 0`() {
        val invalidStage =
            WorkoutStage(
                id = 1,
                programmedWorkoutId = 1,
                stageTypeId = 1,
                position = 0, // Invalid value
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidStage))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when position is negative`() {
        val invalidStage =
            WorkoutStage(
                id = 1,
                programmedWorkoutId = 1,
                stageTypeId = 1,
                position = -1, // Invalid value
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidStage))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: -1")
    }

    @Test
    fun `should accept valid workout stage data`() {
        val validStage =
            WorkoutStage(
                id = 1,
                programmedWorkoutId = 1,
                stageTypeId = 1,
                position = 1, // Valid value
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validStage))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programmedWorkoutId").isEqualTo(1)
            .jsonPath("$.stageTypeId").isEqualTo(1)
            .jsonPath("$.position").isEqualTo(1)
    }

    @Test
    fun `should get workout stage by id`() {
        // First create a workout stage
        val stage =
            WorkoutStage(
                id = 2,
                programmedWorkoutId = 1,
                stageTypeId = 2,
                position = 5,
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage))
            .exchange()
            .expectStatus().isOk()

        // Then get the stage by id
        webTestClient.get()
            .uri("/workout-stage/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(2)
            .jsonPath("$.programmedWorkoutId").isEqualTo(1)
            .jsonPath("$.stageTypeId").isEqualTo(2)
            .jsonPath("$.position").isEqualTo(5)
    }

    @Test
    fun `should get workout stages by programmed workout id`() {
        // First create multiple stages for the same workout
        val stage1 = WorkoutStage(id = 3, programmedWorkoutId = 2, stageTypeId = 1, position = 1)
        val stage2 = WorkoutStage(id = 4, programmedWorkoutId = 2, stageTypeId = 2, position = 2)
        val stage3 = WorkoutStage(id = 5, programmedWorkoutId = 2, stageTypeId = 3, position = 3)

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage3))
            .exchange()
            .expectStatus().isOk()

        // Then get all stages for the workout
        webTestClient.get()
            .uri("/workout-stage/workout/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programmedWorkoutId").isEqualTo(2)
            .jsonPath("$[1].programmedWorkoutId").isEqualTo(2)
            .jsonPath("$[2].programmedWorkoutId").isEqualTo(2)
    }

    @Test
    fun `should get all workout stages`() {
        webTestClient.get()
            .uri("/workout-stage/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update workout stage`() {
        // First create a workout stage
        val stage =
            WorkoutStage(
                id = 6,
                programmedWorkoutId = 3,
                stageTypeId = 1,
                position = 10,
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage))
            .exchange()
            .expectStatus().isOk()

        // Then update the stage
        val updatedStage =
            WorkoutStage(
                id = 6,
                programmedWorkoutId = 3,
                stageTypeId = 2,
                position = 15,
            )

        webTestClient.put()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(updatedStage))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(6)
            .jsonPath("$.stageTypeId").isEqualTo(2)
            .jsonPath("$.position").isEqualTo(15)
    }

    @Test
    fun `should delete workout stage`() {
        // First create a workout stage
        val stage =
            WorkoutStage(
                id = 7,
                programmedWorkoutId = 4,
                stageTypeId = 1,
                position = 20,
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(stage))
            .exchange()
            .expectStatus().isOk()

        // Then delete the stage
        webTestClient.delete()
            .uri("/workout-stage/7")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(7)
            .jsonPath("$.position").isEqualTo(20)
    }
}
