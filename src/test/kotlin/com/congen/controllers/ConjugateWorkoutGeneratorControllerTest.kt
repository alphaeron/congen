package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import com.congen.service.ConjugateWorkoutGeneratorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class ConjugateWorkoutGeneratorControllerTest {
    @Mock
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService

    @InjectMocks
    private lateinit var conjugateWorkoutGeneratorController: ConjugateWorkoutGeneratorController

    private lateinit var testProgram: Program

    companion object {
        private const val USER_ID = 123
        private const val INVALID_USER_ID = 999
        private const val CURRENT_WEEK = 1
        private const val INVALID_WEEK_ZERO = 0
        private const val INVALID_WEEK_NEGATIVE = -1
        private const val PROGRAM_ID = 1L
        private const val PROGRAM_NAME = "Conjugate Powerlifting - Week 1"
    }

    @BeforeEach
    fun setUp() {
        testProgram =
            Program(
                id = PROGRAM_ID,
                userId = USER_ID,
                name = PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
    }

    @Test
    fun `generateNextWeek should generate workout program successfully`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.just(testProgram))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(USER_ID, CURRENT_WEEK)
    }

    @Test
    fun `generateNextWeek should use default week number when not provided`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.just(testProgram))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(USER_ID, CURRENT_WEEK)
    }

    @Test
    fun `generateNextWeek should return bad request for invalid week number`() {
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, INVALID_WEEK_ZERO)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService, never()).generateNextWeek(USER_ID, INVALID_WEEK_ZERO)
    }

    @Test
    fun `generateNextWeek should return bad request for negative week number`() {
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, INVALID_WEEK_NEGATIVE)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService, never()).generateNextWeek(USER_ID, INVALID_WEEK_NEGATIVE)
    }

    @Test
    fun `generateNextWeek should return 404 when user not found`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(NoResultsFoundException("User not found")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(INVALID_USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(INVALID_USER_ID, CURRENT_WEEK)
    }

    @Test
    fun `generateNextWeek should return bad request for IllegalArgumentException`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(IllegalArgumentException("Invalid parameters")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(USER_ID, CURRENT_WEEK)
    }

    @Test
    fun `generateNextWeek should return unprocessable entity for other errors`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(RuntimeException("Generation failed")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.unprocessableEntity().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(USER_ID, CURRENT_WEEK)
    }

    @Test
    fun `generateNextWeek should handle service error`() {
        val error = RuntimeException("Database error")
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(error))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(USER_ID, CURRENT_WEEK)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.unprocessableEntity().build())
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(USER_ID, CURRENT_WEEK)
    }
}
