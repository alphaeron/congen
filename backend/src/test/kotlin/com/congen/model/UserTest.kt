package com.congen.model

import com.congen.mockUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class UserTest {
    private val now = Instant.now()

    @Test
    fun `should create user with correct properties`() {
        val user =
            mockUser(
                keycloakId = "test-keycloak-id",
                name = "John Doe",
                createdAt = now,
                updatedAt = now
            )

        assertEquals("test-keycloak-id", user.keycloakId)
        assertEquals("John Doe", user.name)
        assertEquals(now, user.createdAt)
        assertEquals(now, user.updatedAt)
    }
}
