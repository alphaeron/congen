package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockProgram
import com.congen.model.Program
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
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
                (user_id, name, current_week_number, is_active)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()

        // Mock the deactivation to complete successfully
        whenever(
            postgresClient.updateLiteral<Unit>(
                "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
                Unit::class,
                program.userId
            )
        ).thenReturn(Mono.just(Unit))

        // Mock the insert to succeed
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.userId,
                program.name,
                program.currentWeekNumber,
                program.isActive,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber, program.isActive)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).updateLiteral<Unit>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
            Unit::class,
            program.userId
        )
        verify(postgresClient).update<Program>(
            expectedQuery,
            program.userId,
            program.name,
            program.currentWeekNumber,
            program.isActive,
        )
    }

    @Test
    fun `insertProgram should handle NoResultsFoundException when deactivating programs`() {
        val expectedQuery =
            """
            INSERT INTO program
                (user_id, name, current_week_number, is_active)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()

        // Mock the deactivation to throw NoResultsFoundException (no programs to deactivate)
        whenever(
            postgresClient.updateLiteral<Unit>(
                "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
                Unit::class,
                program.userId
            )
        ).thenReturn(Mono.error(NoResultsFoundException("No programs found to deactivate")))

        // Mock the insert to succeed
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.userId,
                program.name,
                program.currentWeekNumber,
                program.isActive,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber, program.isActive)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).updateLiteral<Unit>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
            Unit::class,
            program.userId
        )
        verify(postgresClient).update<Program>(
            expectedQuery,
            program.userId,
            program.name,
            program.currentWeekNumber,
            program.isActive,
        )
    }

    @Test
    fun `insertProgram should insert program directly when not active`() {
        val expectedQuery =
            """
            INSERT INTO program
                (user_id, name, current_week_number, is_active)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()

        // Mock the insert to succeed for inactive program
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.userId,
                program.name,
                program.currentWeekNumber,
                false,
            ),
        ).thenReturn(Mono.just(program.copy(isActive = false)))

        val result = programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber, false)

        StepVerifier.create(result)
            .expectNext(program.copy(isActive = false))
            .verifyComplete()
        // Should not call updateLiteral since program is not active
        verify(postgresClient).update<Program>(
            expectedQuery,
            program.userId,
            program.name,
            program.currentWeekNumber,
            false,
        )
    }

    @Test
    fun `insertProgram should deactivate existing programs before inserting new one`() {
        val expectedQuery =
            """
            INSERT INTO program
                (user_id, name, current_week_number, is_active)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()

        // Mock the deactivation to complete successfully
        whenever(
            postgresClient.updateLiteral<Unit>(
                "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
                Unit::class,
                program.userId
            )
        ).thenReturn(Mono.just(Unit))

        // Mock the insert to succeed
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                program.userId,
                program.name,
                program.currentWeekNumber,
                program.isActive,
            ),
        ).thenReturn(Mono.just(program))

        val result = programDAL.insertProgram(program.userId, program.name, program.currentWeekNumber, program.isActive)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
        verify(postgresClient).updateLiteral<Unit>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
            Unit::class,
            program.userId
        )
        verify(postgresClient).update<Program>(
            expectedQuery,
            program.userId,
            program.name,
            program.currentWeekNumber,
            program.isActive,
        )
    }

    @Test
    fun `updateProgram should return updated program when setting to inactive`() {
        val updatedProgram = mockProgram(name = "Updated Conjugate Program", currentWeekNumber = 2, isActive = false)
        val newName = "Test Program"
        val expectedQuery =
            """
            UPDATE program
            SET name=$2, current_week_number=$3, is_active=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<Program>(
                expectedQuery,
                updatedProgram.id,
                newName,
                updatedProgram.currentWeekNumber,
                false,
            ),
        ).thenReturn(Mono.just(updatedProgram))

        val result = programDAL.updateProgram(updatedProgram.id, newName, updatedProgram.currentWeekNumber, false)

        StepVerifier.create(result)
            .expectNext(updatedProgram)
            .verifyComplete()
        verify(postgresClient).update<Program>(
            expectedQuery,
            updatedProgram.id,
            newName,
            updatedProgram.currentWeekNumber,
            false,
        )
    }

    @Test
    fun `updateProgram should deactivate other programs when setting to active`() {
        val programToUpdate = mockProgram(name = "Program to Activate", isActive = true)
        val newName = "Updated Program Name"
        val expectedUpdateQuery =
            """
            UPDATE program
            SET name=$2, current_week_number=$3, is_active=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        val expectedDeactivateQuery = "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1 AND id != $2"

        // Mock selectProgramById to return the program
        whenever(
            postgresClient.selectIndividual<Program>(
                "SELECT * FROM program WHERE id=$1",
                programToUpdate.id
            )
        ).thenReturn(Mono.just(programToUpdate))

        // Mock deactivateOtherProgramsForUser
        whenever(
            postgresClient.updateLiteral<Unit>(
                expectedDeactivateQuery,
                Unit::class,
                programToUpdate.userId,
                programToUpdate.id
            )
        ).thenReturn(Mono.just(Unit))

        // Mock the final update
        whenever(
            postgresClient.update<Program>(
                expectedUpdateQuery,
                programToUpdate.id,
                newName,
                programToUpdate.currentWeekNumber,
                true,
            ),
        ).thenReturn(Mono.just(programToUpdate))

        val result = programDAL.updateProgram(programToUpdate.id, newName, programToUpdate.currentWeekNumber, true)

        StepVerifier.create(result)
            .expectNext(programToUpdate)
            .verifyComplete()

        // Verify that selectProgramById was called
        verify(postgresClient).selectIndividual<Program>(
            "SELECT * FROM program WHERE id=$1",
            programToUpdate.id
        )

        // Verify that deactivateOtherProgramsForUser was called
        verify(postgresClient).updateLiteral<Unit>(
            expectedDeactivateQuery,
            Unit::class,
            programToUpdate.userId,
            programToUpdate.id
        )

        // Verify that the final update was called
        verify(postgresClient).update<Program>(
            expectedUpdateQuery,
            programToUpdate.id,
            newName,
            programToUpdate.currentWeekNumber,
            true,
        )
    }

    @Test
    fun `updateProgram should handle NoResultsFoundException when deactivating programs`() {
        val programToUpdate = mockProgram(name = "Program to Activate", isActive = true)
        val newName = "Updated Program Name"
        val expectedUpdateQuery =
            """
            UPDATE program
            SET name=$2, current_week_number=$3, is_active=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        val expectedDeactivateQuery = "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1 AND id != $2"

        // Mock selectProgramById to return the program
        whenever(
            postgresClient.selectIndividual<Program>(
                "SELECT * FROM program WHERE id=$1",
                programToUpdate.id
            )
        ).thenReturn(Mono.just(programToUpdate))

        // Mock deactivateOtherProgramsForUser to throw NoResultsFoundException
        whenever(
            postgresClient.updateLiteral<Unit>(
                expectedDeactivateQuery,
                Unit::class,
                programToUpdate.userId,
                programToUpdate.id
            )
        ).thenReturn(Mono.error(NoResultsFoundException("No programs found to deactivate")))

        // Mock the final update
        whenever(
            postgresClient.update<Program>(
                expectedUpdateQuery,
                programToUpdate.id,
                newName,
                programToUpdate.currentWeekNumber,
                true,
            ),
        ).thenReturn(Mono.just(programToUpdate))

        val result = programDAL.updateProgram(programToUpdate.id, newName, programToUpdate.currentWeekNumber, true)

        StepVerifier.create(result)
            .expectNext(programToUpdate)
            .verifyComplete()

        // Verify that selectProgramById was called
        verify(postgresClient).selectIndividual<Program>(
            "SELECT * FROM program WHERE id=$1",
            programToUpdate.id
        )

        // Verify that deactivateOtherProgramsForUser was called
        verify(postgresClient).updateLiteral<Unit>(
            expectedDeactivateQuery,
            Unit::class,
            programToUpdate.userId,
            programToUpdate.id
        )

        // Verify that the final update was called
        verify(postgresClient).update<Program>(
            expectedUpdateQuery,
            programToUpdate.id,
            newName,
            programToUpdate.currentWeekNumber,
            true,
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

    @Test
    fun `selectProgramsByUserId should return programs for user without filter`() {
        whenever(
            postgresClient.select<Program>(
                "SELECT * FROM program WHERE user_id=$1 ORDER BY name",
                program.userId
            )
        ).thenReturn(Mono.just(programs))

        val result = programDAL.selectProgramsByUserId(program.userId)

        StepVerifier.create(result)
            .expectNext(programs)
            .verifyComplete()
        verify(postgresClient).select<Program>(
            "SELECT * FROM program WHERE user_id=$1 ORDER BY name",
            program.userId
        )
    }

    @Test
    fun `selectProgramsByUserId should return active programs for user`() {
        val activePrograms = listOf(mockProgram(isActive = true))
        whenever(
            postgresClient.select<Program>(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(activePrograms))

        val result = programDAL.selectProgramsByUserId(program.userId, true)

        StepVerifier.create(result)
            .expectNext(activePrograms)
            .verifyComplete()
        verify(postgresClient).select<Program>(
            "SELECT * FROM program WHERE user_id=$1 AND is_active=$2 ORDER BY name",
            program.userId,
            true
        )
    }

    @Test
    fun `selectProgramsByUserId should return inactive programs for user`() {
        val inactivePrograms = listOf(mockProgram(isActive = false))
        whenever(
            postgresClient.select<Program>(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(inactivePrograms))

        val result = programDAL.selectProgramsByUserId(program.userId, false)

        StepVerifier.create(result)
            .expectNext(inactivePrograms)
            .verifyComplete()
        verify(postgresClient).select<Program>(
            "SELECT * FROM program WHERE user_id=$1 AND is_active=$2 ORDER BY name",
            program.userId,
            false
        )
    }
}
