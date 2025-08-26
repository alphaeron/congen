package com.congen

import com.congen.model.ExerciseRotationHistory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExerciseRotationHistoryIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var testUserId: String = ""
    private var testToken: String = ""

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Exercises already exist in migrations
        // Ensure a user exists before each test and use the same token throughout
        testToken = getValidToken("user")
        testUserId = IntegrationTestHelpers.createTestUser(webTestClient, token = testToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, testToken)
    }

    @Test
    fun `should create exercise rotation history record`() {
        webTestClient.post()
            .uri("/api/v1/exercise_rotation_history/?user_id=$testUserId&exercise_name=Bench Press&is_accessory=false")
            .header("Authorization", "Bearer $testToken")
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
            .uri("/api/v1/exercise_rotation_history/?user_id=$testUserId&exercise_name=Bench Press&is_accessory=invalid")
            .header("Authorization", "Bearer $testToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should get exercise rotation history by id`() {
        // First create a record
        val response =
            webTestClient.post()
                .uri("/api/v1/exercise_rotation_history/?user_id=$testUserId&exercise_name=Safety Bar Squat&is_accessory=true")
                .header("Authorization", "Bearer $testToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .header("Authorization", "Bearer $testToken")
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
            isAccessory = false,
            token = testToken
        )
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Safety Bar Squat",
            "2024-01-01",
            isAccessory = true,
            token = testToken
        )

        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/is_accessory/true")
            .header("Authorization", "Bearer $testToken")
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
            isAccessory = false,
            token = testToken
        )
        IntegrationTestHelpers.createTestExerciseRotationHistory(
            webTestClient,
            testUserId,
            "Safety Bar Squat",
            "2024-01-01",
            isAccessory = true,
            token = testToken
        )
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/")
            .header("Authorization", "Bearer $testToken")
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
                .uri("/api/v1/exercise_rotation_history/?user_id=$testUserId&exercise_name=Bench Press&is_accessory=false")
                .header("Authorization", "Bearer $testToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.patch()
            .uri("/api/v1/exercise_rotation_history/${response.id}?user_id=$testUserId&exercise_name=Bench Press&is_accessory=true")
            .header("Authorization", "Bearer $testToken")
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
                .uri("/api/v1/exercise_rotation_history/?user_id=$testUserId&exercise_name=Bench Press&is_accessory=false")
                .header("Authorization", "Bearer $testToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.delete()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .header("Authorization", "Bearer $testToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)

        // Verify the record is deleted
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/${response.id}")
            .header("Authorization", "Bearer $testToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 404 when getting non-existent record`() {
        webTestClient.get()
            .uri("/api/v1/exercise_rotation_history/999999")
            .header("Authorization", "Bearer $testToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }
}
