package com.congen.generator

import com.congen.mockDayTemplate
import com.congen.mockPrilepinGuidelines
import com.congen.mockSetSchemeParams
import com.congen.model.ExerciseEquipment
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConjugateModelsTest {
    @Test
    fun `DayTemplate should be created with type`() {
        val dayTemplate = mockDayTemplate("ME_Upper")
        assertEquals("ME_Upper", dayTemplate.type)
    }

    @Test
    fun `DayTemplate should support data class functionality`() {
        val template1 = mockDayTemplate("ME_Upper")
        val template2 = mockDayTemplate("ME_Upper")
        val template3 = mockDayTemplate("DE_Lower")

        assertEquals(template1, template2)
        assertNotNull(template1 != template3)
        assertEquals(template1.hashCode(), template2.hashCode())
        assertTrue(template1.toString().contains("DayTemplate"))
        assertTrue(template1.toString().contains("ME_Upper"))
    }

    @Test
    fun `PrilepinGuidelines should be created with all fields`() {
        val guidelines = mockPrilepinGuidelines()

        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
    }

    @Test
    fun `PrilepinGuidelines should support data class functionality`() {
        val guidelines1 = mockPrilepinGuidelines()
        val guidelines2 = mockPrilepinGuidelines()
        val guidelines3 =
            mockPrilepinGuidelines(
                intensityRange = 0.7..0.8,
                repsPerSetRange = 3..6,
                totalReps = 18,
                restSeconds = 90..120
            )

        assertEquals(guidelines1, guidelines2)
        assertNotNull(guidelines1 != guidelines3)
        assertEquals(guidelines1.hashCode(), guidelines2.hashCode())
        assertTrue(guidelines1.toString().contains("PrilepinGuidelines"))
    }

    @Test
    fun `SetSchemeParams should be created with all fields`() {
        val params =
            mockSetSchemeParams(
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                performedWeight = BigDecimal("100.0"),
                performedRepCount = 5,
                band = null
            )

        assertEquals(1, params.setNumber)
        assertFalse(params.isAmrap)
        assertFalse(params.isEmom)
        assertTrue(params.useTempo)
        assertEquals("3", params.eccentricTempo)
        assertEquals("1", params.isometricTempo)
        assertEquals("1", params.concentricTempo)
        assertEquals(BigDecimal("100.0"), params.targetWeight)
        assertEquals(BigDecimal("100.0"), params.performedWeight)
        assertEquals(5, params.targetRepCount)
        assertEquals(5, params.performedRepCount)
        assertEquals(180, params.restSeconds)
    }

    @Test
    fun `SetSchemeParams should be created with minimal fields`() {
        val params =
            mockSetSchemeParams(
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null,
                band = null
            )

        assertEquals(1, params.setNumber)
        assertFalse(params.isAmrap)
        assertFalse(params.isEmom)
        assertFalse(params.useTempo)
        assertNull(params.eccentricTempo)
        assertNull(params.isometricTempo)
        assertNull(params.concentricTempo)
        assertNull(params.targetWeight)
        assertNull(params.performedWeight)
        assertNull(params.targetRepCount)
        assertNull(params.performedRepCount)
        assertNull(params.restSeconds)
    }

    @Test
    fun `SetSchemeParams should support data class functionality`() {
        val params1 =
            mockSetSchemeParams(
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                band = null
            )
        val params2 =
            mockSetSchemeParams(
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                band = null
            )
        val params3 =
            mockSetSchemeParams(
                setNumber = 2,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                band = null
            )

        assertEquals(params1, params2)
        assertNotNull(params1 != params3)
        assertEquals(params1.hashCode(), params2.hashCode())
        assertTrue(params1.toString().contains("SetSchemeParams"))
    }

    @Test
    fun `ConjugateConstants should have correct default values`() {
        assertEquals(listOf("hamstrings", "glutes", "upper back", "lats"), ConjugateConstants.DEFAULT_WEAK_MUSCLES)
        assertEquals(60, ConjugateConstants.DEFAULT_SESSION_TIME_MINUTES)
    }

    @Test
    fun `ConjugateConstants TimeAllocation should have correct values`() {
        assertEquals(10, ConjugateConstants.TimeAllocation.PRIMARY_MOVEMENT_TIME_IN_MINUTES)
        assertEquals(8, ConjugateConstants.TimeAllocation.SECONDARY_MOVEMENT_TIME_IN_MINUTES)
        assertEquals(10, ConjugateConstants.TimeAllocation.CONDITIONING_TIME_IN_MINUTES)
        assertEquals(5, ConjugateConstants.TimeAllocation.SINGLE_ACCESSORY_EXERCISE_TIME_IN_MINUTES)
    }

    @Test
    fun `exerciseUsesConditioningEquipment should return true for conditioning equipment`() {
        val equipment = listOf(ExerciseEquipment("Sled Push", "sled"))
        assertTrue(ConjugateConstants.exerciseUsesConditioningEquipment(equipment))
    }

    @Test
    fun `exerciseUsesConditioningEquipment should return false for non-conditioning equipment`() {
        val equipment = listOf(ExerciseEquipment("Bench Press", "power bar"))
        assertFalse(ConjugateConstants.exerciseUsesConditioningEquipment(equipment))
    }

    @Test
    fun `exerciseUsesConditioningEquipment should return false when equipment list is empty`() {
        assertFalse(ConjugateConstants.exerciseUsesConditioningEquipment(emptyList()))
    }

    @Test
    fun `SetSchemeParams should handle AMRAP sets`() {
        val amrapParams =
            mockSetSchemeParams(
                isAmrap = true,
                targetWeight = BigDecimal("80.0"),
                targetRepCount = null,
                restSeconds = 0,
                band = null
            )

        assertTrue(amrapParams.isAmrap)
        assertFalse(amrapParams.isEmom)
        assertEquals(BigDecimal("80.0"), amrapParams.targetWeight)
        assertNull(amrapParams.targetRepCount)
        assertEquals(0, amrapParams.restSeconds)
    }

    @Test
    fun `SetSchemeParams should handle EMOM sets`() {
        val emomParams =
            mockSetSchemeParams(
                isEmom = true,
                targetWeight = BigDecimal("60.0"),
                targetRepCount = null,
                restSeconds = 0,
                band = null
            )

        assertFalse(emomParams.isAmrap)
        assertTrue(emomParams.isEmom)
        assertEquals(BigDecimal("60.0"), emomParams.targetWeight)
        assertNull(emomParams.targetRepCount)
        assertEquals(0, emomParams.restSeconds)
    }

    @Test
    fun `SetSchemeParams should handle tempo training`() {
        val tempoParams =
            mockSetSchemeParams(
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                band = null
            )

        assertTrue(tempoParams.useTempo)
        assertEquals("3", tempoParams.eccentricTempo)
        assertEquals("1", tempoParams.isometricTempo)
        assertEquals("1", tempoParams.concentricTempo)
    }
}
