package com.congen.controllers

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ProgrammedExercise
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

@ExtendWith(MockitoExtension::class)
class ProgrammedExerciseControllerTest {
    @Mock
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    @InjectMocks
    private lateinit var programmedExerciseController: ProgrammedExerciseController

    private lateinit var testProgrammedExercise: ProgrammedExercise

    @BeforeEach
    fun setUp() {
        testProgrammedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )
    }

    @Test
    fun `save should create new programmed exercise successfully`() {
        // Given
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any()))
            .thenReturn(Mono.just(testProgrammedExercise))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "Bench Press", "Focus on controlled descent")
    }

    @Test
    fun `save should handle null notes`() {
        // Given
        val exerciseWithNullNotes = testProgrammedExercise.copy(notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(5L, "Bench Press", null))
            .thenReturn(Mono.just(exerciseWithNullNotes))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseWithNullNotes))
            .verifyComplete()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "Bench Press", null)
    }

    @Test
    fun `save should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any()))
            .thenReturn(Mono.error(error))

        // When
        val result =
            programmedExerciseController.save(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedExerciseDAL).insertProgrammedExercise(5L, "Bench Press", "Focus on controlled descent")
    }

    @Test
    fun `get should return programmed exercise by id`() {
        // Given
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L))
            .thenReturn(Mono.just(testProgrammedExercise))

        // When
        val result = programmedExerciseController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `get should return 404 when exercise not found`() {
        // Given
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        // When
        val result = programmedExerciseController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `get should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L))
            .thenReturn(Mono.error(error))

        // When
        val result = programmedExerciseController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `getByStage should return programmed exercises for stage`() {
        // Given
        val exercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(id = 2L, exerciseName = "Squat")
            )
        whenever(programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(5L))
            .thenReturn(Mono.just(exercises))

        // When
        val result = programmedExerciseController.getByStage(5L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(exercises)
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExercisesByWorkoutStageId(5L)
    }

    @Test
    fun `getAll should return all programmed exercises`() {
        // Given
        val exercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(id = 2L, exerciseName = "Squat")
            )
        whenever(programmedExerciseDAL.selectProgrammedExercises())
            .thenReturn(Mono.just(exercises))

        // When
        val result = programmedExerciseController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(exercises)
            .verifyComplete()

        verify(programmedExerciseDAL).selectProgrammedExercises()
    }

    @Test
    fun `update should update programmed exercise successfully`() {
        // Given
        val updatedExercise =
            testProgrammedExercise.copy(
                exerciseName = "Incline Bench Press",
                notes = "Use 30 degree incline"
            )
        whenever(programmedExerciseDAL.updateProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.just(updatedExercise))

        // When
        val result =
            programmedExerciseController.update(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Incline Bench Press",
                notes = "Use 30 degree incline"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).updateProgrammedExercise(1L, 5L, "Incline Bench Press", "Use 30 degree incline")
    }

    @Test
    fun `update should return 404 when exercise not found`() {
        // Given
        whenever(programmedExerciseDAL.updateProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        // When
        val result =
            programmedExerciseController.update(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedExerciseDAL).updateProgrammedExercise(1L, 5L, "Bench Press", "Focus on controlled descent")
    }

    @Test
    fun `update should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(programmedExerciseDAL.updateProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(error))

        // When
        val result =
            programmedExerciseController.update(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedExerciseDAL).updateProgrammedExercise(1L, 5L, "Bench Press", "Focus on controlled descent")
    }

    @Test
    fun `delete should delete programmed exercise successfully`() {
        // Given
        whenever(programmedExerciseDAL.deleteProgrammedExercise(1L))
            .thenReturn(Mono.just(testProgrammedExercise))

        // When
        val result = programmedExerciseController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()

        verify(programmedExerciseDAL).deleteProgrammedExercise(1L)
    }

    @Test
    fun `delete should return 404 when exercise not found`() {
        // Given
        whenever(programmedExerciseDAL.deleteProgrammedExercise(1L))
            .thenReturn(Mono.error(NoResultsFoundException("Exercise not found")))

        // When
        val result = programmedExerciseController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedExerciseDAL).deleteProgrammedExercise(1L)
    }

    @Test
    fun `delete should handle service error`() {
        // Given
        val error = RuntimeException("Database error")
        whenever(programmedExerciseDAL.deleteProgrammedExercise(1L))
            .thenReturn(Mono.error(error))

        // When
        val result = programmedExerciseController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedExerciseDAL).deleteProgrammedExercise(1L)
    }
}
