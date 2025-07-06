package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WorkoutStageDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var workoutStageDAL: WorkoutStageDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        workoutStageDAL = WorkoutStageDAL(postgresClient)
    }

    @Test
    fun `selectWorkoutStageById should return workout stage`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        whenever(
            postgresClient.selectIndividual<WorkoutStage>(
                "SELECT * FROM workout_stage WHERE id=$1",
                1L
            )
        ).thenReturn(Mono.just(workoutStage))

        val result = workoutStageDAL.selectWorkoutStageById(1L)

        StepVerifier.create(result)
            .expectNext(workoutStage)
            .verifyComplete()

        verify(postgresClient).selectIndividual<WorkoutStage>(
            "SELECT * FROM workout_stage WHERE id=$1",
            1L
        )
    }

    @Test
    fun `selectWorkoutStagesByProgrammedWorkoutId should return list of stages`() {
        val stages =
            listOf(
                WorkoutStage(
                    id = 1L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 1,
                    position = 1
                ),
                WorkoutStage(
                    id = 2L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 2,
                    position = 2
                )
            )

        whenever(
            postgresClient.select<WorkoutStage>(
                "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
                5L
            )
        ).thenReturn(Mono.just(stages))

        val result = workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(5L)

        StepVerifier.create(result)
            .expectNext(stages)
            .verifyComplete()

        verify(postgresClient).select<WorkoutStage>(
            "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
            5L
        )
    }

    @Test
    fun `selectWorkoutStages should return all stages`() {
        val stages =
            listOf(
                WorkoutStage(
                    id = 1L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 1,
                    position = 1
                ),
                WorkoutStage(
                    id = 2L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 2,
                    position = 2
                )
            )

        whenever(
            postgresClient.select<WorkoutStage>("SELECT * FROM workout_stage ORDER BY programmed_workout_id, position")
        ).thenReturn(Mono.just(stages))

        val result = workoutStageDAL.selectWorkoutStages()

        StepVerifier.create(result)
            .expectNext(stages)
            .verifyComplete()

        verify(postgresClient).select<WorkoutStage>("SELECT * FROM workout_stage ORDER BY programmed_workout_id, position")
    }

    @Test
    fun `insertWorkoutStage should return created stage`() {
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        whenever(
            postgresClient.update<WorkoutStage>(
                """
                INSERT INTO workout_stage
                    (programmed_workout_id, stage_type_id, position)
                VALUES
                    ($1, $2, $3)
                """.trimIndent(),
                5L,
                1L,
                1
            )
        ).thenReturn(Mono.just(createdStage))

        val result =
            workoutStageDAL.insertWorkoutStage(
                programmedWorkoutId = 5L,
                stageTypeId = 1L,
                position = 1
            )

        StepVerifier.create(result)
            .expectNext(createdStage)
            .verifyComplete()

        verify(postgresClient).update<WorkoutStage>(
            """
            INSERT INTO workout_stage
                (programmed_workout_id, stage_type_id, position)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            5L,
            1L,
            1
        )
    }

    @Test
    fun `insertWorkoutStage should throw ValidationException for invalid position`() {
        assertThrows<ValidationException> {
            workoutStageDAL.insertWorkoutStage(
                programmedWorkoutId = 5L,
                stageTypeId = 1L,
                position = 0
            )
        }
    }

    @Test
    fun `updateWorkoutStage should return updated stage`() {
        val updatedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2
            )

        whenever(
            postgresClient.update<WorkoutStage>(
                """
                UPDATE workout_stage
                SET programmed_workout_id=$2, stage_type_id=$3, position=$4
                WHERE id=$1
                """.trimIndent(),
                1L,
                5L,
                2L,
                2
            )
        ).thenReturn(Mono.just(updatedStage))

        val result =
            workoutStageDAL.updateWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2L,
                position = 2
            )

        StepVerifier.create(result)
            .expectNext(updatedStage)
            .verifyComplete()

        verify(postgresClient).update<WorkoutStage>(
            """
            UPDATE workout_stage
            SET programmed_workout_id=$2, stage_type_id=$3, position=$4
            WHERE id=$1
            """.trimIndent(),
            1L,
            5L,
            2L,
            2
        )
    }

    @Test
    fun `updateWorkoutStage should throw ValidationException for invalid position`() {
        assertThrows<ValidationException> {
            workoutStageDAL.updateWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1L,
                position = 0
            )
        }
    }

    @Test
    fun `deleteWorkoutStage should return deleted stage`() {
        val deletedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        whenever(
            postgresClient.update<WorkoutStage>(
                "DELETE FROM workout_stage WHERE id=$1",
                1L
            )
        ).thenReturn(Mono.just(deletedStage))

        val result = workoutStageDAL.deleteWorkoutStage(1L)

        StepVerifier.create(result)
            .expectNext(deletedStage)
            .verifyComplete()

        verify(postgresClient).update<WorkoutStage>(
            "DELETE FROM workout_stage WHERE id=$1",
            1L
        )
    }
}
