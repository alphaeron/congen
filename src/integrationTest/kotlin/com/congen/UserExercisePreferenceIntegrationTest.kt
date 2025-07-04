package com.congen

import com.congen.model.UserExercisePreference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserExercisePreferenceIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should save user exercise preference`() {
        val userExercisePreference =
            UserExercisePreference(
                userId = 1,
                exerciseName = "Bench Press",
                shouldAvoid = false,
            )

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userExercisePreference))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(1)
            .jsonPath("$.exerciseName").isEqualTo("Bench Press")
            .jsonPath("$.shouldAvoid").isEqualTo(false)
    }

    @Test
    fun `should get user exercise preferences by user id`() {
        // First create user exercise preference
        val userExercisePreference =
            UserExercisePreference(
                userId = 2,
                exerciseName = "Squat",
                shouldAvoid = true,
            )

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userExercisePreference))
            .exchange()
            .expectStatus().isOk()

        // Then get preferences for the user
        webTestClient.get()
            .uri("/user-exercise-preferences/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].userId").isEqualTo(2)
            .jsonPath("$[0].exerciseName").isEqualTo("Squat")
            .jsonPath("$[0].shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should update user exercise preference`() {
        // First create user exercise preference
        val userExercisePreference =
            UserExercisePreference(
                userId = 3,
                exerciseName = "Deadlift",
                shouldAvoid = false,
            )

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userExercisePreference))
            .exchange()
            .expectStatus().isOk()

        // Then update the preference
        val updatedPreference =
            UserExercisePreference(
                userId = 3,
                exerciseName = "Deadlift",
                shouldAvoid = true,
            )

        webTestClient.post()
            .uri("/user-exercise-preferences/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(updatedPreference))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(3)
            .jsonPath("$.exerciseName").isEqualTo("Deadlift")
            .jsonPath("$.shouldAvoid").isEqualTo(true)
    }

    @Test
    fun `should delete user exercise preference`() {
        // First create user exercise preference
        val userExercisePreference =
            UserExercisePreference(
                userId = 4,
                exerciseName = "Overhead Press",
                shouldAvoid = false,
            )

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userExercisePreference))
            .exchange()
            .expectStatus().isOk()

        // Then delete the preference
        webTestClient.delete()
            .uri("/user-exercise-preferences/4/Overhead Press")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(4)
            .jsonPath("$.exerciseName").isEqualTo("Overhead Press")
    }

    @Test
    fun `should handle multiple exercise preferences for same user`() {
        val preference1 = UserExercisePreference(userId = 5, exerciseName = "Bench Press", shouldAvoid = false)
        val preference2 = UserExercisePreference(userId = 5, exerciseName = "Squat", shouldAvoid = true)
        val preference3 = UserExercisePreference(userId = 5, exerciseName = "Deadlift", shouldAvoid = false)

        // Add multiple preferences for the same user
        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(preference1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(preference2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-exercise-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(preference3))
            .exchange()
            .expectStatus().isOk()

        // Get all preferences for the user
        webTestClient.get()
            .uri("/user-exercise-preferences/5")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
