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
import java.time.Instant

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
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
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
                    userId = 1,
                    name = "Conjugate Powerlifting Program",
                    currentWeekNumber = 1,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                ),
                Program(
                    id = 2L,
                    userId = 1,
                    name = "5/3/1 Program",
                    currentWeekNumber = 1,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
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
                id = 1,
                userId = 1,
                name = "Test Program",
                currentWeekNumber = 1,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

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

        // When
        val result = programDAL.insertProgram(1, "Test Program", 1)

        // Then
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
        // Given
        val program =
            Program(
                id = 1L,
                userId = 1,
                name = "Updated Conjugate Program",
                currentWeekNumber = 2,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

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
                program.id,
                newName,
                program.currentWeekNumber,
            ),
        ).thenReturn(Mono.just(program))

        // When
        val result = programDAL.updateProgram(1L, newName, 2)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(postgresClient).update<Program>(
            expectedQuery,
            program.id,
            newName,
            program.currentWeekNumber,
        )
    }

    @Test
    fun `deleteProgram should return deleted program`() {
        // Given
        val programId = 1L
        val now = Instant.now()
        val program =
            Program(
                id = programId,
                userId = 1,
                name = "Conjugate Powerlifting Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
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
