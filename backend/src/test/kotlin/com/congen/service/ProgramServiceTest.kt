package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ProgramServiceTest {
    @Mock
    private lateinit var programDAL: ProgramDAL

    private lateinit var programService: ProgramService

    private val testProgram =
        Program(
            id = 1L,
            userId = 123,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

    @BeforeEach
    fun setUp() {
        programService = ProgramService(programDAL)
    }

    @Test
    fun `isOwner should return true when user owns the program`() {
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.just(testProgram))

        val result = programService.isOwner(1L, "123")

        assertTrue(result)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `isOwner should return false when user does not own the program`() {
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.just(testProgram))

        val result = programService.isOwner(1L, "456")

        assertFalse(result)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `isOwner should return false when program not found`() {
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.error(NoResultsFoundException("Program not found")))

        val exception =
            assertThrows<RuntimeException> {
                programService.isOwner(1L, "123")
            }

        assertTrue(exception.cause is NoResultsFoundException)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `getProgramById should delegate to DAL`() {
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.just(testProgram))

        val result = programService.getProgramById(1L)

        StepVerifier.create(result)
            .expectNext(testProgram)
            .verifyComplete()
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `getAllPrograms should delegate to DAL`() {
        val programs = listOf(testProgram)
        whenever(programDAL.selectPrograms()).thenReturn(Mono.just(programs))

        val result = programService.getAllPrograms()

        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()
        verify(programDAL).selectPrograms()
    }

    @Test
    fun `getProgramsByUserId should delegate to DAL with userId only`() {
        val programs = listOf(testProgram)
        whenever(programDAL.selectProgramsByUserId(123, null)).thenReturn(Mono.just(programs))

        val result = programService.getProgramsByUserId(123)

        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()
        verify(programDAL).selectProgramsByUserId(123, null)
    }

    @Test
    fun `getProgramsByUserId should delegate to DAL with userId and isActive`() {
        val programs = listOf(testProgram)
        whenever(programDAL.selectProgramsByUserId(123, true)).thenReturn(Mono.just(programs))

        val result = programService.getProgramsByUserId(123, true)

        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()
        verify(programDAL).selectProgramsByUserId(123, true)
    }

    @Test
    fun `createProgram should delegate to DAL`() {
        whenever(programDAL.insertProgram(123, "Test Program", 1, true)).thenReturn(Mono.just(testProgram))

        val result = programService.createProgram(123, "Test Program", 1, true)

        StepVerifier.create(result)
            .expectNext(testProgram)
            .verifyComplete()
        verify(programDAL).insertProgram(123, "Test Program", 1, true)
    }

    @Test
    fun `createProgram should use default isActive value`() {
        whenever(programDAL.insertProgram(123, "Test Program", 1, true)).thenReturn(Mono.just(testProgram))

        val result = programService.createProgram(123, "Test Program", 1)

        StepVerifier.create(result)
            .expectNext(testProgram)
            .verifyComplete()
        verify(programDAL).insertProgram(123, "Test Program", 1, true)
    }

    @Test
    fun `updateProgram should delegate to DAL`() {
        whenever(programDAL.updateProgram(1L, "Updated Program", 2, false)).thenReturn(Mono.just(testProgram))

        val result = programService.updateProgram(1L, "Updated Program", 2, false)

        StepVerifier.create(result)
            .expectNext(testProgram)
            .verifyComplete()
        verify(programDAL).updateProgram(1L, "Updated Program", 2, false)
    }

    @Test
    fun `deleteProgram should delegate to DAL`() {
        whenever(programDAL.deleteProgram(1L)).thenReturn(Mono.just(testProgram))

        val result = programService.deleteProgram(1L)

        StepVerifier.create(result)
            .expectNext(testProgram)
            .verifyComplete()
        verify(programDAL).deleteProgram(1L)
    }
}
