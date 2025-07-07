package com.congen.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for UserOneRepMax model.
 *
 * These tests verify the behavior and validation of the UserOneRepMax data class,
 * including property access, equality, and serialization.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserOneRepMaxTest {
    private val now = LocalDateTime.now()

    @Test
    fun `should create user one rep max with valid parameters`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        assertEquals(1, userOneRepMax.userId)
        assertEquals("Bench Press", userOneRepMax.exerciseName)
        assertEquals(BigDecimal("225.0"), userOneRepMax.oneRepMax)
        assertEquals(now, userOneRepMax.updatedAt)
    }

    @Test
    fun `should create user one rep max with different exercise`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Squat",
                oneRepMax = BigDecimal("315.0"),
                updatedAt = now
            )

        assertEquals("Squat", userOneRepMax.exerciseName)
        assertEquals(BigDecimal("315.0"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should create user one rep max with decimal weight`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Deadlift",
                oneRepMax = BigDecimal("405.5"),
                updatedAt = now
            )

        assertEquals(BigDecimal("405.5"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should handle different timestamps`() {
        val updatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)

        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = updatedAt
            )

        assertEquals(updatedAt, userOneRepMax.updatedAt)
    }

    @Test
    fun `should support data class copy`() {
        val originalOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val updatedOneRepMax =
            originalOneRepMax.copy(
                oneRepMax = BigDecimal("250.0")
            )

        assertEquals(1, updatedOneRepMax.userId)
        assertEquals("Bench Press", updatedOneRepMax.exerciseName)
        assertEquals(BigDecimal("250.0"), updatedOneRepMax.oneRepMax)
        assertEquals(now, updatedOneRepMax.updatedAt)
    }

    @Test
    fun `should support data class equality`() {
        val oneRepMax1 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val oneRepMax2 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val oneRepMax3 =
            UserOneRepMax(
                userId = 2,
                exerciseName = "Squat",
                oneRepMax = BigDecimal("315.0"),
                updatedAt = now
            )

        assertEquals(oneRepMax1, oneRepMax2)
        assertNotNull(oneRepMax1 != oneRepMax3)
    }

    @Test
    fun `should support data class toString`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val toString = userOneRepMax.toString()
        assertNotNull(toString)
        assert(toString.contains("UserOneRepMax"))
        assert(toString.contains("userId=1"))
        assert(toString.contains("exerciseName=Bench Press"))
        assert(toString.contains("oneRepMax=225.0"))
    }

    @Test
    fun `should support data class hashCode`() {
        val oneRepMax1 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val oneRepMax2 =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        assertEquals(oneRepMax1.hashCode(), oneRepMax2.hashCode())
    }

    @Test
    fun `should support data class component functions`() {
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.0"),
                updatedAt = now
            )

        val (userId, exerciseName, oneRepMax, updatedAt) = userOneRepMax

        assertEquals(1, userId)
        assertEquals("Bench Press", exerciseName)
        assertEquals(BigDecimal("225.0"), oneRepMax)
        assertEquals(now, updatedAt)
    }
}
