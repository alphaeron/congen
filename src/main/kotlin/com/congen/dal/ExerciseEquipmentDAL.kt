package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseEquipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExerciseEquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseEquipmentDAL::class.java)
    }

    fun selectExerciseEquipment(
        exerciseName: String,
        equipmentName: String,
    ): Mono<ExerciseEquipment> {
        logger.debug("Selecting exercise equipment: {} - {}", exerciseName, equipmentName)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName,
        )
    }

    fun selectExerciseEquipmentByExercise(exerciseName: String): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting equipment for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
            exerciseName,
        )
    }

    fun selectExerciseEquipmentByEquipment(equipmentName: String): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting exercises for equipment: {}", equipmentName)
        return postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
            equipmentName,
        )
    }

    fun selectAllExerciseEquipment(): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting all exercise equipment relationships")
        return postgresClient.select("SELECT * FROM exercise_equipment")
    }

    fun insertExerciseEquipment(exerciseEquipment: ExerciseEquipment): Mono<ExerciseEquipment> {
        logger.debug("Inserting exercise equipment: {} - {}", exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)
        return postgresClient.update(
            """
            INSERT INTO exercise_equipment
                (exercise_name, equipment_name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            exerciseEquipment.exerciseName,
            exerciseEquipment.equipmentName,
        )
    }

    fun deleteExerciseEquipment(
        exerciseName: String,
        equipmentName: String,
    ): Mono<ExerciseEquipment> {
        logger.debug("Deleting exercise equipment: {} - {}", exerciseName, equipmentName)
        return postgresClient.update(
            "DELETE FROM exercise_equipment WHERE exercise_name=$1 AND equipment_name=$2",
            exerciseName,
            equipmentName,
        )
    }
}
