package com.congen.service.conjugate

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrilepinGuidelinesServiceTest {
    private val service = PrilepinGuidelinesService()

    private companion object {
        private const val DAY_TYPE_ME_UPPER = "ME_Upper"
        private const val DAY_TYPE_ME_LOWER = "ME_Lower"
        private const val DAY_TYPE_DE_UPPER = "DE_Upper"
        private const val DAY_TYPE_DE_LOWER = "DE_Lower"
        private const val MOVEMENT_ROLE_PRIMARY = "primary"
        private const val MOVEMENT_ROLE_SECONDARY = "secondary"
        private const val EXERCISE_BENCH_PRESS = "Bench Press"
        private const val EXERCISE_SQUAT = "Squat"
        private const val EXERCISE_OVERHEAD_PRESS = "Overhead Press"
        private const val CURRENT_WEEK_1 = 1
        private const val CURRENT_WEEK_2 = 2
        private const val CURRENT_WEEK_3 = 3
        private const val CURRENT_WEEK_0 = 0
        private const val CURRENT_WEEK_52 = 52
        private const val CURRENT_WEEK_NEG1 = -1
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for ME_Upper primary`() {
        val result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for ME_Lower primary`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_LOWER, MOVEMENT_ROLE_PRIMARY, CURRENT_WEEK_1, EXERCISE_SQUAT)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for DE_Upper primary`() {
        val result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_DE_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.5..0.5, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.5..0.5)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for DE_Lower primary`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, MOVEMENT_ROLE_PRIMARY, CURRENT_WEEK_1, EXERCISE_SQUAT)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.75..0.75, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 24 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.75..0.75)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for secondary movement`() {
        val result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_SECONDARY,
                CURRENT_WEEK_1,
                EXERCISE_OVERHEAD_PRESS
            )
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle different week numbers`() {
        val week1Result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        val week2Result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_2,
                EXERCISE_BENCH_PRESS
            )
        val week3Result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_3,
                EXERCISE_BENCH_PRESS
            )
        assertNotNull(week1Result)
        assertNotNull(week2Result)
        assertNotNull(week3Result)
        val week1Intensity = week1Result.second
        val week2Intensity = week2Result.second
        val week3Intensity = week3Result.second
        assertTrue(week1Intensity in 0.8..0.9)
        assertTrue(week2Intensity in 0.8..0.9)
        assertTrue(week3Intensity in 0.9..0.95)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle different exercises`() {
        val benchResult =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        val pressResult =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_OVERHEAD_PRESS
            )
        assertNotNull(benchResult)
        assertNotNull(pressResult)
        val benchIntensity = benchResult.second
        val pressIntensity = pressResult.second
        assertTrue(benchIntensity in 0.8..0.9)
        assertTrue(pressIntensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return consistent results for same inputs`() {
        val result1 =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        val result2 =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_1,
                EXERCISE_BENCH_PRESS
            )
        assertEquals(result1.first, result2.first)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle edge case week numbers`() {
        val week0Result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_0,
                EXERCISE_BENCH_PRESS
            )
        val week52Result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_52,
                EXERCISE_BENCH_PRESS
            )
        assertNotNull(week0Result)
        assertNotNull(week52Result)
        val week0Intensity = week0Result.second
        val week52Intensity = week52Result.second
        assertTrue(week0Intensity in 0.8..0.9)
        assertTrue(week52Intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle negative week numbers`() {
        val result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                MOVEMENT_ROLE_PRIMARY,
                CURRENT_WEEK_NEG1,
                EXERCISE_BENCH_PRESS
            )
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }
}
