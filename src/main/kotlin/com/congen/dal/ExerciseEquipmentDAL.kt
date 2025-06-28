package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseEquipment
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseEquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    fun selectExerciseEquipment(exerciseName: String, equipmentName: String): Mono<ExerciseEquipment> =
        postgresClient.selectIndividual(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName
        )

    fun selectExerciseEquipmentByExercise(exerciseName: String): Mono<List<ExerciseEquipment>> =
        postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
            exerciseName
        )

    fun selectExerciseEquipmentByEquipment(equipmentName: String): Mono<List<ExerciseEquipment>> =
        postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
            equipmentName
        )

    fun selectAllExerciseEquipment(): Mono<List<ExerciseEquipment>> =
        postgresClient.select("SELECT * FROM exercise_equipment")

    fun insertExerciseEquipment(exerciseEquipment: ExerciseEquipment): Mono<ExerciseEquipment> =
        postgresClient.update(
            """
                INSERT INTO exercise_equipment
                    (exercise_name, equipment_name)
                VALUES
                    ($1, $2)
            """.trimIndent(),
            exerciseEquipment.exerciseName,
            exerciseEquipment.equipmentName
        )

    fun deleteExerciseEquipment(exerciseName: String, equipmentName: String): Mono<ExerciseEquipment> =
        postgresClient.update(
            "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName
        )
} 