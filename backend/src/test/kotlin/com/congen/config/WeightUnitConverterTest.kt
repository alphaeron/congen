package com.congen.config

import com.congen.exceptions.ValidationException
import com.congen.model.WeightUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for WeightUnitConverter.
 *
 * Tests the conversion of string URL parameters to WeightUnit enum values.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WeightUnitConverterTest {
    private val converter = WeightUnitConverter()

    @Test
    fun `should convert KG to KG`() {
        val result = converter.convert("KG")
        assertEquals(WeightUnit.KG, result)
    }

    @Test
    fun `should convert LBS to LBS`() {
        val result = converter.convert("LBS")
        assertEquals(WeightUnit.LBS, result)
    }

    @Test
    fun `should handle case insensitive conversion`() {
        val resultKg = converter.convert("kg")
        assertEquals(WeightUnit.KG, resultKg)

        val resultLbs = converter.convert("lbs")
        assertEquals(WeightUnit.LBS, resultLbs)
    }

    @Test
    fun `should handle mixed case conversion`() {
        val resultKg = converter.convert("Kg")
        assertEquals(WeightUnit.KG, resultKg)

        val resultLbs = converter.convert("Lbs")
        assertEquals(WeightUnit.LBS, resultLbs)
    }

    @Test
    fun `should throw ValidationException for invalid weight unit`() {
        val exception =
            assertThrows<ValidationException> {
                converter.convert("INVALID")
            }
        assertEquals(
            "Invalid weight unit: INVALID. Valid values are: KG, LBS",
            exception.message
        )
    }

    @Test
    fun `should throw ValidationException for empty string`() {
        val exception =
            assertThrows<ValidationException> {
                converter.convert("")
            }
        assertEquals(
            "Invalid weight unit: . Valid values are: KG, LBS",
            exception.message
        )
    }

    @Test
    fun `should throw ValidationException for partial match`() {
        val exception =
            assertThrows<ValidationException> {
                converter.convert("K")
            }
        assertEquals(
            "Invalid weight unit: K. Valid values are: KG, LBS",
            exception.message
        )
    }

    @Test
    fun `should throw ValidationException for extra characters`() {
        val exception =
            assertThrows<ValidationException> {
                converter.convert("KGS")
            }
        assertEquals(
            "Invalid weight unit: KGS. Valid values are: KG, LBS",
            exception.message
        )
    }
}
