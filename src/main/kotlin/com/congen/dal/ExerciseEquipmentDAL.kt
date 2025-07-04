package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseEquipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ExerciseEquipment entity operations.
 *
 * This class provides database operations for the ExerciseEquipment entity in the Congen application.
 * ExerciseEquipment represents the many-to-many relationship between exercises and equipment,
 * indicating which equipment can be used for specific exercises.
 *
 * ## ExerciseEquipment Entity
 *
 * ExerciseEquipment represents:
 * - Association between exercises and equipment
 * - Many-to-many relationship mapping
 * - Used for exercise filtering and workout generation
 *
 * ## Database Operations
 *
 * - **Select by exercise and equipment**: Retrieve specific exercise-equipment relationship
 * - **Select by exercise**: Retrieve all equipment for a specific exercise
 * - **Select by equipment**: Retrieve all exercises for a specific equipment
 * - **Select all**: Retrieve all exercise-equipment relationships
 * - **Insert**: Create new exercise-equipment relationships
 * - **Delete**: Remove exercise-equipment relationships
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When exercise-equipment relationship doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExerciseEquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExerciseEquipmentDAL::class.java)
    }

    /**
     * Retrieves a specific exercise-equipment relationship.
     *
     * This method queries the database to find the relationship between the specified
     * exercise and equipment. If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param equipmentName The name of the equipment
     * @return Mono containing the exercise-equipment relationship if found
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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

    /**
     * Retrieves all equipment for a specific exercise.
     *
     * This method fetches all equipment that can be used with the specified exercise.
     * If no equipment exists for the exercise, an empty list is returned.
     *
     * @param exerciseName The name of the exercise
     * @return Mono containing a list of exercise-equipment relationships
     */
    fun selectExerciseEquipmentByExercise(exerciseName: String): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting equipment for exercise: {}", exerciseName)
        return postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE exercise_name=$1",
            exerciseName,
        )
    }

    /**
     * Retrieves all exercises for a specific equipment.
     *
     * This method fetches all exercises that can be performed with the specified equipment.
     * If no exercises exist for the equipment, an empty list is returned.
     *
     * @param equipmentName The name of the equipment
     * @return Mono containing a list of exercise-equipment relationships
     */
    fun selectExerciseEquipmentByEquipment(equipmentName: String): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting exercises for equipment: {}", equipmentName)
        return postgresClient.select(
            "SELECT * FROM exercise_equipment WHERE equipment_name=$1",
            equipmentName,
        )
    }

    /**
     * Retrieves all exercise-equipment relationships from the database.
     *
     * This method fetches all exercise-equipment relationships and returns them as a list.
     * If no relationships exist, an empty list is returned.
     *
     * @return Mono containing a list of all exercise-equipment relationships
     */
    fun selectAllExerciseEquipment(): Mono<List<ExerciseEquipment>> {
        logger.debug("Selecting all exercise equipment relationships")
        return postgresClient.select("SELECT * FROM exercise_equipment")
    }

    /**
     * Creates a new exercise-equipment relationship in the database.
     *
     * This method inserts a new relationship between the specified exercise and equipment.
     * The combination of exercise name and equipment name must be unique.
     *
     * @param exerciseEquipment The exercise-equipment relationship to create
     * @return Mono containing the created exercise-equipment relationship
     * @throws DatabaseException when the relationship already exists or database operation fails
     */
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

    /**
     * Deletes an exercise-equipment relationship from the database.
     *
     * This method removes the relationship between the specified exercise and equipment.
     * If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The name of the exercise
     * @param equipmentName The name of the equipment
     * @return Mono containing the deleted exercise-equipment relationship
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
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
