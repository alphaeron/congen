package com.congen.util

import com.congen.model.WeightUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal

/**
 * Unit tests for UnitConverter.
 *
 * Tests the conversion between different weight units (kg and lbs)
 * to ensure accurate calculations and proper rounding.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UnitConverterTest {
    private val unitConverter = UnitConverter()

    @Test
    fun `should convert kg to kg without change`() {
        val weight = BigDecimal("100.0")
        val result = unitConverter.toKg(weight, WeightUnit.KG)
        assertEquals(weight, result)
    }

    @Test
    fun `should convert lbs to kg correctly`() {
        val weightInLbs = BigDecimal("220.0")
        val expectedKg = BigDecimal("99.79") // 220 lbs * 0.453592 = 99.79 kg
        val result = unitConverter.toKg(weightInLbs, WeightUnit.LBS)
        assertEquals(expectedKg, result)
    }

    @Test
    fun `should convert kg to lbs correctly`() {
        val weightInKg = BigDecimal("100.0")
        val expectedLbs = BigDecimal("220.46") // 100 kg * 2.20462 = 220.46 lbs
        val result = unitConverter.fromKg(weightInKg, WeightUnit.LBS)
        assertEquals(expectedLbs, result)
    }

    @Test
    fun `should convert lbs to kg without change`() {
        val weightInKg = BigDecimal("100.0")
        val result = unitConverter.fromKg(weightInKg, WeightUnit.KG)
        assertEquals(weightInKg, result)
    }

    @Test
    fun `should convert between units correctly`() {
        val weightInLbs = BigDecimal("150.0")
        val result = unitConverter.convert(weightInLbs, WeightUnit.LBS, WeightUnit.KG)
        val expectedKg = BigDecimal("68.04") // 150 lbs * 0.453592 = 68.04 kg
        assertEquals(expectedKg, result)
    }

    @Test
    fun `should convert between units correctly reverse`() {
        val weightInKg = BigDecimal("75.0")
        val result = unitConverter.convert(weightInKg, WeightUnit.KG, WeightUnit.LBS)
        val expectedLbs = BigDecimal("165.35") // 75 kg * 2.20462 = 165.35 lbs
        assertEquals(expectedLbs, result)
    }

    @Test
    fun `should return same weight when converting to same unit`() {
        val weight = BigDecimal("100.0")
        val result = unitConverter.convert(weight, WeightUnit.KG, WeightUnit.KG)
        assertEquals(weight, result)
    }

    @Test
    fun `should handle decimal precision correctly`() {
        val weightInLbs = BigDecimal("135.5")
        val result = unitConverter.toKg(weightInLbs, WeightUnit.LBS)
        val expectedKg = BigDecimal("61.46") // 135.5 lbs * 0.453592 = 61.46 kg
        assertEquals(expectedKg, result)
    }

    @Test
    fun `should round to 2 decimal places`() {
        val weightInLbs = BigDecimal("100.0")
        val result = unitConverter.toKg(weightInLbs, WeightUnit.LBS)
        // 100 lbs * 0.453592 = 45.3592 kg, should round to 45.36
        val expectedKg = BigDecimal("45.36")
        assertEquals(expectedKg, result)
    }

    @Test
    fun `should handle zero weight`() {
        val weight = BigDecimal.ZERO
        val result = unitConverter.toKg(weight, WeightUnit.LBS)
        assertEquals(BigDecimal("0.00"), result)
    }

    @Test
    fun `should handle very small weights`() {
        val weightInLbs = BigDecimal("0.1")
        val result = unitConverter.toKg(weightInLbs, WeightUnit.LBS)
        val expectedKg = BigDecimal("0.05") // 0.1 lbs * 0.453592 = 0.0453592, rounds to 0.05
        assertEquals(expectedKg, result)
    }

    @Test
    fun `should handle very large weights`() {
        val weightInKg = BigDecimal("1000.0")
        val result = unitConverter.fromKg(weightInKg, WeightUnit.LBS)
        val expectedLbs = BigDecimal("2204.62") // 1000 kg * 2.20462 = 2204.62 lbs
        assertEquals(expectedLbs, result)
    }

    companion object {
        @JvmStatic
        fun unitConversionTestCases() =
            listOf(
                // 100 kg = 220.46 lbs
                arrayOf(BigDecimal("100.0"), WeightUnit.KG, BigDecimal("220.46")),
                // 50 kg = 110.23 lbs
                arrayOf(BigDecimal("50.0"), WeightUnit.KG, BigDecimal("110.23")),
                // 0 kg = 0 lbs
                arrayOf(BigDecimal("0.0"), WeightUnit.KG, BigDecimal("0.00")),
                // 225 lbs = 102.06 kg
                arrayOf(BigDecimal("225.0"), WeightUnit.LBS, BigDecimal("102.06")),
                // 100 lbs = 45.36 kg
                arrayOf(BigDecimal("100.0"), WeightUnit.LBS, BigDecimal("45.36")),
                // 0 lbs = 0 kg
                arrayOf(BigDecimal("0.0"), WeightUnit.LBS, BigDecimal("0.00"))
            )
    }

    @ParameterizedTest
    @MethodSource("unitConversionTestCases")
    fun `should handle all unit conversion test cases`(
        inputWeight: BigDecimal,
        inputUnit: WeightUnit,
        expectedOutput: BigDecimal
    ) {
        val result =
            when (inputUnit) {
                WeightUnit.KG -> unitConverter.fromKg(inputWeight, WeightUnit.LBS)
                WeightUnit.LBS -> unitConverter.toKg(inputWeight, WeightUnit.LBS)
            }
        assertEquals(expectedOutput, result, "Failed for input: $inputWeight $inputUnit")
    }
}
