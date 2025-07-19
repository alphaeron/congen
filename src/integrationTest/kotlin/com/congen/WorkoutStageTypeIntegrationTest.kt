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
        // First create a workout stage type
        val stageTypeName = IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)

        // Get the stage type by name to get its ID
        val stageTypeResponse =
            webTestClient.get()
                .uri("/workout_stage_type/name/$stageTypeName")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStageType::class.java)
                .returnResult()
                .responseBody!!

        // Then get it by id
        webTestClient.get()
            .uri("/workout_stage_type/${stageTypeResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageTypeResponse.id)
            .jsonPath("$.name").isEqualTo(stageTypeName)
    }

    @Test
    fun `should return 404 when workout stage type not found by id`() {
        webTestClient.get()
            .uri("/workout_stage_type/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get workout stage type by name`() {
        // First create a workout stage type
        val stageTypeName = IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)

        // Then get it by name
        webTestClient.get()
            .uri("/workout_stage_type/name/$stageTypeName")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(stageTypeName)
    }

    @Test
    fun `should return 404 when workout stage type not found by name`() {
        webTestClient.get()
            .uri("/workout_stage_type/name/NonExistentStageType")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all workout stage types`() {
        IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.WARMUP)
        IntegrationTestHelpers.createTestWorkoutStageType(WorkoutStageTypeEnum.PRIMARY)
        webTestClient.get()
            .uri("/workout_stage_type/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(WorkoutStageTypeEnum.values().size)
    }
}
