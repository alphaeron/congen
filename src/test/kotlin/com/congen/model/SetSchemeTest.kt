package com.congen.model

import com.congen.mockSetScheme
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
class SetSchemeTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    private val now = Instant.now()

    @Test
    fun `test set scheme creation with all fields`() {
        val setScheme =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, setScheme.id)
        assertEquals(123L, setScheme.programmedExerciseId)
        assertEquals(1, setScheme.setNumber)
        assertTrue(setScheme.isAmrap)
        assertFalse(setScheme.isEmom)
        assertTrue(setScheme.useTempo)
        assertEquals("3", setScheme.eccentricTempo)
        assertEquals("1", setScheme.isometricTempo)
        assertEquals("1", setScheme.concentricTempo)
        assertEquals(BigDecimal("225.5"), setScheme.targetWeight)
        assertEquals(BigDecimal("225.0"), setScheme.performedWeight)
        assertEquals(5, setScheme.targetRepCount)
        assertEquals(5, setScheme.performedRepCount)
        assertEquals(180, setScheme.restSeconds)
        assertEquals(now, setScheme.createdAt)
        assertEquals(now, setScheme.updatedAt)
    }

    @Test
    fun `test set scheme creation with minimal fields`() {
        val setScheme =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
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
                restSeconds = null,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(1L, setScheme.id)
        assertEquals(123L, setScheme.programmedExerciseId)
        assertEquals(1, setScheme.setNumber)
        assertFalse(setScheme.isAmrap)
        assertFalse(setScheme.isEmom)
        assertFalse(setScheme.useTempo)
        assertEquals(now, setScheme.createdAt)
        assertEquals(now, setScheme.updatedAt)
    }

    @Test
    fun `test set scheme JSON serialization`() {
        val setScheme =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        val json = objectMapper.writeValueAsString(setScheme)
        val deserialized = objectMapper.readValue(json, SetScheme::class.java)

        assertEquals(setScheme, deserialized)
    }

    @Test
    fun `test set scheme equality`() {
        val setScheme1 =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        val setScheme2 =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        val setScheme3 =
            mockSetScheme(
                id = 2L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        assertEquals(setScheme1, setScheme2)
        assertFalse(setScheme1 == setScheme3)
    }

    @Test
    fun `test set scheme copy`() {
        val original =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 123L,
                setNumber = 1,
                isAmrap = true,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("225.5"),
                performedWeight = BigDecimal("225.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        val copy =
            original.copy(
                id = 2L,
                setNumber = 2,
                targetWeight = BigDecimal("250.0")
            )

        assertEquals(2L, copy.id)
        assertEquals(123L, copy.programmedExerciseId)
        assertEquals(2, copy.setNumber)
        assertTrue(copy.isAmrap)
        assertFalse(copy.isEmom)
        assertTrue(copy.useTempo)
        assertEquals("3", copy.eccentricTempo)
        assertEquals("1", copy.isometricTempo)
        assertEquals("1", copy.concentricTempo)
        assertEquals(BigDecimal("250.0"), copy.targetWeight)
        assertEquals(BigDecimal("225.0"), copy.performedWeight)
        assertEquals(5, copy.targetRepCount)
        assertEquals(5, copy.performedRepCount)
        assertEquals(180, copy.restSeconds)
        assertEquals(now, copy.createdAt)
        assertEquals(now, copy.updatedAt)
    }
}
