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

    @BeforeEach
    fun setUp() {
        testProgram =
            Program(
                id = 1L,
                userId = 123,
                name = "Conjugate Powerlifting - Week 1",
                currentWeekNumber = 1,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
    }

    @Test
    fun `generateNextWeek should generate workout program successfully`() {
        // Given
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.just(testProgram))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(123, 1)
    }

    @Test
    fun `generateNextWeek should use default week number when not provided`() {
        // Given
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.just(testProgram))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(123, 1)
    }

    @Test
    fun `generateNextWeek should return bad request for invalid week number`() {
        // Given
        // No service call needed since validation happens in controller

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 0
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()

        // Service should not be called for invalid parameters
        verify(conjugateWorkoutGeneratorService, never()).generateNextWeek(123, 0)
    }

    @Test
    fun `generateNextWeek should return bad request for negative week number`() {
        // Given
        // No service call needed since validation happens in controller

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = -1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()

        // Service should not be called for invalid parameters
        verify(conjugateWorkoutGeneratorService, never()).generateNextWeek(123, -1)
    }

    @Test
    fun `generateNextWeek should return 404 when user not found`() {
        // Given
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(NoResultsFoundException("User not found")))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 999,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(999, 1)
    }

    @Test
    fun `generateNextWeek should return bad request for IllegalArgumentException`() {
        // Given
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(IllegalArgumentException("Invalid parameters")))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build())
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(123, 1)
    }

    @Test
    fun `generateNextWeek should return unprocessable entity for other errors`() {
        // Given
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(RuntimeException("Generation failed")))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.unprocessableEntity().build())
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(123, 1)
    }

    @Test
    fun `generateNextWeek should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any(), any()))
            .thenReturn(Mono.error(error))

        // When
        val result =
            conjugateWorkoutGeneratorController.generateNextWeek(
                userId = 123,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.unprocessableEntity().build())
            .verifyComplete()

        verify(conjugateWorkoutGeneratorService).generateNextWeek(123, 1)
    }
}
