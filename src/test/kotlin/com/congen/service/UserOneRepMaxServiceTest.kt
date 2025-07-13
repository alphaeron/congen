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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UserOneRepMaxServiceTest {
    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @Mock
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @Mock
    private lateinit var unitConversionService: UnitConversionService

    private lateinit var service: UserOneRepMaxService

    private val userId = 1
    private val exerciseName = "Bench Press"
    private val oneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName)

    companion object {
        @JvmStatic
        fun createUserOneRepMaxTestData(): List<com.congen.model.UserOneRepMax> =
            listOf(
                mockUserOneRepMax(userId = 1, exerciseName = "Bench Press", oneRepMax = BigDecimal("225.0")),
                mockUserOneRepMax(userId = 1, exerciseName = "Deadlift", oneRepMax = BigDecimal("315.0")),
                mockUserOneRepMax(userId = 1, exerciseName = "Squat", oneRepMax = BigDecimal("275.0"))
            )

        @JvmStatic
        fun createWeightUnitPreferenceTestData(): List<com.congen.model.UserWeightUnitPreference> =
            listOf(
                mockUserWeightUnitPreference(userId = 1, exerciseName = "Bench Press", preferredUnit = com.congen.model.WeightUnit.LBS),
                mockUserWeightUnitPreference(userId = 1, exerciseName = "Deadlift", preferredUnit = com.congen.model.WeightUnit.KG),
                mockUserWeightUnitPreference(userId = 1, exerciseName = "Squat", preferredUnit = com.congen.model.WeightUnit.LBS)
            )
    }

    @BeforeEach
    fun setUp() {
        service = UserOneRepMaxService(userOneRepMaxDAL, userWeightUnitPreferenceDAL, unitConversionService)
    }

    @Test
    fun `upsertOneRepMax stores value in kg when unit is kg`() {
        // Given
        val weight = BigDecimal("100.0")
        whenever(unitConversionService.toKg(weight, WeightUnit.KG)).thenReturn(weight)
        whenever(userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, weight)).thenReturn(createMockMono(oneRepMax))

        // When
        val result = service.upsertOneRepMax(userId, exerciseName, weight, "kg")

        // Then
        assertMonoSuccess(result, oneRepMax)
        verify(userOneRepMaxDAL).upsertUserOneRepMax(userId, exerciseName, weight)
    }

    @Test
    fun `upsertOneRepMax converts lbs to kg for storage`() {
        // Given
        val weightInLbs = BigDecimal("220.46")
        val weightInKg = BigDecimal("100.0")
        whenever(unitConversionService.toKg(weightInLbs, WeightUnit.LBS)).thenReturn(weightInKg)
        whenever(userOneRepMaxDAL.upsertUserOneRepMax(userId, exerciseName, weightInKg)).thenReturn(createMockMono(oneRepMax))

        // When
        val result = service.upsertOneRepMax(userId, exerciseName, weightInLbs, "lbs")

        // Then
        assertMonoSuccess(result, oneRepMax)
        verify(userOneRepMaxDAL).upsertUserOneRepMax(userId, exerciseName, weightInKg)
    }

    @Test
    fun `getAllByUser returns converted values using preferences`() {
        // Given
        val oneRepMaxes = createUserOneRepMaxTestData()
        val preferences = createWeightUnitPreferenceTestData()
        val convertedWeight1 = BigDecimal("220.46")
        val convertedWeight2 = BigDecimal("315.0") // KG stays the same
        val convertedWeight3 = BigDecimal("606.27")

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(createMockMono(oneRepMaxes))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)).thenReturn(createMockMono(preferences))
        whenever(unitConversionService.fromKg(eq(oneRepMaxes[0].oneRepMax), eq(WeightUnit.LBS))).thenReturn(convertedWeight1)
        whenever(unitConversionService.fromKg(eq(oneRepMaxes[1].oneRepMax), eq(WeightUnit.KG))).thenReturn(convertedWeight2)
        whenever(unitConversionService.fromKg(eq(oneRepMaxes[2].oneRepMax), eq(WeightUnit.LBS))).thenReturn(convertedWeight3)

        // When
        val result = service.getAllByUser(userId, null)

        // Then
        reactor.test.StepVerifier.create(result)
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
        // Given
        val preference = mockUserWeightUnitPreference(userId = userId, exerciseName = exerciseName)
        val convertedWeight = BigDecimal("220.46")

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(createMockMono(oneRepMax))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)).thenReturn(createMockMono(preference))
        whenever(unitConversionService.fromKg(oneRepMax.oneRepMax, WeightUnit.LBS)).thenReturn(convertedWeight)

        // When
        val result = service.getByUserAndExercise(userId, exerciseName, null)

        // Then
        reactor.test.StepVerifier.create(result)
            .assertNext { userOneRepMax ->
                assert(userOneRepMax.oneRepMax == convertedWeight)
            }
            .verifyComplete()
    }

    @Test
    fun `deleteOneRepMax returns deleted value`() {
        // Given
        whenever(userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)).thenReturn(createMockMono(oneRepMax))

        // When
        val result = service.deleteOneRepMax(userId, exerciseName)

        // Then
        assertMonoSuccess(result, oneRepMax)
    }

    @Test
    fun `getByUserAndExercise returns error if not found`() {
        // Given
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(
            createMockMonoError(NoResultsFoundException("not found"))
        )

        // When & Then
        assertMonoError(
            service.getByUserAndExercise(userId, exerciseName, null),
            NoResultsFoundException::class.java
        )
    }
}
