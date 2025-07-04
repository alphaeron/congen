package com.congen.dal

import com.congen.client.PostgresClient
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

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        equipmentDAL = EquipmentDAL(postgresClient)
    }

    @Test
    fun `selectEquipmentByName should return equipment`() {
        // Given
        val equipmentName = "Barbell"
        val equipment =
            Equipment(
                name = equipmentName,
                description = "A barbell for weightlifting",
            )

        whenever(
            postgresClient.selectIndividual<Equipment>(
                "SELECT * FROM equipment WHERE name=$1",
                equipmentName,
            ),
        ).thenReturn(Mono.just(equipment))

        // When
        val result = equipmentDAL.selectEquipmentByName(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()

        verify(postgresClient).selectIndividual<Equipment>(
            "SELECT * FROM equipment WHERE name=$1",
            equipmentName,
        )
    }

    @Test
    fun `selectEquipment should return list of equipment`() {
        // Given
        val equipmentList =
            listOf(
                Equipment(
                    name = "Barbell",
                    description = "A barbell for weightlifting",
                ),
                Equipment(
                    name = "Dumbbell",
                    description = "A dumbbell for weightlifting",
                ),
            )

        whenever(postgresClient.select<Equipment>("SELECT * FROM equipment")).thenReturn(Mono.just(equipmentList))

        // When
        val result = equipmentDAL.selectEquipment()

        // Then
        StepVerifier.create(result)
            .expectNext(equipmentList)
            .verifyComplete()

        verify(postgresClient).select<Equipment>("SELECT * FROM equipment")
    }

    @Test
    fun `insertEquipment should return inserted equipment`() {
        // Given
        val equipment =
            Equipment(
                name = "Barbell",
                description = "A barbell for weightlifting",
            )

        val expectedQuery =
            """
            INSERT INTO equipment
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent()

        whenever(
            postgresClient.update<Equipment>(
                expectedQuery,
                equipment.name,
                equipment.description,
            ),
        ).thenReturn(Mono.just(equipment))

        // When
        val result = equipmentDAL.insertEquipment(equipment)

        // Then
        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()

        verify(postgresClient).update<Equipment>(
            expectedQuery,
            equipment.name,
            equipment.description,
        )
    }

    @Test
    fun `updateEquipment should return updated equipment`() {
        // Given
        val equipment =
            Equipment(
                name = "Barbell",
                description = "Updated barbell description",
            )

        val expectedQuery =
            """
            UPDATE equipment
            SET description=$2
            WHERE name=$1
            """.trimIndent()

        whenever(
            postgresClient.update<Equipment>(
                expectedQuery,
                equipment.name,
                equipment.description,
            ),
        ).thenReturn(Mono.just(equipment))

        // When
        val result = equipmentDAL.updateEquipment(equipment)

        // Then
        StepVerifier.create(result)
            .expectNext(equipment)
            .verifyComplete()

        verify(postgresClient).update<Equipment>(
            expectedQuery,
            equipment.name,
            equipment.description,
        )
    }

    @Test
    fun `deleteEquipment should return deleted equipment`() {
        // Given
        val equipmentName = "Barbell"
        val deletedEquipment =
            Equipment(
                name = equipmentName,
                description = "A barbell for weightlifting",
            )

        whenever(
            postgresClient.update<Equipment>(
                "DELETE FROM equipment WHERE name=$1",
                equipmentName,
            ),
        ).thenReturn(Mono.just(deletedEquipment))

        // When
        val result = equipmentDAL.deleteEquipment(equipmentName)

        // Then
        StepVerifier.create(result)
            .expectNext(deletedEquipment)
            .verifyComplete()

        verify(postgresClient).update<Equipment>(
            "DELETE FROM equipment WHERE name=$1",
            equipmentName,
        )
    }
}
