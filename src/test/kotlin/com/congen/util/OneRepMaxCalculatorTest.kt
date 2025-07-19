package com.congen.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * Unit tests for OneRepMaxCalculator.
 *
 * Tests the various 1RM calculation formulas and edge cases.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class OneRepMaxCalculatorTest {
    private val calculator = OneRepMaxCalculator()

    @Test
    fun `should calculate Brzycki formula correctly`() {
        // Test case: 200 lbs × 5 reps
        // Brzycki: 200 × (36 / (37 - 5)) = 200 × (36 / 32) = 200 × 1.125 = 225
        val result =
            calculator.estimateOneRepMax(
                BigDecimal("200"),
                5,
                OneRepMaxCalculator.OneRepMaxFormula.BRZYCKI
            )
        assertEquals(BigDecimal("225.00"), result)
    }

    @Test
    fun `should calculate Epley formula correctly`() {
        // Test case: 200 lbs × 5 reps
        // Epley: 200 × (1 + 5/30) = 200 × (1 + 0.167) = 200 × 1.167 = 233.33
        val result =
            calculator.estimateOneRepMax(
                BigDecimal("200"),
                5,
                OneRepMaxCalculator.OneRepMaxFormula.EPLEY
            )
        assertEquals(BigDecimal("233.33"), result)
    }

    @Test
    fun `should calculate Lombardi formula correctly`() {
        // Test case: 200 lbs × 5 reps
        // Lombardi approximation: 200 × (1 + 5 × 0.1) = 200 × 1.5 = 300
        val result =
            calculator.estimateOneRepMax(
                BigDecimal("200"),
                5,
                OneRepMaxCalculator.OneRepMaxFormula.LOMBARDI
            )
        assertEquals(BigDecimal("300.00"), result)
    }

    @Test
    fun `should calculate O'Conner formula correctly`() {
        // Test case: 200 lbs × 5 reps
        // O'Conner: 200 × (1 + 5/40) = 200 × (1 + 0.125) = 200 × 1.125 = 225.00
        val result =
            calculator.estimateOneRepMax(
                BigDecimal("200"),
                5,
                OneRepMaxCalculator.OneRepMaxFormula.OCONNER
            )
        assertEquals(BigDecimal("225.00"), result)
    }

    @Test
    fun `should select appropriate formula based on rep range`() {
        // 1-3 reps: Brzycki
        val lowReps = calculator.estimateOneRepMax(BigDecimal("200"), 3)
        val brzycki = calculator.estimateOneRepMax(BigDecimal("200"), 3, OneRepMaxCalculator.OneRepMaxFormula.BRZYCKI)
        assertEquals(brzycki, lowReps)

        // 4-10 reps: Epley
        val mediumReps = calculator.estimateOneRepMax(BigDecimal("200"), 8)
        val epley = calculator.estimateOneRepMax(BigDecimal("200"), 8, OneRepMaxCalculator.OneRepMaxFormula.EPLEY)
        assertEquals(epley, mediumReps)

        // 11-15 reps: Lombardi
        val highReps = calculator.estimateOneRepMax(BigDecimal("200"), 12)
        val lombardi = calculator.estimateOneRepMax(BigDecimal("200"), 12, OneRepMaxCalculator.OneRepMaxFormula.LOMBARDI)
        assertEquals(lombardi, highReps)

        // 16+ reps: O'Conner
        val veryHighReps = calculator.estimateOneRepMax(BigDecimal("200"), 20)
        val oconner = calculator.estimateOneRepMax(BigDecimal("200"), 20, OneRepMaxCalculator.OneRepMaxFormula.OCONNER)
        assertEquals(oconner, veryHighReps)
    }

    @Test
    fun `should handle single rep correctly`() {
        // Single rep should return the weight itself (or very close to it)
        val result = calculator.estimateOneRepMax(BigDecimal("200"), 1)
        assertEquals(BigDecimal("200.00"), result)
    }

    @Test
    fun `should handle decimal weights correctly`() {
        val result = calculator.estimateOneRepMax(BigDecimal("185.5"), 5)
        // Should round to 2 decimal places and use Epley formula for 5 reps
        // Epley: 185.5 × (1 + 5/30) = 185.5 × 1.167 = 216.42
        assertEquals(BigDecimal("216.42"), result)
    }

    @Test
    fun `should throw exception for zero weight`() {
        assertThrows<IllegalArgumentException> {
            calculator.estimateOneRepMax(BigDecimal.ZERO, 5)
        }
    }

    @Test
    fun `should throw exception for negative weight`() {
        assertThrows<IllegalArgumentException> {
            calculator.estimateOneRepMax(BigDecimal("-100"), 5)
        }
    }

    @Test
    fun `should throw exception for zero reps`() {
        assertThrows<IllegalArgumentException> {
            calculator.estimateOneRepMax(BigDecimal("200"), 0)
        }
    }

    @Test
    fun `should throw exception for negative reps`() {
        assertThrows<IllegalArgumentException> {
            calculator.estimateOneRepMax(BigDecimal("200"), -5)
        }
    }

    @Test
    fun `should throw exception for too many reps`() {
        assertThrows<IllegalArgumentException> {
            calculator.estimateOneRepMax(BigDecimal("200"), 31)
        }
    }

    @Test
    fun `should handle edge case of 30 reps`() {
        // Should not throw exception for exactly 30 reps
        val result = calculator.estimateOneRepMax(BigDecimal("100"), 30)
        // Should be a reasonable value
        assert(result > BigDecimal("100"))
    }

    @Test
    fun `should provide reasonable estimates for different rep ranges`() {
        val weight = BigDecimal("200")

        // All estimates should be reasonable (greater than the weight used)
        val oneRep = calculator.estimateOneRepMax(weight, 1)
        val fiveReps = calculator.estimateOneRepMax(weight, 5)
        val tenReps = calculator.estimateOneRepMax(weight, 10)
        val fifteenReps = calculator.estimateOneRepMax(weight, 15)

        println("1 rep: $oneRep")
        println("5 reps: $fiveReps")
        println("10 reps: $tenReps")
        println("15 reps: $fifteenReps")

        // All estimates should be greater than or equal to the weight used
        assert(oneRep >= weight) { "1 rep estimate $oneRep should be >= $weight" }
        assert(fiveReps > weight) { "5 reps estimate $fiveReps should be > $weight" }
        assert(tenReps > weight) { "10 reps estimate $tenReps should be > $weight" }
        assert(fifteenReps > weight) { "15 reps estimate $fifteenReps should be > $weight" }

        // Estimates should be reasonable (not more than 3x the weight)
        assert(oneRep < weight.multiply(BigDecimal("3"))) { "1 rep estimate $oneRep should be < ${weight.multiply(BigDecimal("3"))}" }
        assert(fiveReps < weight.multiply(BigDecimal("3"))) { "5 reps estimate $fiveReps should be < ${weight.multiply(BigDecimal("3"))}" }
        assert(tenReps < weight.multiply(BigDecimal("3"))) { "10 reps estimate $tenReps should be < ${weight.multiply(BigDecimal("3"))}" }
        assert(
            fifteenReps < weight.multiply(BigDecimal("3"))
        ) { "15 reps estimate $fifteenReps should be < ${weight.multiply(BigDecimal("3"))}" }
    }
}
