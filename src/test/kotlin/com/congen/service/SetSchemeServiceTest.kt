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
import java.time.LocalDateTime

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
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        setSchemeDAL = mock()
        programmedExerciseDAL = mock()
        programDAL = mock()
        userOneRepMaxDAL = mock()
        setSchemeService = SetSchemeService(setSchemeDAL, programmedExerciseDAL, programDAL, userOneRepMaxDAL)
    }

    @Test
    fun `should insert set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            setSchemeDAL.insertSetScheme(
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeService.insertSetScheme(
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(
            programmedExerciseId = 1L,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = BigDecimal("100.0"),
            performedWeight = null,
            targetRepCount = null,
            performedRepCount = null,
            restSeconds = null,
        )
    }

    @Test
    fun `should update set scheme and update existing 1RM when performed weight is greater`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
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
                createdAt = now,
                updatedAt = now
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent",
                createdAt = now,
                updatedAt = now
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
                updatedAt = now
            )
        val newOneRepMax =
            UserOneRepMax(
                1,
                "Bench Press",
                BigDecimal("120.0"),
                now,
            )

        whenever(
            setSchemeDAL.updateSetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
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
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(1, "Bench Press", BigDecimal("120.0"))).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
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

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).updateSetScheme(
            id = 1L,
            programmedExerciseId = 1L,
            setNumber = 1,
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
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Bench Press")
        verify(userOneRepMaxDAL).updateUserOneRepMax(1, "Bench Press", BigDecimal("120.0"))
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
                updatedAt = now
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 2L,
                exerciseName = "Deadlift",
                position = 1,
                notes = "Heavy day",
                createdAt = now,
                updatedAt = now
            )
        val newOneRepMax =
            UserOneRepMax(
                2,
                "Deadlift",
                BigDecimal("150.0"),
                now,
            )

        whenever(
            setSchemeDAL.updateSetScheme(
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
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(2L)).thenReturn(Mono.just(2))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(2L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(2, "Deadlift")).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(userOneRepMaxDAL.insertUserOneRepMax(2, "Deadlift", BigDecimal("150.0"))).thenReturn(Mono.just(newOneRepMax))

        val result =
            setSchemeService.updateSetScheme(
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
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).updateSetScheme(
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
        )
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(2L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(2L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(2, "Deadlift")
        verify(userOneRepMaxDAL).insertUserOneRepMax(2, "Deadlift", BigDecimal("150.0"))
    }

    @Test
    fun `should delegate read operations to DAL without 1RM updates`() {
        // Given
        val setScheme =
            SetScheme(
                id = 1,
                programmedExerciseId = 1,
                setNumber = 1,
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
                createdAt = now,
                updatedAt = now
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
        verify(userOneRepMaxDAL, never()).updateUserOneRepMax(any(), any(), any())
        verify(userOneRepMaxDAL, never()).insertUserOneRepMax(any(), any(), any())
    }

    @Test
    fun `should handle decimal one rep max values correctly`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("120.5"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                createdAt = now,
                updatedAt = now
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Decimal test",
                createdAt = now,
                updatedAt = now
            )
        val currentOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("120.0"),
                updatedAt = now
            )

        whenever(
            setSchemeDAL.updateSetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("120.5"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )
        ).thenReturn(Mono.just(setScheme))
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.just(currentOneRepMax))
        whenever(userOneRepMaxDAL.updateUserOneRepMax(1, "Bench Press", BigDecimal("120.5"))).thenReturn(Mono.just(currentOneRepMax))

        val result =
            setSchemeService.updateSetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = BigDecimal("120.5"),
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(setSchemeDAL).updateSetScheme(
            id = 1L,
            programmedExerciseId = 1L,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = null,
            performedWeight = BigDecimal("120.5"),
            targetRepCount = null,
            performedRepCount = null,
            restSeconds = null
        )
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
        verify(userOneRepMaxDAL).selectUserOneRepMax(1, "Bench Press")
        verify(userOneRepMaxDAL).updateUserOneRepMax(1, "Bench Press", BigDecimal("120.5"))
    }

    @Test
    fun `getSetSchemeById should return set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeDAL.selectSetSchemeById(1L)).thenReturn(Mono.just(setScheme))
        val result = setSchemeService.selectSetSchemeById(1L)
        StepVerifier.create(result).expectNext(setScheme).verifyComplete()
        verify(setSchemeDAL).selectSetSchemeById(1L)
    }

    @Test
    fun `getSetSchemesByProgrammedExercise should return list of set schemes`() {
        val setSchemes =
            listOf(
                SetScheme(
                    id = 1L,
                    programmedExerciseId = 5L,
                    setNumber = 1,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("135.0"),
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180,
                    createdAt = now,
                    updatedAt = now
                ),
                SetScheme(
                    id = 2L,
                    programmedExerciseId = 5L,
                    setNumber = 2,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("135.0"),
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(5L)).thenReturn(Mono.just(setSchemes))
        val result = setSchemeService.selectSetSchemesByProgrammedExerciseId(5L)
        StepVerifier.create(result).expectNext(setSchemes).verifyComplete()
        verify(setSchemeDAL).selectSetSchemesByProgrammedExerciseId(5L)
    }

    @Test
    fun `createSetScheme should return created set scheme`() {
        val setScheme =
            SetScheme(
                id = 0L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 5L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Test exercise",
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(5L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(5L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(
            userOneRepMaxDAL.insertUserOneRepMax(1, "Bench Press", BigDecimal("135.0"))
        ).thenReturn(Mono.just(UserOneRepMax(1, "Bench Press", BigDecimal("135.0"), now)))
        whenever(
            setSchemeDAL.insertSetScheme(
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeService.insertSetScheme(
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )
        StepVerifier.create(result).expectNext(setScheme).verifyComplete()
        verify(setSchemeDAL).insertSetScheme(
            programmedExerciseId = 5L,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = BigDecimal("135.0"),
            performedWeight = null,
            targetRepCount = 5,
            performedRepCount = null,
            restSeconds = 180
        )
    }

    @Test
    fun `updateSetScheme should return updated set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = BigDecimal("140.0"),
                targetRepCount = 5,
                performedRepCount = 6,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        val programmedExercise =
            ProgrammedExercise(
                id = 5L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Test exercise",
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(5L)).thenReturn(Mono.just(1))
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(5L)).thenReturn(Mono.just(programmedExercise))
        whenever(userOneRepMaxDAL.selectUserOneRepMax(1, "Bench Press")).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(
            userOneRepMaxDAL.insertUserOneRepMax(1, "Bench Press", BigDecimal("140.0"))
        ).thenReturn(Mono.just(UserOneRepMax(1, "Bench Press", BigDecimal("140.0"), now)))
        whenever(
            setSchemeDAL.updateSetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = BigDecimal("140.0"),
                targetRepCount = 5,
                performedRepCount = 6,
                restSeconds = 180
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeService.updateSetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = BigDecimal("140.0"),
                targetRepCount = 5,
                performedRepCount = 6,
                restSeconds = 180
            )
        StepVerifier.create(result).expectNext(setScheme).verifyComplete()
        verify(setSchemeDAL).updateSetScheme(
            id = 1L,
            programmedExerciseId = 5L,
            setNumber = 1,
            isAmrap = true,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = BigDecimal("135.0"),
            performedWeight = BigDecimal("140.0"),
            targetRepCount = 5,
            performedRepCount = 6,
            restSeconds = 180
        )
    }

    @Test
    fun `deleteSetScheme should return deleted set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("135.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeDAL.deleteSetScheme(1L)).thenReturn(Mono.just(setScheme))
        val result = setSchemeService.deleteSetScheme(1L)
        StepVerifier.create(result).expectNext(setScheme).verifyComplete()
        verify(setSchemeDAL).deleteSetScheme(1L)
    }
}
