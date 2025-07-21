package com.congen.generator

import com.congen.mockSetSchemeParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SessionTimeCalculatorTest {
    private lateinit var calculator: SessionTimeCalculator

    @BeforeEach
    fun setUp() {
        calculator = SessionTimeCalculator()
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for ME_Lower`() {
        val dayType = "ME_Lower"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // ME_Lower: 10 min warmup + 10 min primary + 0 min secondary + 0 min conditioning = 20 min allocated
        // Remaining: 60 - 20 = 40 min
        // Accessories: 40 / 5 = 8
        assertEquals(8, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for ME_Upper`() {
        val dayType = "ME_Upper"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // ME_Upper: 10 min warmup + 10 min primary + 8 min secondary + 0 min conditioning = 28 min allocated
        // Remaining: 60 - 28 = 32 min
        // Accessories: 32 / 5 = 6
        assertEquals(6, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for DE_Lower`() {
        val dayType = "DE_Lower"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // DE_Lower: 10 min warmup + 10 min primary + 0 min secondary + 10 min conditioning = 30 min allocated
        // Remaining: 60 - 30 = 30 min
        // Accessories: 30 / 5 = 6
        assertEquals(6, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for DE_Upper`() {
        val dayType = "DE_Upper"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // DE_Upper: 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning = 38 min allocated
        // Remaining: 60 - 38 = 22 min
        // Accessories: 22 / 5 = 4
        assertEquals(4, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for ME_Upper_DE_Lower`() {
        val dayType = "ME_Upper_DE_Lower"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // ME_Upper_DE_Lower: 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning = 38 min allocated
        // Remaining: 60 - 38 = 22 min
        // Accessories: 22 / 5 = 4
        assertEquals(4, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for ME_Lower_DE_Upper`() {
        val dayType = "ME_Lower_DE_Upper"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // ME_Lower_DE_Upper: 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning = 38 min allocated
        // Remaining: 60 - 38 = 22 min
        // Accessories: 22 / 5 = 4
        assertEquals(4, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return correct number for DE_Full_Body`() {
        val dayType = "DE_Full_Body"
        val sessionTimeMinutes = 60

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // DE_Full_Body: 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning = 38 min allocated
        // Remaining: 60 - 38 = 22 min
        // Accessories: 22 / 5 = 4
        assertEquals(4, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should return 0 for short session`() {
        val dayType = "ME_Upper"
        val sessionTimeMinutes = 20

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        // ME_Upper: 10 min warmup + 10 min primary + 8 min secondary + 0 min conditioning = 28 min allocated
        // Remaining: 20 - 28 = -8 min (negative, so 0 accessories)
        assertEquals(0, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should handle zero session time`() {
        val dayType = "ME_Lower"
        val sessionTimeMinutes = 0

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        assertEquals(0, result)
    }

    @Test
    fun `calculateNumAccessoryExercises should handle negative session time`() {
        val dayType = "ME_Lower"
        val sessionTimeMinutes = -10

        val result = calculator.calculateNumAccessoryExercises(sessionTimeMinutes, dayType)

        assertEquals(0, result)
    }

    @Test
    fun `getNonAccessoryTimeAllocation should return correct time for ME_Lower`() {
        val dayType = "ME_Lower"
        val result = calculator.getNonAccessoryTimeAllocation(dayType)
        assertEquals(20, result) // 10 min warmup + 10 min primary only
    }

    @Test
    fun `getNonAccessoryTimeAllocation should return correct time for ME_Upper`() {
        val dayType = "ME_Upper"
        val result = calculator.getNonAccessoryTimeAllocation(dayType)
        assertEquals(28, result) // 10 min warmup + 10 min primary + 8 min secondary
    }

    @Test
    fun `getNonAccessoryTimeAllocation should return correct time for DE_Lower`() {
        val dayType = "DE_Lower"
        val result = calculator.getNonAccessoryTimeAllocation(dayType)
        assertEquals(30, result) // 10 min warmup + 10 min primary + 10 min conditioning
    }

    @Test
    fun `getNonAccessoryTimeAllocation should return correct time for DE_Upper`() {
        val dayType = "DE_Upper"
        val result = calculator.getNonAccessoryTimeAllocation(dayType)
        assertEquals(38, result) // 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning
    }

    @Test
    fun `getNonAccessoryTimeAllocation should return correct time for combined days`() {
        val meUpperDeLower = calculator.getNonAccessoryTimeAllocation("ME_Upper_DE_Lower")
        val meLowerDeUpper = calculator.getNonAccessoryTimeAllocation("ME_Lower_DE_Upper")
        val deFullBody = calculator.getNonAccessoryTimeAllocation("DE_Full_Body")

        assertEquals(38, meUpperDeLower) // 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning
        assertEquals(38, meLowerDeUpper) // 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning
        assertEquals(38, deFullBody) // 10 min warmup + 10 min primary + 8 min secondary + 10 min conditioning
    }

    @Test
    fun `getRemainingTimeForAccessories should return correct time`() {
        val sessionTimeMinutes = 60
        val meLowerResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "ME_Lower")
        val meUpperResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "ME_Upper")
        val deLowerResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "DE_Lower")
        val deUpperResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "DE_Upper")

        assertEquals(40, meLowerResult) // 60 - 20 = 40
        assertEquals(32, meUpperResult) // 60 - 28 = 32
        assertEquals(30, deLowerResult) // 60 - 30 = 30
        assertEquals(22, deUpperResult) // 60 - 38 = 22
    }

    @Test
    fun `getRemainingTimeForAccessories should return correct time for combined days`() {
        val sessionTimeMinutes = 60
        val meUpperDeLowerResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "ME_Upper_DE_Lower")
        val meLowerDeUpperResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "ME_Lower_DE_Upper")
        val deFullBodyResult = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "DE_Full_Body")

        assertEquals(22, meUpperDeLowerResult) // 60 - 38 = 22
        assertEquals(22, meLowerDeUpperResult) // 60 - 38 = 22
        assertEquals(22, deFullBodyResult) // 60 - 38 = 22
    }

    @Test
    fun `getRemainingTimeForAccessories should return 0 for short session`() {
        val sessionTimeMinutes = 20
        val result = calculator.getRemainingTimeForAccessories(sessionTimeMinutes, "ME_Upper")
        assertEquals(0, result) // 20 - 28 = -8, coerced to 0
    }

    @Test
    fun `calculateExerciseTime should return 0 for empty schemes`() {
        val result = calculator.calculateExerciseTime(emptyList())
        assertEquals(0, result)
    }

    @Test
    fun `calculateExerciseTime should calculate correct time for single set`() {
        val setScheme =
            mockSetSchemeParams(
                targetRepCount = 5,
                restSeconds = 180
            )
        val result = calculator.calculateExerciseTime(listOf(setScheme))

        // 180 rest + (5 reps * 6 seconds) = 180 + 30 = 210 seconds
        assertEquals(210, result)
    }

    @Test
    fun `calculateExerciseTime should calculate correct time for multiple sets`() {
        val setScheme1 =
            mockSetSchemeParams(
                setNumber = 1,
                targetRepCount = 5,
                restSeconds = 180
            )
        val setScheme2 =
            mockSetSchemeParams(
                setNumber = 2,
                targetRepCount = 3,
                restSeconds = 240
            )
        val result = calculator.calculateExerciseTime(listOf(setScheme1, setScheme2))

        // Set 1: 180 + (5 * 6) = 210
        // Set 2: 240 + (3 * 6) = 258
        // Total: 210 + 258 = 468 seconds
        assertEquals(468, result)
    }

    @Test
    fun `calculateExerciseTime should handle null values`() {
        val setScheme =
            mockSetSchemeParams(
                targetRepCount = null,
                restSeconds = null
            )
        val result = calculator.calculateExerciseTime(listOf(setScheme))

        // 0 rest + (0 reps * 6 seconds) = 0 + 0 = 0 seconds
        assertEquals(0, result)
    }
}
