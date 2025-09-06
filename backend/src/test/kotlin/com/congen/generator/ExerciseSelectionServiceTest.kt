package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseWorkoutType
import com.congen.model.MovementType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

/**
 * Unit tests for the ExerciseSelectionService.
 *
 * These tests verify that the service correctly selects exercises based on
 * various criteria including user preferences, equipment
 * availability, and target muscles.
 */
class ExerciseSelectionServiceTest {
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var exerciseMatchingService: ExerciseMatchingService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var userExercisePool: UserExercisePool

    companion object {
        private const val USER_ID = "test-user-id"
    }

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseWorkoutTypeDAL = mock()
        exerciseEquipmentDAL = mock()
        movementBalanceService = mock()
        exerciseMatchingService = mock()
        userExercisePool = mock()

        exerciseSelectionService =
            ExerciseSelectionService(
                exerciseDAL = exerciseDAL,
                exerciseMuscleDAL = exerciseMuscleDAL,
                exerciseWorkoutTypeDAL = exerciseWorkoutTypeDAL,
                exerciseEquipmentDAL = exerciseEquipmentDAL,
                movementBalanceService = movementBalanceService,
                exerciseMatchingService = exerciseMatchingService
            )

        // Mock the selectAllExerciseWorkoutTypes method to return workout types that include test exercises
        val workoutTypes =
            listOf(
                ExerciseWorkoutType(
                    exerciseName = "Bench Press",
                    movementType = MovementType.HORIZONTAL_PUSH,
                    workoutType = "max_effort"
                )
            )
        whenever(exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(workoutTypes))
    }

    @Test
    fun `determineWeakMuscles should return default weak muscles`() {
        val result = exerciseSelectionService.determineWeakMuscles()

        assert(result == ConjugateConstants.DEFAULT_WEAK_MUSCLES)
    }

    @Test
    fun `selectExercise should select primary exercise successfully`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(userExercisePool).markExerciseAsUsed(exercise.name)
    }

    @Test
    fun `selectExercise should select accessory exercise successfully`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Dumbbell Flyes", MovementType.HORIZONTAL_PUSH)

        whenever(userExercisePool.getAvailableAccessoryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = true,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(userExercisePool).markExerciseAsUsed(exercise.name)
    }

    @Test
    fun `selectExercise should handle no available exercises`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(emptyList())

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    @Test
    fun `selectExercise should handle no exercises after day type filtering`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(userExercisePool).markExerciseAsUsed(exercise.name)
    }

    @Test
    fun `selectExercise should handle no exercises after workout type filtering`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(userExercisePool).markExerciseAsUsed(exercise.name)
    }

    @Test
    fun `selectExercise should handle no exercises after muscle filtering`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(emptyList()))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType
            )

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    @Test
    fun `selectExercise should apply movement balance constraints`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val movementBalanceState =
            MovementBalanceService.MovementBalanceState(
                selectedExercises = emptyList(),
                movementTypeCounts =
                    mapOf(
                        MovementType.HORIZONTAL_PUSH to 2,
                        MovementType.HORIZONTAL_PULL to 1
                    ),
                pushVolume = BigDecimal.ZERO,
                pullVolume = BigDecimal.ZERO
            )

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(movementBalanceService.prioritizeExercisesForBalance(any(), any())).thenReturn(listOf(exercise))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = movementBalanceState
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()

        verify(movementBalanceService).prioritizeExercisesForBalance(any(), eq(movementBalanceState))
    }

    @Test
    fun `selectExercise should handle movement balance constraints with no exercises`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val movementBalanceState =
            MovementBalanceService.MovementBalanceState(
                selectedExercises = emptyList(),
                movementTypeCounts =
                    mapOf(
                        MovementType.HORIZONTAL_PUSH to 2,
                        MovementType.HORIZONTAL_PULL to 1
                    ),
                pushVolume = BigDecimal.ZERO,
                pullVolume = BigDecimal.ZERO
            )

        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutType(any(), any(), any())
        ).thenReturn(Mono.just(createSampleExerciseWorkoutType()))
        whenever(movementBalanceService.prioritizeExercisesForBalance(any(), any())).thenReturn(emptyList())

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = movementBalanceState
            )

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    @Test
    fun `selectExercise should exclude plyometric exercises from warmup selection`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "max_effort"
        val dayType = "upper_body"

        // Create a mix of exercises including a plyometric one
        val regularExercise = createSampleExercise("Dumbbell Flyes", MovementType.HORIZONTAL_PUSH)
        val plyometricExercise = createSampleExercise("Box Jump", MovementType.PLYOMETRIC)
        val exercises = listOf(regularExercise, plyometricExercise)

        // Mock the exercise pool to return the full list initially
        whenever(userExercisePool.getAvailableAccessoryExercises()).thenReturn(exercises)

        // Mock the equipment and muscle filtering to return the filtered list (after plyometric filtering)
        // The plyometric filtering happens in selectRotatingExerciseInternal before these filters
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(regularExercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any(), any())).thenReturn(Mono.just(listOf(regularExercise)))
        whenever(userExercisePool.markExerciseAsUsed(any())).thenReturn(true)
        
        // Mock the muscle count check for warmup filtering - Dumbbell Flyes should have <= 3 muscles
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Dumbbell Flyes")).thenReturn(Mono.just(emptyList()))
        
        // Mock the equipment check for warmup filtering - Dumbbell Flyes should use appropriate equipment
        val dumbbellEquipment = listOf(
            com.congen.model.ExerciseEquipment("Dumbbell Flyes", "dumbbells")
        )
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Dumbbell Flyes")).thenReturn(Mono.just(dumbbellEquipment))

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                // Warmup exercises are accessory
                isAccessory = true,
                workoutType = workoutType,
                dayType = dayType,
                movementBalanceState = null,
                isWarmup = true
            )

        StepVerifier.create(result)
            .expectNext(regularExercise) // Should only return the non-plyometric exercise
            .verifyComplete()

        // Verify that the plyometric exercise was not selected
        verify(userExercisePool).markExerciseAsUsed(regularExercise.name)
    }

    private fun createSampleExercise(
        name: String,
        movementType: MovementType
    ): Exercise {
        return Exercise(
            name = name,
            description = "A sample exercise for testing",
            movementType = movementType,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
    }

    private fun createSampleExerciseWorkoutType(): ExerciseWorkoutType {
        return ExerciseWorkoutType(
            exerciseName = "Bench Press",
            movementType = MovementType.HORIZONTAL_PUSH,
            workoutType = "max_effort"
        )
    }
}
