package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
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
import com.congen.service.SetSchemeService
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
 * Unit tests for the FourDayWorkoutStageGenerationService.
 *
 * These tests verify that the service correctly handles traditional separate
 * maximal effort and dynamic effort days for 4-day conjugate programs.
 */
class FourDayWorkoutStageGenerationServiceTest {
    private lateinit var fourDayService: FourDayWorkoutStageGenerationService
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
    private val userId = "test-user-id"

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

        fourDayService =
            FourDayWorkoutStageGenerationService(
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
    fun `generateStagesForDayType should handle ME Lower day`() {
        // Given
        val dayType = "ME_Lower"

        // Mock basic exercise selection to prevent null pointer exceptions
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any(), any()))
            .thenReturn(Mono.just(exercises))
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), any()))
            .thenReturn(exercises)
        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
        whenever(exerciseSelectionService.selectSimilarSecondaryExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(secondaryExercise))
        whenever(exerciseSelectionService.selectWarmupExercises(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(emptyList<Exercise>()))

        // Mock set scheme generation to prevent null pointer exceptions
        whenever(prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any()))
            .thenReturn(Pair(mockPrilepinGuidelines(), 0.8))
        whenever(weightSelectionService.getTargetWeight(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(mockWeightSelectionResult()))

        // Mock DAL methods to prevent null pointer exceptions
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any()))
            .thenReturn(Mono.empty())
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.empty())
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(Mono.empty())
        whenever(
            setSchemeService.insertSetScheme(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(
            fourDayService.generateWorkoutStages(
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
    fun `generateStagesForDayType should handle DE Upper day`() {
        // Given
        val dayType = "DE_Upper"

        // Mock basic exercise selection to prevent null pointer exceptions
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any(), any()))
            .thenReturn(Mono.just(exercises))
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any()))
            .thenReturn(Mono.just(exercises))
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), any()))
            .thenReturn(exercises)
        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
            .thenReturn(Mono.just(secondaryExercise))
        whenever(exerciseSelectionService.selectWarmupExercises(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(emptyList<Exercise>()))

        // Mock set scheme generation to prevent null pointer exceptions
        whenever(prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any()))
            .thenReturn(Pair(mockPrilepinGuidelines(), 0.8))
        whenever(weightSelectionService.getTargetWeight(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(mockWeightSelectionResult()))

        // Mock DAL methods to prevent null pointer exceptions
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any()))
            .thenReturn(Mono.empty())
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.empty())
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(Mono.empty())
        whenever(
            setSchemeService.insertSetScheme(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(
            fourDayService.generateWorkoutStages(
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
    fun `generateStagesForDayType should handle case where exercises are null`() {
        // Given
        val dayType = "ME_Upper"
        whenever(conjugateTemplates.hasSecondaryMovement(dayType)).thenReturn(true)
        whenever(conjugateTemplates.hasConditioning(dayType)).thenReturn(false)

        // Mock exercise selection to return empty list
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any(), eq("maximal_effort")))
            .thenReturn(Mono.just(emptyList<Exercise>()))
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), any()))
            .thenReturn(emptyList<Exercise>())
        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(primaryExercise))
        whenever(exerciseSelectionService.selectSimilarSecondaryExercise(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(secondaryExercise))
        whenever(exerciseSelectionService.selectWarmupExercises(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(emptyList<Exercise>()))

        // Mock set scheme generation to prevent null pointer exceptions
        whenever(prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(any(), any()))
            .thenReturn(Pair(mockPrilepinGuidelines(), 0.8))
        whenever(weightSelectionService.getTargetWeight(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(mockWeightSelectionResult()))

        // Mock DAL methods to prevent null pointer exceptions
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(any(), any()))
            .thenReturn(Mono.empty())
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.empty())
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(any(), any()))
            .thenReturn(Mono.empty())
        whenever(
            setSchemeService.insertSetScheme(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.empty())

        // When
        val result =
            fourDayService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                exercises = emptyList(),
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

        // Verify no stage creation was attempted since no exercises were available
        verify(workoutStageDAL, times(0)).insertWorkoutStage(any(), any(), any(), any())
    }
}
