package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.WorkoutStage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetSchemeIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var programId: Long = 0
    private var programmedExerciseId: Long = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a program first
        val programResponse =
            webTestClient.post()
                .uri("/program/?name=Test Program&description=Test program for integration tests")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        programId = programResponse.id!!

        // Create a programmed workout
        val workoutResponse =
            webTestClient.post()
                .uri("/programmed-workout/?programId=$programId&dayNumber=1&name=Test Workout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!

        // Create a workout stage
        val stageResponse =
            webTestClient.post()
                .uri("/workout-stage/?programmedWorkoutId=${workoutResponse.id}&stageTypeId=1&position=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!

        // Create a programmed exercise
        val exerciseResponse =
            webTestClient.post()
                .uri("/programmed-exercise/?workoutStageId=${stageResponse.id}&exerciseName=Bench Press&notes=Test exercise")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedExercise::class.java)
                .returnResult()
                .responseBody!!

        // Store the generated IDs for use in tests
        programmedExerciseId = exerciseResponse.id!!
    }

    @Test
    fun `should return 422 when set number is 0`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=0&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Set number must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when set number is negative`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=-1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Set number must be greater than 0, got: -1")
    }

    @Test
    fun `should return 422 when target weight is 0`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target weight must be greater than 0, got: 0")
    }

    @Test
    fun `should return 422 when target rep count is 0`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=0&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Target rep count must be between 1 and 1000, got: 0")
    }

    @Test
    fun `should return 422 when rest seconds is negative`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=-1"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Rest seconds must be between 0 and 3600, got: -1")
    }

    @Test
    fun `should accept valid set scheme data`() {
        val uri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.programmedExerciseId").isEqualTo(programmedExerciseId)
            .jsonPath("$.setNumber").isEqualTo(1)
            .jsonPath("$.targetWeight").isEqualTo(100.0)
            .jsonPath("$.targetRepCount").isEqualTo(8)
            .jsonPath("$.restSeconds").isEqualTo(120)
    }

    @Test
    fun `should get set scheme by id`() {
        // First create a set scheme
        val createUri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=2&" +
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
            .uri("/set-scheme/${setSchemeResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.programmedExerciseId").isEqualTo(programmedExerciseId)
            .jsonPath("$.setNumber").isEqualTo(2)
            .jsonPath("$.wasSetPerformed").isEqualTo(true)
            .jsonPath("$.targetWeight").isEqualTo(150.0)
            .jsonPath("$.performedWeight").isEqualTo(145.0)
            .jsonPath("$.targetRepCount").isEqualTo(5)
            .jsonPath("$.performedRepCount").isEqualTo(4)
            .jsonPath("$.restSeconds").isEqualTo(180)
    }

    @Test
    fun `should get set schemes by programmed exercise id`() {
        // First create multiple set schemes for the same exercise
        val uri1 =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&targetRepCount=8&restSeconds=120"
        webTestClient.post()
            .uri(uri1)
            .exchange()
            .expectStatus().isOk()

        val uri2 =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=2&" +
                "wasSetPerformed=false&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=110.0&targetRepCount=6&restSeconds=180"
        webTestClient.post()
            .uri(uri2)
            .exchange()
            .expectStatus().isOk()

        val uri3 =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=3&" +
                "wasSetPerformed=false&isAmrap=true&isEmom=false&useTempo=false&" +
                "targetWeight=120.0&targetRepCount=10&restSeconds=240"
        webTestClient.post()
            .uri(uri3)
            .exchange()
            .expectStatus().isOk()

        // Then get all set schemes for the exercise
        webTestClient.get()
            .uri("/set-scheme/exercise/$programmedExerciseId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].programmedExerciseId").isEqualTo(programmedExerciseId)
            .jsonPath("$[1].programmedExerciseId").isEqualTo(programmedExerciseId)
            .jsonPath("$[2].programmedExerciseId").isEqualTo(programmedExerciseId)
            .jsonPath("$[2].isAmrap").isEqualTo(true)
    }

    @Test
    fun `should get all set schemes`() {
        webTestClient.get()
            .uri("/set-scheme/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }

    @Test
    fun `should update set scheme`() {
        // First create a set scheme
        val createUri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
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
            "/set-scheme/?id=${setSchemeResponse.id}&programmedExerciseId=$programmedExerciseId&" +
                "setNumber=1&wasSetPerformed=true&isAmrap=false&isEmom=false&useTempo=false&" +
                "targetWeight=100.0&performedWeight=95.0&targetRepCount=8&performedRepCount=7&restSeconds=120"
        webTestClient.patch()
            .uri(updateUri)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.wasSetPerformed").isEqualTo(true)
            .jsonPath("$.performedWeight").isEqualTo(95.0)
            .jsonPath("$.performedRepCount").isEqualTo(7)
    }

    @Test
    fun `should delete set scheme`() {
        // First create a set scheme
        val createUri =
            "/set-scheme/?programmedExerciseId=$programmedExerciseId&setNumber=1&" +
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
            .uri("/set-scheme/${setSchemeResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(setSchemeResponse.id)
            .jsonPath("$.setNumber").isEqualTo(1)
    }
}
