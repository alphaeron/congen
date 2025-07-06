package com.congen.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetSchemeTest {
    @Test
    fun `SetScheme should be created with all required fields`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertEquals(1L, setScheme.id)
        assertEquals(5L, setScheme.programmedExerciseId)
        assertEquals(1, setScheme.setNumber)
        assertTrue(setScheme.wasSetPerformed)
        assertFalse(setScheme.isAmrap)
        assertFalse(setScheme.isEmom)
        assertFalse(setScheme.useTempo)
        assertNull(setScheme.eccentricTempo)
        assertNull(setScheme.isometricTempo)
        assertNull(setScheme.concentricTempo)
        assertNull(setScheme.targetWeight)
        assertNull(setScheme.performedWeight)
        assertNull(setScheme.targetRepCount)
        assertNull(setScheme.performedRepCount)
        assertNull(setScheme.restSeconds)
    }

    @Test
    fun `SetScheme should be created with all fields`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                wasSetPerformed = true,
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

        assertEquals(1L, setScheme.id)
        assertEquals(5L, setScheme.programmedExerciseId)
        assertEquals(1, setScheme.setNumber)
        assertTrue(setScheme.wasSetPerformed)
        assertFalse(setScheme.isAmrap)
        assertFalse(setScheme.isEmom)
        assertTrue(setScheme.useTempo)
        assertEquals("3", setScheme.eccentricTempo)
        assertEquals("1", setScheme.isometricTempo)
        assertEquals("1", setScheme.concentricTempo)
        assertEquals(BigDecimal("100.0"), setScheme.targetWeight)
        assertEquals(BigDecimal("100.0"), setScheme.performedWeight)
        assertEquals(5, setScheme.targetRepCount)
        assertEquals(5, setScheme.performedRepCount)
        assertEquals(180, setScheme.restSeconds)
    }

    @Test
    fun `SetScheme should handle AMRAP sets`() {
        val amrapSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = true,
                targetWeight = BigDecimal("80.0"),
                targetRepCount = 10,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertTrue(amrapSet.isAmrap)
        assertFalse(amrapSet.isEmom)
        assertFalse(amrapSet.useTempo)
        assertEquals(BigDecimal("80.0"), amrapSet.targetWeight)
        assertEquals(10, amrapSet.targetRepCount)
    }

    @Test
    fun `SetScheme should handle EMOM sets`() {
        val emomSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isEmom = true,
                targetWeight = BigDecimal("60.0"),
                targetRepCount = 8,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertFalse(emomSet.isAmrap)
        assertTrue(emomSet.isEmom)
        assertFalse(emomSet.useTempo)
        assertEquals(BigDecimal("60.0"), emomSet.targetWeight)
        assertEquals(8, emomSet.targetRepCount)
    }

    @Test
    fun `SetScheme should handle tempo sets`() {
        val tempoSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                useTempo = true,
                eccentricTempo = "4",
                isometricTempo = "2",
                concentricTempo = "1",
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertFalse(tempoSet.isAmrap)
        assertFalse(tempoSet.isEmom)
        assertTrue(tempoSet.useTempo)
        assertEquals("4", tempoSet.eccentricTempo)
        assertEquals("2", tempoSet.isometricTempo)
        assertEquals("1", tempoSet.concentricTempo)
    }

    @Test
    fun `SetScheme should handle different set numbers`() {
        val set1 =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        val set2 =
            SetScheme(
                id = 2L,
                programmedExerciseId = 5L,
                setNumber = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        val set3 =
            SetScheme(
                id = 3L,
                programmedExerciseId = 5L,
                setNumber = 10,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertEquals(1, set1.setNumber)
        assertEquals(5, set2.setNumber)
        assertEquals(10, set3.setNumber)
    }

    @Test
    fun `SetScheme should handle different weights`() {
        val lightSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("20.0"),
                performedWeight = BigDecimal("20.0"),
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        val heavySet =
            SetScheme(
                id = 2L,
                programmedExerciseId = 5L,
                setNumber = 2,
                targetWeight = BigDecimal("200.0"),
                performedWeight = BigDecimal("195.0"),
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertEquals(BigDecimal("20.0"), lightSet.targetWeight)
        assertEquals(BigDecimal("20.0"), lightSet.performedWeight)
        assertEquals(BigDecimal("200.0"), heavySet.targetWeight)
        assertEquals(BigDecimal("195.0"), heavySet.performedWeight)
    }

    @Test
    fun `SetScheme should handle different rep counts`() {
        val lowRepSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetRepCount = 1,
                performedRepCount = 1,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                restSeconds = null
            )

        val highRepSet =
            SetScheme(
                id = 2L,
                programmedExerciseId = 5L,
                setNumber = 2,
                targetRepCount = 20,
                performedRepCount = 18,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                restSeconds = null
            )

        assertEquals(1, lowRepSet.targetRepCount)
        assertEquals(1, lowRepSet.performedRepCount)
        assertEquals(20, highRepSet.targetRepCount)
        assertEquals(18, highRepSet.performedRepCount)
    }

    @Test
    fun `SetScheme should handle different rest periods`() {
        val shortRest =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                restSeconds = 60,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null
            )

        val longRest =
            SetScheme(
                id = 2L,
                programmedExerciseId = 5L,
                setNumber = 2,
                restSeconds = 300,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null
            )

        assertEquals(60, shortRest.restSeconds)
        assertEquals(300, longRest.restSeconds)
    }

    @Test
    fun `SetScheme should handle unperformed sets`() {
        val unperformedSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                wasSetPerformed = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertFalse(unperformedSet.wasSetPerformed)
    }

    @Test
    fun `SetScheme should support data class copy`() {
        val originalSet =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val updatedSet =
            originalSet.copy(
                setNumber = 2,
                targetWeight = BigDecimal("110.0"),
                targetRepCount = 6
            )

        assertEquals(1L, updatedSet.id)
        assertEquals(5L, updatedSet.programmedExerciseId)
        assertEquals(2, updatedSet.setNumber)
        assertEquals(BigDecimal("110.0"), updatedSet.targetWeight)
        assertEquals(6, updatedSet.targetRepCount)
    }

    @Test
    fun `SetScheme should support data class equality`() {
        val set1 =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val set2 =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val set3 =
            SetScheme(
                id = 2L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertEquals(set1, set2)
        assertNotNull(set1 != set3)
    }

    @Test
    fun `SetScheme should support data class toString`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val toString = setScheme.toString()
        assertNotNull(toString)
        assert(toString.contains("SetScheme"))
        assert(toString.contains("id=1"))
        assert(toString.contains("programmedExerciseId=5"))
        assert(toString.contains("setNumber=1"))
    }

    @Test
    fun `SetScheme should support data class hashCode`() {
        val set1 =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val set2 =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertEquals(set1.hashCode(), set2.hashCode())
    }

    @Test
    fun `SetScheme should support data class component functions`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                performedWeight = null,
                performedRepCount = null,
                restSeconds = null
            )

        val (
            id, programmedExerciseId, setNumber, wasSetPerformed, isAmrap, isEmom, useTempo,
            eccentricTempo, isometricTempo, concentricTempo, targetWeight, performedWeight,
            targetRepCount, performedRepCount, restSeconds
        ) = setScheme

        assertEquals(1L, id)
        assertEquals(5L, programmedExerciseId)
        assertEquals(1, setNumber)
        assertTrue(wasSetPerformed)
        assertFalse(isAmrap)
        assertFalse(isEmom)
        assertFalse(useTempo)
        assertNull(eccentricTempo)
        assertNull(isometricTempo)
        assertNull(concentricTempo)
        assertEquals(BigDecimal("100.0"), targetWeight)
        assertNull(performedWeight)
        assertEquals(5, targetRepCount)
        assertNull(performedRepCount)
        assertNull(restSeconds)
    }

    @Test
    fun `SetScheme should handle null optional fields`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = null,
                performedWeight = null,
                targetRepCount = null,
                performedRepCount = null,
                restSeconds = null
            )

        assertNull(setScheme.eccentricTempo)
        assertNull(setScheme.isometricTempo)
        assertNull(setScheme.concentricTempo)
        assertNull(setScheme.targetWeight)
        assertNull(setScheme.performedWeight)
        assertNull(setScheme.targetRepCount)
        assertNull(setScheme.performedRepCount)
        assertNull(setScheme.restSeconds)
    }
} 
