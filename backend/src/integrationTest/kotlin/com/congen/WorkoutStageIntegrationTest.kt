package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class WorkoutStageIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    private fun createTestProgram(id: Long): Pair<Long, String> {
        val userToken = getValidToken("user")
        // First create a user with user token
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        // Then create a program for that user with user token
        val unique = System.nanoTime()
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/program/" +
                        "?user_id=$userId" +
                        "&name=Test Program $unique" +
                        "&current_week_number=1"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        return Pair(response.id, userToken)
    }

    private fun getWorkoutStageTypeId(
        name: String,
        token: String
    ): Long {
        val response =
            webTestClient.get()
                .uri("/api/v1/workout_stage_type/name/$name")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStageType::class.java)
                .returnResult()
                .responseBody!!
        return response.id.toLong()
    }

    @Test
    fun `should return 422 when position is 0`() {
        val (programId, userToken) = createTestProgram(1)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=101" +
                        "&name=Test Workout for Position 0 Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId" +
                    "&position=0" +
                    "&name=Test Stage"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when position is negative`() {
        val (programId, userToken) = createTestProgram(2)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=102" +
                        "&name=Test Workout for Negative Position Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId" +
                    "&position=-1" +
                    "&name=Test Stage"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Position must be greater than 0, got: -1")
    }

    @Test
    fun `should accept valid workout stage data`() {
        val (programId, userToken) = createTestProgram(1)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=101" +
                        "&name=Test Workout for Valid Stage Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId" +
                    "&position=1" +
                    "&name=Test Stage"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$.stage_type_id").isEqualTo(stageTypeId)
            .jsonPath("$.position").isEqualTo(1)
    }

    @Test
    fun `should get workout stage by id`() {
        val (programId, userToken) = createTestProgram(4)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=104" +
                        "&name=Test Workout for Get Stage Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Primary", userToken)
        val stageResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/workout_stage/" +
                        "?programmed_workout_id=${workoutResponse.id}" +
                        "&stage_type_id=$stageTypeId" +
                        "&position=5" +
                        "&name=Test Stage"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.get()
            .uri("/api/v1/workout_stage/${stageResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(stageResponse.id)
            .jsonPath("$.programmed_workout_id").isEqualTo(workoutResponse.id)
            .jsonPath("$.stage_type_id").isEqualTo(stageTypeId)
            .jsonPath("$.position").isEqualTo(5)
    }

    @Test
    fun `should get workout stages by programmed workout id`() {
        val (programId, userToken) = createTestProgram(5)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=105" +
                        "&name=Test Workout for Get Stages Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId1 = getWorkoutStageTypeId("Warmup", userToken)
        val stageTypeId2 = getWorkoutStageTypeId("Primary", userToken)

        // Create first stage
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId1" +
                    "&position=1" +
                    "&name=Warmup Stage"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Create second stage
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId2" +
                    "&position=2" +
                    "&name=Primary Stage"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Get all stages for the workout
        webTestClient.get()
            .uri("/api/v1/workout_stage/workout/${workoutResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update workout stage`() {
        val (programId, userToken) = createTestProgram(6)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=106" +
                        "&name=Test Workout for Update Stage Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        val stageResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/workout_stage/" +
                        "?programmed_workout_id=${workoutResponse.id}" +
                        "&stage_type_id=$stageTypeId" +
                        "&position=1" +
                        "&name=Original Stage Name"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        // Update the stage
        webTestClient.patch()
            .uri(
                "/api/v1/workout_stage/" +
                    "?id=${stageResponse.id}" +
                    "&programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId" +
                    "&position=1" +
                    "&name=Updated Stage Name"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated Stage Name")
    }

    @Test
    fun `should delete workout stage`() {
        val (programId, userToken) = createTestProgram(7)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=107" +
                        "&name=Test Workout for Delete Stage Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        val stageResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/workout_stage/" +
                        "?programmed_workout_id=${workoutResponse.id}" +
                        "&stage_type_id=$stageTypeId" +
                        "&position=1" +
                        "&name=Stage to Delete"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        // Delete the stage
        webTestClient.delete()
            .uri("/api/v1/workout_stage/${stageResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Verify it's deleted
        webTestClient.get()
            .uri("/api/v1/workout_stage/${stageResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 for non-existent workout stage`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/workout_stage/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should handle multiple stages with different positions`() {
        val (programId, userToken) = createTestProgram(8)
        val workoutResponse =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/" +
                        "?program_id=$programId" +
                        "&day_number=108" +
                        "&name=Test Workout for Multiple Stages Test"
                )
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        val stageTypeId1 = getWorkoutStageTypeId("Warmup", userToken)
        val stageTypeId2 = getWorkoutStageTypeId("Primary", userToken)
        val stageTypeId3 = getWorkoutStageTypeId("Accessory", userToken)

        // Create stages with different positions
        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId1" +
                    "&position=1" +
                    "&name=Warmup"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId2" +
                    "&position=2" +
                    "&name=Primary"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri(
                "/api/v1/workout_stage/" +
                    "?programmed_workout_id=${workoutResponse.id}" +
                    "&stage_type_id=$stageTypeId3" +
                    "&position=3" +
                    "&name=Accessory"
            )
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        // Get all stages and verify they're in the correct order
        val stages =
            webTestClient.get()
                .uri("/api/v1/workout_stage/workout/${workoutResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        assert(stages.size == 3)
        assert(stages[0].position == 1)
        assert(stages[1].position == 2)
        assert(stages[2].position == 3)
    }
}
