package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class WorkoutStageTypeTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = Instant.now()

    @Test
    fun `should create workout stage type with valid parameters`() {
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        assertEquals(1, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.PRIMARY, workoutStageType.name)
        assertEquals(now, workoutStageType.createdAt)
    }

    @Test
    fun `should create workout stage type with accessory type`() {
        val workoutStageType =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.ACCESSORY,
                createdAt = now
            )

        assertEquals(2, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, workoutStageType.name)
    }

    @Test
    fun `should create workout stage type with warmup type`() {
        val workoutStageType =
            WorkoutStageType(
                id = 3,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = now
            )

        assertEquals(3, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.WARMUP, workoutStageType.name)
    }

    @Test
    fun `should create workout stage type with cooldown type`() {
        val workoutStageType =
            WorkoutStageType(
                id = 4,
                name = WorkoutStageTypeEnum.COOLDOWN,
                createdAt = now
            )

        assertEquals(4, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.COOLDOWN, workoutStageType.name)
    }

    @Test
    fun `should handle different timestamps`() {
        val createdAt = Instant.parse("2024-01-01T10:00:00Z")

        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = createdAt
            )

        assertEquals(createdAt, workoutStageType.createdAt)
    }

    @Test
    fun `should support data class copy`() {
        val originalType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val updatedType =
            originalType.copy(
                name = WorkoutStageTypeEnum.ACCESSORY
            )

        assertEquals(1, updatedType.id)
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, updatedType.name)
        assertEquals(now, updatedType.createdAt)
    }

    @Test
    fun `should support data class equality`() {
        val type1 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val type2 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val type3 =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.ACCESSORY,
                createdAt = now
            )

        assertEquals(type1, type2)
        assertNotNull(type1 != type3)
    }

    @Test
    fun `should support data class toString`() {
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val toString = workoutStageType.toString()
        assertNotNull(toString)
        assert(toString.contains("WorkoutStageType"))
        assert(toString.contains("id=1"))
        assert(toString.contains("name=PRIMARY"))
    }

    @Test
    fun `should support data class hashCode`() {
        val type1 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val type2 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        assertEquals(type1.hashCode(), type2.hashCode())
    }

    @Test
    fun `should support data class component functions`() {
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = now
            )

        val (id, name, createdAt) = workoutStageType

        assertEquals(1, id)
        assertEquals(WorkoutStageTypeEnum.PRIMARY, name)
        assertEquals(now, createdAt)
    }

    @Test
    fun `should serialize to JSON with snake_case`() {
        // Given
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = Instant.now()
            )

        // When
        val json = objectMapper.writeValueAsString(workoutStageType)

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"name\":\"WARMUP\""))
    }

    @Test
    fun `should deserialize from JSON with snake_case`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "WARMUP",
                "created_at": "2024-07-06T12:00:00Z"
            }
            """.trimIndent()

        // When
        val workoutStageType = objectMapper.readValue(json, WorkoutStageType::class.java)

        // Then
        assertEquals(1, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.WARMUP, workoutStageType.name)
        assertEquals(Instant.parse("2024-07-06T12:00:00Z"), workoutStageType.createdAt)
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        // Given
        val json =
            """
            {
                "id": 1,
                "name": "WARMUP",
                "created_at": "2024-07-06T12:00:00Z",
                "unknown_property": "should be ignored"
            }
            """.trimIndent()

        // When
        val workoutStageType = objectMapper.readValue(json, WorkoutStageType::class.java)

        // Then
        assertEquals(1, workoutStageType.id)
        assertEquals(WorkoutStageTypeEnum.WARMUP, workoutStageType.name)
        assertEquals(Instant.parse("2024-07-06T12:00:00Z"), workoutStageType.createdAt)
    }

    @Test
    fun `should have correct equals and hashCode`() {
        // Given
        val fixedTimestamp = Instant.parse("2024-01-01T10:00:00Z")
        val workoutStageType1 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = fixedTimestamp
            )
        val workoutStageType2 =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = fixedTimestamp
            )
        val workoutStageType3 =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = fixedTimestamp
            )

        // Then
        assertEquals(workoutStageType1, workoutStageType2)
        assertEquals(workoutStageType1.hashCode(), workoutStageType2.hashCode())
        assertFalse(workoutStageType1 == workoutStageType3)
        assertFalse(workoutStageType1.hashCode() == workoutStageType3.hashCode())
    }
}
