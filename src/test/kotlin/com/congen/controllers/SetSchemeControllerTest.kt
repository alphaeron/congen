package com.congen.controllers

import com.congen.model.SetScheme
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class SetSchemeControllerTest {
    @Mock
    private lateinit var setSchemeService: SetSchemeService

    private lateinit var setSchemeController: SetSchemeController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        setSchemeController = SetSchemeController(setSchemeService)
    }

    @Test
    fun `should get all set schemes`() {
        val now = LocalDateTime.now()
        val setSchemes =
            listOf(
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
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 90,
                    createdAt = now,
                    updatedAt = now
                ),
                SetScheme(
                    id = 2L,
                    programmedExerciseId = 1L,
                    setNumber = 2,
                    isAmrap = true,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = null,
                    targetRepCount = null,
                    performedRepCount = null,
                    restSeconds = 90,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(setSchemeService.selectSetSchemes()).thenReturn(Mono.just(setSchemes))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == setSchemes)
            }
            .verifyComplete()
    }

    @Test
    fun `should get set scheme by id`() {
        val now = LocalDateTime.now()
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
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 90,
                createdAt = now,
                updatedAt = now
            )

        whenever(setSchemeService.selectSetSchemeById(1L)).thenReturn(Mono.just(setScheme))

        val result = setSchemeController.get(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should return empty when set scheme not found`() {
        whenever(setSchemeService.selectSetSchemeById(999L)).thenReturn(Mono.empty())

        val result = setSchemeController.get(999L)

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create set scheme`() {
        val now = LocalDateTime.now()
        val programmedExerciseId = 1L
        val setNumber = 1
        val isAmrap = false
        val isEmom = false
        val useTempo = false
        val eccentricTempo: String? = null
        val isometricTempo: String? = null
        val concentricTempo: String? = null
        val targetWeight = "100.0"
        val performedWeight: String? = null
        val targetRepCount = 5
        val performedRepCount: Int? = null
        val restSeconds = 90
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = isAmrap,
                isEmom = isEmom,
                useTempo = useTempo,
                eccentricTempo = eccentricTempo,
                isometricTempo = isometricTempo,
                concentricTempo = concentricTempo,
                targetWeight = BigDecimal("100.0"),
                performedWeight = performedWeight?.toBigDecimalOrNull(),
                targetRepCount = targetRepCount,
                performedRepCount = performedRepCount,
                restSeconds = restSeconds,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            setSchemeService.insertSetScheme(
                programmedExerciseId,
                setNumber,
                isAmrap,
                isEmom,
                useTempo,
                eccentricTempo,
                isometricTempo,
                concentricTempo,
                BigDecimal("100.0"),
                null,
                targetRepCount,
                performedRepCount,
                restSeconds
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeController.save(
                programmedExerciseId,
                setNumber,
                isAmrap,
                isEmom,
                useTempo,
                eccentricTempo,
                isometricTempo,
                concentricTempo,
                targetWeight,
                performedWeight,
                targetRepCount,
                performedRepCount,
                restSeconds
            )

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should update set scheme`() {
        val now = LocalDateTime.now()
        val id = 1L
        val programmedExerciseId = 1L
        val setNumber = 2
        val isAmrap = true
        val isEmom = false
        val useTempo = false
        val eccentricTempo: String? = null
        val isometricTempo: String? = null
        val concentricTempo: String? = null
        val targetWeight = "110.0"
        val performedWeight: String? = null
        val targetRepCount: Int? = null
        val performedRepCount: Int? = null
        val restSeconds = 120
        val setScheme =
            SetScheme(
                id = id,
                programmedExerciseId = programmedExerciseId,
                setNumber = setNumber,
                isAmrap = isAmrap,
                isEmom = isEmom,
                useTempo = useTempo,
                eccentricTempo = eccentricTempo,
                isometricTempo = isometricTempo,
                concentricTempo = concentricTempo,
                targetWeight = BigDecimal("110.0"),
                performedWeight = performedWeight?.toBigDecimalOrNull(),
                targetRepCount = targetRepCount,
                performedRepCount = performedRepCount,
                restSeconds = restSeconds,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            setSchemeService.updateSetScheme(
                id,
                programmedExerciseId,
                setNumber,
                isAmrap,
                isEmom,
                useTempo,
                eccentricTempo,
                isometricTempo,
                concentricTempo,
                BigDecimal("110.0"),
                null,
                targetRepCount,
                performedRepCount,
                restSeconds
            )
        ).thenReturn(Mono.just(setScheme))

        val result =
            setSchemeController.update(
                id,
                programmedExerciseId,
                setNumber,
                isAmrap,
                isEmom,
                useTempo,
                eccentricTempo,
                isometricTempo,
                concentricTempo,
                targetWeight,
                performedWeight,
                targetRepCount,
                performedRepCount,
                restSeconds
            )

        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<SetScheme>)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should delete set scheme`() {
        val now = LocalDateTime.now()
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
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 90,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.deleteSetScheme(1L)).thenReturn(Mono.just(setScheme))

        val result = setSchemeController.delete(1L)

        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<SetScheme>)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should get set schemes by programmed exercise`() {
        val now = LocalDateTime.now()
        val programmedExerciseId = 1L
        val setSchemes =
            listOf(
                SetScheme(
                    id = 1L,
                    programmedExerciseId = programmedExerciseId,
                    setNumber = 1,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 90,
                    createdAt = now,
                    updatedAt = now
                ),
                SetScheme(
                    id = 2L,
                    programmedExerciseId = programmedExerciseId,
                    setNumber = 2,
                    isAmrap = true,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = null,
                    targetRepCount = null,
                    performedRepCount = null,
                    restSeconds = 90,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(setSchemeService.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)).thenReturn(Mono.just(setSchemes))

        val result = setSchemeController.getByProgrammedExerciseId(programmedExerciseId)

        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<List<SetScheme>>)
            .expectNext(setSchemes)
            .verifyComplete()
    }

    @Test
    fun `should handle service error gracefully`() {
        whenever(setSchemeService.selectSetSchemes()).thenThrow(RuntimeException("Service error"))

        val result = setSchemeController.getAll()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
