package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ProgrammedExercise
import com.congen.model.SetScheme
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
    private lateinit var programDAL: ProgramDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var setSchemeService: SetSchemeService

    @BeforeEach
    fun setUp() {
        setSchemeDAL = mock()
        programmedExerciseDAL = mock()
        programDAL = mock()
        userOneRepMaxDAL = mock()
        setSchemeService = SetSchemeService(setSchemeDAL, programmedExerciseDAL, programDAL, userOneRepMaxDAL)
    }

    @Test
    fun `should insert set scheme and update 1RM when performed weight exceeds current 1RM`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("110.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Bench Press",
                notes = null,
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(any())).thenReturn(Mono.just(currentOneRepMax))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(setScheme)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Bench Press")
        verify(userOneRepMaxDAL).updateUserOneRepMax(any())
    }

    @Test
    fun `should insert set scheme and create new 1RM when no existing 1RM`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("100.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Squat",
                notes = null,
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Squat")).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(userOneRepMaxDAL.insertUserOneRepMax(any())).thenReturn(Mono.just(UserOneRepMax(1, "Squat", BigDecimal("100.0"))))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(setScheme)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Squat")
        verify(userOneRepMaxDAL).insertUserOneRepMax(any())
    }

    @Test
    fun `should insert set scheme without 1RM update when performed weight is less than current 1RM`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("90.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Bench Press",
                notes = null,
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.just(currentOneRepMax))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(setScheme)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Bench Press")

        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any())
    }

    @Test
    fun `should insert set scheme without 1RM update when set was not performed`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("110.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(setScheme)

        verify(programmedExerciseDAL, never()).getUserIdFromProgrammedExercise(any())
        verify(programmedExerciseDAL, never()).selectProgrammedExerciseById(any())
        verify(userOneRepMaxDAL, never()).selectUserOneRepMax(any(), any())
        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any())
    }

    @Test
    fun `should insert set scheme without 1RM update when performed weight is null`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
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
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(setScheme)

        verify(programmedExerciseDAL, never()).getUserIdFromProgrammedExercise(any())
        verify(programmedExerciseDAL, never()).selectProgrammedExerciseById(any())
        verify(userOneRepMaxDAL, never()).selectUserOneRepMax(any(), any())
        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any())
    }

    @Test
    fun `should update set scheme and update 1RM when performed weight exceeds current 1RM`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("120.0"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Deadlift",
                notes = null,
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("110.0"),
            )

        whenever(setSchemeDAL.updateSetScheme(setScheme)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Deadlift")).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(any())).thenReturn(Mono.just(currentOneRepMax))

        // When
        val result = setSchemeService.updateSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).updateSetScheme(setScheme)
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Deadlift")
        verify(userOneRepMaxDAL).updateUserOneRepMax(any())
    }

    @Test
    fun `should delegate read operations to DAL without 1RM updates`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
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
            )

        whenever(setSchemeDAL.selectSetSchemeById(1L)).thenReturn(Mono.just(setScheme))
        whenever(setSchemeDAL.selectSetSchemes()).thenReturn(Mono.just(listOf(setScheme)))
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(1L)).thenReturn(Mono.just(listOf(setScheme)))
        whenever(setSchemeDAL.deleteSetScheme(1L)).thenReturn(Mono.just(setScheme))

        // When & Then
        StepVerifier.create(setSchemeService.selectSetSchemeById(1L))
            .expectNext(setScheme)
            .verifyComplete()

        StepVerifier.create(setSchemeService.selectSetSchemes())
            .expectNext(listOf(setScheme))
            .verifyComplete()

        StepVerifier.create(setSchemeService.selectSetSchemesByProgrammedExerciseId(1L))
            .expectNext(listOf(setScheme))
            .verifyComplete()

        StepVerifier.create(setSchemeService.deleteSetScheme(1L))
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).selectSetSchemeById(1L)
        verify(setSchemeDAL).selectSetSchemes()
        verify(setSchemeDAL).selectSetSchemesByProgrammedExerciseId(1L)
        verify(setSchemeDAL).deleteSetScheme(1L)

        verify(programmedExerciseDAL, never()).getUserIdFromProgrammedExercise(any())
        verify(programmedExerciseDAL, never()).selectProgrammedExerciseById(any())
        verify(userOneRepMaxDAL, never()).selectUserOneRepMax(any(), any())
        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any())
    }

    @Test
    fun `should handle decimal one rep max values correctly`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("225.5"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = "Deadlift",
                notes = null,
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("225.0"),
            )

        whenever(setSchemeDAL.insertSetScheme(setScheme)).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Deadlift")).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(any())).thenReturn(Mono.just(currentOneRepMax))

        // When
        val result = setSchemeService.insertSetScheme(setScheme)

        // Then
        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(userOneRepMaxDAL).updateUserOneRepMax(any())
    }
}
