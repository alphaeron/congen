package com.congen.controllers

import com.congen.dal.ProgrammedWorkoutDAL
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
import java.time.LocalDateTime

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

    @BeforeEach
    fun setUp() {
        programmedWorkoutDAL = mock()
        programmedWorkoutController = ProgrammedWorkoutController(programmedWorkoutDAL)
    }

    @Test
    fun `should get all programmed workouts`() {
        val now = LocalDateTime.now()
        val programmedWorkouts = listOf(
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Workout 1",
                createdAt = now,
                updatedAt = now
            ),
            ProgrammedWorkout(
                id = 2L,
                programId = 1L,
                dayNumber = 2,
                name = "Workout 2",
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
        val now = LocalDateTime.now()
        val programmedWorkout = ProgrammedWorkout(
            id = 1L,
            programId = 1L,
            dayNumber = 1,
            name = "Test Workout",
            createdAt = now,
            updatedAt = now
        )

        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(1L)).thenReturn(Mono.just(programmedWorkout))

        val result = programmedWorkoutController.get(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(1L)
    }

    @Test
    fun `should return not found when programmed workout not found`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(999L)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = programmedWorkoutController.get(999L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(999L)
    }

    @Test
    fun `should create programmed workout`() {
        val now = LocalDateTime.now()
        val programId = 1L
        val dayNumber = 1
        val name = "New Workout"
        val programmedWorkout = ProgrammedWorkout(
            id = 0L,
            programId = programId,
            dayNumber = dayNumber,
            name = name,
            createdAt = now,
            updatedAt = now
        )
        val savedProgrammedWorkout = programmedWorkout.copy(id = 1L)
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(programId, dayNumber, name)).thenReturn(Mono.just(savedProgrammedWorkout))

        val result = programmedWorkoutController.save(programId, dayNumber, name)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedProgrammedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(programId, dayNumber, name)
    }

    @Test
    fun `should update programmed workout`() {
        val now = LocalDateTime.now()
        val id = 1L
        val programId = 1L
        val dayNumber = 2
        val name = "Updated Workout"
        val programmedWorkout = ProgrammedWorkout(
            id = id,
            programId = programId,
            dayNumber = dayNumber,
            name = name,
            createdAt = now,
            updatedAt = now
        )
        whenever(programmedWorkoutDAL.updateProgrammedWorkout(id, programId, dayNumber, name)).thenReturn(Mono.just(programmedWorkout))

        val result = programmedWorkoutController.update(id, programId, dayNumber, name)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).updateProgrammedWorkout(id, programId, dayNumber, name)
    }

    @Test
    fun `should return not found when updating non-existent programmed workout`() {
        whenever(programmedWorkoutDAL.updateProgrammedWorkout(999L, 1L, 1, "Test")).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = programmedWorkoutController.update(999L, 1L, 1, "Test")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).updateProgrammedWorkout(999L, 1L, 1, "Test")
    }

    @Test
    fun `should delete programmed workout`() {
        val now = LocalDateTime.now()
        val programmedWorkout = ProgrammedWorkout(
            id = 1L,
            programId = 1L,
            dayNumber = 1,
            name = "Test Workout",
            createdAt = now,
            updatedAt = now
        )
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(1L)).thenReturn(Mono.just(programmedWorkout))

        val result = programmedWorkoutController.delete(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).deleteProgrammedWorkout(1L)
    }

    @Test
    fun `should return not found when deleting non-existent programmed workout`() {
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(999L)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = programmedWorkoutController.delete(999L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).deleteProgrammedWorkout(999L)
    }

    @Test
    fun `should get programmed workouts by program`() {
        val now = LocalDateTime.now()
        val programId = 1L
        val programmedWorkouts = listOf(
            ProgrammedWorkout(
                id = 1L,
                programId = programId,
                dayNumber = 1,
                name = "Workout 1",
                createdAt = now,
                updatedAt = now
            ),
            ProgrammedWorkout(
                id = 2L,
                programId = programId,
                dayNumber = 2,
                name = "Workout 2",
                createdAt = now,
                updatedAt = now
            )
        )

        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programId)).thenReturn(Mono.just(programmedWorkouts))

        val result = programmedWorkoutController.getByProgramId(programId)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(programmedWorkouts)
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(programId)
    }

    @Test
    fun `should return empty list when no programmed workouts for program`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(999L)).thenReturn(Mono.just(emptyList()))

        val result = programmedWorkoutController.getByProgramId(999L)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(emptyList<ProgrammedWorkout>())
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(999L)
    }

    @Test
    fun `should handle DAL error gracefully`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkouts()).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = programmedWorkoutController.getAll()

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedWorkoutDAL).selectProgrammedWorkouts()
    }
}
