package com.congen.generator

import com.congen.dal.ExerciseMuscleDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.UserOneRepMax
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for automatically matching exercises to reference lifts using multiple similarity metrics.
 *
 * This service combines movement pattern classification, equipment similarity, and muscle group 
 * relationships to determine the most similar reference exercise for weight estimation.
 *
 * ## Similarity Metrics
 *
 * - **Name Similarity**: String similarity between exercise names
 * - **Movement Pattern**: Classification by movement type (squat, hinge, push, pull, isolation)
 * - **Equipment Similarity**: Overlap in required equipment
 * - **Muscle Group Similarity**: Overlap in target muscle groups
 *
 * ## Reference Lifts
 *
 * The system dynamically finds reference exercises using the ReferenceExerciseDetector
 * based on exercise characteristics rather than hardcoded names.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ExerciseMatchingService(
    private val referenceExerciseDetector: ReferenceExerciseDetector
) {
    companion object {
        private const val WEIGHT_SCALE = 2
        
        // Scoring weights for similarity calculation
        private const val NAME_SIMILARITY_WEIGHT = 0.3
        private const val MOVEMENT_PATTERN_WEIGHT = 0.3
        private const val EQUIPMENT_SIMILARITY_WEIGHT = 0.2
        private const val MUSCLE_GROUP_SIMILARITY_WEIGHT = 0.2

        /**
         * Result of exercise matching with similarity score and reference exercise.
         *
         * @property referenceExercise The best matching reference exercise
         * @property similarityScore The overall similarity score (0.0 to 1.0)
         * @property movementPattern The classified movement pattern
         * @property factors Detailed similarity factors for transparency
         */
        data class ExerciseMatch(
            val referenceExercise: Exercise,
            val similarityScore: Double,
            val movementPattern: MovementType,
            val factors: SimilarityFactors
        )

        /**
         * Detailed similarity factors for transparency.
         *
         * @property nameSimilarity Similarity based on exercise name (0.0 to 1.0)
         * @property movementPatternSimilarity Similarity based on movement pattern (0.0 or 1.0)
         * @property equipmentSimilarity Similarity based on equipment overlap (0.0 to 1.0)
         * @property muscleGroupSimilarity Similarity based on muscle group overlap (0.0 to 1.0)
         */
        data class SimilarityFactors(
            val nameSimilarity: Double,
            val movementPatternSimilarity: Double,
            val equipmentSimilarity: Double,
            val muscleGroupSimilarity: Double
        )
    }

    /**
     * Finds the best matching reference exercise for an exercise.
     *
     * @param targetExercise The exercise to find a reference for
     * @param allExercises List of all available exercises for comparison
     * @param exerciseEquipmentMap Map of exercise names to their equipment relationships
     * @param exerciseMuscleMap Map of exercise names to their muscle relationships
     * @param userOneRepMaxes Optional list of user's 1RM data to prioritize exercises they can actually do
     * @return ExerciseMatch with the best reference exercise and similarity score
     */
    fun findBestReferenceExercise(
        targetExercise: Exercise,
        allExercises: List<Exercise>,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>,
        userOneRepMaxes: List<UserOneRepMax> = emptyList()
    ): ExerciseMatch {
        val movementPattern = targetExercise.movementType

        // Use the ReferenceExerciseDetector to find the best reference exercises
        val referenceExercises =
            referenceExerciseDetector.findBestReferenceExercises(
                allExercises = allExercises,
                userOneRepMaxes = userOneRepMaxes
            )

        // Find the best reference exercise for this movement pattern
        val bestReferenceExercise =
            referenceExercises
                .filter { exercise -> exercise.movementType == movementPattern }
                .maxByOrNull { exercise ->
                    calculateOverallSimilarity(
                        targetExercise,
                        exercise,
                        exerciseEquipmentMap,
                        exerciseMuscleMap
                    )
                }

        if (bestReferenceExercise != null) {
            val similarity =
                calculateOverallSimilarity(
                    targetExercise,
                    bestReferenceExercise,
                    exerciseEquipmentMap,
                    exerciseMuscleMap
                )

            return ExerciseMatch(
                referenceExercise = bestReferenceExercise,
                similarityScore = similarity,
                movementPattern = movementPattern,
                factors = calculateSimilarityFactors(
                    targetExercise,
                    bestReferenceExercise,
                    exerciseEquipmentMap,
                    exerciseMuscleMap
                )
            )
        }

        // Fallback: find any exercise with the same movement pattern
        val fallbackExercise = allExercises
            .filter { it.movementType == movementPattern }
            .firstOrNull()
            ?: allExercises.first()

        return ExerciseMatch(
            referenceExercise = fallbackExercise,
            similarityScore = 0.5, // Default fallback score
            movementPattern = movementPattern,
            factors = SimilarityFactors(
                nameSimilarity = 0.0,
                movementPatternSimilarity = 1.0,
                equipmentSimilarity = 0.0,
                muscleGroupSimilarity = 0.0
            )
        )
    }

    /**
     * Estimates weight for an exercise based on a reference exercise and similarity score.
     */
    fun estimateWeightFromReference(
        targetExercise: Exercise,
        referenceExercise: Exercise,
        referenceOneRepMax: BigDecimal,
        similarityScore: Double
    ): BigDecimal {
        // Base percentage depends on exercise characteristics rather than hardcoded names
        val basePercentage = calculateBasePercentageFromCharacteristics(targetExercise, referenceExercise)
        
        // Adjust based on similarity score
        val adjustedPercentage = adjustPercentageBySimilarity(basePercentage, similarityScore)
        
        return (referenceOneRepMax * BigDecimal(adjustedPercentage))
            .setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Estimates weight for isolation exercises using bodyweight-based calculation.
     *
     * @param targetExercise The isolation exercise
     * @param userBodyweight The user's bodyweight in kg
     * @return Estimated weight for the isolation exercise
     */
    fun estimateIsolationWeight(
        targetExercise: Exercise,
        userBodyweight: BigDecimal
    ): BigDecimal {
        val bodyweightPercentage = getBodyweightPercentageForExercise(targetExercise)
        return userBodyweight
            .multiply(BigDecimal(bodyweightPercentage))
            .setScale(WEIGHT_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Calculates overall similarity between two exercises.
     */
    private fun calculateOverallSimilarity(
        targetExercise: Exercise,
        referenceExercise: Exercise,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>
    ): Double {
        val factors = calculateSimilarityFactors(
            targetExercise,
            referenceExercise,
            exerciseEquipmentMap,
            exerciseMuscleMap
        )

        return (
            factors.nameSimilarity * NAME_SIMILARITY_WEIGHT +
            factors.movementPatternSimilarity * MOVEMENT_PATTERN_WEIGHT +
            factors.equipmentSimilarity * EQUIPMENT_SIMILARITY_WEIGHT +
            factors.muscleGroupSimilarity * MUSCLE_GROUP_SIMILARITY_WEIGHT
        )
    }

    /**
     * Calculates detailed similarity factors between two exercises.
     */
    private fun calculateSimilarityFactors(
        targetExercise: Exercise,
        referenceExercise: Exercise,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>
    ): SimilarityFactors {
        val nameSimilarity = calculateNameSimilarity(targetExercise.name, referenceExercise.name)
        val movementPatternSimilarity = if (targetExercise.movementType == referenceExercise.movementType) 1.0 else 0.0
        val equipmentSimilarity = calculateEquipmentSimilarity(
            targetExercise.name,
            referenceExercise.name,
            exerciseEquipmentMap
        )
        val muscleGroupSimilarity = calculateMuscleGroupSimilarity(
            targetExercise.name,
            referenceExercise.name,
            exerciseMuscleMap
        )

        return SimilarityFactors(
            nameSimilarity = nameSimilarity,
            movementPatternSimilarity = movementPatternSimilarity,
            equipmentSimilarity = equipmentSimilarity,
            muscleGroupSimilarity = muscleGroupSimilarity
        )
    }

    /**
     * Calculates name similarity using Jaro-Winkler distance.
     */
    private fun calculateNameSimilarity(name1: String, name2: String): Double {
        val normalized1 = name1.lowercase().replace(Regex("[^a-z0-9]"), "")
        val normalized2 = name2.lowercase().replace(Regex("[^a-z0-9]"), "")
        
        if (normalized1 == normalized2) return 1.0
        if (normalized1.isEmpty() || normalized2.isEmpty()) return 0.0
        
        return calculateJaroWinklerSimilarity(normalized1, normalized2)
    }

    /**
     * Calculates Jaro-Winkler similarity between two strings.
     */
    private fun calculateJaroWinklerSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        
        val matchWindow = (maxOf(s1.length, s2.length) / 2) - 1
        if (matchWindow < 0) return 0.0
        
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        
        var matches = 0
        var transpositions = 0
        
        // Find matches
        for (i in s1.indices) {
            val start = maxOf(0, i - matchWindow)
            val end = minOf(i + matchWindow + 1, s2.length)
            
            for (j in start until end) {
                if (!s2Matches[j] && s1[i] == s2[j]) {
                    s1Matches[i] = true
                    s2Matches[j] = true
                    matches++
                    break
                }
            }
        }
        
        if (matches == 0) return 0.0
        
        // Count transpositions
        var k = 0
        for (i in s1.indices) {
            if (s1Matches[i]) {
                while (!s2Matches[k]) k++
                if (s1[i] != s2[k]) transpositions++
                k++
            }
        }
        
        val jaroDistance = (matches / s1.length.toDouble() + 
                           matches / s2.length.toDouble() + 
                           (matches - transpositions / 2) / matches.toDouble()) / 3.0
        
        // Calculate Winkler modification
        var prefixLength = 0
        for (i in 0 until minOf(4, minOf(s1.length, s2.length))) {
            if (s1[i] == s2[i]) prefixLength++ else break
        }
        
        return jaroDistance + (0.1 * prefixLength * (1 - jaroDistance))
    }

    /**
     * Calculates equipment similarity based on equipment overlap.
     */
    private fun calculateEquipmentSimilarity(
        exercise1Name: String,
        exercise2Name: String,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>
    ): Double {
        val equipment1 = exerciseEquipmentMap[exercise1Name]?.map { it.equipmentName }?.toSet() ?: emptySet()
        val equipment2 = exerciseEquipmentMap[exercise2Name]?.map { it.equipmentName }?.toSet() ?: emptySet()
        
        if (equipment1.isEmpty() && equipment2.isEmpty()) return 1.0
        if (equipment1.isEmpty() || equipment2.isEmpty()) return 0.0
        
        val intersection = equipment1.intersect(equipment2).size
        val union = equipment1.size + equipment2.size - intersection
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /**
     * Calculates muscle group similarity based on muscle overlap.
     */
    private fun calculateMuscleGroupSimilarity(
        exercise1Name: String,
        exercise2Name: String,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>
    ): Double {
        val muscles1 = exerciseMuscleMap[exercise1Name]?.map { it.muscleName }?.toSet() ?: emptySet()
        val muscles2 = exerciseMuscleMap[exercise2Name]?.map { it.muscleName }?.toSet() ?: emptySet()
        
        if (muscles1.isEmpty() && muscles2.isEmpty()) return 1.0
        if (muscles1.isEmpty() || muscles2.isEmpty()) return 0.0
        
        val intersection = muscles1.intersect(muscles2).size
        val union = muscles1.size + muscles2.size - intersection
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /**
     * Calculates base percentage based on exercise characteristics rather than hardcoded names.
     */
    private fun calculateBasePercentageFromCharacteristics(
        targetExercise: Exercise,
        referenceExercise: Exercise
    ): Double {
        // Base percentage from movement type characteristics
        var basePercentage = when (targetExercise.movementType) {
            MovementType.SQUAT -> 0.85
            MovementType.HINGE -> 0.80
            MovementType.HORIZONTAL_PUSH -> 0.80
            MovementType.VERTICAL_PUSH -> 0.75
            MovementType.HORIZONTAL_PULL -> 0.75
            MovementType.VERTICAL_PULL -> 0.70
            MovementType.CORE -> 0.60
            MovementType.LUNGE -> 0.70
            MovementType.PLYOMETRIC -> 0.50
            MovementType.CARRY -> 0.60
            MovementType.ISOLATION -> 0.50
        }
        
        // Adjust based on exercise characteristics
        when {
            targetExercise.isUnilateral -> basePercentage *= 0.8
            targetExercise.isAccessory -> basePercentage *= 0.7
            targetExercise.isUpper && referenceExercise.isUpper -> basePercentage *= 1.1
            !targetExercise.isUpper && !referenceExercise.isUpper -> basePercentage *= 1.1
        }
        
        return basePercentage.coerceIn(0.1, 1.0)
    }

    /**
     * Adjusts percentage based on similarity score.
     */
    private fun adjustPercentageBySimilarity(
        basePercentage: Double,
        similarityScore: Double
    ): Double {
        // Higher similarity = closer to base percentage
        // Lower similarity = more conservative estimate
        val adjustmentFactor = 0.5 + (similarityScore * 0.5)
        return basePercentage * adjustmentFactor
    }

    /**
     * Gets bodyweight percentage for isolation exercises.
     */
    private fun getBodyweightPercentageForExercise(exercise: Exercise): Double {
        val name = exercise.name.lowercase()

        return when {
            name.contains("curl") -> 0.20
            name.contains("extension") -> 0.25
            name.contains("lateral") -> 0.12
            name.contains("raise") -> 0.15
            name.contains("fly") -> 0.18
            // Conservative default
            else -> 0.15
        }
    }

    /**
     * Calculates a similarity score for an exercise compared to a primary exercise.
     * Higher scores indicate more similarity.
     *
     * @param exercise The exercise to score
     * @param primaryExercise The primary exercise to compare against
     * @return Similarity score (higher is more similar)
     */
    fun calculateExerciseSimilarityScore(
        exercise: Exercise,
        primaryExercise: Exercise
    ): Double {
        var score = 0.0

        // Movement type similarity (highest weight)
        if (exercise.movementType == primaryExercise.movementType) {
            score += 100.0
        } else {
            // Partial credit for related movement types
            score += calculateMovementTypeSimilarity(exercise.movementType, primaryExercise.movementType)
        }

        return score
    }

    /**
     * Calculates a comprehensive similarity score for an exercise compared to a primary exercise.
     * This method includes muscle overlap scoring.
     *
     * @param exercise The exercise to score
     * @param primaryExercise The primary exercise to compare against
     * @param exerciseMuscleDAL Data access layer for exercise muscle relationships
     * @return Similarity score (higher is more similar)
     */
    fun calculateComprehensiveExerciseSimilarityScore(
        exercise: Exercise,
        primaryExercise: Exercise,
        exerciseMuscleDAL: ExerciseMuscleDAL
    ): Mono<Double> {
        return exerciseMuscleDAL.selectExerciseMuscleByExercise(primaryExercise.name)
            .flatMap { primaryExerciseMuscles ->
                exerciseMuscleDAL.selectExerciseMuscleByExercise(exercise.name)
                    .map { exerciseMuscles ->
                        val primaryMuscleNames = primaryExerciseMuscles.map { it.muscleName }.toSet()
                        val exerciseMuscleNames = exerciseMuscles.map { it.muscleName }.toSet()

                        var score = 0.0

                        // Movement type similarity (highest weight)
                        if (exercise.movementType == primaryExercise.movementType) {
                            score += 100.0
                        } else {
                            // Partial credit for related movement types
                            score += calculateMovementTypeSimilarity(exercise.movementType, primaryExercise.movementType)
                        }

                        // Muscle overlap similarity
                        val muscleOverlapScore = calculateMuscleOverlapScore(primaryMuscleNames, exerciseMuscleNames)
                        score += muscleOverlapScore

                        score
                    }
                    .onErrorReturn(0.0)
            }
            .onErrorReturn(0.0)
    }

    /**
     * Calculates muscle overlap score between primary and secondary exercise muscles.
     *
     * @param primaryMuscles The muscles worked by the primary exercise
     * @param exerciseMuscles The muscles worked by the exercise being evaluated
     * @return Muscle overlap score
     */
    private fun calculateMuscleOverlapScore(
        primaryMuscles: Set<String>,
        exerciseMuscles: Set<String>
    ): Double {
        if (primaryMuscles.isEmpty() || exerciseMuscles.isEmpty()) {
            return 0.0
        }

        // Calculate intersection (overlapping muscles)
        val overlappingMuscles = primaryMuscles.intersect(exerciseMuscles)

        // Calculate overlap percentage
        val overlapPercentage = overlappingMuscles.size.toDouble() / primaryMuscles.size.toDouble()

        // Score based on overlap percentage (max 50 points for complete overlap)
        return overlapPercentage * 50.0
    }

    /**
     * Calculates similarity between movement types.
     *
     * @param movementType1 First movement type
     * @param movementType2 Second movement type
     * @return Similarity score
     */
    private fun calculateMovementTypeSimilarity(
        movementType1: MovementType,
        movementType2: MovementType
    ): Double {
        return when {
            // Same category (push/pull)
            (movementType1 == MovementType.HORIZONTAL_PUSH && movementType2 == MovementType.HORIZONTAL_PUSH) ||
                (movementType1 == MovementType.VERTICAL_PUSH && movementType2 == MovementType.VERTICAL_PUSH) ||
                (movementType1 == MovementType.HORIZONTAL_PULL && movementType2 == MovementType.HORIZONTAL_PULL) ||
                (movementType1 == MovementType.VERTICAL_PULL && movementType2 == MovementType.VERTICAL_PULL) -> 50.0

            // Same plane (horizontal/vertical)
            (movementType1 == MovementType.HORIZONTAL_PUSH && movementType2 == MovementType.HORIZONTAL_PULL) ||
                (movementType1 == MovementType.HORIZONTAL_PULL && movementType2 == MovementType.HORIZONTAL_PUSH) ||
                (movementType1 == MovementType.VERTICAL_PUSH && movementType2 == MovementType.VERTICAL_PULL) ||
                (movementType1 == MovementType.VERTICAL_PULL && movementType2 == MovementType.VERTICAL_PUSH) -> 25.0

            // Same body part focus (upper/lower)
            (movementType1 == MovementType.SQUAT && movementType2 == MovementType.HINGE) ||
                (movementType1 == MovementType.HINGE && movementType2 == MovementType.SQUAT) ||
                (
                    movementType1 == MovementType.LUNGE &&
                        (movementType2 == MovementType.SQUAT || movementType2 == MovementType.HINGE)
                ) -> 15.0

            else -> 0.0
        }
    }

    /**
     * Sorts exercises by similarity score to a primary exercise.
     *
     * @param exercises List of exercises to sort
     * @param primaryExercise The primary exercise to compare against
     * @return Sorted list of exercises (most similar first)
     */
    fun sortExercisesBySimilarity(
        exercises: List<Exercise>,
        primaryExercise: Exercise
    ): List<Exercise> {
        return exercises.sortedByDescending { exercise ->
            calculateExerciseSimilarityScore(exercise, primaryExercise)
        }
    }
}
