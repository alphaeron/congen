package com.congen.generator

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
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
 * Unit tests for WorkoutStageGenerationService.
 *
 * Tests the common functionality provided by the base WorkoutStageGenerationService
 * class, including stage creation patterns and exercise selection logic.
 */
class WorkoutStageGenerationServiceTest {
    private lateinit var workoutStageGenerationService: TestWorkoutStageGenerationService
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

    private lateinit var workout: ProgrammedWorkout
    private lateinit var exercises: List<Exercise>
    private lateinit var preferences: List<UserExercisePreference>
    private lateinit var userEquipment: List<UserEquipment>
    private lateinit var oneRepMaxes: List<UserOneRepMax>
    private lateinit var weakMuscles: List<String>
    private lateinit var setSchemeParams: List<SetSchemeParams>
    private var currentWeekNumber: Int = 0
    private var userId: Int = 0

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

        workoutStageGenerationService =
            TestWorkoutStageGenerationService(
                exerciseSelectionService,
                workoutStageDAL,
                workoutStageTypeDAL,
                programmedExerciseDAL,
                setSchemeDAL,
                setSchemeService,
                prilepinGuidelinesService,
                weightSelectionService,
                userWeightUnitPreferenceDAL,
                sessionTimeCalculator
            )

        // Setup test data
        workout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Test Workout",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        val exercise =
            Exercise(
                name = "Bench Press",
                description = "A compound exercise for chest",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )
        exercises = listOf(exercise)

        preferences = listOf(mock())
        userEquipment = listOf(mock())
        oneRepMaxes = listOf(mock())
        weakMuscles = listOf("chest", "back")
        setSchemeParams = listOf(mockSetSchemeParams())
        currentWeekNumber = 1
        userId = 1
    }

    @Test
    fun `createPrimaryStage should create workout stage and programmed exercise`() {
        // Given
        val exercise = exercises.first()
        val primaryStage =
            WorkoutStage(
                id = 1,
                programmedWorkoutId = 1L,
                // PRIMARY position
                stageTypeId = 2,
                position = WorkoutStageTypeEnum.PRIMARY.position,
                name = "Primary",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        val primaryProgrammedExercise =
            ProgrammedExercise(
                id = 1,
                workoutStageId = 1,
                exerciseName = exercise.name,
                position = 1,
                notes = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        val workoutStageType =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = Instant.now()
            )

        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(eq(1L), eq(WorkoutStageTypeEnum.PRIMARY.position)))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(WorkoutStageTypeEnum.PRIMARY)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(1L), eq(2), eq(WorkoutStageTypeEnum.PRIMARY.position), eq("Primary")))
            .thenReturn(Mono.just(primaryStage))

        whenever(programmedExerciseDAL.insertProgrammedExercise(eq(1L), eq(exercise.name), eq(1), eq(null)))
            .thenReturn(Mono.just(primaryProgrammedExercise))

        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(eq(1L)))
            .thenReturn(Mono.just(emptyList()))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), eq(exercise.name)))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // Create a mock SetScheme object
        val mockSetScheme =
            com.congen.model.SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
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
                restSeconds = 180,
                band = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        // Mock the DAL methods that setSchemeService.createSetScheme calls internally
        whenever(
            setSchemeDAL.insertSetScheme(
                // programmedExerciseId: Long
                any(),
                // setNumber: Int
                any(),
                // isAmrap: Boolean
                any(),
                // isEmom: Boolean
                any(),
                // useTempo: Boolean
                any(),
                // eccentricTempo: String?
                anyOrNull(),
                // isometricTempo: String?
                anyOrNull(),
                // concentricTempo: String?
                anyOrNull(),
                // targetWeight: BigDecimal?
                anyOrNull(),
                // performedWeight: BigDecimal?
                anyOrNull(),
                // targetRepCount: Int?
                anyOrNull(),
                // performedRepCount: Int?
                anyOrNull(),
                // restSeconds: Int?
                anyOrNull(),
                // band: Band?
                anyOrNull()
            )
        ).thenReturn(Mono.just(mockSetScheme))

        // Also mock the selectSetSchemesByProgrammedExerciseId method that might be called
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(any()))
            .thenReturn(Mono.just(emptyList()))

        // Mock the setSchemeService.createSetScheme method
        whenever(
            setSchemeService.createSetScheme(
                // programmedExerciseId: Long
                any(),
                // setNumber: Int
                any(),
                // isAmrap: Boolean
                any(),
                // isEmom: Boolean
                any(),
                // useTempo: Boolean
                any(),
                // eccentricTempo: String?
                anyOrNull(),
                // isometricTempo: String?
                anyOrNull(),
                // concentricTempo: String?
                anyOrNull(),
                // targetWeight: String?
                anyOrNull(),
                // performedWeight: String?
                anyOrNull(),
                // targetRepCount: Int?
                anyOrNull(),
                // performedRepCount: Int?
                anyOrNull(),
                // restSeconds: Int?
                anyOrNull(),
                // unit: String?
                anyOrNull(),
                // band: Band?
                anyOrNull()
            )
        ).thenReturn(Mono.just(mockSetScheme))

        // When
        val result =
            workoutStageGenerationService.testCreatePrimaryStage(
                workout = workout,
                exercise = exercise,
                setSchemes = setSchemeParams,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStageByWorkoutIdAndPosition(
            eq(1L),
            eq(WorkoutStageTypeEnum.PRIMARY.position)
        )
        verify(workoutStageTypeDAL).selectWorkoutStageTypeByEnum(
            eq(WorkoutStageTypeEnum.PRIMARY)
        )
        verify(workoutStageDAL).insertWorkoutStage(
            eq(1L),
            eq(2),
            eq(WorkoutStageTypeEnum.PRIMARY.position),
            eq("Primary")
        )
        verify(programmedExerciseDAL).insertProgrammedExercise(
            eq(1L),
            eq(exercise.name),
            eq(1),
            eq(null)
        )
        verify(setSchemeService).createSetScheme(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `createSecondaryStage should create workout stage and programmed exercise`() {
        // Given
        val exercise = exercises.first()
        val workoutStageType =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.SECONDARY,
                createdAt = Instant.now()
            )
        val secondaryStage =
            WorkoutStage(
                id = 2,
                programmedWorkoutId = 1L,
                // SECONDARY position
                stageTypeId = 3,
                position = WorkoutStageTypeEnum.SECONDARY.position,
                name = "Secondary",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        val secondaryProgrammedExercise =
            ProgrammedExercise(
                id = 2,
                workoutStageId = 2,
                exerciseName = exercise.name,
                position = 1,
                notes = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(
            workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(
                eq(1L),
                eq(WorkoutStageTypeEnum.SECONDARY.position)
            )
        )
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        whenever(
            workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(WorkoutStageTypeEnum.SECONDARY))
        )
            .thenReturn(Mono.just(workoutStageType))
        whenever(
            workoutStageDAL.insertWorkoutStage(
                eq(1L),
                eq(2),
                eq(WorkoutStageTypeEnum.SECONDARY.position),
                eq("Secondary")
            )
        )
            .thenReturn(Mono.just(secondaryStage))
        whenever(
            programmedExerciseDAL.insertProgrammedExercise(
                eq(2L),
                eq(exercise.name),
                eq(1),
                eq(null)
            )
        )
            .thenReturn(Mono.just(secondaryProgrammedExercise))
        whenever(
            setSchemeDAL.selectSetSchemesByProgrammedExerciseId(eq(2L))
        )
            .thenReturn(Mono.just(emptyList()))
        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), eq(exercise.name))
        )
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // Create a mock SetScheme object
        val mockSetScheme =
            com.congen.model.SetScheme(
                id = 2L,
                programmedExerciseId = 2L,
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
                restSeconds = 180,
                band = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        // Mock the DAL methods that setSchemeService.createSetScheme calls internally
        whenever(
            setSchemeDAL.insertSetScheme(
                // programmedExerciseId: Long
                any(),
                // setNumber: Int
                any(),
                // isAmrap: Boolean
                any(),
                // isEmom: Boolean
                any(),
                // useTempo: Boolean
                any(),
                // eccentricTempo: String?
                anyOrNull(),
                // isometricTempo: String?
                anyOrNull(),
                // concentricTempo: String?
                anyOrNull(),
                // targetWeight: BigDecimal?
                anyOrNull(),
                // performedWeight: BigDecimal?
                anyOrNull(),
                // targetRepCount: Int?
                anyOrNull(),
                // performedRepCount: Int?
                anyOrNull(),
                // restSeconds: Int?
                anyOrNull(),
                // band: Band?
                anyOrNull()
            )
        ).thenReturn(Mono.just(mockSetScheme))

        // Also mock the selectSetSchemesByProgrammedExerciseId method that might be called
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(any()))
            .thenReturn(Mono.just(emptyList()))

        // Mock the setSchemeService.createSetScheme method
        whenever(
            setSchemeService.createSetScheme(
                // programmedExerciseId: Long
                any(),
                // setNumber: Int
                any(),
                // isAmrap: Boolean
                any(),
                // isEmom: Boolean
                any(),
                // useTempo: Boolean
                any(),
                // eccentricTempo: String?
                anyOrNull(),
                // isometricTempo: String?
                anyOrNull(),
                // concentricTempo: String?
                anyOrNull(),
                // targetWeight: String?
                anyOrNull(),
                // performedWeight: String?
                anyOrNull(),
                // targetRepCount: Int?
                anyOrNull(),
                // performedRepCount: Int?
                anyOrNull(),
                // restSeconds: Int?
                anyOrNull(),
                // unit: String?
                anyOrNull(),
                // band: Band?
                anyOrNull()
            )
        ).thenReturn(Mono.just(mockSetScheme))

        // When
        val result =
            workoutStageGenerationService.testCreateSecondaryStage(
                workout = workout,
                exercise = exercise,
                setSchemes = setSchemeParams,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStageByWorkoutIdAndPosition(
            eq(1L),
            eq(WorkoutStageTypeEnum.SECONDARY.position)
        )
        verify(workoutStageTypeDAL).selectWorkoutStageTypeByEnum(
            eq(WorkoutStageTypeEnum.SECONDARY)
        )
        verify(workoutStageDAL).insertWorkoutStage(
            eq(1L),
            eq(2),
            eq(WorkoutStageTypeEnum.SECONDARY.position),
            eq("Secondary")
        )
        verify(programmedExerciseDAL).insertProgrammedExercise(
            eq(2L),
            eq(exercise.name),
            eq(1),
            eq(null)
        )
        verify(setSchemeService).createSetScheme(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `createAccessoryStage should return empty when numAccessoryExercises is zero`() {
        // When
        val result =
            workoutStageGenerationService.testCreateAccessoryStage(
                workout = workout,
                numAccessoryExercises = 0,
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                dayType = "ME_Upper",
                weakMuscles = weakMuscles,
                currentWeekNumber = currentWeekNumber,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Verify no stage creation was attempted
        verify(workoutStageDAL, org.mockito.kotlin.times(0)).insertWorkoutStage(any(), any(), any(), any())
    }

    @Test
    fun `createConditioningStage should return empty when day type does not have conditioning`() {
        // When
        val result =
            workoutStageGenerationService.testCreateConditioningStage(
                workout = workout,
                // No conditioning for ME days
                dayType = "ME_Upper",
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                oneRepMaxes = oneRepMaxes,
                weakMuscles = weakMuscles,
                userId = userId
            )

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        // Verify no stage creation was attempted
        verify(workoutStageDAL, org.mockito.kotlin.times(0)).insertWorkoutStage(any(), any(), any(), any())
    }

    @Test
    fun `hasConditioning should return true for DE days`() {
        // When & Then
        assert(workoutStageGenerationService.testHasConditioning("DE_Upper"))
        assert(workoutStageGenerationService.testHasConditioning("DE_Lower"))
        assert(workoutStageGenerationService.testHasConditioning("DE_Full_Body"))
        assert(!workoutStageGenerationService.testHasConditioning("ME_Upper"))
        assert(!workoutStageGenerationService.testHasConditioning("ME_Lower"))
    }

    @Test
    fun `calculateNumAccessoryExercises should delegate to sessionTimeCalculator`() {
        // Given
        val expectedNumAccessories = 3
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(expectedNumAccessories)

        // When
        val result =
            workoutStageGenerationService.testCalculateNumAccessoryExercises(
                sessionTimeMinutes = 60,
                primarySetSchemes = setSchemeParams,
                secondarySetSchemes = setSchemeParams,
                dayType = "ME_Upper"
            )

        // Then
        assert(result == expectedNumAccessories)
        verify(sessionTimeCalculator).calculateNumAccessoryExercisesDynamic(
            eq(60),
            eq(setSchemeParams),
            eq(setSchemeParams),
            eq("ME_Upper")
        )
    }

    /**
     * Test implementation of the abstract WorkoutStageGenerationService for testing purposes.
     */
    private inner class TestWorkoutStageGenerationService(
        exerciseSelectionService: ExerciseSelectionService,
        workoutStageDAL: WorkoutStageDAL,
        workoutStageTypeDAL: WorkoutStageTypeDAL,
        programmedExerciseDAL: ProgrammedExerciseDAL,
        setSchemeDAL: SetSchemeDAL,
        setSchemeService: SetSchemeService,
        prilepinGuidelinesService: PrilepinGuidelinesService,
        weightSelectionService: WeightSelectionService,
        userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
        sessionTimeCalculator: SessionTimeCalculator,
    ) : WorkoutStageGenerationService(
            exerciseSelectionService,
            workoutStageDAL,
            workoutStageTypeDAL,
            programmedExerciseDAL,
            setSchemeDAL,
            setSchemeService,
            prilepinGuidelinesService,
            weightSelectionService,
            userWeightUnitPreferenceDAL,
            sessionTimeCalculator
        ) {
        override fun generateStagesForDayType(
            workout: ProgrammedWorkout,
            dayType: String,
            exercises: List<Exercise>,
            preferences: List<UserExercisePreference>,
            userEquipment: List<UserEquipment>,
            oneRepMaxes: List<UserOneRepMax>,
            programPreferences: UserProgramPreferences,
            rotationHistory: List<com.congen.model.ExerciseRotationHistory>,
            weakMuscles: List<String>,
            currentWeekNumber: Int,
            userId: Int
        ): Mono<Void> {
            // Simple test implementation that creates a primary stage
            return createPrimaryStage(
                workout = workout,
                exercise = exercises.first(),
                setSchemes = listOf(mockSetSchemeParams()),
                userId = userId
            )
        }

        // Expose protected methods for testing
        fun testCreatePrimaryStage(
            workout: ProgrammedWorkout,
            exercise: Exercise,
            setSchemes: List<SetSchemeParams>,
            userId: Int
        ): Mono<Void> = createPrimaryStage(workout, exercise, setSchemes, userId)

        fun testCreateSecondaryStage(
            workout: ProgrammedWorkout,
            exercise: Exercise,
            setSchemes: List<SetSchemeParams>,
            userId: Int
        ): Mono<Void> = createSecondaryStage(workout, exercise, setSchemes, userId)

        fun testCreateAccessoryStage(
            workout: ProgrammedWorkout,
            numAccessoryExercises: Int,
            exercises: List<Exercise>,
            preferences: List<UserExercisePreference>,
            userEquipment: List<UserEquipment>,
            oneRepMaxes: List<UserOneRepMax>,
            dayType: String,
            weakMuscles: List<String>,
            currentWeekNumber: Int,
            userId: Int
        ): Mono<Void> =
            createAccessoryStage(
                workout,
                exercises,
                preferences,
                userEquipment,
                oneRepMaxes,
                dayType,
                weakMuscles,
                numAccessoryExercises,
                emptyList(),
                currentWeekNumber,
                userId
            )

        fun testCreateConditioningStage(
            workout: ProgrammedWorkout,
            dayType: String,
            exercises: List<Exercise>,
            preferences: List<UserExercisePreference>,
            userEquipment: List<UserEquipment>,
            oneRepMaxes: List<UserOneRepMax>,
            weakMuscles: List<String>,
            userId: Int
        ): Mono<Void> =
            createConditioningStage(
                workout,
                dayType,
                exercises,
                preferences,
                userEquipment,
                oneRepMaxes,
                weakMuscles,
                emptyList(),
                1,
                userId
            )

        fun testHasConditioning(dayType: String): Boolean = hasConditioning(dayType)

        fun testCalculateNumAccessoryExercises(
            sessionTimeMinutes: Int,
            primarySetSchemes: List<SetSchemeParams>,
            secondarySetSchemes: List<SetSchemeParams>,
            dayType: String
        ): Int = calculateNumAccessoryExercises(sessionTimeMinutes, primarySetSchemes, secondarySetSchemes, dayType)
    }

    private fun mockSetSchemeParams(): SetSchemeParams {
        return SetSchemeParams(
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
            restSeconds = 180,
            band = null
        )
    }
}
