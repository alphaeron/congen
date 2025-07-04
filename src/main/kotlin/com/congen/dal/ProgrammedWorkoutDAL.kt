package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ProgrammedWorkout
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ProgrammedWorkoutDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammedWorkoutDAL::class.java)
    }

    fun selectProgrammedWorkoutById(id: Long): Mono<ProgrammedWorkout> {
        logger.debug("Selecting programmed workout by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM programmed_workout WHERE id=$1",
            id,
        )
    }

    fun selectProgrammedWorkoutsByProgramId(programId: Long): Mono<List<ProgrammedWorkout>> {
        logger.debug("Selecting programmed workouts by program id: {}", programId)
        return postgresClient.select(
            "SELECT * FROM programmed_workout WHERE program_id=$1 ORDER BY day_number",
            programId,
        )
    }

    fun selectProgrammedWorkouts(): Mono<List<ProgrammedWorkout>> {
        logger.debug("Selecting all programmed workouts")
        return postgresClient.select("SELECT * FROM programmed_workout ORDER BY program_id, day_number")
    }

    fun insertProgrammedWorkout(programmedWorkout: ProgrammedWorkout): Mono<ProgrammedWorkout> {
        logger.debug("Inserting programmed workout: {}", programmedWorkout.name)

        // Validate all CHECK constraints
        ValidationUtil.validateDayNumber(programmedWorkout.dayNumber)

        return postgresClient.update(
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

    fun updateProgrammedWorkout(programmedWorkout: ProgrammedWorkout): Mono<ProgrammedWorkout> {
        logger.debug("Updating programmed workout: {}", programmedWorkout.id)

        // Validate all CHECK constraints
        ValidationUtil.validateDayNumber(programmedWorkout.dayNumber)

        return postgresClient.update(
            """
            UPDATE programmed_workout
            SET program_id=$2, day_number=$3, name=$4
            WHERE id=$1
            """.trimIndent(),
            programmedWorkout.id,
            programmedWorkout.programId,
            programmedWorkout.dayNumber,
            programmedWorkout.name,
        )
    }

    fun deleteProgrammedWorkout(id: Long): Mono<ProgrammedWorkout> {
        logger.debug("Deleting programmed workout: {}", id)
        return postgresClient.update(
            "DELETE FROM programmed_workout WHERE id=$1",
            id,
        )
    }
}
