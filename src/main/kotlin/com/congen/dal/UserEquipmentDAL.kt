package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserEquipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserEquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserEquipmentDAL::class.java)
    }

    fun selectUserEquipment(
        userId: Int,
        equipmentName: String,
    ): Mono<UserEquipment> {
        logger.debug("Selecting user equipment: {} - {}", userId, equipmentName)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            userId,
            equipmentName,
        )
    }

    fun selectUserEquipmentByUser(userId: Int): Mono<List<UserEquipment>> {
        logger.debug("Selecting equipment for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_equipment WHERE user_id=$1",
            userId,
        )
    }

    fun insertUserEquipment(userEquipment: UserEquipment): Mono<UserEquipment> {
        logger.debug("Inserting user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName)
        return postgresClient.update(
            """
            INSERT INTO user_equipment
                (user_id, equipment_name)
            VALUES
                ($1, $2)
            RETURNING user_id, equipment_name
            """.trimIndent(),
            userEquipment.userId,
            userEquipment.equipmentName,
        )
    }

    fun deleteUserEquipment(
        userId: Int,
        equipmentName: String,
    ): Mono<UserEquipment> {
        logger.debug("Deleting user equipment: {} - {}", userId, equipmentName)
        return postgresClient.update(
            "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2 RETURNING user_id, equipment_name",
            userId,
            equipmentName,
        )
    }
}
