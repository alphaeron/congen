package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for WorkoutStageGenerationService.
 */
class WorkoutStageGenerationServiceTest {
    private lateinit var baseService: TestWorkoutStageGenerationService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var sessionTimeCalculator: SessionTimeCalculator

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        prilepinGuidelinesService = mock()
        weightSelectionService = mock()
        exerciseSelectionService = mock()
        movementBalanceService = mock()
        sessionTimeCalculator = mock()
        
        baseService = TestWorkoutStageGenerationService(
            prilepinGuidelinesService = prilepinGuidelinesService,
            weightSelectionService = weightSelectionService,
            exerciseSelectionService = exerciseSelectionService,
            movementBalanceService = movementBalanceService,
            sessionTimeCalculator = sessionTimeCalculator
        )
    }

    @Test
    fun `generateWorkoutStages should generate stages successfully`() {
        val preparedData = createSamplePreparedData()
        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Bench Press", MovementType.HORIZONTAL_PUSH)

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        // Mock primary exercise selection (isAccessory = false, workoutType = "maximal_effort")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq("ME_Upper"),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any()
            )
        ).thenReturn(Mono.just(primaryExercise))

        // Mock secondary exercise selection
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(true),
                workoutType = eq("maximal_effort"),
                dayType = eq("ME_Upper"),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any()
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
                exerciseName = eq("Incline Bench Press"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("185"), null)))

        // Mock Prilepin guidelines
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any(), any())
        ).thenReturn(Pair(com.congen.generator.PrilepinGuidelines(
            intensityRange = 0.85..0.95,
            repsPerSetRange = 1..3,
            totalReps = 10,
            totalRepsRange = 8..12,
            restSeconds = 60..120
        ), 0.90))

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(any(), any(), any())
        ).thenReturn(Pair(3, 3))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(any(), any(), any(), any())
        ).thenReturn(120)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any())
        ).thenReturn(90)

        // Mock session time calculator
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(any(), any(), any(), any())
        ).thenReturn(2)

        // Mock warmup exercises
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = any(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        val result = baseService.generateWorkoutStages(
            programId = 1L,
            dayNumber = 1,
            dayType = "ME_Upper",
            preparedData = preparedData
        )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `generateWorkoutStages should handle empty exercise selection gracefully`() {
        val preparedData = createSamplePreparedData()

        // Mock movement balance service
        val mockMovementBalanceState = mock<MovementBalanceService.MovementBalanceState>()
        whenever(mockMovementBalanceState.addExercise(any(), any())).thenReturn(mockMovementBalanceState)
        whenever(
            movementBalanceService.createInitialState()
        ).thenReturn(mockMovementBalanceState)

        whenever(
            movementBalanceService.estimateExerciseVolume(any())
        ).thenReturn(BigDecimal("10.0"))

        whenever(
            movementBalanceService.logBalanceState(any(), any())
        ).thenAnswer { /* do nothing */ }

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any(),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any()
            )
        ).thenReturn(Mono.empty())

        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = any(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        val result = baseService.generateWorkoutStages(
            programId = 1L,
            dayNumber = 1,
            dayType = "ME_Upper",
            preparedData = preparedData
        )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }
    private fun createSampleExercise(name: String, movementType: MovementType): Exercise {
        return Exercise(
            name = name,
            description = "Sample exercise description",
            movementType = movementType,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
    }

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool = UserExercisePool(
                allExercises = createSampleExercises(),
                preferences = emptyList(),
                userEquipment = createSampleUserEquipment(),
                exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            ),
            oneRepMaxes = listOf(
                UserOneRepMax(USER_ID, "Bench Press", BigDecimal("225"), now),
                UserOneRepMax(USER_ID, "Squat", BigDecimal("315"), now),
                UserOneRepMax(USER_ID, "Deadlift", BigDecimal("405"), now)
            ),
            programPreferences = ProgramPreferences(
                programId = 1L,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = now,
                updatedAt = now
            ),
            weakMuscles = listOf("chest"),
            currentWeekNumber = 1,
            userId = USER_ID,
            weightUnitPreferences = mapOf("Bench Press" to WeightUnit.LBS),
            exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
            exerciseWorkoutTypeMappings = mapOf(
                "Bench Press" to listOf("maximal_effort", "dynamic_effort"),
                "Squat" to listOf("maximal_effort", "dynamic_effort"),
                "Deadlift" to listOf("maximal_effort")
            ),
            exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
            previouslyProgrammedExercises = emptyList(),
            allExercises = createSampleExercises(),
            userEquipment = createSampleUserEquipment(),
            userExercisePreferences = emptyList()
        )
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createSampleExercise("Squat", MovementType.SQUAT),
            createSampleExercise("Deadlift", MovementType.HINGE),
            createSampleExercise("Sled Push", MovementType.PLYOMETRIC)
        )
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return listOf(
            UserEquipment(USER_ID, "power bar", now),
            UserEquipment(USER_ID, "bench", now),
            UserEquipment(USER_ID, "squat rack", now)
        )
    }

    private fun createSampleExerciseEquipmentMappings(): Map<String, List<ExerciseEquipment>> {
        return mapOf(
            "Bench Press" to listOf(ExerciseEquipment("Bench Press", "power bar")),
            "Squat" to listOf(ExerciseEquipment("Squat", "power bar")),
            "Deadlift" to listOf(ExerciseEquipment("Deadlift", "power bar"))
        )
    }

    private fun createSampleExerciseMuscleMappings(): Map<String, List<ExerciseMuscle>> {
        return mapOf(
            "Bench Press" to listOf(
                ExerciseMuscle("Bench Press", "chest"),
                ExerciseMuscle("Bench Press", "triceps")
            ),
            "Squat" to listOf(
                ExerciseMuscle("Squat", "quadriceps"),
                ExerciseMuscle("Squat", "glutes")
            ),
            "Deadlift" to listOf(
                ExerciseMuscle("Deadlift", "hamstrings"),
                ExerciseMuscle("Deadlift", "glutes")
            )
        )
    }

    /**
     * Test implementation of WorkoutStageGenerationService for testing purposes.
     */
    private class TestWorkoutStageGenerationService(
        prilepinGuidelinesService: PrilepinGuidelinesService,
        weightSelectionService: WeightSelectionService,
        exerciseSelectionService: ExerciseSelectionService,
        movementBalanceService: MovementBalanceService,
        sessionTimeCalculator: SessionTimeCalculator,
    ) : WorkoutStageGenerationService(
        prilepinGuidelinesService,
        weightSelectionService,
        exerciseSelectionService,
        movementBalanceService,
        sessionTimeCalculator
    ) {
        override fun generateStagesForDayType(
            programId: Long,
            dayNumber: Int,
            dayType: String,
            preparedData: WorkoutGenerationPreparedData,
        ): Mono<List<WorkoutStageData>> {
            return Mono.just(emptyList())
        }
    }
}