package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `should create user with correct properties`() {
        val user = User(id = 1, name = "John Doe", age = 30, height = 180.5, weight = 75.0)
        assertEquals(1, user.id)
        assertEquals("John Doe", user.name)
        assertEquals(30, user.age)
        assertEquals(180.5, user.height)
        assertEquals(75.0, user.weight)
    }
}
