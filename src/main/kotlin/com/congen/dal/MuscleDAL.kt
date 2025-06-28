package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Muscle
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MuscleDAL(
    private val postgresClient: PostgresClient,
) {
    fun selectMuscleByName(muscleName: String): Mono<Muscle> =
        postgresClient.selectIndividual(
            "SELECT * FROM muscle WHERE name=$1",
            muscleName
        )

    fun selectMuscles(): Mono<List<Muscle>> =
        postgresClient.select("SELECT * FROM muscle")

    fun insertMuscle(muscle: Muscle): Mono<Muscle> =
        postgresClient.update(
            """
                INSERT INTO muscle
                    (name, description)
                VALUES
                    ($1, $2)
            """.trimIndent(),
            muscle.name,
            muscle.description
        )

    fun updateMuscle(muscle: Muscle): Mono<Muscle> =
        postgresClient.update(
            """
                UPDATE muscle
                SET description=$2
                WHERE name=$1
            """.trimIndent(),
            muscle.name,
            muscle.description
        )

    fun deleteMuscle(muscleName: String): Mono<Muscle> =
        postgresClient.update(
            "DELETE FROM muscle WHERE name=$1",
            muscleName
        )
} 