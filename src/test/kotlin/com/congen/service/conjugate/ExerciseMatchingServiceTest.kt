package com.congen.service.conjugate

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ExerciseMatchingService.
 *
 * Tests exercise matching, similarity calculations, and weight estimation.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ExerciseMatchingServiceTest {
    private val mockReferenceDetector = mock(ReferenceExerciseDetector::class.java)
    private val service = ExerciseMatchingService(mockReferenceDetector)

    @Test
    fun `should classify movement patterns correctly`() {
        val exercises =
            listOf(
                Exercise("Safety Bar Squat", "Squat movement", MovementType.SQUAT, false, false, false),
                Exercise("Bench Press", "Bench press", MovementType.HORIZONTAL_PUSH, false, true, false),
                Exercise("Deadlift", "Deadlift", MovementType.HINGE, false, false, false),
                Exercise("Barbell Curl", "Curl", MovementType.ISOLATION, false, true, true),
                Exercise("Overhead Press", "Overhead press", MovementType.VERTICAL_PUSH, false, true, false)
            )

        val equipmentMap = mapOf<String, List<ExerciseEquipment>>()
        val muscleMap = mapOf<String, List<ExerciseMuscle>>()
        val userOneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Setup mock to return appropriate reference exercises
        `when`(mockReferenceDetector.findBestReferenceExercises(exercises, userOneRepMaxes, emptyMap()))
            .thenReturn(listOf(exercises[0], exercises[1], exercises[2], exercises[4]))

        val squatMatch = service.findBestReferenceExercise(exercises[0], exercises, equipmentMap, muscleMap, userOneRepMaxes)
        assertEquals(exercises[0], squatMatch.referenceExercise)

        val benchMatch = service.findBestReferenceExercise(exercises[1], exercises, equipmentMap, muscleMap, userOneRepMaxes)
        assertEquals(exercises[1], benchMatch.referenceExercise)

        val deadliftMatch = service.findBestReferenceExercise(exercises[2], exercises, equipmentMap, muscleMap, userOneRepMaxes)
        assertEquals(exercises[2], deadliftMatch.referenceExercise)

        val curlMatch = service.findBestReferenceExercise(exercises[3], exercises, equipmentMap, muscleMap, userOneRepMaxes)
        // Should find a fallback exercise since curl is isolation
        // The service looks for "Bodyweight" but falls back to first exercise if not found
        assertTrue(curlMatch.referenceExercise.name.isNotEmpty())

        val overheadMatch = service.findBestReferenceExercise(exercises[4], exercises, equipmentMap, muscleMap, userOneRepMaxes)
        assertEquals(exercises[4], overheadMatch.referenceExercise)
    }

    @Test
    fun `should calculate name similarity correctly`() {
        val exercise1 = Exercise("Bench Press", "Bench press", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise2 = Exercise("Incline Bench Press", "Incline bench", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise3 = Exercise("Deadlift", "Deadlift", MovementType.HINGE, false, false, false)

        val equipmentMap = mapOf<String, List<ExerciseEquipment>>()
        val muscleMap = mapOf<String, List<ExerciseMuscle>>()
        val userOneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Setup mock
        `when`(mockReferenceDetector.findBestReferenceExercises(listOf(exercise1, exercise2, exercise3), userOneRepMaxes, emptyMap()))
            .thenReturn(listOf(exercise1, exercise3))

        val match1 =
            service.findBestReferenceExercise(
                exercise1,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )
        val match2 =
            service.findBestReferenceExercise(
                exercise2,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )

        // Bench Press should match Bench Press with high similarity
        assertEquals(exercise1, match1.referenceExercise)
        println("Bench Press similarity: ${match1.similarityScore}")
        assertTrue(match1.similarityScore > 0.6, "Bench Press similarity ${match1.similarityScore} should be > 0.6")

        // Incline Bench Press should match Bench Press with moderate similarity
        assertEquals(exercise1, match2.referenceExercise)
        println("Incline Bench Press similarity: ${match2.similarityScore}")
        assertTrue(match2.similarityScore > 0.4, "Incline Bench Press similarity ${match2.similarityScore} should be > 0.4")
    }

    @Test
    fun `should estimate weight from reference lift correctly`() {
        val exercise = Exercise("Incline Bench Press", "Incline bench", MovementType.HORIZONTAL_PUSH, false, true, false)
        val referenceOneRepMax = BigDecimal("200")
        val similarityScore = 0.8

        val estimatedWeight =
            service.estimateWeightFromReference(
                exercise,
                // Use same exercise as reference for test
                exercise,
                referenceOneRepMax,
                similarityScore
            )

        // Should be approximately 80% of bench press (0.8 * 200 = 160)
        // But adjusted by similarity score, so it could be higher
        println("Estimated weight: $estimatedWeight")
        assertTrue(estimatedWeight > BigDecimal("150"), "Estimated weight $estimatedWeight should be > 150")
        assertTrue(estimatedWeight < BigDecimal("200"), "Estimated weight $estimatedWeight should be < 200")
    }

    @Test
    fun `should estimate isolation weight correctly`() {
        val exercise = Exercise("Barbell Curl", "Curl", MovementType.ISOLATION, false, true, true)
        val userBodyweight = BigDecimal("70")

        val estimatedWeight = service.estimateIsolationWeight(exercise, userBodyweight)

        // Should be approximately 20% of bodyweight (0.2 * 70 = 14)
        assertTrue(estimatedWeight > BigDecimal("13"))
        assertTrue(estimatedWeight < BigDecimal("15"))
    }

    @Test
    fun `should handle equipment similarity`() {
        val exercise1 = Exercise("Bench Press", "Bench press", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise2 = Exercise("Incline Bench Press", "Incline bench", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise3 = Exercise("Dumbbell Bench Press", "Dumbbell bench", MovementType.HORIZONTAL_PUSH, false, true, false)

        val equipmentMap =
            mapOf(
                "Bench Press" to listOf(ExerciseEquipment("Bench Press", "barbell")),
                "Incline Bench Press" to listOf(ExerciseEquipment("Incline Bench Press", "barbell")),
                "Dumbbell Bench Press" to listOf(ExerciseEquipment("Dumbbell Bench Press", "dumbbell"))
            )
        val muscleMap = mapOf<String, List<ExerciseMuscle>>()
        val userOneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Setup mock
        `when`(mockReferenceDetector.findBestReferenceExercises(listOf(exercise1, exercise2, exercise3), userOneRepMaxes, emptyMap()))
            .thenReturn(listOf(exercise1))

        val match1 =
            service.findBestReferenceExercise(
                exercise1,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )
        val match2 =
            service.findBestReferenceExercise(
                exercise2,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )
        val match3 =
            service.findBestReferenceExercise(
                exercise3,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )

        // Bench Press and Incline Bench Press should have higher similarity due to same equipment
        assertTrue(match1.similarityScore > match3.similarityScore)
        assertTrue(match2.similarityScore > match3.similarityScore)
    }

    @Test
    fun `should handle muscle group similarity`() {
        val exercise1 = Exercise("Bench Press", "Bench press", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise2 = Exercise("Incline Bench Press", "Incline bench", MovementType.HORIZONTAL_PUSH, false, true, false)
        val exercise3 = Exercise("Deadlift", "Deadlift", MovementType.HINGE, false, false, false)

        val equipmentMap = mapOf<String, List<ExerciseEquipment>>()
        val muscleMap =
            mapOf(
                "Bench Press" to listOf(ExerciseMuscle("Bench Press", "chest"), ExerciseMuscle("Bench Press", "triceps")),
                "Incline Bench Press" to
                    listOf(
                        ExerciseMuscle("Incline Bench Press", "chest"),
                        ExerciseMuscle("Incline Bench Press", "triceps")
                    ),
                "Deadlift" to listOf(ExerciseMuscle("Deadlift", "hamstrings"), ExerciseMuscle("Deadlift", "glutes"))
            )
        val userOneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Setup mock
        `when`(mockReferenceDetector.findBestReferenceExercises(listOf(exercise1, exercise2, exercise3), userOneRepMaxes, emptyMap()))
            .thenReturn(listOf(exercise1, exercise3))

        val match1 =
            service.findBestReferenceExercise(
                exercise1,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )
        val match2 =
            service.findBestReferenceExercise(
                exercise2,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )
        val match3 =
            service.findBestReferenceExercise(
                exercise3,
                listOf(exercise1, exercise2, exercise3),
                equipmentMap,
                muscleMap,
                userOneRepMaxes
            )

        // All exercises should have reasonable similarity scores
        println("Bench Press similarity: ${match1.similarityScore}")
        println("Incline Bench Press similarity: ${match2.similarityScore}")
        println("Deadlift similarity: ${match3.similarityScore}")
        assertTrue(match1.similarityScore > 0.5, "Bench Press similarity ${match1.similarityScore} should be > 0.5")
        assertTrue(match2.similarityScore > 0.4, "Incline Bench Press similarity ${match2.similarityScore} should be > 0.4")
        assertTrue(match3.similarityScore > 0.5, "Deadlift similarity ${match3.similarityScore} should be > 0.5")
    }

    @Test
    fun `should provide fallback for unknown exercises`() {
        val exercise = Exercise("Unknown Exercise", "Unknown", MovementType.HORIZONTAL_PUSH, false, false, false)
        val exercises = listOf(exercise)

        val equipmentMap = mapOf<String, List<ExerciseEquipment>>()
        val muscleMap = mapOf<String, List<ExerciseMuscle>>()
        val userOneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Setup mock to return no reference exercises
        `when`(mockReferenceDetector.findBestReferenceExercises(exercises, userOneRepMaxes, emptyMap()))
            .thenReturn(emptyList())

        val match = service.findBestReferenceExercise(exercise, exercises, equipmentMap, muscleMap, userOneRepMaxes)

        // Should provide a fallback reference exercise
        assertTrue(match.referenceExercise.name.isNotEmpty())
        assertTrue(match.similarityScore > 0.0)
        assertTrue(match.similarityScore <= 1.0)
    }

    @Test
    fun `should adjust weight estimation based on similarity score`() {
        val exercise = Exercise("Incline Bench Press", "Incline bench", MovementType.HORIZONTAL_PUSH, false, true, false)
        val referenceOneRepMax = BigDecimal("200")

        val highSimilarityWeight =
            service.estimateWeightFromReference(
                exercise,
                // Use same exercise as reference for test
                exercise,
                referenceOneRepMax,
                // High similarity
                0.9
            )

        val lowSimilarityWeight =
            service.estimateWeightFromReference(
                exercise,
                // Use same exercise as reference for test
                exercise,
                referenceOneRepMax,
                // Low similarity
                0.3
            )

        // Higher similarity should result in weight closer to base percentage
        // Lower similarity should result in more conservative estimate
        assertTrue(highSimilarityWeight > lowSimilarityWeight)
    }
}
