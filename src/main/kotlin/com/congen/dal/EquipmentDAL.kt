package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Equipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EquipmentDAL::class.java)
    }

    fun selectEquipmentByName(equipmentName: String): Mono<Equipment> {
        logger.debug("Selecting equipment by name: {}", equipmentName)
        return postgresClient.selectIndividual(
            "SELECT * FROM equipment WHERE name=$1",
            equipmentName,
        )
    }

    fun selectEquipment(): Mono<List<Equipment>> {
        logger.debug("Selecting all equipment")
        return postgresClient.select("SELECT * FROM equipment")
    }

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

    fun deleteEquipment(equipmentName: String): Mono<Equipment> {
        logger.debug("Deleting equipment: {}", equipmentName)
        return postgresClient.update(
            "DELETE FROM equipment WHERE name=$1",
            equipmentName,
        )
    }
}
