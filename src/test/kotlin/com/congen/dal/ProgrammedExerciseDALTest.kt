package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ProgrammedExercise
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class ProgrammedExerciseDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedExerciseDAL = ProgrammedExerciseDAL(postgresClient)
    }

    @Test
    fun `selectProgrammedExerciseById should return programmed exercise`() {
        val programmedExercise = ProgrammedExercise(
            id = 1L,
            workoutStageId = 5L,
            exerciseName = "Bench Press",
            position = 1,
            notes = "Focus on controlled descent",
            createdAt = now,
            updatedAt = now
        )
        whenever(postgresClient.selectIndividual<ProgrammedExercise>("SELECT * FROM programmed_exercise WHERE id=$1", 1L)).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.selectProgrammedExerciseById(1L)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).selectIndividual<ProgrammedExercise>("SELECT * FROM programmed_exercise WHERE id=$1", 1L)
    }

    @Test
    fun `selectProgrammedExercisesByWorkoutStage should return list of programmed exercises`() {
        val programmedExercises = listOf(
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Focus on controlled descent",
                createdAt = now,
                updatedAt = now
            ),
            ProgrammedExercise(
                id = 2L,
                workoutStageId = 5L,
                exerciseName = "Dumbbell Flyes",
                position = 2,
                notes = "Light weight, high reps",
                createdAt = now,
                updatedAt = now
            )
        )
        whenever(
            postgresClient.select<ProgrammedExercise>(
                "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1 ORDER BY position",
                5L
            )
        ).thenReturn(Mono.just(programmedExercises))
        val result = programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(5L)
        StepVerifier.create(result).expectNext(programmedExercises).verifyComplete()
        verify(postgresClient).select<ProgrammedExercise>(
            "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1 ORDER BY position",
            5L
        )
    }

    @Test
    fun `selectProgrammedExercises should return all exercises`() {
        val exercises =
            listOf(
                ProgrammedExercise(
                    id = 1L,
                    workoutStageId = 5L,
                    exerciseName = "Bench Press",
                    position = 1,
                    notes = "Focus on controlled descent",
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedExercise(
                    id = 2L,
                    workoutStageId = 6L,
                    exerciseName = "Squat",
                    position = 2,
                    notes = "Keep chest up",
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(
            postgresClient.select<ProgrammedExercise>("SELECT * FROM programmed_exercise ORDER BY position")
        ).thenReturn(Mono.just(exercises))

        val result = programmedExerciseDAL.selectProgrammedExercises()

        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()

        verify(postgresClient).select<ProgrammedExercise>("SELECT * FROM programmed_exercise ORDER BY position")
    }

    @Test
    fun `insertProgrammedExercise should return inserted programmed exercise`() {
        val programmedExercise = ProgrammedExercise(
            id = 0L,
            workoutStageId = 5L,
            exerciseName = "Bench Press",
            position = 1,
            notes = "Focus on controlled descent",
            createdAt = now,
            updatedAt = now
        )
        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                INSERT INTO programmed_exercise
                    (workout_stage_id, exercise_name, position, notes)
                VALUES
                    ($1, $2, $3, $4)
                """.trimIndent(),
                programmedExercise.workoutStageId,
                programmedExercise.exerciseName,
                programmedExercise.position,
                programmedExercise.notes,
            ),
        ).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.insertProgrammedExercise(programmedExercise.workoutStageId, programmedExercise.exerciseName, programmedExercise.position, programmedExercise.notes)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, position, notes)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            programmedExercise.workoutStageId,
            programmedExercise.exerciseName,
            programmedExercise.position,
            programmedExercise.notes,
        )
    }

    @Test
    fun `insertProgrammedExercise should handle null notes`() {
        val createdExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                INSERT INTO programmed_exercise
                    (workout_stage_id, exercise_name, position, notes)
                VALUES
                    ($1, $2, $3, $4)
                """.trimIndent(),
                5L,
                "Bench Press",
                1,
                null,
            )
        ).thenReturn(Mono.just(createdExercise))

        val result =
            programmedExerciseDAL.insertProgrammedExercise(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
            )

        StepVerifier.create(result)
            .expectNext(createdExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, position, notes)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            5L,
            "Bench Press",
            1,
            null,
        )
    }

    @Test
    fun `updateProgrammedExercise should return updated programmed exercise`() {
        val programmedExercise = ProgrammedExercise(
            id = 1L,
            workoutStageId = 5L,
            exerciseName = "Barbell Bench Press",
            position = 2,
            notes = "Updated notes",
            createdAt = now,
            updatedAt = now
        )
        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                UPDATE programmed_exercise
                SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
                WHERE id=$1
                """.trimIndent(),
                programmedExercise.id,
                programmedExercise.workoutStageId,
                programmedExercise.exerciseName,
                programmedExercise.position,
                programmedExercise.notes,
            ),
        ).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.updateProgrammedExercise(programmedExercise.id, programmedExercise.workoutStageId, programmedExercise.exerciseName, programmedExercise.position, programmedExercise.notes)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>(
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent(),
            programmedExercise.id,
            programmedExercise.workoutStageId,
            programmedExercise.exerciseName,
            programmedExercise.position,
            programmedExercise.notes,
        )
    }

    @Test
    fun `updateProgrammedExercise should handle null notes`() {
        val updatedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
                createdAt = now,
                updatedAt = now
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                UPDATE programmed_exercise
                SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
                WHERE id=$1
                """.trimIndent(),
                updatedExercise.id,
                updatedExercise.workoutStageId,
                updatedExercise.exerciseName,
                updatedExercise.position,
                updatedExercise.notes,
            )
        ).thenReturn(Mono.just(updatedExercise))

        val result =
            programmedExerciseDAL.updateProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                position = 1,
                notes = null,
            )

        StepVerifier.create(result)
            .expectNext(updatedExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, position=$4, notes=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent(),
            1L,
            5L,
            "Bench Press",
            1,
            null,
        )
    }

    @Test
    fun `deleteProgrammedExercise should return deleted programmed exercise`() {
        val programmedExercise = ProgrammedExercise(
            id = 1L,
            workoutStageId = 5L,
            exerciseName = "Bench Press",
            position = 1,
            notes = "Focus on controlled descent",
            createdAt = now,
            updatedAt = now
        )
        whenever(
            postgresClient.update<ProgrammedExercise>("DELETE FROM programmed_exercise WHERE id=$1", 1L),
        ).thenReturn(Mono.just(programmedExercise))
        val result = programmedExerciseDAL.deleteProgrammedExercise(1L)
        StepVerifier.create(result).expectNext(programmedExercise).verifyComplete()
        verify(postgresClient).update<ProgrammedExercise>("DELETE FROM programmed_exercise WHERE id=$1", 1L)
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
                1L
            )
        ).thenReturn(Mono.just(userId))

        val result = programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)

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
            1L
        )
    }
}
