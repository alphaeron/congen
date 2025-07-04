package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ProgrammedExercise
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ProgrammedExerciseDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammedExerciseDAL::class.java)
    }

    fun selectProgrammedExerciseById(id: Long): Mono<ProgrammedExercise> {
        logger.debug("Selecting programmed exercise by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM programmed_exercise WHERE id=$1",
            id,
        )
    }

    fun selectProgrammedExercisesByWorkoutStageId(workoutStageId: Long): Mono<List<ProgrammedExercise>> {
        logger.debug("Selecting programmed exercises by workout stage id: {}", workoutStageId)
        return postgresClient.select(
            "SELECT * FROM programmed_exercise WHERE workout_stage_id=$1",
            workoutStageId,
        )
    }

    fun selectProgrammedExercises(): Mono<List<ProgrammedExercise>> {
        logger.debug("Selecting all programmed exercises")
        return postgresClient.select("SELECT * FROM programmed_exercise")
    }

    fun insertProgrammedExercise(programmedExercise: ProgrammedExercise): Mono<ProgrammedExercise> {
        logger.debug("Inserting programmed exercise: {} for stage: {}", programmedExercise.exerciseName, programmedExercise.workoutStageId)
        return postgresClient.update(
            """
            INSERT INTO programmed_exercise
                (workout_stage_id, exercise_name, notes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            programmedExercise.workoutStageId,
            programmedExercise.exerciseName,
            programmedExercise.notes,
        )
    }

    fun updateProgrammedExercise(programmedExercise: ProgrammedExercise): Mono<ProgrammedExercise> {
        logger.debug("Updating programmed exercise: {}", programmedExercise.id)
        return postgresClient.update(
            """
            UPDATE programmed_exercise
            SET workout_stage_id=$2, exercise_name=$3, notes=$4
            WHERE id=$1
            """.trimIndent(),
            programmedExercise.id,
            programmedExercise.workoutStageId,
            programmedExercise.exerciseName,
            programmedExercise.notes,
        )
    }

    fun deleteProgrammedExercise(id: Long): Mono<ProgrammedExercise> {
        logger.debug("Deleting programmed exercise: {}", id)
        return postgresClient.update(
            "DELETE FROM programmed_exercise WHERE id=$1",
            id,
        )
    }
}
