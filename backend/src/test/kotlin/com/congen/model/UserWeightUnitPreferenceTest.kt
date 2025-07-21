package com.congen.model

import com.congen.mockUserWeightUnitPreference
import com.congen.sampleInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for UserWeightUnitPreference model.
 *
 * Tests the data class properties and behavior to ensure proper
 * handling of user weight unit preferences.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserWeightUnitPreferenceTest {
    @Test
    fun `should create UserWeightUnitPreference with all properties`() {
        val preference =
            mockUserWeightUnitPreference(
                userId = 1,
                exerciseName = "Bench Press",
                preferredUnit = WeightUnit.LBS
            )

        assertEquals(1, preference.userId)
        assertEquals("Bench Press", preference.exerciseName)
        assertEquals(WeightUnit.LBS, preference.preferredUnit)
        assertEquals(sampleInstant(), preference.createdAt)
        assertEquals(sampleInstant(), preference.updatedAt)
    }

    @Test
    fun `should create UserWeightUnitPreference with KG unit`() {
        val preference =
            mockUserWeightUnitPreference(
                userId = 2,
                exerciseName = "Deadlift",
                preferredUnit = WeightUnit.KG
            )

        assertEquals(2, preference.userId)
        assertEquals("Deadlift", preference.exerciseName)
        assertEquals(WeightUnit.KG, preference.preferredUnit)
    }

    @Test
    fun `should support different exercise names`() {
        val exercises = listOf("Squat", "Bench Press", "Deadlift", "Overhead Press")

        exercises.forEach { exerciseName ->
            val preference =
                mockUserWeightUnitPreference(
                    userId = 1,
                    exerciseName = exerciseName,
                    preferredUnit = WeightUnit.LBS
                )
            assertEquals(exerciseName, preference.exerciseName)
        }
    }

    @Test
    fun `should support different user IDs`() {
        val userIds = listOf(1, 2, 3, 100, 999)

        userIds.forEach { userId ->
            val preference =
                mockUserWeightUnitPreference(
                    userId = userId,
                    exerciseName = "Bench Press",
                    preferredUnit = WeightUnit.LBS
                )
            assertEquals(userId, preference.userId)
        }
    }

    @Test
    fun `should support both weight units`() {
        val kgPreference =
            mockUserWeightUnitPreference(
                userId = 1,
                exerciseName = "Bench Press",
                preferredUnit = WeightUnit.KG
            )

        val lbsPreference =
            mockUserWeightUnitPreference(
                userId = 1,
                exerciseName = "Bench Press",
                preferredUnit = WeightUnit.LBS
            )

        assertEquals(WeightUnit.KG, kgPreference.preferredUnit)
        assertEquals(WeightUnit.LBS, lbsPreference.preferredUnit)
    }

    @Test
    fun `should handle different timestamps`() {
        val createdAt = Instant.parse("2024-01-01T00:00:00Z")
        val updatedAt = Instant.parse("2024-01-02T12:00:00Z")

        val preference =
            UserWeightUnitPreference(
                userId = 1,
                exerciseName = "Bench Press",
                preferredUnit = WeightUnit.LBS,
                createdAt = createdAt,
                updatedAt = updatedAt
            )

        assertEquals(createdAt, preference.createdAt)
        assertEquals(updatedAt, preference.updatedAt)
    }
}
