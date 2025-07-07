package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockExerciseMuscle
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

    private val exerciseMuscle = mockExerciseMuscle()
    private val exerciseMuscleList =
        listOf(
            exerciseMuscle,
            mockExerciseMuscle(muscleName = "Triceps")
        )
    private val exerciseMuscleListByMuscle =
        listOf(
            exerciseMuscle,
            mockExerciseMuscle(exerciseName = "Push-up")
        )
    private val allExerciseMuscles =
        listOf(
            exerciseMuscle,
            mockExerciseMuscle(exerciseName = "Squat", muscleName = "Legs")
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseMuscleDAL = ExerciseMuscleDAL(postgresClient)
    }

    @Test
    fun `selectExerciseMuscle should return exercise muscle`() {
        whenever(
            postgresClient.selectIndividual<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
                exerciseMuscle.exerciseName,
                exerciseMuscle.muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleDAL.selectExerciseMuscle(exerciseMuscle.exerciseName, exerciseMuscle.muscleName)

        StepVerifier.create(result)
            .expectNext(exerciseMuscle)
            .verifyComplete()
        verify(postgresClient).selectIndividual<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseMuscle.exerciseName,
            exerciseMuscle.muscleName,
        )
    }

    @Test
    fun `selectExerciseMuscleByExercise should return list of exercise muscles`() {
        whenever(
            postgresClient.select<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
                exerciseMuscle.exerciseName,
            ),
        ).thenReturn(Mono.just(exerciseMuscleList))

        val result = exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseMuscle.exerciseName)

        StepVerifier.create(result)
            .expectNext(exerciseMuscleList)
            .verifyComplete()
        verify(postgresClient).select<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE exercise_name=$1",
            exerciseMuscle.exerciseName,
        )
    }

    @Test
    fun `selectExerciseMuscleByMuscle should return list of exercise muscles`() {
        whenever(
            postgresClient.select<ExerciseMuscle>(
                "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
                exerciseMuscle.muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscleListByMuscle))

        val result = exerciseMuscleDAL.selectExerciseMuscleByMuscle(exerciseMuscle.muscleName)

        StepVerifier.create(result)
            .expectNext(exerciseMuscleListByMuscle)
            .verifyComplete()
        verify(postgresClient).select<ExerciseMuscle>(
            "SELECT * FROM exercise_muscle WHERE muscle_name=$1",
            exerciseMuscle.muscleName,
        )
    }

    @Test
    fun `selectAllExerciseMuscle should return all exercise muscles`() {
        whenever(postgresClient.select<ExerciseMuscle>("SELECT * FROM exercise_muscle")).thenReturn(Mono.just(allExerciseMuscles))

        val result = exerciseMuscleDAL.selectAllExerciseMuscle()

        StepVerifier.create(result)
            .expectNext(allExerciseMuscles)
            .verifyComplete()
        verify(postgresClient).select<ExerciseMuscle>("SELECT * FROM exercise_muscle")
    }

    @Test
    fun `insertExerciseMuscle should return inserted exercise muscle`() {
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

        val result = exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle.exerciseName, exerciseMuscle.muscleName)

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
        whenever(
            postgresClient.update<ExerciseMuscle>(
                "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
                exerciseMuscle.exerciseName,
                exerciseMuscle.muscleName,
            ),
        ).thenReturn(Mono.just(exerciseMuscle))

        val result = exerciseMuscleDAL.deleteExerciseMuscle(exerciseMuscle.exerciseName, exerciseMuscle.muscleName)

        StepVerifier.create(result)
            .expectNext(exerciseMuscle)
            .verifyComplete()
        verify(postgresClient).update<ExerciseMuscle>(
            "DELETE FROM exercise_muscle WHERE exercise_name=$1 AND muscle_name=$2",
            exerciseMuscle.exerciseName,
            exerciseMuscle.muscleName,
        )
    }
}
