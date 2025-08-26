package com.congen

import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorkoutStageTypeIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    @Test
    fun `should get workout stage type by id`() {
        val token = getValidToken("user")
        // First create a workout stage type
        val stageTypeName = IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)

        // Get the stage type by name to get its ID
        val stageTypeResponse =
            webTestClient.get()
                .uri("/api/v1/workout_stage_type/name/$stageTypeName")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStageType::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.get()
            .uri("/api/v1/workout_stage_type/${stageTypeResponse.id}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageTypeResponse.id)
            .jsonPath("$.name").isEqualTo(stageTypeName)
    }

    @Test
    fun `should return 404 when workout stage type not found by id`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/workout_stage_type/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get workout stage type by name`() {
        val token = getValidToken("user")
        // First create a workout stage type
        val stageTypeName = IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)

        webTestClient.get()
            .uri("/api/v1/workout_stage_type/name/$stageTypeName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(stageTypeName)
    }

    @Test
    fun `should return 404 when workout stage type not found by name`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/workout_stage_type/name/NonExistentStageType")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all workout stage types`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)
        IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.PRIMARY)
        webTestClient.get()
            .uri("/api/v1/workout_stage_type/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(WorkoutStageTypeEnum.values().size)
    }
}
