package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class ConjugateWorkoutGeneratorServiceTest {
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var conjugateTemplates: com.congen.service.conjugate.ConjugateTemplates
    private lateinit var exerciseSelectionService: com.congen.service.conjugate.ExerciseSelectionService
    private lateinit var workoutStageGenerator: com.congen.service.conjugate.WorkoutStageGenerator
    private lateinit var sessionTimeCalculator: com.congen.service.conjugate.SessionTimeCalculator
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        userExercisePreferenceDAL = mock()
        userEquipmentDAL = mock()
        userOneRepMaxDAL = mock()
        userProgramPreferencesDAL = mock()
        exerciseRotationHistoryDAL = mock()
        programDAL = mock()
        programmedWorkoutDAL = mock()
        conjugateTemplates = mock()
        exerciseSelectionService = mock()
        workoutStageGenerator = mock()
        sessionTimeCalculator = mock()

        conjugateWorkoutGeneratorService =
            ConjugateWorkoutGeneratorService(
                exerciseDAL = exerciseDAL,
                userExercisePreferenceDAL = userExercisePreferenceDAL,
                userEquipmentDAL = userEquipmentDAL,
                userOneRepMaxDAL = userOneRepMaxDAL,
                userProgramPreferencesDAL = userProgramPreferencesDAL,
                exerciseRotationHistoryDAL = exerciseRotationHistoryDAL,
                programDAL = programDAL,
                programmedWorkoutDAL = programmedWorkoutDAL,
                conjugateTemplates = conjugateTemplates,
                exerciseSelectionService = exerciseSelectionService,
                workoutStageGenerator = workoutStageGenerator,
                sessionTimeCalculator = sessionTimeCalculator
            )
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                UserExercisePreference(userId = userId, exerciseName = "Squat", shouldAvoid = true)
            )
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                    usedAt = LocalDateTime.now()
                )
            )

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                sessionTimeLengthInMinutes = 60
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                sessionTimeLengthInMinutes = 60
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "DE_Lower Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation - should create fewer stages for DE days due to conditioning
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                    usedAt = LocalDateTime.now().minusDays(7)
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    usedAt = LocalDateTime.now().minusDays(14)
                ),
                ExerciseRotationHistory(
                    id = 3L,
                    userId = userId,
                    exerciseName = "Incline Bench Press",
                    isAccessory = false,
                    usedAt = LocalDateTime.now().minusDays(21)
                )
            )

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week 1",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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

        val createdProgram =
            Program(
                id = 1L,
                userId = userId,
                name = "Conjugate Powerlifting - Week $currentWeekNumber",
                description = "Conjugate powerlifting program with ${programPreferences.programDaysPerWeek} days per week"
            )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(createdStage))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
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
                sessionTimeLengthInMinutes = 60
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
            UserEquipment(userId = 1, equipmentName = "Barbell"),
            UserEquipment(userId = 1, equipmentName = "Dumbbells"),
            UserEquipment(userId = 1, equipmentName = "Bench"),
            UserEquipment(userId = 1, equipmentName = "Pull-up Bar")
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            UserOneRepMax(userId = 1, exerciseName = "Bench Press", oneRepMax = BigDecimal("100.0")),
            UserOneRepMax(userId = 1, exerciseName = "Squat", oneRepMax = BigDecimal("150.0")),
            UserOneRepMax(userId = 1, exerciseName = "Deadlift", oneRepMax = BigDecimal("200.0"))
        )
    }

    private fun createSampleProgramPreferences(): UserProgramPreferences {
        return UserProgramPreferences(
            userId = 1,
            programDaysPerWeek = 3,
            sessionTimeLengthInMinutes = 60
        )
    }

    private fun createSampleSetScheme(): SetScheme {
        return SetScheme(
            id = 1L,
            programmedExerciseId = 1L,
            setNumber = 1,
            wasSetPerformed = false,
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
            restSeconds = 180
        )
    }
}
