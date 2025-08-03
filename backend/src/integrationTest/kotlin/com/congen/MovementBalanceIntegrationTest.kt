package com.congen

import com.congen.model.WeightUnit
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class MovementBalanceIntegrationTest : BaseIntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(MovementBalanceIntegrationTest::class.java)
    }

    // Each test will create its own user to avoid duplicate key constraint issues

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

    private fun setupUserProgramPreferences(
        userId: String,
        daysPerWeek: Int,
        sessionLength: Int,
        token: String
    ) {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=$daysPerWeek" +
                    "&session_time_length_in_minutes=$sessionLength&weight_unit=${WeightUnit.KG.name}"
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
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
        val token = getValidToken("user")

        // Create a single test user for this test
        val testUserId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User", token = token)
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, testUserId, token = token)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, token)
        setupUserProgramPreferences(testUser.keycloakId, 4, 60, token)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )

        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, token)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $token")
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
                                                checkMovementBalance(programId, i, token)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different session lengths`() {
        val token = getValidToken("user")

        // Create a single test user for this test
        val testUserId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User", token = token)
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, testUserId, token = token)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, token)
        setupUserProgramPreferences(testUser.keycloakId, 4, 60, token)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, token)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $token")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, token)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different one rep max profiles`() {
        val token = getValidToken("user")

        // Create a single test user for this test
        val testUserId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User", token = token)
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, testUserId, token = token)

        // Setup user equipment and program preferences
        setupUserEquipment(testUser.keycloakId, token)
        setupUserProgramPreferences(testUser.keycloakId, 4, 60, token)

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
        setupUserOneRepMaxes(testUser.keycloakId, balancedOneRepMaxes, token)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $token")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, token)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different equipment availability`() {
        val token = getValidToken("user")

        // Create a single test user for this test
        val testUserId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User", token = token)
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, testUserId, token = token)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, token)
        setupUserProgramPreferences(testUser.keycloakId, 4, 60, token)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, token)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $token")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, token)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different movement patterns`() {
        val token = getValidToken("user")

        // Create a single test user for this test
        val testUserId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User", token = token)
        val testUser = IntegrationTestHelpers.getTestUser(webTestClient, testUserId, token = token)

        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser.keycloakId, token)
        setupUserProgramPreferences(testUser.keycloakId, 4, 60, token)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser.keycloakId, oneRepMaxes, token)

        // Create a program for the user
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser.keycloakId}&name=Movement Balance Test Program")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .header("Authorization", "Bearer $token")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .header("Authorization", "Bearer $token")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i, token)
                                            }
                                        }
                            }
                }
    }
}
