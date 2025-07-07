package com.congen.service.conjugate

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
        val dayTemplate = DayTemplate("ME_Upper")

        assertEquals("ME_Upper", dayTemplate.type)
    }

    @Test
    fun `DayTemplate should support data class functionality`() {
        val template1 = DayTemplate("ME_Upper")
        val template2 = DayTemplate("ME_Upper")
        val template3 = DayTemplate("DE_Lower")

        assertEquals(template1, template2)
        assertNotNull(template1 != template3)
        assertEquals(template1.hashCode(), template2.hashCode())
        assertTrue(template1.toString().contains("DayTemplate"))
        assertTrue(template1.toString().contains("ME_Upper"))
    }

    @Test
    fun `PrilepinGuidelines should be created with all fields`() {
        val guidelines =
            PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            )

        assertEquals(0.8..0.9, guidelines.intensityRange)
        assertEquals(2..4, guidelines.repsPerSetRange)
        assertEquals(15, guidelines.totalReps)
        assertEquals(180..300, guidelines.restSeconds)
    }

    @Test
    fun `PrilepinGuidelines should support data class functionality`() {
        val guidelines1 =
            PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            )
        val guidelines2 =
            PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            )
        val guidelines3 =
            PrilepinGuidelines(
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
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
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
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
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
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )
        val params2 =
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )
        val params3 =
            SetSchemeParams(
                setNumber = 2,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )

        assertEquals(params1, params2)
        assertNotNull(params1 != params3)
        assertEquals(params1.hashCode(), params2.hashCode())
        assertTrue(params1.toString().contains("SetSchemeParams"))
    }

    @Test
    fun `ConjugateConstants should have correct default values`() {
        assertEquals(listOf("hamstrings", "glutes", "upper_back", "core"), ConjugateConstants.DEFAULT_WEAK_MUSCLES)
        assertEquals("50.0", ConjugateConstants.DEFAULT_WEIGHT)
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
    fun `SetSchemeParams should handle AMRAP sets`() {
        val amrapParams =
            SetSchemeParams(
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("80.0"),
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = 0
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
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = true,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("60.0"),
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = 60
            )

        assertFalse(emomParams.isAmrap)
        assertTrue(emomParams.isEmom)
        assertEquals(BigDecimal("60.0"), emomParams.targetWeight)
        assertNull(emomParams.targetRepCount)
        assertEquals(60, emomParams.restSeconds)
    }

    @Test
    fun `SetSchemeParams should handle tempo sets`() {
        val tempoParams =
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "4",
                isometricTempo = "2",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )

        assertTrue(tempoParams.useTempo)
        assertEquals("4", tempoParams.eccentricTempo)
        assertEquals("2", tempoParams.isometricTempo)
        assertEquals("1", tempoParams.concentricTempo)
        assertEquals(5, tempoParams.targetRepCount)
        assertEquals(180, tempoParams.restSeconds)
    }

    @Test
    fun `SetSchemeParams should support data class copy`() {
        val originalParams =
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )

        val updatedParams =
            originalParams.copy(
                setNumber = 2,
                targetWeight = BigDecimal("110.0"),
                targetRepCount = 6
            )

        assertEquals(2, updatedParams.setNumber)
        assertEquals(BigDecimal("110.0"), updatedParams.targetWeight)
        assertEquals(6, updatedParams.targetRepCount)
        assertEquals(1, originalParams.setNumber) // Original unchanged
        assertEquals(BigDecimal("100.0"), originalParams.targetWeight)
        assertEquals(5, originalParams.targetRepCount)
    }

    @Test
    fun `SetSchemeParams should support data class component functions`() {
        val params =
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 180
            )

        val (
            setNumber, isAmrap, isEmom, useTempo, eccentricTempo,
            isometricTempo, concentricTempo, targetWeight, performedWeight,
            targetRepCount, performedRepCount, restSeconds
        ) = params

        assertEquals(1, setNumber)
        assertFalse(isAmrap)
        assertFalse(isEmom)
        assertTrue(useTempo)
        assertEquals("3", eccentricTempo)
        assertEquals("1", isometricTempo)
        assertEquals("1", concentricTempo)
        assertEquals(BigDecimal("100.0"), targetWeight)
        assertNull(performedWeight)
        assertEquals(5, targetRepCount)
        assertNull(performedRepCount)
        assertEquals(180, restSeconds)
    }
} 
