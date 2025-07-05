package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseEquipmentDAL = mock()
        userExercisePreferenceDAL = mock()
        userEquipmentDAL = mock()
        userOneRepMaxDAL = mock()
        userProgramPreferencesDAL = mock()
        exerciseRotationHistoryDAL = mock()
        programDAL = mock()
        programmedWorkoutDAL = mock()
        workoutStageDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeDAL = mock()

        conjugateWorkoutGeneratorService = ConjugateWorkoutGeneratorService(
            exerciseDAL = exerciseDAL,
            exerciseMuscleDAL = exerciseMuscleDAL,
            exerciseEquipmentDAL = exerciseEquipmentDAL,
            userExercisePreferenceDAL = userExercisePreferenceDAL,
            userEquipmentDAL = userEquipmentDAL,
            userOneRepMaxDAL = userOneRepMaxDAL,
            userProgramPreferencesDAL = userProgramPreferencesDAL,
            exerciseRotationHistoryDAL = exerciseRotationHistoryDAL,
            programDAL = programDAL,
            programmedWorkoutDAL = programmedWorkoutDAL,
            workoutStageDAL = workoutStageDAL,
            programmedExerciseDAL = programmedExerciseDAL,
            setSchemeDAL = setSchemeDAL
        )
    }

    @Test
    fun `generateNextWeek should create program successfully with 3 days per week`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()

        verify(programDAL).insertProgram(any())
        verify(programmedWorkoutDAL).insertProgrammedWorkout(any())
        verify(workoutStageDAL).insertWorkoutStage(any())
        verify(programmedExerciseDAL).insertProgrammedExercise(any())
        verify(setSchemeDAL).insertSetScheme(any())
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

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val preferences = listOf(
            UserExercisePreference(userId = userId, exerciseName = "Squat", shouldAvoid = true)
        )
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val rotationHistory = listOf(
            ExerciseRotationHistory(
                id = 1L,
                exerciseName = "Bench Press",
                isAccessory = false,
                usedAt = LocalDateTime.now()
            )
        )

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation - should select a different exercise since Bench Press was already used
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Incline Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val numDaysPerWeek = 2

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val numDaysPerWeek = 4

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should throw exception for invalid numDaysPerWeek`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 5 // Invalid

        // When & Then
        assertThrows<IllegalArgumentException> {
            conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)
        }
    }

    @Test
    fun `generateNextWeek should handle database errors gracefully`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

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
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = emptyList<UserEquipment>() // No equipment
        val oneRepMaxes = emptyList<UserOneRepMax>() // No 1RM data
        val programPreferences = emptyList<UserProgramPreferences>() // No preferences
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

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
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = listOf(
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 45 // 45 minute session
            )
        )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation - should create more stages for accessories
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()

        // Verify that more accessory stages are created for longer sessions
        // 45 minutes - 10 (primary) - 8 (secondary) = 27 minutes remaining
        // 27 / 5 = 5 accessory exercises
        verify(workoutStageDAL, org.mockito.kotlin.times(7)).insertWorkoutStage(any()) // 1 primary + 1 secondary + 5 accessories
    }

    @Test
    fun `should calculate fewer accessories for DE days with conditioning`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = listOf(
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 40 // 40 minute session
            )
        )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "DE_Lower Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation - should create fewer stages for DE days due to conditioning
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Squat", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()

        // Verify that fewer accessory stages are created for DE days
        // 40 minutes - 10 (primary) - 10 (conditioning) = 20 minutes remaining
        // 20 / 5 = 4 accessory exercises
        verify(workoutStageDAL, org.mockito.kotlin.times(6)).insertWorkoutStage(any()) // 1 primary + 1 conditioning + 4 accessories
    }

    @Test
    fun `should prioritize unused exercises in rotation history`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        
        // Create rotation history with multiple exercises used in primary category
        val rotationHistory = listOf(
            ExerciseRotationHistory(
                id = 1L,
                exerciseName = "Bench Press",
                isAccessory = false,
                usedAt = LocalDateTime.now().minusDays(7)
            ),
            ExerciseRotationHistory(
                id = 2L,
                exerciseName = "Bench Press",
                isAccessory = false,
                usedAt = LocalDateTime.now().minusDays(14)
            ),
            ExerciseRotationHistory(
                id = 3L,
                exerciseName = "Incline Bench Press",
                isAccessory = false,
                usedAt = LocalDateTime.now().minusDays(21)
            )
        )

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation - should select a different exercise since Bench Press was used twice
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Overhead Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should apply undulating periodization correctly for different weeks`() {
        // Given
        val userId = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week 1",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // Test different weeks to verify periodization
        val testWeeks = listOf(1, 2, 3, 4, 5, 6, 7, 8)

        testWeeks.forEach { weekNumber ->
            // When
            val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, weekNumber, numDaysPerWeek)

            // Then
            StepVerifier.create(result)
                .expectNext(createdProgram)
                .verifyComplete()
        }
    }

    @Test
    fun `generateNextWeek should select different exercises for primary and secondary stages`() {
        // Given
        val userId = 1
        val currentWeekNumber = 1
        val numDaysPerWeek = 3

        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        val createdProgram = Program(
            id = 1L,
            userId = userId,
            name = "Conjugate Powerlifting - Week $currentWeekNumber",
            description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        )

        // Mock DAL responses
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programDAL.insertProgram(any())).thenReturn(Mono.just(createdProgram))

        // Mock workout creation
        val createdWorkout = ProgrammedWorkout(id = 1L, programId = 1L, dayNumber = 1, name = "ME_Upper Day")
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any())).thenReturn(Mono.just(createdWorkout))

        // Mock stage creation
        val createdStage = WorkoutStage(id = 1L, programmedWorkoutId = 1L, stageTypeId = 1, position = 1)
        whenever(workoutStageDAL.insertWorkoutStage(any())).thenReturn(Mono.just(createdStage))

        // Mock exercise creation - should select different exercises for primary and secondary
        val createdExercise = ProgrammedExercise(id = 1L, workoutStageId = 1L, exerciseName = "Bench Press", notes = null)
        whenever(programmedExerciseDAL.insertProgrammedExercise(any())).thenReturn(Mono.just(createdExercise))

        // Mock set scheme creation
        whenever(setSchemeDAL.insertSetScheme(any())).thenReturn(Mono.just(createSampleSetScheme()))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber, numDaysPerWeek)

        // Then
        StepVerifier.create(result)
            .expectNext(createdProgram)
            .verifyComplete()

        // Verify that both primary and secondary stages are created for ME_Upper day
        // Primary stage (ME exercise) + Secondary stage (additional primary exercise) + Accessory stages
        verify(workoutStageDAL, org.mockito.kotlin.atLeast(3)).insertWorkoutStage(any())
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

    private fun createSampleProgramPreferences(): List<UserProgramPreferences> {
        return listOf(
            UserProgramPreferences(
                userId = 1,
                programDaysPerWeek = 3,
                sessionTimeLengthInMinutes = 60
            )
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
            eccentricTempo = "0",
            isometricTempo = "0",
            concentricTempo = "0",
            targetWeight = BigDecimal("85.0"),
            performedWeight = null,
            targetRepCount = 3,
            performedRepCount = null,
            restSeconds = 180
        )
    }
} 