package com.congen.dal

import com.congen.client.PostgresClient
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

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseDAL = ExerciseDAL(postgresClient)
    }

    @Test
    fun `selectExerciseByName should return exercise`() {
        // Given
        val exerciseName = "Bench Press"
        val exercise =
            Exercise(
                name = exerciseName,
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(
            postgresClient.selectIndividual<Exercise>(
                "SELECT * FROM exercise WHERE name=$1",
                exerciseName,
            ),
        ).thenReturn(Mono.just(exercise))

        // When
        val result = exerciseDAL.selectExerciseByName(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(postgresClient).selectIndividual<Exercise>(
            "SELECT * FROM exercise WHERE name=$1",
            exerciseName,
        )
    }

    @Test
    fun `selectExercises should return list of exercises`() {
        // Given
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound exercise",
                    movementType = "push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true,
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound exercise",
                    movementType = "push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = true,
                ),
            )

        whenever(postgresClient.select<Exercise>("SELECT * FROM exercise")).thenReturn(Mono.just(exercises))

        // When
        val result = exerciseDAL.selectExercises()

        // Then
        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()

        verify(postgresClient).select<Exercise>("SELECT * FROM exercise")
    }

    @Test
    fun `insertExercise should return inserted exercise`() {
        // Given
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

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

        // When
        val result = exerciseDAL.insertExercise(exercise)

        // Then
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
        // Given
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "Updated description",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        val expectedQuery =
            """
            UPDATE exercise
            SET description=$2, movement_type=$3, is_unilateral=$4, is_upper=$5, is_accessory=$6
            WHERE name=$1
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

        // When
        val result = exerciseDAL.updateExercise(exercise)

        // Then
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
    fun `deleteExercise should return deleted exercise`() {
        // Given
        val exerciseName = "Bench Press"
        val deletedExercise =
            Exercise(
                name = exerciseName,
                description = "A compound exercise",
                movementType = "push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true,
            )

        whenever(postgresClient.update<Exercise>("DELETE FROM exercise WHERE name=$1", exerciseName)).thenReturn(Mono.just(deletedExercise))

        // When
        val result = exerciseDAL.deleteExercise(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedExercise)
            .verifyComplete()

        verify(postgresClient).update<Exercise>("DELETE FROM exercise WHERE name=$1", exerciseName)
    }
}
