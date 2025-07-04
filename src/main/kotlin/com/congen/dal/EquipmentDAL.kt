package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Equipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for Equipment entity operations.
 *
 * This class provides database operations for the Equipment entity in the Congen application.
 * Equipment represents physical items or tools that can be used during exercises,
 * such as dumbbells, barbells, resistance bands, or bodyweight exercises.
 *
 * ## Equipment Entity
 *
 * Equipment represents:
 * - Physical items or tools used in exercises
 * - Name and description of the equipment
 * - Used for exercise categorization and workout generation
 *
 * ## Database Operations
 *
 * - **Select by name**: Retrieve equipment by its unique name
 * - **Select all**: Retrieve all equipment records
 * - **Insert**: Create new equipment records
 * - **Update**: Modify existing equipment descriptions
 * - **Delete**: Remove equipment records
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When equipment with specified name doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class EquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(EquipmentDAL::class.java)
    }

    /**
     * Retrieves equipment by its unique name.
     *
     * This method queries the database to find equipment with the specified name.
     * If no equipment exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param equipmentName The unique name of the equipment to retrieve
     * @return Mono containing the equipment if found
     * @throws NoResultsFoundException when equipment with the specified name doesn't exist
     */
    fun selectEquipmentByName(equipmentName: String): Mono<Equipment> {
        logger.debug("Selecting equipment by name: {}", equipmentName)
        return postgresClient.selectIndividual(
            "SELECT * FROM equipment WHERE name=$1",
            equipmentName,
        )
    }

    /**
     * Retrieves all equipment records from the database.
     *
     * This method fetches all equipment records and returns them as a list.
     * If no equipment exists, an empty list is returned.
     *
     * @return Mono containing a list of all equipment
     */
    fun selectEquipment(): Mono<List<Equipment>> {
        logger.debug("Selecting all equipment")
        return postgresClient.select("SELECT * FROM equipment")
    }

    /**
     * Creates a new equipment record in the database.
     *
     * This method inserts a new equipment record with the provided name and description.
     * The equipment name must be unique in the database.
     *
     * @param equipment The equipment object containing name and description
     * @return Mono containing the created equipment
     * @throws DatabaseException when the equipment name already exists or database operation fails
     */
    fun insertEquipment(equipment: Equipment): Mono<Equipment> {
        logger.debug("Inserting equipment: {}", equipment.name)
        return postgresClient.update(
            """
            INSERT INTO equipment
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent(),
            equipment.name,
            equipment.description,
        )
    }

    /**
     * Updates an existing equipment record in the database.
     *
     * This method modifies the description of equipment with the specified name.
     * If no equipment exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param equipment The equipment object containing name and updated description
     * @return Mono containing the updated equipment
     * @throws NoResultsFoundException when equipment with the specified name doesn't exist
     */
    fun updateEquipment(equipment: Equipment): Mono<Equipment> {
        logger.debug("Updating equipment: {}", equipment.name)
        return postgresClient.update(
            """
            UPDATE equipment
            SET description=$2
            WHERE name=$1
            """.trimIndent(),
            equipment.name,
            equipment.description,
        )
    }

    /**
     * Deletes an equipment record from the database.
     *
     * This method removes the equipment record with the specified name.
     * If no equipment exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param equipmentName The unique name of the equipment to delete
     * @return Mono containing the deleted equipment
     * @throws NoResultsFoundException when equipment with the specified name doesn't exist
     */
    fun deleteEquipment(equipmentName: String): Mono<Equipment> {
        logger.debug("Deleting equipment: {}", equipmentName)
        return postgresClient.update(
            "DELETE FROM equipment WHERE name=$1",
            equipmentName,
        )
    }
}
