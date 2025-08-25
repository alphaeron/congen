package com.congen.generator

import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.UserWeakMuscle
import com.congen.service.ProgramService
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
 * Unit tests for the ConjugateWorkoutGeneratorService.
 *
 * These tests verify that the service correctly generates conjugate workout programs
 * with proper exercise selection, workout structure, and user preference handling.
 */
class ConjugateWorkoutGeneratorServiceTest {
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var programService: ProgramService
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator
    private lateinit var userWeakMuscleDAL: UserWeakMuscleDAL
    private lateinit var exercisePoolFactory: ExercisePoolFactory

    companion object {
        private const val USER_ID = "test-user-123"
        private const val PROGRAM_ID = 1L
    }

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        userProgramPreferencesDAL = mock()
        programService = mock()
        programmedWorkoutDAL = mock()
        conjugateTemplates = mock()
        workoutStageGenerationOrchestrator = mock()
        userWeakMuscleDAL = mock()
        exercisePoolFactory = mock()

        conjugateWorkoutGeneratorService = ConjugateWorkoutGeneratorService(
            userOneRepMaxDAL = userOneRepMaxDAL,
            userProgramPreferencesDAL = userProgramPreferencesDAL,
            programService = programService,
            programmedWorkoutDAL = programmedWorkoutDAL,
            conjugateTemplates = conjugateTemplates,
            workoutStageGenerationOrchestrator = workoutStageGenerationOrchestrator,
            userWeakMuscleDAL = userWeakMuscleDAL,
            exercisePoolFactory = exercisePoolFactory
        )
    }

    @Test
    fun `generateNextWeek should generate workouts successfully`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val userExercisePool = mock<UserExercisePool>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.just(userExercisePool))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(PROGRAM_ID, 1, "Max Effort Day")).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(PROGRAM_ID, 2, "Dynamic Effort Day")).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(workoutStageGenerationOrchestrator.generateWorkoutStages(
            workout = any(),
            dayType = any(),
            userExercisePool = any(),
            oneRepMaxes = any(),
            programPreferences = any(),
            weakMuscles = any(),
            currentWeekNumber = any(),
            userId = any()
        )).thenReturn(Mono.empty())
        whenever(programService.updateProgram(PROGRAM_ID, "Conjugate Powerlifting - Week 2", 2, true)).thenReturn(Mono.just(program))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(programService).selectProgramById(PROGRAM_ID)
        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(USER_ID)
        verify(userProgramPreferencesDAL).selectUserProgramPreferences(USER_ID)
        verify(userWeakMuscleDAL).selectUserWeakMusclesByUser(USER_ID)
        verify(conjugateTemplates).selectTemplate(4)
        verify(exercisePoolFactory).createPoolForUser(USER_ID)
    }

    @Test
    fun `generateNextWeek should handle empty weak muscles`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val emptyUserWeakMuscles = emptyList<UserWeakMuscle>()
        val userExercisePool = mock<UserExercisePool>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(emptyUserWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.just(userExercisePool))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(workoutStageGenerationOrchestrator.generateWorkoutStages(
            workout = any(),
            dayType = any(),
            userExercisePool = any(),
            oneRepMaxes = any(),
            programPreferences = any(),
            weakMuscles = any(),
            currentWeekNumber = any(),
            userId = any()
        )).thenReturn(Mono.empty())
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(program))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle program not found`() {
        // Given
        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.error(RuntimeException("Program not found")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle one rep max DAL error`() {
        // Given
        val program = createSampleProgram()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle program preferences DAL error`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle weak muscles DAL error`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle exercise pool factory error`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Pool creation error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle workout stage generation error`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val userExercisePool = mock<UserExercisePool>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.just(userExercisePool))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(workoutStageGenerationOrchestrator.generateWorkoutStages(
            workout = any(),
            dayType = any(),
            userExercisePool = any(),
            oneRepMaxes = any(),
            programPreferences = any(),
            weakMuscles = any(),
            currentWeekNumber = any(),
            userId = any()
        )).thenReturn(Mono.error(RuntimeException("Stage generation error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle program update error`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val userExercisePool = mock<UserExercisePool>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.just(userExercisePool))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(workoutStageGenerationOrchestrator.generateWorkoutStages(
            workout = any(),
            dayType = any(),
            userExercisePool = any(),
            oneRepMaxes = any(),
            programPreferences = any(),
            weakMuscles = any(),
            currentWeekNumber = any(),
            userId = any()
        )).thenReturn(Mono.empty())
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.error(RuntimeException("Update error")))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle different program days per week`() {
        // Given
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences().copy(programDaysPerWeek = 3)
        val userWeakMuscles = createSampleUserWeakMuscles()
        val userExercisePool = mock<UserExercisePool>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(3)).thenReturn(template)
        whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.just(userExercisePool))
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(workoutStageGenerationOrchestrator.generateWorkoutStages(
            workout = any(),
            dayType = any(),
            userExercisePool = any(),
            oneRepMaxes = any(),
            programPreferences = any(),
            weakMuscles = any(),
            currentWeekNumber = any(),
            userId = any()
        )).thenReturn(Mono.empty())
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(program))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()

        verify(conjugateTemplates).selectTemplate(3)
    }

    private fun createSampleProgram(): Program {
        return Program(
            id = PROGRAM_ID,
            userId = USER_ID,
            name = "Conjugate Powerlifting - Week 1",
            currentWeekNumber = 1,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            UserOneRepMax(
                userId = USER_ID,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225"),
                updatedAt = Instant.now()
            ),
            UserOneRepMax(
                userId = USER_ID,
                exerciseName = "Squat",
                oneRepMax = BigDecimal("315"),
                updatedAt = Instant.now()
            )
        )
    }

    private fun createSampleProgramPreferences(): UserProgramPreferences {
        return UserProgramPreferences(
            userId = USER_ID,
            programDaysPerWeek = 4,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleUserWeakMuscles(): List<UserWeakMuscle> {
        return listOf(
            UserWeakMuscle(
                userId = USER_ID,
                muscleName = "Chest",
                createdAt = Instant.now()
            )
        )
    }

    private fun createSampleTemplate(): List<DayTemplate> {
        return listOf(
            DayTemplate(type = "Max Effort"),
            DayTemplate(type = "Dynamic Effort")
        )
    }

    private fun createSampleProgrammedWorkout(): ProgrammedWorkout {
        return ProgrammedWorkout(
            id = 1L,
            programId = PROGRAM_ID,
            dayNumber = 1,
            name = "Max Effort Day",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
