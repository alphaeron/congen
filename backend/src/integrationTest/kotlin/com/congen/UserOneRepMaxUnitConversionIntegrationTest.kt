package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal

/**
 * Integration tests for UserOneRepMax unit conversion functionality.
 *
 * Tests the weight unit conversion features in the one rep max endpoints,
 * including input conversion to kg and output conversion to user's preferred units.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserOneRepMaxUnitConversionIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
    }

    @Test
    fun `should store weight in kg when input is in lbs`() {
        val exerciseName = "Bench Press"
        val weightInLbs = BigDecimal("225.0")
        val expectedWeightInKg = BigDecimal("102.06") // 225 lbs * 0.453592 = 102.06 kg

        val oneRepMax =
            IntegrationTestHelpers.putUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                weightInLbs,
                unit = "LBS",
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(expectedWeightInKg) == 0)
    }

    @Test
    fun `should store weight in kg when input is in kg`() {
        val exerciseName = "Deadlift"
        val weightInKg = BigDecimal("150.0")

        val oneRepMax =
            IntegrationTestHelpers.putUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                weightInKg,
                unit = "KG",
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(weightInKg) == 0)
    }

    @Test
    fun `should use user preference when no unit specified`() {
        val exerciseName = "Safety Bar Squat"
        val weightInLbs = BigDecimal("315.0")
        val expectedWeightInKg = BigDecimal("142.88") // 315 lbs * 0.453592 = 142.88 kg

        IntegrationTestHelpers.setUserWeightUnitPreference(
            webTestClient,
            userId,
            exerciseName,
            "LBS",
            token = userToken
        )
        val oneRepMax =
            IntegrationTestHelpers.putUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                weightInLbs,
                unit = "LBS",
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(expectedWeightInKg) == 0)
    }

    @Test
    fun `should default to kg when no unit specified and no preference exists`() {
        val exerciseName = "Bent-Over Row"
        val weightInKg = BigDecimal("100.0")

        val oneRepMax =
            IntegrationTestHelpers.putUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                weightInKg,
                unit = "KG",
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(weightInKg) == 0)
    }

    @Test
    fun `should return weight in user's preferred unit when retrieving`() {
        val exerciseName = "Deadlift"
        val weightInLbs = BigDecimal("225.0")
        val expectedWeightInLbs = BigDecimal("225.00")

        IntegrationTestHelpers.setUserWeightUnitPreference(
            webTestClient,
            userId,
            exerciseName,
            "LBS",
            token = userToken
        )
        IntegrationTestHelpers.putUserOneRepMax(
            webTestClient,
            userId,
            exerciseName,
            weightInLbs,
            unit = "LBS",
            token = userToken
        )
        // Small delay to ensure database transaction is committed
        Thread.sleep(100)
        val oneRepMax =
            IntegrationTestHelpers.getUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(expectedWeightInLbs) == 0)
    }

    @Test
    fun `should return weight in specified unit when retrieving with unit parameter`() {
        val exerciseName = "Deadlift"
        val weightInKg = BigDecimal("150.0")
        val expectedWeightInLbs = BigDecimal("330.69") // 150 kg * 2.20462 = 330.69 lbs

        IntegrationTestHelpers.putUserOneRepMax(
            webTestClient,
            userId,
            exerciseName,
            weightInKg,
            unit = "KG",
            token = userToken
        )
        // Small delay to ensure database transaction is committed
        Thread.sleep(100)
        val oneRepMax =
            IntegrationTestHelpers.getUserOneRepMax(
                webTestClient,
                userId,
                exerciseName,
                unit = "LBS",
                token = userToken
            )
        assert(oneRepMax.userId == userId)
        assert(oneRepMax.exerciseName == exerciseName)
        assert(oneRepMax.oneRepMax.compareTo(expectedWeightInLbs) == 0)
    }

    @Test
    fun `should return 400 for invalid unit parameter`() {
        val exerciseName = "Bench Press"
        val weight = BigDecimal("225.0")
        val invalidUnit = "INVALID"

        val uri = "/api/v1/user_one_rep_max/?user_id=$userId&exercise_name=$exerciseName&one_rep_max=$weight&unit=$invalidUnit"
        webTestClient.put()
            .uri(uri)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("Invalid weight unit"))
            }
    }

    @Test
    fun `should return all one rep maxes in user's preferred units`() {
        val exercise1 = "Bench Press"
        val exercise2 = "Deadlift"
        val weight1 = BigDecimal("225.0") // lbs
        val weight2 = BigDecimal("150.0") // kg

        IntegrationTestHelpers.setUserWeightUnitPreference(
            webTestClient,
            userId,
            exercise1,
            "LBS",
            token = userToken
        )
        IntegrationTestHelpers.setUserWeightUnitPreference(
            webTestClient,
            userId,
            exercise2,
            "KG",
            token = userToken
        )
        IntegrationTestHelpers.putUserOneRepMax(
            webTestClient,
            userId,
            exercise1,
            weight1,
            unit = "LBS",
            token = userToken
        )
        IntegrationTestHelpers.putUserOneRepMax(
            webTestClient,
            userId,
            exercise2,
            weight2,
            unit = "KG",
            token = userToken
        )
        val oneRepMaxes =
            IntegrationTestHelpers.getAllUserOneRepMaxes(
                webTestClient,
                userId,
                token = userToken
            )
        assert(oneRepMaxes.size == 2)
        val benchPress = oneRepMaxes.find { it.exerciseName == exercise1 }
        val deadlift = oneRepMaxes.find { it.exerciseName == exercise2 }
        assert(benchPress != null)
        assert(deadlift != null)
        assert(benchPress!!.oneRepMax.compareTo(weight1) == 0)
        assert(deadlift!!.oneRepMax.compareTo(weight2) == 0)
    }
}
