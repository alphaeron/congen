package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the TwoDayWorkoutStageGenerationService.
 *
 * These tests verify that the service correctly generates workout stages
 * for two-day conjugate programs, including proper exercise selection,
 * stage creation, and error handling.
 */
class TwoDayWorkoutStageGenerationServiceTest {
    private lateinit var twoDayService: TwoDayWorkoutStageGenerationService
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

        twoDayService = TwoDayWorkoutStageGenerationService(
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
        // Given
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        whenever(exerciseSelectionService.selectExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
        whenever(exerciseSelectionService.selectSimilarSecondaryExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(secondaryExercise))

        // When
        val result = twoDayService.generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )

        // Then
        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = false,
            workoutType = "maximal_effort",
            dayType = dayType,
            movementBalanceState = null
        )
    }

    @Test
    fun `should generate workout stages for dynamic effort day`() {
        // Given
        val workout = createSampleWorkout()
        val dayType = "dynamic_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        whenever(exerciseSelectionService.selectExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
        whenever(exerciseSelectionService.selectSimilarSecondaryExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(secondaryExercise))

        // When
        val result = twoDayService.generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )

        // Then
        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = false,
            workoutType = "dynamic_effort",
            dayType = dayType,
            movementBalanceState = null
        )
    }

    @Test
    fun `should handle exercise selection failure`() {
        // Given
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(exerciseSelectionService.selectExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(RuntimeException("Exercise selection failed")))

        // When
        val result = twoDayService.generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should generate workout stages for repetition effort day`() {
        // Given
        val workout = createSampleWorkout()
        val dayType = "repetition_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val secondaryExercise = createSampleExercise("Incline Press", MovementType.HORIZONTAL_PUSH)

        whenever(exerciseSelectionService.selectExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
        whenever(exerciseSelectionService.selectSimilarSecondaryExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(secondaryExercise))

        // When
        val result = twoDayService.generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )

        // Then
        StepVerifier.create(result)
            .expectComplete()
            .verify()

        verify(exerciseSelectionService).selectExercise(
            userExercisePool = userExercisePool,
            targetMuscles = weakMuscles,
            isAccessory = false,
            workoutType = "repetition_effort",
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

    private fun createSampleExercise(name: String, movementType: MovementType): Exercise {
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

    private fun createSampleProgramPreferences(): UserProgramPreferences {
        return UserProgramPreferences(
            userId = "user123",
            programDaysPerWeek = 2,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
