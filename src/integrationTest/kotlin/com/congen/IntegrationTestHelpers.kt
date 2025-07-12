package com.congen

import com.congen.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

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
    const val TEST_MUSCLE_DESCRIPTION = "The primary functions are flexion, adduction, and internal rotation of the humerus. The pectoral major may colloquially be referred to as \"pecs\", \"pectoral muscle\", or \"chest muscle\", because it is the largest and most superficial muscle in the chest area."
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
        weight: Double = TEST_USER_WEIGHT
    ): Int {
        val response = webTestClient.post()
            .uri("/user/?name=$name&age=$age&height=$height&weight=$weight")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        return response.id
    }
    
    /**
     * Returns the equipment name for testing.
     * Equipment already exists in migrations, so we don't need to create it.
     */
    fun getTestEquipment(
        webTestClient: WebTestClient,
        name: String = TEST_EQUIPMENT_NAME
    ): String {
        return name
    }
    
    /**
     * Returns the muscle name for testing.
     * Muscles already exist in migrations, so we don't need to create them.
     */
    fun getTestMuscle(
        webTestClient: WebTestClient,
        name: String = TEST_MUSCLE_NAME
    ): String {
        return name
    }
    
    /**
     * Returns the exercise name for testing.
     * Exercises already exist in migrations, so we don't need to create them.
     */
    fun getTestExercise(
        webTestClient: WebTestClient,
        name: String = TEST_EXERCISE_NAME
    ): String {
        return name
    }
    
    /**
     * Creates test program via the API and returns the program ID.
     */
    fun createTestProgram(
        webTestClient: WebTestClient,
        userId: Int,
        name: String = TEST_PROGRAM_NAME,
        currentWeekNumber: Int = 1
    ): Long {
        val response = webTestClient.post()
            .uri("/program/?userId=$userId&name=$name&currentWeekNumber=$currentWeekNumber")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Program::class.java)
            .returnResult()
            .responseBody!!
        return response.id
    }
    
    /**
     * Creates test programmed workout via the API and returns the workout ID.
     */
    fun createTestProgrammedWorkout(
        webTestClient: WebTestClient,
        programId: Long,
        dayNumber: Int = 1,
        name: String = TEST_WORKOUT_NAME
    ): Long {
        val response = webTestClient.post()
            .uri("/programmed_workout/?programId=$programId&dayNumber=$dayNumber&name=$name")
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
        position: Int = 1,
        name: String = TEST_STAGE_NAME
    ): Long {
        val response = webTestClient.post()
            .uri("/workout_stage/?programmedWorkoutId=$programmedWorkoutId&stageTypeId=$stageTypeId&position=$position&name=$name")
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
            .uri("/user_equipment/?userId=$userId&equipmentName=$equipmentName")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test user exercise preference via the API.
     * Exercises need to exist first, so we create them if needed.
     */
    fun createTestUserExercisePreference(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String = TEST_EXERCISE_NAME,
        shouldAvoid: Boolean = false
    ) {
        // Now create the preference
        val encodedExerciseName = java.net.URLEncoder.encode(exerciseName, "UTF-8")
        webTestClient.post()
            .uri("/user_exercise_preference/?userId=$userId&exerciseName=$encodedExerciseName&shouldAvoid=$shouldAvoid")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test user one rep max via the API.
     * Exercises need to exist first, so we create them if needed.
     */
    fun createTestUserOneRepMax(
        webTestClient: WebTestClient,
        userId: Int,
        exerciseName: String = TEST_EXERCISE_NAME,
        oneRepMax: Double = 100.0
    ) {
        // Now create the one rep max - use raw exercise name for query parameters
        val bigDecimalValue = java.math.BigDecimal(oneRepMax)
        webTestClient.post()
            .uri("/user_one_rep_max/?userId=$userId&exerciseName=$exerciseName&oneRepMax=$bigDecimalValue")
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
            .uri("/user_program_preferences/?userId=$userId&programDaysPerWeek=$programDaysPerWeek&sessionTimeLengthInMinutes=$sessionTimeLengthInMinutes")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates a test user and returns the user ID.
     */
    fun createTestUserWithId(webTestClient: WebTestClient, name: String = TEST_USER_NAME): Int {
        val response = webTestClient.post()
            .uri("/user/?name=$name&age=$TEST_USER_AGE&height=$TEST_USER_HEIGHT&weight=$TEST_USER_WEIGHT")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        return response.id
    }
    
    /**
     * Creates a test user and program preferences, then generates a conjugate program.
     */
    fun createTestUserWithConjugateProgram(
        webTestClient: WebTestClient, 
        userName: String = "Test User",
        programDaysPerWeek: Int = 3
    ): Pair<Int, Program> {
        val userId = createTestUserWithId(webTestClient, userName)
        createTestUserProgramPreferences(webTestClient, userId, programDaysPerWeek)
        
        val programResponse = webTestClient.post()
            .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Program::class.java)
            .returnResult()
            .responseBody!!
        
        return Pair(userId, programResponse)
    }
    
    /**
     * Creates test user program preferences via the API.
     */
    fun createTestUserProgramPreferences(webTestClient: WebTestClient, userId: Int) {
        webTestClient.post()
            .uri("/user_program_preferences/?userId=$userId&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test exercise equipment relationship via the API.
     */
    fun createTestExerciseEquipment(
        webTestClient: WebTestClient,
        exerciseName: String = TEST_EXERCISE_NAME,
        equipmentName: String = TEST_EQUIPMENT_NAME
    ) {
        webTestClient.post()
            .uri("/exercise_equipment/?exerciseName=$exerciseName&equipmentName=$equipmentName")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test exercise muscle relationship via the API.
     */
    fun createTestExerciseMuscle(
        webTestClient: WebTestClient,
        exerciseName: String = TEST_EXERCISE_NAME,
        muscleName: String = TEST_MUSCLE_NAME,
        isPrimary: Boolean = true
    ) {
        webTestClient.post()
            .uri("/exercise_muscle/?exerciseName=$exerciseName&muscleName=$muscleName&isPrimary=$isPrimary")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test exercise workout type relationship via the API.
     */
    fun createTestExerciseWorkoutType(
        webTestClient: WebTestClient,
        exerciseName: String = TEST_EXERCISE_NAME,
        movementType: String = "horizontal push",
        workoutType: String = "dynamic_effort"
    ) {
        webTestClient.post()
            .uri("/exercise_workout_type/?exerciseName=$exerciseName&movementType=$movementType&workoutType=$workoutType")
            .exchange()
            .expectStatus().isOk()
    }
    
    /**
     * Creates test programmed exercise via the API and returns the programmed exercise ID.
     */
    fun createTestProgrammedExercise(
        webTestClient: WebTestClient,
        workoutStageId: Long,
        exerciseName: String = TEST_EXERCISE_NAME,
        position: Int = 1,
        notes: String? = null
    ): Long {
        val uri = StringBuilder("/programmed_exercise/?workoutStageId=$workoutStageId&exerciseName=$exerciseName&position=$position")
        if (notes != null) uri.append("&notes=$notes")
        val response = webTestClient.post()
            .uri(uri.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProgrammedExercise::class.java)
            .returnResult()
            .responseBody!!
        return response.id
    }
    
    /**
     * Creates test exercise rotation history via the API.
     */
    fun createTestExerciseRotationHistory(webTestClient: WebTestClient, userId: Int, exerciseName: String = TEST_EXERCISE_NAME, isAccessory: Boolean = false): Long {
        val response = webTestClient.post()
            .uri("/exercise_rotation_history/?userId=$userId&exerciseName=$exerciseName&isAccessory=$isAccessory")
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
    fun createTestWorkoutStageType(webTestClient: WebTestClient, name: WorkoutStageTypeEnum = WorkoutStageTypeEnum.WARMUP): String {
        // Workout stage types are pre-populated in the database by BaseIntegrationTest
        return name.displayName
    }

    /**
     * Returns a set of primary and accessory exercises for testing.
     * Exercises already exist in migrations, so we don't need to create them.
     */
    fun getPrimaryAndAccessoryExercises(webTestClient: WebTestClient) {
        // Primary exercises already exist in migrations: "Deadlift", "Safety Bar Squat", "Bent-Over Row", etc.
        // Accessory exercises already exist in migrations: "Chin-Up", "Ab Wheel", "GHR", etc.
        // No need to create anything
    }

    /**
     * Returns all required equipment for testing.
     * Equipment already exists in migrations, so we don't need to create it.
     */
    fun getAllEquipment(webTestClient: WebTestClient) {
        // Equipment already exists in migrations: "power bar", "dumbbells", etc.
        // No need to create anything
    }

    /**
     * Creates all reference data for a user: equipment, exercises, preferences, one rep maxes, etc.
     *
     * Note: Only adds each equipment once per user. If a test adds 'power bar' or 'bench' separately, do not add it again here.
     */
    fun createAllReferenceDataForUser(webTestClient: WebTestClient, userId: Int, programDaysPerWeek: Int = 3) {
        // Equipment and exercises already exist in migrations
        createTestUserProgramPreferences(webTestClient, userId, programDaysPerWeek)
        // Only add each equipment once per user
        if (TEST_EQUIPMENT_NAME != TEST_EQUIPMENT_NAME_2) {
            createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME)
            createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME_2)
        } else {
            createTestUserEquipment(webTestClient, userId, TEST_EQUIPMENT_NAME)
        }
        createTestUserExercisePreference(webTestClient, userId, "Deadlift", false)
        createTestUserExercisePreference(webTestClient, userId, "Safety Bar Squat", false)
        createTestUserOneRepMax(webTestClient, userId, "Deadlift")
        createTestUserOneRepMax(webTestClient, userId, "Safety Bar Squat")
    }
} 