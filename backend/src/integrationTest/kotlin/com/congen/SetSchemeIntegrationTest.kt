package com.congen

import com.congen.model.SetScheme
import com.congen.model.WorkoutStageType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SetSchemeIntegrationTest : BaseIntegrationTest() {
    private var programId: Long = 0
    private var programmedExerciseId: Long = 0
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        setupTestEntities()
    }

    private fun setupTestEntities() {
        userToken = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)
        programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program" + System.nanoTime(),
                token = userToken
            )
        // Create a programmed workout
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Test Workout" + System.nanoTime(),
                token = userToken
            )
        // Create a workout stage
        val stageTypeId = getWorkoutStageTypeId("Warmup", userToken)
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = stageTypeId,
                position = 1,
                name = "Warmup Stage" + System.nanoTime(),
                token = userToken
            )
        // Create a programmed exercise first
        programmedExerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press",
                token = userToken
            )
    }

    private fun getWorkoutStageTypeId(
        name: String,
        token: String
    ): Int {
        val response =
            webTestClient.get()
                .uri("/api/v1/workout_stage_type/name/$name")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStageType::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    @Test
    fun `should return 422 when set number is 0`() {
        val uri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=0&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").exists()
    }

    @Test
    fun `should return 422 when set number is negative`() {
        val uri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=-1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").exists()
    }

    @Test
    fun `should return 422 when target rep count is 0`() {
        val uri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=0&rest_seconds=120"
        webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").exists()
    }

    @Test
    fun `should return 422 when rest seconds is negative`() {
        val uri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=-1"
        webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").exists()
    }

    @Test
    fun `should accept valid set scheme data`() {
        val uri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.programmed_exercise_id").isEqualTo(programmedExerciseId)
            .jsonPath("$.set_number").isEqualTo(1)
            .jsonPath("$.target_weight").isEqualTo(100.0)
            .jsonPath("$.target_rep_count").isEqualTo(8)
            .jsonPath("$.rest_seconds").isEqualTo(120)
    }

    @Test
    fun `should get set scheme by id`() {
        // First create a set scheme
        val createUri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=2&" +
                "was_set_performed=true&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=150.0&performed_weight=145.0&target_rep_count=5&" +
                "performed_rep_count=4&rest_seconds=180"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.get()
            .uri("/api/v1/set_scheme/${setSchemeResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.programmed_exercise_id").isEqualTo(programmedExerciseId)
            .jsonPath("$.set_number").isEqualTo(2)
            .jsonPath("$.target_weight").isEqualTo(150.0)
            .jsonPath("$.performed_weight").isEqualTo(145.0)
            .jsonPath("$.target_rep_count").isEqualTo(5)
            .jsonPath("$.performed_rep_count").isEqualTo(4)
            .jsonPath("$.rest_seconds").isEqualTo(180)
    }

    @Test
    fun `should get set schemes by programmed exercise id`() {
        // First create multiple set schemes for the same exercise
        val uri1 =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        webTestClient.post()
            .uri(uri1)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        val uri2 =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=2&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=110.0&target_rep_count=6&rest_seconds=180"
        webTestClient.post()
            .uri(uri2)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        val uri3 =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=3&" +
                "was_set_performed=false&is_amrap=true&is_emom=false&use_tempo=false&" +
                "target_weight=120.0&target_rep_count=10&rest_seconds=240"
        webTestClient.post()
            .uri(uri3)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/set_scheme/exercise/$programmedExerciseId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programmed_exercise_id").isEqualTo(programmedExerciseId)
            .jsonPath("$[1].programmed_exercise_id").isEqualTo(programmedExerciseId)
            .jsonPath("$[2].programmed_exercise_id").isEqualTo(programmedExerciseId)
            .jsonPath("$[2].is_amrap").isEqualTo(true)
    }

    @Test
    fun `should get all set schemes`() {
        val uri1 =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        val uri2 =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=2&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=110.0&target_rep_count=6&rest_seconds=180"
        webTestClient.post().uri(uri1).header("Authorization", "Bearer $userToken").exchange().expectStatus().isOk()
        webTestClient.post().uri(uri2).header("Authorization", "Bearer $userToken").exchange().expectStatus().isOk()
        webTestClient.get()
            .uri("/api/v1/set_scheme/")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update set scheme`() {
        // First create a set scheme
        val createUri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        val updateUri =
            "/api/v1/set_scheme/${setSchemeResponse.id}?programmed_exercise_id=$programmedExerciseId&" +
                "set_number=1&was_set_performed=true&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&performed_weight=95.0&target_rep_count=8&performed_rep_count=7&rest_seconds=120"
        webTestClient.patch()
            .uri(updateUri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.performed_weight").isEqualTo(95.0)
            .jsonPath("$.performed_rep_count").isEqualTo(7)
    }

    @Test
    fun `should delete set scheme`() {
        // First create a set scheme
        val createUri =
            "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId&set_number=1&" +
                "was_set_performed=false&is_amrap=false&is_emom=false&use_tempo=false&" +
                "target_weight=100.0&target_rep_count=8&rest_seconds=120"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        webTestClient.delete()
            .uri("/api/v1/set_scheme/${setSchemeResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.set_number").isEqualTo(1)
    }
}
