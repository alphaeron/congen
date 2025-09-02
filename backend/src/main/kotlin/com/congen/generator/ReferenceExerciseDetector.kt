package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.MovementType
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
     * @property exercise The reference exercise candidate
     * @property score The overall similarity score (0.0 to 1.0)
     * @property factors Detailed scoring factors for transparency
     */
    data class ReferenceCandidate(
        val exercise: Exercise,
        val score: Double,
        val factors: Map<String, Double>
    )

    /**
     * Configuration for scoring weights that can be adjusted without code changes.
     */
    companion object {
        private const val EQUIPMENT_WEIGHT = 0.3
        private const val PATTERN_PURITY_WEIGHT = 0.25
        private const val ONE_RM_WEIGHT = 0.25
        private const val USAGE_WEIGHT = 0.1
        private const val NAME_CLARITY_WEIGHT = 0.1
        
        // Equipment preference scores (can be moved to configuration)
        private val EQUIPMENT_PREFERENCES = mapOf(
            "barbell" to 1.0,
            "dumbbell" to 0.7,
            "machine" to 0.4,
            "cable" to 0.4,
            "bodyweight" to 0.6,
            "kettlebell" to 0.8,
            "trap bar" to 0.9,
            "safety squat bar" to 0.9
        )
    }

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
        val candidates = allExercises.mapNotNull { exercise ->
            val score = calculateReferenceScore(
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

        // Factor 1: Equipment preference (dynamic based on equipment type)
        val equipmentScore = calculateEquipmentScore(exercise)
        factors["equipment"] = equipmentScore

        // Factor 2: Movement pattern purity (based on exercise characteristics)
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
            equipmentScore * EQUIPMENT_WEIGHT +
            patternScore * PATTERN_PURITY_WEIGHT +
            oneRepMaxScore * ONE_RM_WEIGHT +
            usageScore * USAGE_WEIGHT +
            nameScore * NAME_CLARITY_WEIGHT
        )

        return ReferenceScore(totalScore, factors)
    }

    /**
     * Calculates equipment score based on exercise characteristics rather than hardcoded names.
     */
    private fun calculateEquipmentScore(exercise: Exercise): Double {
        val name = exercise.name.lowercase()
        
        // Find the best matching equipment preference
        return EQUIPMENT_PREFERENCES.entries
            .firstOrNull { (equipment, _) -> name.contains(equipment) }
            ?.value ?: 0.8 // Default assumption for unknown equipment
    }

    /**
     * Calculates pattern purity score based on exercise characteristics rather than hardcoded names.
     */
    private fun calculatePatternPurityScore(exercise: Exercise): Double {
        val name = exercise.name.lowercase()
        val movementType = exercise.movementType
        
        // Base score from movement type characteristics
        var baseScore = when (movementType) {
            MovementType.SQUAT -> 0.9
            MovementType.HINGE -> 0.9
            MovementType.HORIZONTAL_PUSH -> 0.8
            MovementType.HORIZONTAL_PULL -> 0.8
            MovementType.VERTICAL_PUSH -> 0.8
            MovementType.VERTICAL_PULL -> 0.8
            MovementType.CORE -> 0.7
            MovementType.CARRY -> 0.6
            MovementType.LUNGE -> 0.7
            MovementType.PLYOMETRIC -> 0.6
            MovementType.ISOLATION -> 0.5
        }
        
        // Adjust based on exercise characteristics
        when {
            // Prefer compound movements (not unilateral, not accessory)
            exercise.isUnilateral -> baseScore *= 0.8
            exercise.isAccessory -> baseScore *= 0.7
            
            // Prefer upper body movements for upper body patterns
            exercise.isUpper && (movementType == MovementType.HORIZONTAL_PUSH || 
                                movementType == MovementType.VERTICAL_PUSH ||
                                movementType == MovementType.HORIZONTAL_PULL || 
                                movementType == MovementType.VERTICAL_PULL) -> 
                baseScore *= 1.1
            
            // Prefer lower body movements for lower body patterns
            !exercise.isUpper && (movementType == MovementType.SQUAT || movementType == MovementType.HINGE) -> 
                baseScore *= 1.1
        }
        
        return baseScore.coerceIn(0.0, 1.0)
    }

    /**
     * Calculates usage score based on exercise popularity.
     */
    private fun calculateUsageScore(
        exerciseName: String,
        exerciseUsageCounts: Map<String, Int>
    ): Double {
        val usageCount = exerciseUsageCounts[exerciseName] ?: 0
        
        // Dynamic scoring based on usage distribution
        return when {
            usageCount > 100 -> 1.0
            usageCount > 50 -> 0.8
            usageCount > 20 -> 0.6
            usageCount > 5 -> 0.4
            else -> 0.2
        }
    }

    /**
     * Calculates name clarity score based on exercise name characteristics.
     */
    private fun calculateNameClarityScore(exerciseName: String): Double {
        val name = exerciseName.lowercase()
        
        // Prefer shorter, clearer names
        var score = when {
            name.length < 15 -> 1.0
            name.length < 25 -> 0.8
            name.length < 35 -> 0.6
            else -> 0.4
        }
        
        return score.coerceIn(0.0, 1.0)
    }

    private data class ReferenceScore(
        val score: Double,
        val factors: Map<String, Double>
    )
}
