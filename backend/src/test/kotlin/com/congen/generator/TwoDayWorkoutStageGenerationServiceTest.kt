package com.congen.generator

import com.congen.mockExercise
import com.congen.mockPrilepinGuidelines
import com.congen.mockProgrammedWorkout
import com.congen.mockUserEquipment
import com.congen.mockUserExercisePreference
import com.congen.mockUserOneRepMax
import com.congen.mockUserProgramPreferences
import com.congen.mockWeightSelectionResult
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for the TwoDayWorkoutStageGenerationService.
 *
 * These tests verify that the service correctly handles combined ME+DE days
 * for 2-day conjugate programs.
 */
class TwoDayWorkoutStageGenerationServiceTest {
    private lateinit var twoDayService: TwoDayWorkoutStageGenerationService
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var workoutStageDAL: com.congen.dal.WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: com.congen.dal.WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: com.congen.dal.ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: com.congen.dal.SetSchemeDAL
    private lateinit var setSchemeService: com.congen.service.SetSchemeService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var userWeightUnitPreferenceDAL: com.congen.dal.UserWeightUnitPreferenceDAL
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var conjugateTemplates: ConjugateTemplates

    private val workout = mockProgrammedWorkout(id = 1L, name = "Test Workout")
    private val primaryExercise = mockExercise(name = "Primary Exercise")
    private val secondaryExercise = mockExercise(name = "Secondary Exercise")
    private val exercises = listOf(primaryExercise, secondaryExercise)
    private val preferences = listOf(mockUserExercisePreference())
    private val userEquipment = listOf(mockUserEquipment())
    private val oneRepMaxes = listOf(mockUserOneRepMax())
    private val programPreferences = mockUserProgramPreferences()
    private val rotationHistory = listOf<ExerciseRotationHistory>()
    private val weakMuscles = listOf("hamstrings", "glutes")
    private val currentWeekNumber = 1
    private val userId = 1

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
        conjugateTemplates = mock()

        twoDayService =
            TwoDayWorkoutStageGenerationService(
                exerciseSelectionService,
                workoutStageDAL,
                workoutStageTypeDAL,
                programmedExerciseDAL,
                setSchemeDAL,
                setSchemeService,
                prilepinGuidelinesService,
                weightSelectionService,
                userWeightUnitPreferenceDAL,
                sessionTimeCalculator,
                MovementBalanceService(),
                conjugateTemplates
            )
    }

    @Test
    fun `generateStagesForDayType should handle combined ME+DE day`() {
        // Given
        val dayType = "ME_Upper_DE_Lower"

        // When & Then
        StepVerifier.create(
            twoDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                rotationHistory = rotationHistory,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )
        )
            .expectComplete()
            .verify()
    }

    @Test
    fun `generateStagesForDayType should return empty for non-combined ME day`() {
        // Given
        val dayType = "ME_Upper" // Not a combined ME day
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(false)

        // When
        val result =
            twoDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                rotationHistory = rotationHistory,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Verify no stage creation was attempted
        verify(workoutStageDAL, times(0)).insertWorkoutStage(any(), any(), any(), any())
    }

    @Test
    fun `generateStagesForDayType should handle case where exercises are null`() {
        // Given
        val dayType = "ME_Upper_DE_Lower"
        whenever(conjugateTemplates.isCombinedMEDay(dayType)).thenReturn(true)
        whenever(conjugateTemplates.getPrimaryMovementType(dayType)).thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType(dayType)).thenReturn("DE_Lower")

        // Mock exercise selection to return null
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any(), eq("maximal_effort")))
            .thenReturn(Mono.just(exercises))
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), eq(false)))
            .thenReturn(exercises)
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any()))
            .thenReturn(Mono.just(exercises))
        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
            .thenReturn(Mono.empty())
        whenever(exerciseSelectionService.selectWarmupExercises(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(emptyList<Exercise>()))

        // Mock set scheme generation to return empty lists
        whenever(prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any()))
            .thenReturn(Pair(mockPrilepinGuidelines(), 0.8))
        whenever(weightSelectionService.getTargetWeight(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(mockWeightSelectionResult()))

        // Mock time calculation
        whenever(sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(any(), any(), any(), any()))
            .thenReturn(0)

        // When
        val result =
            twoDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                rotationHistory = rotationHistory,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Verify no stage creation was attempted since no exercises were selected
        verify(workoutStageDAL, times(0)).insertWorkoutStage(any(), any(), any(), any())
    }
}
