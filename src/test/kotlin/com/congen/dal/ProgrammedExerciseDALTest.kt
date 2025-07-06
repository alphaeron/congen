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

class ProgrammedExerciseDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedExerciseDAL = ProgrammedExerciseDAL(postgresClient)
    }

    @Test
    fun `selectProgrammedExerciseById should return programmed exercise`() {
        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        whenever(
            postgresClient.selectIndividual<ProgrammedExercise>(
                "SELECT * FROM programmed_exercise WHERE id=$1",
                1L
            )
        ).thenReturn(Mono.just(programmedExercise))

        val result = programmedExerciseDAL.selectProgrammedExerciseById(1L)

        StepVerifier.create(result)
            .expectNext(programmedExercise)
            .verifyComplete()

        verify(postgresClient).selectIndividual<ProgrammedExercise>(
            "SELECT * FROM programmed_exercise WHERE id=$1",
            1L
        )
    }

    @Test
    fun `selectProgrammedExercisesByWorkoutStageId should return list of exercises`() {
        val exercises =
            listOf(
                ProgrammedExercise(
                    id = 1L,
                    workoutStageId = 5L,
                    exerciseName = "Bench Press",
                    notes = "Focus on controlled descent"
                ),
                ProgrammedExercise(
                    id = 2L,
                    workoutStageId = 5L,
                    exerciseName = "Dumbbell Flyes",
                    notes = "Light weight, high reps"
                )
            )

        whenever(
            postgresClient.select<ProgrammedExercise>(
                "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1",
                5L
            )
        ).thenReturn(Mono.just(exercises))

        val result = programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(5L)

        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()

        verify(postgresClient).select<ProgrammedExercise>(
            "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1",
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
                    notes = "Focus on controlled descent"
                ),
                ProgrammedExercise(
                    id = 2L,
                    workoutStageId = 6L,
                    exerciseName = "Squat",
                    notes = "Keep chest up"
                )
            )

        whenever(
            postgresClient.select<ProgrammedExercise>("SELECT * FROM programmed_exercise")
        ).thenReturn(Mono.just(exercises))

        val result = programmedExerciseDAL.selectProgrammedExercises()

        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()

        verify(postgresClient).select<ProgrammedExercise>("SELECT * FROM programmed_exercise")
    }

    @Test
    fun `insertProgrammedExercise should return created exercise`() {
        val createdExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                INSERT INTO programmed_exercise
                    (workout_stage_id, exercise_name, notes)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                5L,
                "Bench Press",
                "Focus on controlled descent"
            )
        ).thenReturn(Mono.just(createdExercise))

        val result =
            programmedExerciseDAL.insertProgrammedExercise(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        StepVerifier.create(result)
            .expectNext(createdExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, notes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            5L,
            "Bench Press",
            "Focus on controlled descent"
        )
    }

    @Test
    fun `insertProgrammedExercise should handle null notes`() {
        val createdExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                INSERT INTO programmed_exercise
                    (workout_stage_id, exercise_name, notes)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                5L,
                "Bench Press",
                null
            )
        ).thenReturn(Mono.just(createdExercise))

        val result =
            programmedExerciseDAL.insertProgrammedExercise(
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        StepVerifier.create(result)
            .expectNext(createdExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, notes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            5L,
            "Bench Press",
            null
        )
    }

    @Test
    fun `updateProgrammedExercise should return updated exercise`() {
        val updatedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Incline Bench Press",
                notes = "30 degree angle"
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                UPDATE programmed_exercise
                SET workout_stage_id=$2, exercise_name=$3, notes=$4
                WHERE id=$1
                """.trimIndent(),
                1L,
                5L,
                "Incline Bench Press",
                "30 degree angle"
            )
        ).thenReturn(Mono.just(updatedExercise))

        val result =
            programmedExerciseDAL.updateProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Incline Bench Press",
                notes = "30 degree angle"
            )

        StepVerifier.create(result)
            .expectNext(updatedExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, notes=$4
            WHERE id=$1
            """.trimIndent(),
            1L,
            5L,
            "Incline Bench Press",
            "30 degree angle"
        )
    }

    @Test
    fun `updateProgrammedExercise should handle null notes`() {
        val updatedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                """
                UPDATE programmed_exercise
                SET workout_stage_id=$2, exercise_name=$3, notes=$4
                WHERE id=$1
                """.trimIndent(),
                1L,
                5L,
                "Bench Press",
                null
            )
        ).thenReturn(Mono.just(updatedExercise))

        val result =
            programmedExerciseDAL.updateProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = null
            )

        StepVerifier.create(result)
            .expectNext(updatedExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, notes=$4
            WHERE id=$1
            """.trimIndent(),
            1L,
            5L,
            "Bench Press",
            null
        )
    }

    @Test
    fun `deleteProgrammedExercise should return deleted exercise`() {
        val deletedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 5L,
                exerciseName = "Bench Press",
                notes = "Focus on controlled descent"
            )

        whenever(
            postgresClient.update<ProgrammedExercise>(
                "DELETE FROM programmed_exercise WHERE id=$1",
                1L
            )
        ).thenReturn(Mono.just(deletedExercise))

        val result = programmedExerciseDAL.deleteProgrammedExercise(1L)

        StepVerifier.create(result)
            .expectNext(deletedExercise)
            .verifyComplete()

        verify(postgresClient).update<ProgrammedExercise>(
            "DELETE FROM programmed_exercise WHERE id=$1",
            1L
        )
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
