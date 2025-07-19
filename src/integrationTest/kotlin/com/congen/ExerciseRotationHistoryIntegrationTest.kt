package com.congen

import com.congen.model.ExerciseRotationHistory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExerciseRotationHistoryIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var testUserId: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Exercises already exist in migrations
        // Ensure a user exists before each test
        testUserId = IntegrationTestHelpers.createTestUser(webTestClient)
    }

    @Test
    fun `should create exercise rotation history record`() {
        webTestClient.post()
            .uri("/api/v1/exercise_rotation_history/?userId=$testUserId&exerciseName=Bench Press&isAccessory=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.user_id").isEqualTo(testUserId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
            .jsonPath("$.is_accessory").isEqualTo(false)
            .jsonPath("$.created_at").isNotEmpty()
    }

    @Test
    fun `should return 400 when isAccessory is invalid`() {
        webTestClient.post()
            .uri("/api/v1/exercise_rotation_history/?userId=$testUserId&exerciseName=Bench Press&isAccessory=invalid")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should get exercise rotation history by id`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/api/v1/exercise_rotation_history/?userId=$testUserId&exerciseName=Safety Bar Squat&isAccessory=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then get the record by id
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.user_id").isEqualTo(testUserId)
            .jsonPath("$.exercise_name").isEqualTo("Safety Bar Squat")
            .jsonPath("$.is_accessory").isEqualTo(true)
    }

    @Test
    fun `should get exercise rotation history by isAccessory`() {
        // First create some records for different accessory types
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Bench Press",
            "2024-01-01",
            isAccessory = false
        )
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Safety Bar Squat",
            "2024-01-01",
            isAccessory = true
        )

        // Then get records for accessory exercises
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/isAccessory/true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].is_accessory").isEqualTo(true)
    }

    @Test
    fun `should get all exercise rotation history`() {
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Bench Press",
            "2024-01-01",
            isAccessory = false
        )
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Safety Bar Squat",
            "2024-01-01",
            isAccessory = true
        )
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update exercise rotation history`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/api/v1/exercise_rotation_history/?userId=$testUserId&exerciseName=Bench Press&isAccessory=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then update the record
        webTestClient.patch()
            .uri("/api/v1/exercise_rotation_history/${response.id}?userId=$testUserId&exerciseName=Bench Press&isAccessory=true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.is_accessory").isEqualTo(true)
    }

    @Test
    fun `should delete exercise rotation history by id`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/api/v1/exercise_rotation_history/?userId=$testUserId&exerciseName=Bench Press&isAccessory=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        // Then delete the record
        webTestClient.delete()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)

        // Verify the record is deleted
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 404 when getting non-existent record`() {
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/999999")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }
}
