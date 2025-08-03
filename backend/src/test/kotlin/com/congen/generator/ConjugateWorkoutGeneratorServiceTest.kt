package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.MovementType
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.service.ProgramService
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
import java.time.Instant

class ConjugateWorkoutGeneratorServiceTest {
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService
    private lateinit var programService: ProgramService
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator
    private lateinit var sessionTimeCalculator: SessionTimeCalculator
    private lateinit var userWeakMuscleDAL: UserWeakMuscleDAL

    companion object {
        private const val PROGRAM_ID = 1L
        private const val CURRENT_WEEK = 1
    }

    @BeforeEach
    fun setUp() {
        programService = mock()
        userProgramPreferencesDAL = mock()
        exerciseDAL = mock()
        userExercisePreferenceDAL = mock()
        userEquipmentDAL = mock()
        userOneRepMaxDAL = mock()
        exerciseRotationHistoryDAL = mock()
        programmedWorkoutDAL = mock()
        conjugateTemplates = mock()
        exerciseSelectionService = mock()
        workoutStageGenerationOrchestrator = mock()
        sessionTimeCalculator = mock()
        userWeakMuscleDAL = mock()
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(any())).thenReturn(Mono.just(emptyList()))

        conjugateWorkoutGeneratorService =
            ConjugateWorkoutGeneratorService(
                exerciseDAL = exerciseDAL,
                userExercisePreferenceDAL = userExercisePreferenceDAL,
                userEquipmentDAL = userEquipmentDAL,
                userOneRepMaxDAL = userOneRepMaxDAL,
                userProgramPreferencesDAL = userProgramPreferencesDAL,
                exerciseRotationHistoryDAL = exerciseRotationHistoryDAL,
                programService = programService,
                programmedWorkoutDAL = programmedWorkoutDAL,
                conjugateTemplates = conjugateTemplates,
                workoutStageGenerationOrchestrator = workoutStageGenerationOrchestrator,
                userWeakMuscleDAL = userWeakMuscleDAL
            )
    }

    @Test
    fun `generateNextWeek should successfully generate next week`() {
        // Given
        val program = mockProgram(id = PROGRAM_ID, currentWeekNumber = CURRENT_WEEK)
        val exercises = createSampleExercises()
        val preferences = createSamplePreferences()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = 3)
        val rotationHistory = createSampleRotationHistory()

        setupDALMocks(
            exercises = exercises,
            preferences = preferences,
            userEquipment = userEquipment,
            oneRepMaxes = oneRepMaxes,
            programPreferences = programPreferences,
            rotationHistory = rotationHistory
        )

        whenever(conjugateTemplates.selectTemplate(3)).thenReturn(
            listOf(
                DayTemplate("ME_Upper_DE_Lower"),
                DayTemplate("ME_Lower_DE_Upper"),
                DayTemplate("DE_Full_Body")
            )
        )
        whenever(conjugateTemplates.hasSecondaryMovement(any())).thenReturn(false)
        whenever(conjugateTemplates.isCombinedMEDay("ME_Upper_DE_Lower")).thenReturn(true)
        whenever(conjugateTemplates.isCombinedMEDay("ME_Lower_DE_Upper")).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE("DE_Full_Body")).thenReturn(true)
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Upper_DE_Lower")).thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Upper_DE_Lower")).thenReturn("DE_Lower")
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Lower_DE_Upper")).thenReturn("ME_Lower")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Lower_DE_Upper")).thenReturn("DE_Upper")

        // Set up exercise selection mocks
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any<List<Exercise>>(), any())).thenReturn(
            Mono.just(exercises.filter { !it.isAccessory })
        )
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any<List<Exercise>>())).thenReturn(
            Mono.just(exercises)
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any<List<Exercise>>(), any())).thenReturn(
            exercises.filter { !it.isAccessory }
        )
        whenever(
            exerciseSelectionService.selectRotatingExercise(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(exercises.first()))

        // Set up workout stage generation orchestrator mocks
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
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

        // Set up session time calculator mocks
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(0)

        // Set up workout mocks directly
        val createdWorkout = mockProgrammedWorkout()
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // Set up workout stage generation orchestrator mocks
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
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

        // Add missing mock for filterExercisesForDEWorkout
        // whenever(exerciseSelectionService.filterExercisesForDEWorkout(any<List<Exercise>>())).thenReturn(
        //     Mono.just(exercises.filter { !it.isAccessory })
        // )

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { updatedProgram ->
                updatedProgram.currentWeekNumber == CURRENT_WEEK + 1
            }
            .verifyComplete()

        verify(programService).updateProgram(eq(PROGRAM_ID), any(), eq(CURRENT_WEEK + 1), eq(true))
    }

    @Test
    fun `generateNextWeek should handle program not found`() {
        // Given
        whenever(programService.getProgramById(PROGRAM_ID)).thenReturn(Mono.empty())

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(programService, times(0)).updateProgram(any(), any(), any(), any())
    }

    @Test
    fun `generateNextWeek should handle combined ME+DE days`() {
        // Given
        val programId = 1L
        val program = mockProgram(id = programId, currentWeekNumber = 1)
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = 2)
        val exercises = createSampleExercises()

        whenever(programService.getProgramById(programId)).thenReturn(Mono.just(program))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(any())).thenReturn(Mono.just(programPreferences))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(emptyList()))
        whenever(
            programService.updateProgram(any(), any(), any(), any())
        ).thenReturn(Mono.just(mockProgram(id = programId, currentWeekNumber = 2)))

        whenever(conjugateTemplates.selectTemplate(2)).thenReturn(
            listOf(
                DayTemplate("ME_Upper_DE_Lower"),
                DayTemplate("ME_Lower_DE_Upper")
            )
        )
        whenever(conjugateTemplates.isCombinedMEDay("ME_Upper_DE_Lower")).thenReturn(true)
        whenever(conjugateTemplates.isCombinedMEDay("ME_Lower_DE_Upper")).thenReturn(true)
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Upper_DE_Lower")).thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Upper_DE_Lower")).thenReturn("DE_Lower")
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Lower_DE_Upper")).thenReturn("ME_Lower")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Lower_DE_Upper")).thenReturn("DE_Upper")

        // Set up exercise selection mocks
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any<List<Exercise>>(), any())).thenReturn(
            Mono.just(exercises.filter { !it.isAccessory })
        )
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any<List<Exercise>>())).thenReturn(
            Mono.just(exercises)
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any<List<Exercise>>(), any())).thenReturn(
            exercises.filter { !it.isAccessory }
        )
        whenever(
            exerciseSelectionService.selectRotatingExercise(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(exercises.first()))

        // Set up workout stage generation orchestrator mocks
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
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

        // Set up session time calculator mocks
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(0)

        // Set up workout mocks
        val createdWorkout = mockProgrammedWorkout()
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(programId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { updatedProgram ->
                updatedProgram.currentWeekNumber == 2
            }
            .verifyComplete()

        verify(programmedWorkoutDAL, times(2)).insertProgrammedWorkout(any(), any(), any())
        verify(programService).updateProgram(eq(programId), any(), eq(2), eq(true))
    }

    @Test
    fun `generateNextWeek should handle full body DE day`() {
        // Given
        val programId = 1L
        val program = mockProgram(id = programId, currentWeekNumber = 1)
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = 3)
        val exercises = createSampleExercises()

        whenever(programService.getProgramById(programId)).thenReturn(Mono.just(program))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(any())).thenReturn(Mono.just(programPreferences))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(emptyList()))
        whenever(
            programService.updateProgram(any(), any(), any(), any())
        ).thenReturn(Mono.just(mockProgram(id = programId, currentWeekNumber = 2)))

        whenever(conjugateTemplates.selectTemplate(3)).thenReturn(
            listOf(
                DayTemplate("ME_Upper_DE_Lower"),
                DayTemplate("ME_Lower_DE_Upper"),
                DayTemplate("DE_Full_Body")
            )
        )
        whenever(conjugateTemplates.isCombinedMEDay("ME_Upper_DE_Lower")).thenReturn(true)
        whenever(conjugateTemplates.isCombinedMEDay("ME_Lower_DE_Upper")).thenReturn(true)
        whenever(conjugateTemplates.isFullBodyDE("DE_Full_Body")).thenReturn(true)
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Upper_DE_Lower")).thenReturn("ME_Upper")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Upper_DE_Lower")).thenReturn("DE_Lower")
        whenever(conjugateTemplates.getPrimaryMovementType("ME_Lower_DE_Upper")).thenReturn("ME_Lower")
        whenever(conjugateTemplates.getSecondaryMovementType("ME_Lower_DE_Upper")).thenReturn("DE_Upper")

        // Set up exercise selection mocks
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any<List<Exercise>>(), any())).thenReturn(
            Mono.just(exercises.filter { !it.isAccessory })
        )
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any<List<Exercise>>())).thenReturn(
            Mono.just(exercises)
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any<List<Exercise>>(), any())).thenReturn(
            exercises.filter { !it.isAccessory }
        )
        whenever(
            exerciseSelectionService.selectRotatingExercise(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(exercises.first()))

        // Set up workout stage generation orchestrator mocks
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
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

        // Set up session time calculator mocks
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(0)

        // Set up workout mocks
        val createdWorkout = mockProgrammedWorkout()
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(programId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { updatedProgram ->
                updatedProgram.currentWeekNumber == 2
            }
            .verifyComplete()

        verify(programmedWorkoutDAL, times(3)).insertProgrammedWorkout(any(), any(), any())
        verify(programService).updateProgram(eq(programId), any(), eq(2), eq(true))
    }

    @Test
    fun `generateNextWeek should handle traditional 4-day program`() {
        // Given
        val programId = 1L
        val program = mockProgram(id = programId, currentWeekNumber = 1)
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = 4)
        val exercises = createSampleExercises()

        whenever(programService.getProgramById(programId)).thenReturn(Mono.just(program))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(any())).thenReturn(Mono.just(programPreferences))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(emptyList()))
        whenever(
            programService.updateProgram(any(), any(), any(), any())
        ).thenReturn(Mono.just(mockProgram(id = programId, currentWeekNumber = 2)))

        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(
            listOf(
                DayTemplate("ME_Upper"),
                DayTemplate("DE_Lower"),
                DayTemplate("ME_Lower"),
                DayTemplate("DE_Upper")
            )
        )
        whenever(conjugateTemplates.isCombinedMEDay(any())).thenReturn(false)
        whenever(conjugateTemplates.isFullBodyDE(any())).thenReturn(false)
        whenever(conjugateTemplates.hasSecondaryMovement("ME_Upper")).thenReturn(true)
        whenever(conjugateTemplates.hasSecondaryMovement("DE_Upper")).thenReturn(true)
        whenever(conjugateTemplates.hasSecondaryMovement("ME_Lower")).thenReturn(false)
        whenever(conjugateTemplates.hasSecondaryMovement("DE_Lower")).thenReturn(false)

        // Set up exercise selection mocks
        whenever(exerciseSelectionService.filterExercisesByWorkoutType(any<List<Exercise>>(), any())).thenReturn(
            Mono.just(exercises.filter { !it.isAccessory })
        )
        whenever(exerciseSelectionService.filterExercisesForDEWorkout(any<List<Exercise>>())).thenReturn(
            Mono.just(exercises)
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any<List<Exercise>>(), any())).thenReturn(
            exercises.filter { !it.isAccessory }
        )
        whenever(exerciseSelectionService.filterExercisesExcluding(any<List<Exercise>>(), any())).thenReturn(
            exercises.filter { !it.isAccessory }
        )
        whenever(
            exerciseSelectionService.selectRotatingExercise(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(exercises.first()))
        whenever(
            exerciseSelectionService.selectSimilarSecondaryExercise(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.empty())

        // Set up workout stage generation orchestrator mocks
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
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

        // Set up session time calculator mocks
        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(0)

        // Set up workout mocks
        val createdWorkout = mockProgrammedWorkout()
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))

        // When
        val result = conjugateWorkoutGeneratorService.generateNextWeek(programId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { updatedProgram ->
                updatedProgram.currentWeekNumber == 2
            }
            .verifyComplete()

        verify(programmedWorkoutDAL, times(4)).insertProgrammedWorkout(any(), any(), any())
        verify(programService).updateProgram(eq(programId), any(), eq(2), eq(true))
    }

    private fun setupDALMocks(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>
    ) {
        whenever(programService.getProgramById(PROGRAM_ID)).thenReturn(Mono.just(mockProgram()))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(any())).thenReturn(Mono.just(programPreferences))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(any())).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(any())).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(any())).thenReturn(Mono.just(oneRepMaxes))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(mockProgram(currentWeekNumber = 2)))
    }

    private fun mockProgram(
        id: Long = PROGRAM_ID,
        currentWeekNumber: Int = CURRENT_WEEK
    ): Program {
        return Program(
            id = id,
            userId = "b226d772-c063-4974-ae08-ab64134abbcf",
            name = "Test Program",
            currentWeekNumber = currentWeekNumber,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun mockUserProgramPreferences(programDaysPerWeek: Int): UserProgramPreferences {
        return UserProgramPreferences(
            userId = "b226d772-c063-4974-ae08-ab64134abbcf",
            programDaysPerWeek = programDaysPerWeek,
            sessionTimeLengthInMinutes = 60,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun mockProgrammedWorkout(): ProgrammedWorkout {
        return ProgrammedWorkout(
            id = 1,
            programId = PROGRAM_ID,
            dayNumber = CURRENT_WEEK + 1,
            name = "Test Day",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            Exercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            Exercise(
                name = "Squat",
                description = "A compound lower body exercise",
                movementType = MovementType.SQUAT,
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            )
        )
    }

    private fun createSamplePreferences(): List<UserExercisePreference> {
        return emptyList()
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return emptyList()
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return emptyList()
    }

    private fun createSampleRotationHistory(): List<ExerciseRotationHistory> {
        return emptyList()
    }
}
