package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Equipment
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    fun selectEquipmentByName(equipmentName: String): Mono<Equipment> =
        postgresClient.selectIndividual(
            "SELECT * FROM equipment WHERE name=$1",
            equipmentName
        )

    fun selectEquipment(): Mono<List<Equipment>> =
        postgresClient.select("SELECT * FROM equipment")

    fun insertEquipment(equipment: Equipment): Mono<Equipment> =
        postgresClient.update(
            """
                INSERT INTO equipment
                    (name, description)
                VALUES
                    ($1, $2)
            """.trimIndent(),
            equipment.name,
            equipment.description
        )

    fun updateEquipment(equipment: Equipment): Mono<Equipment> =
        postgresClient.update(
            """
                UPDATE equipment
                SET description=$2
                WHERE name=$1
            """.trimIndent(),
            equipment.name,
            equipment.description
        )

    fun deleteEquipment(equipmentName: String): Mono<Equipment> =
        postgresClient.update(
            "DELETE FROM equipment WHERE name=$1",
            equipmentName
        )
} 