package com.congen.generator

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.DatabaseException
import com.congen.model.ExerciseEquipment
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Unit tests for SupportedEquipmentWeightRoundingService.
 *
 * Tests weight rounding functionality for different equipment types including:
 * - Barbell exercises with plate-based weight selection
 * - Kettlebell exercises with standard kettlebell weights
 * - Dumbbell exercises with 5lb increment rounding
 * - Exercises without specific equipment requirements
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class SupportedEquipmentWeightRoundingServiceTest {
    @Mock
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    private lateinit var supportedEquipmentWeightRoundingService: SupportedEquipmentWeightRoundingService

    @BeforeEach
    fun setUp() {
        supportedEquipmentWeightRoundingService = SupportedEquipmentWeightRoundingService(exerciseEquipmentDAL)
    }

    @Test
    fun `roundWeightForExercise should round barbell weight to achievable plate combination`() {
        // Given
        val exerciseName = "Bench Press"
        val targetWeight = BigDecimal("185.0") // 185 lbs
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "power bar"),
                ExerciseEquipment(exerciseName, "bench")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("185.00")) // 45lb bar + 2x45lb + 2x25lb plates = 185lbs
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should return bar weight for target below bar weight`() {
        // Given
        val exerciseName = "Bench Press"
        val targetWeight = BigDecimal("30.0") // 30 lbs (below 45lb bar)
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "power bar")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("45.00")) // Should return bar weight
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should round kettlebell weight to nearest available weight`() {
        // Given
        val exerciseName = "Kettlebell Swing"
        val targetWeight = BigDecimal("35.0") // 35 lbs
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "kettlebell")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("35.00")) // Should match available kettlebell weight
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should round kettlebell weight to closest available when exact match not available`() {
        // Given
        val exerciseName = "Kettlebell Swing"
        val targetWeight = BigDecimal("37.0") // 37 lbs (not in standard weights)
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "kettlebell")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        // 37 is closer to 35 (difference of 2) than to 40 (difference of 3)
        StepVerifier.create(result)
            .expectNext(BigDecimal("35.00")) // Should round to closest available (35lb)
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should round dumbbell weight to nearest 5lb increment`() {
        // Given
        val exerciseName = "Dumbbell Row"
        val targetWeight = BigDecimal("27.5") // 27.5 lbs
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "dumbbells")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("30.00")) // Should round to nearest 5lb increment
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should round dumbbell weight to nearest 2 5kg increment for kg`() {
        // Given
        val exerciseName = "Dumbbell Row"
        val targetWeight = BigDecimal("12.3") // 12.3 kg
        val weightUnit = WeightUnit.KG
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "dumbbells")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("12.50")) // Should round to nearest 2.5kg increment
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should return original weight for exercises without specific equipment`() {
        // Given
        val exerciseName = "Push-Up"
        val targetWeight = BigDecimal("0.0") // Bodyweight exercise
        val weightUnit = WeightUnit.LBS
        val equipment = emptyList<ExerciseEquipment>()

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(targetWeight.setScale(2, RoundingMode.HALF_UP)) // Should return original weight with proper scale
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should handle database errors gracefully`() {
        // Given
        val exerciseName = "Bench Press"
        val targetWeight = BigDecimal("185.0")
        val weightUnit = WeightUnit.LBS

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.error(DatabaseException("Database error")))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(targetWeight.setScale(2, RoundingMode.HALF_UP)) // Should return original weight on error with proper scale
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should handle barbell exercises with kg units`() {
        // Given
        val exerciseName = "Squat"
        val targetWeight = BigDecimal("100.0") // 100 kg
        val weightUnit = WeightUnit.KG
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "power bar")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("100.00")) // 20kg bar + 2x25kg + 2x15kg plates = 100kg
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should handle kettlebell exercises with kg units`() {
        // Given
        val exerciseName = "Kettlebell Snatch"
        val targetWeight = BigDecimal("18.0") // 18 kg
        val weightUnit = WeightUnit.KG
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "kettlebell")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("18.00")) // Should match available kettlebell weight
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should handle trap bar exercises as barbell`() {
        // Given
        val exerciseName = "Trap Bar Deadlift"
        val targetWeight = BigDecimal("225.0") // 225 lbs
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "trap bar")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("225.00")) // Should use barbell plate logic
            .verifyComplete()
    }

    @Test
    fun `roundWeightForExercise should handle landmine exercises as barbell`() {
        // Given
        val exerciseName = "Landmine Row"
        val targetWeight = BigDecimal("135.0") // 135 lbs
        val weightUnit = WeightUnit.LBS
        val equipment =
            listOf(
                ExerciseEquipment(exerciseName, "landmine"),
                ExerciseEquipment(exerciseName, "power bar")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
            .thenReturn(Mono.just(equipment))

        // When
        val result = supportedEquipmentWeightRoundingService.roundWeightForExercise(exerciseName, targetWeight, weightUnit)

        // Then
        StepVerifier.create(result)
            .expectNext(BigDecimal("135.00")) // Should use barbell plate logic
            .verifyComplete()
    }
}
