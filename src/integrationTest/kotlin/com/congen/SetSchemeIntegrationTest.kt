package com.congen

import com.congen.model.SetScheme
import com.congen.model.WorkoutStageType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SetSchemeIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var programId: Long = 0
    private var programmedExerciseId: Long = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        setupTestEntities()
    }

    private fun setupTestEntities() {
        val userId = IntegrationTestHelpers.createTestUserWithId(webTestClient, "SetScheme User" + System.nanoTime())
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId)
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = "Test Program" + System.nanoTime())
        // Create a programmed workout
        val workoutId =
            IntegrationTestHelpers.createTestProgrammedWorkout(
                webTestClient,
                programId,
                dayNumber = 1,
                name = "Test Workout" + System.nanoTime()
            )
        // Create a workout stage
        val stageTypeId = getWorkoutStageTypeId("Warmup")
        val stageId =
            IntegrationTestHelpers.createTestWorkoutStage(
                webTestClient,
                workoutId,
                stageTypeId = stageTypeId,
                position = 1,
                name = "Warmup Stage" + System.nanoTime()
            )
        // Create a programmed exercise first
        programmedExerciseId =
            IntegrationTestHelpers.createTestProgrammedExercise(
                webTestClient,
                stageId,
                exerciseName = "Bench Press"
            )
    }

    private fun getWorkoutStageTypeId(name: String): Int {
        val response =
            webTestClient.get()
                .uri("/api/v1/workout_stage_type/name/$name")
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=0&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").exists()
    }

    @Test
    fun `should return 422 when set number is negative`() {
        val uri =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=-1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Set number must be greater than 0, got: -1")
    }

    @Test
    fun `should return 422 when target weight is 0`() {
        val uri =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target weight must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when target rep count is 0`() {
        val uri =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=0&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target rep count must be between 1 and 1000, got: 0")
    }

    @Test
    fun `should return 422 when rest seconds is negative`() {
        val uri =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=-1"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Rest seconds must be between 0 and 3600, got: -1")
    }

    @Test
    fun `should accept valid set scheme data`() {
        val uri =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=2&" +
                "wasSetPerformed=true&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=150.0&performedWeight=145.0&targetRepCount=5&" +
                "performedRepCount=4&restSeconds=180"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        // Then get the set scheme by id
        webTestClient.get()
            .uri("/api/v1/set_scheme/${setSchemeResponse.id}")
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri1)
            .exchange()
            .expectStatus().isOk()

        val uri2 =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=2&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=110.0&targetRepCount=6&restSeconds=180"
        webTestClient.post()
            .uri(uri2)
            .exchange()
            .expectStatus().isOk()

        val uri3 =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=3&" +
                "wasSetPerformed=false&isAmrap=true&isEmom=false&useTempo=false&" +
                "targetWeight=120.0&targetRepCount=10&restSeconds=240"
        webTestClient.post()
            .uri(uri3)
            .exchange()
            .expectStatus().isOk()

        // Then get all set schemes for the exercise
        webTestClient.get()
            .uri("/api/v1/set_scheme/exercise/$programmedExerciseId")
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        val uri2 =
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=2&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=110.0&targetRepCount=6&restSeconds=180"
        webTestClient.post().uri(uri1).exchange().expectStatus().isOk()
        webTestClient.post().uri(uri2).exchange().expectStatus().isOk()
        webTestClient.get()
            .uri("/api/v1/set_scheme/")
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        // Then update the set scheme
        val updateUri =
            "/api/v1/set_scheme/${setSchemeResponse.id}?programmedExerciseId=$programmedExerciseId&" +
                "setNumber=1&wasSetPerformed=true&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&performedWeight=95.0&targetRepCount=8&performedRepCount=7&restSeconds=120"
        webTestClient.patch()
            .uri(updateUri)
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
            "/api/v1/set_scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        val setSchemeResponse =
            webTestClient.post()
                .uri(createUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!

        // Then delete the set scheme
        webTestClient.delete()
            .uri("/api/v1/set_scheme/${setSchemeResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.set_number").isEqualTo(1)
    }
}
