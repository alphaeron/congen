package com.congen.service

import com.congen.dal.UserDAL
import com.congen.model.User
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserWeeklyTest
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
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
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformanceScoringServiceTest {
    private lateinit var wilksCalculationService: WilksCalculationService
    private lateinit var userDAL: UserDAL
    private lateinit var unitConverter: UnitConverter
    private lateinit var performanceScoringService: PerformanceScoringService

    private val testKeycloakId = "test-keycloak-id"
    private val now = Instant.now()
    private val weekStart =
        now.atZone(ZoneOffset.UTC).toLocalDate().let { date ->
            val dayOfWeek = date.dayOfWeek.value
            val daysToSubtract = if (dayOfWeek == 1) 0 else dayOfWeek - 1
            date.minusDays(daysToSubtract.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant()
        }

    @BeforeEach
    fun setUp() {
        wilksCalculationService = mock()
        userDAL = mock()
        unitConverter = mock()

        performanceScoringService =
            PerformanceScoringService(
                wilksCalculationService,
                userDAL,
                unitConverter
            )
    }

    @Test
    fun `calculatePerformanceScores should calculate scores with all metrics`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val weeklyTest =
            UserWeeklyTest(
                keycloakId = testKeycloakId,
                weekStartTimestamp = weekStart,
                verticalJumpStatus = com.congen.model.TestStatus.COMPLETED,
                verticalJumpResult = 60.0,
                hrRecoveryStatus = com.congen.model.TestStatus.COMPLETED,
                hrRecoveryResult = 30.0,
                reflexStatus = com.congen.model.TestStatus.COMPLETED,
                reflexResult = 200.0,
                mobilityStatus = com.congen.model.TestStatus.COMPLETED,
                mobilityResult = 85.0,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 180,
                height = 72,
                gender = "male",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(180), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(81.6))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 81.6, true))
            .thenReturn(Mono.just(350.0))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, weeklyTest, "test")

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.keycloakId == testKeycloakId)
                assert(scores.explosivenessScore != null)
                assert(scores.aerobicCapacityScore != null)
                assert(scores.recoveryScore != null)
                assert(scores.reactionTimeScore != null)
                assert(scores.mobilityScore != null)
                assert(scores.strengthScore != null)
                assert(scores.wilksScore == 350.0)
                assert(scores.level >= 1)
                assert(scores.levelChangeReason == "test")
                assert(scores.hp >= 0.0)
                assert(scores.hpLoss >= 0.0)
                assert(scores.mp >= 0.0)
                assert(scores.mpLoss >= 0.0)
                assert(scores.fatigue >= 0.0)
                assert(scores.fatigueLoss >= 0.0)
                assert(scores.skills.isNotEmpty())
                assert(scores.createdAt != null)
            }
            .verifyComplete()

        verify(userDAL).selectUserByKeycloakId(testKeycloakId)
        verify(unitConverter).toKg(BigDecimal.valueOf(180), WeightUnit.LBS)
        verify(wilksCalculationService).calculateWilksScore(testKeycloakId, 81.6, true)
    }

    @Test
    fun `calculatePerformanceScores should handle null weekly test`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 180,
                height = 72,
                gender = "male",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(180), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(81.6))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 81.6, true))
            .thenReturn(Mono.just(350.0))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null, "test")

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.keycloakId == testKeycloakId)
                assert(scores.explosivenessScore == null)
                assert(scores.aerobicCapacityScore != null)
                assert(scores.recoveryScore == null)
                assert(scores.reactionTimeScore == null)
                assert(scores.mobilityScore == null)
                assert(scores.strengthScore != null)
                assert(scores.wilksScore == 350.0)
                assert(scores.level >= 1)
                assert(scores.levelChangeReason == "test")
                assert(scores.hp >= 0.0)
                assert(scores.hpLoss >= 0.0)
                assert(scores.mp >= 0.0)
                assert(scores.mpLoss >= 0.0)
                assert(scores.fatigue >= 0.0)
                assert(scores.fatigueLoss >= 0.0)
                assert(scores.skills.isNotEmpty())
                assert(scores.createdAt != null)
            }
            .verifyComplete()
    }

    @Test
    fun `calculatePerformanceScores should handle missing user data`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = null,
                height = 72,
                gender = null,
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null, "test")

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.keycloakId == testKeycloakId)
                assert(scores.explosivenessScore == null)
                assert(scores.aerobicCapacityScore != null)
                assert(scores.recoveryScore == null)
                assert(scores.reactionTimeScore == null)
                assert(scores.mobilityScore == null)
                assert(scores.strengthScore == 0.0)
                assert(scores.wilksScore == 0.0)
                assert(scores.level >= 1)
                assert(scores.levelChangeReason == "test")
                assert(scores.hp >= 0.0)
                assert(scores.hpLoss >= 0.0)
                assert(scores.mp >= 0.0)
                assert(scores.mpLoss >= 0.0)
                assert(scores.fatigue >= 0.0)
                assert(scores.fatigueLoss >= 0.0)
                assert(scores.skills.isNotEmpty())
                assert(scores.createdAt != null)
            }
            .verifyComplete()
    }

    @Test
    fun `calculatePerformanceScores should handle wilks calculation error`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 180,
                height = 72,
                gender = "male",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(180), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(81.6))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 81.6, true))
            .thenReturn(Mono.error(RuntimeException("Wilks calculation failed")))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null, "test")

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.keycloakId == testKeycloakId)
                assert(scores.explosivenessScore == null)
                assert(scores.aerobicCapacityScore != null)
                assert(scores.recoveryScore == null)
                assert(scores.reactionTimeScore == null)
                assert(scores.mobilityScore == null)
                assert(scores.strengthScore == 0.0)
                assert(scores.wilksScore == 0.0)
                assert(scores.level >= 1)
                assert(scores.levelChangeReason == "test")
                assert(scores.hp >= 0.0)
                assert(scores.hpLoss >= 0.0)
                assert(scores.mp >= 0.0)
                assert(scores.mpLoss >= 0.0)
                assert(scores.fatigue >= 0.0)
                assert(scores.fatigueLoss >= 0.0)
                assert(scores.skills.isNotEmpty())
                assert(scores.createdAt != null)
            }
            .verifyComplete()
    }

    @Test
    fun `calculatePerformanceScores should handle null wilks score`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 180,
                height = 72,
                gender = "male",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(180), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(81.6))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 81.6, true))
            .thenReturn(Mono.fromCallable { null as Double? })

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null, "test")

        StepVerifier.create(result)
            .verifyComplete()
    }

    @Test
    fun `calculatePerformanceScores should use default level change reason`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 180,
                height = 72,
                gender = "male",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(180), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(81.6))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 81.6, true))
            .thenReturn(Mono.just(350.0))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null)

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.levelChangeReason == "daily_metrics_updated")
            }
            .verifyComplete()
    }

    @Test
    fun `calculatePerformanceScores should handle female user`() {
        val dailyMetrics =
            UserPerformanceMetrics(
                keycloakId = testKeycloakId,
                vo2Max = 45.0,
                strain = 12.5,
                recovery = 75.0,
                hrv = 55.0,
                sleepScore = 80.0,
                remSleepMinutes = 90.0,
                deepSleepMinutes = 120.0,
                subjectiveTiredness = 3,
                createdAt = now,
                updatedAt = now
            )

        val user =
            User(
                keycloakId = testKeycloakId,
                name = "Test User",
                age = 25,
                weight = 140,
                height = 65,
                gender = "female",
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserByKeycloakId(testKeycloakId)).thenReturn(Mono.just(user))
        whenever(unitConverter.toKg(BigDecimal.valueOf(140), WeightUnit.LBS)).thenReturn(BigDecimal.valueOf(63.5))
        whenever(wilksCalculationService.calculateWilksScore(testKeycloakId, 63.5, false))
            .thenReturn(Mono.just(280.0))

        val result = performanceScoringService.calculatePerformanceScores(dailyMetrics, null, "test")

        StepVerifier.create(result)
            .assertNext { scores ->
                assert(scores.keycloakId == testKeycloakId)
                assert(scores.wilksScore == 280.0)
            }
            .verifyComplete()

        verify(wilksCalculationService).calculateWilksScore(testKeycloakId, 63.5, false)
    }
}
