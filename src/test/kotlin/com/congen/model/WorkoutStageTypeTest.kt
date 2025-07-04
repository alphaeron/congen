package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutStageTypeTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create workout stage type with all properties`() {
        // Given & When
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = "Warmup",
            )

        // Then
        assertEquals(1, workoutStageType.id)
        assertEquals("Warmup", workoutStageType.name)
    }

    @Test
    fun `should create workout stage type without id`() {
        // Given & When
        val workoutStageType =
            WorkoutStageType(
                name = "Warmup",
            )

        // Then
        assertNull(workoutStageType.id)
        assertEquals("Warmup", workoutStageType.name)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = "Warmup",
            )

        // When
        val json = objectMapper.writeValueAsString(workoutStageType)

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"name\":\"Warmup\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Warmup"
            }
            """.trimIndent()

        // When
        val workoutStageType = objectMapper.readValue(json, WorkoutStageType::class.java)

        // Then
        assertEquals(1, workoutStageType.id)
        assertEquals("Warmup", workoutStageType.name)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "Warmup",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val workoutStageType = objectMapper.readValue(json, WorkoutStageType::class.java)

        // Then
        assertEquals(1, workoutStageType.id)
        assertEquals("Warmup", workoutStageType.name)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val workoutStageType1 =
            WorkoutStageType(
                id = 1,
                name = "Warmup",
            )
        val workoutStageType2 =
            WorkoutStageType(
                id = 1,
                name = "Warmup",
            )
        val workoutStageType3 =
            WorkoutStageType(
                id = 2,
                name = "Primary",
            )

        // Then
        assertEquals(workoutStageType1, workoutStageType2)
        assertEquals(workoutStageType1.hashCode(), workoutStageType2.hashCode())
        assertFalse(workoutStageType1 == workoutStageType3)
        assertFalse(workoutStageType1.hashCode() == workoutStageType3.hashCode())
    }
}
