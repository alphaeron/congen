package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class UserTest {
    private val now = LocalDateTime.now()

    @Test
    fun `should create user with correct properties`() {
        val user =
            User(
                id = 1,
                name = "John Doe",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            )
        assertEquals(1, user.id)
        assertEquals("John Doe", user.name)
        assertEquals(30, user.age)
        assertEquals(BigDecimal("180.5"), user.height)
        assertEquals(BigDecimal("75.0"), user.weight)
        assertEquals(now, user.createdAt)
        assertEquals(now, user.updatedAt)
    }
}
