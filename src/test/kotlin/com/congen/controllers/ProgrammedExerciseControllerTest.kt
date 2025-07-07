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
import java.time.Instant

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

    private val now = Instant.now()
    private val objectMapper = ObjectMapper()

    private lateinit var testProgrammedExercise: ProgrammedExercise

    companion object {
        private const val EXERCISE_ID_1 = 1L
        private const val EXERCISE_ID_2 = 2L
        private const val WORKOUT_STAGE_ID = 5L
        private const val BENCH_PRESS = "Bench Press"
        private const val SQUAT = "Squat"
        private const val POSITION_1 = 1
        private const val POSITION_2 = 2
        private const val NOTES = "Focus on controlled descent"
        private const val UPDATED_NOTES = "Updated notes"
        private const val NON_EXISTENT_ID = 999L
        private const val EMPTY_EXERCISE_NAME = ""
    }

    @BeforeEach
    fun setUp() {
        programmedExerciseController = ProgrammedExerciseController(programmedExerciseDAL)
        testProgrammedExercise =
            ProgrammedExercise(
                id = EXERCISE_ID_1,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES,
                createdAt = now,
                updatedAt = now
            )
    }

    @Test
    fun `save should create new programmed exercise successfully`() {
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.just(testProgrammedExercise))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()
        verify(programmedExerciseDAL).insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, NOTES)
    }

    @Test
    fun `save should handle null notes`() {
        val exerciseWithNullNotes = testProgrammedExercise.copy(notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, null))
            .thenReturn(Mono.just(exerciseWithNullNotes))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = null
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseWithNullNotes))
            .verifyComplete()
        verify(programmedExerciseDAL).insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, null)
    }

    @Test
    fun `save should handle validation errors`() {
        whenever(programmedExerciseDAL.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(IllegalArgumentException("Invalid exercise name")))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = EMPTY_EXERCISE_NAME,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()
        verify(programmedExerciseDAL).insertProgrammedExercise(WORKOUT_STAGE_ID, EMPTY_EXERCISE_NAME, POSITION_1, NOTES)
    }

    @Test
    fun `get should return programmed exercise when found`() {
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(EXERCISE_ID_1)).thenReturn(Mono.just(testProgrammedExercise))
        val result = programmedExerciseController.get(EXERCISE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExerciseById(EXERCISE_ID_1)
    }

    @Test
    fun `get should return not found when programmed exercise not found`() {
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(NON_EXISTENT_ID))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = programmedExerciseController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExerciseById(NON_EXISTENT_ID)
    }

    @Test
    fun `getByStage should return programmed exercises for stage`() {
        val programmedExercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(
                    id = EXERCISE_ID_2,
                    exerciseName = SQUAT,
                    position = POSITION_2,
                    notes = UPDATED_NOTES,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(
            programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID)
        ).thenReturn(Mono.just(programmedExercises))
        val result = programmedExerciseController.getByStage(WORKOUT_STAGE_ID)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(programmedExercises)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID)
    }

    @Test
    fun `getAll should return all programmed exercises`() {
        val programmedExercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(
                    id = EXERCISE_ID_2,
                    exerciseName = SQUAT,
                    position = POSITION_2,
                    notes = UPDATED_NOTES,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(programmedExerciseDAL.selectProgrammedExercises()).thenReturn(Mono.just(programmedExercises))
        val result = programmedExerciseController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedExercise>>)
            .expectNext(programmedExercises)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercises()
    }
}
