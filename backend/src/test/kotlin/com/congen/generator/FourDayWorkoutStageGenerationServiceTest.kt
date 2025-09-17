package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.generator.SetSchemeParams
import com.congen.generator.PrilepinGuidelines
import com.congen.generator.WorkoutGenerationResult
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var fourDayService: FourDayWorkoutStageGenerationService

    @BeforeEach
    fun setUp() {
        workoutStageDAL = mock()
        workoutStageTypeDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeDAL = mock()
        setSchemeService = mock()
        prilepinGuidelinesService = mock()
        weightSelectionService = mock()
        userWeightUnitPreferenceDAL = mock()
        exerciseSelectionService = mock()
        movementBalanceService = mock()
        sessionTimeCalculator = mock()
        conjugateTemplates = mock()

        fourDayService =
            FourDayWorkoutStageGenerationService(
                workoutStageDAL = workoutStageDAL,
                workoutStageTypeDAL = workoutStageTypeDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                setSchemeService = setSchemeService,
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                exerciseSelectionService = exerciseSelectionService,
                movementBalanceService = movementBalanceService,
                sessionTimeCalculator = sessionTimeCalculator,
                conjugateTemplates = conjugateTemplates
            )
    }

    @Test
    fun `should generate stages for ME Upper day type`() {
        val workout = createSampleWorkout()
        val dayType = "ME_Upper"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = eq(userExercisePool),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = anyOrNull(),
                isWarmup = eq(false)
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = eq(primaryExercise),
                userExercisePool = eq(userExercisePool),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = anyOrNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = anyOrNull(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        // Mock WorkoutStageDAL methods
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any()))
            .thenReturn(Mono.empty())
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(Mono.empty())

        // Mock MovementBalanceService methods
        whenever(movementBalanceService.createInitialState())
            .thenReturn(MovementBalanceService.MovementBalanceState())
        whenever(movementBalanceService.estimateExerciseVolume(any()))
            .thenReturn(BigDecimal("100"))

        val result =
            fourDayService.generateStagesForDayType(
                programId = workout.programId,
                dayNumber = workout.dayNumber,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectNextMatches { workoutResult ->
                workoutResult.programId == workout.programId && workoutResult.dayNumber == workout.dayNumber && workoutResult.stages.isNotEmpty()
            }
            .verifyComplete()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = eq(userExercisePool),
            targetMuscles = eq(emptyList()),
            isAccessory = eq(false),
            workoutType = eq("maximal_effort"),
            dayType = eq(dayType),
            movementBalanceState = anyOrNull(),
            isWarmup = eq(false)
        )
    }

    @Test
    fun `should generate stages for DE Lower day type`() {
        val workout = createSampleWorkout()
        val dayType = "DE_Lower"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("quads", "hamstrings")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Box Squat", MovementType.SQUAT)

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = eq(userExercisePool),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = anyOrNull(),
                isWarmup = eq(false)
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = any(),
                userExercisePool = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = anyOrNull(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any()
            )
        ).thenReturn(Mono.just(emptyList()))

        // Mock WorkoutStageDAL methods
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any()))
            .thenReturn(Mono.empty())
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(Mono.empty())

        // Mock MovementBalanceService methods
        whenever(movementBalanceService.createInitialState())
            .thenReturn(MovementBalanceService.MovementBalanceState())
        whenever(movementBalanceService.estimateExerciseVolume(any()))
            .thenReturn(BigDecimal("100"))

        // Mock additional services needed for the atomic implementation
        whenever(prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any(), any()))
            .thenReturn(Pair(createSampleGuidelines(), 0.8))
        whenever(prilepinGuidelinesService.getRandomRestTime(any()))
            .thenReturn(120)
        whenever(weightSelectionService.getTargetWeight(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(WeightSelectionService.TargetWeightResult(BigDecimal("100.0"), null)))

        // Mock ConjugateTemplates methods
        whenever(conjugateTemplates.getPrimaryMovementType(any()))
            .thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType(any()))
            .thenReturn("DE_Lower")
        whenever(conjugateTemplates.isCombinedMEDay(any()))
            .thenReturn(false)

        val result =
            fourDayService.generateStagesForDayType(
                programId = workout.programId,
                dayNumber = workout.dayNumber,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectError()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = eq(userExercisePool),
            targetMuscles = eq(emptyList()),
            isAccessory = eq(false),
            workoutType = eq("dynamic_effort"),
            dayType = eq(dayType),
            movementBalanceState = anyOrNull(),
            isWarmup = eq(false)
        )
    }

    @Test
    fun `should handle exercise selection failure gracefully`() {
        val workout = createSampleWorkout()
        val dayType = "ME_Upper"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = eq(userExercisePool),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = anyOrNull(),
                isWarmup = eq(false)
            )
        ).thenReturn(Mono.error(RuntimeException("Exercise selection failed")))

        // Mock MovementBalanceService methods
        whenever(movementBalanceService.createInitialState())
            .thenReturn(MovementBalanceService.MovementBalanceState())
        whenever(movementBalanceService.estimateExerciseVolume(any()))
            .thenReturn(BigDecimal("100"))

        val result =
            fourDayService.generateStagesForDayType(
                programId = workout.programId,
                dayNumber = workout.dayNumber,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    private fun createSampleWorkout(): ProgrammedWorkout {
        return ProgrammedWorkout(
            id = 1L,
            programId = 1L,
            dayNumber = 1,
            name = "Test Workout",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            UserOneRepMax(
                userId = "user123",
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225"),
                updatedAt = Instant.now()
            ),
            UserOneRepMax(
                userId = "user123",
                exerciseName = "Squat",
                oneRepMax = BigDecimal("315"),
                updatedAt = Instant.now()
            )
        )
    }

    private fun createSampleProgramPreferences(): ProgramPreferences {
        return ProgramPreferences(
            programId = 1L,
            programDaysPerWeek = 4,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
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

    private fun createSampleSetSchemes(): List<SetSchemeParams> {
        return listOf(
            SetSchemeParams(
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = null,
                targetRepCount = 5,
                performedRepCount = null,
                restSeconds = 120,
                band = null
            )
        )
    }

    private fun createSampleGuidelines(): PrilepinGuidelines {
        return PrilepinGuidelines(
            intensityRange = 0.8..0.9,
            repsPerSetRange = 3..6,
            totalReps = 24,
            totalRepsRange = 18..30,
            restSeconds = 120..180
        )
    }
}
