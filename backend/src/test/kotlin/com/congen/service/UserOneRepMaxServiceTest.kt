package com.congen.service

import com.congen.assertMonoError
import com.congen.assertMonoSuccess
import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.dal.UserOneRepMaxDAL
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UserOneRepMaxServiceTest {
    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

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
        service = UserOneRepMaxService(userOneRepMaxDAL, userWeightUnitPreferenceDAL, unitConverter)
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
    fun `getAllByUser returns converted values using preferences`() {
        val oneRepMaxes = createUserOneRepMaxTestData()
        val preferences = createWeightUnitPreferenceTestData()
        val convertedWeight1 = BigDecimal("220.46")
        val convertedWeight2 = BigDecimal("315.0") // KG stays the same
        val convertedWeight3 = BigDecimal("606.27")

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(createMockMono(oneRepMaxes))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)).thenReturn(createMockMono(preferences))
        whenever(unitConverter.fromKg(eq(oneRepMaxes[0].oneRepMax), eq(WeightUnit.LBS))).thenReturn(convertedWeight1)
        whenever(unitConverter.fromKg(eq(oneRepMaxes[1].oneRepMax), eq(WeightUnit.KG))).thenReturn(convertedWeight2)
        whenever(unitConverter.fromKg(eq(oneRepMaxes[2].oneRepMax), eq(WeightUnit.LBS))).thenReturn(convertedWeight3)

        val result = service.selectUserOneRepMaxByUser(userId, null)

        StepVerifier.create(result)
            .assertNext { list ->
                assert(list.size == 3)
                assert(list[0].oneRepMax == convertedWeight1)
                assert(list[1].oneRepMax == convertedWeight2)
                assert(list[2].oneRepMax == convertedWeight3)
            }
            .verifyComplete()
    }

    @Test
    fun `getByUserAndExercise returns converted value using preference`() {
        val preference = mockUserWeightUnitPreference(userId = userId, exerciseName = exerciseName)
        val convertedWeight = BigDecimal("220.46")

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(createMockMono(oneRepMax))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)).thenReturn(createMockMono(preference))
        whenever(unitConverter.fromKg(oneRepMax.oneRepMax, WeightUnit.LBS)).thenReturn(convertedWeight)

        val result = service.selectUserOneRepMax(userId, exerciseName, null)

        StepVerifier.create(result)
            .assertNext { userOneRepMax ->
                assert(userOneRepMax.oneRepMax == convertedWeight)
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
            service.selectUserOneRepMax(userId, exerciseName, null),
            NoResultsFoundException::class.java
        )
    }
}
