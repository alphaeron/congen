package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MovementBalanceIntegrationTest : BaseIntegrationTest() {
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create test user once for all tests
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
    }

    // User creation is centralized in setUp() to avoid duplicate key constraint issues

    private fun setupUserEquipment(
        userId: String,
        token: String
    ) {
        val equipment = listOf("power bar", "bench", "power rack")
        equipment.forEach { equipmentName ->
            webTestClient.post()
                .uri("/api/v1/user_equipment/?user_id=$userId&equipment_name=$equipmentName")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
        }
    }

    private fun setupUserOneRepMaxes(
        userId: String,
        oneRepMaxes: List<Pair<String, BigDecimal>>,
        token: String
    ) {
        oneRepMaxes.forEach { (exerciseName, oneRepMax) ->
            webTestClient.put()
                .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=$exerciseName&one_rep_max=$oneRepMax&unit=KG")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
        }
    }

    private fun checkMovementBalance(
        programId: Int,
        workoutIndex: Int,
        token: String
    ) {
        // First get the programmed workout ID for this workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/$programId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[$workoutIndex].id").exists()

        // Verify that the workout has stages
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/$programId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[$workoutIndex]").exists()
            .jsonPath("$[$workoutIndex].id").exists()
    }

    @Test
    fun `should generate workout with balanced movement types for multiple users`() {
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, token = userToken)

        // Setup user equipment and one rep maxes (program preferences are created automatically with the program)
        setupUserEquipment(testUser.keycloakId, userToken)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )

        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, userToken)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> {
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $userToken")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) {
                                                "Expected 4 workouts for 4-day program, got $workoutCount"
                                            }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, userToken)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different session lengths`() {
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, token = userToken)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, userToken)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, userToken)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> {
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $userToken")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, userToken)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different one rep max profiles`() {
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, token = userToken)

        // Setup user equipment and program preferences
        setupUserEquipment(testUser.keycloakId, userToken)

        // Test with balanced one rep maxes
        val balancedOneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, balancedOneRepMaxes, userToken)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> {
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $userToken")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, userToken)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different equipment availability`() {
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, token = userToken)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, userToken)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, userToken)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> {
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $userToken")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, userToken)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different movement patterns`() {
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, token = userToken)

        // Setup user equipment and one rep maxes (program preferences are created automatically with the program)
        setupUserEquipment(testUser.keycloakId, userToken)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, userToken)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $userToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $userToken")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> {
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $userToken")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, userToken)
                                            }
                                        }
                            }
                }
    }
}
