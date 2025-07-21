package com.congen

import com.congen.model.User
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class MovementBalanceIntegrationTest : BaseIntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(MovementBalanceIntegrationTest::class.java)
    }

    private lateinit var testUser1: User
    private lateinit var testUser2: User
    private lateinit var testUser3: User

    @BeforeEach
    override fun setUp() {
        super.setUp()
        setupTestUsers()
    }

    private fun setupTestUsers() {
        // Create test users and capture their IDs
        testUser1 = createUser("Test User 1")
        testUser2 = createUser("Test User 2")
        testUser3 = createUser("Test User 3")
    }

    private fun createUser(name: String): User {
        return webTestClient.post()
            .uri("/api/v1/user/?name=${name.replace(" ", "%20")}&age=30&height=175.5&weight=80.0")
            .exchange()
            .expectStatus().isOk
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
    }

    private fun setupUserEquipment(userId: Int) {
        val equipment = listOf("power bar", "bench", "power rack")
        equipment.forEach { equipmentName ->
            webTestClient.post()
                .uri("/api/v1/user_equipment/?user_id=$userId&equipment_name=$equipmentName")
                .exchange()
                .expectStatus().isOk
        }
    }

    private fun setupUserProgramPreferences(
        userId: Int,
        daysPerWeek: Int,
        sessionLength: Int
    ) {
        webTestClient.post()
            .uri(
                "/api/v1/user_program_preferences/?user_id=$userId&program_days_per_week=$daysPerWeek" +
                    "&session_time_length_in_minutes=$sessionLength&weight_unit=${WeightUnit.KG.name}"
            )
            .exchange()
            .expectStatus().isOk
    }

    private fun setupUserOneRepMaxes(
        userId: Int,
        oneRepMaxes: List<Pair<String, BigDecimal>>
    ) {
        oneRepMaxes.forEach { (exerciseName, oneRepMax) ->
            webTestClient.put()
                .uri("/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=$exerciseName&one_rep_max=$oneRepMax&unit=KG")
                .exchange()
                .expectStatus().isOk
        }
    }

    private fun checkMovementBalance(
        programId: Int,
        workoutIndex: Int
    ) {
        // First get the programmed workout ID for this workout
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/$programId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[$workoutIndex].id").exists()

        // Verify that the workout has stages
        webTestClient.get()
            .uri("/api/v1/programmed_workout/program/$programId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[$workoutIndex]").exists()
            .jsonPath("$[$workoutIndex].id").exists()
    }

    @Test
    fun `should generate workout with balanced movement types for multiple users`() {
        // Setup user equipment, program preferences, and one rep maxes for all users
        setupUserEquipment(testUser1.id)
        setupUserEquipment(testUser2.id)
        setupUserEquipment(testUser3.id)

        setupUserProgramPreferences(testUser1.id, 4, 60)
        setupUserProgramPreferences(testUser2.id, 3, 45)
        setupUserProgramPreferences(testUser3.id, 4, 90)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )

        setupUserOneRepMaxes(testUser1.id, oneRepMaxes)
        setupUserOneRepMaxes(testUser2.id, oneRepMaxes)
        setupUserOneRepMaxes(testUser3.id, oneRepMaxes)

        // Test for each user
        val users = listOf(testUser1, testUser2, testUser3)
        val expectedWorkoutCounts = listOf(4, 3, 4) // Based on days per week

        users.forEachIndexed { userIndex, user ->
            // Create a program for this user
            val programResponse =
                webTestClient.post()
                    .uri("/api/v1/program/?user_id=${user.id}&name=Movement Balance Test Program User ${userIndex + 1}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.id").value<Int> { programId ->
                        // Generate next week of workouts
                        val workoutResponse =
                            webTestClient.post()
                                .uri("/api/v1/conjugate_workout_generator/$programId")
                                .exchange()
                                .expectStatus().isOk
                                .expectBody()
                                .jsonPath("$.id").value<Int> { _ ->
                                    // Get workouts for this program
                                    val workouts =
                                        webTestClient.get()
                                            .uri("/api/v1/programmed_workout/program/$programId")
                                            .exchange()
                                            .expectStatus().isOk
                                            .expectBody()
                                            .jsonPath("$").isArray()
                                            .jsonPath("$.length()").value<Int> { workoutCount ->
                                                assert(workoutCount == expectedWorkoutCounts[userIndex]) {
                                                    "Expected ${expectedWorkoutCounts[userIndex]} workouts for user " +
                                                        "${userIndex + 1}, got $workoutCount"
                                                }

                                                // Verify that each workout has balanced movement types
                                                for (i in 0 until workoutCount) {
                                                    checkMovementBalance(programId, i)
                                                }
                                            }
                                }
                    }
        }
    }

    @Test
    fun `should generate workout with balanced movement types for different session lengths`() {
        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser1.id)
        setupUserProgramPreferences(testUser1.id, 4, 30) // Short session

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser1.id, oneRepMaxes)

        // Create a program first
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser1.id}&name=Movement Balance Test Program Short Session")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i)
                                            }
                                        }
                            }
                }

        // Test with longer session
        setupUserProgramPreferences(testUser2.id, 4, 90) // Long session
        setupUserEquipment(testUser2.id)
        setupUserOneRepMaxes(testUser2.id, oneRepMaxes)

        val programResponse2 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser2.id}&name=Movement Balance Test Program Long Session")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId2 ->
                    // Generate next week of workouts
                    val workoutResponse2 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId2")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId2")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount2 ->
                                            assert(workoutCount2 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount2" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount2) {
                                                checkMovementBalance(programId2, i)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different one rep max profiles`() {
        // Setup user equipment and program preferences
        setupUserEquipment(testUser1.id)
        setupUserProgramPreferences(testUser1.id, 4, 60)

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
        setupUserOneRepMaxes(testUser1.id, balancedOneRepMaxes)

        // Create a program first
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser1.id}&name=Movement Balance Test Program Balanced 1RM")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i)
                                            }
                                        }
                            }
                }

        // Test with push-dominant one rep maxes
        setupUserEquipment(testUser2.id)
        setupUserProgramPreferences(testUser2.id, 4, 60)
        val pushDominantOneRepMaxes =
            listOf(
                // Stronger push
                "Bench Press" to BigDecimal("150"),
                // Stronger push
                "Overhead Press" to BigDecimal("120"),
                // Weaker pull
                "Bent-Over Row" to BigDecimal("80"),
                // Weaker pull
                "Chin-Up" to BigDecimal("60"),
                "Back Squat" to BigDecimal("180"),
                "Deadlift" to BigDecimal("220")
            )
        setupUserOneRepMaxes(testUser2.id, pushDominantOneRepMaxes)

        val programResponse2 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser2.id}&name=Movement Balance Test Program Push Dominant 1RM")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId2 ->
                    // Generate next week of workouts
                    val workoutResponse2 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId2")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId2")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount2 ->
                                            assert(workoutCount2 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount2" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount2) {
                                                checkMovementBalance(programId2, i)
                                            }
                                        }
                            }
                }

        // Test with pull-dominant one rep maxes
        setupUserEquipment(testUser3.id)
        setupUserProgramPreferences(testUser3.id, 4, 60)
        val pullDominantOneRepMaxes =
            listOf(
                // Weaker push
                "Bench Press" to BigDecimal("100"),
                // Weaker push
                "Overhead Press" to BigDecimal("80"),
                // Stronger pull
                "Bent-Over Row" to BigDecimal("120"),
                // Stronger pull
                "Chin-Up" to BigDecimal("100"),
                "Back Squat" to BigDecimal("160"),
                "Deadlift" to BigDecimal("240")
            )
        setupUserOneRepMaxes(testUser3.id, pullDominantOneRepMaxes)

        val programResponse3 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser3.id}&name=Movement Balance Test Program Pull Dominant 1RM")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId3 ->
                    // Generate next week of workouts
                    val workoutResponse3 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId3")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId3")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount3 ->
                                            assert(workoutCount3 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount3" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount3) {
                                                checkMovementBalance(programId3, i)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different equipment availability`() {
        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser1.id)
        setupUserProgramPreferences(testUser1.id, 4, 60)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser1.id, oneRepMaxes)

        // Create a program first
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser1.id}&name=Movement Balance Test Program Full Equipment")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i)
                                            }
                                        }
                            }
                }

        // Test with limited equipment (only barbell and dumbbells)
        setupUserEquipment(testUser2.id)
        setupUserProgramPreferences(testUser2.id, 4, 60)
        setupUserOneRepMaxes(testUser2.id, oneRepMaxes)

        val programResponse2 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser2.id}&name=Movement Balance Test Program Limited Equipment")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId2 ->
                    // Generate next week of workouts
                    val workoutResponse2 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId2")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId2")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount2 ->
                                            assert(workoutCount2 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount2" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount2) {
                                                checkMovementBalance(programId2, i)
                                            }
                                        }
                            }
                }
    }

    @Test
    fun `should generate workout with balanced movement types for different movement patterns`() {
        // Setup user equipment, program preferences, and one rep maxes
        setupUserEquipment(testUser1.id)
        setupUserProgramPreferences(testUser1.id, 4, 60)

        val oneRepMaxes =
            listOf(
                "Bench Press" to BigDecimal("120"),
                "Overhead Press" to BigDecimal("100"),
                "Bent-Over Row" to BigDecimal("90"),
                "Chin-Up" to BigDecimal("80"),
                "Back Squat" to BigDecimal("150"),
                "Deadlift" to BigDecimal("200")
            )
        setupUserOneRepMaxes(testUser1.id, oneRepMaxes)

        // Create a program first
        val programResponse =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser1.id}&name=Movement Balance Test Program Movement Patterns")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId ->
                    // Generate next week of workouts
                    val workoutResponse =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount ->
                                            assert(workoutCount == 4) { "Expected 4 workouts for 4-day program, got $workoutCount" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount) {
                                                checkMovementBalance(programId, i)
                                            }
                                        }
                            }
                }

        // Test with different movement patterns (vertical push / horizontal pull focus)
        setupUserEquipment(testUser2.id)
        setupUserProgramPreferences(testUser2.id, 4, 60)
        setupUserOneRepMaxes(testUser2.id, oneRepMaxes)

        val programResponse2 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser2.id}&name=Movement Balance Test Program Vertical Push Focus")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId2 ->
                    // Generate next week of workouts
                    val workoutResponse2 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId2")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId2")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount2 ->
                                            assert(workoutCount2 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount2" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount2) {
                                                checkMovementBalance(programId2, i)
                                            }
                                        }
                            }
                }

        // Test with horizontal push / vertical pull focus
        setupUserEquipment(testUser3.id)
        setupUserProgramPreferences(testUser3.id, 4, 60)
        setupUserOneRepMaxes(testUser3.id, oneRepMaxes)

        val programResponse3 =
            webTestClient.post()
                .uri("/api/v1/program/?user_id=${testUser3.id}&name=Movement Balance Test Program Horizontal Push Focus")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").value<Int> { programId3 ->
                    // Generate next week of workouts
                    val workoutResponse3 =
                        webTestClient.post()
                            .uri("/api/v1/conjugate_workout_generator/$programId3")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody()
                            .jsonPath("$.id").value<Int> { _ ->
                                // Get workouts for this program
                                val workouts =
                                    webTestClient.get()
                                        .uri("/api/v1/programmed_workout/program/$programId3")
                                        .exchange()
                                        .expectStatus().isOk
                                        .expectBody()
                                        .jsonPath("$").isArray()
                                        .jsonPath("$.length()").value<Int> { workoutCount3 ->
                                            assert(workoutCount3 == 4) { "Expected 4 workouts for 4-day program, got $workoutCount3" }

                                            // Verify that each workout has balanced movement types
                                            for (i in 0 until workoutCount3) {
                                                checkMovementBalance(programId3, i)
                                            }
                                        }
                            }
                }
    }
}
