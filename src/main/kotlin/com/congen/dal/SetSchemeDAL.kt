package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.SetScheme
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SetSchemeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SetSchemeDAL::class.java)
    }

    fun selectSetSchemeById(id: Long): Mono<SetScheme> {
        logger.debug("Selecting set scheme by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM set_scheme WHERE id=$1",
            id,
        )
    }

    fun selectSetSchemesByProgrammedExerciseId(programmedExerciseId: Long): Mono<List<SetScheme>> {
        logger.debug("Selecting set schemes by programmed exercise id: {}", programmedExerciseId)
        return postgresClient.select(
            "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
            programmedExerciseId,
        )
    }

    fun selectSetSchemes(): Mono<List<SetScheme>> {
        logger.debug("Selecting all set schemes")
        return postgresClient.select("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
    }

    fun insertSetScheme(setScheme: SetScheme): Mono<SetScheme> {
        logger.debug("Inserting set scheme for exercise: {}, set: {}", setScheme.programmedExerciseId, setScheme.setNumber)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setScheme.setNumber)
        ValidationUtil.validateTempo(setScheme.eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(setScheme.isometricTempo, "Isometric")
        ValidationUtil.validateTempo(setScheme.concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(setScheme.targetWeight)
        ValidationUtil.validatePerformedWeight(setScheme.performedWeight)
        ValidationUtil.validateTargetRepCount(setScheme.targetRepCount)
        ValidationUtil.validatePerformedRepCount(setScheme.performedRepCount)
        ValidationUtil.validateRestSeconds(setScheme.restSeconds)

        return postgresClient.update(
            """
            INSERT INTO set_scheme
                (programmed_exercise_id, set_number, was_set_performed, is_amrap, is_emom, use_tempo,
                 eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                 target_rep_count, performed_rep_count, rest_seconds)
            VALUES
                ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
            """.trimIndent(),
            setScheme.programmedExerciseId,
            setScheme.setNumber,
            setScheme.wasSetPerformed,
            setScheme.isAmrap,
            setScheme.isEmom,
            setScheme.useTempo,
            setScheme.eccentricTempo,
            setScheme.isometricTempo,
            setScheme.concentricTempo,
            setScheme.targetWeight,
            setScheme.performedWeight,
            setScheme.targetRepCount,
            setScheme.performedRepCount,
            setScheme.restSeconds,
        )
    }

    fun updateSetScheme(setScheme: SetScheme): Mono<SetScheme> {
        logger.debug("Updating set scheme: {}", setScheme.id)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setScheme.setNumber)
        ValidationUtil.validateTempo(setScheme.eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(setScheme.isometricTempo, "Isometric")
        ValidationUtil.validateTempo(setScheme.concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(setScheme.targetWeight)
        ValidationUtil.validatePerformedWeight(setScheme.performedWeight)
        ValidationUtil.validateTargetRepCount(setScheme.targetRepCount)
        ValidationUtil.validatePerformedRepCount(setScheme.performedRepCount)
        ValidationUtil.validateRestSeconds(setScheme.restSeconds)

        return postgresClient.update(
            """
            UPDATE set_scheme
            SET programmed_exercise_id=$2, set_number=$3, was_set_performed=$4, is_amrap=$5, is_emom=$6, use_tempo=$7,
                eccentric_tempo=$8, isometric_tempo=$9, concentric_tempo=$10, target_weight=$11, performed_weight=$12,
                target_rep_count=$13, performed_rep_count=$14, rest_seconds=$15
            WHERE id=$1
            """.trimIndent(),
            setScheme.id,
            setScheme.programmedExerciseId,
            setScheme.setNumber,
            setScheme.wasSetPerformed,
            setScheme.isAmrap,
            setScheme.isEmom,
            setScheme.useTempo,
            setScheme.eccentricTempo,
            setScheme.isometricTempo,
            setScheme.concentricTempo,
            setScheme.targetWeight,
            setScheme.performedWeight,
            setScheme.targetRepCount,
            setScheme.performedRepCount,
            setScheme.restSeconds,
        )
    }

    fun deleteSetScheme(id: Long): Mono<SetScheme> {
        logger.debug("Deleting set scheme: {}", id)
        return postgresClient.update(
            """
            DELETE FROM set_scheme WHERE id=$1
            """.trimIndent(),
            id,
        )
    }
}
