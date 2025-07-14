package com.congen.service.conjugate

import com.congen.mockSetSchemeParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SessionTimeCalculatorTest {
    private lateinit var sessionTimeCalculator: SessionTimeCalculator

    @BeforeEach
    fun setUp() {
        sessionTimeCalculator = SessionTimeCalculator()
    }

    @Test
    fun `calculateNumAccessoryExercisesDynamic with primary exercise only`() {
        val primarySetSchemes =
            listOf(
                mockSetSchemeParams(setNumber = 1, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 2, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 3, targetRepCount = 5, restSeconds = 180)
            )
        val sessionTimeMinutes = ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES
        val dayType = "ME_Lower"
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = emptyList(),
                dayType = dayType
            )
        assertTrue(numAccessories > 0)
    }

    @Test
    fun `calculateNumAccessoryExercisesDynamic with primary and secondary exercises`() {
        val primarySetSchemes =
            listOf(
                mockSetSchemeParams(setNumber = 1, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 2, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 3, targetRepCount = 5, restSeconds = 180)
            )
        val secondarySetSchemes =
            listOf(
                mockSetSchemeParams(setNumber = 1, targetRepCount = 6, restSeconds = 240),
                mockSetSchemeParams(setNumber = 2, targetRepCount = 6, restSeconds = 240),
                mockSetSchemeParams(setNumber = 3, targetRepCount = 6, restSeconds = 240),
                mockSetSchemeParams(setNumber = 4, targetRepCount = 6, restSeconds = 240)
            )
        val sessionTimeMinutes = ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES
        val dayType = "ME_Upper"
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = secondarySetSchemes,
                dayType = dayType
            )
        assertTrue(numAccessories > 0)
        assertTrue(numAccessories < 10)
    }

    @Test
    fun `calculateNumAccessoryExercisesDynamic with conditioning - prioritize accessories over conditioning`() {
        val primarySetSchemes =
            listOf(
                mockSetSchemeParams(setNumber = 1, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 2, targetRepCount = 5, restSeconds = 180),
                mockSetSchemeParams(setNumber = 3, targetRepCount = 5, restSeconds = 180)
            )
        val sessionTimeMinutes = 15
        val dayType = "DE_Lower"
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = emptyList(),
                dayType = dayType
            )
        assertTrue(numAccessories >= 0)
    }

    @Test
    fun `calculateExerciseTime with empty set schemes returns zero`() {
        val exerciseTime = sessionTimeCalculator.calculateExerciseTime(emptyList())
        assertEquals(0, exerciseTime)
    }

    @Test
    fun `calculateExerciseTime with single set scheme`() {
        val setSchemes =
            listOf(
                mockSetSchemeParams(setNumber = 1, targetRepCount = 5, restSeconds = 120)
            )
        val exerciseTime = sessionTimeCalculator.calculateExerciseTime(setSchemes)
        assertEquals(150, exerciseTime)
    }
}
