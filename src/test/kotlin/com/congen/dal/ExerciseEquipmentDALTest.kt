package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseEquipmentDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseEquipmentDAL = ExerciseEquipmentDAL(postgresClient)
    }

    @Test
    fun `selectExerciseEquipment should return exercise equipment`() {
        // Given
        val exerciseName = "Bench Press"
        val equipmentName = "Barbell"
        val exerciseEquipment =
            ExerciseEquipment(
                exerciseName = exerciseName,
                equipmentName = equipmentName,
            )

        whenever(
            postgresClient.selectIndividual<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
                exerciseName,
                equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipment))

        // When
        val result = exerciseEquipmentDAL.selectExerciseEquipment(exerciseName, equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseEquipment)
            .verifyComplete()

        verify(postgresClient).selectIndividual<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName,
        )
    }

    @Test
    fun `selectExerciseEquipmentByExercise should return list of exercise equipment`() {
        // Given
        val exerciseName = "Bench Press"
        val exerciseEquipmentList =
            listOf(
                ExerciseEquipment(
                    exerciseName = exerciseName,
                    equipmentName = "Barbell",
                ),
                ExerciseEquipment(
                    exerciseName = exerciseName,
                    equipmentName = "Bench",
                ),
            )

        whenever(
            postgresClient.select<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
                exerciseName,
            ),
        ).thenReturn(Mono.just(exerciseEquipmentList))

        // When
        val result = exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()

        verify(postgresClient).select<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
            exerciseName,
        )
    }

    @Test
    fun `selectExerciseEquipmentByEquipment should return list of exercise equipment`() {
        // Given
        val equipmentName = "Barbell"
        val exerciseEquipmentList =
            listOf(
                ExerciseEquipment(
                    exerciseName = "Bench Press",
                    equipmentName = equipmentName,
                ),
                ExerciseEquipment(
                    exerciseName = "Squat",
                    equipmentName = equipmentName,
                ),
            )

        whenever(
            postgresClient.select<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
                equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipmentList))

        // When
        val result = exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()

        verify(postgresClient).select<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
            equipmentName,
        )
    }

    @Test
    fun `selectAllExerciseEquipment should return all exercise equipment`() {
        // Given
        val exerciseEquipmentList =
            listOf(
                ExerciseEquipment(
                    exerciseName = "Bench Press",
                    equipmentName = "Barbell",
                ),
                ExerciseEquipment(
                    exerciseName = "Squat",
                    equipmentName = "Barbell",
                ),
            )

        whenever(postgresClient.select<ExerciseEquipment>("SELECT * FROM exercise_equipment")).thenReturn(Mono.just(exerciseEquipmentList))

        // When
        val result = exerciseEquipmentDAL.selectAllExerciseEquipment()

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()

        verify(postgresClient).select<ExerciseEquipment>("SELECT * FROM exercise_equipment")
    }

    @Test
    fun `insertExerciseEquipment should return inserted exercise equipment`() {
        // Given
        val exerciseEquipment =
            ExerciseEquipment(
                exerciseName = "Bench Press",
                equipmentName = "Barbell",
            )

        val expectedQuery =
            """
            INSERT INTO exercise_equipment
                (exercise_name, equipment_name)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(
            postgresClient.update<ExerciseEquipment>(
                expectedQuery,
                exerciseEquipment.exerciseName,
                exerciseEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipment))

        // When
        val result = exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(exerciseEquipment)
            .verifyComplete()

        verify(postgresClient).update<ExerciseEquipment>(
            expectedQuery,
            exerciseEquipment.exerciseName,
            exerciseEquipment.equipmentName,
        )
    }

    @Test
    fun `deleteExerciseEquipment should return deleted exercise equipment`() {
        // Given
        val exerciseName = "Bench Press"
        val equipmentName = "Barbell"
        val deletedExerciseEquipment =
            ExerciseEquipment(
                exerciseName = exerciseName,
                equipmentName = equipmentName,
            )

        whenever(
            postgresClient.update<ExerciseEquipment>(
                "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
                exerciseName,
                equipmentName,
            ),
        ).thenReturn(Mono.just(deletedExerciseEquipment))

        // When
        val result = exerciseEquipmentDAL.deleteExerciseEquipment(exerciseName, equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedExerciseEquipment)
            .verifyComplete()

        verify(postgresClient).update<ExerciseEquipment>(
            "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName,
        )
    }
}
