package com.congen.config

import com.congen.model.MovementType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for MovementTypeConverter.
 *
 * Tests the conversion of string URL parameters to MovementType enum values.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class MovementTypeConverterTest {
    private val converter = MovementTypeConverter()

    @Test
    fun `should convert horizontal_push to HORIZONTAL_PUSH`() {
        val result = converter.convert("horizontal_push")
        assertEquals(MovementType.HORIZONTAL_PUSH, result)
    }

    @Test
    fun `should convert vertical_push to VERTICAL_PUSH`() {
        val result = converter.convert("vertical_push")
        assertEquals(MovementType.VERTICAL_PUSH, result)
    }

    @Test
    fun `should convert horizontal_pull to HORIZONTAL_PULL`() {
        val result = converter.convert("horizontal_pull")
        assertEquals(MovementType.HORIZONTAL_PULL, result)
    }

    @Test
    fun `should convert vertical_pull to VERTICAL_PULL`() {
        val result = converter.convert("vertical_pull")
        assertEquals(MovementType.VERTICAL_PULL, result)
    }

    @Test
    fun `should convert squat to SQUAT`() {
        val result = converter.convert("squat")
        assertEquals(MovementType.SQUAT, result)
    }

    @Test
    fun `should convert hinge to HINGE`() {
        val result = converter.convert("hinge")
        assertEquals(MovementType.HINGE, result)
    }

    @Test
    fun `should convert lunge to LUNGE`() {
        val result = converter.convert("lunge")
        assertEquals(MovementType.LUNGE, result)
    }

    @Test
    fun `should convert core to CORE`() {
        val result = converter.convert("core")
        assertEquals(MovementType.CORE, result)
    }

    @Test
    fun `should convert plyometric to PLYOMETRIC`() {
        val result = converter.convert("plyometric")
        assertEquals(MovementType.PLYOMETRIC, result)
    }

    @Test
    fun `should convert carry to CARRY`() {
        val result = converter.convert("carry")
        assertEquals(MovementType.CARRY, result)
    }

    @Test
    fun `should convert isolation to ISOLATION`() {
        val result = converter.convert("isolation")
        assertEquals(MovementType.ISOLATION, result)
    }

    @Test
    fun `should handle case insensitive conversion`() {
        val result = converter.convert("HORIZONTAL_PUSH")
        assertEquals(MovementType.HORIZONTAL_PUSH, result)
    }

    @Test
    fun `should throw exception for invalid movement type`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                converter.convert("invalid_movement")
            }
        assertEquals(
            "Invalid movement type: invalid_movement. Valid values are: horizontal_push, vertical_push, " +
                "horizontal_pull, vertical_pull, squat, hinge, lunge, core, plyometric, carry, isolation",
            exception.message
        )
    }

    @Test
    fun `should throw exception for null value`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                converter.convert("")
            }
        assertEquals(
            "Invalid movement type: . Valid values are: horizontal_push, vertical_push, " +
                "horizontal_pull, vertical_pull, squat, hinge, lunge, core, plyometric, carry, isolation",
            exception.message
        )
    }
}
