package com.congen.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals

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
    @Test
    fun `should create UserOneRepMax with all properties`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("100.0")
        val lastUpdated = LocalDateTime.now()

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
                lastUpdated = lastUpdated,
            )

        // Then
        assertEquals(userId, userOneRepMax.userId)
        assertEquals(exerciseName, userOneRepMax.exerciseName)
        assertEquals(oneRepMax, userOneRepMax.oneRepMax)
        assertEquals(lastUpdated, userOneRepMax.lastUpdated)
    }

    @Test
    fun `should create UserOneRepMax with null lastUpdated`() {
        // Given
        val userId = 1
        val exerciseName = "Squat"
        val oneRepMax = BigDecimal("150.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(userId, userOneRepMax.userId)
        assertEquals(exerciseName, userOneRepMax.exerciseName)
        assertEquals(oneRepMax, userOneRepMax.oneRepMax)
        assertEquals(null, userOneRepMax.lastUpdated)
    }

    @Test
    fun `should handle decimal one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "Deadlift"
        val oneRepMax = BigDecimal("225.5")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(BigDecimal("225.5"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should handle zero one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "Push-up"
        val oneRepMax = BigDecimal("0.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(BigDecimal("0.0"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should handle large one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "Heavy Deadlift"
        val oneRepMax = BigDecimal("500.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(BigDecimal("500.0"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should handle special characters in exercise name`() {
        // Given
        val userId = 1
        val exerciseName = "Barbell Bench Press (Incline)"
        val oneRepMax = BigDecimal("120.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(exerciseName, userOneRepMax.exerciseName)
    }

    @Test
    fun `should handle empty exercise name`() {
        // Given
        val userId = 1
        val exerciseName = ""
        val oneRepMax = BigDecimal("100.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals("", userOneRepMax.exerciseName)
    }

    @Test
    fun `should handle negative user ID`() {
        // Given
        val userId = -1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("100.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(-1, userOneRepMax.userId)
    }

    @Test
    fun `should handle zero user ID`() {
        // Given
        val userId = 0
        val exerciseName = "Squat"
        val oneRepMax = BigDecimal("150.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(0, userOneRepMax.userId)
    }

    @Test
    fun `should handle very large user ID`() {
        // Given
        val userId = Int.MAX_VALUE
        val exerciseName = "Deadlift"
        val oneRepMax = BigDecimal("300.0")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(Int.MAX_VALUE, userOneRepMax.userId)
    }

    @Test
    fun `should handle very large one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "World Record Lift"
        val oneRepMax = BigDecimal("999.99")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(BigDecimal("999.99"), userOneRepMax.oneRepMax)
    }

    @Test
    fun `should handle very small one rep max values`() {
        // Given
        val userId = 1
        val exerciseName = "Light Exercise"
        val oneRepMax = BigDecimal("0.01")

        // When
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = oneRepMax,
            )

        // Then
        assertEquals(BigDecimal("0.01"), userOneRepMax.oneRepMax)
    }
}
