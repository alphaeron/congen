package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockWorkoutStage
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WorkoutStageDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var workoutStageDAL: WorkoutStageDAL

    private val workoutStage = mockWorkoutStage()
    private val workoutStageList = listOf(workoutStage, mockWorkoutStage(id = 2L, stageTypeId = 2, position = 2, name = "Accessory"))

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        workoutStageDAL = WorkoutStageDAL(postgresClient)
    }

    @Test
    fun `selectWorkoutStageById should return workout stage`() {
        whenever(
            postgresClient.selectIndividual<WorkoutStage>("SELECT * FROM workout_stage WHERE id=$1", workoutStage.id)
        ).thenReturn(Mono.just(workoutStage))
        val result = workoutStageDAL.selectWorkoutStageById(workoutStage.id)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).selectIndividual<WorkoutStage>("SELECT * FROM workout_stage WHERE id=$1", workoutStage.id)
    }

    @Test
    fun `selectWorkoutStagesByProgrammedWorkoutId should return list of workout stages`() {
        whenever(
            postgresClient.select<WorkoutStage>(
                "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
                workoutStage.programmedWorkoutId
            )
        ).thenReturn(Mono.just(workoutStageList))
        val result = workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(workoutStage.programmedWorkoutId)
        StepVerifier.create(result).expectNext(workoutStageList).verifyComplete()
        verify(
            postgresClient
        ).select<WorkoutStage>(
            "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
            workoutStage.programmedWorkoutId
        )
    }

    @Test
    fun `insertWorkoutStage should return inserted workout stage`() {
        val insertStage = mockWorkoutStage(id = 0L)
        val expectedQuery =
            """
            INSERT INTO workout_stage
                (programmed_workout_id, stage_type_id, position, name)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()
        whenever(
            postgresClient.update<WorkoutStage>(
                expectedQuery,
                insertStage.programmedWorkoutId,
                insertStage.stageTypeId,
                insertStage.position,
                insertStage.name,
            ),
        ).thenReturn(Mono.just(insertStage))
        val result =
            workoutStageDAL.insertWorkoutStage(
                insertStage.programmedWorkoutId,
                insertStage.stageTypeId,
                insertStage.position,
                insertStage.name
            )
        StepVerifier.create(result).expectNext(insertStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>(
            expectedQuery,
            insertStage.programmedWorkoutId,
            insertStage.stageTypeId,
            insertStage.position,
            insertStage.name,
        )
    }

    @Test
    fun `updateWorkoutStage should return updated workout stage`() {
        val updatedStage = mockWorkoutStage(stageTypeId = 2, position = 2, name = "Accessory")
        val expectedQuery =
            """
            UPDATE workout_stage
            SET programmed_workout_id=$2, stage_type_id=$3, position=$4, name=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()
        whenever(
            postgresClient.update<WorkoutStage>(
                expectedQuery,
                updatedStage.id,
                updatedStage.programmedWorkoutId,
                updatedStage.stageTypeId,
                updatedStage.position,
                updatedStage.name,
            ),
        ).thenReturn(Mono.just(updatedStage))
        val result =
            workoutStageDAL.updateWorkoutStage(
                updatedStage.id,
                updatedStage.programmedWorkoutId,
                updatedStage.stageTypeId,
                updatedStage.position,
                updatedStage.name
            )
        StepVerifier.create(result).expectNext(updatedStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>(
            expectedQuery,
            updatedStage.id,
            updatedStage.programmedWorkoutId,
            updatedStage.stageTypeId,
            updatedStage.position,
            updatedStage.name,
        )
    }

    @Test
    fun `deleteWorkoutStage should return deleted workout stage`() {
        whenever(
            postgresClient.update<WorkoutStage>("DELETE FROM workout_stage WHERE id=$1", workoutStage.id),
        ).thenReturn(Mono.just(workoutStage))
        val result = workoutStageDAL.deleteWorkoutStage(workoutStage.id)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(postgresClient).update<WorkoutStage>("DELETE FROM workout_stage WHERE id=$1", workoutStage.id)
    }
}
