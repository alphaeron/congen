package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WorkoutStageTypeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutStageTypeDAL::class.java)
    }

    fun selectWorkoutStageTypeById(id: Int): Mono<WorkoutStageType> {
        logger.debug("Selecting workout stage type by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage_type WHERE id=$1",
            id,
        )
    }

    fun selectWorkoutStageTypeByName(name: String): Mono<WorkoutStageType> {
        logger.debug("Selecting workout stage type by name: {}", name)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage_type WHERE name=$1",
            name,
        )
    }

    fun selectWorkoutStageTypes(): Mono<List<WorkoutStageType>> {
        logger.debug("Selecting all workout stage types")
        return postgresClient.select("SELECT * FROM workout_stage_type ORDER BY name")
    }
}
