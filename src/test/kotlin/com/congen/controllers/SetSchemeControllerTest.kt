package com.congen.controllers

import com.congen.mockSetScheme
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
import java.time.Instant

class SetSchemeControllerTest {
    @Mock
    private lateinit var setSchemeService: SetSchemeService

    private lateinit var setSchemeController: SetSchemeController

    companion object {
        private const val SCHEME_ID_1 = 1L
        private const val SCHEME_ID_2 = 2L
        private const val PROGRAMMED_EXERCISE_ID = 1L
        private const val SET_NUMBER_1 = 1
        private const val SET_NUMBER_2 = 2
        private const val TARGET_REP_COUNT = 5
        private const val REST_SECONDS_90 = 90
        private const val REST_SECONDS_120 = 120
        private const val NON_EXISTENT_ID = 999L
        private const val TARGET_WEIGHT_100 = "100.0"
        private const val TARGET_WEIGHT_110 = "110.0"
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        setSchemeController = SetSchemeController(setSchemeService)
    }

    @Test
    fun `should get all set schemes`() {
        val now = Instant.now()
        val setSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                ),
                mockSetScheme(
                    id = SCHEME_ID_2,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_2,
                    isAmrap = true,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    restSeconds = REST_SECONDS_90,
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
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.selectSetSchemeById(SCHEME_ID_1)).thenReturn(Mono.just(setScheme))
        val result = setSchemeController.get(SCHEME_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should return empty when set scheme not found`() {
        whenever(setSchemeService.selectSetSchemeById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = setSchemeController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            setSchemeService.insertSetScheme(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                BigDecimal(TARGET_WEIGHT_100),
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.save(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_100,
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should update set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_2,
                isAmrap = true,
                targetWeight = BigDecimal(TARGET_WEIGHT_110),
                restSeconds = REST_SECONDS_120,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            setSchemeService.updateSetScheme(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                BigDecimal(TARGET_WEIGHT_110),
                null,
                null,
                null,
                REST_SECONDS_120
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.update(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_110,
                null,
                null,
                null,
                REST_SECONDS_120
            )
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<SetScheme>)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should delete set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.deleteSetScheme(SCHEME_ID_1)).thenReturn(Mono.just(setScheme))
        val result = setSchemeController.delete(SCHEME_ID_1)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<SetScheme>)
            .expectNext(setScheme)
            .verifyComplete()
    }

    @Test
    fun `should get set schemes by programmed exercise`() {
        val now = Instant.now()
        val setSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                ),
                mockSetScheme(
                    id = SCHEME_ID_2,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_2,
                    isAmrap = true,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(setSchemeService.selectSetSchemesByProgrammedExerciseId(PROGRAMMED_EXERCISE_ID)).thenReturn(Mono.just(setSchemes))
        val result = setSchemeController.getByProgrammedExerciseId(PROGRAMMED_EXERCISE_ID)
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
