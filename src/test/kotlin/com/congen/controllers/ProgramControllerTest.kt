package com.congen.controllers

import com.congen.dal.ProgramDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

/**
 * Unit tests for ProgramController.
 *
 * These tests verify the REST API endpoints for program operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ProgramControllerTest {
    @Mock
    private lateinit var programDAL: ProgramDAL

    private lateinit var programController: ProgramController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        programController = ProgramController(programDAL)
    }

    @Test
    fun `save should return created program`() {
        // Given
        val program =
            Program(
                id = 1L,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val savedProgram = program.copy(id = 2L)

        whenever(programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber)).thenReturn(Mono.just(savedProgram))

        // When
        val result = programController.save(program.userId, program.name, program.currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedProgram))
            .verifyComplete()

        verify(programDAL).insertProgram(program.userId, program.name, program.currentWeekNumber)
    }

    @Test
    fun `get should return program when found`() {
        // Given
        val programId = 1L
        val program =
            Program(
                id = programId,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        whenever(programDAL.selectProgramById(programId)).thenReturn(Mono.just(program))

        // When
        val result = programController.get(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()

        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `get should return not found when program not found`() {
        // Given
        val programId = 999L

        whenever(
            programDAL.selectProgramById(programId),
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM program WHERE id=$1")))

        // When
        val result = programController.get(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `getAll should return all programs`() {
        // Given
        val programs =
            listOf(
                Program(
                    id = 1L,
                    userId = 1,
                    name = "Conjugate Powerlifting Program",
                    currentWeekNumber = 1,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                Program(
                    id = 2L,
                    userId = 1,
                    name = "5/3/1 Program",
                    currentWeekNumber = 1,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
            )

        whenever(programDAL.selectPrograms()).thenReturn(Mono.just(programs))

        // When
        val result = programController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Program>>)
            .expectNext(programs)
            .verifyComplete()

        verify(programDAL).selectPrograms()
    }

    @Test
    fun `update should return updated program when found`() {
        // Given
        val programId = 2L
        val program =
            Program(
                id = 1,
                userId = 1,
                name = "Updated Conjugate Program",
                currentWeekNumber = 2,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val updatedProgram = program.copy(id = programId)

        whenever(programDAL.updateProgram(programId, program.name, program.currentWeekNumber)).thenReturn(Mono.just(updatedProgram))

        // When
        val result = programController.update(programId, program.name, program.currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedProgram))
            .verifyComplete()

        verify(programDAL).updateProgram(programId, program.name, program.currentWeekNumber)
    }

    @Test
    fun `update should return not found when program not found`() {
        // Given
        val programId = 999L
        val program =
            Program(
                id = 1,
                userId = 1,
                name = "Updated Conjugate Program",
                currentWeekNumber = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val updatedProgram = program.copy(id = programId)

        whenever(
            programDAL.updateProgram(programId, program.name, program.currentWeekNumber)
        ).thenReturn(Mono.error(NoResultsFoundException("UPDATE program WHERE id=$1")))

        // When
        val result = programController.update(programId, program.name, program.currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programDAL).updateProgram(programId, program.name, program.currentWeekNumber)
    }

    @Test
    fun `delete should return deleted program when found`() {
        // Given
        val programId = 1L
        val program =
            Program(
                id = programId,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        whenever(programDAL.deleteProgram(programId)).thenReturn(Mono.just(program))

        // When
        val result = programController.delete(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()

        verify(programDAL).deleteProgram(programId)
    }

    @Test
    fun `delete should return not found when program not found`() {
        // Given
        val programId = 999L

        whenever(programDAL.deleteProgram(programId)).thenReturn(Mono.error(NoResultsFoundException("DELETE FROM program WHERE id=$1")))

        // When
        val result = programController.delete(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programDAL).deleteProgram(programId)
    }
}
