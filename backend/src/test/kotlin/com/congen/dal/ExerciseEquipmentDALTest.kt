package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockExerciseEquipment
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

    private val exerciseEquipment = mockExerciseEquipment()
    private val exerciseEquipmentList =
        listOf(
            exerciseEquipment,
            mockExerciseEquipment(equipmentName = "Bench")
        )
    private val exerciseEquipmentListByEquipment =
        listOf(
            exerciseEquipment,
            mockExerciseEquipment(exerciseName = "Squat")
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        exerciseEquipmentDAL = ExerciseEquipmentDAL(postgresClient)
    }

    @Test
    fun `selectExerciseEquipment should return exercise equipment`() {
        whenever(
            postgresClient.selectIndividual<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
                exerciseEquipment.exerciseName,
                exerciseEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseEquipmentDAL.selectExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)

        StepVerifier.create(result)
            .expectNext(exerciseEquipment)
            .verifyComplete()
        verify(postgresClient).selectIndividual<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseEquipment.exerciseName,
            exerciseEquipment.equipmentName,
        )
    }

    @Test
    fun `selectExerciseEquipmentByExercise should return list of exercise equipment`() {
        whenever(
            postgresClient.select<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
                exerciseEquipment.exerciseName,
            ),
        ).thenReturn(Mono.just(exerciseEquipmentList))

        val result = exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseEquipment.exerciseName)

        StepVerifier.create(result)
            .expectNext(exerciseEquipmentList)
            .verifyComplete()
        verify(postgresClient).select<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
            exerciseEquipment.exerciseName,
        )
    }

    @Test
    fun `selectExerciseEquipmentByEquipment should return list of exercise equipment`() {
        whenever(
            postgresClient.select<ExerciseEquipment>(
                "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
                exerciseEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipmentListByEquipment))

        val result = exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(exerciseEquipment.equipmentName)

        StepVerifier.create(result)
            .expectNext(exerciseEquipmentListByEquipment)
            .verifyComplete()
        verify(postgresClient).select<ExerciseEquipment>(
            "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
            exerciseEquipment.equipmentName,
        )
    }

    @Test
    fun `selectAllExerciseEquipment should return all exercise equipment`() {
        whenever(
            postgresClient.select<ExerciseEquipment>("SELECT * FROM exercise_equipment")
        ).thenReturn(Mono.just(exerciseEquipmentListByEquipment))

        val result = exerciseEquipmentDAL.selectAllExerciseEquipment()

        StepVerifier.create(result)
            .expectNext(exerciseEquipmentListByEquipment)
            .verifyComplete()
        verify(postgresClient).select<ExerciseEquipment>("SELECT * FROM exercise_equipment")
    }

    @Test
    fun `insertExerciseEquipment should return inserted exercise equipment`() {
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

        val result = exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)

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
        whenever(
            postgresClient.update<ExerciseEquipment>(
                "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
                exerciseEquipment.exerciseName,
                exerciseEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(exerciseEquipment))

        val result = exerciseEquipmentDAL.deleteExerciseEquipment(exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)

        StepVerifier.create(result)
            .expectNext(exerciseEquipment)
            .verifyComplete()
        verify(postgresClient).update<ExerciseEquipment>(
            "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseEquipment.exerciseName,
            exerciseEquipment.equipmentName,
        )
    }
}
