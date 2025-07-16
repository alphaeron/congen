package com.congen.controllers

import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.model.ProgrammedWorkout
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for ProgrammedWorkoutController.
 *
 * These tests verify the REST API endpoints for programmed workout operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ProgrammedWorkoutControllerTest {
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var programmedWorkoutController: ProgrammedWorkoutController

    companion object {
        private const val WORKOUT_ID_1 = 1L
        private const val WORKOUT_ID_2 = 2L
        private const val PROGRAM_ID = 1L
        private const val DAY_NUMBER_1 = 1
        private const val DAY_NUMBER_2 = 2
        private const val NON_EXISTENT_ID = 999L
        private const val WORKOUT_NAME_1 = "Workout 1"
        private const val WORKOUT_NAME_2 = "Workout 2"
        private const val TEST_WORKOUT = "Test Workout"
        private const val NEW_WORKOUT = "New Workout"
        private const val UPDATED_WORKOUT = "Updated Workout"
        private const val TEST_NAME = "Test"
    }

    @BeforeEach
    fun setUp() {
        programmedWorkoutDAL = mock()
        programmedWorkoutController = ProgrammedWorkoutController(programmedWorkoutDAL)
    }

    @Test
    fun `should get all programmed workouts`() {
        val now = Instant.now()
        val programmedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = WORKOUT_ID_2,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_2,
                    name = WORKOUT_NAME_2,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkouts()).thenReturn(Mono.just(programmedWorkouts))
        val result = programmedWorkoutController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(programmedWorkouts)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkouts()
    }

    @Test
    fun `should get programmed workout by id`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = TEST_WORKOUT,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(WORKOUT_ID_1)).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.get(WORKOUT_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(WORKOUT_ID_1)
    }

    @Test
    fun `should return not found when programmed workout not found`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(NON_EXISTENT_ID)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(NON_EXISTENT_ID)
    }

    @Test
    fun `should create programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = 0L,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = NEW_WORKOUT,
                createdAt = now,
                updatedAt = now
            )
        val savedProgrammedWorkout = programmedWorkout.copy(id = WORKOUT_ID_1)
        whenever(
            programmedWorkoutDAL.insertProgrammedWorkout(PROGRAM_ID, DAY_NUMBER_1, NEW_WORKOUT)
        ).thenReturn(Mono.just(savedProgrammedWorkout))
        val result = programmedWorkoutController.save(PROGRAM_ID, DAY_NUMBER_1, NEW_WORKOUT)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedProgrammedWorkout))
            .verifyComplete()
        verify(programmedWorkoutDAL).insertProgrammedWorkout(PROGRAM_ID, DAY_NUMBER_1, NEW_WORKOUT)
    }

    @Test
    fun `should update programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_2,
                name = UPDATED_WORKOUT,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            programmedWorkoutDAL.updateProgrammedWorkout(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, UPDATED_WORKOUT)
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.update(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, UPDATED_WORKOUT)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutDAL).updateProgrammedWorkout(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, UPDATED_WORKOUT)
    }

    @Test
    fun `should return not found when updating non-existent programmed workout`() {
        whenever(
            programmedWorkoutDAL.updateProgrammedWorkout(NON_EXISTENT_ID, PROGRAM_ID, DAY_NUMBER_1, TEST_NAME)
        ).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.update(NON_EXISTENT_ID, PROGRAM_ID, DAY_NUMBER_1, TEST_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programmedWorkoutDAL).updateProgrammedWorkout(NON_EXISTENT_ID, PROGRAM_ID, DAY_NUMBER_1, TEST_NAME)
    }

    @Test
    fun `should delete programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = TEST_WORKOUT,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(WORKOUT_ID_1)).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.delete(WORKOUT_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutDAL).deleteProgrammedWorkout(WORKOUT_ID_1)
    }

    @Test
    fun `should return not found when deleting non-existent programmed workout`() {
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(NON_EXISTENT_ID)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programmedWorkoutDAL).deleteProgrammedWorkout(NON_EXISTENT_ID)
    }

    @Test
    fun `should get programmed workouts by program`() {
        val now = Instant.now()
        val programmedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = WORKOUT_ID_2,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_2,
                    name = WORKOUT_NAME_2,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(PROGRAM_ID)).thenReturn(Mono.just(programmedWorkouts))
        val result = programmedWorkoutController.getByProgramId(PROGRAM_ID)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(programmedWorkouts)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(PROGRAM_ID)
    }

    @Test
    fun `should return empty list when no programmed workouts for program`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(NON_EXISTENT_ID)).thenReturn(Mono.just(emptyList()))
        val result = programmedWorkoutController.getByProgramId(NON_EXISTENT_ID)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(emptyList<ProgrammedWorkout>())
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(NON_EXISTENT_ID)
    }

    @Test
    fun `should handle DAL error gracefully`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkouts()).thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = programmedWorkoutController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedWorkoutDAL).selectProgrammedWorkouts()
    }
}
