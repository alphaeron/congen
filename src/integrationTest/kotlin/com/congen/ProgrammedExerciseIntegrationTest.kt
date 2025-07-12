package com.congen

import org.junit.jupiter.api.Test

class ProgrammedExerciseIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create programmed exercise`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations
        val programmedExerciseId = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)
        webTestClient.post()
            .uri("/programmed_exercise/?workoutStageId=$stageId&exerciseName=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.workout_stage_id").isEqualTo(stageId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
            .jsonPath("$.position").isEqualTo(1)
    }

    @Test
    fun `should get programmed exercise by id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations
        val programmedExercise = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)
        webTestClient.get()
            .uri("/programmed_exercise/$programmedExercise")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value { value: Any ->
                assert(value.toString() == programmedExercise.toString())
            }
    }

    @Test
    fun `should return 404 when programmed exercise not found`() {
        webTestClient.get()
            .uri("/programmed_exercise/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all programmed exercises`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, "Deadlift")
        webTestClient.get()
            .uri("/programmed_exercise/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].workout_stage_id").exists()
    }

    @Test
    fun `should get programmed exercises by stage`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations

        // Create programmed exercises for the stage
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)
        IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId, "Deadlift")

        webTestClient.get()
            .uri("/programmed_exercise/stage/$stageId")
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
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations

        // First create a programmed exercise
        val exerciseResponse = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)

        // Then update it
        webTestClient.patch()
            .uri(
                "/programmed_exercise/$exerciseResponse?workoutStageId=$stageId&exerciseName=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=2"
            )
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
        webTestClient.patch()
            .uri("/programmed_exercise/999?workoutStageId=1&exerciseName=${IntegrationTestHelpers.TEST_EXERCISE_NAME}&position=1")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete programmed exercise`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId)
        val workoutId = IntegrationTestHelpers.createTestProgrammedWorkout(webTestClient, programId, dayNumber = 1, name = "Workout 1")
        val stageId = IntegrationTestHelpers.createTestWorkoutStage(webTestClient, workoutId, position = 1)
        // Exercises and equipment already exist in migrations

        // First create a programmed exercise
        val exerciseResponse = IntegrationTestHelpers.createTestProgrammedExercise(webTestClient, stageId)

        // Then delete it
        webTestClient.delete()
            .uri("/programmed_exercise/$exerciseResponse")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(exerciseResponse)
            .jsonPath("$.workout_stage_id").isEqualTo(stageId)
            .jsonPath("$.exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
    }

    @Test
    fun `should return 404 when deleting non-existent programmed exercise`() {
        webTestClient.delete()
            .uri("/programmed_exercise/999")
            .exchange()
            .expectStatus().isNotFound()
    }
}
