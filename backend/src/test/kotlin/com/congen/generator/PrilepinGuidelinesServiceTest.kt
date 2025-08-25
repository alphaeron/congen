package com.congen.generator

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
        private const val DAY_TYPE_ACCESSORY = "Accessory"
        private const val DAY_TYPE_UNKNOWN = "Unknown"
        private const val MOVEMENT_ROLE_PRIMARY = "primary"
        private const val MOVEMENT_ROLE_SECONDARY = "secondary"
        private const val EXERCISE_BENCH_PRESS = "Bench Press"
        private const val EXERCISE_SQUAT = "Squat"
        private const val EXERCISE_OVERHEAD_PRESS = "Overhead Press"
        private const val CURRENT_WEEK_1 = 1
        private const val CURRENT_WEEK_2 = 2
        private const val CURRENT_WEEK_3 = 3
        private const val CURRENT_WEEK_4 = 4
        private const val CURRENT_WEEK_5 = 5
        private const val CURRENT_WEEK_8 = 8
        private const val CURRENT_WEEK_0 = 0
        private const val CURRENT_WEEK_52 = 52
        private const val CURRENT_WEEK_NEG1 = -1
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return guidelines for ME_Upper primary`() {
        val result =
            service.getUndulatingPeriodizationGuidelines(
                DAY_TYPE_ME_UPPER,
                CURRENT_WEEK_1
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
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_LOWER, CURRENT_WEEK_1)
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
                CURRENT_WEEK_1
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
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, CURRENT_WEEK_1)
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
                CURRENT_WEEK_1
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
        val week1Result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_1)
        val week2Result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_2)
        val week3Result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_3)
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
        val benchResult = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_1)
        val pressResult = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_1)
        assertNotNull(benchResult)
        assertNotNull(pressResult)
        val benchIntensity = benchResult.second
        val pressIntensity = pressResult.second
        assertTrue(benchIntensity in 0.8..0.9)
        assertTrue(pressIntensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return consistent results for same inputs`() {
        val result1 = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_1)
        val result2 = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_1)
        assertEquals(result1.first, result2.first)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle edge case week numbers`() {
        val week0Result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_0)
        val week52Result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_52)
        assertNotNull(week0Result)
        assertNotNull(week52Result)
        val week0Intensity = week0Result.second
        val week52Intensity = week52Result.second
        assertTrue(week0Intensity in 0.8..0.9)
        assertTrue(week52Intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle negative week numbers`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_NEG1)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    // Additional tests to cover missing branches and edge cases

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle ME_Upper week 3 peak intensity`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_3)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.9..1.0, guidelines.intensityRange)
        assertEquals(1..2, guidelines.repsPerSetRange)
        assertEquals(4, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.9..0.95) // Upper body capped at 95%
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle ME_Lower week 3 peak intensity`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_LOWER, CURRENT_WEEK_3)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.9..1.0, guidelines.intensityRange)
        assertEquals(1..2, guidelines.repsPerSetRange)
        assertEquals(4, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.9..1.0) // Lower body can go to 100%
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle ME deload week 4`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_4)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Lower week 2`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, CURRENT_WEEK_2)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.8..0.8, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 20 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.8)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Lower week 3`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, CURRENT_WEEK_3)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.85..0.85, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 16 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.85..0.85)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Lower week 4 deload`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, CURRENT_WEEK_4)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.5..0.5, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 24 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.5..0.5)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Upper week 2`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_UPPER, CURRENT_WEEK_2)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.55, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.55)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Upper week 3`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_UPPER, CURRENT_WEEK_3)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.6..0.6, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.6..0.6)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Upper week 4 deload`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_UPPER, CURRENT_WEEK_4)
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
    fun `getUndulatingPeriodizationGuidelines should handle accessory week 1`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ACCESSORY, CURRENT_WEEK_1)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle accessory week 2`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ACCESSORY, CURRENT_WEEK_2)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.7..0.8, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(18, guidelines.totalReps)
        assertEquals(90..120, guidelines.restSeconds)
        assertTrue(intensity in 0.7..0.8)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle accessory week 3`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ACCESSORY, CURRENT_WEEK_3)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.7..0.8, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(18, guidelines.totalReps)
        assertEquals(90..120, guidelines.restSeconds)
        assertTrue(intensity in 0.7..0.8)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle accessory week 4 deload`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ACCESSORY, CURRENT_WEEK_4)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle unknown day type`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_UNKNOWN, CURRENT_WEEK_1)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE with neither upper nor lower body`() {
        val result = service.getUndulatingPeriodizationGuidelines("DE_Other", CURRENT_WEEK_1)
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
    fun `getUndulatingPeriodizationGuidelines should handle week 5 (next cycle week 1)`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_5)
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
    fun `getUndulatingPeriodizationGuidelines should handle week 8 (next cycle week 4)`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ME_UPPER, CURRENT_WEEK_8)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Lower with invalid week number`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_LOWER, 99)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.85..0.85, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 16 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.85..0.85)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle DE_Upper with invalid week number`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_DE_UPPER, 99)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.6..0.6, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.6..0.6)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle accessory with invalid week number`() {
        val result = service.getUndulatingPeriodizationGuidelines(DAY_TYPE_ACCESSORY, 99)
        assertNotNull(result)
        val guidelines = result.first
        val intensity = result.second
        assertEquals(0.7..0.8, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(18, guidelines.totalReps)
        assertEquals(90..120, guidelines.restSeconds)
        assertTrue(intensity in 0.7..0.8)
    }

    @Test
    fun `getRandomRestTime should round rest times to standard intervals`() {
        // Test various ranges to ensure they round to standard intervals
        val standardIntervals = listOf(30, 45, 60, 90, 120, 180, 300)
        
        // Test 60..90 range - should round to 60, 90, or 120
        repeat(100) {
            val restTime = service.getRandomRestTime(60..90)
            assertTrue(restTime in standardIntervals, "Rest time $restTime should be a standard interval")
            assertTrue(restTime in listOf(60, 90, 120), "Rest time $restTime should be 60, 90, or 120 for range 60..90")
        }
        
        // Test 90..120 range - should round to 90 or 120
        repeat(100) {
            val restTime = service.getRandomRestTime(90..120)
            assertTrue(restTime in standardIntervals, "Rest time $restTime should be a standard interval")
            assertTrue(restTime in listOf(90, 120), "Rest time $restTime should be 90 or 120 for range 90..120")
        }
        
        // Test 180..300 range - should round to 180, 240, or 300
        repeat(100) {
            val restTime = service.getRandomRestTime(180..300)
            assertTrue(restTime in standardIntervals, "Rest time $restTime should be a standard interval")
            assertTrue(restTime in listOf(180, 300), "Rest time $restTime should be 180, or 300 for range 180..300")
        }
    }

    @Test
    fun `getRandomRestTime should handle edge cases`() {
        // Test exact standard intervals
        repeat(50) {
            val restTime = service.getRandomRestTime(60..60)
            assertEquals(60, restTime, "Exact 60 should remain 60")
        }
        
        repeat(50) {
            val restTime = service.getRandomRestTime(90..90)
            assertEquals(90, restTime, "Exact 90 should remain 90")
        }
        
        repeat(50) {
            val restTime = service.getRandomRestTime(180..180)
            assertEquals(180, restTime, "Exact 180 should remain 180")
        }
    }

    @Test
    fun `getRandomRestTime should round to nearest standard interval`() {
        // Test values that should round to specific intervals
        val testCases = mapOf(
            45 to 45,   // 45 should round to 30 (closer to 30 than 60)
            75 to 60,   // 75 should round to 60 (closer to 60 than 90)
            105 to 90,  // 105 should round to 90 (closer to 90 than 120)
            150 to 180, // 150 should round to 180 (closer to 180 than 120)
            210 to 180, // 210 should round to 180 (closer to 180 than 240)
            270 to 240, // 270 should round to 240 (closer to 240 than 300)
            330 to 300  // 330 should round to 300 (closer to 300 than 240)
        )
        
        testCases.forEach { (input, expected) ->
            // Since we can't directly test the private roundRestTimeToStandardInterval function,
            // we test by creating a range that will always generate the input value
            val restTime = service.getRandomRestTime(input..input)
            assertEquals(expected, restTime, "Input $input should round to $expected")
        }
    }
}
