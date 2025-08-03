package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for UserWeakMuscle model.
 */
class UserWeakMuscleTest {
    private val objectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())

    @Test
    fun `should construct and compare UserWeakMuscle`() {
        val now = Instant.now()
        val a = UserWeakMuscle("test-keycloak-user-id", "Hamstrings", now)
        val b = UserWeakMuscle("test-keycloak-user-id", "Hamstrings", now)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `should serialize and deserialize UserWeakMuscle`() {
        val now = Instant.parse("2024-01-01T00:00:00Z")
        val original = UserWeakMuscle("test-keycloak-user-id-2", "hamstrings", now)
        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue<UserWeakMuscle>(json)
        assertEquals(original, deserialized)
    }
}
