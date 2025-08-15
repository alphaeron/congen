package com.congen

import org.junit.jupiter.api.Test

class ProgrammedExerciseIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create programmed exercise`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        val exerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press",
                token = token
            )
        assert(exerciseId > 0)
    }

    @Test
    fun `should get programmed exercise by id`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        val exerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press",
                token = token
            )
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/$exerciseId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseId)
            .jsonPath("$.exercise_name").isEqualTo("Bench Press")
    }

    @Test
    fun `should return 404 when programmed exercise not found`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(404)
    }

    @Test
    fun `should get all programmed exercises`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        IntegrationTestHelpers.createTestProgrammedExercise(
            webTestClient,
            stageId,
            exerciseName = "Bench Press",
            token = token
        )
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should get programmed exercises by stage`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        IntegrationTestHelpers.createTestProgrammedExercise(
            webTestClient,
            stageId,
            exerciseName = "Bench Press",
            token = token
        )
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/stage/$stageId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].workout_stage_id").isEqualTo(stageId)
    }

    @Test
    fun `should update programmed exercise`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        val exerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press",
                token = token
            )
        webTestClient.patch()
            .uri("/api/v1/programmed_exercise/$exerciseId?workout_stage_id=$stageId&exercise_name=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=2")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.position").isEqualTo(2)
    }

    @Test
    fun `should return 404 when updating non-existent programmed exercise`() {
        val token = getValidToken("user")
        webTestClient.patch()
            .uri("/api/v1/programmed_exercise/999?workout_stage_id=1&exercise_name=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(404)
    }

    @Test
    fun `should delete programmed exercise`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, token = token)
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Workout 1",
                token = token
            )
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = 1,
                position = 1,
                name = "Stage 1",
                token = token
            )
        val exerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press",
                token = token
            )
        webTestClient.delete()
            .uri("/api/v1/programmed_exercise/$exerciseId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseId)
    }

    @Test
    fun `should return 404 when deleting non-existent programmed exercise`() {
        val token = getValidToken("user")
        webTestClient.delete()
            .uri("/api/v1/programmed_exercise/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(404)
    }
}
