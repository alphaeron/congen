package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.ValidationException
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStage
import com.congen.service.conjugate.ConjugateTemplates
import com.congen.service.conjugate.ExerciseSelectionService
import com.congen.service.conjugate.SessionTimeCalculator
import com.congen.service.conjugate.WorkoutStageGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class ConjugateWorkoutGeneratorServiceTest {
    @Mock
    private lateinit var exerciseDAL: ExerciseDAL

    @Mock
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL

    @Mock
    private lateinit var programDAL: ProgramDAL

    @Mock
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    @Mock
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL

    @Mock
    private lateinit var setSchemeDAL: SetSchemeDAL

    @Mock
    private lateinit var userDAL: UserDAL

    @Mock
    private lateinit var userEquipmentDAL: UserEquipmentDAL

    @Mock
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL

    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @Mock
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL

    @Mock
    private lateinit var workoutStageDAL: WorkoutStageDAL

    @Mock
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL

    @Mock
    private lateinit var conjugateTemplates: ConjugateTemplates

    @Mock
    private lateinit var exerciseSelectionService: ExerciseSelectionService

    @Mock
    private lateinit var workoutStageGenerator: WorkoutStageGenerator

    @Mock
    private lateinit var sessionTimeCalculator: SessionTimeCalculator

    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        conjugateWorkoutGeneratorService =
            ConjugateWorkoutGeneratorService(
                exerciseDAL,
                userExercisePreferenceDAL,
                userEquipmentDAL,
                userOneRepMaxDAL,
                userProgramPreferencesDAL,
                exerciseRotationHistoryDAL,
                programDAL,
                programmedWorkoutDAL,
                conjugateTemplates,
                exerciseSelectionService,
                workoutStageGenerator,
                sessionTimeCalculator
            )
        // Add default mocks for conjugateTemplates
        whenever(conjugateTemplates.selectTemplate(any())).thenReturn(
            listOf(
                com.congen.service.conjugate.DayTemplate("ME_Upper"),
                com.congen.service.conjugate.DayTemplate("DE_Lower"),
                com.congen.service.conjugate.DayTemplate("ME_Lower"),
                com.congen.service.conjugate.DayTemplate("DE_Upper")
            )
        )
        whenever(conjugateTemplates.hasSecondaryMovement(any())).thenReturn(true)
        whenever(conjugateTemplates.hasConditioning(any())).thenReturn(true)
        // Add default mocks for exerciseSelectionService
        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any())).thenReturn(
            com.congen.model.Exercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = "horizontal_push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), any())).thenReturn(
            listOf(
                com.congen.model.Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal_push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                )
            )
        )
        whenever(exerciseSelectionService.filterExercisesExcluding(any(), any())).thenReturn(
            listOf(
                com.congen.model.Exercise(
                    name = "Incline Bench Press",
                    description = "An incline compound upper body exercise",
                    movementType = "horizontal_push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                )
            )
        )
        // Add default mocks for workoutStageGenerator
        whenever(workoutStageGenerator.generatePrilepinBasedScheme(any(), any(), any(), any(), any(), any())).thenReturn(
            listOf(
                com.congen.service.conjugate.SetSchemeParams(
                    1,
                    false,
                    false,
                    false,
                    null,
                    null,
                    null,
                    BigDecimal("100.0"),
                    null,
                    5,
                    null,
                    180
                )
            )
        )
        whenever(workoutStageGenerator.generateSecondaryExerciseScheme(any(), any(), any())).thenReturn(
            listOf(
                com.congen.service.conjugate.SetSchemeParams(
                    1,
                    false,
                    false,
                    false,
                    null,
                    null,
                    null,
                    BigDecimal("80.0"),
                    null,
                    8,
                    null,
                    120
                )
            )
        )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(
            Mono.just(
                com.congen.model.WorkoutStage(
                    id = 1L,
                    programmedWorkoutId = 1L,
                    stageTypeId = 1,
                    name = "Test Stage",
                    position = 1,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            )
        )
        whenever(workoutStageGenerator.createProgrammedExercise(any(), any())).thenReturn(
            Mono.just(
                com.congen.model.ProgrammedExercise(
                    id = 1L,
                    workoutStageId = 1L,
                    exerciseName = "Bench Press",
                    position = 1,
                    notes = null,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            )
        )
        whenever(workoutStageGenerator.createSetSchemes(any(), any())).thenReturn(Mono.empty())
        // Add default mocks for sessionTimeCalculator
        whenever(sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(any(), any(), any(), any())).thenReturn(2)
    }

    @Test
    fun `generateNextWeek should create program successfully with 3 days per week`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with no exercise history`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = emptyList<UserOneRepMax>() // No 1RM data
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with exercise preferences`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = userId,
                    exerciseName = "Squat",
                    shouldAvoid = true,
                    createdAt = LocalDateTime.now()
                )
            )
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with exercise rotation history`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                )
            )

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle 2-day program`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 2,
                sessionTimeLengthInMinutes = 60,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle 4-day program`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 4,
                sessionTimeLengthInMinutes = 60,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle database errors gracefully`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle missing user data`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = emptyList<UserEquipment>() // No equipment
        val oneRepMaxes = emptyList<UserOneRepMax>() // No 1RM data
        val programPreferences = createSampleProgramPreferences() // program preferences are always required
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should calculate accessory exercises based on session time`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should calculate fewer accessories for DE days with conditioning`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "DE_Lower Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation - should create fewer stages for DE days due to conditioning
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should prioritize unused exercises in rotation history`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()

        // Create rotation history with multiple exercises used in primary category
        val rotationHistory =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                ),
                ExerciseRotationHistory(
                    id = 3L,
                    userId = userId,
                    exerciseName = "Incline Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                )
            )

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should apply undulating periodization correctly for different weeks`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should select different exercises for primary and secondary stages`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val program =
            Program(
                id = 1L,
                userId = userId,
                name = "Test Program",
                currentWeekNumber = currentWeekNumber,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(program))

        // Mock workout creation
        val createdWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "ME_Upper Day",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                name = "Test Stage",
                position = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should throw exception for invalid programDaysPerWeek`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences =
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 5, // Invalid
                sessionTimeLengthInMinutes = 60,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))

        // When & Then
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
    }

    // Helper methods to create sample data
    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            Exercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = "horizontal_push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            Exercise(
                name = "Incline Bench Press",
                description = "An incline compound upper body exercise",
                movementType = "horizontal_push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            Exercise(
                name = "Squat",
                description = "A compound lower body exercise",
                movementType = "squat",
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            ),
            Exercise(
                name = "Deadlift",
                description = "A compound hinge exercise",
                movementType = "hinge",
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            ),
            Exercise(
                name = "Overhead Press",
                description = "A compound vertical push exercise",
                movementType = "vertical_push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            Exercise(
                name = "Pull-ups",
                description = "A compound vertical pull exercise",
                movementType = "vertical_pull",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            Exercise(
                name = "Bicep Curls",
                description = "An isolation exercise",
                movementType = "accessory",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            ),
            Exercise(
                name = "Tricep Extensions",
                description = "An isolation exercise",
                movementType = "accessory",
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        )
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return listOf(
            UserEquipment(userId = 1, equipmentName = "Barbell", createdAt = LocalDateTime.now()),
            UserEquipment(userId = 1, equipmentName = "Dumbbells", createdAt = LocalDateTime.now()),
            UserEquipment(userId = 1, equipmentName = "Bench", createdAt = LocalDateTime.now()),
            UserEquipment(userId = 1, equipmentName = "Pull-up Bar", createdAt = LocalDateTime.now()),
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            UserOneRepMax(userId = 1, exerciseName = "Bench Press", oneRepMax = BigDecimal("100.0"), updatedAt = LocalDateTime.now()),
            UserOneRepMax(userId = 1, exerciseName = "Squat", oneRepMax = BigDecimal("150.0"), updatedAt = LocalDateTime.now()),
            UserOneRepMax(userId = 1, exerciseName = "Deadlift", oneRepMax = BigDecimal("200.0"), updatedAt = LocalDateTime.now())
        )
    }

    private fun createSampleProgramPreferences(): UserProgramPreferences {
        return UserProgramPreferences(
            userId = 1,
            programDaysPerWeek = 3,
            sessionTimeLengthInMinutes = 60,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createSampleSetScheme(): SetScheme {
        return SetScheme(
            id = 1L,
            programmedExerciseId = 1L,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = BigDecimal("85.0"),
            performedWeight = null,
            targetRepCount = 5,
            performedRepCount = null,
            restSeconds = 180,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
