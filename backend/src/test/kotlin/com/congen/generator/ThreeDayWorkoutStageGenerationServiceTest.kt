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
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the ThreeDayWorkoutStageGenerationService.
 *
 * These tests verify that the service correctly generates workout stages
 * for three-day conjugate programs, including proper exercise selection,
 * stage creation, and error handling.
 */
class ThreeDayWorkoutStageGenerationServiceTest {
    private lateinit var threeDayService: ThreeDayWorkoutStageGenerationService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var conjugateTemplates: ConjugateTemplates

    @BeforeEach
    fun setUp() {
        exerciseSelectionService = mock()
        workoutStageDAL = mock()
        workoutStageTypeDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeDAL = mock()
        setSchemeService = mock()
        prilepinGuidelinesService = mock()
        weightSelectionService = mock()
        userWeightUnitPreferenceDAL = mock()
        sessionTimeCalculator = mock()
        movementBalanceService = mock()
        conjugateTemplates = mock()

        threeDayService =
            ThreeDayWorkoutStageGenerationService(
                exerciseSelectionService = exerciseSelectionService,
                workoutStageDAL = workoutStageDAL,
                workoutStageTypeDAL = workoutStageTypeDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                setSchemeService = setSchemeService,
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                sessionTimeCalculator = sessionTimeCalculator,
                movementBalanceService = movementBalanceService,
                conjugateTemplates = conjugateTemplates
            )
    }

    @Test
    fun `should generate workout stages for maximal effort day`() {
        val workout = createSampleWorkout()
        val dayType = "ME_Lower_DE_Upper"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        // Mock Prilepin guidelines service methods
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("ME_Lower"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("primary")
            )
        ).thenReturn(Pair(mock(), 0.8))
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("DE_Upper"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("secondary")
            )
        ).thenReturn(Pair(mock(), 0.6))
        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(5, 3))
        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(90)

        // Mock weight selection service
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                userId = any(),
                isDynamicEffort = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(mock()))

        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE(dayType)).thenReturn(false)
        whenever(conjugateTemplates.getPrimaryMovementType(dayType)).thenReturn("ME_Lower")
        whenever(conjugateTemplates.getSecondaryMovementType(dayType)).thenReturn("DE_Upper")
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(weakMuscles),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        // Mock the selectConditioningExercise call
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(listOf("triceps")),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = any(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any()
            )
        ).thenReturn(Mono.just(emptyList()))
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE(dayType)).thenReturn(false)
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any())).thenReturn(Mono.empty())

        val result =
            threeDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            isAccessory = false,
            workoutType = "maximal_effort",
            dayType = dayType,
            movementBalanceState = null
        )
    }

    @Test
    fun `should generate workout stages for dynamic effort day`() {
        val workout = createSampleWorkout()
        val dayType = "DE_Full_Body"
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
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = eq("dynamic_effort"),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = eq(primaryExercise),
                userExercisePool = any(),
                workoutType = eq("dynamic_effort"),
                dayType = any(),
                movementBalanceState = isNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = any(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any()
            )
        ).thenReturn(Mono.just(emptyList()))
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(false)
        whenever(conjugateTemplates.isFullBodyDE(dayType)).thenReturn(true)
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = eq("dynamic_effort"),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = eq("dynamic_effort"),
                dayType = eq("DE_Upper"),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = eq("dynamic_effort"),
                dayType = eq("DE_Lower"),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any())).thenReturn(Mono.empty())

        val result =
            threeDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            isAccessory = false,
            workoutType = "dynamic_effort",
            dayType = "DE_Upper",
            movementBalanceState = null
        )
    }

    @Test
    fun `should handle exercise selection failure`() {
        val workout = createSampleWorkout()
        val dayType = "ME_Upper_DE_Lower"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.error(RuntimeException("Exercise selection failed")))
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE(dayType)).thenReturn(false)
        whenever(conjugateTemplates.getPrimaryMovementType(dayType)).thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType(dayType)).thenReturn("DE_Lower")

        // Mock Prilepin guidelines service methods
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("ME_Upper"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("primary")
            )
        ).thenReturn(Pair(mock(), 0.8))
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("DE_Lower"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("secondary")
            )
        ).thenReturn(Pair(mock(), 0.6))
        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(5, 3))
        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(90)

        // Mock weight selection service
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                userId = any(),
                isDynamicEffort = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(mock()))

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.error(RuntimeException("Exercise selection failed")))
        // Mock the selectConditioningExercise call
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(listOf("triceps")),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any()
            )
        ).thenReturn(Mono.error(RuntimeException("Exercise selection failed")))
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any())).thenReturn(Mono.empty())

        val result =
            threeDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .verifyError(RuntimeException::class.java)
    }

    @Test
    fun `should generate workout stages for repetition effort day`() {
        val workout = createSampleWorkout()
        val dayType = "ME_Lower_DE_Upper"
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
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = any(),
                isAccessory = any(),
                workoutType = any(),
                dayType = eq("ME_Lower"),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = eq(primaryExercise),
                userExercisePool = any(),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = any(),
                primaryExercise = any(),
                secondaryExercise = any(),
                isFourDayTemplate = any(),
                dayType = any(),
                workoutType = any()
            )
        ).thenReturn(Mono.just(emptyList()))
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE(dayType)).thenReturn(false)
        whenever(conjugateTemplates.getPrimaryMovementType(dayType)).thenReturn("ME_Lower")
        whenever(conjugateTemplates.getSecondaryMovementType(dayType)).thenReturn("DE_Upper")

        // Mock Prilepin guidelines service methods
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("ME_Lower"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("primary")
            )
        ).thenReturn(Pair(mock(), 0.8))
        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = eq("DE_Upper"),
                currentWeekNumber = eq(currentWeekNumber),
                movementRole = eq("secondary")
            )
        ).thenReturn(Pair(mock(), 0.6))
        whenever(
            prilepinGuidelinesService.getRepsAndSetsBasedOnIntensity(
                guidelines = any(),
                intensity = any(),
                movementRole = any()
            )
        ).thenReturn(Pair(5, 3))
        whenever(
            prilepinGuidelinesService.getRestTimeBasedOnIntensity(
                restRange = any(),
                intensity = any(),
                totalReps = any(),
                totalRepsRange = any()
            )
        ).thenReturn(90)

        // Mock weight selection service
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Bench Press"),
                intensity = eq(0.8),
                oneRepMaxes = eq(oneRepMaxes),
                userId = eq(userId),
                isDynamicEffort = eq(false),
                currentWeekNumber = eq(currentWeekNumber)
            )
        ).thenReturn(Mono.just(mock()))
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = eq("Incline Press"),
                intensity = eq(0.6),
                oneRepMaxes = eq(oneRepMaxes),
                userId = eq(userId),
                isDynamicEffort = eq(false),
                currentWeekNumber = eq(currentWeekNumber)
            )
        ).thenReturn(Mono.just(mock()))
        // Add a generic mock for any other calls
        whenever(
            weightSelectionService.getTargetWeight(
                exerciseName = any(),
                intensity = any(),
                oneRepMaxes = any(),
                userId = any(),
                isDynamicEffort = any(),
                currentWeekNumber = any()
            )
        ).thenReturn(Mono.just(mock()))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(weakMuscles),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = any(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))
        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = any(),
                targetMuscles = eq(weakMuscles),
                isAccessory = eq(true),
                workoutType = any(),
                dayType = any(),
                movementBalanceState = isNull(),
                isWarmup = any()
            )
        ).thenReturn(Mono.just(secondaryExercise))

        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any())).thenReturn(Mono.empty())

        val result =
            threeDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = emptyList(),
            isAccessory = false,
            workoutType = "maximal_effort",
            dayType = dayType,
            movementBalanceState = null
        )
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
            programDaysPerWeek = 3,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
