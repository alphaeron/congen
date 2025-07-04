package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
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
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class SetSchemeIntegrationTest {
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

        // Create a programmed workout
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1,
                programId = 1,
                dayNumber = 1,
                name = "Test Workout",
            )

        webTestClient.post()
            .uri("/programmed-workout/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedWorkout))
            .exchange()
            .expectStatus().isOk()

        // Create a workout stage
        val workoutStage =
            WorkoutStage(
                id = 1,
                programmedWorkoutId = 1,
                stageTypeId = 1,
                position = 1,
            )

        webTestClient.post()
            .uri("/workout-stage/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(workoutStage))
            .exchange()
            .expectStatus().isOk()

        // Create a programmed exercise
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Bench Press",
                notes = "Test exercise",
            )

        webTestClient.post()
            .uri("/programmed-exercise/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(programmedExercise))
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should return 422 when set number is 0`() {
        val invalidSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 0, // Invalid value
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidSetScheme))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Set number must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when set number is negative`() {
        val invalidSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = -1, // Invalid value
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidSetScheme))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Set number must be greater than 0, got: -1")
    }

    @Test
    fun `should return 422 when target weight is 0`() {
        val invalidSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal.ZERO, // Invalid value
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidSetScheme))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target weight must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when target rep count is 0`() {
        val invalidSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 0, // Invalid value
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidSetScheme))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target rep count must be between 1 and 1000, got: 0")
    }

    @Test
    fun `should return 422 when rest seconds is negative`() {
        val invalidSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = -1, // Invalid value
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidSetScheme))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Rest seconds must be between 0 and 3600, got: -1")
    }

    @Test
    fun `should accept valid set scheme data`() {
        val validSetScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1, // Valid value
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"), // Valid value
                performedWeight = null,
                targetRepCount = 8, // Valid value
                performedRepCount = null,
                restSeconds = 120, // Valid value
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validSetScheme))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programmedExerciseId").isEqualTo(1)
            .jsonPath("$.setNumber").isEqualTo(1)
            .jsonPath("$.targetWeight").isEqualTo(100.0)
            .jsonPath("$.targetRepCount").isEqualTo(8)
            .jsonPath("$.restSeconds").isEqualTo(120)
    }

    @Test
    fun `should get set scheme by id`() {
        // First create a set scheme
        val setScheme =
            SetScheme(
                id = 2,
                programmedExerciseId = 1,
                setNumber = 2,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("135.0"),
                performedWeight = BigDecimal("135.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme))
            .exchange()
            .expectStatus().isOk()

        // Then get the set scheme by id
        webTestClient.get()
            .uri("/set-scheme/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(2)
            .jsonPath("$.programmedExerciseId").isEqualTo(1)
            .jsonPath("$.setNumber").isEqualTo(2)
            .jsonPath("$.wasSetPerformed").isEqualTo(true)
            .jsonPath("$.useTempo").isEqualTo(true)
            .jsonPath("$.eccentricTempo").isEqualTo("3")
            .jsonPath("$.targetWeight").isEqualTo(135.0)
            .jsonPath("$.performedWeight").isEqualTo(135.0)
    }

    @Test
    fun `should get set schemes by programmed exercise id`() {
        // First create multiple set schemes for the same exercise
        val setScheme1 =
            SetScheme(
                id = 3,
                programmedExerciseId = 2,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )
        val setScheme2 =
            SetScheme(
                id = 4,
                programmedExerciseId = 2,
                setNumber = 2,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )
        val setScheme3 =
            SetScheme(
                id = 5,
                programmedExerciseId = 2,
                setNumber = 3,
                wasSetPerformed = false,
                isAmrap = true,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme3))
            .exchange()
            .expectStatus().isOk()

        // Then get all set schemes for the exercise
        webTestClient.get()
            .uri("/set-scheme/exercise/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programmedExerciseId").isEqualTo(2)
            .jsonPath("$[1].programmedExerciseId").isEqualTo(2)
            .jsonPath("$[2].programmedExerciseId").isEqualTo(2)
            .jsonPath("$[2].isAmrap").isEqualTo(true)
    }

    @Test
    fun `should get all set schemes`() {
        webTestClient.get()
            .uri("/set-scheme/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update set scheme`() {
        // First create a set scheme
        val setScheme =
            SetScheme(
                id = 6,
                programmedExerciseId = 3,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme))
            .exchange()
            .expectStatus().isOk()

        // Then update the set scheme
        val updatedSetScheme =
            SetScheme(
                id = 6,
                programmedExerciseId = 3,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("95.0"),
                targetRepCount = 8,
                performedRepCount = 6,
                restSeconds = 120,
            )

        webTestClient.put()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(updatedSetScheme))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(6)
            .jsonPath("$.wasSetPerformed").isEqualTo(true)
            .jsonPath("$.performedWeight").isEqualTo(95.0)
            .jsonPath("$.performedRepCount").isEqualTo(6)
    }

    @Test
    fun `should delete set scheme`() {
        // First create a set scheme
        val setScheme =
            SetScheme(
                id = 7,
                programmedExerciseId = 4,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 8,
                performedRepCount = null,
                restSeconds = 120,
            )

        webTestClient.post()
            .uri("/set-scheme/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(setScheme))
            .exchange()
            .expectStatus().isOk()

        // Then delete the set scheme
        webTestClient.delete()
            .uri("/set-scheme/7")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(7)
            .jsonPath("$.setNumber").isEqualTo(1)
    }
}
