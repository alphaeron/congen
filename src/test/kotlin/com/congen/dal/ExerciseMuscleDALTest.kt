package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseMuscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseMuscleDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseMuscleDAL = ExerciseMuscleDAL(postgresClient)
    }

    @Test
    fun `selectExerciseMuscle should return exercise muscle`() {
        // Given
        val exerciseName = "Bench Press"
        val muscleName = "Chest"
        val exerciseMuscle =
            ExerciseMuscle(
                exerciseName = exerciseName,
                muscleName = muscleName,
            )

        whenever(
            postgresClient.selectIndividual<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
                exerciseName,
                muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscle))

        // When
        val result = exerciseMuscleDAL.selectExerciseMuscle(exerciseName, muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseMuscle)
            .verifyComplete()

        verify(postgresClient).selectIndividual<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName,
        )
    }

    @Test
    fun `selectExerciseMuscleByExercise should return list of exercise muscles`() {
        // Given
        val exerciseName = "Bench Press"
        val exerciseMuscles =
            listOf(
                ExerciseMuscle(
                    exerciseName = exerciseName,
                    muscleName = "Chest",
                ),
                ExerciseMuscle(
                    exerciseName = exerciseName,
                    muscleName = "Triceps",
                ),
            )

        whenever(
            postgresClient.select<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
                exerciseName,
            ),
        ).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseMuscles)
            .verifyComplete()

        verify(postgresClient).select<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseName,
        )
    }

    @Test
    fun `selectExerciseMuscleByMuscle should return list of exercise muscles`() {
        // Given
        val muscleName = "Chest"
        val exerciseMuscles =
            listOf(
                ExerciseMuscle(
                    exerciseName = "Bench Press",
                    muscleName = muscleName,
                ),
                ExerciseMuscle(
                    exerciseName = "Push-up",
                    muscleName = muscleName,
                ),
            )

        whenever(
            postgresClient.select<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
                muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseMuscles)
            .verifyComplete()

        verify(postgresClient).select<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
            muscleName,
        )
    }

    @Test
    fun `selectAllExerciseMuscle should return all exercise muscles`() {
        // Given
        val exerciseMuscles =
            listOf(
                ExerciseMuscle(
                    exerciseName = "Bench Press",
                    muscleName = "Chest",
                ),
                ExerciseMuscle(
                    exerciseName = "Squat",
                    muscleName = "Legs",
                ),
            )

        whenever(postgresClient.select<ExerciseMuscle>("SELECT * FROM exercise_muscle")).thenReturn(Mono.just(exerciseMuscles))

        // When
        val result = exerciseMuscleDAL.selectAllExerciseMuscle()

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseMuscles)
            .verifyComplete()

        verify(postgresClient).select<ExerciseMuscle>("SELECT * FROM exercise_muscle")
    }

    @Test
    fun `insertExerciseMuscle should return inserted exercise muscle`() {
        // Given
        val exerciseMuscle =
            ExerciseMuscle(
                exerciseName = "Bench Press",
                muscleName = "Chest",
            )

        val expectedQuery =
            """
            INSERT INTO exercise_muscle
                (exercise_name, muscle_name)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(
            postgresClient.update<ExerciseMuscle>(
                expectedQuery,
                exerciseMuscle.exerciseName,
                exerciseMuscle.muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscle))

        // When
        val result = exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle.exerciseName, exerciseMuscle.muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseMuscle)
            .verifyComplete()

        verify(postgresClient).update<ExerciseMuscle>(
            expectedQuery,
            exerciseMuscle.exerciseName,
            exerciseMuscle.muscleName,
        )
    }

    @Test
    fun `deleteExerciseMuscle should return deleted exercise muscle`() {
        // Given
        val exerciseName = "Bench Press"
        val muscleName = "Chest"
        val deletedExerciseMuscle =
            ExerciseMuscle(
                exerciseName = exerciseName,
                muscleName = muscleName,
            )

        whenever(
            postgresClient.update<ExerciseMuscle>(
                "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
                exerciseName,
                muscleName,
            ),
        ).thenReturn(Mono.just(deletedExerciseMuscle))

        // When
        val result = exerciseMuscleDAL.deleteExerciseMuscle(exerciseName, muscleName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedExerciseMuscle)
            .verifyComplete()

        verify(postgresClient).update<ExerciseMuscle>(
            "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseName,
            muscleName,
        )
    }
}
