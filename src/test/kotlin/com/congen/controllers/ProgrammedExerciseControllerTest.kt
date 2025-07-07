package com.congen.controllers

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ProgrammedExercise
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

/**
 * Unit tests for ProgrammedExerciseController.
 *
 * These tests verify the REST API endpoints for programmed exercise operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class ProgrammedExerciseControllerTest {
    @Mock
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    private lateinit var programmedExerciseController: ProgrammedExerciseController

    private val now = LocalDateTime.now()
    private val objectMapper = ObjectMapper()

    private lateinit var testProgrammedExercise: ProgrammedExercise

    @BeforeEach
    fun setUp() {
        programmedExerciseController = ProgrammedExerciseController(programmedExerciseDAL)
        testProgrammedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent",
                createdAt = now,
                updatedAt = now
            )
    }

    @Test
    fun `save should create new programmed exercise successfully`() {
        // Given
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.just(testProgrammedExercise))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "Bench Press", 1, "Focus on controlled descent")
    }

    @Test
    fun `save should handle null notes`() {
        // Given
        val exerciseWithNullNotes = testProgrammedExercise.copy(notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(5L, "Bench Press", 1, null))
            .thenReturn(Mono.just(exerciseWithNullNotes))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseWithNullNotes))
            .verifyComplete()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "Bench Press", 1, null)
    }

    @Test
    fun `save should handle validation errors`() {
        // Given
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(IllegalArgumentException("Invalid exercise name")))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "",
                position = 1,
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "", 1, "Focus on controlled descent")
    }

    @Test
    fun `get should return programmed exercise when found`() {
        // Given
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(testProgrammedExercise))

        // When
        val result = programmedExerciseController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `get should return not found when programmed exercise not found`() {
        // Given
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(999L))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = programmedExerciseController.get(999L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExerciseById(999L)
    }

    @Test
    fun `getByStage should return programmed exercises for stage`() {
        // Given
        val programmedExercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(
                    id = 2L,
                    exerciseName = "Squat",
                    position = 2,
                    notes = "Updated notes",
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(5L)).thenReturn(Mono.just(programmedExercises))

        // When
        val result = programmedExerciseController.getByStage(5L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(programmedExercises)
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExercisesByWorkoutStageId(5L)
    }

    @Test
    fun `getAll should return all programmed exercises`() {
        // Given
        val programmedExercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(
                    id = 2L,
                    exerciseName = "Squat",
                    position = 2,
                    notes = "Updated notes",
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(programmedExerciseDAL.selectProgrammedExercises()).thenReturn(Mono.just(programmedExercises))

        // When
        val result = programmedExerciseController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(programmedExercises)
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExercises()
    }
}
