package com.congen.controllers

import com.congen.model.SetScheme
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class SetSchemeControllerTest {
    @Mock
    private lateinit var setSchemeService: SetSchemeService

    @InjectMocks
    private lateinit var setSchemeController: SetSchemeController

    private lateinit var testSetScheme: SetScheme

    @BeforeEach
    fun setUp() {
        testSetScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
    }

    @Test
    fun `save should create new set scheme successfully`() {
        // Given
        whenever(
            setSchemeService.insertSetScheme(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(testSetScheme))

        // When
        val result =
            setSchemeController.save(
                programmedExerciseId = 5L,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = "100.0",
                performedWeight = "100.0",
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testSetScheme))
            .verifyComplete()

        verify(setSchemeService).insertSetScheme(
            5L,
            1,
            true,
            false,
            false,
            true,
            "3",
            "1",
            "1",
            BigDecimal("100.0"),
            BigDecimal("100.0"),
            5,
            5,
            180
        )
    }

    @Test
    fun `save should handle null weight values`() {
        // Given
        val setSchemeWithNullWeights =
            testSetScheme.copy(
                targetWeight = null,
                performedWeight = null
            )
        whenever(
            setSchemeService.insertSetScheme(
                5L,
                1,
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        ).thenReturn(Mono.just(setSchemeWithNullWeights))

        // When
        val result =
            setSchemeController.save(
                programmedExerciseId = 5L,
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
                restSeconds = null
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setSchemeWithNullWeights))
            .verifyComplete()

        verify(setSchemeService).insertSetScheme(
            5L,
            1,
            true,
            false,
            false,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    @Test
    fun `save should handle invalid weight string values`() {
        // Given
        whenever(
            setSchemeService.insertSetScheme(
                5L,
                1,
                true,
                false,
                false,
                true,
                "3",
                "1",
                "1",
                null,
                null,
                5,
                5,
                180
            )
        ).thenReturn(Mono.just(testSetScheme))

        // When
        val result =
            setSchemeController.save(
                programmedExerciseId = 5L,
                setNumber = 1,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = "invalid",
                performedWeight = "invalid",
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testSetScheme))
            .verifyComplete()

        verify(setSchemeService).insertSetScheme(
            5L,
            1,
            true,
            false,
            false,
            true,
            "3",
            "1",
            "1",
            null,
            null,
            5,
            5,
            180
        )
    }

    @Test
    fun `get should return set scheme by id`() {
        // Given
        whenever(setSchemeService.selectSetSchemeById(1L)).thenReturn(Mono.just(testSetScheme))

        // When
        val result = setSchemeController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testSetScheme))
            .verifyComplete()

        verify(setSchemeService).selectSetSchemeById(1L)
    }

    @Test
    fun `get should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(setSchemeService.selectSetSchemeById(1L)).thenReturn(Mono.error(error))

        // When
        val result = setSchemeController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(setSchemeService).selectSetSchemeById(1L)
    }

    @Test
    fun `getAll should return all set schemes`() {
        // Given
        val setSchemes = listOf(testSetScheme, testSetScheme.copy(id = 2L))
        whenever(setSchemeService.selectSetSchemes()).thenReturn(Mono.just(setSchemes))

        // When
        val result = setSchemeController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<SetScheme>>)
            .expectNext(setSchemes)
            .verifyComplete()

        verify(setSchemeService).selectSetSchemes()
    }

    @Test
    fun `getByProgrammedExerciseId should return set schemes for exercise`() {
        // Given
        val setSchemes = listOf(testSetScheme, testSetScheme.copy(id = 2L, setNumber = 2))
        whenever(setSchemeService.selectSetSchemesByProgrammedExerciseId(5L)).thenReturn(Mono.just(setSchemes))

        // When
        val result = setSchemeController.getByProgrammedExerciseId(5L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<SetScheme>>)
            .expectNext(setSchemes)
            .verifyComplete()

        verify(setSchemeService).selectSetSchemesByProgrammedExerciseId(5L)
    }

    @Test
    fun `update should update set scheme successfully`() {
        // Given
        val updatedSetScheme =
            testSetScheme.copy(
                setNumber = 2,
                targetWeight = BigDecimal("110.0"),
                performedWeight = BigDecimal("110.0")
            )
        whenever(
            setSchemeService.updateSetScheme(
                1L,
                5L,
                2,
                true,
                false,
                false,
                true,
                "3",
                "1",
                "1",
                BigDecimal("110.0"),
                BigDecimal("110.0"),
                5,
                5,
                180
            )
        ).thenReturn(Mono.just(updatedSetScheme))

        // When
        val result =
            setSchemeController.update(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 2,
                wasSetPerformed = true,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = "110.0",
                performedWeight = "110.0",
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<SetScheme>)
            .expectNext(updatedSetScheme)
            .verifyComplete()

        verify(setSchemeService).updateSetScheme(
            1L,
            5L,
            2,
            true,
            false,
            false,
            true,
            "3",
            "1",
            "1",
            BigDecimal("110.0"),
            BigDecimal("110.0"),
            5,
            5,
            180
        )
    }

    @Test
    fun `update should handle null weight values`() {
        // Given
        val updatedSetScheme =
            testSetScheme.copy(
                targetWeight = null,
                performedWeight = null
            )
        whenever(
            setSchemeService.updateSetScheme(
                1L,
                5L,
                1,
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        ).thenReturn(Mono.just(updatedSetScheme))

        // When
        val result =
            setSchemeController.update(
                id = 1L,
                programmedExerciseId = 5L,
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
                restSeconds = null
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<SetScheme>)
            .expectNext(updatedSetScheme)
            .verifyComplete()

        verify(setSchemeService).updateSetScheme(
            1L,
            5L,
            1,
            true,
            false,
            false,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    @Test
    fun `delete should delete set scheme successfully`() {
        // Given
        whenever(setSchemeService.deleteSetScheme(1L)).thenReturn(Mono.just(testSetScheme))

        // When
        val result = setSchemeController.delete(1L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<SetScheme>)
            .expectNext(testSetScheme)
            .verifyComplete()

        verify(setSchemeService).deleteSetScheme(1L)
    }
}
