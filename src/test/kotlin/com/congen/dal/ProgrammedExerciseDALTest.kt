package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockProgrammedExercise
import com.congen.model.ProgrammedExercise
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProgrammedExerciseDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    private val programmedExercise = mockProgrammedExercise()
    private val programmedExerciseList =
        listOf(
            programmedExercise,
            mockProgrammedExercise(id = 2L, exerciseName = "Dumbbell Flyes", position = 2, notes = "Light weight, high reps")
        )
    private val allExercises =
        listOf(
            programmedExercise,
            mockProgrammedExercise(id = 2L, workoutStageId = 6L, exerciseName = "Squat", position = 2, notes = "Keep chest up")
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedExerciseDAL = ProgrammedExerciseDAL(postgresClient)
    }

    @Test
    fun `selectProgrammedExerciseById should return programmed exercise`() {
        whenever(
            postgresClient.selectIndividual<ProgrammedExercise>("SELECT * FROM programmed_exercise WHERE id=$1", programmedExercise.id)
        ).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.selectProgrammedExerciseById(programmedExercise.id)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).selectIndividual<ProgrammedExercise>("SELECT * FROM programmed_exercise WHERE id=$1", programmedExercise.id)
    }

    @Test
    fun `selectProgrammedExercisesByWorkoutStage should return list of programmed exercises`() {
        whenever(
            postgresClient.select<ProgrammedExercise>(
                "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1 ORDER BY position",
                programmedExercise.workoutStageId
            )
        ).thenReturn(Mono.just(programmedExerciseList))
        val result = programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(programmedExercise.workoutStageId)
        StepVerifier.create(result).expectNext(programmedExerciseList).verifyComplete()
        verify(postgresClient).select<ProgrammedExercise>(
            "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1 ORDER BY position",
            programmedExercise.workoutStageId
        )
    }

    @Test
    fun `selectProgrammedExercises should return all exercises`() {
        whenever(
            postgresClient.select<ProgrammedExercise>("SELECT * FROM programmed_exercise ORDER BY position")
        ).thenReturn(Mono.just(allExercises))
        val result = programmedExerciseDAL.selectProgrammedExercises()
        StepVerifier.create(result)
            .expectNext(allExercises)
            .verifyComplete()
        verify(postgresClient).select<ProgrammedExercise>("SELECT * FROM programmed_exercise ORDER BY position")
    }

    @Test
    fun `insertProgrammedExercise should return inserted programmed exercise`() {
        val insertExercise = mockProgrammedExercise(id = 0L)
        val expectedQuery =
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, position, notes)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedExercise>(
                expectedQuery,
                insertExercise.workoutStageId,
                insertExercise.exerciseName,
                insertExercise.position,
                insertExercise.notes,
            ),
        ).thenReturn(Mono.just(insertExercise))
        val result =
            programmedExerciseDAL.insertProgrammedExercise(
                insertExercise.workoutStageId,
                insertExercise.exerciseName,
                insertExercise.position,
                insertExercise.notes
            )
        StepVerifier.create(result).expectNext(insertExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            expectedQuery,
            insertExercise.workoutStageId,
            insertExercise.exerciseName,
            insertExercise.position,
            insertExercise.notes,
        )
    }

    @Test
    fun `insertProgrammedExercise should handle null notes`() {
        val createdExercise = mockProgrammedExercise(notes = null)
        val expectedQuery =
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, position, notes)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedExercise>(
                expectedQuery,
                createdExercise.workoutStageId,
                createdExercise.exerciseName,
                createdExercise.position,
                createdExercise.notes,
            )
        ).thenReturn(Mono.just(createdExercise))
        val result =
            programmedExerciseDAL.insertProgrammedExercise(
                createdExercise.workoutStageId,
                createdExercise.exerciseName,
                createdExercise.position,
                createdExercise.notes
            )
        StepVerifier.create(result)
            .expectNext(createdExercise)
            .verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            expectedQuery,
            createdExercise.workoutStageId,
            createdExercise.exerciseName,
            createdExercise.position,
            createdExercise.notes,
        )
    }

    @Test
    fun `updateProgrammedExercise should return updated programmed exercise`() {
        val updatedExercise = mockProgrammedExercise(exerciseName = "Barbell Bench Press", position = 2, notes = "Updated notes")
        val expectedQuery =
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedExercise>(
                expectedQuery,
                updatedExercise.id,
                updatedExercise.workoutStageId,
                updatedExercise.exerciseName,
                updatedExercise.position,
                updatedExercise.notes,
            ),
        ).thenReturn(Mono.just(updatedExercise))
        val result =
            programmedExerciseDAL.updateProgrammedExercise(
                updatedExercise.id,
                updatedExercise.workoutStageId,
                updatedExercise.exerciseName,
                updatedExercise.position,
                updatedExercise.notes
            )
        StepVerifier.create(result).expectNext(updatedExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            expectedQuery,
            updatedExercise.id,
            updatedExercise.workoutStageId,
            updatedExercise.exerciseName,
            updatedExercise.position,
            updatedExercise.notes,
        )
    }

    @Test
    fun `updateProgrammedExercise should handle null notes`() {
        val updatedExercise = mockProgrammedExercise(notes = null)
        val expectedQuery =
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<ProgrammedExercise>(
                expectedQuery,
                updatedExercise.id,
                updatedExercise.workoutStageId,
                updatedExercise.exerciseName,
                updatedExercise.position,
                updatedExercise.notes,
            )
        ).thenReturn(Mono.just(updatedExercise))
        val result =
            programmedExerciseDAL.updateProgrammedExercise(
                updatedExercise.id,
                updatedExercise.workoutStageId,
                updatedExercise.exerciseName,
                updatedExercise.position,
                updatedExercise.notes
            )
        StepVerifier.create(result)
            .expectNext(updatedExercise)
            .verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            expectedQuery,
            updatedExercise.id,
            updatedExercise.workoutStageId,
            updatedExercise.exerciseName,
            updatedExercise.position,
            updatedExercise.notes,
        )
    }

    @Test
    fun `deleteProgrammedExercise should return deleted programmed exercise`() {
        whenever(
            postgresClient.update<ProgrammedExercise>("DELETE FROM programmed_exercise WHERE id=$1", programmedExercise.id),
        ).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.deleteProgrammedExercise(programmedExercise.id)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>("DELETE FROM programmed_exercise WHERE id=$1", programmedExercise.id)
    }

    @Test
    fun `getUserIdFromProgrammedExercise should return user ID`() {
        val userId = 123
        whenever(
            postgresClient.selectIndividual<Int>(
                """
                SELECT p.user_id
                FROM programmed_exercise pe
                JOIN workout_stage ws ON pe.workout_stage_id = ws.id
                JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
                JOIN program p ON pw.program_id = p.id
                WHERE pe.id = $1
                """.trimIndent(),
                programmedExercise.id
            )
        ).thenReturn(Mono.just(userId))
        val result = programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExercise.id)
        StepVerifier.create(result)
            .expectNext(userId)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Int>(
            """
            SELECT p.user_id
            FROM programmed_exercise pe
            JOIN workout_stage ws ON pe.workout_stage_id = ws.id
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE pe.id = $1
            """.trimIndent(),
            programmedExercise.id
        )
    }
}
