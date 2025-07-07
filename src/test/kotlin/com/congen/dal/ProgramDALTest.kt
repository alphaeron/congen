package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockProgram
import com.congen.model.Program
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProgramDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programDAL: ProgramDAL

    private val program = mockProgram()
    private val programs = listOf(program, mockProgram(id = 2L, name = "5/3/1 Program"))

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programDAL = ProgramDAL(postgresClient)
    }

    @Test
    fun `selectProgramById should return program`() {
        whenever(
            postgresClient.selectIndividual<Program>(
                "SELECT * FROM program WHERE id=$1",
                program.id,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.selectProgramById(program.id)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Program>(
            "SELECT * FROM program WHERE id=$1",
            program.id,
        )
    }

    @Test
    fun `selectPrograms should return list of programs`() {
        whenever(postgresClient.select<Program>("SELECT * FROM program ORDER BY name")).thenReturn(Mono.just(programs))

        val result = programDAL.selectPrograms()

        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()
        verify(postgresClient).select<Program>("SELECT * FROM program ORDER BY name")
    }

    @Test
    fun `insertProgram should return inserted program`() {
        val expectedQuery =
            """
            INSERT INTO program
                (user_id, name, current_week_number)
            VALUES
                ($1, $2, $3)
            """.trimIndent()

        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.userId,
                program.name,
                program.currentWeekNumber,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).update<Program>(
            expectedQuery,
            program.userId,
            program.name,
            program.currentWeekNumber,
        )
    }

    @Test
    fun `updateProgram should return updated program`() {
        val updatedProgram = mockProgram(name = "Updated Conjugate Program", currentWeekNumber = 2)
        val newName = "Test Program"
        val expectedQuery =
            """
            UPDATE program
            SET name=$2, current_week_number=$3, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                updatedProgram.id,
                newName,
                updatedProgram.currentWeekNumber,
            ),
        ).thenReturn(Mono.just(updatedProgram))

        val result = programDAL.updateProgram(updatedProgram.id, newName, updatedProgram.currentWeekNumber)

        StepVerifier.create(result)
            .expectNext(updatedProgram)
            .verifyComplete()
        verify(postgresClient).update<Program>(
            expectedQuery,
            updatedProgram.id,
            newName,
            updatedProgram.currentWeekNumber,
        )
    }

    @Test
    fun `deleteProgram should return deleted program`() {
        whenever(
            postgresClient.update<Program>(
                "DELETE FROM program WHERE id=$1",
                program.id,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.deleteProgram(program.id)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).update<Program>(
            "DELETE FROM program WHERE id=$1",
            program.id,
        )
    }
}
