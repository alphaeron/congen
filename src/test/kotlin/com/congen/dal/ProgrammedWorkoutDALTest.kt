package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
import com.congen.mockProgrammedWorkout
import com.congen.model.ProgrammedWorkout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProgrammedWorkoutDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL

    private val programmedWorkout = mockProgrammedWorkout()
    private val programmedWorkoutList =
        listOf(
            programmedWorkout,
            mockProgrammedWorkout(id = 2L, dayNumber = 2, name = "Week 2 - Lower Body")
        )
    private val allWorkouts =
        listOf(
            mockProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "Bench Press Day"),
            mockProgrammedWorkout(id = 2L, programId = 1L, dayNumber = 2, name = "Squat Day"),
            mockProgrammedWorkout(id = 3L, programId = 2L, dayNumber = 1, name = "Deadlift Day")
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = ProgrammedWorkoutDAL(postgresClient)
    }

    @Test
    fun `selectProgrammedWorkoutById should return programmed workout`() {
        whenever(
            postgresClient.selectIndividual<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE id=$1", programmedWorkout.id)
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkout.id)
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).selectIndividual<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE id=$1", programmedWorkout.id)
    }

    @Test
    fun `selectProgrammedWorkoutsByProgram should return list of programmed workouts`() {
        whenever(
            postgresClient.select<ProgrammedWorkout>(
                "SELECT * FROM programmed_workout WHERE program_id=$1 ORDER BY day_number",
                programmedWorkout.programId
            )
        ).thenReturn(Mono.just(programmedWorkoutList))
        val result = programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programmedWorkout.programId)
        StepVerifier.create(result).expectNext(programmedWorkoutList).verifyComplete()
        verify(
            postgresClient
        ).select<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE program_id=$1 ORDER BY day_number", programmedWorkout.programId)
    }

    @Test
    fun `insertProgrammedWorkout should return inserted programmed workout`() {
        val insertWorkout = mockProgrammedWorkout(id = 0L)
        val expectedQuery =
            """
            INSERT INTO programmed_workout
                (program_id, day_number, name)
            VALUES
                ($1, $2, $3)
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedWorkout>(
                expectedQuery,
                insertWorkout.programId,
                insertWorkout.dayNumber,
                insertWorkout.name,
            ),
        ).thenReturn(Mono.just(insertWorkout))
        val result = programmedWorkoutDAL.insertProgrammedWorkout(insertWorkout.programId, insertWorkout.dayNumber, insertWorkout.name)
        StepVerifier.create(result).expectNext(insertWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>(
            expectedQuery,
            insertWorkout.programId,
            insertWorkout.dayNumber,
            insertWorkout.name,
        )
    }

    @Test
    fun `updateProgrammedWorkout should return updated programmed workout`() {
        val updatedWorkout = mockProgrammedWorkout(dayNumber = 2, name = "Week 1 - Upper Body Focus")
        val expectedQuery =
            """
            UPDATE programmed_workout
            SET program_id=$2, day_number=$3, name=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedWorkout>(
                expectedQuery,
                updatedWorkout.id,
                updatedWorkout.programId,
                updatedWorkout.dayNumber,
                updatedWorkout.name,
            ),
        ).thenReturn(Mono.just(updatedWorkout))
        val result =
            programmedWorkoutDAL.updateProgrammedWorkout(
                updatedWorkout.id,
                updatedWorkout.programId,
                updatedWorkout.dayNumber,
                updatedWorkout.name
            )
        StepVerifier.create(result).expectNext(updatedWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>(
            expectedQuery,
            updatedWorkout.id,
            updatedWorkout.programId,
            updatedWorkout.dayNumber,
            updatedWorkout.name,
        )
    }

    @Test
    fun `deleteProgrammedWorkout should return deleted programmed workout`() {
        whenever(
            postgresClient.update<ProgrammedWorkout>("DELETE FROM programmed_workout WHERE id=$1", programmedWorkout.id),
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutDAL.deleteProgrammedWorkout(programmedWorkout.id)
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>("DELETE FROM programmed_workout WHERE id=$1", programmedWorkout.id)
    }

    @Test
    fun `selectProgrammedWorkouts should return all programmed workouts`() {
        whenever(
            postgresClient.select<ProgrammedWorkout>("SELECT * FROM programmed_workout ORDER BY program_id, day_number")
        ).thenReturn(Mono.just(allWorkouts))
        val result = programmedWorkoutDAL.selectProgrammedWorkouts()
        StepVerifier.create(result)
            .expectNext(allWorkouts)
            .verifyComplete()
        verify(postgresClient).select<ProgrammedWorkout>("SELECT * FROM programmed_workout ORDER BY program_id, day_number")
    }

    @Test
    fun `insertProgrammedWorkout should throw ValidationException for invalid day number`() {
        val programId = 1L
        val invalidDayNumber = 0
        val name = "Bench Press Day"
        val expectedMessage = "Day number must be between 1 and 365, got: 0"

        val exception =
            assertThrows(ValidationException::class.java) {
                programmedWorkoutDAL.insertProgrammedWorkout(programId, invalidDayNumber, name)
            }
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `updateProgrammedWorkout should throw ValidationException for invalid day number`() {
        val id = 1L
        val programId = 1L
        val invalidDayNumber = 366
        val name = "Updated Workout"
        val expectedMessage = "Day number must be between 1 and 365, got: 366"

        val exception =
            assertThrows(ValidationException::class.java) {
                programmedWorkoutDAL.updateProgrammedWorkout(id, programId, invalidDayNumber, name)
            }
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `hasUserExistingWorkouts should return true when user has workouts`() {
        val userId = 1
        val mockResult = mapOf("value" to true)

        whenever(
            postgresClient.selectIndividual<Map<String, Any>>(
                """
                SELECT EXISTS(
                    SELECT 1
                    FROM programmed_workout pw
                    JOIN program p ON pw.program_id = p.id
                    WHERE p.user_id = $1
                ) AS value
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(mockResult))

        val result = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

        StepVerifier.create(result).expectNext(true).verifyComplete()
        verify(postgresClient).selectIndividual<Map<String, Any>>(
            """
            SELECT EXISTS(
                SELECT 1
                FROM programmed_workout pw
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
            ) AS value
            """.trimIndent(),
            userId
        )
    }

    @Test
    fun `hasUserExistingWorkouts should return false when user has no workouts`() {
        val userId = 1
        val mockResult = mapOf("value" to false)

        whenever(
            postgresClient.selectIndividual<Map<String, Any>>(
                """
                SELECT EXISTS(
                    SELECT 1
                    FROM programmed_workout pw
                    JOIN program p ON pw.program_id = p.id
                    WHERE p.user_id = $1
                ) AS value
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(mockResult))

        val result = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

        StepVerifier.create(result).expectNext(false).verifyComplete()
        verify(postgresClient).selectIndividual<Map<String, Any>>(
            """
            SELECT EXISTS(
                SELECT 1
                FROM programmed_workout pw
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
            ) AS value
            """.trimIndent(),
            userId
        )
    }
}
