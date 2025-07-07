package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
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
import java.time.LocalDateTime

class ProgrammedWorkoutDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = ProgrammedWorkoutDAL(postgresClient)
    }

    @Test
    fun `selectProgrammedWorkoutById should return programmed workout`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 5L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.selectIndividual<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE id=$1", 1L)
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutDAL.selectProgrammedWorkoutById(1L)
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).selectIndividual<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE id=$1", 1L)
    }

    @Test
    fun `selectProgrammedWorkoutsByProgram should return list of programmed workouts`() {
        val programmedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = 1L,
                    programId = 5L,
                    dayNumber = 1,
                    name = "Week 1 - Upper Body",
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = 2L,
                    programId = 5L,
                    dayNumber = 2,
                    name = "Week 2 - Lower Body",
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(
            postgresClient.select<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE program_id=$1 ORDER BY day_number", 5L)
        ).thenReturn(Mono.just(programmedWorkouts))
        val result = programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(5L)
        StepVerifier.create(result).expectNext(programmedWorkouts).verifyComplete()
        verify(postgresClient).select<ProgrammedWorkout>("SELECT * FROM programmed_workout WHERE program_id=$1 ORDER BY day_number", 5L)
    }

    @Test
    fun `insertProgrammedWorkout should return inserted programmed workout`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 0L,
                programId = 5L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<ProgrammedWorkout>(
                """
                INSERT INTO programmed_workout
                    (program_id, day_number, name)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                programmedWorkout.programId,
                programmedWorkout.dayNumber,
                programmedWorkout.name,
            ),
        ).thenReturn(Mono.just(programmedWorkout))
        val result =
            programmedWorkoutDAL.insertProgrammedWorkout(
                programmedWorkout.programId,
                programmedWorkout.dayNumber,
                programmedWorkout.name
            )
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>(
            """
            INSERT INTO programmed_workout
                (program_id, day_number, name)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            programmedWorkout.programId,
            programmedWorkout.dayNumber,
            programmedWorkout.name,
        )
    }

    @Test
    fun `updateProgrammedWorkout should return updated programmed workout`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 5L,
                dayNumber = 2,
                name = "Week 1 - Upper Body Focus",
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            UPDATE programmed_workout
            SET program_id=$2, day_number=$3, name=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedWorkout>(
                expectedQuery,
                programmedWorkout.id,
                programmedWorkout.programId,
                programmedWorkout.dayNumber,
                programmedWorkout.name,
            ),
        ).thenReturn(Mono.just(programmedWorkout))
        val result =
            programmedWorkoutDAL.updateProgrammedWorkout(
                programmedWorkout.id,
                programmedWorkout.programId,
                programmedWorkout.dayNumber,
                programmedWorkout.name
            )
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>(
            expectedQuery,
            programmedWorkout.id,
            programmedWorkout.programId,
            programmedWorkout.dayNumber,
            programmedWorkout.name,
        )
    }

    @Test
    fun `deleteProgrammedWorkout should return deleted programmed workout`() {
        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 5L,
                dayNumber = 1,
                name = "Week 1 - Upper Body",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<ProgrammedWorkout>("DELETE FROM programmed_workout WHERE id=$1", 1L),
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutDAL.deleteProgrammedWorkout(1L)
        StepVerifier.create(result).expectNext(programmedWorkout).verifyComplete()
        verify(postgresClient).update<ProgrammedWorkout>("DELETE FROM programmed_workout WHERE id=$1", 1L)
    }

    @Test
    fun `selectProgrammedWorkouts should return all programmed workouts`() {
        val expectedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = 1L,
                    programId = 1L,
                    dayNumber = 1,
                    name = "Bench Press Day",
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = 2L,
                    programId = 1L,
                    dayNumber = 2,
                    name = "Squat Day",
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = 3L,
                    programId = 2L,
                    dayNumber = 1,
                    name = "Deadlift Day",
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(
            postgresClient.select<ProgrammedWorkout>("SELECT * FROM programmed_workout ORDER BY program_id, day_number")
        ).thenReturn(Mono.just(expectedWorkouts))

        val result = programmedWorkoutDAL.selectProgrammedWorkouts()

        StepVerifier.create(result)
            .expectNext(expectedWorkouts)
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

        whenever(
            postgresClient.selectIndividual<Boolean>(
                """
                SELECT EXISTS(
                    SELECT TRUE
                    FROM programmed_workout pw
                    JOIN program p ON pw.program_id = p.id
                    WHERE p.user_id = $1
                )
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(true))

        val result = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

        StepVerifier.create(result).expectNext(true).verifyComplete()
        verify(postgresClient).selectIndividual<Boolean>(
            """
            SELECT EXISTS(
                SELECT TRUE
                FROM programmed_workout pw
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
            )
            """.trimIndent(),
            userId
        )
    }

    @Test
    fun `hasUserExistingWorkouts should return false when user has no workouts`() {
        val userId = 1

        whenever(
            postgresClient.selectIndividual<Boolean>(
                """
                SELECT EXISTS(
                    SELECT TRUE
                    FROM programmed_workout pw
                    JOIN program p ON pw.program_id = p.id
                    WHERE p.user_id = $1
                )
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(false))

        val result = programmedWorkoutDAL.hasUserExistingWorkouts(userId)

        StepVerifier.create(result).expectNext(false).verifyComplete()
        verify(postgresClient).selectIndividual<Boolean>(
            """
            SELECT EXISTS(
                SELECT TRUE
                FROM programmed_workout pw
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
            )
            """.trimIndent(),
            userId
        )
    }
}
