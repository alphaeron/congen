package com.congen

import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class ConjugateWorkoutGeneratorIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    @Test
    fun `should generate 3-day conjugate workout program successfully`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences for 3 days per week
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
        assert(programResponse.name?.contains("Week 1") == true)
        assert(programResponse.description?.contains("3 days per week") == true)

        // Verify workouts were created
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3) // 3 workouts for 3-day program
    }

    @Test
    fun `should generate 2-day conjugate workout program successfully`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 2&age=30&height=180.0&weight=85.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences for 2 days per week
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 2,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
        assert(programResponse.name?.contains("Week 1") == true)
        assert(programResponse.description?.contains("2 days per week") == true)

        // Verify workouts were created
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2) // 2 workouts for 2-day program
    }

    @Test
    fun `should generate 4-day conjugate workout program successfully`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 3&age=28&height=170.0&weight=75.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences for 4 days per week
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 4,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
        assert(programResponse.name?.contains("Week 1") == true)
        assert(programResponse.description?.contains("4 days per week") == true)

        // Verify workouts were created
        webTestClient.get()
            .uri("/programmed_workout/program/${programResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(4) // 4 workouts for 4-day program
    }

    @Test
    fun `should handle invalid programDaysPerWeek in database`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 4&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences with invalid days per week
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 5,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When & Then - Try to generate with invalid days per week
        webTestClient.post()
            .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `should handle invalid currentWeekNumber parameter`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 5&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When & Then - Try to generate with invalid week number
        webTestClient.post()
            .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=0")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `should handle non-existent user`() {
        // When & Then - Try to generate for non-existent user
        webTestClient.post()
            .uri("/conjugate_workout_generator/99999/generate?currentWeekNumber=1")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should generate workout with user exercise preferences`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 6&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // Add exercise preference
        webTestClient.post()
            .uri("/user_exercise_preference/")
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "exercise_name": "Safety Bar Squat",
                    "should_avoid": true
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user_exercise_preference/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "exercise_name": "Safety Bar Squat",
                    "should_avoid": false
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
    }

    @Test
    fun `should generate workout with user equipment`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 7&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // Add user equipment
        webTestClient.post()
            .uri("/user-equipment/?userId=$userId&equipmentName=Barbell")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-equipment/?userId=$userId&equipmentName=Dumbbells")
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
    }

    @Test
    fun `should generate workout with user one rep max data`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 8&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Create user program preferences
        webTestClient.post()
            .uri("/user_program_preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // Add one rep max data
        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "exercise_name": "Bench Press",
                    "one_rep_max": 100.0
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-one-rep-max/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "exercise_name": "Squat",
                    "one_rep_max": 150.0
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
    }

    @Test
    fun `should generate workout with user program preferences`() {
        // Given - Create a user first
        val userResponse =
            webTestClient.post()
                .uri("/user/?name=Test User 9&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val userId = userResponse.id!!

        // Add program preferences
        webTestClient.post()
            .uri("/user_program_preferences/")
            .bodyValue(
                """
                {
                    "user_id": $userId,
                    "program_days_per_week": 3,
                    "session_time_length_in_minutes": 60
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk()

        // When - Generate workout program
        val programResponse =
            webTestClient.post()
                .uri("/conjugate_workout_generator/$userId/generate?currentWeekNumber=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Conjugate Program")
                .jsonPath("$.description").isEqualTo("Generated conjugate powerlifting program")
                .returnResult()
                .responseBody

        // Then - Verify program was created
        assert(programResponse.id != null)
        assert(programResponse.userId == userId)
    }
}
