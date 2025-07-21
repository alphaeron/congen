package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockUserEquipment
import com.congen.model.UserEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserEquipmentDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userEquipmentDAL: UserEquipmentDAL

    private val userEquipment = mockUserEquipment()
    private val userEquipmentList = listOf(userEquipment)

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userEquipmentDAL = UserEquipmentDAL(postgresClient)
    }

    @Test
    fun `selectUserEquipment should return user equipment`() {
        whenever(
            postgresClient.selectIndividual<UserEquipment>(
                "SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
                userEquipment.userId,
                userEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentDAL.selectUserEquipment(userEquipment.userId, userEquipment.equipmentName)
        StepVerifier.create(result).expectNext(userEquipment).verifyComplete()
        verify(
            postgresClient
        ).selectIndividual<UserEquipment>(
            "SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            userEquipment.userId,
            userEquipment.equipmentName
        )
    }

    @Test
    fun `selectUserEquipmentByUser should return list of user equipment`() {
        whenever(
            postgresClient.select<UserEquipment>("SELECT * FROM user_equipment WHERE user_id=$1", userEquipment.userId),
        ).thenReturn(Mono.just(userEquipmentList))
        val result = userEquipmentDAL.selectUserEquipmentByUser(userEquipment.userId)
        StepVerifier.create(result).expectNext(userEquipmentList).verifyComplete()
        verify(postgresClient).select<UserEquipment>("SELECT * FROM user_equipment WHERE user_id=$1", userEquipment.userId)
    }

    @Test
    fun `insertUserEquipment should return inserted user equipment`() {
        whenever(
            postgresClient.update<UserEquipment>(
                """
                INSERT INTO user_equipment
                    (user_id, equipment_name)
                VALUES
                    ($1, $2)
                """.trimIndent(),
                userEquipment.userId,
                userEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentDAL.insertUserEquipment(userEquipment.userId, userEquipment.equipmentName)
        StepVerifier.create(result).expectNext(userEquipment).verifyComplete()
        verify(postgresClient).update<UserEquipment>(
            """
            INSERT INTO user_equipment
                (user_id, equipment_name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            userEquipment.userId,
            userEquipment.equipmentName,
        )
    }

    @Test
    fun `deleteUserEquipment should return deleted user equipment`() {
        whenever(
            postgresClient.update<UserEquipment>(
                "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
                userEquipment.userId,
                userEquipment.equipmentName,
            ),
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentDAL.deleteUserEquipment(userEquipment.userId, userEquipment.equipmentName)
        StepVerifier.create(result).expectNext(userEquipment).verifyComplete()
        verify(postgresClient).update<UserEquipment>(
            "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            userEquipment.userId,
            userEquipment.equipmentName,
        )
    }
}
