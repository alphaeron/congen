package com.congen.service.conjugate

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrilepinGuidelinesServiceTest {
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService

    @BeforeEach
    fun setUp() {
        prilepinGuidelinesService = PrilepinGuidelinesService()
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Max Effort guidelines for ME_Upper week 1`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "primary",
                currentWeekNumber = 1,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Max Effort guidelines for ME_Lower week 1`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Lower",
                movementRole = "primary",
                currentWeekNumber = 1,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return peak intensity for ME_Upper week 3`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "primary",
                currentWeekNumber = 3,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.9..1.0, guidelines.intensityRange)
        assertEquals(1..2, guidelines.repsPerSetRange)
        assertEquals(4, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.9..0.95) // Upper body capped at 95%
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return peak intensity for ME_Lower week 3`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Lower",
                movementRole = "primary",
                currentWeekNumber = 3,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.9..1.0, guidelines.intensityRange)
        assertEquals(1..2, guidelines.repsPerSetRange)
        assertEquals(4, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.9..1.0) // Lower body can go to 100%
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return deload for ME_Upper week 4`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "primary",
                currentWeekNumber = 4,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Dynamic Effort guidelines for DE_Lower week 1`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Lower",
                movementRole = "primary",
                currentWeekNumber = 1,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.75, intensity)
        assertTrue(guidelines.repsPerSetRange.first == 2 || guidelines.repsPerSetRange.first == 5)
        assertTrue(guidelines.totalReps == 24 || guidelines.totalReps == 25) // 12*2 or 5*5
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Dynamic Effort guidelines for DE_Lower week 2`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Lower",
                movementRole = "primary",
                currentWeekNumber = 2,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.8, intensity)
        assertTrue(guidelines.repsPerSetRange.first == 2 || guidelines.repsPerSetRange.first == 5)
        assertTrue(guidelines.totalReps == 20 || guidelines.totalReps == 25) // 10*2 or 5*5
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Dynamic Effort guidelines for DE_Lower week 3`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Lower",
                movementRole = "primary",
                currentWeekNumber = 3,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.85, intensity)
        assertTrue(guidelines.repsPerSetRange.first == 2 || guidelines.repsPerSetRange.first == 5)
        assertTrue(guidelines.totalReps == 16 || guidelines.totalReps == 25) // 8*2 or 5*5
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Dynamic Effort guidelines for DE_Lower week 4`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Lower",
                movementRole = "primary",
                currentWeekNumber = 4,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.5..0.5, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 24 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertEquals(0.5, intensity)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Upper Body Dynamic Effort guidelines for DE_Upper week 1`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Upper",
                movementRole = "primary",
                currentWeekNumber = 1,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.5, intensity)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps) // 9*3
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Upper Body Dynamic Effort guidelines for DE_Upper week 2`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Upper",
                movementRole = "primary",
                currentWeekNumber = 2,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.55, intensity)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps) // 9*3
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Upper Body Dynamic Effort guidelines for DE_Upper week 3`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Upper",
                movementRole = "primary",
                currentWeekNumber = 3,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.6, intensity)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps) // 9*3
        assertEquals(60..90, guidelines.restSeconds)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Upper Body Dynamic Effort guidelines for DE_Upper week 4`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Upper",
                movementRole = "primary",
                currentWeekNumber = 4,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.5..0.5, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertEquals(0.5, intensity)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Accessory guidelines for accessory week 1`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "Accessory",
                movementRole = "accessory",
                currentWeekNumber = 1,
                exercise = "Push-ups"
            )

        assertNotNull(guidelines)
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Accessory guidelines for accessory week 2`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "Accessory",
                movementRole = "accessory",
                currentWeekNumber = 2,
                exercise = "Push-ups"
            )

        assertNotNull(guidelines)
        assertEquals(0.7..0.8, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(18, guidelines.totalReps)
        assertEquals(90..120, guidelines.restSeconds)
        assertTrue(intensity in 0.7..0.8)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should return Accessory guidelines for accessory week 4`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "Accessory",
                movementRole = "accessory",
                currentWeekNumber = 4,
                exercise = "Push-ups"
            )

        assertNotNull(guidelines)
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle week numbers beyond 4`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "primary",
                currentWeekNumber = 5,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle week numbers beyond 4 for DE_Lower`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Lower",
                movementRole = "primary",
                currentWeekNumber = 5,
                exercise = "Squat"
            )

        assertNotNull(guidelines)
        assertEquals(0.75..0.75, guidelines.intensityRange)
        assertTrue(guidelines.repsPerSetRange == 2..2 || guidelines.repsPerSetRange == 5..5)
        assertTrue(guidelines.totalReps == 24 || guidelines.totalReps == 25)
        assertEquals(60..90, guidelines.restSeconds)
        assertEquals(0.75, intensity)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle week numbers beyond 4 for DE_Upper`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "DE_Upper",
                movementRole = "primary",
                currentWeekNumber = 5,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.5..0.5, guidelines.intensityRange)
        assertEquals(3..3, guidelines.repsPerSetRange)
        assertEquals(27, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertEquals(0.5, intensity)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle week numbers beyond 4 for accessory`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "Accessory",
                movementRole = "accessory",
                currentWeekNumber = 5,
                exercise = "Push-ups"
            )

        assertNotNull(guidelines)
        assertEquals(0.55..0.65, guidelines.intensityRange)
        assertEquals(3..6, guidelines.repsPerSetRange)
        assertEquals(24, guidelines.totalReps)
        assertEquals(60..90, guidelines.restSeconds)
        assertTrue(intensity in 0.55..0.65)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle different movement roles`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "secondary",
                currentWeekNumber = 1,
                exercise = "Bench Press"
            )

        assertNotNull(guidelines)
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }

    @Test
    fun `getUndulatingPeriodizationGuidelines should handle different exercise names`() {
        val (guidelines, intensity) =
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = "ME_Upper",
                movementRole = "primary",
                currentWeekNumber = 1,
                exercise = "Deadlift"
            )

        assertNotNull(guidelines)
        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
        assertTrue(intensity in 0.8..0.9)
    }
}
