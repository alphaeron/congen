package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStage
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WorkoutStageDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutStageDAL::class.java)
    }

    fun selectWorkoutStageById(id: Long): Mono<WorkoutStage> {
        logger.debug("Selecting workout stage by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage WHERE id=$1",
            id,
        )
    }

    fun selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId: Long): Mono<List<WorkoutStage>> {
        logger.debug("Selecting workout stages by programmed workout id: {}", programmedWorkoutId)
        return postgresClient.select(
            "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
            programmedWorkoutId,
        )
    }

    fun selectWorkoutStages(): Mono<List<WorkoutStage>> {
        logger.debug("Selecting all workout stages")
        return postgresClient.select("SELECT * FROM workout_stage ORDER BY programmed_workout_id, position")
    }

    fun insertWorkoutStage(workoutStage: WorkoutStage): Mono<WorkoutStage> {
        logger.debug("Inserting workout stage for workout: {}, position: {}", workoutStage.programmedWorkoutId, workoutStage.position)

        // Validate all CHECK constraints
        ValidationUtil.validatePosition(workoutStage.position)

        return postgresClient.update(
            """
            INSERT INTO workout_stage
                (programmed_workout_id, stage_type_id, position)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            workoutStage.programmedWorkoutId,
            workoutStage.stageTypeId,
            workoutStage.position,
        )
    }

    fun updateWorkoutStage(workoutStage: WorkoutStage): Mono<WorkoutStage> {
        logger.debug("Updating workout stage: {}", workoutStage.id)

        // Validate all CHECK constraints
        ValidationUtil.validatePosition(workoutStage.position)

        return postgresClient.update(
            """
            UPDATE workout_stage
            SET programmed_workout_id=$2, stage_type_id=$3, position=$4
            WHERE id=$1
            """.trimIndent(),
            workoutStage.id,
            workoutStage.programmedWorkoutId,
            workoutStage.stageTypeId,
            workoutStage.position,
        )
    }

    fun deleteWorkoutStage(id: Long): Mono<WorkoutStage> {
        logger.debug("Deleting workout stage: {}", id)
        return postgresClient.update(
            "DELETE FROM workout_stage WHERE id=$1",
            id,
        )
    }
}
