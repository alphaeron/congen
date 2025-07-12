package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.Program
import com.congen.service.ConjugateWorkoutGeneratorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
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
        private const val CURRENT_WEEK = 1
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
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.just(testProgram))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 404 when program not found`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(NoResultsFoundException("Program not found")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 422 for validation error`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(ValidationException("Invalid program parameters")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 500 for unexpected error`() {
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(RuntimeException("Unexpected error")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }
}
