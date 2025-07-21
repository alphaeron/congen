package com.congen.generator

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConjugateTemplatesTest {
    private lateinit var templates: ConjugateTemplates

    @BeforeEach
    fun setUp() {
        templates = ConjugateTemplates()
    }

    @Test
    fun `selectTemplate should return 2-day template`() {
        val result = templates.selectTemplate(2)
        assertEquals(2, result.size)
        assertEquals("ME_Upper_DE_Lower", result[0].type)
        assertEquals("ME_Lower_DE_Upper", result[1].type)
    }

    @Test
    fun `selectTemplate should return 3-day template`() {
        val result = templates.selectTemplate(3)
        assertEquals(3, result.size)
        assertEquals("ME_Upper_DE_Lower", result[0].type)
        assertEquals("ME_Lower_DE_Upper", result[1].type)
        assertEquals("DE_Full_Body", result[2].type)
    }

    @Test
    fun `selectTemplate should return 4-day template`() {
        val result = templates.selectTemplate(4)
        assertEquals(4, result.size)
        assertEquals("ME_Upper", result[0].type)
        assertEquals("DE_Lower", result[1].type)
        assertEquals("ME_Lower", result[2].type)
        assertEquals("DE_Upper", result[3].type)
    }

    @Test
    fun `selectTemplate should throw exception for invalid days`() {
        assertThrows<IllegalArgumentException> { templates.selectTemplate(1) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(5) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(0) }
        assertThrows<IllegalArgumentException> { templates.selectTemplate(-1) }
    }

    @Test
    fun `hasSecondaryMovement should return true for ME_Upper`() {
        assertTrue(templates.hasSecondaryMovement("ME_Upper"))
    }

    @Test
    fun `hasSecondaryMovement should return true for DE_Upper`() {
        assertTrue(templates.hasSecondaryMovement("DE_Upper"))
    }

    @Test
    fun `hasSecondaryMovement should return false for ME_Lower`() {
        assertFalse(templates.hasSecondaryMovement("ME_Lower"))
    }

    @Test
    fun `hasSecondaryMovement should return false for DE_Lower`() {
        assertFalse(templates.hasSecondaryMovement("DE_Lower"))
    }

    @Test
    fun `hasSecondaryMovement should return false for combined days`() {
        assertFalse(templates.hasSecondaryMovement("ME_Upper_DE_Lower"))
        assertFalse(templates.hasSecondaryMovement("ME_Lower_DE_Upper"))
    }

    @Test
    fun `hasSecondaryMovement should return false for full body DE`() {
        assertFalse(templates.hasSecondaryMovement("DE_Full_Body"))
    }

    @Test
    fun `hasSecondaryMovement should return false for other day types`() {
        assertFalse(templates.hasSecondaryMovement("Accessory"))
        assertFalse(templates.hasSecondaryMovement("Conditioning"))
        assertFalse(templates.hasSecondaryMovement(""))
    }

    @Test
    fun `hasConditioning should return true for DE_Upper`() {
        assertTrue(templates.hasConditioning("DE_Upper"))
    }

    @Test
    fun `hasConditioning should return true for DE_Lower`() {
        assertTrue(templates.hasConditioning("DE_Lower"))
    }

    @Test
    fun `hasConditioning should return true for combined days`() {
        assertTrue(templates.hasConditioning("ME_Upper_DE_Lower"))
        assertTrue(templates.hasConditioning("ME_Lower_DE_Upper"))
    }

    @Test
    fun `hasConditioning should return true for full body DE`() {
        assertTrue(templates.hasConditioning("DE_Full_Body"))
    }

    @Test
    fun `hasConditioning should return false for ME_Upper`() {
        assertFalse(templates.hasConditioning("ME_Upper"))
    }

    @Test
    fun `hasConditioning should return false for ME_Lower`() {
        assertFalse(templates.hasConditioning("ME_Lower"))
    }

    @Test
    fun `hasConditioning should return false for other day types`() {
        assertFalse(templates.hasConditioning("Accessory"))
        assertFalse(templates.hasConditioning("Primary"))
        assertFalse(templates.hasConditioning(""))
    }

    @Test
    fun `hasConditioning should return true for day types containing DE`() {
        assertTrue(templates.hasConditioning("DE_Upper"))
        assertTrue(templates.hasConditioning("DE_Lower"))
        assertTrue(templates.hasConditioning("DE_Squat"))
        assertTrue(templates.hasConditioning("DE_Bench"))
    }

    @Test
    fun `isCombinedMEDay should return true for combined days`() {
        assertTrue(templates.isCombinedMEDay("ME_Upper_DE_Lower"))
        assertTrue(templates.isCombinedMEDay("ME_Lower_DE_Upper"))
    }

    @Test
    fun `isCombinedMEDay should return false for other days`() {
        assertFalse(templates.isCombinedMEDay("ME_Upper"))
        assertFalse(templates.isCombinedMEDay("DE_Lower"))
        assertFalse(templates.isCombinedMEDay("DE_Full_Body"))
        assertFalse(templates.isCombinedMEDay("ME_Lower"))
        assertFalse(templates.isCombinedMEDay("DE_Upper"))
    }

    @Test
    fun `isFullBodyDE should return true for full body DE`() {
        assertTrue(templates.isFullBodyDE("DE_Full_Body"))
    }

    @Test
    fun `isFullBodyDE should return false for other days`() {
        assertFalse(templates.isFullBodyDE("ME_Upper_DE_Lower"))
        assertFalse(templates.isFullBodyDE("ME_Lower_DE_Upper"))
        assertFalse(templates.isFullBodyDE("ME_Upper"))
        assertFalse(templates.isFullBodyDE("DE_Lower"))
        assertFalse(templates.isFullBodyDE("ME_Lower"))
        assertFalse(templates.isFullBodyDE("DE_Upper"))
    }

    @Test
    fun `getPrimaryMovementType should return correct types`() {
        assertEquals("ME_Upper", templates.getPrimaryMovementType("ME_Upper_DE_Lower"))
        assertEquals("ME_Lower", templates.getPrimaryMovementType("ME_Lower_DE_Upper"))
        assertEquals("DE_Full_Body", templates.getPrimaryMovementType("DE_Full_Body"))
        assertEquals("ME_Upper", templates.getPrimaryMovementType("ME_Upper"))
        assertEquals("DE_Lower", templates.getPrimaryMovementType("DE_Lower"))
    }

    @Test
    fun `getSecondaryMovementType should return correct types`() {
        assertEquals("DE_Lower", templates.getSecondaryMovementType("ME_Upper_DE_Lower"))
        assertEquals("DE_Upper", templates.getSecondaryMovementType("ME_Lower_DE_Upper"))
        assertTrue(templates.getSecondaryMovementType("DE_Full_Body") == null)
        assertTrue(templates.getSecondaryMovementType("ME_Upper") == null)
        assertTrue(templates.getSecondaryMovementType("DE_Lower") == null)
    }

    @Test
    fun `templates should have correct structure`() {
        val twoDay = templates.selectTemplate(2)
        assertEquals(2, twoDay.size)
        assertTrue(twoDay[0].type.contains("ME") && twoDay[0].type.contains("DE"))
        assertTrue(twoDay[1].type.contains("ME") && twoDay[1].type.contains("DE"))

        val threeDay = templates.selectTemplate(3)
        assertEquals(3, threeDay.size)
        assertTrue(threeDay[0].type.contains("ME") && threeDay[0].type.contains("DE"))
        assertTrue(threeDay[1].type.contains("ME") && threeDay[1].type.contains("DE"))
        assertTrue(threeDay[2].type.contains("DE") && threeDay[2].type.contains("Full"))

        val fourDay = templates.selectTemplate(4)
        assertEquals(4, fourDay.size)
        assertTrue(fourDay[0].type.contains("ME"))
        assertTrue(fourDay[1].type.contains("DE"))
        assertTrue(fourDay[2].type.contains("ME"))
        assertTrue(fourDay[3].type.contains("DE"))
    }

    @Test
    fun `day templates should be immutable`() {
        val result = templates.selectTemplate(3)
        val result2 = templates.selectTemplate(3)
        assertEquals(result, result2)
    }

    @Test
    fun `template selection should be deterministic`() {
        val result1 = templates.selectTemplate(2)
        val result2 = templates.selectTemplate(2)
        val result3 = templates.selectTemplate(2)

        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(result1, result3)
    }
}
