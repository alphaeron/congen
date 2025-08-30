package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.ProgramPreferences
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the WorkoutStageGenerationOrchestrator.
 *
 * These tests verify that the orchestrator correctly coordinates the generation
 * of workout stages across different services and handles various scenarios.
 */
class WorkoutStageGenerationOrchestratorTest {
    private lateinit var orchestrator: WorkoutStageGenerationOrchestrator
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var workoutStageGenerationServiceFactory: WorkoutStageGenerationServiceFactory
    private lateinit var mockWorkoutStageGenerationService: WorkoutStageGenerationService

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
        workoutStageGenerationServiceFactory = mock()
        mockWorkoutStageGenerationService = mock()

        orchestrator =
            WorkoutStageGenerationOrchestrator(
                workoutStageDAL = workoutStageDAL,
                workoutStageTypeDAL = workoutStageTypeDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                setSchemeService = setSchemeService,
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                workoutStageGenerationServiceFactory = workoutStageGenerationServiceFactory
            )
    }

    @Test
    fun `should orchestrate workout stage generation successfully`() {
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(4))
            .thenReturn(mockWorkoutStageGenerationService)
        whenever(
            mockWorkoutStageGenerationService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )
        ).thenReturn(Mono.empty())

        val result =
            orchestrator.generateWorkoutStages(
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

        verify(workoutStageGenerationServiceFactory).getWorkoutStageGenerationService(4)
        verify(mockWorkoutStageGenerationService).generateWorkoutStages(
            workout = workout,
            dayType = dayType,
            userExercisePool = userExercisePool,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            weakMuscles = weakMuscles,
            currentWeekNumber = currentWeekNumber,
            userId = userId
        )
    }

    @Test
    fun `should handle service errors`() {
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(4))
            .thenReturn(mockWorkoutStageGenerationService)
        whenever(
            mockWorkoutStageGenerationService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )
        ).thenReturn(Mono.error(RuntimeException("Service error")))

        val result =
            orchestrator.generateWorkoutStages(
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
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle different program days per week`() {
        val workout = createSampleWorkout()
        val dayType = "dynamic_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences().copy(programDaysPerWeek = 3)
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(3))
            .thenReturn(mockWorkoutStageGenerationService)
        whenever(
            mockWorkoutStageGenerationService.generateWorkoutStages(
                workout = workout,
                dayType = dayType,
                userExercisePool = userExercisePool,
                oneRepMaxes = oneRepMaxes,
                programPreferences = programPreferences,
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )
        ).thenReturn(Mono.empty())

        val result =
            orchestrator.generateWorkoutStages(
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

        verify(workoutStageGenerationServiceFactory).getWorkoutStageGenerationService(3)
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
}
