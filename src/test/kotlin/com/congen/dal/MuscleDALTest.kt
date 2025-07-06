package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Muscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class MuscleDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var muscleDAL: MuscleDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        muscleDAL = MuscleDAL(postgresClient)
    }

    @Test
    fun `selectMuscleByName should return muscle`() {
        // Given
        val muscleName = "Chest"
        val muscle =
            Muscle(
                name = muscleName,
                description = "Chest muscles",
            )

        whenever(
            postgresClient.selectIndividual<Muscle>(
                "SELECT * FROM muscle WHERE name=$1",
                muscleName,
            ),
        ).thenReturn(Mono.just(muscle))

        // When
        val result = muscleDAL.selectMuscleByName(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(muscle)
            .verifyComplete()

        verify(postgresClient).selectIndividual<Muscle>(
            "SELECT * FROM muscle WHERE name=$1",
            muscleName,
        )
    }

    @Test
    fun `selectMuscles should return list of muscles`() {
        // Given
        val muscles =
            listOf(
                Muscle(
                    name = "Chest",
                    description = "Chest muscles",
                ),
                Muscle(
                    name = "Back",
                    description = "Back muscles",
                ),
            )

        whenever(postgresClient.select<Muscle>("SELECT * FROM muscle")).thenReturn(Mono.just(muscles))

        // When
        val result = muscleDAL.selectMuscles()

        // Then
        StepVerifier.create(result)
            .expectNext(muscles)
            .verifyComplete()

        verify(postgresClient).select<Muscle>("SELECT * FROM muscle")
    }

    @Test
    fun `insertMuscle should return inserted muscle`() {
        // Given
        val muscle =
            Muscle(
                name = "Chest",
                description = "Chest muscles",
            )

        val expectedQuery =
            """
            INSERT INTO muscle
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(
            postgresClient.update<Muscle>(
                expectedQuery,
                muscle.name,
                muscle.description,
            ),
        ).thenReturn(Mono.just(muscle))

        // When
        val result = muscleDAL.insertMuscle(muscle.name, muscle.description)

        // Then
        StepVerifier.create(result)
            .expectNext(muscle)
            .verifyComplete()

        verify(postgresClient).update<Muscle>(
            expectedQuery,
            muscle.name,
            muscle.description,
        )
    }

    @Test
    fun `updateMuscle should return updated muscle`() {
        // Given
        val muscle =
            Muscle(
                name = "Chest",
                description = "Updated chest muscles description",
            )

        val expectedQuery =
            """
            UPDATE muscle
            SET description=$2
            WHERE name=$1
            """.trimIndent()

        whenever(
            postgresClient.update<Muscle>(
                expectedQuery,
                muscle.name,
                muscle.description,
            ),
        ).thenReturn(Mono.just(muscle))

        // When
        val result = muscleDAL.updateMuscle(muscle.name, muscle.description)

        // Then
        StepVerifier.create(result)
            .expectNext(muscle)
            .verifyComplete()

        verify(postgresClient).update<Muscle>(
            expectedQuery,
            muscle.name,
            muscle.description,
        )
    }

    @Test
    fun `deleteMuscle should return deleted muscle`() {
        // Given
        val muscleName = "Chest"
        val deletedMuscle =
            Muscle(
                name = muscleName,
                description = "Chest muscles",
            )

        whenever(
            postgresClient.update<Muscle>(
                "DELETE FROM muscle WHERE name=$1",
                muscleName,
            ),
        ).thenReturn(Mono.just(deletedMuscle))

        // When
        val result = muscleDAL.deleteMuscle(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedMuscle)
            .verifyComplete()

        verify(postgresClient).update<Muscle>(
            "DELETE FROM muscle WHERE name=$1",
            muscleName,
        )
    }
}
