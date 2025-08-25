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
 * This service combines keyword-based pattern matching, movement pattern classification,
 * equipment similarity, and muscle group relationships to determine the most similar
 * reference exercise for weight estimation.
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
 * The system uses these primary reference lifts:
 * - **Squat**: For squat pattern movements
 * - **Bench Press**: For push pattern movements
 * - **Deadlift**: For hinge and pull pattern movements
 * - **Overhead Press**: For overhead movements
 * - **Bodyweight**: For isolation and bodyweight exercises
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

        // Movement pattern keywords
        private val SQUAT_KEYWORDS = setOf("squat", "lunge", "leg press", "step up", "split squat")
        private val HINGE_KEYWORDS = setOf("deadlift", "hinge", "roman", "sumo", "trap bar", "good morning")
        private val PUSH_KEYWORDS = setOf("bench", "press", "push", "dip", "incline", "decline")
        private val PULL_KEYWORDS = setOf("row", "pull", "chin", "pull up", "lat pulldown")
        private val OVERHEAD_KEYWORDS = setOf("overhead", "shoulder", "military", "strict press")
        private val ISOLATION_KEYWORDS = setOf("curl", "extension", "raise", "fly", "kickback", "lateral")

        // Equipment categories
        private val BARBELL_EQUIPMENT = setOf("barbell", "football bar", "power bar", "safety squat bar", "trap bar")
        private val DUMBBELL_EQUIPMENT = setOf("dumbbell", "dumbbells")
        private val CABLE_EQUIPMENT = setOf("cable", "pulley", "lat pulldown", "seated row")
        private val BODYWEIGHT_EQUIPMENT = setOf("bodyweight", "assisted", "resistance band")

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
        val movementPattern = classifyMovementPattern(targetExercise)

        // Use the ReferenceExerciseDetector to find the best reference exercises
        val referenceExercises =
            referenceExerciseDetector.findBestReferenceExercises(
                allExercises = allExercises,
                userOneRepMaxes = userOneRepMaxes
            )

        // Find the best reference exercise for this movement pattern
        val bestReferenceExercise =
            referenceExercises
                .filter { exercise -> classifyMovementPattern(exercise) == movementPattern }
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
                factors =
                    calculateSimilarityFactors(
                        targetExercise,
                        bestReferenceExercise,
                        exerciseEquipmentMap,
                        exerciseMuscleMap
                    )
            )
        }

        // Fallback to movement pattern-based reference
        val fallbackExercise = getFallbackReferenceExercise(movementPattern, allExercises)
        return ExerciseMatch(
            referenceExercise = fallbackExercise,
            // Moderate confidence for fallback
            similarityScore = 0.5,
            movementPattern = movementPattern,
            factors = SimilarityFactors(0.0, 1.0, 0.0, 0.0)
        )
    }

    /**
     * Estimates weight for an exercise based on reference exercise and similarity.
     *
     * @param targetExercise The exercise to estimate weight for
     * @param referenceExercise The reference exercise to use
     * @param referenceOneRepMax The 1RM of the reference exercise
     * @param similarityScore The similarity score (0.0 to 1.0)
     * @return Estimated weight for the target exercise
     */
    fun estimateWeightFromReference(
        targetExercise: Exercise,
        referenceExercise: Exercise,
        referenceOneRepMax: BigDecimal,
        similarityScore: Double
    ): BigDecimal {
        val basePercentage = getBasePercentageForExercise(targetExercise, referenceExercise)
        val adjustedPercentage = adjustPercentageBySimilarity(basePercentage, similarityScore)

        return referenceOneRepMax
            .multiply(BigDecimal(adjustedPercentage))
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
     * Classifies the movement pattern of an exercise based on its MovementType and name.
     */
    private fun classifyMovementPattern(exercise: Exercise): MovementType {
        // Use the exercise's MovementType directly, with fallback to name-based classification
        val name = exercise.name.lowercase()

        // If the MovementType is already specific, use it
        if (exercise.movementType != MovementType.HORIZONTAL_PUSH &&
            exercise.movementType != MovementType.VERTICAL_PUSH
        ) {
            return exercise.movementType
        }

        // Fallback to name-based classification for push movements or if MovementType is ambiguous
        return when {
            SQUAT_KEYWORDS.any { name.contains(it) } -> MovementType.SQUAT
            HINGE_KEYWORDS.any { name.contains(it) } -> MovementType.HINGE
            OVERHEAD_KEYWORDS.any { name.contains(it) } -> MovementType.VERTICAL_PUSH
            PUSH_KEYWORDS.any { name.contains(it) } -> MovementType.HORIZONTAL_PUSH
            PULL_KEYWORDS.any { name.contains(it) } -> MovementType.HORIZONTAL_PULL
            ISOLATION_KEYWORDS.any { name.contains(it) } -> MovementType.ISOLATION
            name.contains("carry") || name.contains("walk") -> MovementType.CARRY
            // Default fallback
            else -> MovementType.HORIZONTAL_PUSH
        }
    }

    /**
     * Calculates overall similarity between two exercises.
     */
    private fun calculateOverallSimilarity(
        exercise1: Exercise,
        exercise2: Exercise,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>
    ): Double {
        val factors = calculateSimilarityFactors(exercise1, exercise2, exerciseEquipmentMap, exerciseMuscleMap)

        // Weighted average of similarity factors
        return (
            factors.nameSimilarity * 0.4 +
                factors.movementPatternSimilarity * 0.3 +
                factors.equipmentSimilarity * 0.2 +
                factors.muscleGroupSimilarity * 0.1
        )
    }

    /**
     * Calculates detailed similarity factors between two exercises.
     */
    private fun calculateSimilarityFactors(
        exercise1: Exercise,
        exercise2: Exercise,
        exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
        exerciseMuscleMap: Map<String, List<ExerciseMuscle>>
    ): SimilarityFactors {
        val nameSimilarity = calculateNameSimilarity(exercise1.name, exercise2.name)
        val movementSimilarity = if (classifyMovementPattern(exercise1) == classifyMovementPattern(exercise2)) 1.0 else 0.0
        val equipmentSimilarity =
            calculateEquipmentSimilarity(
                exerciseEquipmentMap[exercise1.name] ?: emptyList(),
                exerciseEquipmentMap[exercise2.name] ?: emptyList()
            )
        val muscleSimilarity =
            calculateMuscleGroupSimilarity(
                exerciseMuscleMap[exercise1.name] ?: emptyList(),
                exerciseMuscleMap[exercise2.name] ?: emptyList()
            )

        return SimilarityFactors(nameSimilarity, movementSimilarity, equipmentSimilarity, muscleSimilarity)
    }

    /**
     * Calculates name similarity using Levenshtein distance.
     */
    private fun calculateNameSimilarity(
        name1: String,
        name2: String
    ): Double {
        val maxLength = maxOf(name1.length, name2.length)
        if (maxLength == 0) return 1.0

        val distance = levenshteinDistance(name1.lowercase(), name2.lowercase())
        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Calculates equipment similarity using Jaccard index.
     */
    private fun calculateEquipmentSimilarity(
        equipment1: List<ExerciseEquipment>,
        equipment2: List<ExerciseEquipment>
    ): Double {
        val names1 = equipment1.map { it.equipmentName.lowercase() }.toSet()
        val names2 = equipment2.map { it.equipmentName.lowercase() }.toSet()

        val intersection = names1.intersect(names2).size
        val union = names1.union(names2).size

        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /**
     * Calculates muscle group similarity using Jaccard index.
     */
    private fun calculateMuscleGroupSimilarity(
        muscles1: List<ExerciseMuscle>,
        muscles2: List<ExerciseMuscle>
    ): Double {
        val names1 = muscles1.map { it.muscleName.lowercase() }.toSet()
        val names2 = muscles2.map { it.muscleName.lowercase() }.toSet()

        val intersection = names1.intersect(names2).size
        val union = names1.union(names2).size

        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /**
     * Gets fallback reference exercise based on movement pattern.
     */
    private fun getFallbackReferenceExercise(
        pattern: MovementType,
        allExercises: List<Exercise>
    ): Exercise {
        val fallbackName =
            when (pattern) {
                MovementType.SQUAT -> "Back Squat"
                MovementType.HINGE -> "Deadlift"
                MovementType.HORIZONTAL_PUSH, MovementType.VERTICAL_PUSH -> "Bench Press"
                MovementType.HORIZONTAL_PULL, MovementType.VERTICAL_PULL -> "Deadlift"
                MovementType.CORE, MovementType.ISOLATION -> "Bodyweight"
                MovementType.PLYOMETRIC -> "Bench Press"
                MovementType.CARRY -> "Bodyweight"
                MovementType.LUNGE -> "Back Squat"
            }

        return allExercises.find { it.name == fallbackName }
            ?: allExercises.firstOrNull { it.movementType == pattern }
            // Last resort
            ?: allExercises.first()
    }

    /**
     * Gets base percentage for exercise relative to reference exercise.
     */
    private fun getBasePercentageForExercise(
        targetExercise: Exercise,
        referenceExercise: Exercise
    ): Double {
        val targetName = targetExercise.name.lowercase()
        val referenceName = referenceExercise.name.lowercase()

        // Determine the movement pattern of the reference exercise
        val referencePattern =
            when {
                referenceName.contains("squat") -> "squat"
                referenceName.contains("bench") -> "bench"
                referenceName.contains("deadlift") -> "deadlift"
                referenceName.contains("press") && referenceName.contains("overhead") -> "overhead"
                referenceName.contains("press") && referenceName.contains("strict") -> "overhead"
                referenceName.contains("press") && referenceName.contains("military") -> "overhead"
                else -> "bodyweight"
            }

        return when (referencePattern) {
            "squat" -> {
                when {
                    targetName.contains("front squat") -> 0.85
                    targetName.contains("split squat") -> 0.70
                    targetName.contains("lunge") -> 0.65
                    targetName.contains("step up") -> 0.50
                    else -> 0.80
                }
            }
            "bench" -> {
                when {
                    targetName.contains("incline") -> 0.80
                    targetName.contains("decline") -> 0.85
                    targetName.contains("dumbbell") -> 0.75
                    targetName.contains("dip") -> 0.70
                    else -> 0.75
                }
            }
            "deadlift" -> {
                when {
                    targetName.contains("roman") -> 0.70
                    targetName.contains("sumo") -> 0.85
                    targetName.contains("trap bar") -> 0.90
                    targetName.contains("row") -> 0.60
                    else -> 0.70
                }
            }
            "overhead" -> {
                when {
                    targetName.contains("dumbbell") -> 0.70
                    targetName.contains("strict") -> 0.85
                    else -> 0.75
                }
            }
            // Conservative default for bodyweight exercises
            else -> 0.20
        }
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
        // Range: 0.8 to 1.2
        val adjustmentFactor = 0.8 + (similarityScore * 0.4)
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
     * Calculates Levenshtein distance between two strings.
     */
    private fun levenshteinDistance(
        s1: String,
        s2: String
    ): Int {
        val matrix = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) matrix[i][0] = i
        for (j in 0..s2.length) matrix[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                matrix[i][j] =
                    minOf(
                        // deletion
                        matrix[i - 1][j] + 1,
                        // insertion
                        matrix[i][j - 1] + 1,
                        // substitution
                        matrix[i - 1][j - 1] + cost
                    )
            }
        }

        return matrix[s1.length][s2.length]
    }

    /**
     * Calculates a similarity score for an exercise compared to a primary exercise.
     * Higher scores indicate more similarity.
     *
     * @param exercise The exercise to score
     * @param primaryExercise The primary exercise to compare against
     * @param rotationHistory List of exercise rotation history
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
     * @param rotationHistory List of exercise rotation history
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
     * @param rotationHistory List of exercise rotation history
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
