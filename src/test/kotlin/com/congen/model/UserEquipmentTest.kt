package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserEquipmentTest {
    @Test
    fun `should create user equipment with correct properties`() {
        val userEquipment = UserEquipment(userId = 1, equipmentName = "Barbell")
        assertEquals(1, userEquipment.userId)
        assertEquals("Barbell", userEquipment.equipmentName)
    }
}
