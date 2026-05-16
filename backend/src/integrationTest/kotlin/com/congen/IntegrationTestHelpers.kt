package com.congen

import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.User
import com.congen.model.UserOneRepMax
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageTypeEnum
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.net.URLEncoder

/**
 * Helper class for integration tests providing reusable constants and helper methods.
 * This class reduces duplication across integration tests and provides consistent test data.
 */
object IntegrationTestHelpers {
    // Common test data constants
    const val TEST_USER_NAME = "Integration Test User"

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
     * Creates a test user via the API and returns the user's Keycloak ID.
     * This function works with the new authentication flow that uses Keycloak user IDs.
     * The Keycloak user ID is automatically extracted from the JWT token.
     * The controller extracts user info from the JWT token.
     *
     * @param webTestClient The WebTestClient to use for API calls
     * @param token The JWT token to use for authentication (optional)
     * @return The Keycloak ID of the created user
     */
    fun createTestUser(
        webTestClient: WebTestClient,
        token: String? = null,
    ): String {
        val baseUri = "/api/v1/user/"

        // Add authorization header - use provided token or get a default one
        val authToken = token ?: BaseIntegrationTest.getDefaultTestToken()

        val request =
            webTestClient.post()
                .uri(baseUri)

        request.header("Authorization", "Bearer $authToken")

        val result =
            request
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()

        val response = result.responseBody
        if (response == null) {
            throw RuntimeException("User creation failed - response body is null")
        }

        return response.keycloakId
    }

    /**
     * Creates consent for the authenticated user.
     *
     * @param webTestClient The WebTestClient to use for API calls
     * @param token The JWT token to use for authentication
     * @param hasConsent Whether the user has given consent for data processing (defaults to true)
     */
    fun createUserConsent(
        webTestClient: WebTestClient,
        token: String,
        hasConsent: Boolean = true,
    ) {
        val consentRequest =
            webTestClient.post()
                .uri("/api/v1/gdpr/consent?consent=$hasConsent")
                .header("Authorization", "Bearer $token")

        consentRequest
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Retrieves the current test user's profile via the API.
     * Uses the /me endpoint which returns the profile of the authenticated user.
     */
    fun getTestUser(
        webTestClient: WebTestClient,
        token: String? = null
    ): User {
        val request =
            webTestClient.get()
                .uri("/api/v1/user/me")

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val result =
            request
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
        keycloakId: String,
        name: String = TEST_PROGRAM_NAME,
        isActive: Boolean = true,
        numDaysPerWeek: Int = 4,
        token: String? = null
    ): Long {
        val request =
            webTestClient.post()
                .uri(
                    "/api/v1/program/?user_id=$keycloakId" +
                        "&name=$name" +
                        "&is_active=$isActive" +
                        "&num_days_per_week=$numDaysPerWeek"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val result =
            request
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
        dayNumber: Int = (System.nanoTime() % 365).toInt() + 1,
        name: String = TEST_WORKOUT_NAME,
        token: String? = null
    ): Long {
        val request =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_workout/?program_id=$programId" +
                        "&day_number=$dayNumber" +
                        "&name=$name"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val response =
            request
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
        name: String = TEST_STAGE_NAME,
        token: String? = null
    ): Long {
        val request =
            webTestClient.post()
                .uri(
                    "/api/v1/workout_stage/?programmed_workout_id=$programmedWorkoutId" +
                        "&stage_type_id=$stageTypeId" +
                        "&position=$position" +
                        "&name=$name"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val response =
            request
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
        keycloakId: String,
        equipmentName: String = TEST_EQUIPMENT_NAME,
        token: String? = null
    ) {
        val encodedEquipmentName = URLEncoder.encode(equipmentName, Charsets.UTF_8)
        val request =
            webTestClient.post()
                .uri(
                    "/api/v1/user_equipment/?user_id=$keycloakId" +
                        "&equipment_name=$encodedEquipmentName"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        // Now create the user equipment relationship
        request
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates test user exercise preference via the API.
     */
    fun createTestUserExercisePreference(
        webTestClient: WebTestClient,
        keycloakId: String,
        exerciseName: String = TEST_EXERCISE_NAME,
        shouldAvoid: Boolean = false,
        token: String? = null
    ) {
        val request =
            webTestClient.put()
                .uri(
                    "/api/v1/user_exercise_preference/?user_id=$keycloakId" +
                        "&exercise_name=$exerciseName" +
                        "&should_avoid=$shouldAvoid"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        request
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates test user one rep max via the API.
     */
    fun createTestUserOneRepMax(
        webTestClient: WebTestClient,
        keycloakId: String,
        exerciseName: String = TEST_EXERCISE_NAME,
        oneRepMax: Double = 100.0,
        unit: String = "KG",
        token: String? = null
    ) {
        val request =
            webTestClient.put()
                .uri(
                    "/api/v1/user_one_rep_max/?user_id=$keycloakId" +
                        "&exercise_name=$exerciseName" +
                        "&one_rep_max=$oneRepMax" +
                        "&unit=$unit"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        request
            .exchange()
            .expectStatus().isOk()
    }

    /**
     * Equipment names defined in exercise/equipment migrations. Used so conjugate generator
     * integration tests can match exercise equipment requirements instead of relying on the
     * equipment filter fallback path.
     */
    val CONJUGATE_GENERATOR_USER_EQUIPMENT_NAMES: List<String> =
        listOf(
            TEST_EQUIPMENT_NAME,
            TEST_EQUIPMENT_NAME_2,
            "pull-up bar",
            "bands",
            "dumbbells",
            "adjustable bench",
            "safety squat bar",
            "ab wheel",
            "trx",
            "med ball",
            "box",
            "kettlebell",
            "trap bar",
            "reverse hyper",
            "ghr",
            "landmine",
            "Lat Pulldown",
            "power rack",
            "dip bars",
            "axle",
            "airex pad",
            "valslides",
            "sliders",
            "physioball",
            "iron neck",
            "weight plate",
            "Straight Bar Handle",
            "Curl Bar Handle",
            "V-Bar Handle",
            "U-Bar Handle",
            "Triangle Bar Handle",
            "Rope Handle",
            "bodyblade",
            "sled",
            "sandbag",
            "battle rope",
            "hurdle",
            "rope",
            "tire"
        )

    /**
     * Creates user equipment covering the full conjugate exercise catalog.
     */
    fun createFullUserEquipmentForConjugateTests(
        webTestClient: WebTestClient,
        keycloakId: String,
        token: String? = null
    ) {
        CONJUGATE_GENERATOR_USER_EQUIPMENT_NAMES.forEach { equipmentName ->
            createTestUserEquipment(webTestClient, keycloakId, equipmentName, token = token)
        }
    }

    /**
     * Creates user equipment required for conditioning stage exercise selection.
     */
    fun createConditioningUserEquipment(
        webTestClient: WebTestClient,
        keycloakId: String,
        token: String? = null
    ) {
        val conditioningEquipment =
            listOf(
                "sled",
                "sandbag",
                "battle rope",
                "hurdle",
                "rope",
                "tire",
                "med ball",
                "box",
                "kettlebell"
            )
        conditioningEquipment.forEach { equipmentName ->
            createTestUserEquipment(webTestClient, keycloakId, equipmentName, token = token)
        }
    }

    /**
     * Creates minimal reference data for a user (just program preferences and one piece of equipment).
     * This is much faster than creating all reference data.
     */
    fun createMinimalReferenceDataForUser(
        webTestClient: WebTestClient,
        keycloakId: String,
        token: String? = null
    ) {
        createTestUserEquipment(webTestClient, keycloakId, TEST_EQUIPMENT_NAME, token = token)
    }

    /**
     * Creates all reference data for a user.
     * Use createMinimalReferenceDataForUser for faster tests when full data isn't needed.
     */
    fun createAllReferenceDataForUser(
        webTestClient: WebTestClient,
        keycloakId: String,
        token: String? = null
    ) {
        createFullUserEquipmentForConjugateTests(webTestClient, keycloakId, token = token)

        createTestUserExercisePreference(webTestClient, keycloakId, "Deadlift", token = token)
        createTestUserExercisePreference(webTestClient, keycloakId, TEST_EXERCISE_NAME_2, token = token)

        createTestUserOneRepMax(webTestClient, keycloakId, "Deadlift", token = token)
        createTestUserOneRepMax(webTestClient, keycloakId, TEST_EXERCISE_NAME_2, token = token)
        createTestUserOneRepMax(webTestClient, keycloakId, "Banded Bench Press", oneRepMax = 200.0, token = token)
        createTestUserOneRepMax(webTestClient, keycloakId, "Banded Safety Bar Squat", oneRepMax = 350.0, token = token)
    }

    /**
     * Creates a complete test program with workout, stage, and exercise.
     * Returns a map with all the created IDs for easy access.
     */
    fun createCompleteTestProgram(
        webTestClient: WebTestClient,
        keycloakId: String,
        token: String? = null
    ): Map<String, Any> {
        val programId = createTestProgram(webTestClient, keycloakId, token = token)
        val workoutId = createTestProgrammedWorkout(webTestClient, programId, token = token)
        val stageId = createTestWorkoutStage(webTestClient, workoutId, token = token)

        return mapOf(
            "keycloakId" to keycloakId,
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
        useTempo: Boolean = false,
        token: String? = null
    ): Long {
        val request =
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

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val response =
            request
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
        position: Int = 1,
        token: String? = null
    ): Long {
        val request =
            webTestClient.post()
                .uri(
                    "/api/v1/programmed_exercise/?workout_stage_id=$workoutStageId" +
                        "&exercise_name=$exerciseName" +
                        "&position=$position"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        val response =
            request
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProgrammedExercise::class.java)
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
        keycloakId: String,
        exerciseName: String,
        preferredUnit: String,
        token: String? = null
    ) {
        val encodedExerciseName = URLEncoder.encode(exerciseName, "UTF-8")
        val request =
            webTestClient.put()
                .uri(
                    "/api/v1/user_weight_unit_preference/?user_id=$keycloakId" +
                        "&exercise_name=$encodedExerciseName" +
                        "&preferred_unit=$preferredUnit"
                )

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        request.exchange()
            .expectStatus().isOk()
    }

    /**
     * Creates or updates a user's one rep max for a specific exercise, with optional unit.
     * Returns the created UserOneRepMax object.
     */
    fun putUserOneRepMax(
        webTestClient: WebTestClient,
        keycloakId: String,
        exerciseName: String,
        oneRepMax: BigDecimal,
        unit: String? = null,
        token: String? = null
    ): UserOneRepMax {
        val uri = StringBuilder("/api/v1/user_one_rep_max/?user_id=$keycloakId&exercise_name=$exerciseName&one_rep_max=$oneRepMax")
        if (unit != null) {
            uri.append("&unit=$unit")
        }
        val request =
            webTestClient.put()
                .uri(uri.toString())

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        return request.exchange()
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
        keycloakId: String,
        exerciseName: String,
        unit: String? = null,
        token: String? = null
    ): UserOneRepMax {
        val uri = StringBuilder("/api/v1/user_one_rep_max/user/$keycloakId/exercise/$exerciseName")
        if (unit != null) {
            uri.append("?unit=$unit")
        }
        val request =
            webTestClient.get()
                .uri(uri.toString())

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        return request.exchange()
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
        keycloakId: String,
        token: String? = null
    ): List<UserOneRepMax> {
        val request =
            webTestClient.get()
                .uri("/api/v1/user_one_rep_max/user/$keycloakId")

        // Add authorization header if token is provided
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        } else {
            request.header("Authorization", "Bearer ${BaseIntegrationTest.getDefaultTestToken()}")
        }

        return request.exchange()
            .expectStatus().isOk()
            .expectBodyList(UserOneRepMax::class.java)
            .returnResult()
            .responseBody ?: emptyList()
    }
}
