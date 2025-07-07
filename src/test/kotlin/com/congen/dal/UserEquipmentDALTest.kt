package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

class UserEquipmentDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private val now = Instant.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userEquipmentDAL = UserEquipmentDAL(postgresClient)
    }

    @Test
    fun `selectUserEquipment should return user equipment`() {
        val userEquipment =
            UserEquipment(
                userId = 1,
                equipmentName = "Barbell",
                createdAt = now
            )
        whenever(
            postgresClient.selectIndividual<UserEquipment>(
                "SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
                1,
                "Barbell",
            ),
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentDAL.selectUserEquipment(1, "Barbell")
        StepVerifier.create(result).expectNext(userEquipment).verifyComplete()
        verify(
            postgresClient,
        ).selectIndividual<UserEquipment>("SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2", 1, "Barbell")
    }

    @Test
    fun `selectUserEquipmentByUser should return list of user equipment`() {
        val userEquipmentList =
            listOf(
                UserEquipment(
                    userId = 1,
                    equipmentName = "Barbell",
                    createdAt = now
                )
            )
        whenever(
            postgresClient.select<UserEquipment>("SELECT * FROM user_equipment WHERE user_id=$1", 1),
        ).thenReturn(Mono.just(userEquipmentList))
        val result = userEquipmentDAL.selectUserEquipmentByUser(1)
        StepVerifier.create(result).expectNext(userEquipmentList).verifyComplete()
        verify(postgresClient).select<UserEquipment>("SELECT * FROM user_equipment WHERE user_id=$1", 1)
    }

    @Test
    fun `insertUserEquipment should return inserted user equipment`() {
        val userEquipment =
            UserEquipment(
                userId = 1,
                equipmentName = "Barbell",
                createdAt = now
            )
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
        val userEquipment =
            UserEquipment(
                userId = 1,
                equipmentName = "Barbell",
                createdAt = now
            )
        whenever(
            postgresClient.update<UserEquipment>(
                "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
                1,
                "Barbell",
            ),
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentDAL.deleteUserEquipment(1, "Barbell")
        StepVerifier.create(result).expectNext(userEquipment).verifyComplete()
        verify(
            postgresClient,
        ).update<UserEquipment>(
            "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            1,
            "Barbell",
        )
    }
}
