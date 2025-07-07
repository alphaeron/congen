package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockExercise
import com.congen.model.Exercise
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var exerciseDAL: ExerciseDAL

    private val exercise = mockExercise()
    private val exercises = listOf(exercise, mockExercise(name = "Squat", isUpper = false))

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseDAL = ExerciseDAL(postgresClient)
    }

    @Test
    fun `selectExerciseByName should return exercise`() {
        whenever(
            postgresClient.selectIndividual<Exercise>(
                "SELECT * FROM exercise WHERE name=$1",
                exercise.name,
            ),
        ).thenReturn(Mono.just(exercise))

        val result = exerciseDAL.selectExerciseByName(exercise.name)

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Exercise>(
            "SELECT * FROM exercise WHERE name=$1",
            exercise.name,
        )
    }

    @Test
    fun `selectExercises should return list of exercises`() {
        whenever(postgresClient.select<Exercise>("SELECT * FROM exercise")).thenReturn(Mono.just(exercises))

        val result = exerciseDAL.selectExercises()

        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()
        verify(postgresClient).select<Exercise>("SELECT * FROM exercise")
    }

    @Test
    fun `insertExercise should return inserted exercise`() {
        val expectedQuery =
            """
            INSERT INTO exercise
                (name, description, movement_type, is_unilateral, is_upper, is_accessory)
            VALUES
                ($1, $2, $3, $4, $5, $6)
            """.trimIndent()

        whenever(
            postgresClient.update<Exercise>(
                expectedQuery,
                exercise.name,
                exercise.description,
                exercise.movementType,
                exercise.isUnilateral,
                exercise.isUpper,
                exercise.isAccessory,
            ),
        ).thenReturn(Mono.just(exercise))

        val result =
            exerciseDAL.insertExercise(
                exercise.name,
                exercise.description,
                exercise.movementType,
                exercise.isUnilateral,
                exercise.isUpper,
                exercise.isAccessory
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(postgresClient).update<Exercise>(
            expectedQuery,
            exercise.name,
            exercise.description,
            exercise.movementType,
            exercise.isUnilateral,
            exercise.isUpper,
            exercise.isAccessory,
        )
    }

    @Test
    fun `updateExercise should return updated exercise`() {
        val updatedExercise = mockExercise(description = "Updated description")
        val expectedQuery =
            """
            UPDATE exercise
            SET description=$2, movement_type=$3, is_unilateral=$4, is_upper=$5, is_accessory=$6
            WHERE name=$1
            """.trimIndent()

        whenever(
            postgresClient.update<Exercise>(
                expectedQuery,
                updatedExercise.name,
                updatedExercise.description,
                updatedExercise.movementType,
                updatedExercise.isUnilateral,
                updatedExercise.isUpper,
                updatedExercise.isAccessory,
            ),
        ).thenReturn(Mono.just(updatedExercise))

        val result =
            exerciseDAL.updateExercise(
                updatedExercise.name,
                updatedExercise.description,
                updatedExercise.movementType,
                updatedExercise.isUnilateral,
                updatedExercise.isUpper,
                updatedExercise.isAccessory
            )

        StepVerifier.create(result)
            .expectNext(updatedExercise)
            .verifyComplete()
        verify(postgresClient).update<Exercise>(
            expectedQuery,
            updatedExercise.name,
            updatedExercise.description,
            updatedExercise.movementType,
            updatedExercise.isUnilateral,
            updatedExercise.isUpper,
            updatedExercise.isAccessory,
        )
    }

    @Test
    fun `deleteExercise should return deleted exercise`() {
        whenever(postgresClient.update<Exercise>("DELETE FROM exercise WHERE name=$1", exercise.name)).thenReturn(Mono.just(exercise))

        val result = exerciseDAL.deleteExercise(exercise.name)

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(postgresClient).update<Exercise>("DELETE FROM exercise WHERE name=$1", exercise.name)
    }
}
