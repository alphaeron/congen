package com.congen.service

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockProgrammedExercise
import com.congen.mockSetScheme
import com.congen.mockUserOneRepMax
import com.congen.model.SetScheme
import com.congen.model.WeightUnit
import com.congen.sampleInstant
import com.congen.util.OneRepMaxCalculator
import com.congen.util.UnitConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

/**
 * Unit tests for SetSchemeService.
 *
 * These tests verify the business logic for set scheme operations,
 * including automatic 1RM updates when performed weights exceed current 1RM values.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class SetSchemeServiceTest {
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var unitConversionService: UnitConverter
    private lateinit var setSchemeService: SetSchemeService

    private val now = sampleInstant()
    private val programmedExerciseId = 1L
    private val setSchemeId = 1L
    private val setNumber = 1
    private val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
    private val exerciseName = "Bench Press"
    private val performedWeight = BigDecimal("120.0")
    private val currentOneRepMaxValue = BigDecimal("100.0")
    private val newOneRepMaxValue = BigDecimal("120.0")

    @BeforeEach
    fun setUp() {
        setSchemeDAL = mock()
        programmedExerciseDAL = mock()
        userOneRepMaxDAL = mock()
        unitConversionService = mock()
        val oneRepMaxCalculator = OneRepMaxCalculator()
        setSchemeService =
            SetSchemeService(
                setSchemeDAL,
                programmedExerciseDAL,
                userOneRepMaxDAL,
                unitConversionService,
                oneRepMaxCalculator
            )
    }

    @Test
    fun `should create set scheme with unit conversion`() {
        val targetWeight = "100.0"
        val performedWeight = "120.0"
        val unit = "LBS"
        val targetWeightInKg = BigDecimal("45.36")
        val performedWeightInKg = BigDecimal("54.43")
        val convertedTargetWeight = BigDecimal("100.0")
        val convertedPerformedWeight = BigDecimal("120.0")

        val setScheme =
            mockSetScheme(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                targetWeight = targetWeightInKg,
                performedWeight = performedWeightInKg
            )

        // Mock unit conversion service for toKg (input conversion)
        whenever(unitConversionService.toKg(convertedTargetWeight, WeightUnit.LBS))
            .thenReturn(targetWeightInKg)
        whenever(unitConversionService.toKg(convertedPerformedWeight, WeightUnit.LBS))
            .thenReturn(performedWeightInKg)

        // Mock unit conversion service for fromKg (output conversion)
        whenever(unitConversionService.fromKg(targetWeightInKg, WeightUnit.LBS))
            .thenReturn(convertedTargetWeight)
        whenever(unitConversionService.fromKg(performedWeightInKg, WeightUnit.LBS))
            .thenReturn(convertedPerformedWeight)

        // Mock the DAL to return the expected setScheme with exact signature match
        whenever(
            setSchemeDAL.insertSetScheme(
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeService.createSetScheme(
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = targetWeight,
                performedWeight = performedWeight,
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                unit = unit
            )
                .block()

        // The service should return the converted values (LBS) after unit conversion
        assertEquals(convertedTargetWeight, result?.targetWeight)
        assertEquals(convertedPerformedWeight, result?.performedWeight)
        assertEquals(setSchemeId, result?.id)
        assertEquals(programmedExerciseId, result?.programmedExerciseId)
        assertEquals(setNumber, result?.setNumber)
    }

    @Test
    fun `should update set scheme with unit conversion`() {
        val targetWeight = "100.0"
        val performedWeight = "120.0"
        val unit = "LBS"
        val targetWeightInKg = BigDecimal("45.36")
        val performedWeightInKg = BigDecimal("54.43")
        val convertedTargetWeight = BigDecimal("100.0")
        val convertedPerformedWeight = BigDecimal("120.0")

        val setScheme =
            mockSetScheme(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                targetWeight = targetWeightInKg,
                performedWeight = performedWeightInKg
            )
        val programmedExercise = mockProgrammedExercise(id = programmedExerciseId, exerciseName = exerciseName)
        val currentOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = BigDecimal("50.0"))
        val newOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = performedWeightInKg)

        whenever(unitConversionService.toKg(eq(BigDecimal(targetWeight)), eq(WeightUnit.LBS))).thenReturn(targetWeightInKg)
        whenever(unitConversionService.toKg(eq(BigDecimal(performedWeight)), eq(WeightUnit.LBS))).thenReturn(performedWeightInKg)
        whenever(unitConversionService.fromKg(eq(targetWeightInKg), eq(WeightUnit.LBS))).thenReturn(convertedTargetWeight)
        whenever(unitConversionService.fromKg(eq(performedWeightInKg), eq(WeightUnit.LBS))).thenReturn(convertedPerformedWeight)
        whenever(
            setSchemeDAL.updateSetScheme(
                eq(setSchemeId),
                eq(programmedExerciseId),
                eq(setNumber),
                eq(false),
                eq(false),
                eq(false),
                eq(null),
                eq(null),
                eq(null),
                eq(targetWeightInKg),
                eq(performedWeightInKg),
                eq(null),
                eq(null),
                eq(null),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(userId))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(programmedExerciseId)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, performedWeightInKg)).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                setSchemeId,
                programmedExerciseId,
                setNumber,
                false,
                false,
                false,
                null,
                null,
                null,
                targetWeight,
                performedWeight,
                null,
                null,
                null,
                unit,
                null
            )

        StepVerifier.create(result)
            .expectNextMatches { scheme ->
                scheme.targetWeight == convertedTargetWeight &&
                    scheme.performedWeight == convertedPerformedWeight
            }
            .verifyComplete()
    }

    @Test
    fun `should insert set scheme`() {
        val setScheme = mockSetScheme()
        val targetWeight = "100.0"
        val performedWeight = null
        val unit = WeightUnit.KG.name

        // Mock the DAL to return the expected setScheme with exact signature match
        whenever(
            setSchemeDAL.insertSetScheme(
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeService.createSetScheme(
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = targetWeight,
                performedWeight = performedWeight,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180,
                unit = unit
            )
                .block()

        assertEquals(setScheme, result)
    }

    @Test
    fun `should update set scheme and update existing 1RM when performed weight is greater`() {
        val setScheme =
            mockSetScheme(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                performedWeight = performedWeight,
                createdAt = now,
                updatedAt = now
            )
        val programmedExercise =
            mockProgrammedExercise(id = programmedExerciseId, exerciseName = exerciseName, createdAt = now, updatedAt = now)
        val currentOneRepMax =
            mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = currentOneRepMaxValue, updatedAt = now)
        val newOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = newOneRepMaxValue, updatedAt = now)

        whenever(
            setSchemeDAL.updateSetScheme(
                anyLong(),
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(userId))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(programmedExerciseId)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, newOneRepMaxValue)).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = performedWeight.toString(),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                unit = "KG",
                band = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should update set scheme and insert new 1RM when none exists`() {
        val setScheme =
            SetScheme(
                id = 2L,
                programmedExerciseId = 2L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("150.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                createdAt = now,
                updatedAt = now,
                band = null,
            )
        val programmedExercise = mockProgrammedExercise(id = 2L, exerciseName = "Deadlift", createdAt = now, updatedAt = now)
        val newOneRepMax =
            mockUserOneRepMax(userId = "different-user-id", exerciseName = "Deadlift", oneRepMax = BigDecimal("150.0"), updatedAt = now)

        whenever(
            setSchemeDAL.updateSetScheme(
                anyLong(),
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(2L)).thenReturn(Mono.just("different-user-id"))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(2L)).thenReturn(Mono.just(programmedExercise))
        whenever(
            userOneRepMaxDAL.selectUserOneRepMax("different-user-id", "Deadlift")
        ).thenReturn(Mono.error(NoResultsFoundException("not found")))
        whenever(
            userOneRepMaxDAL.insertUserOneRepMax("different-user-id", "Deadlift", BigDecimal("150.0"))
        ).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                id = 2L,
                programmedExerciseId = 2L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = "150.0",
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                unit = "KG",
                band = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should update set scheme without 1RM update when performed weight is null`() {
        val setScheme = mockSetScheme(performedWeight = null)

        whenever(
            setSchemeDAL.updateSetScheme(
                anyLong(),
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                unit = "KG",
                band = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(programmedExerciseDAL, never()).selectProgrammedExerciseById(any())
        verify(userOneRepMaxDAL, never()).selectUserOneRepMax(any(), any())
    }

    @Test
    fun `should update set scheme without 1RM update when performed weight is not greater`() {
        val setScheme = mockSetScheme(performedWeight = BigDecimal("80.0"))
        val programmedExercise = mockProgrammedExercise(id = programmedExerciseId, exerciseName = exerciseName)
        val currentOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = BigDecimal("100.0"))

        whenever(
            setSchemeDAL.updateSetScheme(
                anyLong(),
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(userId))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(programmedExerciseId)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(currentOneRepMax))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = "80.0",
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                unit = "KG",
                band = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any(), any(), any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any(), any(), any())
    }

    @Test
    fun `should handle decimal one rep max values correctly`() {
        val setScheme = mockSetScheme(performedWeight = BigDecimal("120.5"))
        val programmedExercise = mockProgrammedExercise(id = programmedExerciseId, exerciseName = exerciseName)
        val currentOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = BigDecimal("120.0"))
        val newOneRepMax = mockUserOneRepMax(userId = userId, exerciseName = exerciseName, oneRepMax = BigDecimal("120.5"))

        whenever(
            setSchemeDAL.updateSetScheme(
                anyLong(),
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(userId))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(programmedExerciseId)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, BigDecimal("120.5"))).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetSchemeWithUnit(
                id = setSchemeId,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = "120.5",
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                unit = "KG",
                band = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should select set scheme by id`() {
        val setScheme = mockSetScheme()
        whenever(setSchemeDAL.selectSetSchemeById(setSchemeId)).thenReturn(Mono.just(setScheme))

        val result = setSchemeService.selectSetSchemeById(setSchemeId)

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should select set schemes by programmed exercise id`() {
        val setSchemes = listOf(mockSetScheme(), mockSetScheme())
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)).thenReturn(Mono.just(setSchemes))

        val result = setSchemeService.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)

        StepVerifier.create(result)
            .expectNext(setSchemes)
            .verifyComplete()
    }

    @Test
    fun `should select all set schemes`() {
        val setSchemes = listOf(mockSetScheme(), mockSetScheme(), mockSetScheme())
        whenever(setSchemeDAL.selectSetSchemes()).thenReturn(Mono.just(setSchemes))

        val result = setSchemeService.selectSetSchemes()

        StepVerifier.create(result)
            .expectNext(setSchemes)
            .verifyComplete()
    }

    @Test
    fun `should delete set scheme`() {
        val setScheme = mockSetScheme()
        whenever(setSchemeDAL.deleteSetScheme(setSchemeId)).thenReturn(Mono.just(setScheme))

        val result = setSchemeService.deleteSetScheme(setSchemeId)

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `isOwner returns true when user is owner`() {
        val setScheme = mockSetScheme(id = setSchemeId, programmedExerciseId = programmedExerciseId)
        val ownerUserId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        whenever(setSchemeDAL.selectSetSchemeById(setSchemeId)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(ownerUserId))

        val result = setSchemeService.isOwner(setSchemeId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(setSchemeDAL).selectSetSchemeById(setSchemeId)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `isOwner returns false when user is not owner`() {
        val setScheme = mockSetScheme(id = setSchemeId, programmedExerciseId = programmedExerciseId)
        val ownerUserId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userId = "different-user-id"
        whenever(setSchemeDAL.selectSetSchemeById(setSchemeId)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(ownerUserId))

        val result = setSchemeService.isOwner(setSchemeId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(setSchemeDAL).selectSetSchemeById(setSchemeId)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `isOwner returns false when set scheme not found`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        whenever(setSchemeDAL.selectSetSchemeById(setSchemeId)).thenReturn(Mono.error(NoResultsFoundException("not found")))

        val result = setSchemeService.isOwner(setSchemeId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(setSchemeDAL).selectSetSchemeById(setSchemeId)
    }

    @Test
    fun `isOwner returns false when programmed exercise not found`() {
        val setScheme = mockSetScheme(id = setSchemeId, programmedExerciseId = programmedExerciseId)
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        whenever(setSchemeDAL.selectSetSchemeById(setSchemeId)).thenReturn(Mono.just(setScheme))
        whenever(
            programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)
        ).thenReturn(Mono.error(NoResultsFoundException("not found")))

        val result = setSchemeService.isOwner(setSchemeId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(setSchemeDAL).selectSetSchemeById(setSchemeId)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `selectSetSchemesByUserId returns list of user owned set schemes`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userSetSchemes =
            listOf(
                mockSetScheme(id = 1L, programmedExerciseId = 1L, setNumber = 1),
                mockSetScheme(id = 2L, programmedExerciseId = 1L, setNumber = 2)
            )
        whenever(setSchemeDAL.selectSetSchemesByUserId(userId)).thenReturn(Mono.just(userSetSchemes))

        val result = setSchemeService.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectNext(userSetSchemes).verifyComplete()
        verify(setSchemeDAL).selectSetSchemesByUserId(userId)
    }

    @Test
    fun `selectSetSchemesByUserId returns empty list when user has no set schemes`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val emptyList = emptyList<SetScheme>()
        whenever(setSchemeDAL.selectSetSchemesByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = setSchemeService.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectNext(emptyList).verifyComplete()
        verify(setSchemeDAL).selectSetSchemesByUserId(userId)
    }

    @Test
    fun `selectSetSchemesByUserId propagates database errors`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val databaseError = RuntimeException("Database connection failed")
        whenever(setSchemeDAL.selectSetSchemesByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = setSchemeService.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectError(databaseError::class.java).verify()
        verify(setSchemeDAL).selectSetSchemesByUserId(userId)
    }
}
