package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
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
class ProgrammedWorkoutIntegrationTest {
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
    }

    @Test
    fun `should return 422 when day number is 0`() {
        val invalidWorkout =
            ProgrammedWorkout(
                id = 1,
                programId = 1,
                dayNumber = 0, // Invalid value
                name = "Test Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidWorkout))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 0")
    }

    @Test
    fun `should return 422 when day number is 366`() {
        val invalidWorkout =
            ProgrammedWorkout(
                id = 1,
                programId = 1,
                dayNumber = 366, // Invalid value
                name = "Test Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidWorkout))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Day number must be between 1 and 365, got: 366")
    }

    @Test
    fun `should accept valid programmed workout data`() {
        val validWorkout =
            ProgrammedWorkout(
                id = 1,
                programId = 1,
                dayNumber = 1, // Valid value
                name = "Test Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validWorkout))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programId").isEqualTo(1)
            .jsonPath("$.dayNumber").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Test Workout")
    }

    @Test
    fun `should get programmed workout by id`() {
        // First create a programmed workout
        val workout =
            ProgrammedWorkout(
                id = 2,
                programId = 1,
                dayNumber = 5,
                name = "Integration Test Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout))
            .exchange()
            .expectStatus().isOk()

        // Then get the workout by id
        webTestClient.get()
            .uri("/programmed-workout/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(2)
            .jsonPath("$.programId").isEqualTo(1)
            .jsonPath("$.dayNumber").isEqualTo(5)
            .jsonPath("$.name").isEqualTo("Integration Test Workout")
    }

    @Test
    fun `should get programmed workouts by program id`() {
        // First create multiple workouts for the same program
        val workout1 = ProgrammedWorkout(id = 3, programId = 2, dayNumber = 1, name = "Workout 1")
        val workout2 = ProgrammedWorkout(id = 4, programId = 2, dayNumber = 2, name = "Workout 2")
        val workout3 = ProgrammedWorkout(id = 5, programId = 2, dayNumber = 3, name = "Workout 3")

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout3))
            .exchange()
            .expectStatus().isOk()

        // Then get all workouts for the program
        webTestClient.get()
            .uri("/programmed-workout/program/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programId").isEqualTo(2)
            .jsonPath("$[1].programId").isEqualTo(2)
            .jsonPath("$[2].programId").isEqualTo(2)
    }

    @Test
    fun `should get all programmed workouts`() {
        webTestClient.get()
            .uri("/programmed-workout/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update programmed workout`() {
        // First create a programmed workout
        val workout =
            ProgrammedWorkout(
                id = 6,
                programId = 3,
                dayNumber = 10,
                name = "Original Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout))
            .exchange()
            .expectStatus().isOk()

        // Then update the workout
        val updatedWorkout =
            ProgrammedWorkout(
                id = 6,
                programId = 3,
                dayNumber = 15,
                name = "Updated Workout",
            )

        webTestClient.put()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(updatedWorkout))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(6)
            .jsonPath("$.dayNumber").isEqualTo(15)
            .jsonPath("$.name").isEqualTo("Updated Workout")
    }

    @Test
    fun `should delete programmed workout`() {
        // First create a programmed workout
        val workout =
            ProgrammedWorkout(
                id = 7,
                programId = 4,
                dayNumber = 20,
                name = "Workout to Delete",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workout))
            .exchange()
            .expectStatus().isOk()

        // Then delete the workout
        webTestClient.delete()
            .uri("/programmed-workout/7")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(7)
            .jsonPath("$.name").isEqualTo("Workout to Delete")
    }
}
