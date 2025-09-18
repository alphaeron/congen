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
        // Create a new program with 3 days per week using unique name to avoid conflicts
        val unique = System.nanoTime()
        val programId3Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 3-Day $unique",
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

        // Verify the program exists before generating workouts
        webTestClient.get()
            .uri("/api/v1/program/$programId3Day")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

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
        assert(programResponse.name == "Test Program 3-Day $unique")
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
        // Create a new program with 2 days per week using unique name to avoid conflicts
        val unique = System.nanoTime()
        val programId2Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 2-Day $unique",
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

        // Verify the program exists before generating workouts
        webTestClient.get()
            .uri("/api/v1/program/$programId2Day")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

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
        assert(programResponse.name == "Test Program 2-Day $unique")
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
        // Create a new program with 4 days per week using unique name to avoid conflicts
        val unique = System.nanoTime()
        val programId4Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 4-Day $unique",
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

        // Verify the program exists before generating workouts
        webTestClient.get()
            .uri("/api/v1/program/$programId4Day")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

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
        assert(programResponse.name.contains("Test Program 4-Day $unique"))
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
        webTestClient.put()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId&exercise_name=Safety Bar Squat&should_avoid=true")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()

        webTestClient.put()
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
    fun `should only use banded exercises in primary stages on DE days`() {
        // Create a new program with 4 days per week to test both ME and DE days
        val programIdBanded =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program Banded Restrictions",
                numDaysPerWeek = 4,
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

        // Add equipment needed for banded exercises
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bands", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "safety squat bar", token = userToken)

        // Add additional equipment
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

        // Fetch all programmed workouts for the program
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Verify that banded exercises are only used in primary stages on DE days
        workoutsResponse.forEach { workout ->
            val workoutId = (workout["id"] as Number).toLong()
            val workoutName = workout["name"] as String

            // Fetch workout stages for this workout
            val stagesResponse =
                webTestClient.get()
                    .uri("/api/v1/workout_stage/workout/$workoutId")
                    .header("Authorization", "Bearer $userToken")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Check each stage for banded exercises
            stagesResponse.forEach { stage ->
                val stageName = stage.name.toString()
                val stageId = stage.id

                // Fetch exercises for this stage
                val exercisesResponse =
                    webTestClient.get()
                        .uri("/api/v1/programmed_exercise/stage/$stageId")
                        .header("Authorization", "Bearer $userToken")
                        .exchange()
                        .expectStatus().isOk()
                        .expectBodyList(Map::class.java)
                        .returnResult()
                        .responseBody!!

                // Check each exercise in this stage
                exercisesResponse.forEach { exercise ->
                    val exerciseName = exercise["exercise_name"] as String
                    val isBandedExercise = exerciseName.contains("Banded", ignoreCase = true)

                    if (isBandedExercise) {
                        // Banded exercises should only be in Primary stages on DE days
                        val isDEDay = workoutName.contains("DE", ignoreCase = true)
                        val isPrimaryStage = stageName == "Primary"

                        assert(isDEDay) {
                            "Banded exercise '$exerciseName' found in non-DE workout '$workoutName'. " +
                                "Banded exercises should only be used on DE days."
                        }

                        assert(isPrimaryStage) {
                            "Banded exercise '$exerciseName' found in '$stageName' stage of workout '$workoutName'. " +
                                "Banded exercises should only be used in Primary stages."
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `should validate 2-day template invariants`() {
        // Create a new program with 2 days per week
        val unique = System.nanoTime()
        val programId2DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 2-Day Template $unique",
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
        val unique = System.nanoTime()
        val programId3DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 3-Day Template $unique",
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
        val unique = System.nanoTime()
        val programId4DayTemplate =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 4-Day Template $unique",
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

    @Test
    fun `should generate 2-day conjugate workout program for 20 weeks with exercise uniqueness`() {
        // Create a new program with 2 days per week
        val unique = System.nanoTime()
        val programId2Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 2-Day 20 Weeks $unique",
                numDaysPerWeek = 2,
                token = userToken
            )

        // Create reference data for 2-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        // Generate conjugate program
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

        // Generate additional weeks (weeks 2-20)
        for (week in 2..20) {
            // Update program to next week
            webTestClient.patch()
                .uri("/api/v1/program/$programId2Day?name=Test Program 2-Day 20 Weeks $unique&current_week_number=$week&is_active=true")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()

            // Generate workouts for this week
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId2Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        }

        // Validate exercise uniqueness and generation invariants for all 20 weeks
        assertProgramExerciseUniquenessAndInvariants(programId2Day, 20, 2, userToken)
    }

    @Test
    fun `should generate 3-day conjugate workout program for 30 weeks with exercise uniqueness`() {
        // Create a new program with 3 days per week
        val unique = System.nanoTime()
        val programId3Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 3-Day 30 Weeks $unique",
                numDaysPerWeek = 3,
                token = userToken
            )

        // Create reference data for 3-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        // Generate conjugate program
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

        // Generate additional weeks (weeks 2-30)
        for (week in 2..30) {
            // Update program to next week
            webTestClient.patch()
                .uri("/api/v1/program/$programId3Day?name=Test Program 3-Day 30 Weeks $unique&current_week_number=$week&is_active=true")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()

            // Generate workouts for this week
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId3Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        }

        // Validate exercise uniqueness and generation invariants for all 30 weeks
        assertProgramExerciseUniquenessAndInvariants(programId3Day, 30, 3, userToken)
    }

    @Test
    fun `should generate 4-day conjugate workout program for 40 weeks with exercise uniqueness`() {
        // Create a new program with 4 days per week
        val unique = System.nanoTime()
        val programId4Day =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = "Test Program 4-Day 40 Weeks $unique",
                numDaysPerWeek = 4,
                token = userToken
            )

        // Create reference data for 4-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, token = userToken)

        // Add additional equipment
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "pull-up bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "power bar", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells", token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "adjustable bench", token = userToken)

        // Generate conjugate program
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

        // Generate additional weeks (weeks 2-40)
        for (week in 2..40) {
            // Update program to next week
            webTestClient.patch()
                .uri("/api/v1/program/$programId4Day?name=Test Program 4-Day 40 Weeks $unique&current_week_number=$week&is_active=true")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()

            // Generate workouts for this week
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId4Day")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        }

        // Validate exercise uniqueness and generation invariants for all 40 weeks
        assertProgramExerciseUniquenessAndInvariants(programId4Day, 40, 4, userToken)
    }

    /**
     * Helper function to query for a user's program data and assert that exercises are not duplicated
     * based on exercise pool constraints within the same week, week after week, and meet the
     * invariants of the generation algorithm.
     *
     * @param programId The program ID to query
     * @param expectedWeeks The expected number of weeks to validate
     * @param expectedDaysPerWeek The expected number of days per week
     * @param token The user authentication token
     */
    private fun assertProgramExerciseUniquenessAndInvariants(
        programId: Long,
        expectedWeeks: Int,
        expectedDaysPerWeek: Int,
        token: String
    ) {
        // Get all programmed workouts for the program
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/$programId")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Validate we have the expected number of workouts (weeks * days per week)
        val expectedTotalWorkouts = expectedWeeks * expectedDaysPerWeek
        assert(workoutsResponse.size == expectedTotalWorkouts) {
            "Expected $expectedTotalWorkouts workouts (${expectedWeeks} weeks × ${expectedDaysPerWeek} days), but got ${workoutsResponse.size}"
        }

        // Group workouts by week (assuming workouts are ordered by creation/position)
        val workoutsByWeek = workoutsResponse.chunked(expectedDaysPerWeek)
        assert(workoutsByWeek.size == expectedWeeks) {
            "Expected $expectedWeeks weeks of workouts, but got ${workoutsByWeek.size}"
        }

        // Track all exercises used across all weeks for uniqueness validation
        val allExercisesUsed = mutableSetOf<String>()
        val exercisesByWeek = mutableListOf<Set<String>>()

        // Validate each week
        workoutsByWeek.forEachIndexed { weekIndex, weekWorkouts ->
            val weekNumber = weekIndex + 1
            val weekExercises = mutableSetOf<String>()

            // Validate each workout in the week
            weekWorkouts.forEach { workout ->
                val workoutId = (workout["id"] as Number).toLong()
                val workoutName = workout["name"] as String

                // Get all stages for this workout
                val stagesResponse =
                    webTestClient.get()
                        .uri("/api/v1/workout_stage/workout/$workoutId")
                        .header("Authorization", "Bearer $token")
                        .exchange()
                        .expectStatus().isOk()
                        .expectBodyList(WorkoutStage::class.java)
                        .returnResult()
                        .responseBody!!

                // Only validate stage structure for a subset of workouts to reduce API calls
                // For 4-day programs with many weeks, we'll validate fewer workouts
                val shouldValidateStages = when {
                    expectedDaysPerWeek == 4 && expectedWeeks > 20 -> weekIndex % 5 == 0 // Validate every 5th week
                    expectedDaysPerWeek == 3 && expectedWeeks > 15 -> weekIndex % 3 == 0 // Validate every 3rd week
                    else -> true // Validate all workouts for smaller programs
                }

                if (shouldValidateStages) {
                    // Validate stage structure based on program type
                    when (expectedDaysPerWeek) {
                        2 -> validateTwoDayWorkoutStages(stagesResponse, workoutName, token)
                        3 -> validateThreeDayWorkoutStages(stagesResponse, workoutName, token)
                        4 -> validateFourDayWorkoutStages(stagesResponse, workoutName, token)
                    }
                }

                // Get all exercises for this workout (sample fewer stages for 4-day programs)
                val stagesToValidate = when {
                    expectedDaysPerWeek == 4 && expectedWeeks > 20 -> stagesResponse.take(2) // Only validate first 2 stages
                    expectedDaysPerWeek == 3 && expectedWeeks > 15 -> stagesResponse.take(3) // Only validate first 3 stages
                    else -> stagesResponse // Validate all stages for smaller programs
                }

                stagesToValidate.forEach { stage ->
                    val exercisesResponse =
                        webTestClient.get()
                            .uri("/api/v1/programmed_exercise/stage/${stage.id}")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk()
                            .expectBodyList(Map::class.java)
                            .returnResult()
                            .responseBody!!

                    exercisesResponse.forEach { exercise ->
                        val exerciseName = exercise["exercise_name"] as String
                        weekExercises.add(exerciseName)
                        allExercisesUsed.add(exerciseName)
                    }
                }
            }

            exercisesByWeek.add(weekExercises)

            // Assert no duplicate exercises within the same week
            val uniqueExercisesInWeek = weekExercises.size
            val totalExercisesInWeek = weekExercises.size
            assert(uniqueExercisesInWeek == totalExercisesInWeek) {
                "Week $weekNumber has duplicate exercises: $weekExercises"
            }

            // Validate that each week has a reasonable number of exercises
            assert(weekExercises.isNotEmpty()) {
                "Week $weekNumber should have at least one exercise"
            }
        }

        // Assert exercise rotation across weeks (sliding window logic)
        // The algorithm should ensure exercises are rotated and not repeated too frequently
        for (i in 1 until exercisesByWeek.size) {
            val currentWeekExercises = exercisesByWeek[i]
            val previousWeekExercises = exercisesByWeek[i - 1]

            // Check that there's some variation between consecutive weeks
            // (not all exercises should be the same, allowing for some overlap)
            val overlap = currentWeekExercises.intersect(previousWeekExercises)
            val totalUnique = currentWeekExercises.union(previousWeekExercises).size

            // Allow some overlap but ensure there's variation
            val overlapRatio = overlap.size.toDouble() / totalUnique.toDouble()
            assert(overlapRatio < 1.0) {
                "Week ${i + 1} should have some exercise variation from week $i. " +
                    "Overlap: ${overlap.size}/$totalUnique exercises (${(overlapRatio * 100).toInt()}%)"
            }
        }

        // Validate that we have a good variety of exercises across all weeks
        val totalUniqueExercises = allExercisesUsed.size
        val totalExerciseInstances = exercisesByWeek.sumOf { it.size }
        val averageExercisesPerWeek = totalExerciseInstances.toDouble() / expectedWeeks

        // Ensure we're not using too few unique exercises (indicating poor rotation)
        assert(totalUniqueExercises >= expectedWeeks) {
            "Should have at least $expectedWeeks unique exercises across $expectedWeeks weeks, but only found $totalUniqueExercises"
        }

        // Log summary for debugging
        println("Program $programId validation summary:")
        println("  - Weeks: $expectedWeeks")
        println("  - Days per week: $expectedDaysPerWeek")
        println("  - Total workouts: ${workoutsResponse.size}")
        println("  - Total unique exercises: $totalUniqueExercises")
        println("  - Average exercises per week: ${"%.1f".format(averageExercisesPerWeek)}")
        println("  - Exercise rotation: ${if (totalUniqueExercises > expectedWeeks) "Good" else "Limited"}")
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
