package com.congen

import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.User
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageTypeEnum
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Helper class for integration tests providing reusable constants and helper methods.
 * This class reduces duplication across integration tests and provides consistent test data.
 */
object IntegrationTestHelpers {
    // Common test data constants
    const val TEST_USER_NAME = "Integration Test User"
    const val TEST_USER_AGE = 30
    const val TEST_USER_HEIGHT = 180.0
    const val TEST_USER_WEIGHT = 75.0

    const val TEST_EQUIPMENT_NAME = "bench"
    const val TEST_EQUIPMENT_DESCRIPTION = "A bench with a foam pad, commonly used for the bench press or other similar exercises."
    const val TEST_EQUIPMENT_NAME_2 = "power bar"
    const val TEST_EQUIPMENT_DESCRIPTION_2 = "A universal weightlifting bar that can be used to perform a variety of exercises."

    const val TEST_MUSCLE_NAME = "pec major"
    const val TEST_MUSCLE_DESCRIPTION =
        "The primary functions are flexion, adduction, and internal rotation of the humerus. " +
            "The pectoral major may colloquially be referred to as \"pecs\", \"pectoral muscle\", or \"chest muscle\", " +
            "because it is the largest and most superficial muscle in the chest area."
    const val TEST_MUSCLE_NAME_2 = "lats"
    const val TEST_MUSCLE_DESCRIPTION_2 = "Functionally, the latissimus dorsi muscle belongs to the muscles of the scapular motion."

    const val TEST_EXERCISE_NAME = "Bench Press"
    const val TEST_EXERCISE_DESCRIPTION = "Standard bench press"
    const val TEST_EXERCISE_NAME_2 = "Safety Bar Squat"
    const val TEST_EXERCISE_DESCRIPTION_2 = "Squat with a safety bar."

    const val TEST_PROGRAM_NAME = "Strength Program"
    const val TEST_PROGRAM_DESCRIPTION = "A comprehensive strength training program"

    const val TEST_WORKOUT_NAME = "Upper Body Workout"
    const val TEST_WORKOUT_DESCRIPTION = "Focus on upper body strength"

    const val TEST_STAGE_NAME = "Warm-up"
    const val TEST_STAGE_DESCRIPTION = "Light warm-up exercises"

    const val TEST_SET_SCHEME_NAME = "3x5"
    const val TEST_SET_SCHEME_DESCRIPTION = "Three sets of five reps"

    // Object mapper for JSON operations
    val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Creates a test user via the API and returns the user ID.
     */
    fun createTestUser(
        webTestClient: WebTestClient,
        name: String = TEST_USER_NAME,
        age: Int = TEST_USER_AGE,
        height: Double = TEST_USER_HEIGHT,
        weight: Double = TEST_USER_WEIGHT,
        email: String? = "test.${System.nanoTime()}@example.com",
        password: String? = "notarealpassword",
        unit: WeightUnit? = null
    ): Int {
        val baseUri =
            "/api/v1/user/?name=$name" +
                "&age=$age" +
                "&height=$height" +
                "&weight=$weight"

        val withAuth =
            if (email != null && password != null) {
                "$baseUri&email=$email&password=$password"
            } else {
                baseUri
            }

        val finalUri =
            if (unit != null) {
                "$withAuth&unit=${unit.name}"
            } else {
                withAuth
            }

        val result =
            webTestClient.post()
                .uri(finalUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()

        val response = result.responseBody
        if (response == null) {
            throw RuntimeException("User creation failed - response body is null")
        }

        return response.id
    }

    /**
     * Retrieves a test user by ID via the API.
     */
    fun getTestUser(
        webTestClient: WebTestClient,
        userId: Int
    ): User {
        val result =
            webTestClient.get()
                .uri("/api/v1/user/$userId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()

        val response = result.responseBody
        if (response == null) {
            throw RuntimeException("User retrieval failed - response body is null")
        }

        return response
    }

    /**
     * Returns the equipment name for testing.
     * Equipment already exists in migrations, so we don't need to create it.
     */
    fun getTestEquipment(name: String = TEST_EQUIPMENT_NAME): String {
        return name
    }

    /**
     * Returns the muscle name for testing.
     * Muscles already exist in migrations, so we don't need to create them.
     */
    fun getTestMuscle(name: String = TEST_MUSCLE_NAME): String {
        return name
    }

    /**
     * Returns the exercise name for testing.
     * Exercises already exist in migrations, so we don't need to create them.
     */
    fun getTestExercise(name: String = TEST_EXERCISE_NAME): String {
        return name
    }

    /**
     * Creates test program via the API and returns the program ID.
     */
    fun createTestProgram(
        webTestClient: WebTestClient,
        userId: Int,
        name: String = TEST_PROGRAM_NAME,
        isActive: Boolean = true
    ): Long {
        val result =
            webTestClient.post()
                .uri(
                    "/api/v1/program/?user_id=$userId" +
                        "&name=$name" +
                        "&is_active=$isActive"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(Program::class.java)
                .returnResult()

        val response = result.responseBody
        if (response == null) {
            throw RuntimeException("Program creation failed - response body is null")
        }

        return response.id
    }

    /**
     * Creates test programmed workout via the API and returns the workout ID.
     */
    fun createTestProgrammedWorkout(
        webTestClient: WebTestClient,
        programId: Long,
        // Use unique day number
        dayNumber: Int = (System.nanoTime() % 1000).toInt() + 1,
        name: String = TEST_WORKOUT_NAME
    ): Long {
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/?program_id=$programId" +
                        "&day_number=$dayNumber" +
                        "&name=$name"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedWorkout::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    /**
     * Creates test workout stage via the API and returns the stage ID.
     */
    fun createTestWorkoutStage(
        webTestClient: WebTestClient,
        programmedWorkoutId: Long,
        stageTypeId: Int = 1,
        // Use unique position
        position: Int = (System.nanoTime() % 1000).toInt() + 1,
        name: String = TEST_STAGE_NAME
    ): Long {
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/workout_stage/?programmed_workout_id=$programmedWorkoutId" +
                        "&stage_type_id=$stageTypeId" +
                        "&position=$position" +
                        "&name=$name"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(WorkoutStage::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    /**
     * Creates test user equipment via the API.
     * Equipment needs to exist first, so we create it if needed.
     */
    fun createTestUserEquipment(
        webTestClient: WebTestClient,
        userId: Int,
        equipmentName: String = TEST_EQUIPMENT_NAME
    ) {
        // Now create the user equipment relationship
        webTestClient.post()
            .uri(
                "/api/v1/user_equipment/?user_id=$userId" +
                    "&equipment_name=$equipmentName"
            )
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates test user exercise preference via the API.
     */
    fun createTestUserExercisePreference(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String = TEST_EXERCISE_NAME,
        shouldAvoid: Boolean = false
    ) {
        webTestClient.post()
            .uri(
                "/api/v1/user_exercise_preference/?user_id=$userId" +
                    "&exercise_name=$exerciseName" +
                    "&should_avoid=$shouldAvoid"
            )
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates test user one rep max via the API.
     */
    fun createTestUserOneRepMax(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String = TEST_EXERCISE_NAME,
        oneRepMax: Double = 100.0,
        unit: String = "KG"
    ) {
        webTestClient.put()
            .uri(
                "/api/v1/user_one_rep_max/?user_id=$userId" +
                    "&exercise_name=$exerciseName" +
                    "&one_rep_max=$oneRepMax" +
                    "&unit=$unit"
            )
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates test user program preferences via the API.
     */
    fun createTestUserProgramPreferences(
        webTestClient: WebTestClient,
        userId: Int,
        programDaysPerWeek: Int = 3,
        sessionTimeLengthInMinutes: Int = 60
    ) {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId" +
                    "&program_days_per_week=$programDaysPerWeek" +
                    "&session_time_length_in_minutes=$sessionTimeLengthInMinutes"
            )
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates minimal reference data for a user (just program preferences and one piece of equipment).
     * This is much faster than creating all reference data.
     */
    fun createMinimalReferenceDataForUser(
        webTestClient: WebTestClient,
        userId: Int
    ) {
        createTestUserProgramPreferences(webTestClient, userId)
        createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME)
    }

    /**
     * Creates all reference data for a user.
     * Use createMinimalReferenceDataForUser for faster tests when full data isn't needed.
     */
    fun createAllReferenceDataForUser(
        webTestClient: WebTestClient,
        userId: Int,
        programDaysPerWeek: Int = 3
    ) {
        // Create program preferences
        createTestUserProgramPreferences(webTestClient, userId, programDaysPerWeek)

        // Create user equipment (bench and power bar)
        createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME)
        createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME_2)

        // Create user exercise preferences
        createTestUserExercisePreference(webTestClient, userId, "Deadlift")
        createTestUserExercisePreference(webTestClient, userId, TEST_EXERCISE_NAME_2)

        // Create user one rep maxes
        createTestUserOneRepMax(webTestClient, userId, "Deadlift")
        createTestUserOneRepMax(webTestClient, userId, TEST_EXERCISE_NAME_2)
    }

    /**
     * Creates a complete test program with workout, stage, and exercise.
     * Returns a map with all the created IDs for easy access.
     */
    fun createCompleteTestProgram(
        webTestClient: WebTestClient,
        userId: Int
    ): Map<String, Any> {
        val programId = createTestProgram(webTestClient, userId)
        val workoutId = createTestProgrammedWorkout(webTestClient, programId)
        val stageId = createTestWorkoutStage(webTestClient, workoutId)

        return mapOf(
            "userId" to userId,
            "programId" to programId,
            "workoutId" to workoutId,
            "stageId" to stageId
        )
    }

    /**
     * Creates test set scheme via the API and returns the set scheme ID.
     */
    fun createTestSetScheme(
        webTestClient: WebTestClient,
        programmedExerciseId: Long,
        setNumber: Int = 1,
        targetWeight: Double = 100.0,
        targetRepCount: Int = 8,
        restSeconds: Int = 120,
        wasSetPerformed: Boolean = false,
        isAmrap: Boolean = false,
        isEmom: Boolean = false,
        useTempo: Boolean = false
    ): Long {
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/set_scheme/?programmed_exercise_id=$programmedExerciseId" +
                        "&set_number=$setNumber" +
                        "&was_set_performed=$wasSetPerformed" +
                        "&is_amrap=$isAmrap" +
                        "&is_emom=$isEmom" +
                        "&use_tempo=$useTempo" +
                        "&target_weight=$targetWeight" +
                        "&target_rep_count=$targetRepCount" +
                        "&rest_seconds=$restSeconds"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(SetScheme::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    /**
     * Creates test programmed exercise via the API and returns the programmed exercise ID.
     */
    fun createTestProgrammedExercise(
        webTestClient: WebTestClient,
        workoutStageId: Long,
        exerciseName: String = TEST_EXERCISE_NAME,
        position: Int = 1
    ): Long {
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_exercise/?workout_stage_id=$workoutStageId" +
                        "&exercise_name=$exerciseName" +
                        "&position=$position"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedExercise::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    /**
     * Creates test exercise rotation history via the API and returns the record ID.
     */
    fun createTestExerciseRotationHistory(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String,
        rotationDate: String,
        isAccessory: Boolean = false
    ): Long {
        val response =
            webTestClient.post()
                .uri(
                    "/api/v1/exercise_rotation_history/?user_id=$userId" +
                        "&exercise_name=$exerciseName" +
                        "&rotation_date=$rotationDate" +
                        "&is_accessory=$isAccessory"
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseRotationHistory::class.java)
                .returnResult()
                .responseBody!!
        return response.id
    }

    /**
     * Gets test workout stage type display name (workout stage types are pre-populated in database).
     */
    fun createTestWorkoutStageType(name: WorkoutStageTypeEnum = WorkoutStageTypeEnum.WARMUP): String {
        // Workout stage types are pre-populated in the database by BaseIntegrationTest
        return name.displayName
    }

    /**
     * Returns a set of primary and accessory exercises for testing.
     * Exercises already exist in migrations, so we don't need to create them.
     */
    fun getPrimaryAndAccessoryExercises() {
        // Primary exercises already exist in migrations: "Deadlift", "Safety Bar Squat", "Bent-Over Row", etc.
        // Accessory exercises already exist in migrations: "Chin-Up", "Ab Wheel", "GHR", etc.
        // No need to create anything
    }

    /**
     * Returns all required equipment for testing.
     * Equipment already exists in migrations, so we don't need to create it.
     */
    fun getAllEquipment() {
        // Equipment already exists in migrations: "power bar", "dumbbells", etc.
        // No need to create anything
    }

    /**
     * Sets a user's weight unit preference for a specific exercise via the API.
     */
    fun setUserWeightUnitPreference(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String,
        preferredUnit: String
    ) {
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")
        webTestClient.put()
            .uri(
                "/api/v1/user_weight_unit_preference/?user_id=$userId" +
                    "&exercise_name=$encodedExerciseName" +
                    "&preferred_unit=$preferredUnit"
            )
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates or updates a user's one rep max for a specific exercise, with optional unit.
     * Returns the created UserOneRepMax object.
     */
    fun putUserOneRepMax(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String,
        oneRepMax: java.math.BigDecimal,
        unit: String? = null
    ): UserOneRepMax {
        val uri = StringBuilder("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=$exerciseName&one_rep_max=$oneRepMax")
        if (unit != null) {
            uri.append("&unit=$unit")
        }
        return webTestClient.put()
            .uri(uri.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserOneRepMax::class.java)
            .returnResult()
            .responseBody!!
    }

    /**
     * Gets a user's one rep max for a specific exercise, with optional unit for conversion.
     * Returns the UserOneRepMax object.
     */
    fun getUserOneRepMax(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String,
        unit: String? = null
    ): UserOneRepMax {
        val uri = StringBuilder("/api/v1/user_one_rep_max/user/$userId/exercise/$exerciseName")
        if (unit != null) {
            uri.append("?unit=$unit")
        }
        return webTestClient.get()
            .uri(uri.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserOneRepMax::class.java)
            .returnResult()
            .responseBody!!
    }

    /**
     * Gets all one rep maxes for a user, returned as a list.
     */
    fun getAllUserOneRepMaxes(
        webTestClient: WebTestClient,
        userId: Int
    ): List<UserOneRepMax> {
        return webTestClient.get()
            .uri("/api/v1/user_one_rep_max/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserOneRepMax::class.java)
            .returnResult()
            .responseBody ?: emptyList()
    }
}
