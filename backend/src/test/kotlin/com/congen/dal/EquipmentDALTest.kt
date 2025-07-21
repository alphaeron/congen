package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockEquipment
import com.congen.model.Equipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EquipmentDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var equipmentDAL: EquipmentDAL

    private val equipment = mockEquipment()
    private val equipmentList = listOf(equipment, mockEquipment(name = "Dumbbell"))

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        equipmentDAL = EquipmentDAL(postgresClient)
    }

    @Test
    fun `selectEquipmentByName should return equipment`() {
        whenever(postgresClient.selectIndividual<Equipment>("SELECT * FROM equipment WHERE name=$1", equipment.name))
            .thenReturn(Mono.just(equipment))

        val result = equipmentDAL.selectEquipmentByName(equipment.name)

        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Equipment>("SELECT * FROM equipment WHERE name=$1", equipment.name)
    }

    @Test
    fun `selectEquipment should return list of equipment`() {
        whenever(postgresClient.select<Equipment>("SELECT * FROM equipment"))
            .thenReturn(Mono.just(equipmentList))

        val result = equipmentDAL.selectEquipment()

        StepVerifier.create(result)
            .expectNext(equipmentList)
            .verifyComplete()
        verify(postgresClient).select<Equipment>("SELECT * FROM equipment")
    }

    @Test
    fun `insertEquipment should return inserted equipment`() {
        val expectedQuery =
            """
            INSERT INTO equipment
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(postgresClient.update<Equipment>(expectedQuery, equipment.name, equipment.description))
            .thenReturn(Mono.just(equipment))

        val result = equipmentDAL.insertEquipment(equipment.name, equipment.description)

        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()
        verify(postgresClient).update<Equipment>(expectedQuery, equipment.name, equipment.description)
    }

    @Test
    fun `updateEquipment should return updated equipment`() {
        val updatedEquipment = mockEquipment(description = "Updated description")
        val expectedQuery =
            """
            UPDATE equipment
            SET description=$2
            WHERE name=$1
            """.trimIndent()

        whenever(postgresClient.update<Equipment>(expectedQuery, updatedEquipment.name, updatedEquipment.description))
            .thenReturn(Mono.just(updatedEquipment))

        val result = equipmentDAL.updateEquipment(updatedEquipment)

        StepVerifier.create(result)
            .expectNext(updatedEquipment)
            .verifyComplete()
        verify(postgresClient).update<Equipment>(expectedQuery, updatedEquipment.name, updatedEquipment.description)
    }

    @Test
    fun `deleteEquipment should return deleted equipment`() {
        whenever(postgresClient.update<Equipment>("DELETE FROM equipment WHERE name=$1", equipment.name))
            .thenReturn(Mono.just(equipment))

        val result = equipmentDAL.deleteEquipment(equipment.name)

        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()
        verify(postgresClient).update<Equipment>("DELETE FROM equipment WHERE name=$1", equipment.name)
    }
}
