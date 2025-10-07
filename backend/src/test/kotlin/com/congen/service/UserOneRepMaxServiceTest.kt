package com.congen.service

import com.congen.assertMonoError
import com.congen.assertMonoSuccess
import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserOneRepMax
import com.congen.mockUserWeightUnitPreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserOneRepMaxServiceTest {
    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @Mock
    private lateinit var performanceScoringService: PerformanceScoringService

    @Mock
    private lateinit var performanceTrackingService: PerformanceTrackingService

    @Mock
    private lateinit var userPerformanceMetricsDAL: UserPerformanceMetricsDAL

    @Mock
    private lateinit var userPerformanceScoresDAL: UserPerformanceScoresDAL

    @Mock
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @Mock
    private lateinit var unitConverter: UnitConverter

    private lateinit var service: UserOneRepMaxService

    private val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
    private val exerciseName = "Bench Press"
    private val oneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName)

    companion object {
        @JvmStatic
        fun createUserOneRepMaxTestData(): List<UserOneRepMax> =
            listOf(
                mockUserOneRepMax(
                    userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("225.0")
                ),
                mockUserOneRepMax(
                    userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                    exerciseName = "Deadlift",
                    oneRepMax = BigDecimal("315.0")
                ),
                mockUserOneRepMax(userId = "b226d772-c063-4974-ae08-ab64134abbcf", exerciseName = "Squat", oneRepMax = BigDecimal("275.0"))
            )

        @JvmStatic
        fun createWeightUnitPreferenceTestData(): List<UserWeightUnitPreference> =
            listOf(
                mockUserWeightUnitPreference(
                    userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                    exerciseName = "Bench Press",
                    preferredUnit = WeightUnit.LBS
                ),
                mockUserWeightUnitPreference(
                    userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                    exerciseName = "Deadlift",
                    preferredUnit = WeightUnit.KG
                ),
                mockUserWeightUnitPreference(
                    userId = "b226d772-c063-4974-ae08-ab64134abbcf",
                    exerciseName = "Squat",
                    preferredUnit = WeightUnit.LBS
                )
            )
    }

    @BeforeEach
    fun setUp() {
        service =
            UserOneRepMaxService(
                userOneRepMaxDAL,
                performanceScoringService,
                performanceTrackingService,
                userPerformanceMetricsDAL,
                userPerformanceScoresDAL,
                userWeightUnitPreferenceDAL,
                unitConverter
            )

        // Mock the performance tracking service methods to return proper Mono objects to avoid null pointer exceptions
        // The triggerPerformanceScoreRecalculation method uses onErrorComplete() so it won't fail the main operation
        whenever(userPerformanceMetricsDAL.getLatestUserPerformanceMetrics(any())).thenReturn(Mono.empty())
        whenever(performanceTrackingService.getWeeklyTests(any(), any(), any())).thenReturn(Mono.empty())
        whenever(performanceTrackingService.convertTestResultsToWeeklyTest(any())).thenReturn(null)
        whenever(performanceScoringService.calculatePerformanceScores(any(), any(), any())).thenReturn(Mono.empty())
        whenever(userPerformanceScoresDAL.upsertUserPerformanceScores(any())).thenReturn(Mono.empty())

        // Mock the weight unit preference DAL to return a default preference to avoid null pointer exceptions
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(
                Mono.just(mockUserWeightUnitPreference(userId = userId, exerciseName = exerciseName, preferredUnit = WeightUnit.KG))
            )

        // Mock the unit converter to return the same weight when converting from KG to KG (default case)
        whenever(unitConverter.fromKg(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<BigDecimal>(0)
        }
    }

    @Test
    fun `upsertOneRepMax stores value in kg when unit is kg`() {
        val weight = BigDecimal("100.0")
        whenever(unitConverter.toKg(weight, WeightUnit.KG)).thenReturn(weight)
        whenever(userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, weight)).thenReturn(createMockMono(oneRepMax))

        val result = service.upsertUserOneRepMax(userId, exerciseName, weight, "kg")

        assertMonoSuccess(result, oneRepMax)
        verify(userOneRepMaxDAL).upsertUserOneRepMax(userId, exerciseName, weight)
    }

    @Test
    fun `upsertOneRepMax converts lbs to kg for storage`() {
        val weightInLbs = BigDecimal("220.46")
        val weightInKg = BigDecimal("100.0")
        whenever(unitConverter.toKg(weightInLbs, WeightUnit.LBS)).thenReturn(weightInKg)
        whenever(userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, weightInKg)).thenReturn(createMockMono(oneRepMax))

        val result = service.upsertUserOneRepMax(userId, exerciseName, weightInLbs, "lbs")

        assertMonoSuccess(result, oneRepMax)
        verify(userOneRepMaxDAL).upsertUserOneRepMax(userId, exerciseName, weightInKg)
    }

    @Test
    fun `getAllByUser returns raw values from DAL`() {
        val oneRepMaxes = createUserOneRepMaxTestData()

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(createMockMono(oneRepMaxes))

        val result = service.selectUserOneRepMaxByUser(userId)

        StepVerifier.create(result)
            .assertNext { list ->
                assert(list.size == 3) { "Expected list size 3, got ${list.size}" }
                assert(
                    list[0].oneRepMax == oneRepMaxes[0].oneRepMax
                ) { "Expected first weight ${oneRepMaxes[0].oneRepMax}, got ${list[0].oneRepMax}" }
                assert(
                    list[1].oneRepMax == oneRepMaxes[1].oneRepMax
                ) { "Expected second weight ${oneRepMaxes[1].oneRepMax}, got ${list[1].oneRepMax}" }
                assert(
                    list[2].oneRepMax == oneRepMaxes[2].oneRepMax
                ) { "Expected third weight ${oneRepMaxes[2].oneRepMax}, got ${list[2].oneRepMax}" }
            }
            .verifyComplete()
    }

    @Test
    fun `getByUserAndExercise returns raw value from DAL`() {
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(createMockMono(oneRepMax))

        val result = service.selectUserOneRepMax(userId, exerciseName)

        StepVerifier.create(result)
            .assertNext { userOneRepMax ->
                assert(
                    userOneRepMax.oneRepMax == oneRepMax.oneRepMax
                ) { "Expected weight ${oneRepMax.oneRepMax}, got ${userOneRepMax.oneRepMax}" }
            }
            .verifyComplete()
    }

    @Test
    fun `deleteOneRepMax returns deleted value`() {
        whenever(userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)).thenReturn(createMockMono(oneRepMax))

        val result = service.deleteUserOneRepMax(userId, exerciseName)

        assertMonoSuccess(result, oneRepMax)
    }

    @Test
    fun `getByUserAndExercise returns error if not found`() {
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(
            createMockMonoError(NoResultsFoundException("not found"))
        )

        assertMonoError(
            service.selectUserOneRepMax(userId, exerciseName),
            NoResultsFoundException::class.java
        )
    }
}
