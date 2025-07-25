package com.congen.config

import com.congen.exceptions.ValidationException
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for WorkoutStageTypeEnumConverter.
 *
 * These tests verify that the converter properly converts string values to
 * WorkoutStageTypeEnum values using case-insensitive matching on display names.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WorkoutStageTypeEnumConverterTest {
    private lateinit var converter: WorkoutStageTypeEnumConverter

    @BeforeEach
    fun setUp() {
        converter = WorkoutStageTypeEnumConverter()
    }

    @Test
    fun `should convert valid display names to enum values`() {
        assertEquals(WorkoutStageTypeEnum.WARMUP, converter.convert("Warmup"))
        assertEquals(WorkoutStageTypeEnum.PRIMARY, converter.convert("Primary"))
        assertEquals(WorkoutStageTypeEnum.SECONDARY, converter.convert("Secondary"))
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, converter.convert("Accessory"))
        assertEquals(WorkoutStageTypeEnum.CONDITIONING, converter.convert("Conditioning"))
        assertEquals(WorkoutStageTypeEnum.MOBILITY, converter.convert("Mobility"))
        assertEquals(WorkoutStageTypeEnum.COOLDOWN, converter.convert("Cooldown"))
    }

    @Test
    fun `should convert case-insensitive display names to enum values`() {
        assertEquals(WorkoutStageTypeEnum.WARMUP, converter.convert("warmup"))
        assertEquals(WorkoutStageTypeEnum.PRIMARY, converter.convert("PRIMARY"))
        assertEquals(WorkoutStageTypeEnum.SECONDARY, converter.convert("secondary"))
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, converter.convert("ACCESSORY"))
        assertEquals(WorkoutStageTypeEnum.CONDITIONING, converter.convert("conditioning"))
        assertEquals(WorkoutStageTypeEnum.MOBILITY, converter.convert("MOBILITY"))
        assertEquals(WorkoutStageTypeEnum.COOLDOWN, converter.convert("cooldown"))
    }

    @Test
    fun `should convert mixed case display names to enum values`() {
        assertEquals(WorkoutStageTypeEnum.WARMUP, converter.convert("WarmUp"))
        assertEquals(WorkoutStageTypeEnum.PRIMARY, converter.convert("Primary"))
        assertEquals(WorkoutStageTypeEnum.SECONDARY, converter.convert("Secondary"))
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, converter.convert("Accessory"))
        assertEquals(WorkoutStageTypeEnum.CONDITIONING, converter.convert("Conditioning"))
        assertEquals(WorkoutStageTypeEnum.MOBILITY, converter.convert("Mobility"))
        assertEquals(WorkoutStageTypeEnum.COOLDOWN, converter.convert("CoolDown"))
    }

    @Test
    fun `should throw ValidationException for invalid display names`() {
        val invalidNames = listOf("Invalid", "Test", "Unknown", "Random", "", "   ")

        invalidNames.forEach { invalidName ->
            val exception =
                assertThrows(ValidationException::class.java) {
                    converter.convert(invalidName)
                }
            assertEquals(
                "Invalid workout stage type: $invalidName. Valid values are: Warmup, Primary, Secondary, " +
                    "Accessory, Conditioning, Mobility, Cooldown",
                exception.message
            )
        }
    }

    @Test
    fun `should convert enum constant names to enum values`() {
        assertEquals(
            WorkoutStageTypeEnum.WARMUP,
            converter.convert("WARMUP"),
            "Failed to convert valid enum string to WorkoutStageTypeEnum"
        )
        assertEquals(WorkoutStageTypeEnum.PRIMARY, converter.convert("PRIMARY"))
        assertEquals(WorkoutStageTypeEnum.SECONDARY, converter.convert("SECONDARY"))
        assertEquals(WorkoutStageTypeEnum.ACCESSORY, converter.convert("ACCESSORY"))
        assertEquals(WorkoutStageTypeEnum.CONDITIONING, converter.convert("CONDITIONING"))
        assertEquals(WorkoutStageTypeEnum.MOBILITY, converter.convert("MOBILITY"))
        assertEquals(WorkoutStageTypeEnum.COOLDOWN, converter.convert("COOLDOWN"))
    }
}
