package com.congen.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WorkoutStageTest {
    @Test
    fun `WorkoutStage should be created with all required fields`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(1L, workoutStage.id)
        assertEquals(5L, workoutStage.programmedWorkoutId)
        assertEquals(1, workoutStage.stageTypeId)
        assertEquals(1, workoutStage.position)
    }

    @Test
    fun `WorkoutStage should handle different stage types`() {
        val warmupStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1, // warm-up
                position = 1
            )

        val mainStage =
            WorkoutStage(
                id = 2L,
                programmedWorkoutId = 5L,
                stageTypeId = 2, // main
                position = 2
            )

        val cooldownStage =
            WorkoutStage(
                id = 3L,
                programmedWorkoutId = 5L,
                stageTypeId = 3, // cool-down
                position = 3
            )

        assertEquals(1, warmupStage.stageTypeId)
        assertEquals(2, mainStage.stageTypeId)
        assertEquals(3, cooldownStage.stageTypeId)
        assertEquals(1, warmupStage.position)
        assertEquals(2, mainStage.position)
        assertEquals(3, cooldownStage.position)
    }

    @Test
    fun `WorkoutStage should handle different positions`() {
        val stage1 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val stage2 =
            WorkoutStage(
                id = 2L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2
            )

        val stage3 =
            WorkoutStage(
                id = 3L,
                programmedWorkoutId = 5L,
                stageTypeId = 3,
                position = 3
            )

        assertEquals(1, stage1.position)
        assertEquals(2, stage2.position)
        assertEquals(3, stage3.position)
    }

    @Test
    fun `WorkoutStage should handle different workout IDs`() {
        val stage1 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                position = 1
            )

        val stage2 =
            WorkoutStage(
                id = 2L,
                programmedWorkoutId = 100L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(1L, stage1.programmedWorkoutId)
        assertEquals(100L, stage2.programmedWorkoutId)
    }

    @Test
    fun `WorkoutStage should handle different IDs`() {
        val stage1 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val stage2 =
            WorkoutStage(
                id = 999L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(1L, stage1.id)
        assertEquals(999L, stage2.id)
    }

    @Test
    fun `WorkoutStage should be created with minimum valid values`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(1L, workoutStage.id)
        assertEquals(1L, workoutStage.programmedWorkoutId)
        assertEquals(1, workoutStage.stageTypeId)
        assertEquals(1, workoutStage.position)
    }

    @Test
    fun `WorkoutStage should be created with maximum reasonable values`() {
        val workoutStage =
            WorkoutStage(
                id = Long.MAX_VALUE,
                programmedWorkoutId = Long.MAX_VALUE,
                stageTypeId = Int.MAX_VALUE,
                position = Int.MAX_VALUE
            )

        assertEquals(Long.MAX_VALUE, workoutStage.id)
        assertEquals(Long.MAX_VALUE, workoutStage.programmedWorkoutId)
        assertEquals(Int.MAX_VALUE, workoutStage.stageTypeId)
        assertEquals(Int.MAX_VALUE, workoutStage.position)
    }

    @Test
    fun `WorkoutStage should support data class copy`() {
        val originalStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val updatedStage =
            originalStage.copy(
                position = 2,
                stageTypeId = 2
            )

        assertEquals(1L, updatedStage.id)
        assertEquals(5L, updatedStage.programmedWorkoutId)
        assertEquals(2, updatedStage.stageTypeId)
        assertEquals(2, updatedStage.position)
    }

    @Test
    fun `WorkoutStage should support data class equality`() {
        val stage1 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val stage2 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val stage3 =
            WorkoutStage(
                id = 2L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(stage1, stage2)
        assertNotNull(stage1 != stage3)
    }

    @Test
    fun `WorkoutStage should support data class toString`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val toString = workoutStage.toString()
        assertNotNull(toString)
        assert(toString.contains("WorkoutStage"))
        assert(toString.contains("id=1"))
        assert(toString.contains("programmedWorkoutId=5"))
        assert(toString.contains("stageTypeId=1"))
        assert(toString.contains("position=1"))
    }

    @Test
    fun `WorkoutStage should support data class hashCode`() {
        val stage1 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val stage2 =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        assertEquals(stage1.hashCode(), stage2.hashCode())
    }

    @Test
    fun `WorkoutStage should support data class component functions`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        val (id, programmedWorkoutId, stageTypeId, position) = workoutStage

        assertEquals(1L, id)
        assertEquals(5L, programmedWorkoutId)
        assertEquals(1, stageTypeId)
        assertEquals(1, position)
    }
} 
