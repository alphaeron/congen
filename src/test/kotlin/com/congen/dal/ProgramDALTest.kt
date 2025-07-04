package com.congen.dal

import com.congen.client.PostgresClient
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

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programDAL = ProgramDAL(postgresClient)
    }

    @Test
    fun `selectProgramById should return program`() {
        // Given
        val programId = 1L
        val program =
            Program(
                id = programId,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )

        whenever(
            postgresClient.selectIndividual<Program>(
                "SELECT * FROM program WHERE id=$1",
                programId,
            ),
        ).thenReturn(Mono.just(program))

        // When
        val result = programDAL.selectProgramById(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(postgresClient).selectIndividual<Program>(
            "SELECT * FROM program WHERE id=$1",
            programId,
        )
    }

    @Test
    fun `selectPrograms should return list of programs`() {
        // Given
        val programs =
            listOf(
                Program(
                    id = 1L,
                    name = "Conjugate Powerlifting Program",
                    description = "A comprehensive conjugate powerlifting program",
                ),
                Program(
                    id = 2L,
                    name = "5/3/1 Program",
                    description = "A strength building program",
                ),
            )

        whenever(postgresClient.select<Program>("SELECT * FROM program ORDER BY name")).thenReturn(Mono.just(programs))

        // When
        val result = programDAL.selectPrograms()

        // Then
        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()

        verify(postgresClient).select<Program>("SELECT * FROM program ORDER BY name")
    }

    @Test
    fun `insertProgram should return inserted program`() {
        // Given
        val program =
            Program(
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )

        val expectedQuery =
            """
            INSERT INTO program
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.name,
                program.description,
            ),
        ).thenReturn(Mono.just(program))

        // When
        val result = programDAL.insertProgram(program)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(postgresClient).update<Program>(
            expectedQuery,
            program.name,
            program.description,
        )
    }

    @Test
    fun `updateProgram should return updated program`() {
        // Given
        val program =
            Program(
                id = 1L,
                name = "Updated Conjugate Program",
                description = "Updated description",
            )

        val expectedQuery =
            """
            UPDATE program
            SET name=$2, description=$3
            WHERE id=$1
            """.trimIndent()

        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.id,
                program.name,
                program.description,
            ),
        ).thenReturn(Mono.just(program))

        // When
        val result = programDAL.updateProgram(program)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(postgresClient).update<Program>(
            expectedQuery,
            program.id,
            program.name,
            program.description,
        )
    }

    @Test
    fun `deleteProgram should return deleted program`() {
        // Given
        val programId = 1L
        val program =
            Program(
                id = programId,
                name = "Conjugate Powerlifting Program",
                description = "A comprehensive conjugate powerlifting program",
            )

        whenever(
            postgresClient.update<Program>(
                "DELETE FROM program WHERE id=$1",
                programId,
            ),
        ).thenReturn(Mono.just(program))

        // When
        val result = programDAL.deleteProgram(programId)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(postgresClient).update<Program>(
            "DELETE FROM program WHERE id=$1",
            programId,
        )
    }
}
