package com.congen.model

import com.congen.mockEquipment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EquipmentTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create equipment with all properties`() {
        val equipment = mockEquipment(name = "Barbell", description = "A barbell for weightlifting")

        assertEquals("Barbell", equipment.name)
        assertEquals("A barbell for weightlifting", equipment.description)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        val equipment = mockEquipment(name = "Barbell", description = "A barbell for weightlifting")

        val json = objectMapper.writeValueAsString(equipment)

        assertTrue(json.contains("\"name\":\"Barbell\""))
        assertTrue(json.contains("\"description\":\"A barbell for weightlifting\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        val json =
            """
            {
                "name": "Barbell",
                "description": "A barbell for weightlifting"
            }
            """.trimIndent()

        val equipment = objectMapper.readValue(json, Equipment::class.java)

        assertEquals("Barbell", equipment.name)
        assertEquals("A barbell for weightlifting", equipment.description)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json =
            """
            {
                "name": "Barbell",
                "description": "A barbell for weightlifting",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        val equipment = objectMapper.readValue(json, Equipment::class.java)

        assertEquals("Barbell", equipment.name)
        assertEquals("A barbell for weightlifting", equipment.description)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        val equipment1 = mockEquipment(name = "Barbell", description = "A barbell for weightlifting")
        val equipment2 = mockEquipment(name = "Barbell", description = "A barbell for weightlifting")
        val equipment3 = mockEquipment(name = "Dumbbell", description = "A dumbbell for weightlifting")

        assertEquals(equipment1, equipment2)
        assertEquals(equipment1.hashCode(), equipment2.hashCode())
        assertFalse(equipment1 == equipment3)
        assertFalse(equipment1.hashCode() == equipment3.hashCode())
    }
}
