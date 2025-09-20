package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.model.Program
import com.congen.model.ProgramPreferences
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserOneRepMax
import com.congen.model.UserWeakMuscle
import com.congen.service.ProgramService
import com.congen.service.SetSchemeService
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
    private lateinit var programPreferencesDAL: ProgramPreferencesDAL
    private lateinit var programService: ProgramService
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var conjugateTemplates: ConjugateTemplates
    private lateinit var workoutStageGenerationOrchestrator: WorkoutStageGenerationOrchestrator
    private lateinit var userWeakMuscleDAL: UserWeakMuscleDAL
    private lateinit var exercisePoolFactory: ExercisePoolFactory
    private lateinit var atomicWorkoutWriter: AtomicWorkoutWriter
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL

    companion object {
        private const val USER_ID = "test-user-123"
        private const val PROGRAM_ID = 1L
    }

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        programPreferencesDAL = mock()
        programService = mock()
        programmedWorkoutDAL = mock()
        programmedExerciseDAL = mock()
        conjugateTemplates = mock()
        workoutStageGenerationOrchestrator = mock()
        userWeakMuscleDAL = mock()
        exercisePoolFactory = mock()
        atomicWorkoutWriter = mock()
        setSchemeDAL = mock()
        setSchemeService = mock()
        userWeightUnitPreferenceDAL = mock()
        exerciseDAL = mock()
        exerciseEquipmentDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseWorkoutTypeDAL = mock()
        userEquipmentDAL = mock()
        userExercisePreferenceDAL = mock()

        conjugateWorkoutGeneratorService =
            ConjugateWorkoutGeneratorService(
                userOneRepMaxDAL = userOneRepMaxDAL,
                programPreferencesDAL = programPreferencesDAL,
                programService = programService,
                programmedWorkoutDAL = programmedWorkoutDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                conjugateTemplates = conjugateTemplates,
                workoutStageGenerationOrchestrator = workoutStageGenerationOrchestrator,
                atomicWorkoutWriter = atomicWorkoutWriter,
                userWeakMuscleDAL = userWeakMuscleDAL,
                exercisePoolFactory = exercisePoolFactory,
                setSchemeService = setSchemeService,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                exerciseDAL = exerciseDAL,
                exerciseEquipmentDAL = exerciseEquipmentDAL,
                exerciseMuscleDAL = exerciseMuscleDAL,
                exerciseWorkoutTypeDAL = exerciseWorkoutTypeDAL,
                userEquipmentDAL = userEquipmentDAL,
                userExercisePreferenceDAL = userExercisePreferenceDAL
            )
    }

    @Test
    fun `generateNextWeek should generate workouts successfully`() {
        val program = createSampleProgram()
        val updatedProgram = program.copy(currentWeekNumber = 2, name = "Week 2")
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        println("Debug: program = $program")
        println("Debug: oneRepMaxes = $oneRepMaxes")
        println("Debug: programPreferences = $programPreferences")
        println("Debug: userWeakMuscles = $userWeakMuscles")
        println("Debug: template = $template")

        whenever(programService.selectProgramById(any())).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(any())).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(any())).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(any())).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(any())).thenReturn(template)

        // Mock the additional DAL methods needed for data preparation
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(any())).thenReturn(Mono.just(emptyList()))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(createSampleExercises()))
        whenever(
            exerciseEquipmentDAL.selectAllExerciseEquipment()
        ).thenReturn(Mono.just(createSampleExerciseEquipmentMappings().values.flatten()))
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(createSampleExerciseMuscleMappings().values.flatten()))
        whenever(exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(emptyList()))
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(any())).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(any())).thenReturn(Mono.just(createSampleUserEquipment()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(any())).thenReturn(Mono.just(emptyList()))

        // Mock the exercise pool factory
        val samplePreparedData = createSamplePreparedData()
        whenever(
            exercisePoolFactory.createPoolFromPreparedData(any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(samplePreparedData.userExercisePool)

        // Mock the DAL methods for atomic workout generation
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(any(), any(), any(), any())
        ).thenReturn(Mono.just(WorkoutGenerationResult(PROGRAM_ID, 1, "max_effort", USER_ID, emptyList(), samplePreparedData)))
        whenever(atomicWorkoutWriter.writeWorkoutAtomically(any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(updatedProgram))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectNext(updatedProgram)
            .verifyComplete()

        verify(programService).selectProgramById(PROGRAM_ID)
        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(USER_ID)
        verify(programPreferencesDAL).selectProgramPreferences(PROGRAM_ID)
        verify(userWeakMuscleDAL).selectUserWeakMusclesByUser(USER_ID)
        verify(conjugateTemplates).selectTemplate(4)
    }

    @Test
    fun `generateNextWeek should handle empty weak muscles`() {
        val program = createSampleProgram()
        val updatedProgram = program.copy(currentWeekNumber = 2, name = "Week 2")
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val emptyUserWeakMuscles = emptyList<UserWeakMuscle>()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(emptyUserWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)

        // Mock the additional DAL methods needed for data preparation
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(emptyList()))
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(emptyList()))

        // Mock the exercise pool factory
        val samplePreparedData = createSamplePreparedData()
        whenever(
            exercisePoolFactory.createPoolFromPreparedData(any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(samplePreparedData.userExercisePool)

        // Mock the DAL methods for atomic workout generation
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(any(), any(), any(), any())
        ).thenReturn(Mono.just(WorkoutGenerationResult(PROGRAM_ID, 1, "max_effort", USER_ID, emptyList(), samplePreparedData)))
        whenever(atomicWorkoutWriter.writeWorkoutAtomically(any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(updatedProgram))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectNext(updatedProgram)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle program not found`() {
        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.error(RuntimeException("Program not found")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle one rep max DAL error`() {
        val program = createSampleProgram()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle program preferences DAL error`() {
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle weak muscles DAL error`() {
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle exercise pool factory error`() {
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        // whenever(exercisePoolFactory.createPoolForUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Pool creation error"))) // Method no longer exists

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle workout stage generation error`() {
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
                programId = any(),
                dayNumber = any(),
                dayType = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.error(RuntimeException("Stage generation error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle program update error`() {
        val program = createSampleProgram()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(4)).thenReturn(template)
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(
                programId = any(),
                dayNumber = any(),
                dayType = any(),
                preparedData = any()
            )
        ).thenReturn(Mono.just(WorkoutGenerationResult(PROGRAM_ID, 1, "max_effort", USER_ID, emptyList(), createSamplePreparedData())))
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.error(RuntimeException("Update error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle different program days per week`() {
        val program = createSampleProgram()
        val updatedProgram = program.copy(currentWeekNumber = 2, name = "Week 2")
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences().copy(programDaysPerWeek = 3)
        val userWeakMuscles = createSampleUserWeakMuscles()
        val template = createSampleTemplate()

        whenever(programService.selectProgramById(PROGRAM_ID)).thenReturn(Mono.just(program))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser(USER_ID)).thenReturn(Mono.just(userWeakMuscles))
        whenever(conjugateTemplates.selectTemplate(3)).thenReturn(template)

        // Mock the additional DAL methods needed for data preparation
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(emptyList()))
        whenever(exerciseWorkoutTypeDAL.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(emptyList()))
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(emptyList()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(emptyList()))

        // Mock the exercise pool factory
        val samplePreparedData = createSamplePreparedData()
        whenever(
            exercisePoolFactory.createPoolFromPreparedData(any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(samplePreparedData.userExercisePool)

        // Mock the DAL methods for atomic workout generation
        whenever(
            workoutStageGenerationOrchestrator.generateWorkoutStages(any(), any(), any(), any())
        ).thenReturn(Mono.just(WorkoutGenerationResult(PROGRAM_ID, 1, "max_effort", USER_ID, emptyList(), samplePreparedData)))
        whenever(atomicWorkoutWriter.writeWorkoutAtomically(any())).thenReturn(Mono.just(createSampleProgrammedWorkout()))
        whenever(programService.updateProgram(any(), any(), any(), any())).thenReturn(Mono.just(updatedProgram))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(PROGRAM_ID)

        StepVerifier.create(result)
            .expectNext(updatedProgram)
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

    private fun createSampleProgramPreferences(): ProgramPreferences {
        return ProgramPreferences(
            programId = PROGRAM_ID,
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

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        val sampleExercises = createSampleExercises()
        val sampleOneRepMaxes = createSampleOneRepMaxes()
        val sampleUserWeakMuscles = createSampleUserWeakMuscles()
        val sampleProgramPreferences = createSampleProgramPreferences()

        return WorkoutGenerationPreparedData(
            userExercisePool =
                UserExercisePool(
                    allExercises = sampleExercises,
                    preferences = emptyList(),
                    userEquipment = createSampleUserEquipment(),
                    exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                    exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                    previouslyUsedExercises = emptyList(),
                    userId = USER_ID
                ),
            oneRepMaxes = sampleOneRepMaxes,
            programPreferences = sampleProgramPreferences,
            weakMuscles = sampleUserWeakMuscles.map { it.muscleName },
            currentWeekNumber = 1,
            userId = USER_ID,
            weightUnitPreferences = mapOf("Bench Press" to com.congen.model.WeightUnit.LBS),
            exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
            exerciseWorkoutTypeMappings = createSampleExerciseWorkoutTypeMappings(),
            exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
            previouslyProgrammedExercises = emptyList(),
            allExercises = sampleExercises,
            userEquipment = createSampleUserEquipment(),
            userExercisePreferences = emptyList()
        )
    }

    private fun createSampleExercises(): List<com.congen.model.Exercise> {
        return listOf(
            com.congen.model.Exercise(
                name = "Bench Press",
                description = "Horizontal push exercise",
                movementType = com.congen.model.MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            ),
            com.congen.model.Exercise(
                name = "Squat",
                description = "Squat movement",
                movementType = com.congen.model.MovementType.SQUAT,
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            ),
            com.congen.model.Exercise(
                name = "Deadlift",
                description = "Hinge movement",
                movementType = com.congen.model.MovementType.HINGE,
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            ),
            com.congen.model.Exercise(
                name = "Incline Bench Press",
                description = "Inclined horizontal push",
                movementType = com.congen.model.MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )
        )
    }

    private fun createSampleUserEquipment(): List<com.congen.model.UserEquipment> {
        return listOf(
            com.congen.model.UserEquipment(USER_ID, "power bar", Instant.now()),
            com.congen.model.UserEquipment(USER_ID, "bench", Instant.now()),
            com.congen.model.UserEquipment(USER_ID, "squat rack", Instant.now())
        )
    }

    private fun createSampleExerciseEquipmentMappings(): Map<String, List<com.congen.model.ExerciseEquipment>> {
        return mapOf(
            "Bench Press" to listOf(com.congen.model.ExerciseEquipment("Bench Press", "power bar")),
            "Squat" to listOf(com.congen.model.ExerciseEquipment("Squat", "power bar")),
            "Deadlift" to listOf(com.congen.model.ExerciseEquipment("Deadlift", "power bar")),
            "Incline Bench Press" to listOf(com.congen.model.ExerciseEquipment("Incline Bench Press", "power bar"))
        )
    }

    private fun createSampleExerciseMuscleMappings(): Map<String, List<com.congen.model.ExerciseMuscle>> {
        return mapOf(
            "Bench Press" to
                listOf(
                    com.congen.model.ExerciseMuscle("Bench Press", "chest"),
                    com.congen.model.ExerciseMuscle("Bench Press", "triceps")
                ),
            "Squat" to
                listOf(
                    com.congen.model.ExerciseMuscle("Squat", "quadriceps"),
                    com.congen.model.ExerciseMuscle("Squat", "glutes")
                ),
            "Deadlift" to
                listOf(
                    com.congen.model.ExerciseMuscle("Deadlift", "hamstrings"),
                    com.congen.model.ExerciseMuscle("Deadlift", "glutes")
                ),
            "Incline Bench Press" to
                listOf(
                    com.congen.model.ExerciseMuscle("Incline Bench Press", "chest"),
                    com.congen.model.ExerciseMuscle("Incline Bench Press", "triceps")
                )
        )
    }

    private fun createSampleExerciseWorkoutTypeMappings(): Map<String, List<String>> {
        return mapOf(
            "Bench Press" to listOf("maximal_effort", "dynamic_effort"),
            "Squat" to listOf("maximal_effort", "dynamic_effort"),
            "Deadlift" to listOf("maximal_effort"),
            "Incline Bench Press" to listOf("maximal_effort", "accessory")
        )
    }
}
