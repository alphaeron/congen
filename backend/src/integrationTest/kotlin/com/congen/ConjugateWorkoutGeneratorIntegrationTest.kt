package com.congen

import com.congen.model.Program
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ConjugateWorkoutGeneratorIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0
    private var programId: Long = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // User and program creation will be done in individual test methods
        // to ensure Spring context is fully initialized first
    }

    private fun createTestUserAndProgram(): Pair<Int, Long> {
        val unique = System.nanoTime()
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User $unique")
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, name = "Test Program $unique")
        return Pair(userId, programId)
    }

    @Test
    fun `should generate 3-day conjugate workout program successfully`() {
        val (userId, programId) = createTestUserAndProgram()
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3)
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }

    @Test
    fun `should generate 2-day conjugate workout program successfully`() {
        val (userId, programId) = createTestUserAndProgram()
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 2)
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should generate 4-day conjugate workout program successfully`() {
        val (userId, programId) = createTestUserAndProgram()
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 4)
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
        assert(programResponse.name.contains("Week 2"))
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(4)
    }

    @Test
    fun `should handle invalid programDaysPerWeek in database`() {
        val (userId, programId) = createTestUserAndProgram()
        // Create user program preferences with invalid days per week should fail
        webTestClient.post()
            .uri("/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=5&session_time_length_in_minutes=60")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath(
                "$.error"
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should handle non-existent program`() {
        // When & Then - Try to generate for non-existent program
        webTestClient.post()
            .uri("/api/v1/conjugate_workout_generator/999999/generate")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should generate workout with user exercise preferences`() {
        val (userId, programId) = createTestUserAndProgram()
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Exercises already exist in migrations

        // Add exercise preferences (different exercises to avoid duplicate key constraint)
        webTestClient.post()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId&exercise_name=Safety Bar Squat&should_avoid=true")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/api/v1/user_exercise_preference/?user_id=$userId&exercise_name=Deadlift&should_avoid=false")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user equipment`() {
        val (userId, programId) = createTestUserAndProgram()
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Equipment already exists in migrations

        // Add user equipment (only add 'dumbbells' since 'power bar' is already added by reference data)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "dumbbells")

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user one rep max data`() {
        val (userId, programId) = createTestUserAndProgram()
        // Create user program preferences (required for workout generation)
        IntegrationTestHelpers.createTestUserProgramPreferences(webTestClient, userId, 3)

        // Exercises already exist in migrations

        // Add one rep max data
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Bench Press")
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat")

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate workout with user program preferences`() {
        val (userId, programId) = createTestUserAndProgram()
        // Add program preferences
        webTestClient.post()
            .uri("/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=3&session_time_length_in_minutes=60")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Verify program was created
        assert(programResponse.userId == userId)
        assert(programResponse.id == programId)
    }

    @Test
    fun `should generate DE set scheme with correct band and bar weights`() {
        val (userId, programId) = createTestUserAndProgram()
        // Set up user with 1RM for banded exercises specifically
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Banded Bench Press", oneRepMax = 200.0)
        IntegrationTestHelpers.createTestUserOneRepMax(webTestClient, userId, "Banded Safety Bar Squat", oneRepMax = 350.0)
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3)

        // Add equipment needed for banded exercises (power bar is already added by createAllReferenceDataForUser)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "bands")
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, "safety squat bar")

        // Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Fetch programmed workouts for the program and get DE workout ID
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map::class.java)
                .returnResult()
                .responseBody!!

        // Verify we have set schemes
        assert(setSchemesResponse.isNotEmpty()) { "No set schemes found for exercise" }

        // Get the first set scheme by its ID to verify band information
        val firstSetSchemeId = setSchemesResponse[0]["id"].toString().toLong()

        val response =
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String::class.java)
                .returnResult()
                .responseBody!!

        // Check if this is week 4 (deload week) - if so, band_weight_lbs should be null
        val isWeek4 = programResponse.currentWeekNumber == 4

        if (isWeek4) {
            // Week 4 is deload week - no bands should be used
            webTestClient.get()
                .uri("/api/v1/set_scheme/$firstSetSchemeId")
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
        val (userId, programId) = createTestUserAndProgram()
        // Given - Set up user with 2-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 2)

        // When - Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Fetch and validate workouts
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 2-day programs
            validateTwoDayWorkoutStages(stagesResponse, workoutName)
        }
    }

    @Test
    fun `should validate 3-day template invariants`() {
        val (userId, programId) = createTestUserAndProgram()
        // Given - Set up user with 3-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 3)

        // When - Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Fetch and validate workouts
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 3-day programs
            validateThreeDayWorkoutStages(stagesResponse, workoutName)
        }
    }

    @Test
    fun `should validate 4-day template invariants`() {
        val (userId, programId) = createTestUserAndProgram()
        // Given - Set up user with 4-day program
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId, 4)

        // When - Generate conjugate program
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/conjugate_workout_generator/$programId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()
                .responseBody!!

        // Then - Fetch and validate workouts
        val workoutsResponse =
            webTestClient.get()
                .uri("/api/v1/programmed_workout/program/${programResponse.id}")
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
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(WorkoutStage::class.java)
                    .returnResult()
                    .responseBody!!

            // Validate stage structure for 4-day programs
            validateFourDayWorkoutStages(stagesResponse, workoutName)
        }
    }

    /**
     * Validates workout stages for 2-day template workouts.
     * 2-day programs have combined ME+DE days with no secondary movements.
     */
    private fun validateTwoDayWorkoutStages(
        stages: List<WorkoutStage>,
        workoutName: String
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
        workoutName: String
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
        workoutName: String
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
