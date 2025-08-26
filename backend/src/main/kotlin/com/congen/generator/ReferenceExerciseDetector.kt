package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.UserOneRepMax
import org.springframework.stereotype.Component

/**
 * Advanced reference exercise detector that uses multiple factors to identify
 * the best reference exercises from the database dynamically.
 *
 * This component can be used to replace hardcoded reference exercise names
 * with intelligent detection based on exercise characteristics, user data,
 * and usage patterns.
 */
@Component
class ReferenceExerciseDetector {
    /**
     * Represents a reference exercise candidate with a score.
     *
     * @param exercise The reference exercise candidate
     * @param score The overall similarity score (0.0 to 1.0)
     * @param factors Detailed scoring factors for transparency
     */
    data class ReferenceCandidate(
        val exercise: Exercise,
        val score: Double,
        val factors: Map<String, Double>
    )

    /**
     * Finds the best reference exercises for each movement pattern.
     *
     * @param allExercises All available exercises
     * @param userOneRepMaxes User's 1RM data to prioritize exercises they can actually do
     * @param exerciseUsageCounts Optional map of exercise usage frequency
     * @return List of best reference exercises
     */
    fun findBestReferenceExercises(
        allExercises: List<Exercise>,
        userOneRepMaxes: List<UserOneRepMax> = emptyList(),
        exerciseUsageCounts: Map<String, Int> = emptyMap()
    ): List<Exercise> {
        val candidates =
            allExercises.mapNotNull { exercise ->
                val score =
                    calculateReferenceScore(
                        exercise = exercise,
                        userOneRepMaxes = userOneRepMaxes,
                        exerciseUsageCounts = exerciseUsageCounts
                    )
                if (score.score > 0.5) { // Only include exercises with decent reference potential
                    ReferenceCandidate(exercise, score.score, score.factors)
                } else {
                    null
                }
            }

        return candidates
            .sortedByDescending { it.score }
            .map { it.exercise }
    }

    /**
     * Calculates a score for how good an exercise is as a reference exercise.
     */
    private fun calculateReferenceScore(
        exercise: Exercise,
        userOneRepMaxes: List<UserOneRepMax>,
        exerciseUsageCounts: Map<String, Int>
    ): ReferenceScore {
        val factors = mutableMapOf<String, Double>()

        // Factor 1: Equipment preference (barbell > dumbbell > machine)
        val equipmentScore = calculateEquipmentScore(exercise)
        factors["equipment"] = equipmentScore

        // Factor 2: Movement pattern purity
        val patternScore = calculatePatternPurityScore(exercise)
        factors["pattern_purity"] = patternScore

        // Factor 3: User has 1RM for this exercise
        val hasOneRepMax = userOneRepMaxes.any { it.exerciseName == exercise.name }
        val oneRepMaxScore = if (hasOneRepMax) 1.0 else 0.0
        factors["has_1rm"] = oneRepMaxScore

        // Factor 4: Exercise popularity/usage
        val usageScore = calculateUsageScore(exercise.name, exerciseUsageCounts)
        factors["usage"] = usageScore

        // Factor 5: Exercise name clarity (prefer clear, standard names)
        val nameScore = calculateNameClarityScore(exercise.name)
        factors["name_clarity"] = nameScore

        // Weighted average of all factors
        val totalScore = (
            equipmentScore * 0.3 +
                patternScore * 0.25 +
                oneRepMaxScore * 0.25 +
                usageScore * 0.1 +
                nameScore * 0.1
        )

        return ReferenceScore(totalScore, factors)
    }

    private fun calculateEquipmentScore(exercise: Exercise): Double {
        val name = exercise.name.lowercase()
        return when {
            name.contains("barbell") || name.contains("bar") -> 1.0
            name.contains("dumbbell") -> 0.7
            name.contains("machine") || name.contains("cable") -> 0.4
            name.contains("bodyweight") -> 0.6
            else -> 0.8 // Default assumption
        }
    }

    private fun calculatePatternPurityScore(exercise: Exercise): Double {
        val name = exercise.name.lowercase()

        return when {
            // Squat patterns
            name.contains("back squat") -> 1.0
            name.contains("squat") && !name.contains("front") && !name.contains("split") -> 0.9
            name.contains("safety bar squat") -> 0.8

            // Bench patterns
            name.contains("bench press") && !name.contains("incline") && !name.contains("decline") -> 1.0
            name.contains("flat bench") -> 0.9

            // Deadlift patterns
            name.contains("conventional deadlift") -> 1.0
            name.contains("deadlift") && !name.contains("romanian") && !name.contains("sumo") -> 0.9

            // Overhead patterns
            name.contains("strict press") -> 1.0
            name.contains("overhead press") -> 0.9
            name.contains("military press") -> 0.8

            // Other compound movements
            name.contains("row") && !name.contains("cable") -> 0.7
            name.contains("dip") -> 0.7
            name.contains("pull up") -> 0.7

            else -> 0.5 // Default for other exercises
        }
    }

    private fun calculateUsageScore(
        exerciseName: String,
        exerciseUsageCounts: Map<String, Int>
    ): Double {
        val usageCount = exerciseUsageCounts[exerciseName] ?: 0
        return when {
            usageCount > 100 -> 1.0
            usageCount > 50 -> 0.8
            usageCount > 20 -> 0.6
            usageCount > 5 -> 0.4
            else -> 0.2
        }
    }

    private fun calculateNameClarityScore(exerciseName: String): Double {
        val name = exerciseName.lowercase()
        return when {
            name.contains("conventional") || name.contains("strict") -> 1.0
            name.contains("flat") || name.contains("back") -> 0.9
            name.length < 20 -> 0.8
            name.length < 30 -> 0.6
            else -> 0.4
        }
    }

    private data class ReferenceScore(
        val score: Double,
        val factors: Map<String, Double>
    )
}
