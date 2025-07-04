package com.congen

import com.congen.model.User
import com.congen.model.UserProgramPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserProgramPreferencesValidationIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun setUp() {
        // No-op, user creation will be handled in each test
    }

    @Test
    fun `should return 422 when program_days_per_week is 1`() {
        val userId = 101
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val invalidPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 1, // Invalid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidPrefs))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 1")
    }

    @Test
    fun `should return 422 when program_days_per_week is 5`() {
        val userId = 102
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val invalidPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 5, // Invalid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidPrefs))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should return 422 when program_days_per_week is 0`() {
        val userId = 103
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val invalidPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 0, // Invalid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidPrefs))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0")
    }

    @Test
    fun `should return 422 when program_days_per_week is 8`() {
        val userId = 104
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val invalidPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 8, // Invalid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(invalidPrefs))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8")
    }

    @Test
    fun `should accept valid program_days_per_week value 2`() {
        val userId = 105
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val validPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 2, // Valid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validPrefs))
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 3`() {
        val userId = 106
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val validPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 3, // Valid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validPrefs))
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 4`() {
        val userId = 107
        val user =
            User(
                id = userId,
                name = "Test User",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
            )
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(user))
            .exchange()
            .expectStatus().isOk()
        val validPrefs =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = 4, // Valid value
                sessionTimeLengthInMinutes = 60,
            )
        webTestClient.post()
            .uri("/user-program-preferences/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(validPrefs))
            .exchange()
            .expectStatus().isOk()
    }
}
