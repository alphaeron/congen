package com.congen.config

import com.congen.model.MovementType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for MovementTypeDeserializer.
 *
 * Tests the deserialization of MovementType enum from both enum names and display names.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class MovementTypeDeserializerTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should deserialize enum name HORIZONTAL_PUSH`() {
        val result = objectMapper.readValue("\"HORIZONTAL_PUSH\"", MovementType::class.java)
        assertEquals(MovementType.HORIZONTAL_PUSH, result)
    }

    @Test
    fun `should deserialize display name horizontal_push`() {
        val result = objectMapper.readValue("\"horizontal_push\"", MovementType::class.java)
        assertEquals(MovementType.HORIZONTAL_PUSH, result)
    }

    @Test
    fun `should deserialize enum name VERTICAL_PUSH`() {
        val result = objectMapper.readValue("\"VERTICAL_PUSH\"", MovementType::class.java)
        assertEquals(MovementType.VERTICAL_PUSH, result)
    }

    @Test
    fun `should deserialize display name vertical_push`() {
        val result = objectMapper.readValue("\"vertical_push\"", MovementType::class.java)
        assertEquals(MovementType.VERTICAL_PUSH, result)
    }

    @Test
    fun `should deserialize enum name SQUAT`() {
        val result = objectMapper.readValue("\"SQUAT\"", MovementType::class.java)
        assertEquals(MovementType.SQUAT, result)
    }

    @Test
    fun `should deserialize display name squat`() {
        val result = objectMapper.readValue("\"squat\"", MovementType::class.java)
        assertEquals(MovementType.SQUAT, result)
    }

    @Test
    fun `should handle case insensitive display names`() {
        val result = objectMapper.readValue("\"HORIZONTAL_PUSH\"", MovementType::class.java)
        assertEquals(MovementType.HORIZONTAL_PUSH, result)
    }

    @Test
    fun `should return null for invalid value`() {
        val result = objectMapper.readValue("\"invalid_movement\"", MovementType::class.java)
        assertNull(result)
    }

    @Test
    fun `should return null for null value`() {
        val result = objectMapper.readValue("null", MovementType::class.java)
        assertNull(result)
    }
}
