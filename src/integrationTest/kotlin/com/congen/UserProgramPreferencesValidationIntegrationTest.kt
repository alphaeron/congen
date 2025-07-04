package com.congen

import com.congen.model.User
import com.congen.model.UserProgramPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.math.BigDecimal

class UserProgramPreferencesValidationIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // No-op, user creation will be handled in each test
    }

    @Test
    fun `should return 422 when program_days_per_week is 1`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=1&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 1")
    }

    @Test
    fun `should return 422 when program_days_per_week is 5`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=5&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5")
    }

    @Test
    fun `should return 422 when program_days_per_week is 0`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=0&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0")
    }

    @Test
    fun `should return 422 when program_days_per_week is 8`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=8&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath(
                "$.error",
            ).isEqualTo("Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8")
    }

    @Test
    fun `should accept valid program_days_per_week value 2`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=2&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 3`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=3&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should accept valid program_days_per_week value 4`() {
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test%20User&age=30&height=180.5&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!
        
        webTestClient.post()
            .uri("/user-program-preferences/?userId=${userResponse.id}&programDaysPerWeek=4&sessionTimeLengthInMinutes=60")
            .exchange()
            .expectStatus().isOk()
    }
}
