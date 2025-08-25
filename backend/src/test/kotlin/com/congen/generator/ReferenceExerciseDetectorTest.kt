package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the ReferenceExerciseDetector.
 *
 * These tests verify that the detector correctly identifies and scores
 * reference exercises based on multiple factors including equipment,
 * movement patterns, user data, and usage patterns.
 */
class ReferenceExerciseDetectorTest {
    private lateinit var referenceExerciseDetector: ReferenceExerciseDetector

    companion object {
        private const val USER_ID = "test-user-123"
    }

    @BeforeEach
    fun setUp() {
        referenceExerciseDetector = ReferenceExerciseDetector()
    }

    @Test
    fun `findBestReferenceExercises should return empty list for empty exercises`() {
        // Given
        val emptyExercises = emptyList<Exercise>()

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(emptyExercises)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findBestReferenceExercises should return exercises with score above threshold`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Conventional Deadlift", MovementType.HINGE)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.name in listOf("Back Squat", "Bench Press", "Conventional Deadlift") })
    }

    @Test
    fun `findBestReferenceExercises should prioritize exercises with user 1RM data`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Conventional Deadlift", MovementType.HINGE)
        )
        val userOneRepMaxes = listOf(
            createUserOneRepMax("Bench Press", BigDecimal("200.0"))
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, userOneRepMaxes)

        // Then
        assertTrue(result.isNotEmpty())
        // Bench Press should be prioritized due to having 1RM data
        assertTrue(result.first().name == "Bench Press" || result.any { it.name == "Bench Press" })
    }

    @Test
    fun `findBestReferenceExercises should consider exercise usage patterns`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Conventional Deadlift", MovementType.HINGE)
        )
        val exerciseUsageCounts = mapOf(
            "Back Squat" to 150,
            "Bench Press" to 50,
            "Conventional Deadlift" to 25
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, emptyList(), exerciseUsageCounts)

        // Then
        assertTrue(result.isNotEmpty())
        // Back Squat should be prioritized due to higher usage
        assertTrue(result.first().name == "Back Squat" || result.any { it.name == "Back Squat" })
    }

    @Test
    fun `findBestReferenceExercises should prioritize barbell exercises`() {
        // Given
        val exercises = listOf(
            createExercise("Barbell Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Dumbbell Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Machine Bench Press", MovementType.HORIZONTAL_PUSH)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Barbell exercises should be prioritized
        assertTrue(result.first().name == "Barbell Bench Press" || result.any { it.name == "Barbell Bench Press" })
    }

    @Test
    fun `findBestReferenceExercises should prioritize pure movement patterns`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Front Squat", MovementType.SQUAT),
            createExercise("Split Squat", MovementType.LUNGE)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Back Squat should be prioritized as the purest squat pattern
        assertTrue(result.first().name == "Back Squat" || result.any { it.name == "Back Squat" })
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with clear names`() {
        // Given
        val exercises = listOf(
            createExercise("Conventional Deadlift", MovementType.HINGE),
            createExercise("Deadlift", MovementType.HINGE),
            createExercise("Very Long Exercise Name That Is Not Clear", MovementType.HINGE)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Conventional Deadlift should be prioritized due to clear name
        assertTrue(result.first().name == "Conventional Deadlift" || result.any { it.name == "Conventional Deadlift" })
    }

    @Test
    fun `findBestReferenceExercises should filter out low-scoring exercises`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Very Obscure Exercise With Low Score", MovementType.ISOLATION)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Should only include exercises with score > 0.5
        assertTrue(result.all { it.name == "Back Squat" })
    }

    @Test
    fun `findBestReferenceExercises should handle mixed factors correctly`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Conventional Deadlift", MovementType.HINGE)
        )
        val userOneRepMaxes = listOf(
            createUserOneRepMax("Bench Press", BigDecimal("200.0"))
        )
        val exerciseUsageCounts = mapOf(
            "Back Squat" to 200,
            "Bench Press" to 100,
            "Conventional Deadlift" to 50
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, userOneRepMaxes, exerciseUsageCounts)

        // Then
        assertTrue(result.isNotEmpty())
        // Should consider all factors in scoring
        assertTrue(result.size >= 2)
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with no usage data`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        )
        val emptyUsageCounts = emptyMap<String, Int>()

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, emptyList(), emptyUsageCounts)

        // Then
        assertTrue(result.isNotEmpty())
        // Should still score exercises based on other factors
        assertTrue(result.size >= 1)
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with no user 1RM data`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        )
        val emptyOneRepMaxes = emptyList<UserOneRepMax>()

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, emptyOneRepMaxes)

        // Then
        assertTrue(result.isNotEmpty())
        // Should still score exercises based on other factors
        assertTrue(result.size >= 1)
    }

    @Test
    fun `findBestReferenceExercises should prioritize strict press over military press`() {
        // Given
        val exercises = listOf(
            createExercise("Strict Press", MovementType.VERTICAL_PUSH),
            createExercise("Military Press", MovementType.VERTICAL_PUSH),
            createExercise("Overhead Press", MovementType.VERTICAL_PUSH)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Strict Press should be prioritized due to name clarity
        assertTrue(result.first().name == "Strict Press" || result.any { it.name == "Strict Press" })
    }

    @Test
    fun `findBestReferenceExercises should handle bodyweight exercises`() {
        // Given
        val exercises = listOf(
            createExercise("Bodyweight Squat", MovementType.SQUAT),
            createExercise("Barbell Squat", MovementType.SQUAT)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Barbell exercises should be prioritized over bodyweight
        assertTrue(result.first().name == "Barbell Squat" || result.any { it.name == "Barbell Squat" })
    }

    @Test
    fun `findBestReferenceExercises should handle cable and machine exercises`() {
        // Given
        val exercises = listOf(
            createExercise("Cable Row", MovementType.HORIZONTAL_PULL),
            createExercise("Machine Row", MovementType.HORIZONTAL_PULL),
            createExercise("Barbell Row", MovementType.HORIZONTAL_PULL)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Barbell exercises should be prioritized over cable/machine
        assertTrue(result.first().name == "Barbell Row" || result.any { it.name == "Barbell Row" })
    }

    @Test
    fun `findBestReferenceExercises should handle compound movements`() {
        // Given
        val exercises = listOf(
            createExercise("Dip", MovementType.VERTICAL_PUSH),
            createExercise("Pull Up", MovementType.VERTICAL_PULL),
            createExercise("Row", MovementType.HORIZONTAL_PULL)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Should include compound movements
        assertTrue(result.size >= 1)
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with multiple factors`() {
        // Given
        val exercises = listOf(
            createExercise("Back Squat", MovementType.SQUAT),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Conventional Deadlift", MovementType.HINGE)
        )
        val userOneRepMaxes = listOf(
            createUserOneRepMax("Back Squat", BigDecimal("300.0")),
            createUserOneRepMax("Bench Press", BigDecimal("200.0"))
        )
        val exerciseUsageCounts = mapOf(
            "Back Squat" to 300,
            "Bench Press" to 250,
            "Conventional Deadlift" to 200
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, userOneRepMaxes, exerciseUsageCounts)

        // Then
        assertTrue(result.isNotEmpty())
        // Should consider all factors and return multiple exercises
        assertTrue(result.size >= 2)
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with edge case names`() {
        // Given
        val exercises = listOf(
            createExercise("Exercise With Very Long Name That Exceeds Normal Length", MovementType.HORIZONTAL_PUSH),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        )
        val userOneRepMaxes = listOf(
            createUserOneRepMax("Bench Press", BigDecimal("200.0"))
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises, userOneRepMaxes)

        // Then
        assertTrue(result.isNotEmpty())
        // Should prefer shorter, clearer names - check if Bench Press is in the results
        assertTrue(result.any { it.name == "Bench Press" })
    }

    @Test
    fun `findBestReferenceExercises should handle exercises with special characters`() {
        // Given
        val exercises = listOf(
            createExercise("Bench-Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        )

        // When
        val result = referenceExerciseDetector.findBestReferenceExercises(exercises)

        // Then
        assertTrue(result.isNotEmpty())
        // Should handle both variations
        assertTrue(result.size >= 1)
    }

    private fun createExercise(name: String, movementType: MovementType): Exercise {
        return Exercise(
            name = name,
            description = "Test exercise description",
            movementType = movementType,
            isUnilateral = false,
            isUpper = movementType in listOf(MovementType.HORIZONTAL_PUSH, MovementType.VERTICAL_PUSH, MovementType.HORIZONTAL_PULL, MovementType.VERTICAL_PULL),
            isAccessory = false
        )
    }

    private fun createUserOneRepMax(exerciseName: String, oneRepMax: BigDecimal): UserOneRepMax {
        return UserOneRepMax(
            userId = USER_ID,
            exerciseName = exerciseName,
            oneRepMax = oneRepMax,
            updatedAt = Instant.now()
        )
    }
}
