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
import java.time.Instant

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

    companion object {
        private const val PROGRAM_ID_1 = 1L
        private const val PROGRAM_ID_2 = 2L
        private const val USER_ID = 1
        private const val CURRENT_WEEK = 1
        private const val UPDATED_WEEK = 2
        private const val NON_EXISTENT_ID = 999L
        private const val CONJUGATE_PROGRAM_NAME = "Conjugate Powerlifting Program"
        private const val FIVE_THREE_ONE_PROGRAM = "5/3/1 Program"
        private const val UPDATED_PROGRAM_NAME = "Updated Conjugate Program"
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        programController = ProgramController(programDAL)
    }

    @Test
    fun `save should return created program`() {
        val program =
            Program(
                id = PROGRAM_ID_1,
                userId = USER_ID,
                name = CONJUGATE_PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
        val savedProgram =
            Program(
                id = PROGRAM_ID_2,
                userId = USER_ID,
                name = CONJUGATE_PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = program.createdAt,
                updatedAt = program.updatedAt,
                isActive = true
            )
        whenever(programDAL.insertProgram(USER_ID, CONJUGATE_PROGRAM_NAME, 1, true)).thenReturn(Mono.just(savedProgram))
        val result = programController.save(USER_ID, CONJUGATE_PROGRAM_NAME, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedProgram))
            .verifyComplete()
        verify(programDAL).insertProgram(USER_ID, CONJUGATE_PROGRAM_NAME, 1, true)
    }

    @Test
    fun `get should return program when found`() {
        val program =
            Program(
                id = PROGRAM_ID_1,
                userId = USER_ID,
                name = CONJUGATE_PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
        whenever(programDAL.selectProgramById(PROGRAM_ID_1)).thenReturn(Mono.just(program))
        val result = programController.get(PROGRAM_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()
        verify(programDAL).selectProgramById(PROGRAM_ID_1)
    }

    @Test
    fun `get should return not found when program not found`() {
        whenever(
            programDAL.selectProgramById(NON_EXISTENT_ID)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM program WHERE id=$1")))
        val result = programController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programDAL).selectProgramById(NON_EXISTENT_ID)
    }

    @Test
    fun `getAll should return all programs`() {
        val programs =
            listOf(
                Program(
                    id = PROGRAM_ID_1,
                    userId = USER_ID,
                    name = CONJUGATE_PROGRAM_NAME,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = true
                ),
                Program(
                    id = PROGRAM_ID_2,
                    userId = USER_ID,
                    name = FIVE_THREE_ONE_PROGRAM,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = false
                )
            )
        whenever(programDAL.selectPrograms()).thenReturn(Mono.just(programs))
        val result = programController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Program>>)
            .expectNext(programs)
            .verifyComplete()
        verify(programDAL).selectPrograms()
    }

    @Test
    fun `update should return updated program when found`() {
        val program =
            Program(
                id = PROGRAM_ID_1,
                userId = USER_ID,
                name = UPDATED_PROGRAM_NAME,
                currentWeekNumber = UPDATED_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
        val updatedProgram =
            Program(
                id = PROGRAM_ID_2,
                userId = USER_ID,
                name = UPDATED_PROGRAM_NAME,
                currentWeekNumber = UPDATED_WEEK,
                createdAt = program.createdAt,
                updatedAt = program.updatedAt,
                isActive = true
            )
        whenever(programDAL.updateProgram(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)).thenReturn(Mono.just(updatedProgram))
        val result = programController.update(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedProgram))
            .verifyComplete()
        verify(programDAL).updateProgram(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)
    }

    @Test
    fun `update should return not found when program not found`() {
        val program =
            Program(
                id = PROGRAM_ID_1,
                userId = USER_ID,
                name = UPDATED_PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
        whenever(programDAL.updateProgram(NON_EXISTENT_ID, UPDATED_PROGRAM_NAME, CURRENT_WEEK, true))
            .thenReturn(Mono.error(NoResultsFoundException("UPDATE program WHERE id=$1")))
        val result = programController.update(NON_EXISTENT_ID, UPDATED_PROGRAM_NAME, CURRENT_WEEK, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programDAL).updateProgram(NON_EXISTENT_ID, UPDATED_PROGRAM_NAME, CURRENT_WEEK, true)
    }

    @Test
    fun `delete should return deleted program when found`() {
        val program =
            Program(
                id = PROGRAM_ID_1,
                userId = USER_ID,
                name = CONJUGATE_PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
        whenever(programDAL.deleteProgram(PROGRAM_ID_1)).thenReturn(Mono.just(program))
        val result = programController.delete(PROGRAM_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()
        verify(programDAL).deleteProgram(PROGRAM_ID_1)
    }

    @Test
    fun `delete should return not found when program not found`() {
        whenever(
            programDAL.deleteProgram(NON_EXISTENT_ID)
        ).thenReturn(Mono.error(NoResultsFoundException("DELETE FROM program WHERE id=$1")))
        val result = programController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(programDAL).deleteProgram(NON_EXISTENT_ID)
    }

    @Test
    fun `getByUserId should return programs for user without filter`() {
        val programs =
            listOf(
                Program(
                    id = PROGRAM_ID_1,
                    userId = USER_ID,
                    name = CONJUGATE_PROGRAM_NAME,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = true
                ),
                Program(
                    id = PROGRAM_ID_2,
                    userId = USER_ID,
                    name = FIVE_THREE_ONE_PROGRAM,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = false
                )
            )
        whenever(programDAL.selectProgramsByUserId(USER_ID, null)).thenReturn(Mono.just(programs))
        val result = programController.getByUserId(USER_ID, null)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Program>>)
            .expectNext(programs)
            .verifyComplete()
        verify(programDAL).selectProgramsByUserId(USER_ID, null)
    }

    @Test
    fun `getByUserId should return active programs for user`() {
        val activePrograms =
            listOf(
                Program(
                    id = PROGRAM_ID_1,
                    userId = USER_ID,
                    name = CONJUGATE_PROGRAM_NAME,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = true
                )
            )
        whenever(programDAL.selectProgramsByUserId(USER_ID, true)).thenReturn(Mono.just(activePrograms))
        val result = programController.getByUserId(USER_ID, true)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Program>>)
            .expectNext(activePrograms)
            .verifyComplete()
        verify(programDAL).selectProgramsByUserId(USER_ID, true)
    }

    @Test
    fun `getByUserId should return inactive programs for user`() {
        val inactivePrograms =
            listOf(
                Program(
                    id = PROGRAM_ID_2,
                    userId = USER_ID,
                    name = FIVE_THREE_ONE_PROGRAM,
                    currentWeekNumber = CURRENT_WEEK,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isActive = false
                )
            )
        whenever(programDAL.selectProgramsByUserId(USER_ID, false)).thenReturn(Mono.just(inactivePrograms))
        val result = programController.getByUserId(USER_ID, false)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<Program>>)
            .expectNext(inactivePrograms)
            .verifyComplete()
        verify(programDAL).selectProgramsByUserId(USER_ID, false)
    }
}
