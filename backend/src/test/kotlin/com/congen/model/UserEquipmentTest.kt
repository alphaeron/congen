package com.congen.model

import com.congen.mockUserEquipment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class UserEquipmentTest {
    private val now = Instant.now()

    @Test
    fun `should create user equipment with correct properties`() {
        val userEquipment =
            mockUserEquipment(
                userId = 1,
                equipmentName = "Barbell",
                createdAt = now
            )

        assertEquals(1, userEquipment.userId)
        assertEquals("Barbell", userEquipment.equipmentName)
        assertEquals(now, userEquipment.createdAt)
    }
}
