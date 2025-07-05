package com.congen.util

import com.congen.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

class ValidationUtilTest {
    @Test
    fun `validateUserAge should pass for valid ages`() {
        assertDoesNotThrow { ValidationUtil.validateUserAge(1) }
        assertDoesNotThrow { ValidationUtil.validateUserAge(25) }
        assertDoesNotThrow { ValidationUtil.validateUserAge(150) }
    }

    @Test
    fun `validateUserAge should throw for invalid ages`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateUserAge(0) }
        assertEquals("User age must be between 1 and 150, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateUserAge(151) }
        assertEquals("User age must be between 1 and 150, got: 151", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateUserAge(-1) }
        assertEquals("User age must be between 1 and 150, got: -1", exception3.message)
    }

    @Test
    fun `validateUserHeight should pass for valid heights`() {
        assertDoesNotThrow { ValidationUtil.validateUserHeight(BigDecimal("0.01")) }
        assertDoesNotThrow { ValidationUtil.validateUserHeight(BigDecimal("175.5")) }
        assertDoesNotThrow { ValidationUtil.validateUserHeight(BigDecimal("300")) }
    }

    @Test
    fun `validateUserHeight should throw for invalid heights`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateUserHeight(BigDecimal.ZERO) }
        assertEquals("User height must be between 0.01 and 300 cm, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateUserHeight(BigDecimal("300.01")) }
        assertEquals("User height must be between 0.01 and 300 cm, got: 300.01", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateUserHeight(BigDecimal("-1")) }
        assertEquals("User height must be between 0.01 and 300 cm, got: -1", exception3.message)
    }

    @Test
    fun `validateUserWeight should pass for valid weights`() {
        assertDoesNotThrow { ValidationUtil.validateUserWeight(BigDecimal("0.01")) }
        assertDoesNotThrow { ValidationUtil.validateUserWeight(BigDecimal("80.5")) }
        assertDoesNotThrow { ValidationUtil.validateUserWeight(BigDecimal("1000")) }
    }

    @Test
    fun `validateUserWeight should throw for invalid weights`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateUserWeight(BigDecimal.ZERO) }
        assertEquals("User weight must be between 0.01 and 1000 kg, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateUserWeight(BigDecimal("1000.01")) }
        assertEquals("User weight must be between 0.01 and 1000 kg, got: 1000.01", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateUserWeight(BigDecimal("-1")) }
        assertEquals("User weight must be between 0.01 and 1000 kg, got: -1", exception3.message)
    }

    @Test
    fun `validateProgramDaysPerWeek should pass for valid days`() {
        assertDoesNotThrow { ValidationUtil.validateProgramDaysPerWeek(2) }
        assertDoesNotThrow { ValidationUtil.validateProgramDaysPerWeek(3) }
        assertDoesNotThrow { ValidationUtil.validateProgramDaysPerWeek(4) }
    }

    @Test
    fun `validateProgramDaysPerWeek should throw for invalid days`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateProgramDaysPerWeek(1) }
        assertEquals(
            "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 1",
            exception1.message,
        )

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateProgramDaysPerWeek(5) }
        assertEquals(
            "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 5",
            exception2.message,
        )

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateProgramDaysPerWeek(0) }
        assertEquals(
            "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 0",
            exception3.message,
        )

        val exception4 = assertThrows<ValidationException> { ValidationUtil.validateProgramDaysPerWeek(8) }
        assertEquals(
            "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: 8",
            exception4.message,
        )
    }

    @Test
    fun `validateSessionTimeLength should pass for valid times`() {
        assertDoesNotThrow { ValidationUtil.validateSessionTimeLength(15) }
        assertDoesNotThrow { ValidationUtil.validateSessionTimeLength(60) }
        assertDoesNotThrow { ValidationUtil.validateSessionTimeLength(300) }
    }

    @Test
    fun `validateSessionTimeLength should throw for invalid times`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateSessionTimeLength(14) }
        assertEquals("Session time length must be between 15 and 300 minutes, got: 14", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateSessionTimeLength(301) }
        assertEquals("Session time length must be between 15 and 300 minutes, got: 301", exception2.message)
    }

    @Test
    fun `validateDayNumber should pass for valid day numbers`() {
        assertDoesNotThrow { ValidationUtil.validateDayNumber(1) }
        assertDoesNotThrow { ValidationUtil.validateDayNumber(30) }
        assertDoesNotThrow { ValidationUtil.validateDayNumber(365) }
    }

    @Test
    fun `validateDayNumber should throw for invalid day numbers`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateDayNumber(0) }
        assertEquals("Day number must be between 1 and 365, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateDayNumber(366) }
        assertEquals("Day number must be between 1 and 365, got: 366", exception2.message)
    }

    @Test
    fun `validatePosition should pass for valid positions`() {
        assertDoesNotThrow { ValidationUtil.validatePosition(1) }
        assertDoesNotThrow { ValidationUtil.validatePosition(5) }
        assertDoesNotThrow { ValidationUtil.validatePosition(100) }
    }

    @Test
    fun `validatePosition should throw for invalid positions`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validatePosition(0) }
        assertEquals("Position must be greater than 0, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validatePosition(-1) }
        assertEquals("Position must be greater than 0, got: -1", exception2.message)
    }

    @Test
    fun `validateSetNumber should pass for valid set numbers`() {
        assertDoesNotThrow { ValidationUtil.validateSetNumber(1) }
        assertDoesNotThrow { ValidationUtil.validateSetNumber(5) }
        assertDoesNotThrow { ValidationUtil.validateSetNumber(100) }
    }

    @Test
    fun `validateSetNumber should throw for invalid set numbers`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateSetNumber(0) }
        assertEquals("Set number must be greater than 0, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateSetNumber(-1) }
        assertEquals("Set number must be greater than 0, got: -1", exception2.message)
    }

    @Test
    fun `validateTempo should pass for valid tempo values`() {
        assertDoesNotThrow { ValidationUtil.validateTempo(null, "Test") }
        assertDoesNotThrow { ValidationUtil.validateTempo("0", "Test") }
        assertDoesNotThrow { ValidationUtil.validateTempo("5", "Test") }
        assertDoesNotThrow { ValidationUtil.validateTempo("9", "Test") }
    }

    @Test
    fun `validateTempo should throw for invalid tempo values`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateTempo("10", "Test") }
        assertEquals("Test tempo must be a single digit (0-9), got: 10", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateTempo("a", "Test") }
        assertEquals("Test tempo must be a single digit (0-9), got: a", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateTempo("", "Test") }
        assertEquals("Test tempo must be a single digit (0-9), got: ", exception3.message)
    }

    @Test
    fun `validateTargetWeight should pass for valid weights`() {
        assertDoesNotThrow { ValidationUtil.validateTargetWeight(null) }
        assertDoesNotThrow { ValidationUtil.validateTargetWeight(BigDecimal("0.01")) }
        assertDoesNotThrow { ValidationUtil.validateTargetWeight(BigDecimal("100.5")) }
    }

    @Test
    fun `validateTargetWeight should throw for invalid weights`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateTargetWeight(BigDecimal.ZERO) }
        assertEquals("Target weight must be greater than 0, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateTargetWeight(BigDecimal("-1")) }
        assertEquals("Target weight must be greater than 0, got: -1", exception2.message)
    }

    @Test
    fun `validatePerformedWeight should pass for valid weights`() {
        assertDoesNotThrow { ValidationUtil.validatePerformedWeight(null) }
        assertDoesNotThrow { ValidationUtil.validatePerformedWeight(BigDecimal("0.01")) }
        assertDoesNotThrow { ValidationUtil.validatePerformedWeight(BigDecimal("100.5")) }
    }

    @Test
    fun `validatePerformedWeight should throw for invalid weights`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validatePerformedWeight(BigDecimal.ZERO) }
        assertEquals("Performed weight must be greater than 0, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validatePerformedWeight(BigDecimal("-1")) }
        assertEquals("Performed weight must be greater than 0, got: -1", exception2.message)
    }

    @Test
    fun `validateTargetRepCount should pass for valid rep counts`() {
        assertDoesNotThrow { ValidationUtil.validateTargetRepCount(null) }
        assertDoesNotThrow { ValidationUtil.validateTargetRepCount(1) }
        assertDoesNotThrow { ValidationUtil.validateTargetRepCount(10) }
        assertDoesNotThrow { ValidationUtil.validateTargetRepCount(1000) }
    }

    @Test
    fun `validateTargetRepCount should throw for invalid rep counts`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateTargetRepCount(0) }
        assertEquals("Target rep count must be between 1 and 1000, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateTargetRepCount(1001) }
        assertEquals("Target rep count must be between 1 and 1000, got: 1001", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateTargetRepCount(-1) }
        assertEquals("Target rep count must be between 1 and 1000, got: -1", exception3.message)
    }

    @Test
    fun `validatePerformedRepCount should pass for valid rep counts`() {
        assertDoesNotThrow { ValidationUtil.validatePerformedRepCount(null) }
        assertDoesNotThrow { ValidationUtil.validatePerformedRepCount(1) }
        assertDoesNotThrow { ValidationUtil.validatePerformedRepCount(10) }
        assertDoesNotThrow { ValidationUtil.validatePerformedRepCount(1000) }
    }

    @Test
    fun `validatePerformedRepCount should throw for invalid rep counts`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validatePerformedRepCount(0) }
        assertEquals("Performed rep count must be between 1 and 1000, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validatePerformedRepCount(1001) }
        assertEquals("Performed rep count must be between 1 and 1000, got: 1001", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validatePerformedRepCount(-1) }
        assertEquals("Performed rep count must be between 1 and 1000, got: -1", exception3.message)
    }

    @Test
    fun `validateRestSeconds should pass for valid rest times`() {
        assertDoesNotThrow { ValidationUtil.validateRestSeconds(null) }
        assertDoesNotThrow { ValidationUtil.validateRestSeconds(0) }
        assertDoesNotThrow { ValidationUtil.validateRestSeconds(60) }
        assertDoesNotThrow { ValidationUtil.validateRestSeconds(3600) }
    }

    @Test
    fun `validateRestSeconds should throw for invalid rest times`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateRestSeconds(-1) }
        assertEquals("Rest seconds must be between 0 and 3600, got: -1", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateRestSeconds(3601) }
        assertEquals("Rest seconds must be between 0 and 3600, got: 3601", exception2.message)
    }

    @Test
    fun `validateExerciseCategory should pass for valid categories`() {
        assertDoesNotThrow { ValidationUtil.validateExerciseCategory("primary") }
        assertDoesNotThrow { ValidationUtil.validateExerciseCategory("secondary") }
        assertDoesNotThrow { ValidationUtil.validateExerciseCategory("accessory") }
    }

    @Test
    fun `validateExerciseCategory should throw for invalid categories`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateExerciseCategory("invalid") }
        assertEquals("Exercise category must be one of: primary, secondary, accessory, got: invalid", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateExerciseCategory("") }
        assertEquals("Exercise category must be one of: primary, secondary, accessory, got: ", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateExerciseCategory("PRIMARY") }
        assertEquals("Exercise category must be one of: primary, secondary, accessory, got: PRIMARY", exception3.message)
    }

    @Test
    fun `validateOneRepMax should pass for valid one rep max values`() {
        assertDoesNotThrow { ValidationUtil.validateOneRepMax(BigDecimal("0.01")) }
        assertDoesNotThrow { ValidationUtil.validateOneRepMax(BigDecimal("100.5")) }
        assertDoesNotThrow { ValidationUtil.validateOneRepMax(BigDecimal("500.0")) }
        assertDoesNotThrow { ValidationUtil.validateOneRepMax(BigDecimal("1000")) }
    }

    @Test
    fun `validateOneRepMax should throw for invalid one rep max values`() {
        val exception1 = assertThrows<ValidationException> { ValidationUtil.validateOneRepMax(BigDecimal.ZERO) }
        assertEquals("One rep max must be between 0.01 and 1000 kg, got: 0", exception1.message)

        val exception2 = assertThrows<ValidationException> { ValidationUtil.validateOneRepMax(BigDecimal("1000.01")) }
        assertEquals("One rep max must be between 0.01 and 1000 kg, got: 1000.01", exception2.message)

        val exception3 = assertThrows<ValidationException> { ValidationUtil.validateOneRepMax(BigDecimal("-1")) }
        assertEquals("One rep max must be between 0.01 and 1000 kg, got: -1", exception3.message)
    }
}
