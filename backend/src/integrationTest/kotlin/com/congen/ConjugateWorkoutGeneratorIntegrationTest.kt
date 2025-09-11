package com.congen

import com.congen.model.Program
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ConjugateWorkoutGeneratorIntegrationTest : BaseIntegrationTest() {
    private var userId: String = ""
    private var programId: Long = 0
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create test user and program once for all tests
        val unique = System.nanoTime()
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
        // Create program
        programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = "Test Program $unique", token = userToken)

        // Program preferences are created automatically with the program, no cleanup needed
    }

    @Test
    fun `should generate 3-day conjugate workout program successfully`() {
        // Create a new program with 3 days per week
        val programId3Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 3-Day",
                numDaysPerWeek = 3,
                token = userToken
            )

        // Create reference data for 3-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId3Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId3Day)
        assert(programResponse.name == "Test Program 3-Day")
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }

    @Test
    fun `should generate 2-day conjugate workout program successfully`() {
        // Create a new program with 2 days per week
        val programId2Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 2-Day",
                numDaysPerWeek = 2,
                token = userToken
            )

        // Create reference data for 2-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId2Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId2Day)
        assert(programResponse.name == "Test Program 2-Day")
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should generate 4-day conjugate workout program successfully`() {
        // Create a new program with 4 days per week
        val programId4Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 4-Day",
                numDaysPerWeek = 4,
                token = userToken
            )

        // Create reference data for 4-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId4Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId4Day)
        assert(programResponse.name.contains("Test Program 4-Day"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(4)
    }

    @Test
    fun `should handle invalid session time in database`() {
        // Try to update program preferences with invalid session time should fail
        webTestClient.patch()
            .uri("/api/v1/program_preferences/?program_id=$programId&session_time_length_in_minutes=0")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo("Session time length must be between 15 and 300 minutes, got: 0")
    }

    @Test
    fun `should handle non-existent program`() {
        webTestClient.post()
            .uri("/api/v1/conjugate_workout_generator/999999")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should generate workout with user exercise preferences`() {
        // Create a new program with default 4 days per week
        val programIdExercise =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program Exercise",
                token = userToken
            )

        // Create minimal reference data (equipment) - program preferences are created automatically with the program
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bench", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)

        // Exercises already exist in migrations

        // Add exercise preferences (different exercises to avoid duplicate key constraint)
        webTestClient.post()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId&exercise_name=Safety Bar Squat&should_avoid=true")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId&exercise_name=Deadlift&should_avoid=false")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programIdExercise")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        assert(programResponse.userId == userId)
        assert(programResponse.id == programIdExercise)
    }

    @Test
    fun `should generate workout with user equipment`() {
        // Create a new program with default 4 days per week
        val programIdEquipment =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program Equipment",
                token = userToken
            )

        // Create minimal reference data (equipment) - program preferences are created automatically with the program
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bench", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)

        // Equipment already exists in migrations

        // Add user equipment (only add 'dumbbells' since 'power bar' is already added by reference data)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programIdEquipment")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        assert(programResponse.userId == userId)
        assert(programResponse.id == programIdEquipment)
    }

    @Test
    fun `should generate workout with user one rep max data`() {
        // Create a new program with default 4 days per week
        val programIdOneRepMax =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program One Rep Max",
                token = userToken
            )

        // Create minimal reference data (equipment) - program preferences are created automatically with the program
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bench", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)

        // Exercises already exist in migrations

        // Add one rep max data
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press", token = userToken)
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programIdOneRepMax")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        assert(programResponse.userId == userId)
        assert(programResponse.id == programIdOneRepMax)
    }

    @Test
    fun `should generate workout with program preferences`() {
        // Create a new program with default 4 days per week
        val programIdPreferences =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program Preferences",
                token = userToken
            )

        // Program preferences are created automatically when the program is created

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programIdPreferences")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        assert(programResponse.userId == userId)
        assert(programResponse.id == programIdPreferences)
    }

    @Test
    fun `should generate DE set scheme with correct band and bar weights`() {
        // Create a new program with 3 days per week
        val programIdBanded =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program Banded",
                numDaysPerWeek = 3,
                token = userToken
            )

        // Set up user with 1RM for banded exercises specifically
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Banded Bench Press", oneRepMax = 200.0, token = userToken)
        IntegrationTestHelpers.createTestUserOneRepMax(
            webTestClient,
            userId,
            "Banded Safety Bar Squat",
            oneRepMax = 350.0,
            token = userToken
        )
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add equipment needed for banded exercises (power bar is already added by createAllReferenceDataForUser)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bands", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "safety squat bar", token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        // Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programIdBanded")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Fetch programmed workouts for the program and get DE workout ID
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!
        val deWorkout =
            workoutsResponse.find { workout ->
                workout["name"]?.toString()?.contains("DE") == true
            } ?: throw AssertionError("No DE workout found")
        val deWorkoutId = (deWorkout["id"] as Number).toLong()

        // Fetch workout stages for the workout and find Primary stage (which contains the DE exercise)
        val stagesResponse =
            webTestClient.get()
                .uri("/api/v1/workout_stage/workout/$deWorkoutId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!
        val primaryStage =
            stagesResponse.find { stage ->
                stage.name.toString() == "Primary"
            } ?: throw AssertionError("No Primary stage found")
        val primaryStageId = primaryStage.id

        // Fetch programmed exercises for the Primary stage and get first exercise ID
        val exercisesResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_exercise/stage/$primaryStageId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Check what exercise was actually selected
        val exerciseName = exercisesResponse[0]["exercise_name"] as String

        // Only proceed with band weight validation if a banded exercise was selected
        val isBandedExercise = exerciseName.contains("Banded")

        val programmedExerciseId = (exercisesResponse[0]["id"] as Number).toLong()

        // Fetch set schemes for the programmed exercise and verify DE fields
        val setSchemesResponse =
            webTestClient.get()
                .uri("/api/v1/set_scheme/exercise/$programmedExerciseId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Verify we have set schemes
        assert(setSchemesResponse.isNotEmpty()) { "No set schemes found for exercise" }

        // Get the first set scheme by its ID to verify band information
        val firstSetSchemeId = setSchemesResponse[0]["id"].toString().toLong()

        // Check if this is week 4 (deload week) - if so, band_weight_lbs should be null
        val isWeek4 = programResponse.currentWeekNumber == 4

        if (isWeek4) {
            // Week 4 is deload week - no bands should be used
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.band_weight_lbs").isEmpty()
                .jsonPath("$.target_weight").exists()
                .jsonPath("$.target_weight").isNumber()
        } else if (isBandedExercise) {
            // For weeks 1-3, banded DE exercises should have band weights
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.band_weight_lbs").isNumber()
                .jsonPath("$.target_weight").exists()
                .jsonPath("$.target_weight").isNumber()
                .jsonPath("$.target_rep_count").exists()
                .jsonPath("$.target_rep_count").isNumber()
                .jsonPath("$.rest_seconds").exists()
                .jsonPath("$.rest_seconds").isNumber()
        } else {
            // For non-banded DE exercises, band_weight_lbs may be null
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.target_weight").exists()
                .jsonPath("$.target_weight").isNumber()
                .jsonPath("$.target_rep_count").exists()
                .jsonPath("$.target_rep_count").isNumber()
                .jsonPath("$.rest_seconds").exists()
                .jsonPath("$.rest_seconds").isNumber()
        }
    }

    @Test
    fun `should validate 2-day template invariants`() {
        // Create a new program with 2 days per week
        val programId2DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 2-Day Template",
                numDaysPerWeek = 2,
                token = userToken
            )

        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId2DayTemplate")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Validate 2-day template structure
        assert(workoutsResponse.size == 2) { "2-day program should have exactly 2 workouts" }

        // Validate each workout has the correct structure
        workoutsResponse.forEach { workout ->
            val workoutId = (workout["id"] as Number).toLong()
            val workoutName = workout["name"] as String

            // Fetch workout stages
            val stagesResponse =
                webTestClient.get()
                    .uri("/api/v1/workout_stage/workout/$workoutId")
                    .header("Authorization", "Bearer $userToken")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 2-day programs
            validateTwoDayWorkoutStages(stagesResponse, workoutName, userToken)
        }
    }

    @Test
    fun `should validate 3-day template invariants`() {
        // Create a new program with 3 days per week
        val programId3DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 3-Day Template",
                numDaysPerWeek = 3,
                token = userToken
            )

        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment.
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId3DayTemplate")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Validate 3-day template structure
        assert(workoutsResponse.size == 3) { "3-day program should have exactly 3 workouts" }

        // Validate each workout has the correct structure
        workoutsResponse.forEach { workout ->
            val workoutId = (workout["id"] as Number).toLong()
            val workoutName = workout["name"] as String

            // Fetch workout stages
            val stagesResponse =
                webTestClient.get()
                    .uri("/api/v1/workout_stage/workout/$workoutId")
                    .header("Authorization", "Bearer $userToken")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 3-day programs
            validateThreeDayWorkoutStages(stagesResponse, workoutName, userToken)
        }
    }

    @Test
    fun `should validate 4-day template invariants`() {
        // Create a new program with 4 days per week
        val programId4DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 4-Day Template",
                numDaysPerWeek = 4,
                token = userToken
            )

        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId4DayTemplate")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Validate 4-day template structure
        assert(workoutsResponse.size == 4) { "4-day program should have exactly 4 workouts" }

        // Validate each workout has the correct structure
        workoutsResponse.forEach { workout ->
            val workoutId = (workout["id"] as Number).toLong()
            val workoutName = workout["name"] as String

            // Fetch workout stages
            val stagesResponse =
                webTestClient.get()
                    .uri("/api/v1/workout_stage/workout/$workoutId")
                    .header("Authorization", "Bearer $userToken")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 4-day programs
            validateFourDayWorkoutStages(stagesResponse, workoutName, userToken)
        }
    }

    /**
     * Validates workout stages for 2-day template workouts.
     * 2-day programs have combined ME+DE days with no secondary movements.
     */
    private fun validateTwoDayWorkoutStages(
        stages: List<WorkoutStage>,
        workoutName: String,
        token: String
    ) {
        // 2-day programs should have:
        // 1. Primary stage (contains both ME and DE exercises)
        // 2. Accessory stage (optional, based on time)
        // 3. Conditioning stage (for DE workouts)

        val stageNames = stages.map { it.name.toString() }

        // Must have Primary stage
        assert(stageNames.contains("Primary")) {
            "2-day workout '$workoutName' must have Primary stage"
        }

        // May have Accessory stage (depends on session time and available exercises)
        // Note: Accessory stage may not be created if there's insufficient time or no suitable exercises

        // Should have Conditioning stage (for DE workouts)
        if (workoutName.contains("DE")) {
            assert(stageNames.contains("Conditioning")) {
                "2-day DE workout '$workoutName' must have Conditioning stage"
            }
        }

        // Should NOT have Secondary stage (2-day programs don't have secondary movements)
        assert(!stageNames.contains("Secondary")) {
            "2-day workout '$workoutName' should NOT have Secondary stage"
        }

        // Validate stage order
        val primaryStage = stages.find { it.name.toString() == "Primary" }
        val accessoryStage = stages.find { it.name.toString() == "Accessory" }
        val conditioningStage = stages.find { it.name.toString() == "Conditioning" }

        // Primary should come before Accessory
        if (primaryStage != null && accessoryStage != null) {
            assert(primaryStage.position < accessoryStage.position) {
                "Primary stage should come before Accessory stage in 2-day workout '$workoutName'"
            }
        }

        // Accessory should come before Conditioning
        if (accessoryStage != null && conditioningStage != null) {
            assert(accessoryStage.position < conditioningStage.position) {
                "Accessory stage should come before Conditioning stage in 2-day workout '$workoutName'"
            }
        }

        // Validate that Primary stage has exercises
        if (primaryStage != null) {
            val exercisesResponse =
                webTestClient.get()
                    .uri("/api/v1/programmed_exercise/stage/${primaryStage.id}")
                    .header("Authorization", "Bearer $token")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Map::class.java)
                    .returnResult()
                    .responseBody!!

            assert(exercisesResponse.isNotEmpty()) {
                "Primary stage in 2-day workout '$workoutName' should have exercises"
            }
        }
    }

    /**
     * Validates workout stages for 3-day template workouts.
     * 3-day programs have combined ME+DE days and full body DE days.
     */
    private fun validateThreeDayWorkoutStages(
        stages: List<WorkoutStage>,
        workoutName: String,
        token: String
    ) {
        // 3-day programs should have:
        // For combined ME+DE days: Primary, Accessory, Conditioning
        // For full body DE day: Primary, Accessory, Conditioning

        val stageNames = stages.map { it.name.toString() }

        // Must have Primary stage
        assert(stageNames.contains("Primary")) {
            "3-day workout '$workoutName' must have Primary stage"
        }

        // May have Accessory stage (depends on session time and available exercises)
        // Note: Accessory stage may not be created if there's insufficient time or no suitable exercises

        // Should have Conditioning stage (for DE workouts)
        if (workoutName.contains("DE")) {
            assert(stageNames.contains("Conditioning")) {
                "3-day DE workout '$workoutName' must have Conditioning stage"
            }
        }

        // Should NOT have Secondary stage (3-day programs don't have secondary movements)
        assert(!stageNames.contains("Secondary")) {
            "3-day workout '$workoutName' should NOT have Secondary stage"
        }

        // Validate stage order
        val primaryStage = stages.find { it.name.toString() == "Primary" }
        val accessoryStage = stages.find { it.name.toString() == "Accessory" }
        val conditioningStage = stages.find { it.name.toString() == "Conditioning" }

        // Primary should come before Accessory
        if (primaryStage != null && accessoryStage != null) {
            assert(primaryStage.position < accessoryStage.position) {
                "Primary stage should come before Accessory stage in 3-day workout '$workoutName'"
            }
        }

        // Accessory should come before Conditioning
        if (accessoryStage != null && conditioningStage != null) {
            assert(accessoryStage.position < conditioningStage.position) {
                "Accessory stage should come before Conditioning stage in 3-day workout '$workoutName'"
            }
        }

        // Validate that Primary stage has exercises
        if (primaryStage != null) {
            val exercisesResponse =
                webTestClient.get()
                    .uri("/api/v1/programmed_exercise/stage/${primaryStage.id}")
                    .header("Authorization", "Bearer $token")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Map::class.java)
                    .returnResult()
                    .responseBody!!

            assert(exercisesResponse.isNotEmpty()) {
                "Primary stage in 3-day workout '$workoutName' should have exercises"
            }
        }
    }

    /**
     * Validates workout stages for 4-day template workouts.
     * 4-day programs have separate ME and DE days with secondary movements.
     */
    private fun validateFourDayWorkoutStages(
        stages: List<WorkoutStage>,
        workoutName: String,
        token: String
    ) {
        // 4-day programs should have:
        // For ME days: Primary, Secondary (optional), Accessory, Conditioning
        // For DE days: Primary, Secondary (optional), Accessory, Conditioning

        val stageNames = stages.map { it.name.toString() }

        // Must have Primary stage
        assert(stageNames.contains("Primary")) {
            "4-day workout '$workoutName' must have Primary stage"
        }

        // May have Accessory stage (depends on session time and available exercises)
        // Note: Accessory stage may not be created if there's insufficient time or no suitable exercises

        // Should have Conditioning stage (for DE workouts)
        if (workoutName.contains("DE")) {
            assert(stageNames.contains("Conditioning")) {
                "4-day DE workout '$workoutName' must have Conditioning stage"
            }
        }

        // May have Secondary stage (for ME_Upper and DE_Upper)
        val hasSecondary = workoutName.contains("Upper")

        // Validate stage order
        val primaryStage = stages.find { it.name.toString() == "Primary" }
        val secondaryStage = stages.find { it.name.toString() == "Secondary" }
        val accessoryStage = stages.find { it.name.toString() == "Accessory" }
        val conditioningStage = stages.find { it.name.toString() == "Conditioning" }

        // Primary should come before Secondary
        if (primaryStage != null && secondaryStage != null) {
            assert(primaryStage.position < secondaryStage.position) {
                "Primary stage should come before Secondary stage in 4-day workout '$workoutName'"
            }
        }

        // Secondary should come before Accessory
        if (secondaryStage != null && accessoryStage != null) {
            assert(secondaryStage.position < accessoryStage.position) {
                "Secondary stage should come before Accessory stage in 4-day workout '$workoutName'"
            }
        }

        // Accessory should come before Conditioning
        if (accessoryStage != null && conditioningStage != null) {
            assert(accessoryStage.position < conditioningStage.position) {
                "Accessory stage should come before Conditioning stage in 4-day workout '$workoutName'"
            }
        }

        // Validate that Primary stage has exercises
        if (primaryStage != null) {
            val exercisesResponse =
                webTestClient.get()
                    .uri("/api/v1/programmed_exercise/stage/${primaryStage.id}")
                    .header("Authorization", "Bearer $token")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Map::class.java)
                    .returnResult()
                    .responseBody!!

            assert(exercisesResponse.isNotEmpty()) {
                "Primary stage in 4-day workout '$workoutName' should have exercises"
            }
        }

        // Validate Secondary stage if it exists
        if (hasSecondary && secondaryStage != null) {
            val exercisesResponse =
                webTestClient.get()
                    .uri("/api/v1/programmed_exercise/stage/${secondaryStage.id}")
                    .header("Authorization", "Bearer $token")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Map::class.java)
                    .returnResult()
                    .responseBody!!

            assert(exercisesResponse.isNotEmpty()) {
                "Secondary stage in 4-day workout '$workoutName' should have exercises"
            }
        }
    }
}
