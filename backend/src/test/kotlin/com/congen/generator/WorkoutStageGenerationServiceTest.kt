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
 * Unit tests for the WorkoutStageGenerationService base class.
 *
 * These tests verify that the base service correctly handles common functionality
 * and delegates to appropriate subclasses for specific implementations.
 */
class WorkoutStageGenerationServiceTest {
    private lateinit var baseService: TestWorkoutStageGenerationService
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

        baseService =
            TestWorkoutStageGenerationService(
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
                sessionTimeCalculator = sessionTimeCalculator
            )
    }

    @Test
    fun `should generate workout stages successfully`() {
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = eq(userExercisePool),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("maximal_effort"),
                dayType = eq(dayType),
                movementBalanceState = isNull()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
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

        val result =
            baseService.generateWorkoutStages(
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
    fun `should handle exercise selection failure`() {
        val workout = createSampleWorkout()
        val dayType = "maximal_effort"
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
                movementBalanceState = isNull()
            )
        ).thenReturn(Mono.error(RuntimeException("Exercise selection failed")))

        val result =
            baseService.generateWorkoutStages(
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
    fun `should handle dynamic effort day type`() {
        val workout = createSampleWorkout()
        val dayType = "dynamic_effort"
        val userExercisePool = mock<UserExercisePool>()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val weakMuscles = listOf("chest", "triceps")
        val currentWeekNumber = 1
        val userId = "user123"

        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)

        whenever(
            exerciseSelectionService.selectExercise(
                userExercisePool = eq(userExercisePool),
                targetMuscles = eq(emptyList()),
                isAccessory = eq(false),
                workoutType = eq("dynamic_effort"),
                dayType = eq(dayType),
                movementBalanceState = isNull()
            )
        ).thenReturn(Mono.just(primaryExercise))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
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

        val result =
            baseService.generateWorkoutStages(
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
            dayType = dayType,
            movementBalanceState = null
        )
    }

    /**
     * Test implementation of WorkoutStageGenerationService for testing base functionality.
     */
    private class TestWorkoutStageGenerationService(
        workoutStageDAL: WorkoutStageDAL,
        workoutStageTypeDAL: WorkoutStageTypeDAL,
        programmedExerciseDAL: ProgrammedExerciseDAL,
        setSchemeDAL: SetSchemeDAL,
        setSchemeService: SetSchemeService,
        prilepinGuidelinesService: PrilepinGuidelinesService,
        weightSelectionService: WeightSelectionService,
        userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
        exerciseSelectionService: ExerciseSelectionService,
        movementBalanceService: MovementBalanceService,
        sessionTimeCalculator: SessionTimeCalculator
    ) : WorkoutStageGenerationService(
            workoutStageDAL,
            workoutStageTypeDAL,
            programmedExerciseDAL,
            setSchemeDAL,
            setSchemeService,
            prilepinGuidelinesService,
            weightSelectionService,
            userWeightUnitPreferenceDAL,
            exerciseSelectionService,
            movementBalanceService,
            sessionTimeCalculator
        ) {
        override fun generateStagesForDayType(
            workout: ProgrammedWorkout,
            dayType: String,
            userExercisePool: UserExercisePool,
            oneRepMaxes: List<UserOneRepMax>,
            programPreferences: ProgramPreferences,
            weakMuscles: List<String>,
            currentWeekNumber: Int,
            userId: String,
        ): Mono<Void> {
            // Simple test implementation that selects exercises and returns success
            return exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = emptyList(),
                isAccessory = false,
                workoutType = dayType,
                dayType = dayType,
                movementBalanceState = null
            ).then(Mono.empty())
        }
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
            programDaysPerWeek = 4,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
