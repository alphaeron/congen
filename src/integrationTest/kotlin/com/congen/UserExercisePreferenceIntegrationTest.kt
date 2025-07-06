package com.congen

import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserExercisePreferenceIntegrationTest : BaseIntegrationTest() {
    private var userId1: Int = 0
    private var userId2: Int = 0
    private var userId3: Int = 0
    private var userId4: Int = 0
    private var userId5: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()

        // Create test users
        val user1Response =
            webTestClient.post()
                .uri("/user/?name=Test User 1&age=25&height=175.0&weight=80.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val user2Response =
            webTestClient.post()
                .uri("/user/?name=Test User 2&age=30&height=180.0&weight=85.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val user3Response =
            webTestClient.post()
                .uri("/user/?name=Test User 3&age=35&height=170.0&weight=75.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val user4Response =
            webTestClient.post()
                .uri("/user/?name=Test User 4&age=28&height=185.0&weight=90.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        val user5Response =
            webTestClient.post()
                .uri("/user/?name=Test User 5&age=32&height=165.0&weight=70.0")
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody!!

        userId1 = user1Response.id
        userId2 = user2Response.id
        userId3 = user3Response.id
        userId4 = user4Response.id
        userId5 = user5Response.id
    }

    @Test
    fun `should save user exercise preference`() {
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId1&exerciseName=Bench Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(userId1)
            .jsonPath("$.exerciseName").isEqualTo("Bench Press")
            .jsonPath("$.shouldAvoid").isEqualTo(false)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId2&exerciseName=Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk

        // Then retrieve it
        webTestClient.get()
            .uri("/user-exercise-preference/user/$userId2/exercise/Squat")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].userId").isEqualTo(userId2)
            .jsonPath("$[0].exerciseName").isEqualTo("Squat")
            .jsonPath("$[0].shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should update user exercise preference`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=3&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk

        // Then update the preference
        webTestClient.patch()
            .uri("/user-exercise-preferences/update?userId=$userId3&exerciseName=Deadlift&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.userId").isEqualTo(userId3)
            .jsonPath("$.exerciseName").isEqualTo("Deadlift")
            .jsonPath("$.shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should delete user exercise preference`() {
        // First create user exercise preference
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId4&exerciseName=Overhead Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk

        // Then delete it
        webTestClient.delete()
            .uri("/user-exercise-preference/$userId4/exercise/Overhead Press")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(userId4)
            .jsonPath("$.exerciseName").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        // Add multiple preferences for the same user
        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId5&exerciseName=Bench Press&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId5&exerciseName=Squat&shouldAvoid=true")
            .exchange()
            .expectStatus().isOk

        webTestClient.post()
            .uri("/user-exercise-preferences/?userId=$userId5&exerciseName=Deadlift&shouldAvoid=false")
            .exchange()
            .expectStatus().isOk

        // Get all preferences for the user
        webTestClient.get()
            .uri("/user-exercise-preferences/$userId5")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
