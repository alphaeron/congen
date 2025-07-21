package com.congen.model

import com.congen.mockWorkoutStage
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class WorkoutStageTest {
    private val now = Instant.now()

    @Test
    fun `should create workout stage with valid parameters`() {
        val workoutStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Main Lift",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, workoutStage.id)
        assertEquals(5L, workoutStage.programmedWorkoutId)
        assertEquals(1, workoutStage.stageTypeId)
        assertEquals(1, workoutStage.position)
        assertEquals("Main Lift", workoutStage.name)
        assertEquals(now, workoutStage.createdAt)
        assertEquals(now, workoutStage.updatedAt)
    }

    @Test
    fun `should create workout stage with accessory type`() {
        val workoutStage =
            mockWorkoutStage(
                id = 2L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2,
                name = "Accessory",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(2, workoutStage.stageTypeId)
        assertEquals(2, workoutStage.position)
        assertEquals("Accessory", workoutStage.name)
    }

    @Test
    fun `should create workout stage with warmup type`() {
        val workoutStage =
            mockWorkoutStage(
                id = 3L,
                programmedWorkoutId = 5L,
                stageTypeId = 3,
                position = 0,
                name = "Warmup",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(3, workoutStage.stageTypeId)
        assertEquals(0, workoutStage.position)
        assertEquals("Warmup", workoutStage.name)
    }

    @Test
    fun `should create workout stage with cooldown type`() {
        val workoutStage =
            mockWorkoutStage(
                id = 4L,
                programmedWorkoutId = 5L,
                stageTypeId = 4,
                position = 3,
                name = "Cooldown",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(4, workoutStage.stageTypeId)
        assertEquals(3, workoutStage.position)
        assertEquals("Cooldown", workoutStage.name)
    }

    @Test
    fun `should handle zero position`() {
        val workoutStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 3,
                position = 0,
                name = "Warmup",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(0, workoutStage.position)
    }

    @Test
    fun `should handle high position values`() {
        val workoutStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 10,
                name = "Accessory",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(10, workoutStage.position)
    }

    @Test
    fun `should handle different timestamps`() {
        val createdAt = Instant.parse("2024-01-01T10:00:00Z")
        val updatedAt = Instant.parse("2024-01-02T15:30:00Z")

        val workoutStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Main Lift",
                createdAt = createdAt,
                updatedAt = updatedAt
            )

        assertEquals(createdAt, workoutStage.createdAt)
        assertEquals(updatedAt, workoutStage.updatedAt)
    }

    @Test
    fun `should handle same timestamps`() {
        val workoutStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Main Lift",
                createdAt = now,
                updatedAt = now
            )

        assertEquals(now, workoutStage.createdAt)
        assertEquals(now, workoutStage.updatedAt)
    }
}
