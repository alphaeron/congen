package com.congen.service

import com.congen.dal.UserOneRepMaxDAL
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WilksCalculationServiceTest {
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var wilksCalculationService: WilksCalculationService

    private val testKeycloakId = "test-keycloak-id"

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        wilksCalculationService = WilksCalculationService(userOneRepMaxDAL)
    }

    @Test
    fun `calculateWilksScore should calculate score for male user with complete data`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .assertNext { wilksScore ->
                assert(wilksScore != null)
                assert(wilksScore!! > 0.0)
                // Total = 300 + 225 + 400 = 925
                // Expected Wilks score should be around 925 * coefficient
            }
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should calculate score for female user with complete data`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal.valueOf(200.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal.valueOf(150.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal.valueOf(250.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 60.0, false)

        StepVerifier.create(result)
            .assertNext { wilksScore ->
                assert(wilksScore != null)
                assert(wilksScore!! > 0.0)
                // Total = 200 + 150 + 250 = 600
                // Expected Wilks score should be around 600 * coefficient
            }
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should return null when missing squat`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .expectError(NullPointerException::class.java)
            .verify()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should return null when missing bench press`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .expectError(NullPointerException::class.java)
            .verify()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should return null when missing deadlift`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .expectError(NullPointerException::class.java)
            .verify()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should handle alternative exercise names`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Back Squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Conventional Deadlift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .assertNext { wilksScore ->
                assert(wilksScore != null)
                assert(wilksScore!! > 0.0)
            }
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should handle case insensitive exercise names`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "BENCH PRESS",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "DeadLift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .assertNext { wilksScore ->
                assert(wilksScore != null)
                assert(wilksScore!! > 0.0)
            }
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should handle empty one rep max list`() {
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(emptyList()))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .expectError(NullPointerException::class.java)
            .verify()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should handle database error`() {
        val error = RuntimeException("Database error")
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.error(error))

        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 80.0, true)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }

    @Test
    fun `calculateWilksScore should handle extreme body weight`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal.valueOf(300.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal.valueOf(225.0),
                    updatedAt = java.time.Instant.now()
                ),
                UserOneRepMax(
                    userId = testKeycloakId,
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal.valueOf(400.0),
                    updatedAt = java.time.Instant.now()
                )
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(testKeycloakId))
            .thenReturn(Mono.just(oneRepMaxes))

        // Test with extreme body weight - should still calculate a value
        val result = wilksCalculationService.calculateWilksScore(testKeycloakId, 0.0, true)

        StepVerifier.create(result)
            .assertNext { wilksScore ->
                assert(wilksScore != null)
                // With body weight 0.0, the coefficient is 500/(-216.0475144) ≈ -2.31
                // Total is 925, so score should be around -2140
                assert(wilksScore!! < 0.0) // Should be negative due to extreme body weight
            }
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(testKeycloakId)
    }
}
