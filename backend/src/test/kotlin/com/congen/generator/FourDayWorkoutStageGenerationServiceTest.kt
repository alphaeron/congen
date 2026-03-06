package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the FourDayWorkoutStageGenerationService.
 *
 * These tests verify that the service correctly generates workout stages
 * for 4-day conjugate powerlifting programs, including proper exercise
 * selection, set scheme generation, and stage creation.
 */
class FourDayWorkoutStageGenerationServiceTest {
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var fourDayService: FourDayWorkoutStageGenerationService

    companion object {
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        prilepinGuidelinesService = mock()
        weightSelectionService = mock()
        exerciseSelectionService = mock()
        movementBalanceService = mock()
        sessionTimeCalculator = mock()
        conjugateTemplates = mock()

        fourDayService =
            FourDayWorkoutStageGenerationService(
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                exerciseSelectionService = exerciseSelectionService,
                movementBalanceService = movementBalanceService,
                sessionTimeCalculator = sessionTimeCalculator,
                conjugateTemplates = conjugateTemplates
            )
    }

    @Test
    fun `should generate stages for ME Upper day type`() {
        val dayType = "ME_Upper"
        val preparedData = createSamplePreparedData()

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        // Mock primary exercise selection (isAccessory = false, workoutType = "maximal_effort")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(primaryExercise))

        // Mock secondary exercise selection
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = eq(primaryExercise),
                userExercisePool = any(),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                exerciseMuscleMappings = any(),
                movementBalanceState = any(),
                exerciseWorkoutTypeMappings = any(),
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock accessory exercise selection
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock weight selection for primary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Bench Press"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("225"), null)))

        // Mock weight selection for secondary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Incline Press"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("185"), null)))

        // Mock weight selection for accessory exercises
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("135"), null)))

        // Mock Prilepin guidelines service
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq(dayType),
                currentWeekNumber = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(PrilepinGuidelines(0.8..0.9, 1..3, 15, 10..20, 60..120), 0.85))

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(3, 5))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(120)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any<IntRange>())
        ).thenReturn(90)

        // Mock session time calculator
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = any(),
                primarySetSchemes = any(),
                secondarySetSchemes = any(),
                dayType = any()
            )
        ).thenReturn(2)

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        // Mock conjugate templates
        whenever(
            conjugateTemplates.hasSecondaryMovement(dayType)
        ).thenReturn(true)

        // Mock warmup exercise selection
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = eq(primaryExercise),
                secondaryExercise = eq(secondaryExercise),
                isFourDayTemplate = eq(true),
                dayType = eq(dayType),
                workoutType = eq("maximal_effort"),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(listOf(primaryExercise)))

        val result =
            fourDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should generate stages for DE Lower day type`() {
        val dayType = "DE_Lower"
        val preparedData = createSamplePreparedData()

        val primaryExercise = createSampleExercise("Squat", MovementType.SQUAT)
        val secondaryExercise = createSampleExercise("Romanian Deadlift", MovementType.HINGE)

        // Mock primary exercise selection (isAccessory = false, workoutType = "dynamic_effort")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(primaryExercise))

        // Mock accessory exercise selection
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock weight selection for primary exercise (DE)
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Squat"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(true),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("315"), null)))

        // Mock weight selection for accessory exercises
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("275"), null)))

        // Mock Prilepin guidelines service
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq(dayType),
                currentWeekNumber = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(PrilepinGuidelines(0.8..0.9, 1..3, 15, 10..20, 60..120), 0.75))

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(2, 8))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(90)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any<IntRange>())
        ).thenReturn(90)

        // Mock session time calculator
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = any(),
                primarySetSchemes = any(),
                secondarySetSchemes = any(),
                dayType = any()
            )
        ).thenReturn(2)

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        // Mock conjugate templates (DE_Lower has no secondary movement)
        whenever(
            conjugateTemplates.hasSecondaryMovement(dayType)
        ).thenReturn(false)

        // Mock warmup exercise selection
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = eq(primaryExercise),
                secondaryExercise = eq(null),
                isFourDayTemplate = eq(true),
                dayType = eq(dayType),
                workoutType = eq("dynamic_effort"),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(listOf(primaryExercise)))

        val result =
            fourDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should generate stages for ME Lower day type`() {
        val dayType = "ME_Lower"
        val preparedData = createSamplePreparedData()

        val primaryExercise = createSampleExercise("Deadlift", MovementType.HINGE)
        val secondaryExercise = createSampleExercise("Front Squat", MovementType.SQUAT)

        // Mock primary exercise selection (isAccessory = false, workoutType = "maximal_effort")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(primaryExercise))

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock weight selection for primary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Deadlift"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("405"), null)))

        // Mock weight selection for accessory exercises
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("225"), null)))

        // Mock Prilepin guidelines service
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq(dayType),
                currentWeekNumber = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(PrilepinGuidelines(0.8..0.9, 1..3, 15, 10..20, 60..120), 0.85))

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(3, 5))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(120)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any<IntRange>())
        ).thenReturn(90)

        // Mock session time calculator
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = any(),
                primarySetSchemes = any(),
                secondarySetSchemes = any(),
                dayType = any()
            )
        ).thenReturn(2)

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        // Mock conjugate templates (ME_Lower has no secondary movement)
        whenever(
            conjugateTemplates.hasSecondaryMovement(dayType)
        ).thenReturn(false)

        // Mock warmup exercise selection
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = eq(primaryExercise),
                secondaryExercise = eq(null),
                isFourDayTemplate = eq(true),
                dayType = eq(dayType),
                workoutType = eq("maximal_effort"),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(listOf(primaryExercise)))

        val result =
            fourDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should generate stages for DE Upper day type`() {
        val dayType = "DE_Upper"
        val preparedData = createSamplePreparedData()

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Dumbbell Rows", MovementType.HORIZONTAL_PULL)

        // Mock primary exercise selection (isAccessory = false, workoutType = "dynamic_effort")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(primaryExercise))

        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = eq(primaryExercise),
                userExercisePool = any(),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                exerciseMuscleMappings = any(),
                movementBalanceState = any(),
                exerciseWorkoutTypeMappings = any(),
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock accessory exercise selection
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))

        // Mock weight selection for primary exercise (DE)
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Bench Press"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(true),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("185"), null)))

        // Mock weight selection for secondary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Dumbbell Rows"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("135"), null)))

        // Mock weight selection for accessory exercises
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("135"), null)))

        // Mock Prilepin guidelines service
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq(dayType),
                currentWeekNumber = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(PrilepinGuidelines(0.8..0.9, 1..3, 15, 10..20, 60..120), 0.75))

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(2, 8))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(90)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any<IntRange>())
        ).thenReturn(90)

        // Mock session time calculator
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                sessionTimeMinutes = any(),
                primarySetSchemes = any(),
                secondarySetSchemes = any(),
                dayType = any()
            )
        ).thenReturn(2)

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        // Mock conjugate templates (DE_Upper has secondary movement)
        whenever(
            conjugateTemplates.hasSecondaryMovement(dayType)
        ).thenReturn(true)

        // Mock warmup exercise selection
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = eq(primaryExercise),
                secondaryExercise = eq(secondaryExercise),
                isFourDayTemplate = eq(true),
                dayType = eq(dayType),
                workoutType = eq("dynamic_effort"),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(listOf(primaryExercise)))

        val result =
            fourDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should handle empty exercise selection gracefully`() {
        val dayType = "ME_Upper"
        val preparedData = createSamplePreparedData()

        // Mock primary exercise selection to return empty (no exercise found)
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.empty())

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        // Mock conjugate templates
        whenever(
            conjugateTemplates.hasSecondaryMovement(dayType)
        ).thenReturn(true)

        val result =
            fourDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextMatches { stages ->
                // When no primary exercise is found, should return fallback stages (warmup + accessory)
                stages.size >= 2 &&
                    stages.any { it.stageType == WorkoutStageTypeEnum.WARMUP } &&
                    stages.any { it.stageType == WorkoutStageTypeEnum.ACCESSORY }
            }
            .verifyComplete()
    }

    private fun createSampleExercise(
        name: String,
        movementType: MovementType
    ): Exercise {
        return Exercise(
            name = name,
            description = "Sample exercise description",
            movementType = movementType,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            UserOneRepMax("user123", "Bench Press", BigDecimal("225"), now),
            UserOneRepMax("user123", "Squat", BigDecimal("315"), now),
            UserOneRepMax("user123", "Deadlift", BigDecimal("405"), now)
        )
    }

    private fun createSampleProgramPreferences(): ProgramPreferences {
        return ProgramPreferences(
            programId = 1L,
            programDaysPerWeek = 4,
            sessionTimeLengthInMinutes = 60,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool = mock(),
            oneRepMaxes = createSampleOneRepMaxes(),
            programPreferences = createSampleProgramPreferences(),
            weakMuscles = emptyList(),
            currentWeekNumber = 1,
            userId = "user123",
            weightUnitPreferences =
                mapOf(
                    "Bench Press" to WeightUnit.LBS,
                    "Squat" to WeightUnit.LBS,
                    "Deadlift" to WeightUnit.LBS
                ),
            exerciseMuscleMappings =
                mapOf(
                    "Bench Press" to
                        listOf(
                            ExerciseMuscle("Bench Press", "chest"),
                            ExerciseMuscle("Bench Press", "triceps")
                        ),
                    "Squat" to
                        listOf(
                            ExerciseMuscle("Squat", "quadriceps"),
                            ExerciseMuscle("Squat", "glutes")
                        ),
                    "Deadlift" to
                        listOf(
                            ExerciseMuscle("Deadlift", "hamstrings"),
                            ExerciseMuscle("Deadlift", "glutes")
                        )
                ),
            exerciseEquipmentMappings =
                mapOf(
                    "Bench Press" to listOf(ExerciseEquipment("Bench Press", "power bar")),
                    "Squat" to listOf(ExerciseEquipment("Squat", "power bar")),
                    "Deadlift" to listOf(ExerciseEquipment("Deadlift", "power bar"))
                ),
            exerciseWorkoutTypeMappings =
                mapOf(
                    "Bench Press" to listOf("maximal_effort", "dynamic_effort"),
                    "Squat" to listOf("maximal_effort", "dynamic_effort"),
                    "Deadlift" to listOf("maximal_effort")
                ),
            previouslyProgrammedExercises = emptyList(),
            allExercises = listOf(createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)),
            userEquipment = emptyList(),
            userExercisePreferences = emptyList()
        )
    }
}
