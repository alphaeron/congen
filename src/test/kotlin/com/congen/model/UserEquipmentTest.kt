package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserEquipmentTest {
    private val now = LocalDateTime.now()

    @Test
    fun `should create user equipment with correct properties`() {
        val userEquipment = UserEquipment(
            userId = 1, 
            equipmentName = "Barbell",
            createdAt = now
        )
        assertEquals(1, userEquipment.userId)
        assertEquals("Barbell", userEquipment.equipmentName)
        assertEquals(now, userEquipment.createdAt)
    }
}
