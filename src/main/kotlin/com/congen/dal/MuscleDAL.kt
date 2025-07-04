package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Muscle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MuscleDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MuscleDAL::class.java)
    }

    fun selectMuscleByName(muscleName: String): Mono<Muscle> {
        logger.debug("Selecting muscle by name: {}", muscleName)
        return postgresClient.selectIndividual(
            "SELECT * FROM muscle WHERE name=$1",
            muscleName,
        )
    }

    fun selectMuscles(): Mono<List<Muscle>> {
        logger.debug("Selecting all muscles")
        return postgresClient.select("SELECT * FROM muscle")
    }

    fun insertMuscle(muscle: Muscle): Mono<Muscle> {
        logger.debug("Inserting muscle: {}", muscle.name)
        return postgresClient.update(
            """
            INSERT INTO muscle
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent(),
            muscle.name,
            muscle.description,
        )
    }

    fun updateMuscle(muscle: Muscle): Mono<Muscle> {
        logger.debug("Updating muscle: {}", muscle.name)
        return postgresClient.update(
            """
            UPDATE muscle
            SET description=$2
            WHERE name=$1
            """.trimIndent(),
            muscle.name,
            muscle.description,
        )
    }

    fun deleteMuscle(muscleName: String): Mono<Muscle> {
        logger.debug("Deleting muscle: {}", muscleName)
        return postgresClient.update(
            "DELETE FROM muscle WHERE name=$1",
            muscleName,
        )
    }
}
