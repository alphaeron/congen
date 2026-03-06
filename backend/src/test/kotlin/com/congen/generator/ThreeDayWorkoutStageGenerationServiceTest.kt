package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
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
 * Unit tests for ThreeDayWorkoutStageGenerationService.
 */
class ThreeDayWorkoutStageGenerationServiceTest {
    private lateinit var threeDayService: ThreeDayWorkoutStageGenerationService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var userExercisePool: UserExercisePool

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        exerciseSelectionService = mock()
        weightSelectionService = mock()
        prilepinGuidelinesService = mock()
        sessionTimeCalculator = mock()
        movementBalanceService = mock()
        conjugateTemplates = mock()
        userExercisePool = mock()

        threeDayService =
            ThreeDayWorkoutStageGenerationService(
                exerciseSelectionService = exerciseSelectionService,
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                sessionTimeCalculator = sessionTimeCalculator,
                movementBalanceService = movementBalanceService,
                conjugateTemplates = conjugateTemplates
            )
    }

    @Test
    fun `generateStagesForDayType should generate stages for ME_Lower`() {
        val dayType = "ME_Lower"
        val preparedData = createSamplePreparedData()
        val primaryExercise = createSampleExercise("Squat", MovementType.SQUAT)
        val secondaryExercise = createSampleExercise("Romanian Deadlift", MovementType.HINGE)

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
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
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
                exerciseName = eq("Squat"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("315"), null)))

        // Mock weight selection for secondary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Romanian Deadlift"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("275"), null)))

        // Mock Prilepin guidelines
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any(), any())
        ).thenReturn(
            Pair(
                PrilepinGuidelines(
                    intensityRange = 0.85..0.95,
                    repsPerSetRange = 1..3,
                    totalReps = 10,
                    totalRepsRange = 8..12,
                    restSeconds = 60..120
                ),
                0.90
            )
        )

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
                isFourDayTemplate = eq(false),
                dayType = any(),
                workoutType = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        val result =
            threeDayService.generateStagesForDayType(
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
    fun `generateStagesForDayType should generate stages for ME_Upper`() {
        val dayType = "ME_Upper"
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
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
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
        ).thenReturn(
            Pair(
                PrilepinGuidelines(
                    intensityRange = 0.85..0.95,
                    repsPerSetRange = 1..3,
                    totalReps = 10,
                    totalRepsRange = 8..12,
                    restSeconds = 60..120
                ),
                0.90
            )
        )

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
                isFourDayTemplate = eq(false),
                dayType = any(),
                workoutType = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        val result =
            threeDayService.generateStagesForDayType(
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
    fun `generateStagesForDayType should generate stages for DE_Lower`() {
        val dayType = "DE_Lower"
        val preparedData = createSamplePreparedData()
        val primaryExercise = createSampleExercise("Box Squat", MovementType.SQUAT)
        val conditioningExercise = createSampleExercise("Sled Push", MovementType.PLYOMETRIC)

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

        // Mock conditioning exercise selection
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("conditioning"),
                dayType = eq(dayType),
                movementBalanceState = any(),
                isWarmup = eq(false),
                exerciseWorkoutTypeMappings = any(),
                exerciseMuscleMappings = any(),
                currentWeekNumber = any(),
                preferredDeExerciseName = anyOrNull()
            )
        ).thenReturn(Mono.just(conditioningExercise))

        // Mock weight selection for primary exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Box Squat"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(true),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("225"), null)))

        // Mock weight selection for conditioning exercise
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Sled Push"),
                intensity = any(),
                oneRepMaxes = any(),
                isDynamicEffort = eq(false),
                currentWeekNumber = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("50"), null)))

        // Mock Prilepin guidelines
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any(), any())
        ).thenReturn(
            Pair(
                PrilepinGuidelines(
                    intensityRange = 0.55..0.65,
                    repsPerSetRange = 3..6,
                    totalReps = 24,
                    totalRepsRange = 18..30,
                    restSeconds = 60..90
                ),
                0.60
            )
        )

        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(any(), any(), any())
        ).thenReturn(Pair(5, 5))

        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(any(), any(), any(), any())
        ).thenReturn(90)

        whenever(
            prilepinGuidelinesService.getRandomRestTime(any())
        ).thenReturn(75)

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
                isFourDayTemplate = eq(false),
                dayType = any(),
                workoutType = any(),
                exerciseMuscleMappings = any(),
                exerciseEquipmentMappings = any(),
                exerciseWorkoutTypeMappings = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        val result =
            threeDayService.generateStagesForDayType(
                programId = 1L,
                dayNumber = 1,
                dayType = dayType,
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
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

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool =
                UserExercisePool(
                    allExercises = createSampleExercises(),
                    preferences = emptyList(),
                    userEquipment = createSampleUserEquipment(),
                    exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                    exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                    previouslyUsedExercises = emptyList(),
                    userId = USER_ID
                ),
            oneRepMaxes =
                listOf(
                    UserOneRepMax(USER_ID, "Bench Press", BigDecimal("225"), now),
                    UserOneRepMax(USER_ID, "Squat", BigDecimal("315"), now),
                    UserOneRepMax(USER_ID, "Deadlift", BigDecimal("405"), now)
                ),
            programPreferences =
                ProgramPreferences(
                    programId = 1L,
                    programDaysPerWeek = 3,
                    sessionTimeLengthInMinutes = 60,
                    createdAt = now,
                    updatedAt = now
                ),
            weakMuscles = listOf("chest"),
            currentWeekNumber = 1,
            userId = USER_ID,
            weightUnitPreferences = mapOf("Bench Press" to WeightUnit.LBS),
            exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
            exerciseWorkoutTypeMappings =
                mapOf(
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
        )
    }
}
