package com.congen

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.generator.SupportedEquipmentWeightRoundingService
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Integration tests for SupportedEquipmentWeightRoundingService.
 *
 * Tests weight rounding functionality with real database interactions,
 * including equipment lookup and weight unit preferences.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class SupportedEquipmentWeightRoundingServiceIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var supportedEquipmentWeightRoundingService: SupportedEquipmentWeightRoundingService

    @Autowired
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    private lateinit var exerciseEquipmentMappings: Map<String, List<com.congen.model.ExerciseEquipment>>

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Database is already populated with exercise and equipment data from migration scripts
        // Load exercise equipment mappings for use in tests
        exerciseEquipmentMappings = exerciseEquipmentDAL.selectAllExerciseEquipment()
            .map { equipmentList ->
                equipmentList.groupBy { it.exerciseName }
            }
            .block() ?: emptyMap()
    }

    @Test
    fun `should round barbell exercise weight to achievable plate combination`() {
        val exerciseName = "Bench Press" // Uses power bar from migration data
        val targetWeight = BigDecimal("185.0") // 185 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("185.00")) // 45lb bar + 2x45lb + 2x25lb plates = 185lbs
            .verifyComplete()
    }

    @Test
    fun `should return bar weight for target below bar weight`() {
        val exerciseName = "Bench Press"
        val targetWeight = BigDecimal("30.0") // 30 lbs (below 45lb bar)
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("45.00")) // Should return bar weight
            .verifyComplete()
    }

    @Test
    fun `should round kettlebell exercise weight to nearest available weight`() {
        val exerciseName = "Overhead Kettlebell Snatch" // Uses kettlebell from migration data
        val targetWeight = BigDecimal("35.0") // 35 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("35.00")) // Should match available kettlebell weight
            .verifyComplete()
    }

    @Test
    fun `should round kettlebell weight to closest available when exact match not available`() {
        val exerciseName = "Overhead Kettlebell Snatch"
        val targetWeight = BigDecimal("37.0") // 37 lbs (not in standard weights)
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("35.00")) // Should round to closest available (35lb is closer than 40lb)
            .verifyComplete()
    }

    @Test
    fun `should round dumbbell exercise weight to nearest 5lb increment`() {
        val exerciseName = "1-Arm Contralateral Stability Row" // Uses dumbbells from migration data
        val targetWeight = BigDecimal("27.5") // 27.5 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("30.00")) // Should round to nearest 5lb increment
            .verifyComplete()
    }

    @Test
    fun `should round dumbbell weight to nearest 2 5kg increment for kg`() {
        val exerciseName = "1-Arm Contralateral Stability Row"
        val targetWeight = BigDecimal("12.3") // 12.3 kg
        val weightUnit = WeightUnit.KG

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("12.50")) // Should round to nearest 2.5kg increment
            .verifyComplete()
    }

    @Test
    fun `should return original weight for bodyweight exercises`() {
        val exerciseName = "Chin-Up" // Uses pull-up bar, not weight-based equipment
        val targetWeight = BigDecimal("0.0") // Bodyweight exercise
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(targetWeight.setScale(2, RoundingMode.HALF_UP)) // Should return original weight with proper scale
            .verifyComplete()
    }

    @Test
    fun `should handle barbell exercises with kg units`() {
        val exerciseName = "Deadlift" // Uses power bar from migration data
        val targetWeight = BigDecimal("100.0") // 100 kg
        val weightUnit = WeightUnit.KG

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("100.00")) // 20kg bar + 2x25kg + 2x15kg plates = 100kg
            .verifyComplete()
    }

    @Test
    fun `should handle kettlebell exercises with kg units`() {
        val exerciseName = "Overhead Kettlebell Snatch"
        val targetWeight = BigDecimal("18.0") // 18 kg
        val weightUnit = WeightUnit.KG

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("18.00")) // Should match available kettlebell weight
            .verifyComplete()
    }

    @Test
    fun `should handle trap bar exercises as barbell`() {
        val exerciseName = "Trap Bar Deadlift" // Uses trap bar from migration data
        val targetWeight = BigDecimal("225.0") // 225 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("225.00")) // Should use barbell plate logic
            .verifyComplete()
    }

    @Test
    fun `should handle landmine exercises as barbell`() {
        val exerciseName = "Landmine Row" // Uses landmine and power bar from migration data
        val targetWeight = BigDecimal("135.0") // 135 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("135.00")) // Should use barbell plate logic
            .verifyComplete()
    }

    @Test
    fun `should handle safety squat bar exercises as barbell`() {
        val exerciseName = "Safety Bar Squat" // Uses safety squat bar from migration data
        val targetWeight = BigDecimal("275.0") // 275 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("225.00")) // Should use barbell plate logic (275 not achievable with standard plates)
            .verifyComplete()
    }

    @Test
    fun `should handle exercises with multiple equipment types`() {
        val exerciseName = "1-Arm Contralateral Stability Row" // Uses dumbbells, physioball, airex pad
        val targetWeight = BigDecimal("22.5") // 22.5 lbs
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(BigDecimal("25.00")) // Should round to nearest 5lb increment for dumbbells
            .verifyComplete()
    }

    @Test
    fun `should handle non-existent exercise gracefully`() {
        val exerciseName = "NonExistentExercise"
        val targetWeight = BigDecimal("100.0")
        val weightUnit = WeightUnit.LBS

        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit, exerciseEquipmentMappings)

        StepVerifier.create(result)
            .expectNext(targetWeight.setScale(2, RoundingMode.HALF_UP)) // Should return original weight when no equipment found
            .verifyComplete()
    }
}
