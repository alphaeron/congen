package com.congen

import org.junit.jupiter.api.Test

class ProgrammedExerciseIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create programmed exercise`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations
        val programmedExerciseId = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)

        // Verify the created programmed exercise
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/$programmedExerciseId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.workout_stage_id").isEqualTo(stageId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.position").isEqualTo(1)
    }

    @Test
    fun `should get programmed exercise by id`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations
        val programmedExercise = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/$programmedExercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value { value: Any ->
                assert(value.toString() == programmedExercise.toString())
            }
    }

    @Test
    fun `should return 404 when programmed exercise not found`() {
        val token = getValidToken("service")
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all programmed exercises`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, "Deadlift", token = token)
        webTestClient.get()
            .uri("/api/v1/programmed_exercise/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].workout_stage_id").exists()
    }

    @Test
    fun `should get programmed exercises by stage`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations

        // Create programmed exercises for the stage
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, "Deadlift", token = token)

        webTestClient.get()
            .uri("/api/v1/programmed_exercise/stage/$stageId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].workout_stage_id").isEqualTo(stageId)
            .jsonPath("$[1].workout_stage_id").isEqualTo(stageId)
    }

    @Test
    fun `should update programmed exercise`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations

        // First create a programmed exercise
        val exerciseResponse = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)

        // Then update it
        webTestClient.patch()
            .uri(
                "/api/v1/programmed_exercise/$exerciseResponse?workout_stage_id=$stageId" +
                    "&exercise_name=${IntegrationTestHelpers.TEST_EXERCISE_NAME}" +
                    "&position=2"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseResponse)
            .jsonPath("$.workout_stage_id").isEqualTo(stageId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.position").isEqualTo(2)
    }

    @Test
    fun `should return 404 when updating non-existent programmed exercise`() {
        val token = getValidToken("service")
        webTestClient.patch()
            .uri("/api/v1/programmed_exercise/999?workout_stage_id=1&exercise_name=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete programmed exercise`() {
        val token = getValidToken("service")
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
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1, token = token)
        // Exercises and equipment already exist in migrations

        // First create a programmed exercise
        val exerciseResponse = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, token = token)

        // Then delete it
        webTestClient.delete()
            .uri("/api/v1/programmed_exercise/$exerciseResponse")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseResponse)
            .jsonPath("$.workout_stage_id").isEqualTo(stageId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
    }

    @Test
    fun `should return 404 when deleting non-existent programmed exercise`() {
        val token = getValidToken("service")
        webTestClient.delete()
            .uri("/api/v1/programmed_exercise/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }
}
