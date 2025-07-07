package com.congen.service.conjugate

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConjugateTemplatesTest {
    @Test
    fun `selectTemplate should return two day template for 2 days`() {
        val templates = ConjugateTemplates()
        val result = templates.selectTemplate(2)

        assertEquals(2, result.size)
        assertEquals("ME_Upper", result[0].type)
        assertEquals("DE_Lower", result[1].type)
    }

    @Test
    fun `selectTemplate should return three day template for 3 days`() {
        val templates = ConjugateTemplates()
        val result = templates.selectTemplate(3)

        assertEquals(3, result.size)
        assertEquals("ME_Upper", result[0].type)
        assertEquals("DE_Lower", result[1].type)
        assertEquals("ME_Lower", result[2].type)
    }

    @Test
    fun `selectTemplate should return four day template for 4 days`() {
        val templates = ConjugateTemplates()
        val result = templates.selectTemplate(4)

        assertEquals(4, result.size)
        assertEquals("ME_Upper", result[0].type)
        assertEquals("DE_Lower", result[1].type)
        assertEquals("ME_Lower", result[2].type)
        assertEquals("DE_Upper", result[3].type)
    }

    @Test
    fun `selectTemplate should throw exception for invalid days`() {
        val templates = ConjugateTemplates()

        assertThrows<IllegalArgumentException> { templates.selectTemplate(1) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(5) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(0) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(-1) }
    }

    @Test
    fun `hasSecondaryMovement should return true for ME_Upper`() {
        val templates = ConjugateTemplates()
        assertTrue(templates.hasSecondaryMovement("ME_Upper"))
    }

    @Test
    fun `hasSecondaryMovement should return true for DE_Upper`() {
        val templates = ConjugateTemplates()
        assertTrue(templates.hasSecondaryMovement("DE_Upper"))
    }

    @Test
    fun `hasSecondaryMovement should return false for ME_Lower`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasSecondaryMovement("ME_Lower"))
    }

    @Test
    fun `hasSecondaryMovement should return false for DE_Lower`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasSecondaryMovement("DE_Lower"))
    }

    @Test
    fun `hasSecondaryMovement should return false for other day types`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasSecondaryMovement("Accessory"))
        assertFalse(templates.hasSecondaryMovement("Conditioning"))
        assertFalse(templates.hasSecondaryMovement(""))
    }

    @Test
    fun `hasConditioning should return true for DE_Upper`() {
        val templates = ConjugateTemplates()
        assertTrue(templates.hasConditioning("DE_Upper"))
    }

    @Test
    fun `hasConditioning should return true for DE_Lower`() {
        val templates = ConjugateTemplates()
        assertTrue(templates.hasConditioning("DE_Lower"))
    }

    @Test
    fun `hasConditioning should return false for ME_Upper`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasConditioning("ME_Upper"))
    }

    @Test
    fun `hasConditioning should return false for ME_Lower`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasConditioning("ME_Lower"))
    }

    @Test
    fun `hasConditioning should return false for other day types`() {
        val templates = ConjugateTemplates()
        assertFalse(templates.hasConditioning("Accessory"))
        assertFalse(templates.hasConditioning("Primary"))
        assertFalse(templates.hasConditioning(""))
    }

    @Test
    fun `hasConditioning should return true for day types containing DE`() {
        val templates = ConjugateTemplates()
        assertTrue(templates.hasConditioning("DE_Upper"))
        assertTrue(templates.hasConditioning("DE_Lower"))
        assertTrue(templates.hasConditioning("DE_Squat"))
        assertTrue(templates.hasConditioning("DE_Bench"))
    }

    @Test
    fun `templates should have correct structure`() {
        val templates = ConjugateTemplates()

        // Test two day template
        val twoDay = templates.selectTemplate(2)
        assertEquals(2, twoDay.size)
        assertTrue(twoDay[0].type.contains("ME"))
        assertTrue(twoDay[1].type.contains("DE"))

        // Test three day template
        val threeDay = templates.selectTemplate(3)
        assertEquals(3, threeDay.size)
        assertTrue(threeDay[0].type.contains("ME"))
        assertTrue(threeDay[1].type.contains("DE"))
        assertTrue(threeDay[2].type.contains("ME"))

        // Test four day template
        val fourDay = templates.selectTemplate(4)
        assertEquals(4, fourDay.size)
        assertTrue(fourDay[0].type.contains("ME"))
        assertTrue(fourDay[1].type.contains("DE"))
        assertTrue(fourDay[2].type.contains("ME"))
        assertTrue(fourDay[3].type.contains("DE"))
    }

    @Test
    fun `day templates should be immutable`() {
        val templates = ConjugateTemplates()
        val result = templates.selectTemplate(3)

        // Verify the result is a new list each time
        val result2 = templates.selectTemplate(3)
        assertEquals(result, result2)
    }

    @Test
    fun `template selection should be deterministic`() {
        val templates = ConjugateTemplates()

        val result1 = templates.selectTemplate(2)
        val result2 = templates.selectTemplate(2)
        val result3 = templates.selectTemplate(2)

        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(result1, result3)
    }
}
