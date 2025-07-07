package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class WorkoutStageDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private val now = LocalDateTime.now()

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
                position = 1,
                name = "Main Lift",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.selectIndividual<WorkoutStage>("SELECT * FROM workout_stage WHERE id=$1", 1L)
        ).thenReturn(Mono.just(workoutStage))
        val result = workoutStageDAL.selectWorkoutStageById(1L)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).selectIndividual<WorkoutStage>("SELECT * FROM workout_stage WHERE id=$1", 1L)
    }

    @Test
    fun `selectWorkoutStagesByProgrammedWorkoutId should return list of workout stages`() {
        val workoutStages =
            listOf(
                WorkoutStage(
                    id = 1L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 1,
                    position = 1,
                    name = "Main Lift",
                    createdAt = now,
                    updatedAt = now
                ),
                WorkoutStage(
                    id = 2L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 2,
                    position = 2,
                    name = "Accessory",
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(
            postgresClient.select<WorkoutStage>("SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position", 5L)
        ).thenReturn(Mono.just(workoutStages))
        val result = workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(5L)
        StepVerifier.create(result).expectNext(workoutStages).verifyComplete()
        verify(postgresClient).select<WorkoutStage>("SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position", 5L)
    }

    @Test
    fun `insertWorkoutStage should return inserted workout stage`() {
        val workoutStage =
            WorkoutStage(
                id = 0L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Main Lift",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<WorkoutStage>(
                """
                INSERT INTO workout_stage
                    (programmed_workout_id, stage_type_id, position, name)
                VALUES
                    ($1, $2, $3, $4)
                """.trimIndent(),
                workoutStage.programmedWorkoutId,
                workoutStage.stageTypeId,
                workoutStage.position,
                workoutStage.name,
            ),
        ).thenReturn(Mono.just(workoutStage))
        val result =
            workoutStageDAL.insertWorkoutStage(
                workoutStage.programmedWorkoutId,
                workoutStage.stageTypeId,
                workoutStage.position,
                workoutStage.name
            )
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>(
            """
            INSERT INTO workout_stage
                (programmed_workout_id, stage_type_id, position, name)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            workoutStage.programmedWorkoutId,
            workoutStage.stageTypeId,
            workoutStage.position,
            workoutStage.name,
        )
    }

    @Test
    fun `updateWorkoutStage should return updated workout stage`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2,
                name = "Accessory",
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            UPDATE workout_stage
            SET programmed_workout_id=$2, stage_type_id=$3, position=$4, name=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<WorkoutStage>(
                expectedQuery,
                workoutStage.id,
                workoutStage.programmedWorkoutId,
                workoutStage.stageTypeId,
                workoutStage.position,
                workoutStage.name,
            ),
        ).thenReturn(Mono.just(workoutStage))
        val result =
            workoutStageDAL.updateWorkoutStage(
                workoutStage.id,
                workoutStage.programmedWorkoutId,
                workoutStage.stageTypeId,
                workoutStage.position,
                workoutStage.name
            )
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>(
            expectedQuery,
            workoutStage.id,
            workoutStage.programmedWorkoutId,
            workoutStage.stageTypeId,
            workoutStage.position,
            workoutStage.name,
        )
    }

    @Test
    fun `deleteWorkoutStage should return deleted workout stage`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Main Lift",
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<WorkoutStage>("DELETE FROM workout_stage WHERE id=$1", 1L),
        ).thenReturn(Mono.just(workoutStage))
        val result = workoutStageDAL.deleteWorkoutStage(1L)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>("DELETE FROM workout_stage WHERE id=$1", 1L)
    }
}
