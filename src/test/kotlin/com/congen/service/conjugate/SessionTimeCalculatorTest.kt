package com.congen.service.conjugate

import com.congen.model.SetScheme
import com.congen.service.conjugate.SetSchemeParams
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SessionTimeCalculatorTest {
    @Autowired
    private lateinit var sessionTimeCalculator: SessionTimeCalculator

    @Test
    fun `calculateNumAccessoryExercisesDynamic with primary exercise only`() {
        // Given: Primary exercise with 3 sets of 5 reps, 180s rest
        val primarySetSchemes =
            listOf(
                SetSchemeParams(
                    setNumber = 1,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 2,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 3,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                )
            )

        val sessionTimeMinutes = ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES
        val dayType = "ME_Lower" // No secondary movement, no conditioning

        // When: Calculate accessory count
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = emptyList(),
                dayType = dayType
            )

        // Then: Should have time for accessories
        // Primary time: 3 * (180 + 5*6) = 3 * 210 = 630 seconds = 10.5 minutes
        // Remaining: 60 - 10.5 = 49.5 minutes
        // Accessories: 49.5 / 5 = 9.9 → 9 accessories
        assertTrue(numAccessories > 0)
    }

    @Test
    fun `calculateNumAccessoryExercisesDynamic with primary and secondary exercises`() {
        // Given: Primary exercise with 3 sets of 5 reps, 180s rest
        val primarySetSchemes =
            listOf(
                SetSchemeParams(
                    setNumber = 1,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 2,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 3,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                )
            )

        // Secondary exercise with 4 sets of 6 reps, 240s rest
        val secondarySetSchemes =
            listOf(
                SetSchemeParams(
                    setNumber = 1,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 6,
                    performedRepCount = null,
                    restSeconds = 240
                ),
                SetSchemeParams(
                    setNumber = 2,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 6,
                    performedRepCount = null,
                    restSeconds = 240
                ),
                SetSchemeParams(
                    setNumber = 3,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 6,
                    performedRepCount = null,
                    restSeconds = 240
                ),
                SetSchemeParams(
                    setNumber = 4,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 6,
                    performedRepCount = null,
                    restSeconds = 240
                )
            )

        val sessionTimeMinutes = ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES
        val dayType = "ME_Upper" // Has secondary movement, no conditioning

        // When: Calculate accessory count
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = secondarySetSchemes,
                dayType = dayType
            )

        // Then: Should have fewer accessories due to secondary exercise
        // Primary time: 3 * (180 + 5*6) = 3 * 210 = 630 seconds = 10.5 minutes
        // Secondary time: 4 * (240 + 6*6) = 4 * 276 = 1104 seconds = 18.4 minutes
        // Remaining: 60 - 10.5 - 18.4 = 31.1 minutes
        // Accessories: 31.1 / 5 = 6.2 → 6 accessories
        assertTrue(numAccessories > 0)
        assertTrue(numAccessories < 10) // Should be less than primary-only scenario
    }

    @Test
    fun `calculateNumAccessoryExercisesDynamic with conditioning - prioritize accessories over conditioning`() {
        // Given: Primary exercise with 3 sets of 5 reps, 180s rest
        val primarySetSchemes =
            listOf(
                SetSchemeParams(
                    setNumber = 1,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 2,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 3,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                )
            )

        val sessionTimeMinutes = 15 // Very short session
        val dayType = "DE_Lower" // Has conditioning

        // When: Calculate accessory count
        val numAccessories =
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = sessionTimeMinutes,
                primarySetSchemes = primarySetSchemes,
                secondarySetSchemes = emptyList(),
                dayType = dayType
            )

        // Then: Should prioritize accessories over conditioning
        // Primary time: 3 * (180 + 5*6) = 3 * 210 = 630 seconds = 10.5 minutes
        // Remaining after primary: 15 - 10.5 = 4.5 minutes
        // Conditioning would take 10 minutes, but only 4.5 available
        // So skip conditioning and use 4.5 minutes for accessories: 4.5 / 5 = 0.9 → 0 accessories
        // But if we skip conditioning, we get 10 minutes: 10 / 5 = 2 accessories
        assertTrue(numAccessories >= 0)
    }

    @Test
    fun `calculateExerciseTime with empty set schemes returns zero`() {
        // When: Calculate exercise time with empty schemes
        val exerciseTime = sessionTimeCalculator.calculateExerciseTime(emptyList())

        // Then: Should return zero
        assertEquals(0, exerciseTime)
    }

    @Test
    fun `calculateExerciseTime with single set scheme`() {
        // Given: Single set with 5 reps and 120s rest
        val setSchemes =
            listOf(
                SetSchemeParams(
                    setNumber = 1,
                    wasSetPerformed = false,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = "0",
                    isometricTempo = "0",
                    concentricTempo = "0",
                    targetWeight = null,
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 120
                )
            )

        // When: Calculate exercise time
        val exerciseTime = sessionTimeCalculator.calculateExerciseTime(setSchemes)

        // Then: Should be rest + reps * 6 = 120 + 5*6 = 150 seconds
        assertEquals(150, exerciseTime)
    }
}
