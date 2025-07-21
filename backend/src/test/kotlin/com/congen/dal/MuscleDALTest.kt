package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockMuscle
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

    private val muscle = mockMuscle()
    private val muscles = listOf(muscle, mockMuscle(name = "Back"))

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        muscleDAL = MuscleDAL(postgresClient)
    }

    @Test
    fun `selectMuscleByName should return muscle`() {
        whenever(
            postgresClient.selectIndividual<Muscle>(
                "SELECT * FROM muscle WHERE name=$1",
                muscle.name,
            ),
        ).thenReturn(Mono.just(muscle))

        val result = muscleDAL.selectMuscleByName(muscle.name)

        StepVerifier.create(result)
            .expectNext(muscle)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Muscle>(
            "SELECT * FROM muscle WHERE name=$1",
            muscle.name,
        )
    }

    @Test
    fun `selectMuscles should return list of muscles`() {
        whenever(postgresClient.select<Muscle>("SELECT * FROM muscle")).thenReturn(Mono.just(muscles))

        val result = muscleDAL.selectMuscles()

        StepVerifier.create(result)
            .expectNext(muscles)
            .verifyComplete()
        verify(postgresClient).select<Muscle>("SELECT * FROM muscle")
    }

    @Test
    fun `insertMuscle should return inserted muscle`() {
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

        val result = muscleDAL.insertMuscle(muscle.name, muscle.description)

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
        val updatedMuscle = mockMuscle(description = "Updated description")
        val expectedQuery =
            """
            UPDATE muscle
            SET description=$2
            WHERE name=$1
            """.trimIndent()

        whenever(
            postgresClient.update<Muscle>(
                expectedQuery,
                updatedMuscle.name,
                updatedMuscle.description,
            ),
        ).thenReturn(Mono.just(updatedMuscle))

        val result = muscleDAL.updateMuscle(updatedMuscle.name, updatedMuscle.description)

        StepVerifier.create(result)
            .expectNext(updatedMuscle)
            .verifyComplete()
        verify(postgresClient).update<Muscle>(
            expectedQuery,
            updatedMuscle.name,
            updatedMuscle.description,
        )
    }

    @Test
    fun `deleteMuscle should return deleted muscle`() {
        whenever(
            postgresClient.update<Muscle>(
                "DELETE FROM muscle WHERE name=$1",
                muscle.name,
            ),
        ).thenReturn(Mono.just(muscle))

        val result = muscleDAL.deleteMuscle(muscle.name)

        StepVerifier.create(result)
            .expectNext(muscle)
            .verifyComplete()
        verify(postgresClient).update<Muscle>(
            "DELETE FROM muscle WHERE name=$1",
            muscle.name,
        )
    }
}
