package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockProgramPreferences
import com.congen.model.ProgramPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProgramPreferencesDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var programPreferencesDAL: ProgramPreferencesDAL

    private val programPreferences = mockProgramPreferences()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = mock()
        programPreferencesDAL = ProgramPreferencesDAL(postgresClient)
    }

    @Test
    fun `selectProgramPreferences should return program preferences`() {
        whenever(
            postgresClient.selectIndividual<ProgramPreferences>(
                "SELECT * FROM program_preferences WHERE program_id=$1",
                programPreferences.programId,
            ),
        ).thenReturn(Mono.just(programPreferences))
        val result = programPreferencesDAL.selectProgramPreferences(programPreferences.programId)
        StepVerifier.create(result).expectNext(programPreferences).verifyComplete()
        verify(
            postgresClient
        ).selectIndividual<ProgramPreferences>("SELECT * FROM program_preferences WHERE program_id=$1", programPreferences.programId)
    }

    @Test
    fun `selectProgramPreferences should return null when not found`() {
        val nonExistentProgramId = 999L
        whenever(
            postgresClient.selectIndividual<ProgramPreferences>(
                "SELECT * FROM program_preferences WHERE program_id=$1",
                nonExistentProgramId,
            ),
        ).thenReturn(Mono.empty())
        val result = programPreferencesDAL.selectProgramPreferences(nonExistentProgramId)
        StepVerifier.create(result).verifyComplete()
        verify(
            postgresClient
        ).selectIndividual<ProgramPreferences>("SELECT * FROM program_preferences WHERE program_id=$1", nonExistentProgramId)
    }

    @Test
    fun `insertProgramPreferences should return inserted program preferences`() {
        whenever(
            postgresClient.update<ProgramPreferences>(
                """
                INSERT INTO program_preferences
                    (program_id, program_days_per_week, session_time_length_in_minutes)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                programPreferences.programId,
                programPreferences.programDaysPerWeek,
                programPreferences.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(programPreferences))
        val result =
            programPreferencesDAL.insertProgramPreferences(
                programPreferences.programId,
                programPreferences.programDaysPerWeek,
                programPreferences.sessionTimeLengthInMinutes
            )
        StepVerifier.create(result).expectNext(programPreferences).verifyComplete()
        verify(postgresClient).update<ProgramPreferences>(
            """
            INSERT INTO program_preferences
                (program_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            programPreferences.programId,
            programPreferences.programDaysPerWeek,
            programPreferences.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `updateProgramPreferences should return updated program preferences`() {
        val updatedPrefs = mockProgramPreferences(sessionTimeLengthInMinutes = 90)
        val expectedQuery =
            """
            UPDATE program_preferences
            SET session_time_length_in_minutes=$2, updated_at=NOW()
            WHERE program_id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ProgramPreferences>(
                expectedQuery,
                updatedPrefs.programId,
                updatedPrefs.sessionTimeLengthInMinutes,
            ),
        ).thenReturn(Mono.just(updatedPrefs))
        val result =
            programPreferencesDAL.updateProgramPreferences(
                updatedPrefs.programId,
                updatedPrefs.sessionTimeLengthInMinutes
            )
        StepVerifier.create(result).expectNext(updatedPrefs).verifyComplete()
        verify(postgresClient).update<ProgramPreferences>(
            expectedQuery,
            updatedPrefs.programId,
            updatedPrefs.sessionTimeLengthInMinutes,
        )
    }

    @Test
    fun `deleteProgramPreferences should return deleted program preferences`() {
        val expectedQuery = "DELETE FROM program_preferences WHERE program_id=$1"
        whenever(
            postgresClient.update<ProgramPreferences>(
                expectedQuery,
                programPreferences.programId,
            ),
        ).thenReturn(Mono.just(programPreferences))
        val result = programPreferencesDAL.deleteProgramPreferences(programPreferences.programId)
        StepVerifier.create(result).expectNext(programPreferences).verifyComplete()
        verify(postgresClient).update<ProgramPreferences>(expectedQuery, programPreferences.programId)
    }
}
