package com.congen.service

import com.congen.dal.TestProtocolConfigDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserTestResultDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.TestProtocol
import com.congen.model.TestStatus
import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserTestResult
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformanceTrackingServiceTest {
    private lateinit var userPerformanceMetricsDAL: UserPerformanceMetricsDAL
    private lateinit var userPerformanceScoresDAL: UserPerformanceScoresDAL
    private lateinit var userTestResultDAL: UserTestResultDAL
    private lateinit var testProtocolConfigDAL: TestProtocolConfigDAL
    private lateinit var performanceScoringService: PerformanceScoringService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var performanceTrackingService: PerformanceTrackingService

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
        userPerformanceMetricsDAL = mock()
        userPerformanceScoresDAL = mock()
        userTestResultDAL = mock()
        testProtocolConfigDAL = mock()
        performanceScoringService = mock()
        keycloakUtil = mock()

        performanceTrackingService =
            PerformanceTrackingService(
                userPerformanceMetricsDAL,
                userPerformanceScoresDAL,
                userTestResultDAL,
                testProtocolConfigDAL,
                performanceScoringService,
                keycloakUtil
            )
    }

    @Test
    fun `getCurrentPerformanceScores should return performance scores`() {
        val expectedScores =
            UserPerformanceScores(
                keycloakId = testKeycloakId,
                explosivenessScore = 75.0,
                aerobicCapacityScore = 80.0,
                recoveryScore = 70.0,
                reactionTimeScore = 65.0,
                mobilityScore = 85.0,
                strengthScore = 90.0,
                wilksScore = 350.0,
                level = 15,
                levelChangeReason = "test",
                hp = 80.0,
                hpLoss = 10.0,
                mp = 75.0,
                mpLoss = 5.0,
                fatigue = 20.0,
                fatigueLoss = 15.0,
                skills = listOf("Powerhouse", "Iron Lungs"),
                createdAt = now
            )

        whenever(userPerformanceScoresDAL.selectUserPerformanceScores(testKeycloakId))
            .thenReturn(Mono.just(expectedScores))

        val result = performanceTrackingService.getCurrentPerformanceScores(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(expectedScores)
            .verifyComplete()

        verify(userPerformanceScoresDAL).selectUserPerformanceScores(testKeycloakId)
    }

    @Test
    fun `getCurrentPerformanceScores should propagate errors`() {
        val error = RuntimeException("Database error")
        whenever(userPerformanceScoresDAL.selectUserPerformanceScores(testKeycloakId))
            .thenReturn(Mono.error(error))

        val result = performanceTrackingService.getCurrentPerformanceScores(testKeycloakId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `getPerformanceScoresHistory should return scores in range`() {
        val startTimestamp = now.minus(7, ChronoUnit.DAYS)
        val endTimestamp = now
        val expectedScores =
            listOf(
                UserPerformanceScores(
                    keycloakId = testKeycloakId,
                    explosivenessScore = 75.0,
                    aerobicCapacityScore = 80.0,
                    recoveryScore = 70.0,
                    reactionTimeScore = 65.0,
                    mobilityScore = 85.0,
                    strengthScore = 90.0,
                    wilksScore = 350.0,
                    level = 15,
                    levelChangeReason = "test",
                    hp = 80.0,
                    hpLoss = 10.0,
                    mp = 75.0,
                    mpLoss = 5.0,
                    fatigue = 20.0,
                    fatigueLoss = 15.0,
                    skills = listOf("Powerhouse"),
                    createdAt = now
                )
            )

        whenever(userPerformanceScoresDAL.selectUserPerformanceScoresInRange(testKeycloakId, startTimestamp, endTimestamp))
            .thenReturn(Mono.just(expectedScores))

        val result = performanceTrackingService.getPerformanceScoresHistory(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(expectedScores)
            .verifyComplete()

        verify(userPerformanceScoresDAL).selectUserPerformanceScoresInRange(testKeycloakId, startTimestamp, endTimestamp)
    }

    @Test
    fun `getCurrentPerformanceMetrics should return performance metrics`() {
        val expectedMetrics =
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

        whenever(userPerformanceMetricsDAL.selectUserPerformanceMetrics(testKeycloakId))
            .thenReturn(Mono.just(expectedMetrics))

        val result = performanceTrackingService.getCurrentPerformanceMetrics(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(expectedMetrics)
            .verifyComplete()

        verify(userPerformanceMetricsDAL).selectUserPerformanceMetrics(testKeycloakId)
    }

    @Test
    fun `createDefaultPerformanceData should create default data for new user`() {
        val testProtocols =
            listOf(
                TestProtocol(
                    testName = "vertical_jump",
                    displayName = "Vertical Jump",
                    description = "Test vertical jump height",
                    unit = "cm",
                    iconName = "jump",
                    isRequired = true,
                    displayOrder = 1,
                    radarChartColor = "#FF0000",
                    radarChartEnabled = true
                ),
                TestProtocol(
                    testName = "hr_recovery",
                    displayName = "HR Recovery",
                    description = "Test heart rate recovery",
                    unit = "bpm",
                    iconName = "heart",
                    isRequired = true,
                    displayOrder = 2,
                    radarChartColor = "#00FF00",
                    radarChartEnabled = true
                )
            )

        val expectedScores =
            UserPerformanceScores(
                keycloakId = testKeycloakId,
                explosivenessScore = null,
                aerobicCapacityScore = null,
                recoveryScore = null,
                reactionTimeScore = null,
                mobilityScore = null,
                strengthScore = null,
                wilksScore = null,
                level = 1,
                levelChangeReason = "account_creation",
                hp = 50.0,
                hpLoss = 0.0,
                mp = 50.0,
                mpLoss = 0.0,
                fatigue = 0.0,
                fatigueLoss = 0.0,
                skills = emptyList(),
                createdAt = now
            )

        whenever(testProtocolConfigDAL.getAllTestProtocols()).thenReturn(Mono.just(testProtocols))
        whenever(userPerformanceMetricsDAL.upsertUserPerformanceMetrics(any())).thenReturn(
            Mono.just(
                UserPerformanceMetrics(
                    keycloakId = testKeycloakId,
                    vo2Max = null,
                    strain = null,
                    recovery = null,
                    hrv = null,
                    sleepScore = null,
                    remSleepMinutes = null,
                    deepSleepMinutes = null,
                    subjectiveTiredness = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
        whenever(userTestResultDAL.upsertUserTestResult(any())).thenReturn(
            Mono.just(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.PENDING,
                    resultValue = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
        whenever(performanceScoringService.calculatePerformanceScores(any(), anyOrNull(), eq("account_creation")))
            .thenReturn(Mono.just(expectedScores))
        whenever(userPerformanceScoresDAL.insertUserPerformanceScores(expectedScores))
            .thenReturn(Mono.just(expectedScores))

        val result = performanceTrackingService.createDefaultPerformanceData(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(expectedScores)
            .verifyComplete()

        verify(testProtocolConfigDAL).getAllTestProtocols()
        verify(userPerformanceMetricsDAL).upsertUserPerformanceMetrics(any())
        verify(performanceScoringService).calculatePerformanceScores(any(), anyOrNull(), eq("account_creation"))
        verify(userPerformanceScoresDAL).insertUserPerformanceScores(expectedScores)
    }

    @Test
    fun `getWeeklyTests should return test results in range`() {
        val startTimestamp = now.minus(7, ChronoUnit.DAYS)
        val endTimestamp = now
        val expectedResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(userTestResultDAL.getUserTestResultsInRange(testKeycloakId, startTimestamp, endTimestamp))
            .thenReturn(Mono.just(expectedResults))

        val result = performanceTrackingService.getWeeklyTests(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(expectedResults)
            .verifyComplete()

        verify(userTestResultDAL).getUserTestResultsInRange(testKeycloakId, startTimestamp, endTimestamp)
    }

    @Test
    fun `getWeeklyTests should return all test results when no range specified`() {
        val expectedResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(userTestResultDAL.getUserTestResultsInRange(testKeycloakId, null, null))
            .thenReturn(Mono.just(expectedResults))

        val result = performanceTrackingService.getWeeklyTests(testKeycloakId)

        StepVerifier.create(result)
            .expectNext(expectedResults)
            .verifyComplete()

        verify(userTestResultDAL).getUserTestResultsInRange(testKeycloakId, null, null)
    }

    @Test
    fun `getPerformanceMetricsInRange should return metrics in range`() {
        val startTimestamp = now.minus(7, ChronoUnit.DAYS)
        val endTimestamp = now
        val expectedMetrics =
            listOf(
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
            )

        whenever(userPerformanceMetricsDAL.getUserPerformanceMetricsInRange(testKeycloakId, startTimestamp, endTimestamp))
            .thenReturn(Mono.just(expectedMetrics))

        val result = performanceTrackingService.getPerformanceMetricsInRange(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(expectedMetrics)
            .verifyComplete()

        verify(userPerformanceMetricsDAL).getUserPerformanceMetricsInRange(testKeycloakId, startTimestamp, endTimestamp)
    }

    @Test
    fun `submitPerformanceMetrics should update metrics and calculate scores`() {
        val metrics =
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

        val existingMetrics = metrics.copy(vo2Max = 40.0)
        val updatedMetrics = metrics.copy(vo2Max = 45.0)
        val testResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val expectedScores =
            UserPerformanceScores(
                keycloakId = testKeycloakId,
                explosivenessScore = 75.0,
                aerobicCapacityScore = 80.0,
                recoveryScore = 70.0,
                reactionTimeScore = 65.0,
                mobilityScore = 85.0,
                strengthScore = 90.0,
                wilksScore = 350.0,
                level = 15,
                levelChangeReason = "daily_metrics_updated",
                hp = 80.0,
                hpLoss = 10.0,
                mp = 75.0,
                mpLoss = 5.0,
                fatigue = 20.0,
                fatigueLoss = 15.0,
                skills = listOf("Powerhouse"),
                createdAt = now
            )

        whenever(userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(testKeycloakId))
            .thenReturn(Mono.just(existingMetrics))
        whenever(userPerformanceMetricsDAL.upsertUserPerformanceMetrics(any()))
            .thenReturn(Mono.just(updatedMetrics))
        whenever(userTestResultDAL.getUserTestResultsInRange(testKeycloakId, null, null))
            .thenReturn(Mono.just(testResults))
        whenever(performanceScoringService.calculatePerformanceScores(any(), anyOrNull(), eq("daily_metrics_updated")))
            .thenReturn(Mono.just(expectedScores))
        whenever(userPerformanceScoresDAL.insertUserPerformanceScores(expectedScores))
            .thenReturn(Mono.just(expectedScores))

        val result = performanceTrackingService.submitPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectNext(expectedScores)
            .verifyComplete()

        verify(userPerformanceMetricsDAL).getLatestUserPerformanceMetrics(testKeycloakId)
        verify(userPerformanceMetricsDAL).upsertUserPerformanceMetrics(any())
        verify(userTestResultDAL).getUserTestResultsInRange(testKeycloakId, null, null)
        verify(performanceScoringService).calculatePerformanceScores(any(), anyOrNull(), eq("daily_metrics_updated"))
        verify(userPerformanceScoresDAL).insertUserPerformanceScores(expectedScores)
    }

    @Test
    fun `submitPerformanceMetrics should handle no existing metrics`() {
        val metrics =
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

        val testResults = emptyList<UserTestResult>()
        val expectedScores =
            UserPerformanceScores(
                keycloakId = testKeycloakId,
                explosivenessScore = null,
                aerobicCapacityScore = 80.0,
                recoveryScore = null,
                reactionTimeScore = null,
                mobilityScore = null,
                strengthScore = null,
                wilksScore = null,
                level = 1,
                levelChangeReason = "daily_metrics_updated",
                hp = 50.0,
                hpLoss = 0.0,
                mp = 50.0,
                mpLoss = 0.0,
                fatigue = 0.0,
                fatigueLoss = 0.0,
                skills = emptyList(),
                createdAt = now
            )

        whenever(userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(testKeycloakId))
            .thenReturn(Mono.error(NoResultsFoundException("No metrics found")))
        whenever(userPerformanceMetricsDAL.upsertUserPerformanceMetrics(metrics))
            .thenReturn(Mono.just(metrics))
        whenever(userTestResultDAL.getUserTestResultsInRange(testKeycloakId, null, null))
            .thenReturn(Mono.just(testResults))
        whenever(performanceScoringService.calculatePerformanceScores(any(), anyOrNull(), eq("daily_metrics_updated")))
            .thenReturn(Mono.just(expectedScores))
        whenever(userPerformanceScoresDAL.insertUserPerformanceScores(expectedScores))
            .thenReturn(Mono.just(expectedScores))

        val result = performanceTrackingService.submitPerformanceMetrics(metrics)

        StepVerifier.create(result)
            .expectNext(expectedScores)
            .verifyComplete()

        verify(userPerformanceMetricsDAL).getLatestUserPerformanceMetrics(testKeycloakId)
        verify(userPerformanceMetricsDAL).upsertUserPerformanceMetrics(metrics)
    }

    @Test
    fun `submitWeeklyTest should validate and update test results`() {
        val testResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        val updatedTestResults = testResults
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
        val allTestResults = testResults
        val expectedScores =
            UserPerformanceScores(
                keycloakId = testKeycloakId,
                explosivenessScore = 75.0,
                aerobicCapacityScore = 80.0,
                recoveryScore = 70.0,
                reactionTimeScore = 65.0,
                mobilityScore = 85.0,
                strengthScore = 90.0,
                wilksScore = 350.0,
                level = 15,
                levelChangeReason = "weekly_test_updated",
                hp = 80.0,
                hpLoss = 10.0,
                mp = 75.0,
                mpLoss = 5.0,
                fatigue = 20.0,
                fatigueLoss = 15.0,
                skills = listOf("Powerhouse"),
                createdAt = now
            )

        whenever(userTestResultDAL.upsertUserTestResult(any())).thenReturn(Mono.just(testResults.first()))
        whenever(userPerformanceMetricsDAL.selectUserPerformanceMetrics(testKeycloakId))
            .thenReturn(Mono.just(dailyMetrics))
        whenever(userTestResultDAL.getUserTestResultsInRange(testKeycloakId, null, null))
            .thenReturn(Mono.just(allTestResults))
        whenever(performanceScoringService.calculatePerformanceScores(any(), anyOrNull(), eq("weekly_test_updated")))
            .thenReturn(Mono.just(expectedScores))
        whenever(userPerformanceScoresDAL.insertUserPerformanceScores(expectedScores))
            .thenReturn(Mono.just(expectedScores))
        whenever(testProtocolConfigDAL.getAllTestProtocols())
            .thenReturn(
                Mono.just(
                    listOf(
                        TestProtocol(
                            testName = "vertical_jump",
                            displayName = "Vertical Jump",
                            description = "Test vertical jump height",
                            unit = "cm",
                            iconName = "jump",
                            isRequired = true,
                            displayOrder = 1,
                            radarChartColor = "#FF0000",
                            radarChartEnabled = true
                        )
                    )
                )
            )

        val result = performanceTrackingService.submitWeeklyTest(testResults)

        StepVerifier.create(result)
            .expectNext(updatedTestResults)
            .verifyComplete()

        verify(userTestResultDAL).upsertUserTestResult(any())
        verify(userPerformanceMetricsDAL).selectUserPerformanceMetrics(testKeycloakId)
        verify(performanceScoringService).calculatePerformanceScores(any(), anyOrNull(), eq("weekly_test_updated"))
        verify(userPerformanceScoresDAL).insertUserPerformanceScores(expectedScores)
    }

    @Test
    fun `submitWeeklyTest should reject empty test results`() {
        val result = performanceTrackingService.submitWeeklyTest(emptyList())

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
    }

    @Test
    fun `submitWeeklyTest should reject invalid week start date`() {
        val invalidWeekStart = now.plus(1, ChronoUnit.DAYS) // Not a Monday
        val testResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = invalidWeekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(testProtocolConfigDAL.getAllTestProtocols()).thenReturn(Mono.just(emptyList()))

        val result = performanceTrackingService.submitWeeklyTest(testResults)

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
    }

    @Test
    fun `submitWeeklyTest should reject mixed user and week data`() {
        val otherWeekStart = weekStart.plus(7, ChronoUnit.DAYS)
        val testResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                ),
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = otherWeekStart,
                    testName = "hr_recovery",
                    status = TestStatus.COMPLETED,
                    resultValue = 30.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        val result = performanceTrackingService.submitWeeklyTest(testResults)

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
    }

    @Test
    fun `getTestProtocolsFromDatabase should return test protocols`() {
        val expectedProtocols =
            listOf(
                TestProtocol(
                    testName = "vertical_jump",
                    displayName = "Vertical Jump",
                    description = "Test vertical jump height",
                    unit = "cm",
                    iconName = "jump",
                    isRequired = true,
                    displayOrder = 1,
                    radarChartColor = "#FF0000",
                    radarChartEnabled = true
                )
            )

        whenever(testProtocolConfigDAL.getAllTestProtocols()).thenReturn(Mono.just(expectedProtocols))

        val result = performanceTrackingService.getTestProtocolsFromDatabase()

        StepVerifier.create(result)
            .expectNext(expectedProtocols)
            .verifyComplete()

        verify(testProtocolConfigDAL).getAllTestProtocols()
    }

    @Test
    fun `convertTestResultsToWeeklyTest should convert test results correctly`() {
        val testResults =
            listOf(
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "vertical_jump",
                    status = TestStatus.COMPLETED,
                    resultValue = 60.0,
                    createdAt = now,
                    updatedAt = now
                ),
                UserTestResult(
                    keycloakId = testKeycloakId,
                    weekStartTimestamp = weekStart,
                    testName = "hr_recovery",
                    status = TestStatus.COMPLETED,
                    resultValue = 30.0,
                    createdAt = now,
                    updatedAt = now
                )
            )

        val result = performanceTrackingService.convertTestResultsToWeeklyTest(testResults)

        assert(result != null)
        assert(result!!.keycloakId == testKeycloakId)
        assert(result.weekStartTimestamp == weekStart)
        assert(result.verticalJumpStatus == TestStatus.COMPLETED)
        assert(result.verticalJumpResult == 60.0)
        assert(result.hrRecoveryStatus == TestStatus.COMPLETED)
        assert(result.hrRecoveryResult == 30.0)
    }

    @Test
    fun `convertTestResultsToWeeklyTest should return null for empty results`() {
        val result = performanceTrackingService.convertTestResultsToWeeklyTest(emptyList())
        assert(result == null)
    }
}
